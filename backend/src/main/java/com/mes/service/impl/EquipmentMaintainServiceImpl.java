package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.EquipmentMaintain;
import com.mes.mapper.EquipmentMaintainMapper;
import com.mes.service.EquipmentMaintainService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentMaintainServiceImpl extends ServiceImpl<EquipmentMaintainMapper, EquipmentMaintain> implements EquipmentMaintainService {

    @Override
    public IPage<EquipmentMaintain> getPageList(int page, int size, Long equipmentId) {
        LambdaQueryWrapper<EquipmentMaintain> wrapper = new LambdaQueryWrapper<>();
        if (equipmentId != null) {
            wrapper.eq(EquipmentMaintain::getEquipmentId, equipmentId);
        }
        wrapper.orderByDesc(EquipmentMaintain::getActualDate);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<EquipmentMaintain> getByEquipmentId(Long equipmentId) {
        LambdaQueryWrapper<EquipmentMaintain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EquipmentMaintain::getEquipmentId, equipmentId);
        wrapper.orderByDesc(EquipmentMaintain::getActualDate);
        return list(wrapper);
    }
}
