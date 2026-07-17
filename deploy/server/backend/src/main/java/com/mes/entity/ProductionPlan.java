package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    private String planName;

    private Long orderId;

    private Long productId;

    private Integer plannedQuantity;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    private String manager;

    private String remark;

    private LocalDateTime createTime;

    // ===== 虚拟字段（前端展示用）=====
    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private Integer quantity;
}
