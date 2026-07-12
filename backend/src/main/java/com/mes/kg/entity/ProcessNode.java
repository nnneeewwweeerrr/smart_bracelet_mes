package com.mes.kg.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@Node("Process")
public class ProcessNode {

    @Id
    private String code;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("params")
    private String params;

    @Relationship(type = "HAS_DEFECT", direction = Relationship.Direction.OUTGOING)
    private List<DefectNode> defects;

    @Relationship(type = "USES_MATERIAL", direction = Relationship.Direction.OUTGOING)
    private List<MaterialNode> materials;

    @Relationship(type = "USES_EQUIPMENT", direction = Relationship.Direction.OUTGOING)
    private List<EquipmentNode> equipments;
}
