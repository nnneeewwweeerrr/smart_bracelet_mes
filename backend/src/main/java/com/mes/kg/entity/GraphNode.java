package com.mes.kg.entity;

import lombok.Data;

/**
 * 知识图谱通用节点基类
 */
@Data
public class GraphNode {
    protected String id;
    protected String name;
    protected String type;
    protected String code;
    protected String description;

    public GraphNode() {}

    public GraphNode(String id, String name, String type, String code, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.code = code;
        this.description = description;
    }
}
