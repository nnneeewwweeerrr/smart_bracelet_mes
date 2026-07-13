package com.mes.kg.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Cause")
public class CauseNode {

    @Id
    private String code;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("probability")
    private Double probability;
}
