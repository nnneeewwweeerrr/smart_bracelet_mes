package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.WarehouseInfo;
import com.mes.service.WarehouseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseInfoService warehouseInfoService;

    @GetMapping("/list")
    public Result<List<WarehouseInfo>> list() {
        return Result.success(warehouseInfoService.list());
    }

    @PostMapping("/save")
    @OperationLog(module = "仓库管理", type = "新增", description = "新增仓库")
    public Result<Void> save(@RequestBody WarehouseInfo warehouseInfo) {
        warehouseInfoService.save(warehouseInfo);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "仓库管理", type = "修改", description = "修改仓库")
    public Result<Void> update(@RequestBody WarehouseInfo warehouseInfo) {
        warehouseInfoService.updateById(warehouseInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "仓库管理", type = "删除", description = "删除仓库")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseInfoService.removeById(id);
        return Result.success();
    }
}
