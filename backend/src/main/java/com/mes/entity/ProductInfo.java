package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product_info")
public class ProductInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productNo;

    private String productName;

    private String category;

    private String model;

    private BigDecimal price;

    private BigDecimal cost;

    private String unit;

    private Integer stockQty;

    private Integer safetyStock;

    private String description;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}