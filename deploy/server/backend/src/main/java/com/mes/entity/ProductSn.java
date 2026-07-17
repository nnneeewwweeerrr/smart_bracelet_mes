package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_sn")
public class ProductSn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String snCode;

    private Long productId;

    private String productCode;

    private String productName;

    private Long workOrderId;

    private String workOrderNo;

    private Long orderId;

    private String orderNo;

    private Long customerId;

    private String customerName;

    private Integer status;

    private LocalDateTime productionDate;

    private Long warehouseId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
