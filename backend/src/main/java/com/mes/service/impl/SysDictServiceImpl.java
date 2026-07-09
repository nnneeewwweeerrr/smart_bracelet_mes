package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.SysDict;
import com.mes.mapper.SysDictMapper;
import com.mes.service.SysDictService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    @Override
    public List<SysDict> listByType(String dictType) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictType, dictType)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getSortOrder);
        return list(wrapper);
    }
}
