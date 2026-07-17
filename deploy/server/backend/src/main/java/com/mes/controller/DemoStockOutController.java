package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.common.Result;
import com.mes.entity.DemoStockOut;
import com.mes.entity.DemoStock;
import com.mes.service.DemoStockOutService;
import com.mes.service.DemoStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demo/stock-out")
public class DemoStockOutController {

    @Autowired
    private DemoStockOutService demoStockOutService;

    @Autowired
    private DemoStockService demoStockService;

    @GetMapping("/list")
    public Result<IPage<DemoStockOut>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) String stockOutNo,
                                             @RequestParam(required = false) String materialNo,
                                             @RequestParam(required = false) String materialName,
                                             @RequestParam(required = false) String warehouseCode,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) Integer status) {
        Page<DemoStockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DemoStockOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(stockOutNo != null && !stockOutNo.isEmpty(), DemoStockOut::getStockOutNo, stockOutNo)
                .like(materialNo != null && !materialNo.isEmpty(), DemoStockOut::getMaterialNo, materialNo)
                .like(materialName != null && !materialName.isEmpty(), DemoStockOut::getMaterialName, materialName)
                .eq(warehouseCode != null && !warehouseCode.isEmpty(), DemoStockOut::getWarehouseCode, warehouseCode)
                .eq(type != null && !type.isEmpty(), DemoStockOut::getType, type)
                .eq(status != null, DemoStockOut::getStatus, status)
                .orderByDesc(DemoStockOut::getOutDate);
        IPage<DemoStockOut> result = demoStockOutService.page(page, wrapper);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<DemoStockOut> detail(@PathVariable Long id) {
        DemoStockOut stockOut = demoStockOutService.getById(id);
        return Result.success(stockOut);
    }

    @GetMapping("/all")
    public Result<List<DemoStockOut>> listAll() {
        List<DemoStockOut> list = demoStockOutService.list();
        return Result.success(list);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = new HashMap<>();

        long total = demoStockOutService.count();
        long pending = demoStockOutService.count(new LambdaQueryWrapper<DemoStockOut>().eq(DemoStockOut::getStatus, 0));
        long completed = demoStockOutService.count(new LambdaQueryWrapper<DemoStockOut>().eq(DemoStockOut::getStatus, 1));
        long cancelled = demoStockOutService.count(new LambdaQueryWrapper<DemoStockOut>().eq(DemoStockOut::getStatus, 2));

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);

        return Result.success(stats);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody DemoStockOut stockOut) {
        if (stockOut.getStatus() == null) {
            stockOut.setStatus(0);
        }
        if (stockOut.getOutDate() == null) {
            stockOut.setOutDate(LocalDateTime.now());
        }
        if (stockOut.getStockOutNo() == null || stockOut.getStockOutNo().isEmpty()) {
            stockOut.setStockOutNo("OUT" + System.currentTimeMillis());
        }
        if (stockOut.getTotalAmount() == null && stockOut.getQuantity() != null && stockOut.getUnitPrice() != null) {
            stockOut.setTotalAmount(new BigDecimal(stockOut.getQuantity()).multiply(stockOut.getUnitPrice()));
        }
        demoStockOutService.save(stockOut);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemoStockOut stockOut) {
        demoStockOutService.updateById(stockOut);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demoStockOutService.removeById(id);
        return Result.success();
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        DemoStockOut stockOut = demoStockOutService.getById(id);
        if (stockOut == null) {
            return Result.error("出库记录不存在");
        }

        if (stockOut.getMaterialId() != null) {
            LambdaQueryWrapper<DemoStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DemoStock::getMaterialId, stockOut.getMaterialId())
                    .eq(DemoStock::getWarehouseId, stockOut.getWarehouseId());
            DemoStock stock = demoStockService.getOne(wrapper);

            if (stock == null) {
                return Result.error("库存不存在");
            }

            if (stock.getQuantity() < stockOut.getQuantity()) {
                return Result.error("库存不足");
            }

            stock.setQuantity(stock.getQuantity() - stockOut.getQuantity());
            demoStockService.updateById(stock);
        }

        stockOut.setStatus(1);
        demoStockOutService.updateById(stockOut);

        return Result.success();
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        DemoStockOut stockOut = demoStockOutService.getById(id);
        if (stockOut == null) {
            return Result.error("出库记录不存在");
        }

        stockOut.setStatus(2);
        demoStockOutService.updateById(stockOut);
        return Result.success();
    }
}