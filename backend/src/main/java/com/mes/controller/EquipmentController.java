package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.EquipmentInfo;
import com.mes.service.EquipmentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
