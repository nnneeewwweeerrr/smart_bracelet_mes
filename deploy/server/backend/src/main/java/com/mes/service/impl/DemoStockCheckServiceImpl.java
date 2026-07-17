package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoStockCheck;
import com.mes.mapper.DemoStockCheckMapper;
import com.mes.service.DemoStockCheckService;
import org.springframework.stereotype.Service;

@Service
public class DemoStockCheckServiceImpl extends ServiceImpl<DemoStockCheckMapper, DemoStockCheck> implements DemoStockCheckService {
}