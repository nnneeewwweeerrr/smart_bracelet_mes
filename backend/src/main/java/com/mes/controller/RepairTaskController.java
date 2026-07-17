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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/repair-task")
public class RepairTaskController {

    @Autowired
    private EquipmentRepairTaskService repairTaskService;

    @GetMapping("/list")
    public Result<IPage<EquipmentRepairTask>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String taskNo,
                                                    @RequestParam(required = false) String equipmentNo,
                                                    @RequestParam(required = false) String equipmentName,
                                                    @RequestParam(required = false) String faultType,
                                                    @RequestParam(required = false) String faultLevel,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Integer priority) {
        Page<EquipmentRepairTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentRepairTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(taskNo != null && !taskNo.isEmpty(), EquipmentRepairTask::getTaskNo, taskNo)
                .like(equipmentNo != null && !equipmentNo.isEmpty(), EquipmentRepairTask::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), EquipmentRepairTask::getEquipmentName, equipmentName)
                .eq(faultType != null && !faultType.isEmpty(), EquipmentRepairTask::getFaultType, faultType)
                .eq(faultLevel != null && !faultLevel.isEmpty(), EquipmentRepairTask::getFaultLevel, faultLevel)
                .eq(status != null, EquipmentRepairTask::getStatus, status)
                .eq(priority != null, EquipmentRepairTask::getPriority, priority)
                .orderByAsc(EquipmentRepairTask::getPriority)
                .orderByDesc(EquipmentRepairTask::getReportTime);
        return Result.success(repairTaskService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<EquipmentRepairTask> detail(@PathVariable Long id) {
        return Result.success(repairTaskService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repairTaskService.count());
        stats.put("pending", repairTaskService.count(new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 0)));
        stats.put("assigned", repairTaskService.count(new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 1)));
        stats.put("repairing", repairTaskService.count(new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 2)));
        stats.put("completed", repairTaskService.count(new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 3)));
        stats.put("accepted", repairTaskService.count(new LambdaQueryWrapper<EquipmentRepairTask>().eq(EquipmentRepairTask::getStatus, 4)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "维修任务", type = "新增", description = "新增维修任务")
    public Result<Void> save(@RequestBody EquipmentRepairTask task) {
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        if (task.getReportTime() == null) {
            task.setReportTime(LocalDateTime.now());
        }
        repairTaskService.save(task);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "维修任务", type = "修改", description = "修改维修任务")
    public Result<Void> update(@RequestBody EquipmentRepairTask task) {
        repairTaskService.updateById(task);
        return Result.success();
    }

    @PutMapping("/assign")
    @OperationLog(module = "维修任务", type = "派工", description = "派工")
    public Result<Void> assign(@RequestBody EquipmentRepairTask task) {
        task.setStatus(1);
        task.setAssignTime(LocalDateTime.now());
        repairTaskService.updateById(task);
        return Result.success();
    }

    @PostMapping("/accept/{id}")
    @OperationLog(module = "维修任务", type = "接单", description = "接单")
    public Result<Void> accept(@PathVariable Long id) {
        EquipmentRepairTask task = repairTaskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        task.setStatus(2);
        task.setAcceptTime(LocalDateTime.now());
        repairTaskService.updateById(task);
        return Result.success();
    }

    @PostMapping("/complete/{id}")
    @OperationLog(module = "维修任务", type = "完成", description = "完成维修")
    public Result<Void> complete(@PathVariable Long id, @RequestBody EquipmentRepairTask dto) {
        EquipmentRepairTask task = repairTaskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        task.setStatus(3);
        task.setCompleteTime(LocalDateTime.now());
        task.setRepairContent(dto.getRepairContent());
        task.setPartsUsed(dto.getPartsUsed());
        task.setRepairCost(dto.getRepairCost());
        task.setRemark(dto.getRemark());
        repairTaskService.updateById(task);
        return Result.success();
    }

    @PostMapping("/acceptance/{id}")
    @OperationLog(module = "维修任务", type = "验收", description = "验收维修任务")
    public Result<Void> acceptance(@PathVariable Long id, @RequestBody EquipmentRepairTask dto) {
        EquipmentRepairTask task = repairTaskService.getById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        task.setStatus(4);
        task.setAcceptanceResult(dto.getAcceptanceResult());
        task.setAcceptanceTime(LocalDateTime.now());
        task.setAcceptanceBy(dto.getAcceptanceBy());
        task.setRemark(dto.getRemark());
        repairTaskService.updateById(task);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "维修任务", type = "删除", description = "删除维修任务")
    public Result<Void> delete(@PathVariable Long id) {
        repairTaskService.removeById(id);
        return Result.success();
    }
}
