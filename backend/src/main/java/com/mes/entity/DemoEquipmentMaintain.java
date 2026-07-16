package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_equipment_maintain")
public class DemoEquipmentMaintain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String maintainNo;

    private Long equipmentId;

    private String equipmentNo;

    private String equipmentName;

    private String maintainType;

    private String maintainLevel;

    private LocalDate planDate;

    private LocalDateTime actualDate;

    private String maintainContent;

    private String maintainResult;

    private BigDecimal cost;

    private String maintainer;

    private BigDecimal durationHours;

    private Long relatedTaskId;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String maintainDate;
}