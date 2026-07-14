package com.mes.kg.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 不良现象节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DefectNode extends GraphNode {
    private String processId;
    private String nextProcessId;
    private Float defectRate;

    public DefectNode() {
        super();
        this.type = "defect";
    }
}
