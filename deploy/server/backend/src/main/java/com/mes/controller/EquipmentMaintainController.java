package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.EquipmentMaintain;
import com.mes.service.EquipmentMaintainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/equipment-maintain")
public class EquipmentMaintainController {

    @Autowired
    private EquipmentMaintainService equipmentMaintainService;

    @GetMapping("/list")
    public Result<IPage<EquipmentMaintain>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) String maintainNo,
                                                  @RequestParam(required = false) String equipmentNo,
                                                  @RequestParam(required = false) String equipmentName,
                                                  @RequestParam(required = false) String maintainType,
                                                  @RequestParam(required = false) Integer status) {
        Page<EquipmentMaintain> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentMaintain> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(maintainNo != null && !maintainNo.isEmpty(), EquipmentMaintain::getMaintainNo, maintainNo)
                .like(equipmentNo != null && !equipmentNo.isEmpty(), EquipmentMaintain::getEquipmentNo, equipmentNo)
                .like(equipmentName != null && !equipmentName.isEmpty(), EquipmentMaintain::getEquipmentName, equipmentName)
                .eq(maintainType != null && !maintainType.isEmpty(), EquipmentMaintain::getMaintainType, maintainType)
                .eq(status != null, EquipmentMaintain::getStatus, status)
                .orderByDesc(EquipmentMaintain::getCreateTime);
        return Result.success(equipmentMaintainService.page(page, wrapper));
    }

    @GetMapping("/equipment/{equipmentId}")
    public Result<java.util.List<EquipmentMaintain>> getByEquipment(@PathVariable Long equipmentId) {
        return Result.success(equipmentMaintainService.list(
                new LambdaQueryWrapper<EquipmentMaintain>().eq(EquipmentMaintain::getEquipmentId, equipmentId)
                        .orderByDesc(EquipmentMaintain::getActualDate)));
    }

    @GetMapping("/{id}")
    public Result<EquipmentMaintain> detail(@PathVariable Long id) {
        return Result.success(equipmentMaintainService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "设备保养", type = "新增", description = "新增保养记录")
    public Result<Void> save(@RequestBody EquipmentMaintain maintain) {
        if (maintain.getStatus() == null) {
            maintain.setStatus(1);
        }
        if (maintain.getActualDate() == null) {
            maintain.setActualDate(LocalDateTime.now());
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
