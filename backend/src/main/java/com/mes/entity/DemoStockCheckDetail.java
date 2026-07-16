package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_stock_check_detail")
public class DemoStockCheckDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private Integer systemQuantity;

    private Integer actualQuantity;

    private Integer diffQuantity;

    private BigDecimal diffAmount;

    private String diffReason;

    private Integer handleStatus;

    private LocalDateTime createTime;
}