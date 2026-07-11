package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ProductInfo;
import com.mes.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductInfoService productInfoService;

    @GetMapping("/list")
    public Result<List<ProductInfo>> list(@RequestParam(required = false) String productName) {
        LambdaQueryWrapper<ProductInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(productName != null && !productName.isEmpty(), ProductInfo::getProductName, productName)
                .orderByDesc(ProductInfo::getCreateTime);
        return Result.success(productInfoService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<ProductInfo> detail(@PathVariable Long id) {
        return Result.success(productInfoService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "产品档案", type = "新增", description = "新增产品")
    public Result<Void> save(@RequestBody ProductInfo productInfo) {
        productInfoService.save(productInfo);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "产品档案", type = "修改", description = "修改产品")
    public Result<Void> update(@RequestBody ProductInfo productInfo) {
        productInfoService.updateById(productInfo);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "产品档案", type = "删除", description = "删除产品")
    public Result<Void> delete(@PathVariable Long id) {
        productInfoService.removeById(id);
        return Result.success();
    }
}
