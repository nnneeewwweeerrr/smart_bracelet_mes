package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_stock_check")
public class DemoStockCheck {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private LocalDateTime checkDate;

    private String operator;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}