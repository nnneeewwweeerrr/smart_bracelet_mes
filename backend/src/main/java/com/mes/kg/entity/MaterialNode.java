package com.mes.kg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialNode extends GraphNode {
    private String specification;
    private Float criticality;

    public MaterialNode() {
        super();
        this.type = "material";
    }
}
