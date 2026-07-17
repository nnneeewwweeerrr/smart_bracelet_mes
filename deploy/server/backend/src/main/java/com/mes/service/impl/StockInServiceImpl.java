package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.StockIn;
import com.mes.mapper.StockInMapper;
import com.mes.service.StockInService;
import com.mes.service.StockInfoService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockInServiceImpl extends ServiceImpl<StockInMapper, StockIn> implements StockInService {

    @Autowired
    private StockInfoService stockInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(StockIn stockIn) {
        stockIn.setInNo(BarcodeUtil.generateStockInNo());
        boolean saved = super.save(stockIn);
        if (saved) {
            stockInfoService.addStock(stockIn.getMaterialId(), stockIn.getWarehouseId(), stockIn.getQuantity());
        }
        return saved;
    }
}
