package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoEquipmentFault;
import com.mes.entity.DemoEquipment;
import com.mes.entity.DemoRepairTask;
import com.mes.service.DemoEquipmentFaultService;
import com.mes.service.DemoEquipmentService;
import com.mes.service.DemoRepairTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/equipment-fault")
public class DemoEquipmentFaultController {

    @Autowired
    private DemoEquipmentFaultService demoEquipmentFaultService;

    @Autowired
    private DemoEquipmentService demoEquipmentService;

    @Autowired
    private DemoRepairTaskService demoRepairTaskService;

    @GetMapping("/list")
    public Result<IPage<DemoEquipmentFault>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String faultNo,
                                                   @RequestParam(required = false) String equipmentNo,
                                                   @RequestParam(required = false) String equipmentName,
                                                   @RequestParam(required = false) String faultType,
                                                   @RequestParam(required = false) String faultLevel,
                                                   @RequestParam(required = false) Integer status) {
        Page<DemoEquipmentFault> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoEquipmentFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(faultNo != null && !faultNo.isEmpty(), DemoEquipmentFault::getFaultNo, faultNo)
                .like(equipmentNo != null && !equipmentNo.isEmpty(), DemoEquipmentFault::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), DemoEquipmentFault::getEquipmentName, equipmentName)
                .eq(faultType != null && !faultType.isEmpty(), DemoEquipmentFault::getFaultType, faultType)
                .eq(faultLevel != null && !faultLevel.isEmpty(), DemoEquipmentFault::getFaultLevel, faultLevel)
                .eq(status != null, DemoEquipmentFault::getStatus, status)
                .orderByDesc(DemoEquipmentFault::getReportTime);
        IPage<DemoEquipmentFault> result = demoEquipmentFaultService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoEquipmentFault> detail(@PathVariable Long id) {
        DemoEquipmentFault fault = demoEquipmentFaultService.getById(id);
        return Result.success(fault);
    }

    @GetMapping("/all")
    public Result<List<DemoEquipmentFault>> listAll() {
        List<DemoEquipmentFault> list = demoEquipmentFaultService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoEquipmentFaultService.count();
        long pending = demoEquipmentFaultService.count(new LambdaQueryWrapper<DemoEquipmentFault>().eq(DemoEquipmentFault::getStatus, 0));
        long assigned = demoEquipmentFaultService.count(new LambdaQueryWrapper<DemoEquipmentFault>().eq(DemoEquipmentFault::getStatus, 1));
        long repairing = demoEquipmentFaultService.count(new LambdaQueryWrapper<DemoEquipmentFault>().eq(DemoEquipmentFault::getStatus, 2));
        long fixed = demoEquipmentFaultService.count(new LambdaQueryWrapper<DemoEquipmentFault>().eq(DemoEquipmentFault::getStatus, 3));
        long closed = demoEquipmentFaultService.count(new LambdaQueryWrapper<DemoEquipmentFault>().eq(DemoEquipmentFault::getStatus, 4));

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("assigned", assigned);
        stats.put("repairing", repairing);
        stats.put("fixed", fixed);
        stats.put("closed", closed);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoEquipmentFault fault) {
        if (fault.getStatus() == null) {
            fault.setStatus(0);
        }
        if (fault.getReportTime() == null) {
            fault.setReportTime(LocalDateTime.now());
        }
        demoEquipmentFaultService.save(fault);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoEquipmentFault fault) {
        demoEquipmentFaultService.updateById(fault);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoEquipmentFaultService.removeById(id);
        return Result.success();
    }

    @PostMapping("/dispatch/{id}")
    public Result<Void> dispatch(@PathVariable Long id, @RequestBody Map<String, String> params) {
        DemoEquipmentFault fault = demoEquipmentFaultService.getById(id);
        if (fault == null) {
            return Result.error("故障记录不存在");
        }

        String assignee = params.get("assignee");
        if (assignee == null || assignee.isEmpty()) {
            return Result.error("派工人员不能为空");
        }

        fault.setAssignee(assignee);
        fault.setAssignTime(LocalDateTime.now());
        fault.setStatus(1);
        demoEquipmentFaultService.updateById(fault);

        DemoEquipment equipment = demoEquipmentService.getById(fault.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus(2);
            equipment.setCurrentFaultId(id);
            demoEquipmentService.updateById(equipment);
        }

        DemoRepairTask task = new DemoRepairTask();
        task.setTaskNo("RT" + System.currentTimeMillis());
        task.setFaultId(id);
        task.setEquipmentId(fault.getEquipmentId());
        task.setEquipmentNo(fault.getEquipmentNo());
        task.setEquipmentName(fault.getEquipmentName());
        task.setFaultDesc(fault.getFaultDesc());
        task.setFaultDescription(fault.getFaultDescription());
        task.setPriority("紧急".equals(fault.getFaultLevel()) ? 2 : "严重".equals(fault.getFaultLevel()) ? 1 : 0);
        task.setAssignee(assignee);
        task.setAssignTime(LocalDateTime.now());
        task.setStatus(0);
        demoRepairTaskService.save(task);

        fault.setRepairTaskId(task.getId());
        demoEquipmentFaultService.updateById(fault);

        return Result.success();
    }

    @PostMapping("/close/{id}")
    public Result<Void> close(@PathVariable Long id) {
        DemoEquipmentFault fault = demoEquipmentFaultService.getById(id);
        if (fault == null) {
            return Result.error("故障记录不存在");
        }

        fault.setStatus(4);
        demoEquipmentFaultService.updateById(fault);

        DemoEquipment equipment = demoEquipmentService.getById(fault.getEquipmentId());
        if (equipment != null) {
            equipment.setStatus(1);
            equipment.setCurrentFaultId(null);
            demoEquipmentService.updateById(equipment);
        }

        return Result.success();
    }
}