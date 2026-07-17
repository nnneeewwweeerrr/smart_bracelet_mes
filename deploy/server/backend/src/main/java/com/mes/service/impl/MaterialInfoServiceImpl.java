package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.MaterialInfo;
import com.mes.mapper.MaterialInfoMapper;
import com.mes.service.MaterialInfoService;
import org.springframework.stereotype.Service;

@Service
public class MaterialInfoServiceImpl extends ServiceImpl<MaterialInfoMapper, MaterialInfo> implements MaterialInfoService {
}
