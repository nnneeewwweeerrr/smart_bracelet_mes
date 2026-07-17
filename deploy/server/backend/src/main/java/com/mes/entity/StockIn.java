package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_in")
public class StockIn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockInNo;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private String spec;

    private String unit;

    private Long warehouseId;

    private String warehouse;

    private String batchNo;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private String sourceType;

    private String sourceNo;

    private Long supplierId;

    private String supplierName;

    private String operator;

    private LocalDateTime inDate;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String inNo;
}
