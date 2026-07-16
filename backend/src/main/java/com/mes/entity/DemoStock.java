package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("mes_demo.demo_stock")
public class DemoStock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Integer quantity;

    private String location;

    private String batchNo;

    private LocalDate expireDate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}