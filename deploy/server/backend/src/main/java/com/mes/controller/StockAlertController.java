package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockAlert;
import com.mes.service.StockAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-alert")
public class StockAlertController {

    @Autowired
    private StockAlertService stockAlertService;

    @GetMapping("/list")
    public Result<IPage<StockAlert>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String alertNo,
                                          @RequestParam(required = false) String materialNo,
                                          @RequestParam(required = false) String alertType,
                                          @RequestParam(required = false) String alertLevel,
                                          @RequestParam(required = false) Integer status) {
        Page<StockAlert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(alertNo != null && !alertNo.isEmpty(), StockAlert::getAlertNo, alertNo)
                .like(materialNo != null && !materialNo.isEmpty(), StockAlert::getMaterialNo, materialNo)
                .eq(alertType != null && !alertType.isEmpty(), StockAlert::getAlertType, alertType)
                .eq(alertLevel != null && !alertLevel.isEmpty(), StockAlert::getAlertLevel, alertLevel)
                .eq(status != null, StockAlert::getStatus, status)
                .orderByDesc(StockAlert::getAlertTime);
        return Result.success(stockAlertService.page(page, wrapper));
    }

    @GetMapping("/pending")
    public Result<List<StockAlert>> pendingList() {
        return Result.success(stockAlertService.list(
                new LambdaQueryWrapper<StockAlert>().eq(StockAlert::getStatus, 0).orderByDesc(StockAlert::getAlertTime)));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", stockAlertService.count());
        stats.put("pending", stockAlertService.count(new LambdaQueryWrapper<StockAlert>().eq(StockAlert::getStatus, 0)));
        stats.put("processing", stockAlertService.count(new LambdaQueryWrapper<StockAlert>().eq(StockAlert::getStatus, 1)));
        stats.put("handled", stockAlertService.count(new LambdaQueryWrapper<StockAlert>().eq(StockAlert::getStatus, 2)));
        return Result.success(stats);
    }

    @PutMapping("/handle/{id}")
    @OperationLog(module = "库存预警", type = "处理", description = "处理预警")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, String> body) {
        StockAlert alert = stockAlertService.getById(id);
        if (alert == null) return Result.error("预警记录不存在");
        alert.setStatus(2);
        alert.setHandler(body.get("handler"));
        alert.setHandleTime(LocalDateTime.now());
        alert.setHandleResult(body.get("handleResult"));
        stockAlertService.updateById(alert);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @OperationLog(module = "库存预警", type = "删除", description = "删除预警")
    public Result<Void> delete(@PathVariable Long id) {
        stockAlertService.removeById(id);
        return Result.success();
    }
}