package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.DemoMaterial;
import com.mes.mapper.DemoMaterialMapper;
import com.mes.service.DemoMaterialService;
import org.springframework.stereotype.Service;

@Service
public class DemoMaterialServiceImpl extends ServiceImpl<DemoMaterialMapper, DemoMaterial> implements DemoMaterialService {
}