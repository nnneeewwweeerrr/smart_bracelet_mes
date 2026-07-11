package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.StockOut;
import com.mes.mapper.StockOutMapper;
import com.mes.service.StockOutService;
import com.mes.service.StockInfoService;
import com.mes.utils.BarcodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockOutServiceImpl extends ServiceImpl<StockOutMapper, StockOut> implements StockOutService {

    @Autowired
    private StockInfoService stockInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(StockOut stockOut) {
        stockInfoService.reduceStock(stockOut.getMaterialId(), stockOut.getWarehouseId(), stockOut.getQuantity());
        stockOut.setOutNo(BarcodeUtil.generateStockOutNo());
        return super.save(stockOut);
    }
}
