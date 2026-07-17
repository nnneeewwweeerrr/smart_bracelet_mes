package com.mes.kg.entity;

import lombok.Data;

/**
 * 知识图谱通用关系基类
 */
@Data
public class GraphRelationship {
    private String id;
    private String sourceId;
    private String targetId;
    private String type;
    private float weight;
    private String description;

    public GraphRelationship() {}

    public GraphRelationship(String id, String sourceId, String targetId, String type, float weight, String description) {
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.type = type;
        this.weight = weight;
        this.description = description;
    }
}
