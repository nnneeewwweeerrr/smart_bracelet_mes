package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.CustomerInfo;
import com.mes.service.CustomerInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerInfoService customerInfoService;

    @GetMapping("/list")
    public Result<List<CustomerInfo>> list(@RequestParam(required = false) String customerName) {
        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(customerName != null && !customerName.isEmpty(), CustomerInfo::getCustomerName, customerName)
                .orderByDesc(CustomerInfo::getCreateTime);
        return Result.success(customerInfoService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<CustomerInfo> detail(@PathVariable Long id) {
        return Result.success(customerInfoService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "客户档案", type = "新增", description = "新增客户")
    public Result<Void> save(@RequestBody CustomerInfo customerInfo) {
        customerInfoService.save(customerInfo);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "客户档案", type = "修改", description = "修改客户")
    public Result<Void> update(@RequestBody CustomerInfo customerInfo) {
        customerInfoService.updateById(customerInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "客户档案", type = "删除", description = "删除客户")
    public Result<Void> delete(@PathVariable Long id) {
        customerInfoService.removeById(id);
        return Result.success();
    }
}
