package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.EquipmentFault;
import com.mes.mapper.EquipmentFaultMapper;
import com.mes.service.EquipmentFaultService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentFaultServiceImpl extends ServiceImpl<EquipmentFaultMapper, EquipmentFault> implements EquipmentFaultService {
}