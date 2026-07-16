package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoEquipment;
import com.mes.mapper.DemoEquipmentMapper;
import com.mes.service.DemoEquipmentService;
import org.springframework.stereotype.Service;

@Service
public class DemoEquipmentServiceImpl extends ServiceImpl<DemoEquipmentMapper, DemoEquipment> implements DemoEquipmentService {
}