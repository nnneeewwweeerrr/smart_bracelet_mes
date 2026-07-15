package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.annotation.OperationLog;
import com.mes.common.Result;
import com.mes.entity.StockInfo;
import com.mes.service.StockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockInfoService stockInfoService;

    @GetMapping("/list")
    public Result<List<StockInfo>> list(@RequestParam(required = false) Long materialId,
                                        @RequestParam(required = false) Long warehouseId) {
        LambdaQueryWrapper<StockInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(materialId != null, StockInfo::getMaterialId, materialId)
                .eq(warehouseId != null, StockInfo::getWarehouseId, warehouseId);
        return Result.success(stockInfoService.list(wrapper));
    }

    @GetMapping("/{id}")
    public Result<StockInfo> detail(@PathVariable Long id) {
        return Result.success(stockInfoService.getById(id));
    }

    @PostMapping("/take-stock")
    @OperationLog(module = "库存管理", type = "盘点", description = "库存盘点")
    public Result<Void> takeStock(@RequestBody Map<String, Object> body) {
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        for (Map<String, Object> item : items) {
            Long materialId = Long.valueOf(item.get("materialId").toString());
            BigDecimal quantity = new BigDecimal(item.get("quantity").toString());
            LambdaQueryWrapper<StockInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(StockInfo::getMaterialId, materialId)
                    .eq(StockInfo::getWarehouseId, warehouseId);
            StockInfo stockInfo = stockInfoService.getOne(wrapper);
            if (stockInfo != null) {
                stockInfo.setQuantity(quantity);
                stockInfoService.updateById(stockInfo);
            } else {
                StockInfo newStock = new StockInfo();
                newStock.setMaterialId(materialId);
                newStock.setWarehouseId(warehouseId);
                newStock.setQuantity(quantity);
                stockInfoService.save(newStock);
            }
        }
        return Result.success();
    }
}
