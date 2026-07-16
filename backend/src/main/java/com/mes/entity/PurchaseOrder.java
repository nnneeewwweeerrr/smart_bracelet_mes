package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String purchaseNo;

    private Long supplierId;

    private String supplierName;

    private Long materialId;

    private String materialName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime expectedDate;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}