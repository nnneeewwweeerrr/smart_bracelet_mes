package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_alert")
public class StockAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alertNo;

    private String alertType;

    private String alertLevel;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private String warehouse;

    private BigDecimal currentQuantity;

    private BigDecimal safeQuantity;

    private BigDecimal alertQuantity;

    private LocalDateTime alertTime;

    private String handler;

    private LocalDateTime handleTime;

    private String handleResult;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}