package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.ProcessInfo;
import com.mes.mapper.ProcessInfoMapper;
import com.mes.service.ProcessInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessInfoServiceImpl extends ServiceImpl<ProcessInfoMapper, ProcessInfo> implements ProcessInfoService {

    @Override
    public List<ProcessInfo> listByProductId(Long productId) {
        LambdaQueryWrapper<ProcessInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, ProcessInfo::getProductId, productId)
                .orderByAsc(ProcessInfo::getSortOrder);
        return list(wrapper);
    }
}
