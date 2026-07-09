package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.StockInfo;

import java.math.BigDecimal;

public interface StockInfoService extends IService<StockInfo> {

    StockInfo getByMaterialAndWarehouse(Long materialId, Long warehouseId);

    void addStock(Long materialId, Long warehouseId, BigDecimal quantity);

    void reduceStock(Long materialId, Long warehouseId, BigDecimal quantity);
}
