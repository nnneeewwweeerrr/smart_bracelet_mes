package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.WarehouseInfo;
import com.mes.mapper.WarehouseInfoMapper;
import com.mes.service.WarehouseInfoService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseInfoServiceImpl extends ServiceImpl<WarehouseInfoMapper, WarehouseInfo> implements WarehouseInfoService {
}
