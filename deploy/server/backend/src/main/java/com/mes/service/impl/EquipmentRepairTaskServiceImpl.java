package com.mes.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.entity.EquipmentFault;
import com.mes.entity.EquipmentInfo;
import com.mes.entity.EquipmentRepairTask;
import com.mes.mapper.EquipmentRepairTaskMapper;
import com.mes.service.EquipmentFaultService;
import com.mes.service.EquipmentInfoService;
import com.mes.service.EquipmentRepairTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EquipmentRepairTaskServiceImpl extends ServiceImpl<EquipmentRepairTaskMapper, EquipmentRepairTask> implements EquipmentRepairTaskService {

    @Autowired
    private EquipmentFaultService equipmentFaultService;

    @Autowired
    private EquipmentInfoService equipmentInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTask(Long faultId, Long equipmentId, String faultDescription,
                              String priority, Long repairerId, String remark) {
        String taskNo = "RT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        EquipmentRepairTask task = new EquipmentRepairTask();
        task.setTaskNo(taskNo);
        task.setFaultId(faultId);
        task.setEquipmentId(equipmentId);
        task.setFaultDescription(faultDescription);
        task.setFaultDesc(faultDescription);
        if (priority != null) {
            switch (priority) {
                case "major": task.setPriority(2); break;
                case "minor": task.setPriority(1); break;
                case "warning": task.setPriority(0); break;
                default: task.setPriority(1);
            }
        } else {
            task.setPriority(1);
        }
        task.setRepairerId(repairerId);
        task.setAssignTime(LocalDateTime.now());
        task.setStatus(1);
        task.setRemark(remark);
        task.setRepairCost(BigDecimal.ZERO);
        boolean saved = save(task);
        if (!saved) return false;

        if (faultId != null) {
            EquipmentFault fault = equipmentFaultService.getById(faultId);
            if (fault != null) {
                fault.setStatus(1);
                fault.setRepairTaskId(task.getId());
                equipmentFaultService.updateById(fault);
            }
        }

        if (equipmentId != null) {
            EquipmentInfo equipment = equipmentInfoService.getById(equipmentId);
            if (equipment != null) {
                equipment.setStatus(2);
                equipment.setCurrentFaultId(faultId);
                equipmentInfoService.updateById(equipment);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptTask(Long taskId, Long repairerId) {
        EquipmentRepairTask task = getById(taskId);
        if (task == null) return false;

        task.setStatus(2);
        task.setAcceptTime(LocalDateTime.now());
        task.setStartTime(LocalDateTime.now());
        if (repairerId != null) {
            task.setRepairerId(repairerId);
        }
        boolean updated = updateById(task);
        if (!updated) return false;

        if (task.getFaultId() != null) {
            EquipmentFault fault = equipmentFaultService.getById(task.getFaultId());
            if (fault != null) {
                fault.setStatus(2);
                equipmentFaultService.updateById(fault);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeTask(Long taskId, String repairContent, String partsUsed,
                                BigDecimal repairCost, String remark) {
        EquipmentRepairTask task = getById(taskId);
        if (task == null) return false;

        task.setStatus(3);
        task.setCompleteTime(LocalDateTime.now());
        task.setRepairContent(repairContent);
        task.setSolution(repairContent);
        task.setPartsUsed(partsUsed);
        task.setRepairCost(repairCost != null ? repairCost : BigDecimal.ZERO);
        task.setCost(repairCost != null ? repairCost : BigDecimal.ZERO);
        if (remark != null) {
            task.setRemark(remark);
        }
        boolean updated = updateById(task);
        if (!updated) return false;

        if (task.getFaultId() != null) {
            EquipmentFault fault = equipmentFaultService.getById(task.getFaultId());
            if (fault != null) {
                fault.setStatus(3);
                equipmentFaultService.updateById(fault);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptanceTask(Long taskId, String acceptanceResult, String acceptanceBy, String remark) {
        EquipmentRepairTask task = getById(taskId);
        if (task == null) return false;

        task.setAcceptanceResult(acceptanceResult);
        task.setAcceptanceTime(LocalDateTime.now());
        task.setAcceptanceBy(acceptanceBy);
        if (remark != null) {
            task.setRemark(remark);
        }

        if ("pass".equals(acceptanceResult)) {
            task.setStatus(4);
        } else {
            task.setStatus(2);
            task.setCompleteTime(null);
        }
        boolean updated = updateById(task);
        if (!updated) return false;

        if ("pass".equals(acceptanceResult)) {
            if (task.getFaultId() != null) {
                EquipmentFault fault = equipmentFaultService.getById(task.getFaultId());
                if (fault != null) {
                    fault.setStatus(4);
                    equipmentFaultService.updateById(fault);
                }
            }

            if (task.getEquipmentId() != null) {
                EquipmentInfo equipment = equipmentInfoService.getById(task.getEquipmentId());
                if (equipment != null) {
                    equipment.setStatus(1);
                    equipment.setCurrentFaultId(null);
                    equipmentInfoService.updateById(equipment);
                }
            }
        } else {
            if (task.getFaultId() != null) {
                EquipmentFault fault = equipmentFaultService.getById(task.getFaultId());
                if (fault != null) {
                    fault.setStatus(2);
                    equipmentFaultService.updateById(fault);
                }
            }
        }

        return true;
    }
}
