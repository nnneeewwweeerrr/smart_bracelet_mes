package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("equipment_fault")
public class EquipmentFault {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("fault_no")
    private String faultNo;

    @TableField("equipment_id")
    private Long equipmentId;

    @TableField("fault_level")
    private Integer faultLevel;

    @TableField("fault_desc")
    private String faultDesc;

    private String reporter;

    @TableField("report_time")
    private LocalDateTime reportTime;

    private Integer status;

    private String handler;

    private String solution;

    @TableField("repair_hours")
    private BigDecimal repairHours;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    private String process;

    private String remark;

    @TableField("assignee_id")
    private Long assigneeId;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("assign_type")
    private String assignType;

    @TableField(exist = false)
    private String equipmentName;

    @TableField(exist = false)
    private String assigneeName;
}