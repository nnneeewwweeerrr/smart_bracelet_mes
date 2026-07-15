package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.SupplierInfo;
import com.mes.service.SupplierInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    @Autowired
    private SupplierInfoService supplierInfoService;

    @GetMapping("/list")
    public Result<IPage<SupplierInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String supplierName,
                                             @RequestParam(required = false) Integer status) {
        Page<SupplierInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SupplierInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(supplierName != null && !supplierName.isEmpty(), SupplierInfo::getSupplierName, supplierName)
                .eq(status != null, SupplierInfo::getStatus, status)
                .orderByDesc(SupplierInfo::getCreateTime);
        IPage<SupplierInfo> result = supplierInfoService.page(page, wrapper);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SupplierInfo> detail(@PathVariable Long id) {
        SupplierInfo info = supplierInfoService.getById(id);
        if (info != null) {
            mapFields(info);
        }
        return Result.success(info);
    }

    private void mapFields(SupplierInfo info) {
        info.setSupplierNo("SUP" + String.format("%03d", info.getId()));
        info.setContact(info.getContactName());
        info.setEmail(info.getContactName() + "@example.com");
        info.setLevel("B");
    }

    @PostMapping("/save")
    @OperationLog(module = "供应商管理", type = "新增", description = "新增供应商")
    public Result<Void> save(@RequestBody SupplierInfo supplier) {
        if (supplier.getStatus() == null) {
            supplier.setStatus(1);
        }
        supplier.setCreateTime(LocalDateTime.now());
        supplierInfoService.save(supplier);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "供应商管理", type = "修改", description = "修改供应商")
    public Result<Void> update(@RequestBody SupplierInfo supplier) {
        supplierInfoService.updateById(supplier);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "供应商管理", type = "删除", description = "删除供应商")
    public Result<Void> delete(@PathVariable Long id) {
        supplierInfoService.removeById(id);
        return Result.success();
    }
}
