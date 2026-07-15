package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.MaterialInfo;
import com.mes.entity.StockOut;
import com.mes.entity.WarehouseInfo;
import com.mes.service.MaterialInfoService;
import com.mes.service.StockOutService;
import com.mes.service.WarehouseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stock-out")
public class StockOutController {

    @Autowired
    private StockOutService stockOutService;

    @Autowired
    private MaterialInfoService materialInfoService;

    @Autowired
    private WarehouseInfoService warehouseInfoService;

    @GetMapping("/list")
    public Result<IPage<StockOut>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String outNo,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(required = false) String targetType,
                                        @RequestParam(required = false) String type) {
        Page<StockOut> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockOut> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(outNo != null && !outNo.isEmpty(), StockOut::getOutNo, outNo)
                .eq(status != null, StockOut::getStatus, status);
        
        String queryType = (type != null && !type.isEmpty()) ? type : targetType;
        wrapper.eq(queryType != null && !queryType.isEmpty(), StockOut::getTargetType, queryType);
        
        wrapper.orderByDesc(StockOut::getCreateTime);
        IPage<StockOut> result = stockOutService.page(page, wrapper);
        result.getRecords().forEach(this::mapFields);
        return Result.success(result);
    }

    private void mapFields(StockOut info) {
        MaterialInfo material = materialInfoService.getById(info.getMaterialId());
        WarehouseInfo warehouse = warehouseInfoService.getById(info.getWarehouseId());
        
        info.setStockOutNo(info.getOutNo());
        info.setWarehouse(warehouse != null ? warehouse.getWarehouseName() : "");
        info.setRelatedNo(info.getTargetNo());
        info.setType(info.getTargetType());
        info.setItemCount(1);
        info.setTotalQuantity(info.getQuantity() != null ? info.getQuantity() : BigDecimal.ZERO);
        BigDecimal unitPrice = material != null && material.getUnitPrice() != null ? material.getUnitPrice() : BigDecimal.ZERO;
        info.setTotalAmount(unitPrice.multiply(info.getQuantity() != null ? info.getQuantity() : BigDecimal.ZERO));
    }

    @GetMapping("/{id}")
    public Result<StockOut> detail(@PathVariable Long id) {
        return Result.success(stockOutService.getById(id));
    }

    @PostMapping("/save")
    @OperationLog(module = "出库管理", type = "新增", description = "手动出库")
    public Result<Void> save(@RequestBody StockOut stockOut) {
        stockOutService.save(stockOut);
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    @OperationLog(module = "出库管理", type = "审批", description = "审批出库单")
    public Result<Void> approve(@PathVariable Long id) {
        StockOut stockOut = stockOutService.getById(id);
        if (stockOut == null) {
            return Result.error("出库单不存在");
        }
        stockOut.setStatus(1);
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
