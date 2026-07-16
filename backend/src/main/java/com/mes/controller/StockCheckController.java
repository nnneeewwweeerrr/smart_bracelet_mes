package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockCheck;
import com.mes.service.StockCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-check")
public class StockCheckController {

    @Autowired
    private StockCheckService stockCheckService;

    @GetMapping("/list")
    public Result<IPage<StockCheck>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String checkNo,
                                          @RequestParam(required = false) String warehouse,
                                          @RequestParam(required = false) String checkType,
                                          @RequestParam(required = false) Integer status) {
        Page<StockCheck> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(checkNo != null && !checkNo.isEmpty(), StockCheck::getCheckNo, checkNo)
                .eq(warehouse != null && !warehouse.isEmpty(), StockCheck::getWarehouse, warehouse)
                .eq(checkType != null && !checkType.isEmpty(), StockCheck::getCheckType, checkType)
                .eq(status != null, StockCheck::getStatus, status)
                .orderByDesc(StockCheck::getCheckDate);
        return Result.success(stockCheckService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockCheck> detail(@PathVariable Long id) {
        return Result.success(stockCheckService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", stockCheckService.count());
        stats.put("completed", stockCheckService.count(new LambdaQueryWrapper<StockCheck>().eq(StockCheck::getStatus, 2)));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "库存盘点", type = "新增", description = "新增盘点单")
    public Result<Void> save(@RequestBody StockCheck stockCheck) {
        if (stockCheck.getStatus() == null) stockCheck.setStatus(0);
        stockCheckService.save(stockCheck);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "库存盘点", type = "修改", description = "修改盘点单")
    public Result<Void> update(@RequestBody StockCheck stockCheck) {
        stockCheckService.updateById(stockCheck);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @OperationLog(module = "库存盘点", type = "完成", description = "完成盘点")
    public Result<Void> complete(@PathVariable Long id) {
        StockCheck stockCheck = stockCheckService.getById(id);
        if (stockCheck == null) return Result.error("盘点单不存在");
        stockCheck.setStatus(2);
        stockCheckService.updateById(stockCheck);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "库存盘点", type = "删除", description = "删除盘点单")
    public Result<Void> delete(@PathVariable Long id) {
        stockCheckService.removeById(id);
        return Result.success();
    }
}