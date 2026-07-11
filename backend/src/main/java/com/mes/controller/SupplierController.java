package com.mes.controller;

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
    public Result<List<SupplierInfo>> list(@RequestParam(required = false) String supplierName) {
        return Result.success(supplierInfoService.listByName(supplierName));
    }

    @GetMapping("/{id}")
    public Result<SupplierInfo> detail(@PathVariable Long id) {
        return Result.success(supplierInfoService.getById(id));
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
