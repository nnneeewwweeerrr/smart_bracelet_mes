package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.ProductInfo;
import com.mes.mapper.ProductInfoMapper;
import com.mes.service.ProductInfoService;
import org.springframework.stereotype.Service;

@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo> implements ProductInfoService {
}
