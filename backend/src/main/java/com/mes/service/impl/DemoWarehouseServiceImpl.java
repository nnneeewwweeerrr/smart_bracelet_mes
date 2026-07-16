package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoWarehouse;
import com.mes.mapper.DemoWarehouseMapper;
import com.mes.service.DemoWarehouseService;
import org.springframework.stereotype.Service;

@Service
public class DemoWarehouseServiceImpl extends ServiceImpl<DemoWarehouseMapper, DemoWarehouse> implements DemoWarehouseService {
}