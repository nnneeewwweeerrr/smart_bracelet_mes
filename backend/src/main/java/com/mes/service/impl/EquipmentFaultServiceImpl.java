package com.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mes.common.Result;
import com.mes.entity.EquipmentFault;
import com.mes.entity.EquipmentMaintain;
import com.mes.mapper.EquipmentFaultMapper;
import com.mes.service.EquipmentFaultService;
import com.mes.service.EquipmentMaintainService;
import com.mes.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentFaultServiceImpl extends ServiceImpl<EquipmentFaultMapper, EquipmentFault> implements EquipmentFaultService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private EquipmentMaintainService equipmentMaintainService;

    @Override
    public Result<Map<String, Object>> getPage(Long pageNum, Long pageSize, String faultNo, Integer status, Long equipmentId) {
        Page<EquipmentFault> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EquipmentFault> wrapper = new LambdaQueryWrapper<>();
        if (faultNo != null && !faultNo.isEmpty()) {
            wrapper.like(EquipmentFault::getFaultNo, faultNo);
        }
        if (status != null) {
            wrapper.eq(EquipmentFault::getStatus, status);
        }
        if (equipmentId != null) {
            wrapper.eq(EquipmentFault::getEquipmentId, equipmentId);
        }
        wrapper.orderByDesc(EquipmentFault::getReportTime);
        IPage<EquipmentFault> result = page(page, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        return Result.success(map);
    }

    @Override
    public boolean save(EquipmentFault entity) {
        if (entity.getFaultNo() == null || entity.getFaultNo().isEmpty()) {
            entity.setFaultNo("EF" + System.currentTimeMillis());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        if (entity.getReportTime() == null) {
            entity.setReportTime(LocalDateTime.now());
        }
        return super.save(entity);
    }

    @Override
    public Result<Void> assignFault(Long faultId, Long assigneeId, String assignType) {
        EquipmentFault fault = getById(faultId);
        if (fault == null) {
            return Result.error("故障记录不存在");
        }
        if (fault.getStatus() != 0) {
            return Result.error("只有待处理的故障才能派工");
        }
        fault.setAssigneeId(assigneeId);
        fault.setAssignTime(LocalDateTime.now());
        fault.setAssignType(assignType);
        updateById(fault);
        return Result.success();
    }

    @Override
    public Result<Void> autoAssign(Long faultId) {
        EquipmentFault fault = getById(faultId);
        if (fault == null) {
            return Result.error("故障记录不存在");
        }
        if (fault.getStatus() != 0) {
            return Result.error("只有待处理的故障才能派工");
        }

        // 自动派工：选择当前维修任务最少的维修人员(role_id=7)
        // 简单实现：查询repair_zhang(id=10)和repair_li(id=11)的任务数，选择较少的
        LambdaQueryWrapper<EquipmentFault> w1 = new LambdaQueryWrapper<>();
        w1.eq(EquipmentFault::getAssigneeId, 10L);
        w1.in(EquipmentFault::getStatus, 0, 1);
        long count1 = count(w1);

        LambdaQueryWrapper<EquipmentFault> w2 = new LambdaQueryWrapper<>();
        w2.eq(EquipmentFault::getAssigneeId, 11L);
        w2.in(EquipmentFault::getStatus, 0, 1);
        long count2 = count(w2);

        Long assigneeId = count1 <= count2 ? 10L : 11L;
        return assignFault(faultId, assigneeId, "auto");
    }

    @Override
    public Result<Void> handleFault(Long faultId, String handler, String solution, Integer status, Double repairHours) {
        EquipmentFault fault = getById(faultId);
        if (fault == null) {
            return Result.error("故障记录不存在");
        }
        fault.setHandler(handler);
        fault.setSolution(solution);
        fault.setStatus(status);
        if (repairHours != null) {
            fault.setRepairHours(BigDecimal.valueOf(repairHours));
        }
        fault.setHandleTime(LocalDateTime.now());
        updateById(fault);

        // 如果状态为已修复(2)，自动插入一条保养记录
        if (status != null && status == 2) {
            EquipmentMaintain maintain = new EquipmentMaintain();
            maintain.setMaintainNo("MN" + System.currentTimeMillis());
            maintain.setEquipmentId(fault.getEquipmentId());
            maintain.setMaintainType("故障维修");
            maintain.setMaintainDate(LocalDateTime.now());
            maintain.setMaintainContent(fault.getFaultDesc() + "；处理方案：" + solution);
            maintain.setMaintainResult("正常");
            maintain.setMaintainerId(fault.getAssigneeId());
            maintain.setMaintainerName(handler);
            maintain.setRemark("由故障单" + fault.getFaultNo() + "自动生成");
            equipmentMaintainService.save(maintain);
        }
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getMyTasks(Long assigneeId, Integer status) {
        LambdaQueryWrapper<EquipmentFault> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EquipmentFault::getAssigneeId, assigneeId);
        if (status != null) {
            wrapper.eq(EquipmentFault::getStatus, status);
        } else {
            wrapper.in(EquipmentFault::getStatus, 0, 1);
        }
        wrapper.orderByDesc(EquipmentFault::getAssignTime);
        List<EquipmentFault> list = list(wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("records", list);
        map.put("total", list.size());
        return Result.success(map);
    }
}