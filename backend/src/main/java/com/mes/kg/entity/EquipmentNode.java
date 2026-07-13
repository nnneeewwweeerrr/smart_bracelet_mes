package com.mes.kg.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Equipment")
public class EquipmentNode {

    @Id
    private String code;

    @Property("name")
    private String name;

    @Property("model")
    private String model;

    @Property("location")
    private String location;

    @Property("status")
    private String status;
}
