package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    public Result<IPage<CustomerInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String customerName,
                                             @RequestParam(required = false) String customerNo,
                                             @RequestParam(required = false) Integer status) {
        Page<CustomerInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(customerName != null && !customerName.isEmpty(), CustomerInfo::getCustomerName, customerName)
                .like(customerNo != null && !customerNo.isEmpty(), CustomerInfo::getCustomerNo, customerNo)
                .eq(status != null, CustomerInfo::getStatus, status)
                .orderByDesc(CustomerInfo::getCreateTime);
        IPage<CustomerInfo> result = customerInfoService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<CustomerInfo> detail(@PathVariable Long id) {
        CustomerInfo info = customerInfoService.getById(id);
        return Result.success(info);
    }

    @GetMapping("/all")
    public Result<List<CustomerInfo>> listAll() {
        List<CustomerInfo> list = customerInfoService.list();
        return Result.success(list);
    }

    @PostMapping("/save")
    @OperationLog(module = "客户档案", type = "新增", description = "新增客户")
    public Result<Void> save(@RequestBody CustomerInfo customerInfo) {
        if (customerInfo.getStatus() == null) {
            customerInfo.setStatus(1);
        }
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