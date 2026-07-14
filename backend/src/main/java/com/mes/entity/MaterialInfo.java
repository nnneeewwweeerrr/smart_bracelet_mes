package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("material_info")
public class MaterialInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("material_code")
    private String materialCode;

    @TableField("material_name")
    private String materialName;

    @TableField("material_type")
    private String materialType;

    @TableField("unit")
    private String unit;

    @TableField("unit_price")
    private java.math.BigDecimal unitPrice;

    @TableField("safety_stock")
    private Integer safetyStock;

    @TableField("status")
    private Integer status;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private String supplier;

    @TableField(exist = false)
    private java.time.LocalDateTime createTime;

    @TableField(exist = false)
    private java.time.LocalDateTime updateTime;
}
