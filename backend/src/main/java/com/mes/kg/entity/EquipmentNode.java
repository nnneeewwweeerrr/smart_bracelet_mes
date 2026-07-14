package com.mes.kg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EquipmentNode extends GraphNode {
    private String specification;

    public EquipmentNode() {
        super();
        this.type = "equipment";
    }
}
