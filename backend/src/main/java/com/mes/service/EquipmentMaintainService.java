package com.mes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.EquipmentMaintain;

import java.util.List;

public interface EquipmentMaintainService extends IService<EquipmentMaintain> {

    IPage<EquipmentMaintain> getPageList(int page, int size, Long equipmentId);

    List<EquipmentMaintain> getByEquipmentId(Long equipmentId);
}