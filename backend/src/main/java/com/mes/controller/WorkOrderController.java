package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.WorkOrder;
import com.mes.service.WorkOrderService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @GetMapping("/list")
    public Result<IPage<WorkOrder>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String workNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long planId) {
        Page<WorkOrder> page = new Page<>(current, size);
        return Result.success(workOrderService.pageList(page, workNo, status, productId, planId));
    }

    @GetMapping("/{id}")
    public Result<WorkOrder> detail(@PathVariable Long id) {
        return Result.success(workOrderService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "工单管理", type = "新增", description = "新增工单")
    public Result<Void> save(@RequestBody WorkOrder workOrder) {
        workOrder.setWorkNo(BarcodeUtil.generateWorkOrderNo());
        if (workOrder.getStatus() == null) {
            workOrder.setStatus(0);
        }
        workOrder.setCreateTime(LocalDateTime.now());
        workOrderService.save(workOrder);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "工单管理", type = "修改", description = "修改工单")
    public Result<Void> update(@RequestBody WorkOrder workOrder) {
        workOrderService.updateById(workOrder);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "工单管理", type = "删除", description = "删除工单")
    public Result<Void> delete(@PathVariable Long id) {
        workOrderService.removeById(id);
        return Result.success();
    }

    @PutMapping("/dispatch/{id}")
    @OperationLog(module = "工单管理", type = "派发", description = "派发工单")
    public Result<Void> dispatch(@PathVariable Long id) {
        boolean success = workOrderService.dispatchWorkOrder(id);
        if (!success) {
            return Result.error("工单状态不允许派发");
        }
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @OperationLog(module = "工单管理", type = "结案", description = "工单结案")
    public Result<Void> complete(@PathVariable Long id) {
        boolean success = workOrderService.completeWorkOrder(id);
        if (!success) {
            return Result.error("工单状态不允许结案");
        }
        return Result.success();
    }

    @PostMapping("/generate-from-plan/{planId}")
    @OperationLog(module = "工单管理", type = "批量生成", description = "从生产计划批量生成工单")
    public Result<List<WorkOrder>> generateFromPlan(@PathVariable Long planId) {
        List<WorkOrder> workOrders = workOrderService.generateFromPlan(planId);
        if (workOrders.isEmpty()) {
            return Result.error("生成工单失败，请检查生产计划和工艺路线");
        }
        return Result.success(workOrders);
    }
}
