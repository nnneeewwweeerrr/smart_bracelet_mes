package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_customer")
public class OrderCustomer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long customerId;

    private String customerName;

    private Long productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private Integer status;

    private LocalDateTime deliveryDate;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}