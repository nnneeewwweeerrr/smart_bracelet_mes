package com.mes.kg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 原因节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CauseNode extends GraphNode {

    public CauseNode() {
        super();
        this.type = "cause";
    }
}
