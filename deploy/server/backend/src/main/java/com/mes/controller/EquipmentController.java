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
                                              @RequestParam(required = false) String equipmentNo,
                                              @RequestParam(required = false) String equipmentName,
                                              @RequestParam(required = false) String equipmentType,
                                              @RequestParam(required = false) String workshop,
                                              @RequestParam(required = false) Integer status) {
        Page<EquipmentInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(equipmentNo != null && !equipmentNo.isEmpty(), EquipmentInfo::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), EquipmentInfo::getEquipmentName, equipmentName)
                .eq(equipmentType != null && !equipmentType.isEmpty(), EquipmentInfo::getEquipmentType, equipmentType)
                .eq(workshop != null && !workshop.isEmpty(), EquipmentInfo::getWorkshop, workshop)
                .eq(status != null, EquipmentInfo::getStatus, status)
                .orderByDesc(EquipmentInfo::getCreateTime);
        IPage<EquipmentInfo> result = equipmentInfoService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<EquipmentInfo> detail(@PathVariable Long id) {
        EquipmentInfo info = equipmentInfoService.getById(id);
        return Result.success(info);
    }

    @GetMapping("/all")
    public Result<List<EquipmentInfo>> listAll() {
        List<EquipmentInfo> list = equipmentInfoService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = equipmentInfoService.count();
        long running = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 1));
        long stopped = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 0));
        long repairing = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 2));
        long scrapped = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>().eq(EquipmentInfo::getStatus, 3));

        long needMaintain = equipmentInfoService.count(new LambdaQueryWrapper<EquipmentInfo>()
                .isNotNull(EquipmentInfo::getNextMaintainDate)
                .le(EquipmentInfo::getNextMaintainDate, LocalDateTime.now()));

        stats.put("total", total);
        stats.put("running", running);
        stats.put("stopped", stopped);
        stats.put("repairing", repairing);
        stats.put("scrapped", scrapped);
        stats.put("needMaintain", needMaintain);

        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "设备档案", type = "新增", description = "新增设备")
    public Result<Void> save(@RequestBody EquipmentInfo equipmentInfo) {
        if (equipmentInfo.getStatus() == null) {
            equipmentInfo.setStatus(1);
        }
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
