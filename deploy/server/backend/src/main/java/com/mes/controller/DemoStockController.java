package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoStock;
import com.mes.entity.DemoStockAlert;
import com.mes.service.DemoStockService;
import com.mes.service.DemoStockAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/stock")
public class DemoStockController {

    @Autowired
    private DemoStockService demoStockService;

    @Autowired
    private DemoStockAlertService demoStockAlertService;

    @GetMapping("/list")
    public Result<IPage<DemoStock>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String materialNo,
                                         @RequestParam(required = false) String materialName,
                                         @RequestParam(required = false) String warehouseCode,
                                         @RequestParam(required = false) String warehouseName) {
        Page<DemoStock> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(materialNo != null && !materialNo.isEmpty(), DemoStock::getMaterialNo, materialNo)
                .like(materialName != null && !materialName.isEmpty(), DemoStock::getMaterialName, materialName)
                .eq(warehouseCode != null && !warehouseCode.isEmpty(), DemoStock::getWarehouseCode, warehouseCode)
                .like(warehouseName != null && !warehouseName.isEmpty(), DemoStock::getWarehouseName, warehouseName)
                .orderByDesc(DemoStock::getUpdateTime);
        IPage<DemoStock> result = demoStockService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoStock> detail(@PathVariable Long id) {
        DemoStock stock = demoStockService.getById(id);
        return Result.success(stock);
    }

    @GetMapping("/all")
    public Result<List<DemoStock>> listAll() {
        List<DemoStock> list = demoStockService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoStockService.count();

        int totalQuantity = demoStockService.list().stream()
                .mapToInt(DemoStock::getQuantity)
                .sum();

        stats.put("total", total);
        stats.put("totalQuantity", totalQuantity);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoStock stock) {
        demoStockService.save(stock);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoStock stock) {
        demoStockService.updateById(stock);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoStockService.removeById(id);
        return Result.success();
    }

    @PostMapping("/check-alert")
    public Result<Void> checkAlert() {
        List<DemoStock> stocks = demoStockService.list();

        for (DemoStock stock : stocks) {
            Integer currentQty = stock.getQuantity() != null ? stock.getQuantity() : 0;

            if (currentQty < 100) {
                LambdaQueryWrapper<DemoStockAlert> alertWrapper = new LambdaQueryWrapper<>();
                alertWrapper.eq(DemoStockAlert::getMaterialId, stock.getMaterialId())
                        .eq(DemoStockAlert::getWarehouseId, stock.getWarehouseId())
                        .eq(DemoStockAlert::getStatus, 0);
                DemoStockAlert existing = demoStockAlertService.getOne(alertWrapper);

                if (existing == null) {
                    DemoStockAlert alert = new DemoStockAlert();
                    alert.setAlertNo("AL" + System.currentTimeMillis());
                    alert.setMaterialId(stock.getMaterialId());
                    alert.setMaterialNo(stock.getMaterialNo());
                    alert.setMaterialName(stock.getMaterialName());
                    alert.setWarehouseId(stock.getWarehouseId());
                    alert.setWarehouseCode(stock.getWarehouseCode());
                    alert.setWarehouseName(stock.getWarehouseName());
                    alert.setAlertType("低于安全库存");
                    alert.setCurrentQuantity(currentQty);
                    alert.setThreshold(100);
                    alert.setStatus(0);
                    demoStockAlertService.save(alert);
                }
            }
        }

        return Result.success();
    }
}