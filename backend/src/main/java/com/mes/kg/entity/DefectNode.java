package com.mes.kg.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Data
@Node("Defect")
public class DefectNode {

    @Id
    private String code;

    @Property("name")
    private String name;

    @Property("description")
    private String description;

    @Property("severity")
    private String severity;

    @Relationship(type = "CAUSED_BY", direction = Relationship.Direction.OUTGOING)
    private List<CauseNode> causes;

    @Relationship(type = "SOLVED_BY", direction = Relationship.Direction.OUTGOING)
    private List<SolutionNode> solutions;
}
