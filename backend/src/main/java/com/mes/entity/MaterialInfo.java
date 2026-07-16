package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("material_info")
public class MaterialInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String materialNo;

    private String materialName;

    private String type;

    private String spec;

    private String unit;

    private BigDecimal price;

    private Integer stockQty;

    private Integer safetyStock;

    private Long supplierId;

    private String supplierName;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}