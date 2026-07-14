package com.mes.kg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessNode extends GraphNode {
    private Integer sortOrder;

    public ProcessNode() {
        super();
        this.type = "process";
    }
}
