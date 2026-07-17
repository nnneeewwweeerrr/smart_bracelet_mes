package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存信息
 */
@Data
@TableName("stock_info")
public class StockInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private Long warehouseId;

    private BigDecimal quantity;

    private String location;

    @TableField(exist = false)
    private String materialNo;

    @TableField(exist = false)
    private String materialName;

    @TableField(exist = false)
    private String type;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String warehouse;

    @TableField(exist = false)
    private Integer safetyStock;

    @TableField(exist = false)
    private BigDecimal avgPrice;

    @TableField(exist = false)
    private BigDecimal totalValue;

    @TableField(exist = false)
    private java.time.LocalDateTime updateTime;
}
