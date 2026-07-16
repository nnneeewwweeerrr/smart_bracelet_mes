package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("stock_check_detail")
public class StockCheckDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private String checkNo;

    private Long materialId;

    private String materialNo;

    private String materialName;

    private String spec;

    private String unit;

    private String batchNo;

    private BigDecimal bookQuantity;

    private BigDecimal actualQuantity;

    private BigDecimal diffQuantity;

    private String diffType;

    private String diffReason;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}