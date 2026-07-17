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
                                            @RequestParam(required = false) String productNo,
                                            @RequestParam(required = false) Integer status) {
        Page<ProductInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(productName != null && !productName.isEmpty(), ProductInfo::getProductName, productName)
                .like(productNo != null && !productNo.isEmpty(), ProductInfo::getProductNo, productNo)
                .eq(status != null, ProductInfo::getStatus, status)
                .orderByDesc(ProductInfo::getCreateTime);
        IPage<ProductInfo> result = productInfoService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ProductInfo> detail(@PathVariable Long id) {
        ProductInfo info = productInfoService.getById(id);
        return Result.success(info);
    }

    @GetMapping("/all")
    public Result<List<ProductInfo>> listAll() {
        List<ProductInfo> list = productInfoService.list();
        return Result.success(list);
    }

    @PostMapping("/save")
    @OperationLog(module = "产品档案", type = "新增", description = "新增产品")
    public Result<Void> save(@RequestBody ProductInfo productInfo) {
        if (productInfo.getStatus() == null) {
            productInfo.setStatus(1);
        }
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