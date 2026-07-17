package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.StockInfo;
import com.mes.exception.BusinessException;
import com.mes.mapper.StockInfoMapper;
import com.mes.service.StockInfoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StockInfoServiceImpl extends ServiceImpl<StockInfoMapper, StockInfo> implements StockInfoService {

    @Override
    public StockInfo getByMaterialAndWarehouse(Long materialId, Long warehouseId) {
        LambdaQueryWrapper<StockInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockInfo::getMaterialId, materialId)
                .eq(StockInfo::getWarehouseId, warehouseId);
        return getOne(wrapper);
    }

    @Override
    public void addStock(Long materialId, Long warehouseId, BigDecimal quantity) {
        StockInfo stockInfo = getByMaterialAndWarehouse(materialId, warehouseId);
        if (stockInfo == null) {
            stockInfo = new StockInfo();
            stockInfo.setMaterialId(materialId);
            stockInfo.setWarehouseId(warehouseId);
            stockInfo.setQuantity(quantity);
            save(stockInfo);
        } else {
            stockInfo.setQuantity(stockInfo.getQuantity().add(quantity));
            updateById(stockInfo);
        }
    }

    @Override
    public void reduceStock(Long materialId, Long warehouseId, BigDecimal quantity) {
        StockInfo stockInfo = getByMaterialAndWarehouse(materialId, warehouseId);
        if (stockInfo == null || stockInfo.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("库存不足");
        }
        stockInfo.setQuantity(stockInfo.getQuantity().subtract(quantity));
        updateById(stockInfo);
    }
}
