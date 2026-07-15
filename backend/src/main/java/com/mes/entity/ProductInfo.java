package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    private String productCode;

    private String productName;

    private String productType;

    private String specification;

    private BigDecimal unitPrice;

    private Integer status;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String productNo;

    @TableField(exist = false)
    private String model;

    @TableField(exist = false)
    private String category;

    @TableField(exist = false)
    private BigDecimal price;

    @TableField(exist = false)
    private BigDecimal cost;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String description;

    @TableField(exist = false)
    private String remark;
}
