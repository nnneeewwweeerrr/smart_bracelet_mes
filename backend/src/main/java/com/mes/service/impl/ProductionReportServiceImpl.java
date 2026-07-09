package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.ProductionReport;
import com.mes.entity.WorkOrder;
import com.mes.mapper.ProductionReportMapper;
import com.mes.service.ProductionReportService;
import com.mes.service.WorkOrderService;
import com.mes.service.ProductionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductionReportServiceImpl extends ServiceImpl<ProductionReportMapper, ProductionReport> implements ProductionReportService {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ProductionPlanService productionPlanService;

    @Override
    public IPage<ProductionReport> pageList(Page<ProductionReport> page, Long workOrderId, Long workerId) {
        LambdaQueryWrapper<ProductionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(workOrderId != null, ProductionReport::getWorkOrderId, workOrderId)
                .eq(workerId != null, ProductionReport::getWorkerId, workerId)
                .orderByDesc(ProductionReport::getReportDate);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveReport(ProductionReport report) {
        boolean saved = save(report);
        if (!saved) {
            return false;
        }

        updateWorkOrderProgress(report.getWorkOrderId());
        return true;
    }

    private void updateWorkOrderProgress(Long workOrderId) {
        if (workOrderId == null) {
            return;
        }

        WorkOrder workOrder = workOrderService.getById(workOrderId);
        if (workOrder == null) {
            return;
        }

        LambdaQueryWrapper<ProductionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductionReport::getWorkOrderId, workOrderId);
        List<ProductionReport> reports = list(wrapper);

        int totalQuantity = 0;
        if (reports != null && !reports.isEmpty()) {
            for (ProductionReport r : reports) {
                if (r.getGoodQuantity() != null) {
                    totalQuantity += r.getGoodQuantity();
                }
                if (r.getBadQuantity() != null) {
                    totalQuantity += r.getBadQuantity();
                }
            }
        }

        if (totalQuantity >= workOrder.getQuantity() && !Integer.valueOf(2).equals(workOrder.getStatus())) {
            workOrder.setStatus(2);
            workOrderService.updateById(workOrder);
            checkAndFinishPlan(workOrder.getPlanId());
        }
    }

    private void checkAndFinishPlan(Long planId) {
        if (planId == null) {
            return;
        }
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getPlanId, planId);
        List<WorkOrder> workOrders = workOrderService.list(wrapper);

        if (workOrders == null || workOrders.isEmpty()) {
            return;
        }

        boolean allFinished = workOrders.stream()
                .allMatch(wo -> Integer.valueOf(2).equals(wo.getStatus()));

        if (allFinished) {
            productionPlanService.finishPlan(planId);
        }
    }
}
