package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_stock_out")
public class DemoStockOut {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stockOutNo;

    private String type;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private String workOrderNo;

    private String orderNo;

    private String operator;

    private LocalDateTime outDate;

    private String scanCode;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String outNo;
}