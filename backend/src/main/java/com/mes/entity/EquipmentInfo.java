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

    private String equipmentNo;

    private String equipmentName;

    private String equipmentType;

    private String model;

    private String manufacturer;

    private String workshop;

    private String location;

    private String manager;

    private String phone;

    private LocalDate purchaseDate;

    private LocalDate commissionDate;

    private BigDecimal runningHours;

    private LocalDateTime lastMaintainDate;

    private LocalDateTime nextMaintainDate;

    private Long currentFaultId;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private String type;
}
