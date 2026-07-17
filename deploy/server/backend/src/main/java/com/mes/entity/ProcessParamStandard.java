package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("process_param_standard")
public class ProcessParamStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processId;

    private String paramName;

    private String paramCode;

    private String unit;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    private BigDecimal targetValue;

    private Integer isRequired;

    private Integer sortOrder;
}
