package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_stock_alert")
public class DemoStockAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alertNo;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String alertType;

    private Integer currentQuantity;

    private Integer threshold;

    private Integer status;

    private String handleRemark;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}