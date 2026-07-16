package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_repair_task")
public class DemoRepairTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskNo;

    private Long faultId;

    private Long equipmentId;

    private String equipmentNo;

    private String equipmentName;

    private String faultDesc;

    private String faultDescription;

    private Integer priority;

    private Long repairerId;

    private String assignee;

    private LocalDateTime assignTime;

    private LocalDateTime acceptTime;

    private LocalDateTime startTime;

    private LocalDateTime completeTime;

    private String repairContent;

    private String solution;

    private String partsUsed;

    private BigDecimal repairCost;

    private BigDecimal cost;

    private String acceptanceResult;

    private LocalDateTime acceptanceTime;

    private String acceptanceBy;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String reportDate;
}