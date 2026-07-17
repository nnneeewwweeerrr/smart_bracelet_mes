package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.*;
import com.mes.mapper.ProductionReportMapper;
import com.mes.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ProductionReportServiceImpl extends ServiceImpl<ProductionReportMapper, ProductionReport> implements ProductionReportService {

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ProductionPlanService productionPlanService;

    @Autowired
    private ProcessParamStandardService processParamStandardService;

    @Autowired
    private ProcessMaterialBomService processMaterialBomService;

    @Autowired
    private MaterialInfoService materialInfoService;

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

    /**
     * 带双重防错校验的报工保存
     * 1. 物料校验：核对扫码物料是否属于当前工序BOM
     * 2. 参数校验：核对实际工艺参数是否在标准阈值内
     * 全部通过才允许生成报工记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveReportWithValidation(ProductionReport report, String materialCode, Map<String, Object> processParams) {
        // 基础校验
        if (report.getWorkOrderId() == null) {
            return "工单ID不能为空";
        }
        if (report.getProcessId() == null) {
            return "工序ID不能为空";
        }
        int good = report.getGoodQuantity() != null ? report.getGoodQuantity() : 0;
        int bad = report.getBadQuantity() != null ? report.getBadQuantity() : 0;
        if (good + bad <= 0) {
            return "报工数量必须大于0";
        }

        // ===== 物料校验 =====
        if (materialCode != null && !materialCode.isEmpty()) {
            LambdaQueryWrapper<MaterialInfo> matWrapper = new LambdaQueryWrapper<>();
            matWrapper.eq(MaterialInfo::getMaterialNo, materialCode);
            MaterialInfo material = materialInfoService.getOne(matWrapper);

            if (material == null) {
                return "物料校验失败：物料编码「" + materialCode + "」不存在";
            }

            LambdaQueryWrapper<ProcessMaterialBom> bomWrapper = new LambdaQueryWrapper<>();
            bomWrapper.eq(ProcessMaterialBom::getProcessId, report.getProcessId())
                    .eq(ProcessMaterialBom::getMaterialId, material.getId());
            ProcessMaterialBom bom = processMaterialBomService.getOne(bomWrapper);

            if (bom == null) {
                return "物料校验失败：物料「" + material.getMaterialName() + "」不属于当前工序的BOM范围，无法开工";
            }
        }

        // ===== 参数校验 =====
        if (processParams != null && !processParams.isEmpty()) {
            LambdaQueryWrapper<ProcessParamStandard> paramWrapper = new LambdaQueryWrapper<>();
            paramWrapper.eq(ProcessParamStandard::getProcessId, report.getProcessId())
                    .orderByAsc(ProcessParamStandard::getSortOrder);
            List<ProcessParamStandard> standards = processParamStandardService.list(paramWrapper);

            StringBuilder failReason = new StringBuilder();
            for (ProcessParamStandard std : standards) {
                Object actualVal = processParams.get(std.getParamCode());
                if (actualVal == null || actualVal.toString().isEmpty()) {
                    if (std.getIsRequired() != null && std.getIsRequired() == 1) {
                        if (failReason.length() > 0) failReason.append("；");
                        failReason.append("必检参数「").append(std.getParamName()).append("」未填写");
                    }
                    continue;
                }
                try {
                    double actual = Double.parseDouble(actualVal.toString());
                    double min = std.getMinValue() != null ? std.getMinValue().doubleValue() : Double.MIN_VALUE;
                    double max = std.getMaxValue() != null ? std.getMaxValue().doubleValue() : Double.MAX_VALUE;
                    String unit = std.getUnit() != null ? std.getUnit() : "";

                    if (actual < min || actual > max) {
                        if (failReason.length() > 0) failReason.append("；");
                        failReason.append(std.getParamName()).append("超差：实际值")
                                .append(actual).append(unit)
                                .append("，标准范围").append(min).append("~").append(max).append(unit);
                    }
                } catch (NumberFormatException e) {
                    if (failReason.length() > 0) failReason.append("；");
                    failReason.append(std.getParamName()).append("参数格式错误：").append(actualVal);
                }
            }

            if (failReason.length() > 0) {
                return "参数校验失败：" + failReason.toString();
            }
        }

        // 校验通过，保存报工记录
        boolean saved = save(report);
        if (!saved) {
            return "报工记录保存失败";
        }
        updateWorkOrderProgress(report.getWorkOrderId());
        return null; // null表示成功
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

    @Override
    public List<ProductionReport> listByWorkOrderId(Long workOrderId) {
        LambdaQueryWrapper<ProductionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductionReport::getWorkOrderId, workOrderId);
        return list(wrapper);
    }
}
