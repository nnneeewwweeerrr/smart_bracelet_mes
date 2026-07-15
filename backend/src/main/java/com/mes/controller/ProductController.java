package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.ProductInfo;
import com.mes.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductInfoService productInfoService;

    @GetMapping("/list")
    public Result<IPage<ProductInfo>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String productName,
                                            @RequestParam(required = false) String productCode,
                                            @RequestParam(required = false) Integer status) {
        Page<ProductInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(productName != null && !productName.isEmpty(), ProductInfo::getProductName, productName)
                .like(productCode != null && !productCode.isEmpty(), ProductInfo::getProductCode, productCode)
                .eq(status != null, ProductInfo::getStatus, status)
                .orderByDesc(ProductInfo::getCreateTime);
        IPage<ProductInfo> result = productInfoService.page(page, wrapper);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ProductInfo> detail(@PathVariable Long id) {
        ProductInfo info = productInfoService.getById(id);
        if (info != null) {
            mapFields(info);
        }
        return Result.success(info);
    }

    private void mapFields(ProductInfo info) {
        info.setProductNo(info.getProductCode());
        info.setModel(info.getSpecification());
        info.setPrice(info.getUnitPrice());
        info.setCategory(info.getProductType());
        info.setCost(info.getUnitPrice().multiply(new BigDecimal("0.7")));
        info.setUnit("台");
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
