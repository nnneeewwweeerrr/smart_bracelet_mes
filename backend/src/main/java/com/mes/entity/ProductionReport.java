package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("production_report")
public class ProductionReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;

    private Long workerId;

    private Long processId;

    private LocalDate reportDate;

    private Integer goodQuantity;

    private Integer badQuantity;
}
