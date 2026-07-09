package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("production_plan")
public class ProductionPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String planNo;

    private Long orderId;

    private Long productId;

    private Integer plannedQuantity;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    private LocalDateTime createTime;
}
