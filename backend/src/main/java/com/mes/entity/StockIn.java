package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单
 */
@Data
@TableName("stock_in")
public class StockIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String inNo;

    private Long materialId;

    private Long warehouseId;

    private BigDecimal quantity;

    private String sourceType;

    private String sourceNo;

    private Long operatorId;

    private Integer status;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String stockInNo;

    @TableField(exist = false)
    private String warehouse;

    @TableField(exist = false)
    private String relatedNo;

    @TableField(exist = false)
    private String type;

    @TableField(exist = false)
    private Integer itemCount;

    @TableField(exist = false)
    private BigDecimal totalQuantity;

    @TableField(exist = false)
    private BigDecimal totalAmount;
}
