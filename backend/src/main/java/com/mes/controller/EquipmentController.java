package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.EquipmentInfo;
import com.mes.service.EquipmentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentInfoService equipmentInfoService;

    @GetMapping("/list")
    public Result<List<EquipmentInfo>> list() {
        LambdaQueryWrapper<EquipmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EquipmentInfo::getCreateTime);
        return Result.success(equipmentInfoService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<EquipmentInfo> detail(@PathVariable Long id) {
        return Result.success(equipmentInfoService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = equipmentInfoService.count();
        long running = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 1));
        long stopped = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 0));
        long repairing = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 2));
        
        long needMaintain = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>()
                .isNotNull(EquipmentInfo::getNextMaintainDate)
                .le(EquipmentInfo::getNextMaintainDate, LocalDateTime.now()));
        
        stats.put("total", total);
        stats.put("running", running);
        stats.put("stopped", stopped);
        stats.put("repairing", repairing);
        stats.put("needMaintain", needMaintain);
        
        return Result.success(stats);
    }

    @GetMapping("/status-list")
    public Result<List<Map<String, Object>>> statusList() {
        List<EquipmentInfo> list = equipmentInfoService.list(new LambdaQueryWrapper<EquipmentInfo>()
                .orderByDesc(EquipmentInfo::getCreateTime).last("LIMIT 20"));
        
        List<Map<String, Object>> result = list.stream().map(eq -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", eq.getId());
            map.put("equipmentCode", eq.getEquipmentCode());
            map.put("equipmentName", eq.getEquipmentName());
            map.put("equipmentType", eq.getEquipmentType());
            map.put("status", eq.getStatus());
            map.put("location", eq.getLocation());
            map.put("nextMaintainDate", eq.getNextMaintainDate());
            map.put("runningHours", eq.getRunningHours());
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @PostMapping("/save")
    @OperationLog(module = "设备档案", type = "新增", description = "新增设备")
    public Result<Void> save(@RequestBody EquipmentInfo equipmentInfo) {
        equipmentInfoService.save(equipmentInfo);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "设备档案", type = "修改", description = "修改设备")
    public Result<Void> update(@RequestBody EquipmentInfo equipmentInfo) {
        equipmentInfoService.updateById(equipmentInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "设备档案", type = "删除", description = "删除设备")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentInfoService.removeById(id);
        return Result.success();
    }
}