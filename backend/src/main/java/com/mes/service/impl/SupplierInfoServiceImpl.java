package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.SupplierInfo;
import com.mes.mapper.SupplierInfoMapper;
import com.mes.service.SupplierInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SupplierInfoServiceImpl extends ServiceImpl<SupplierInfoMapper, SupplierInfo> implements SupplierInfoService {

    @Override
    public List<SupplierInfo> listByName(String supplierName) {
        LambdaQueryWrapper<SupplierInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(supplierName), SupplierInfo::getSupplierName, supplierName)
                .orderByDesc(SupplierInfo::getCreateTime);
        return list(wrapper);
    }
}
