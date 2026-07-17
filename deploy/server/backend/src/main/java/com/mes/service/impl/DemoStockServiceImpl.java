package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoStock;
import com.mes.mapper.DemoStockMapper;
import com.mes.service.DemoStockService;
import org.springframework.stereotype.Service;

@Service
public class DemoStockServiceImpl extends ServiceImpl<DemoStockMapper, DemoStock> implements DemoStockService {
}