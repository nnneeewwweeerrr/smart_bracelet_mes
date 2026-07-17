package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.EquipmentRepairTask;
import com.mes.service.EquipmentRepairTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment-repair-task")
public class EquipmentRepairTaskController {

    @Autowired
    private EquipmentRepairTaskService equipmentRepairTaskService;

    @GetMapping("/list")
    public Result<IPage<EquipmentRepairTask>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String taskNo,
                                                    @RequestParam(required = false) Long equipmentId,
                                                    @RequestParam(required = false) Long faultId,
                                                    @RequestParam(required = false) Long repairerId,
                                                    @RequestParam(required = false) String priority,
                                                    @RequestParam(required = false) Integer status) {
        Page<EquipmentRepairTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentRepairTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(taskNo != null && !taskNo.isEmpty(), EquipmentRepairTask::getTaskNo, taskNo)
                .eq(equipmentId != null, EquipmentRepairTask::getEquipmentId, equipmentId)
                .eq(faultId != null, EquipmentRepairTask::getFaultId, faultId)
                .eq(repairerId != null, EquipmentRepairTask::getRepairerId, repairerId)
                .eq(priority != null && !priority.isEmpty(), EquipmentRepairTask::getPriority, priority)
                .eq(status != null, EquipmentRepairTask::getStatus, status)
                .orderByDesc(EquipmentRepairTask::getAssignTime);
        return Result.success(equipmentRepairTaskService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<EquipmentRepairTask> detail(@PathVariable Long id) {
        return Result.success(equipmentRepairTaskService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", equipmentRepairTaskService.count());
        stats.put("pending", equipmentRepairTaskService.count(
                new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 0)));
        stats.put("assigned", equipmentRepairTaskService.count(
                new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 1)));
        stats.put("repairing", equipmentRepairTaskService.count(
                new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 2)));
        stats.put("completed", equipmentRepairTaskService.count(
                new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 3)));
        stats.put("accepted", equipmentRepairTaskService.count(
                new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 4)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "维修任务", type = "新增", description = "新增维修任务")
    public Result<Void> save(@RequestBody EquipmentRepairTask task) {
        if (task.getStatus() == null) task.setStatus(0);
        equipmentRepairTaskService.save(task);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "维修任务", type = "修改", description = "修改维修任务")
    public Result<Void> update(@RequestBody EquipmentRepairTask task) {
        equipmentRepairTaskService.updateById(task);
        return Result.success();
    }

    @PostMapping("/assign")
    @OperationLog(module = "维修任务", type = "派工", description = "派工创建维修任务")
    public Result<Void> assign(@RequestBody Map<String, Object> body) {
        Long faultId = body.get("faultId") != null ? Long.valueOf(body.get("faultId").toString()) : null;
        Long equipmentId = body.get("equipmentId") != null ? Long.valueOf(body.get("equipmentId").toString()) : null;
        String faultDescription = (String) body.get("faultDescription");
        String priority = (String) body.get("priority");
        Long repairerId = body.get("repairerId") != null ? Long.valueOf(body.get("repairerId").toString()) : null;
        String remark = (String) body.get("remark");

        boolean result = equipmentRepairTaskService.assignTask(
                faultId, equipmentId, faultDescription, priority, repairerId, remark);
        return result ? Result.success() : Result.error("派工失败");
    }

    @PutMapping("/accept/{id}")
    @OperationLog(module = "维修任务", type = "接单", description = "维修工接单")
    public Result<Void> accept(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long repairerId = null;
        if (body != null && body.get("repairerId") != null) {
            repairerId = Long.valueOf(body.get("repairerId").toString());
        }
        boolean result = equipmentRepairTaskService.acceptTask(id, repairerId);
        return result ? Result.success() : Result.error("接单失败");
    }

    @PutMapping("/complete/{id}")
    @OperationLog(module = "维修任务", type = "完成维修", description = "完成维修任务")
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String repairContent = (String) body.get("repairContent");
        String partsUsed = (String) body.get("partsUsed");
        BigDecimal repairCost = body.get("repairCost") != null ?
                new BigDecimal(body.get("repairCost").toString()) : BigDecimal.ZERO;
        String remark = (String) body.get("remark");

        boolean result = equipmentRepairTaskService.completeTask(
                id, repairContent, partsUsed, repairCost, remark);
        return result ? Result.success() : Result.error("提交失败");
    }

    @PutMapping("/acceptance/{id}")
    @OperationLog(module = "维修任务", type = "验收", description = "验收维修任务")
    public Result<Void> acceptance(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String acceptanceResult = (String) body.get("acceptanceResult");
        String acceptanceBy = (String) body.get("acceptanceBy");
        String remark = (String) body.get("remark");

        boolean result = equipmentRepairTaskService.acceptanceTask(
                id, acceptanceResult, acceptanceBy, remark);
        return result ? Result.success() : Result.error("验收失败");
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "维修任务", type = "删除", description = "删除维修任务")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentRepairTaskService.removeById(id);
        return Result.success();
    }
}
