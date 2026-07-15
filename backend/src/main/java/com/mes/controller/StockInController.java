package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.MaterialInfo;
import com.mes.entity.StockIn;
import com.mes.entity.WarehouseInfo;
import com.mes.service.MaterialInfoService;
import com.mes.service.StockInService;
import com.mes.service.WarehouseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stock-in")
public class StockInController {

    @Autowired
    private StockInService stockInService;

    @Autowired
    private MaterialInfoService materialInfoService;

    @Autowired
    private WarehouseInfoService warehouseInfoService;

    @GetMapping("/list")
    public Result<IPage<StockIn>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String inNo,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String sourceType,
                                       @RequestParam(required = false) String type) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(inNo != null && !inNo.isEmpty(), StockIn::getInNo, inNo)
                .eq(status != null, StockIn::getStatus, status);
        
        String queryType = (type != null && !type.isEmpty()) ? type : sourceType;
        wrapper.eq(queryType != null && !queryType.isEmpty(), StockIn::getSourceType, queryType);
        
        wrapper.orderByDesc(StockIn::getCreateTime);
        IPage<StockIn> result = stockInService.page(page, wrapper);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    private void mapFields(StockIn info) {
        MaterialInfo material = materialInfoService.getById(info.getMaterialId());
        WarehouseInfo warehouse = warehouseInfoService.getById(info.getWarehouseId());
        
        info.setStockInNo(info.getInNo());
        info.setWarehouse(warehouse != null ? warehouse.getWarehouseName() : "");
        info.setRelatedNo(info.getSourceNo());
        info.setType(info.getSourceType());
        info.setItemCount(1);
        info.setTotalQuantity(info.getQuantity() != null ? info.getQuantity() : BigDecimal.ZERO);
        BigDecimal unitPrice = material != null && material.getUnitPrice() != null ? material.getUnitPrice() : BigDecimal.ZERO;
        info.setTotalAmount(unitPrice.multiply(info.getQuantity() != null ? info.getQuantity() : BigDecimal.ZERO));
    }

    @GetMapping("/{id}")
    public Result<StockIn> detail(@PathVariable Long id) {
        return Result.success(stockInService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "入库管理", type = "新增", description = "手动入库")
    public Result<Void> save(@RequestBody StockIn stockIn) {
        stockInService.save(stockIn);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    @OperationLog(module = "入库管理", type = "审批", description = "审批入库单")
    public Result<Void> approve(@PathVariable Long id) {
        StockIn stockIn = stockInService.getById(id);
        if (stockIn == null) {
            return Result.error("入库单不存在");
        }
        stockIn.setStatus(1);
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
