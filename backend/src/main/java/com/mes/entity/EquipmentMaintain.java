package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("equipment_maintain")
public class EquipmentMaintain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String maintainNo;

    private Long equipmentId;

    private String maintainType;

    private LocalDateTime maintainDate;

    private String maintainContent;

    private String maintainResult;

    private Long maintainerId;

    private String maintainerName;

    private String remark;

    private LocalDateTime createTime;
}