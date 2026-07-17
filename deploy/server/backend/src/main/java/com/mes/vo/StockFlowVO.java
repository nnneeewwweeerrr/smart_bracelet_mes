package com.mes.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水统一返回对象
 */
@Data
public class StockFlowVO {

    private String flowNo;

    private String flowType;

    private Long materialId;

    private Long warehouseId;

    private BigDecimal quantity;

    private LocalDateTime createTime;
}
