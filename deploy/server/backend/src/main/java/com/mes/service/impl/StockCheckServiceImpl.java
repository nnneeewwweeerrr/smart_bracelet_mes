package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.StockCheck;
import com.mes.mapper.StockCheckMapper;
import com.mes.service.StockCheckService;
import org.springframework.stereotype.Service;

@Service
public class StockCheckServiceImpl extends ServiceImpl<StockCheckMapper, StockCheck> implements StockCheckService {
}