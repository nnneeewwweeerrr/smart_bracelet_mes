package com.mes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("process_material_bom")
public class ProcessMaterialBom {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long processId;

    private Long materialId;

    private BigDecimal quantity;

    private Integer isRequired;

    private Integer sortOrder;
}
