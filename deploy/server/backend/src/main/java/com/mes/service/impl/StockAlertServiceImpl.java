package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.StockAlert;
import com.mes.mapper.StockAlertMapper;
import com.mes.service.StockAlertService;
import org.springframework.stereotype.Service;

@Service
public class StockAlertServiceImpl extends ServiceImpl<StockAlertMapper, StockAlert> implements StockAlertService {
}