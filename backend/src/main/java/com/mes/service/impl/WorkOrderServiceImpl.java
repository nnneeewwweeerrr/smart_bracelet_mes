package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.ProcessInfo;
import com.mes.entity.ProductionPlan;
import com.mes.entity.WorkOrder;
import com.mes.mapper.WorkOrderMapper;
import com.mes.service.ProcessInfoService;
import com.mes.service.ProductionPlanService;
import com.mes.service.WorkOrderService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    @Autowired
    private ProcessInfoService processInfoService;

    @Autowired
    private ProductionPlanService productionPlanService;

    @Override
    public IPage<WorkOrder> pageList(Page<WorkOrder> page, String workNo, Integer status, Long productId, Long planId) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(workNo), WorkOrder::getWorkNo, workNo)
                .eq(status != null, WorkOrder::getStatus, status)
                .eq(productId != null, WorkOrder::getProductId, productId)
                .eq(planId != null, WorkOrder::getPlanId, planId)
                .orderByDesc(WorkOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean dispatchWorkOrder(Long id) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            return false;
        }
        if (!Integer.valueOf(0).equals(workOrder.getStatus())) {
            return false;
        }
        workOrder.setStatus(1);
        return updateById(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeWorkOrder(Long id) {
        WorkOrder workOrder = getById(id);
        if (workOrder == null) {
            return false;
        }
        if (!Integer.valueOf(1).equals(workOrder.getStatus())) {
            return false;
        }
        workOrder.setStatus(2);
        updateById(workOrder);

        checkAndFinishPlan(workOrder.getPlanId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkOrder> generateFromPlan(Long planId) {
        ProductionPlan plan = productionPlanService.getById(planId);
        if (plan == null) {
            return new ArrayList<>();
        }

        List<ProcessInfo> processList = processInfoService.listByProductId(plan.getProductId());
        if (processList == null || processList.isEmpty()) {
            return new ArrayList<>();
        }

        List<WorkOrder> workOrders = new ArrayList<>();
        for (ProcessInfo process : processList) {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setWorkNo(BarcodeUtil.generateWorkOrderNo());
            workOrder.setPlanId(planId);
            workOrder.setProductId(plan.getProductId());
            workOrder.setProcessId(process.getId());
            workOrder.setQuantity(plan.getPlannedQuantity());
            workOrder.setStatus(0);
            workOrder.setCreateTime(LocalDateTime.now());
            workOrders.add(workOrder);
        }

        saveBatch(workOrders);
        return workOrders;
    }

    private void checkAndFinishPlan(Long planId) {
        if (planId == null) {
            return;
        }
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getPlanId, planId);
        List<WorkOrder> workOrders = list(wrapper);

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
