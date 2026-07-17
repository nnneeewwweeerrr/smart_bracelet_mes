package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sn_material_trace")
public class SnMaterialTrace {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long snId;

    private String snCode;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String batchNo;

    private Long supplierId;

    private BigDecimal quantity;

    private Long processId;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createTime;
}
