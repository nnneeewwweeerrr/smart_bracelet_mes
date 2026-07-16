package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.EquipmentFault;
import com.mes.service.EquipmentFaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment-fault")
public class EquipmentFaultController {

    @Autowired
    private EquipmentFaultService equipmentFaultService;

    @GetMapping("/list")
    public Result<IPage<EquipmentFault>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String faultNo,
                                               @RequestParam(required = false) String equipmentNo,
                                               @RequestParam(required = false) String faultType,
                                               @RequestParam(required = false) String faultLevel,
                                               @RequestParam(required = false) Integer status) {
        Page<EquipmentFault> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(faultNo != null && !faultNo.isEmpty(), EquipmentFault::getFaultNo, faultNo)
                .like(equipmentNo != null && !equipmentNo.isEmpty(), EquipmentFault::getEquipmentNo, equipmentNo)
                .eq(faultType != null && !faultType.isEmpty(), EquipmentFault::getFaultType, faultType)
                .eq(faultLevel != null && !faultLevel.isEmpty(), EquipmentFault::getFaultLevel, faultLevel)
                .eq(status != null, EquipmentFault::getStatus, status)
                .orderByDesc(EquipmentFault::getReportTime);
        return Result.success(equipmentFaultService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<EquipmentFault> detail(@PathVariable Long id) {
        return Result.success(equipmentFaultService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", equipmentFaultService.count());
        stats.put("pending", equipmentFaultService.count(new LambdaQueryWrapper<EquipmentFault>().eq(EquipmentFault::getStatus, 0)));
        stats.put("assigned", equipmentFaultService.count(new LambdaQueryWrapper<EquipmentFault>().eq(EquipmentFault::getStatus, 1)));
        stats.put("repairing", equipmentFaultService.count(new LambdaQueryWrapper<EquipmentFault>().eq(EquipmentFault::getStatus, 2)));
        stats.put("fixed", equipmentFaultService.count(new LambdaQueryWrapper<EquipmentFault>().eq(EquipmentFault::getStatus, 3)));
        stats.put("closed", equipmentFaultService.count(new LambdaQueryWrapper<EquipmentFault>().eq(EquipmentFault::getStatus, 4)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "设备故障", type = "新增", description = "新增故障单")
    public Result<Void> save(@RequestBody EquipmentFault fault) {
        if (fault.getStatus() == null) fault.setStatus(0);
        if (fault.getReportTime() == null) fault.setReportTime(LocalDateTime.now());
        equipmentFaultService.save(fault);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "设备故障", type = "修改", description = "修改故障单")
    public Result<Void> update(@RequestBody EquipmentFault fault) {
        equipmentFaultService.updateById(fault);
        return Result.success();
    }

    @PutMapping("/assign/{id}")
    @OperationLog(module = "设备故障", type = "派工", description = "派工")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, String> body) {
        EquipmentFault fault = equipmentFaultService.getById(id);
        if (fault == null) return Result.error("故障单不存在");
        fault.setStatus(1);
        fault.setAssignee(body.get("assignee"));
        fault.setAssignTime(LocalDateTime.now());
        equipmentFaultService.updateById(fault);
        return Result.success();
    }

    @PutMapping("/fix/{id}")
    @OperationLog(module = "设备故障", type = "修复", description = "修复故障")
    public Result<Void> fix(@PathVariable Long id, @RequestBody EquipmentFault fault) {
        fault.setStatus(2);
        equipmentFaultService.updateById(fault);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "设备故障", type = "删除", description = "删除故障单")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentFaultService.removeById(id);
        return Result.success();
    }
}