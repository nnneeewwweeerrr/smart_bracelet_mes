package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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

    private String faultNo;

    private Long equipmentId;

    private String equipmentNo;

    private String equipmentName;

    private String faultType;

    private String faultLevel;

    private String faultDesc;

    private String faultDescription;

    private Long reporterId;

    private String reporter;

    private LocalDateTime reportTime;

    private Long repairTaskId;

    private String assignee;

    private LocalDateTime assignTime;

    private String solution;

    private Integer status;

    private BigDecimal cost;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}