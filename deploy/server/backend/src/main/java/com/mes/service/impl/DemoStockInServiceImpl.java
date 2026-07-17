package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoStockIn;
import com.mes.mapper.DemoStockInMapper;
import com.mes.service.DemoStockInService;
import org.springframework.stereotype.Service;

@Service
public class DemoStockInServiceImpl extends ServiceImpl<DemoStockInMapper, DemoStockIn> implements DemoStockInService {
}