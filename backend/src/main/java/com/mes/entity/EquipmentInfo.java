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
@TableName("equipment_info")
public class EquipmentInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String equipmentCode;

    private String equipmentName;

    private String equipmentType;

    private Integer status;

    private String location;

    private LocalDate purchaseDate;

    private LocalDateTime lastMaintainDate;

    private LocalDateTime nextMaintainDate;

    private BigDecimal runningHours;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String equipmentNo;

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String type;

    @TableField(exist = false)
    private String workshop;

    @TableField(exist = false)
    private String manager;

    @TableField(exist = false)
    private String phone;
}