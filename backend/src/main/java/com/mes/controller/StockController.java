package com.mes.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mes.common.Result;
import com.mes.entity.StockInfo;
import com.mes.service.StockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
