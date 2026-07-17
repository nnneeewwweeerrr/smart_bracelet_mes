package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.UnqualifiedProduct;
import com.mes.service.UnqualifiedProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/unqualified")
public class UnqualifiedProductController {

    @Autowired
    private UnqualifiedProductService unqualifiedProductService;

    @GetMapping("/list")
    public Result<IPage<UnqualifiedProduct>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                   @RequestParam(required = false) String unqualifiedNo,
                                                   @RequestParam(required = false) String sourceType,
                                                   @RequestParam(required = false) String defectType,
                                                   @RequestParam(required = false) Integer status) {
        Page<UnqualifiedProduct> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<UnqualifiedProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(unqualifiedNo != null && !unqualifiedNo.isEmpty(), UnqualifiedProduct::getUnqualifiedNo, unqualifiedNo)
                .eq(sourceType != null && !sourceType.isEmpty(), UnqualifiedProduct::getSourceType, sourceType)
                .eq(defectType != null && !defectType.isEmpty(), UnqualifiedProduct::getDefectType, defectType)
                .eq(status != null, UnqualifiedProduct::getStatus, status)
                .orderByDesc(UnqualifiedProduct::getDiscoveryTime);
        return Result.success(unqualifiedProductService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<UnqualifiedProduct> detail(@PathVariable Long id) {
        return Result.success(unqualifiedProductService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", unqualifiedProductService.count());
        stats.put("pending", unqualifiedProductService.count(new LambdaQueryWrapper<UnqualifiedProduct>().eq(UnqualifiedProduct::getStatus, 0)));
        stats.put("completed", unqualifiedProductService.count(new LambdaQueryWrapper<UnqualifiedProduct>().eq(UnqualifiedProduct::getStatus, 2)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "不合格品管理", type = "新增", description = "新增不合格品")
    public Result<Void> save(@RequestBody UnqualifiedProduct product) {
        if (product.getStatus() == null) product.setStatus(0);
        unqualifiedProductService.save(product);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "不合格品管理", type = "修改", description = "修改不合格品")
    public Result<Void> update(@RequestBody UnqualifiedProduct product) {
        unqualifiedProductService.updateById(product);
        return Result.success();
    }

    @PutMapping("/handle/{id}")
    @OperationLog(module = "不合格品管理", type = "处置", description = "处置不合格品")
    public Result<Void> handle(@PathVariable Long id, @RequestBody UnqualifiedProduct product) {
        product.setStatus(2);
        unqualifiedProductService.updateById(product);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "不合格品管理", type = "删除", description = "删除不合格品")
    public Result<Void> delete(@PathVariable Long id) {
        unqualifiedProductService.removeById(id);
        return Result.success();
    }
}