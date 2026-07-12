package com.mes.kg.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Solution")
public class SolutionNode {

    @Id
    private String code;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("steps")
    private String steps;

    @Property("effectiveness")
    private Double effectiveness;
}
