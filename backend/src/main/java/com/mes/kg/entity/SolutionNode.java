package com.mes.kg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 解决方案节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SolutionNode extends GraphNode {
    private Float effectiveness;

    public SolutionNode() {
        super();
        this.type = "solution";
    }
}
