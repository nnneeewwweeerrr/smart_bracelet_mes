package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoStockOut;
import com.mes.mapper.DemoStockOutMapper;
import com.mes.service.DemoStockOutService;
import org.springframework.stereotype.Service;

@Service
public class DemoStockOutServiceImpl extends ServiceImpl<DemoStockOutMapper, DemoStockOut> implements DemoStockOutService {
}