package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoRepairTask;
import com.mes.entity.DemoEquipmentFault;
import com.mes.entity.DemoEquipmentMaintain;
import com.mes.service.DemoRepairTaskService;
import com.mes.service.DemoEquipmentFaultService;
import com.mes.service.DemoEquipmentMaintainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/repair-task")
public class DemoRepairTaskController {

    @Autowired
    private DemoRepairTaskService demoRepairTaskService;

    @Autowired
    private DemoEquipmentFaultService demoEquipmentFaultService;

    @Autowired
    private DemoEquipmentMaintainService demoEquipmentMaintainService;

    @GetMapping("/list")
    public Result<IPage<DemoRepairTask>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String taskNo,
                                               @RequestParam(required = false) String equipmentNo,
                                               @RequestParam(required = false) String equipmentName,
                                               @RequestParam(required = false) String assignee,
                                               @RequestParam(required = false) Integer status) {
        Page<DemoRepairTask> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoRepairTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(taskNo != null && !taskNo.isEmpty(), DemoRepairTask::getTaskNo, taskNo)
                .like(equipmentNo != null && !equipmentNo.isEmpty(), DemoRepairTask::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), DemoRepairTask::getEquipmentName, equipmentName)
                .like(assignee != null && !assignee.isEmpty(), DemoRepairTask::getAssignee, assignee)
                .eq(status != null, DemoRepairTask::getStatus, status)
                .orderByDesc(DemoRepairTask::getCreateTime);
        IPage<DemoRepairTask> result = demoRepairTaskService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoRepairTask> detail(@PathVariable Long id) {
        DemoRepairTask task = demoRepairTaskService.getById(id);
        return Result.success(task);
    }

    @GetMapping("/all")
    public Result<List<DemoRepairTask>> listAll() {
        List<DemoRepairTask> list = demoRepairTaskService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoRepairTaskService.count();
        long pending = demoRepairTaskService.count(new LambdaQueryWrapper<DemoRepairTask>().eq(DemoRepairTask::getStatus, 0));
        long assigned = demoRepairTaskService.count(new LambdaQueryWrapper<DemoRepairTask>().eq(DemoRepairTask::getStatus, 1));
        long repairing = demoRepairTaskService.count(new LambdaQueryWrapper<DemoRepairTask>().eq(DemoRepairTask::getStatus, 2));
        long pendingAcceptance = demoRepairTaskService.count(new LambdaQueryWrapper<DemoRepairTask>().eq(DemoRepairTask::getStatus, 3));
        long accepted = demoRepairTaskService.count(new LambdaQueryWrapper<DemoRepairTask>().eq(DemoRepairTask::getStatus, 4));

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("assigned", assigned);
        stats.put("repairing", repairing);
        stats.put("pendingAcceptance", pendingAcceptance);
        stats.put("accepted", accepted);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoRepairTask task) {
        if (task.getStatus() == null) {
            task.setStatus(0);
        }
        if (task.getAssignTime() == null) {
            task.setAssignTime(LocalDateTime.now());
        }
        demoRepairTaskService.save(task);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoRepairTask task) {
        demoRepairTaskService.updateById(task);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoRepairTaskService.removeById(id);
        return Result.success();
    }

    @PostMapping("/accept/{id}")
    public Result<Void> accept(@PathVariable Long id) {
        DemoRepairTask task = demoRepairTaskService.getById(id);
        if (task == null) {
            return Result.error("维修任务不存在");
        }

        task.setAcceptTime(LocalDateTime.now());
        task.setStatus(1);
        demoRepairTaskService.updateById(task);

        return Result.success();
    }

    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        DemoRepairTask task = demoRepairTaskService.getById(id);
        if (task == null) {
            return Result.error("维修任务不存在");
        }

        task.setStartTime(LocalDateTime.now());
        task.setStatus(2);
        demoRepairTaskService.updateById(task);

        DemoEquipmentFault fault = demoEquipmentFaultService.getById(task.getFaultId());
        if (fault != null) {
            fault.setStatus(2);
            demoEquipmentFaultService.updateById(fault);
        }

        return Result.success();
    }

    @PostMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        DemoRepairTask task = demoRepairTaskService.getById(id);
        if (task == null) {
            return Result.error("维修任务不存在");
        }

        task.setCompleteTime(LocalDateTime.now());
        task.setRepairContent((String) params.get("repairContent"));
        task.setSolution((String) params.get("solution"));
        task.setPartsUsed((String) params.get("partsUsed"));
        if (params.get("repairCost") != null) {
            task.setRepairCost(new java.math.BigDecimal(params.get("repairCost").toString()));
            task.setCost(task.getRepairCost());
        }
        task.setStatus(3);
        demoRepairTaskService.updateById(task);

        DemoEquipmentFault fault = demoEquipmentFaultService.getById(task.getFaultId());
        if (fault != null) {
            fault.setSolution((String) params.get("solution"));
            fault.setStatus(3);
            if (task.getRepairCost() != null) {
                fault.setCost(task.getRepairCost());
            }
            demoEquipmentFaultService.updateById(fault);
        }

        return Result.success();
    }

    @PostMapping("/acceptance/{id}")
    public Result<Void> acceptance(@PathVariable Long id, @RequestBody Map<String, String> params) {
        DemoRepairTask task = demoRepairTaskService.getById(id);
        if (task == null) {
            return Result.error("维修任务不存在");
        }

        String result = params.get("result");
        String acceptanceBy = params.get("acceptanceBy");

        task.setAcceptanceResult(result);
        task.setAcceptanceTime(LocalDateTime.now());
        task.setAcceptanceBy(acceptanceBy);
        task.setStatus("pass".equals(result) ? 4 : 2);
        demoRepairTaskService.updateById(task);

        if ("pass".equals(result)) {
            DemoEquipmentFault fault = demoEquipmentFaultService.getById(task.getFaultId());
            if (fault != null) {
                fault.setStatus(4);
                demoEquipmentFaultService.updateById(fault);
            }

            DemoEquipmentMaintain maintain = new DemoEquipmentMaintain();
            maintain.setMaintainNo("MT" + System.currentTimeMillis());
            maintain.setEquipmentId(task.getEquipmentId());
            maintain.setEquipmentNo(task.getEquipmentNo());
            maintain.setEquipmentName(task.getEquipmentName());
            maintain.setMaintainType("维修");
            maintain.setMaintainLevel("一级");
            maintain.setActualDate(LocalDateTime.now());
            maintain.setMaintainContent(task.getRepairContent());
            maintain.setMaintainResult("正常");
            maintain.setCost(task.getCost());
            maintain.setMaintainer(task.getAssignee());
            maintain.setRelatedTaskId(id);
            maintain.setStatus(2);
            demoEquipmentMaintainService.save(maintain);
        }

        return Result.success();
    }
}