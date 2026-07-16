package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.UnqualifiedProduct;
import com.mes.mapper.UnqualifiedProductMapper;
import com.mes.service.UnqualifiedProductService;
import org.springframework.stereotype.Service;

@Service
public class UnqualifiedProductServiceImpl extends ServiceImpl<UnqualifiedProductMapper, UnqualifiedProduct> implements UnqualifiedProductService {
}