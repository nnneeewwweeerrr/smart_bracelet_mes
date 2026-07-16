package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("equipment_repair_task")
public class EquipmentRepairTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private Long faultId;

    private Long equipmentId;

    private String equipmentNo;

    private String equipmentName;

    private String faultType;

    private String faultLevel;

    private String faultDesc;

    private String faultDescription;

    private String reporter;

    private LocalDateTime reportTime;

    private String assignee;

    private Long repairerId;

    private LocalDateTime assignTime;

    private LocalDateTime acceptTime;

    private LocalDateTime startTime;

    private LocalDateTime completeTime;

    private String solution;

    private String repairContent;

    private String result;

    private String partsUsed;

    private BigDecimal cost;

    private BigDecimal repairCost;

    private Integer priority;

    private Integer status;

    private String acceptanceResult;

    private LocalDateTime acceptanceTime;

    private String acceptanceBy;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String reportDate;
}
