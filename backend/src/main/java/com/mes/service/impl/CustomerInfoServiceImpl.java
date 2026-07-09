package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.CustomerInfo;
import com.mes.mapper.CustomerInfoMapper;
import com.mes.service.CustomerInfoService;
import org.springframework.stereotype.Service;

@Service
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {
}
