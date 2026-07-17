package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("unqualified_product")
public class UnqualifiedProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String unqualifiedNo;

    private String sourceType;

    private String sourceNo;

    private String workOrderNo;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private String batchNo;

    private BigDecimal quantity;

    private String defectType;

    private String defectDesc;

    private LocalDateTime discoveryTime;

    private String discoverer;

    private String disposition;

    private String dispositionResult;

    private String responsible;

    private String rectifyAction;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}