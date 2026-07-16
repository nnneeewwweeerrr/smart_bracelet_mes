package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockIn;
import com.mes.service.StockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-in")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    @GetMapping("/list")
    public Result<IPage<StockIn>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String stockInNo,
                                       @RequestParam(required = false) String materialNo,
                                       @RequestParam(required = false) String materialName,
                                       @RequestParam(required = false) String sourceType,
                                       @RequestParam(required = false) String warehouse,
                                       @RequestParam(required = false) Integer status) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(stockInNo != null && !stockInNo.isEmpty(), StockIn::getStockInNo, stockInNo)
                .like(materialNo != null && !materialNo.isEmpty(), StockIn::getMaterialNo, materialNo)
                .like(materialName != null && !materialName.isEmpty(), StockIn::getMaterialName, materialName)
                .eq(sourceType != null && !sourceType.isEmpty(), StockIn::getSourceType, sourceType)
                .eq(warehouse != null && !warehouse.isEmpty(), StockIn::getWarehouse, warehouse)
                .eq(status != null, StockIn::getStatus, status)
                .orderByDesc(StockIn::getInDate);
        return Result.success(stockInService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockIn> detail(@PathVariable Long id) {
        return Result.success(stockInService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", stockInService.count());
        stats.put("purchase", stockInService.count(new LambdaQueryWrapper<StockIn>().eq(StockIn::getSourceType, "采购入库")));
        stats.put("production", stockInService.count(new LambdaQueryWrapper<StockIn>().eq(StockIn::getSourceType, "生产入库")));
        stats.put("return", stockInService.count(new LambdaQueryWrapper<StockIn>().eq(StockIn::getSourceType, "退货入库")));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "入库管理", type = "新增", description = "手动入库")
    public Result<Void> save(@RequestBody StockIn stockIn) {
        if (stockIn.getStatus() == null) {
            stockIn.setStatus(1);
        }
        if (stockIn.getInDate() == null) {
            stockIn.setInDate(LocalDateTime.now());
        }
        if (stockIn.getQuantity() != null && stockIn.getPrice() != null) {
            stockIn.setTotalAmount(stockIn.getQuantity().multiply(stockIn.getPrice()));
        }
        stockInService.save(stockIn);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "入库管理", type = "修改", description = "修改入库记录")
    public Result<Void> update(@RequestBody StockIn stockIn) {
        if (stockIn.getQuantity() != null && stockIn.getPrice() != null) {
            stockIn.setTotalAmount(stockIn.getQuantity().multiply(stockIn.getPrice()));
        }
        stockInService.updateById(stockIn);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "入库管理", type = "删除", description = "删除入库记录")
    public Result<Void> delete(@PathVariable Long id) {
        stockInService.removeById(id);
        return Result.success();
    }
}
