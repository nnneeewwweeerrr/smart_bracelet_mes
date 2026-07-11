package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.EquipmentMaintain;
import com.mes.service.EquipmentMaintainService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/equipment-maintain")
public class EquipmentMaintainController {

    @Autowired
    private EquipmentMaintainService equipmentMaintainService;

    @GetMapping("/list")
    public Result<IPage<EquipmentMaintain>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long equipmentId) {
        return Result.success(equipmentMaintainService.getPageList(page, size, equipmentId));
    }

    @GetMapping("/equipment/{equipmentId}")
    public Result<List<EquipmentMaintain>> getByEquipment(@PathVariable Long equipmentId) {
        return Result.success(equipmentMaintainService.getByEquipmentId(equipmentId));
    }

    @GetMapping("/{id}")
    public Result<EquipmentMaintain> detail(@PathVariable Long id) {
        return Result.success(equipmentMaintainService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "设备保养", type = "新增", description = "新增保养记录")
    public Result<Void> save(@RequestBody EquipmentMaintain maintain) {
        if (maintain.getMaintainNo() == null || maintain.getMaintainNo().isEmpty()) {
            maintain.setMaintainNo(BarcodeUtil.generateMaintainNo());
        }
        if (maintain.getMaintainDate() == null) {
            maintain.setMaintainDate(LocalDateTime.now());
        }
        equipmentMaintainService.save(maintain);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "设备保养", type = "修改", description = "修改保养记录")
    public Result<Void> update(@RequestBody EquipmentMaintain maintain) {
        equipmentMaintainService.updateById(maintain);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "设备保养", type = "删除", description = "删除保养记录")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentMaintainService.removeById(id);
        return Result.success();
    }
}