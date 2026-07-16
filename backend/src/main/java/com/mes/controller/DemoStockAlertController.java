package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoStockAlert;
import com.mes.service.DemoStockAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/stock-alert")
public class DemoStockAlertController {

    @Autowired
    private DemoStockAlertService demoStockAlertService;

    @GetMapping("/list")
    public Result<IPage<DemoStockAlert>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String alertNo,
                                               @RequestParam(required = false) String materialNo,
                                               @RequestParam(required = false) String materialName,
                                               @RequestParam(required = false) String warehouse,
                                               @RequestParam(required = false) String warningType,
                                               @RequestParam(required = false) Integer status) {
        Page<DemoStockAlert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoStockAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(alertNo != null && !alertNo.isEmpty(), DemoStockAlert::getAlertNo, alertNo)
                .like(materialNo != null && !materialNo.isEmpty(), DemoStockAlert::getMaterialNo, materialNo)
                .like(materialName != null && !materialName.isEmpty(), DemoStockAlert::getMaterialName, materialName)
                .eq(warehouse != null && !warehouse.isEmpty(), DemoStockAlert::getWarehouseCode, warehouse)
                .eq(warningType != null && !warningType.isEmpty(), DemoStockAlert::getAlertType, "low".equals(warningType) ? "低于安全库存" : "高于最大库存")
                .eq(status != null, DemoStockAlert::getStatus, status)
                .orderByDesc(DemoStockAlert::getCreateTime);
        IPage<DemoStockAlert> result = demoStockAlertService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoStockAlert> detail(@PathVariable Long id) {
        DemoStockAlert alert = demoStockAlertService.getById(id);
        return Result.success(alert);
    }

    @GetMapping("/all")
    public Result<List<DemoStockAlert>> listAll() {
        List<DemoStockAlert> list = demoStockAlertService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoStockAlertService.count();
        long pendingCount = demoStockAlertService.count(new LambdaQueryWrapper<DemoStockAlert>().eq(DemoStockAlert::getStatus, 0));
        long processedCount = demoStockAlertService.count(new LambdaQueryWrapper<DemoStockAlert>().eq(DemoStockAlert::getStatus, 1));

        long lowStockCount = demoStockAlertService.count(new LambdaQueryWrapper<DemoStockAlert>().eq(DemoStockAlert::getAlertType, "低于安全库存"));
        long highStockCount = demoStockAlertService.count(new LambdaQueryWrapper<DemoStockAlert>().eq(DemoStockAlert::getAlertType, "高于最大库存"));
        long expire = demoStockAlertService.count(new LambdaQueryWrapper<DemoStockAlert>().eq(DemoStockAlert::getAlertType, "即将过期"));

        stats.put("total", total);
        stats.put("pendingCount", pendingCount);
        stats.put("processedCount", processedCount);
        stats.put("lowStockCount", lowStockCount);
        stats.put("highStockCount", highStockCount);
        stats.put("expire", expire);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoStockAlert alert) {
        if (alert.getStatus() == null) {
            alert.setStatus(0);
        }
        if (alert.getAlertNo() == null || alert.getAlertNo().isEmpty()) {
            alert.setAlertNo("AL" + System.currentTimeMillis());
        }
        demoStockAlertService.save(alert);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoStockAlert alert) {
        demoStockAlertService.updateById(alert);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoStockAlertService.removeById(id);
        return Result.success();
    }

    @PostMapping("/process/{id}")
    public Result<Void> process(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        DemoStockAlert alert = demoStockAlertService.getById(id);
        if (alert == null) {
            return Result.error("预警记录不存在");
        }

        alert.setStatus(1);
        alert.setHandleRemark((String) params.get("processRemark"));
        alert.setHandleTime(LocalDateTime.now());
        demoStockAlertService.updateById(alert);
        return Result.success();
    }
}