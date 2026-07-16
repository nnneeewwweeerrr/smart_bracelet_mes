package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoStockAlert;
import com.mes.mapper.DemoStockAlertMapper;
import com.mes.service.DemoStockAlertService;
import org.springframework.stereotype.Service;

@Service
public class DemoStockAlertServiceImpl extends ServiceImpl<DemoStockAlertMapper, DemoStockAlert> implements DemoStockAlertService {
}