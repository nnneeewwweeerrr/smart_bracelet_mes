package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_check")
public class StockCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private String checkType;

    private String warehouse;

    private LocalDate checkDate;

    private Integer totalItems;

    private Integer matchItems;

    private Integer diffItems;

    private String checker;

    private String supervisor;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}