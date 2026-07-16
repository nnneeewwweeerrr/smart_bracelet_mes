package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoStockIn;
import com.mes.entity.DemoStock;
import com.mes.service.DemoStockInService;
import com.mes.service.DemoStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/stock-in")
public class DemoStockInController {

    @Autowired
    private DemoStockInService demoStockInService;

    @Autowired
    private DemoStockService demoStockService;

    @GetMapping("/list")
    public Result<IPage<DemoStockIn>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String stockInNo,
                                            @RequestParam(required = false) String materialNo,
                                            @RequestParam(required = false) String materialName,
                                            @RequestParam(required = false) String warehouseCode,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(required = false) Integer status) {
        Page<DemoStockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoStockIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(stockInNo != null && !stockInNo.isEmpty(), DemoStockIn::getStockInNo, stockInNo)
                .like(materialNo != null && !materialNo.isEmpty(), DemoStockIn::getMaterialNo, materialNo)
                .like(materialName != null && !materialName.isEmpty(), DemoStockIn::getMaterialName, materialName)
                .eq(warehouseCode != null && !warehouseCode.isEmpty(), DemoStockIn::getWarehouseCode, warehouseCode)
                .eq(type != null && !type.isEmpty(), DemoStockIn::getType, type)
                .eq(status != null, DemoStockIn::getStatus, status)
                .orderByDesc(DemoStockIn::getInDate);
        IPage<DemoStockIn> result = demoStockInService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoStockIn> detail(@PathVariable Long id) {
        DemoStockIn stockIn = demoStockInService.getById(id);
        return Result.success(stockIn);
    }

    @GetMapping("/all")
    public Result<List<DemoStockIn>> listAll() {
        List<DemoStockIn> list = demoStockInService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoStockInService.count();
        long pending = demoStockInService.count(new LambdaQueryWrapper<DemoStockIn>().eq(DemoStockIn::getStatus, 0));
        long completed = demoStockInService.count(new LambdaQueryWrapper<DemoStockIn>().eq(DemoStockIn::getStatus, 1));
        long cancelled = demoStockInService.count(new LambdaQueryWrapper<DemoStockIn>().eq(DemoStockIn::getStatus, 2));

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoStockIn stockIn) {
        if (stockIn.getStatus() == null) {
            stockIn.setStatus(0);
        }
        if (stockIn.getInDate() == null) {
            stockIn.setInDate(LocalDateTime.now());
        }
        if (stockIn.getStockInNo() == null || stockIn.getStockInNo().isEmpty()) {
            stockIn.setStockInNo("IN" + System.currentTimeMillis());
        }
        if (stockIn.getTotalAmount() == null && stockIn.getQuantity() != null && stockIn.getUnitPrice() != null) {
            stockIn.setTotalAmount(new BigDecimal(stockIn.getQuantity()).multiply(stockIn.getUnitPrice()));
        }
        demoStockInService.save(stockIn);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoStockIn stockIn) {
        demoStockInService.updateById(stockIn);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoStockInService.removeById(id);
        return Result.success();
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        DemoStockIn stockIn = demoStockInService.getById(id);
        if (stockIn == null) {
            return Result.error("入库记录不存在");
        }

        stockIn.setStatus(1);
        demoStockInService.updateById(stockIn);

        if (stockIn.getMaterialId() != null) {
            LambdaQueryWrapper<DemoStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DemoStock::getMaterialId, stockIn.getMaterialId())
                    .eq(DemoStock::getWarehouseId, stockIn.getWarehouseId());
            DemoStock stock = demoStockService.getOne(wrapper);

            if (stock != null) {
                stock.setQuantity(stock.getQuantity() + stockIn.getQuantity());
                demoStockService.updateById(stock);
            } else {
                stock = new DemoStock();
                stock.setMaterialId(stockIn.getMaterialId());
                stock.setMaterialNo(stockIn.getMaterialNo());
                stock.setMaterialName(stockIn.getMaterialName());
                stock.setWarehouseId(stockIn.getWarehouseId());
                stock.setWarehouseCode(stockIn.getWarehouseCode());
                stock.setWarehouseName(stockIn.getWarehouseName());
                stock.setQuantity(stockIn.getQuantity());
                demoStockService.save(stock);
            }
        }

        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        DemoStockIn stockIn = demoStockInService.getById(id);
        if (stockIn == null) {
            return Result.error("入库记录不存在");
        }

        stockIn.setStatus(2);
        demoStockInService.updateById(stockIn);
        return Result.success();
    }

    @PostMapping("/scan")
    public Result<Void> scan(@RequestBody Map<String, Object> params) {
        String scanCode = (String) params.get("scanCode");
        Long materialId = params.get("materialId") != null ? Long.parseLong(params.get("materialId").toString()) : null;
        Long warehouseId = params.get("warehouseId") != null ? Long.parseLong(params.get("warehouseId").toString()) : null;
        Integer quantity = params.get("quantity") != null ? Integer.parseInt(params.get("quantity").toString()) : 1;
        String operator = (String) params.get("operator");
        String materialNo = (String) params.get("materialNo");
        String materialName = (String) params.get("materialName");
        String warehouseCode = (String) params.get("warehouseCode");
        String warehouseName = (String) params.get("warehouseName");

        DemoStockIn stockIn = new DemoStockIn();
        stockIn.setStockInNo("IN" + System.currentTimeMillis());
        stockIn.setType("采购入库");
        stockIn.setMaterialId(materialId);
        stockIn.setMaterialNo(materialNo);
        stockIn.setMaterialName(materialName);
        stockIn.setWarehouseId(warehouseId);
        stockIn.setWarehouseCode(warehouseCode);
        stockIn.setWarehouseName(warehouseName);
        stockIn.setQuantity(quantity);
        stockIn.setOperator(operator);
        stockIn.setInDate(LocalDateTime.now());
        stockIn.setScanCode(scanCode);
        stockIn.setStatus(1);
        demoStockInService.save(stockIn);

        if (materialId != null) {
            LambdaQueryWrapper<DemoStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DemoStock::getMaterialId, materialId)
                    .eq(DemoStock::getWarehouseId, warehouseId);
            DemoStock stock = demoStockService.getOne(wrapper);

            if (stock != null) {
                stock.setQuantity(stock.getQuantity() + quantity);
                demoStockService.updateById(stock);
            } else {
                stock = new DemoStock();
                stock.setMaterialId(materialId);
                stock.setMaterialNo(materialNo);
                stock.setMaterialName(materialName);
                stock.setWarehouseId(warehouseId);
                stock.setWarehouseCode(warehouseCode);
                stock.setWarehouseName(warehouseName);
                stock.setQuantity(quantity);
                demoStockService.save(stock);
            }
        }

        return Result.success();
    }
}