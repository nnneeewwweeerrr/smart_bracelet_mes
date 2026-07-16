package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockOut;
import com.mes.service.StockOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-out")
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    @GetMapping("/list")
    public Result<IPage<StockOut>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String stockOutNo,
                                        @RequestParam(required = false) String materialNo,
                                        @RequestParam(required = false) String materialName,
                                        @RequestParam(required = false) String targetType,
                                        @RequestParam(required = false) String warehouse,
                                        @RequestParam(required = false) Integer status) {
        Page<StockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(stockOutNo != null && !stockOutNo.isEmpty(), StockOut::getStockOutNo, stockOutNo)
                .like(materialNo != null && !materialNo.isEmpty(), StockOut::getMaterialNo, materialNo)
                .like(materialName != null && !materialName.isEmpty(), StockOut::getMaterialName, materialName)
                .eq(targetType != null && !targetType.isEmpty(), StockOut::getTargetType, targetType)
                .eq(warehouse != null && !warehouse.isEmpty(), StockOut::getWarehouse, warehouse)
                .eq(status != null, StockOut::getStatus, status)
                .orderByDesc(StockOut::getOutDate);
        return Result.success(stockOutService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockOut> detail(@PathVariable Long id) {
        return Result.success(stockOutService.getById(id));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", stockOutService.count());
        stats.put("production", stockOutService.count(new LambdaQueryWrapper<StockOut>().eq(StockOut::getTargetType, "生产领料")));
        stats.put("sale", stockOutService.count(new LambdaQueryWrapper<StockOut>().eq(StockOut::getTargetType, "销售出库")));
        stats.put("transfer", stockOutService.count(new LambdaQueryWrapper<StockOut>().eq(StockOut::getTargetType, "调拨出库")));
        return Result.success(stats);
    }

    @PostMapping("/save")
    @OperationLog(module = "出库管理", type = "新增", description = "手动出库")
    public Result<Void> save(@RequestBody StockOut stockOut) {
        if (stockOut.getStatus() == null) {
            stockOut.setStatus(1);
        }
        if (stockOut.getOutDate() == null) {
            stockOut.setOutDate(LocalDateTime.now());
        }
        if (stockOut.getQuantity() != null && stockOut.getPrice() != null) {
            stockOut.setTotalAmount(stockOut.getQuantity().multiply(stockOut.getPrice()));
        }
        stockOutService.save(stockOut);
        return Result.success();
    }

    @PutMapping("/update")
    @OperationLog(module = "出库管理", type = "修改", description = "修改出库记录")
    public Result<Void> update(@RequestBody StockOut stockOut) {
        if (stockOut.getQuantity() != null && stockOut.getPrice() != null) {
            stockOut.setTotalAmount(stockOut.getQuantity().multiply(stockOut.getPrice()));
        }
        stockOutService.updateById(stockOut);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "出库管理", type = "删除", description = "删除出库记录")
    public Result<Void> delete(@PathVariable Long id) {
        stockOutService.removeById(id);
        return Result.success();
    }
}
