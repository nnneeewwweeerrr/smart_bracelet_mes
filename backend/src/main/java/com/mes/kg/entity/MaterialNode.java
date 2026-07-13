package com.mes.kg.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Material")
public class MaterialNode {

    @Id
    private String code;

    @Property("name")
    private String name;

    @Property("specification")
    private String specification;

    @Property("supplier")
    private String supplier;

    @Property("unit")
    private String unit;
}
