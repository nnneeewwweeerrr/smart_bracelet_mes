package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.common.Result;
import com.mes.entity.EquipmentFault;

import java.util.Map;

public interface EquipmentFaultService extends IService<EquipmentFault> {

    Result<Map<String, Object>> getPage(Long pageNum, Long pageSize, String faultNo, Integer status, Long equipmentId);

    Result<Void> assignFault(Long faultId, Long assigneeId, String assignType);

    Result<Void> autoAssign(Long faultId);

    Result<Void> handleFault(Long faultId, String handler, String solution, Integer status, Double repairHours);

    Result<Map<String, Object>> getMyTasks(Long assigneeId, Integer status);
}