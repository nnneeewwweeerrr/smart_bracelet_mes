package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.EquipmentInfo;
import com.mes.mapper.EquipmentInfoMapper;
import com.mes.service.EquipmentInfoService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentInfoServiceImpl extends ServiceImpl<EquipmentInfoMapper, EquipmentInfo> implements EquipmentInfoService {
}
