package com.mes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mes.entity.EquipmentRepairTask;

import java.math.BigDecimal;

public interface EquipmentRepairTaskService extends IService<EquipmentRepairTask> {

    boolean assignTask(Long faultId, Long equipmentId, String faultDescription,
                       String priority, Long repairerId, String remark);

    boolean acceptTask(Long taskId, Long repairerId);

    boolean completeTask(Long taskId, String repairContent, String partsUsed,
                         BigDecimal repairCost, String remark);

    boolean acceptanceTask(Long taskId, String acceptanceResult, String acceptanceBy, String remark);
}
