package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public Result<IPage<EquipmentInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String equipmentCode,
                                              @RequestParam(required = false) String equipmentName,
                                              @RequestParam(required = false) String equipmentType,
                                              @RequestParam(required = false) Integer status) {
        Page<EquipmentInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(equipmentCode != null && !equipmentCode.isEmpty(), EquipmentInfo::getEquipmentCode, equipmentCode)
                .like(equipmentName != null && !equipmentName.isEmpty(), EquipmentInfo::getEquipmentName, equipmentName)
                .eq(equipmentType != null && !equipmentType.isEmpty(), EquipmentInfo::getEquipmentType, equipmentType)
                .eq(status != null, EquipmentInfo::getStatus, status)
                .orderByDesc(EquipmentInfo::getCreateTime);
        IPage<EquipmentInfo> result = equipmentInfoService.page(page, wrapper);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<EquipmentInfo> detail(@PathVariable Long id) {
        EquipmentInfo info = equipmentInfoService.getById(id);
        if (info != null) {
            mapFields(info);
        }
        return Result.success(info);
    }

    private void mapFields(EquipmentInfo info) {
        info.setEquipmentNo(info.getEquipmentCode());
        info.setName(info.getEquipmentName());
        info.setType(info.getEquipmentType());
        info.setWorkshop(info.getLocation());
        info.setManager("管理员");
        info.setPhone("13800000000");
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