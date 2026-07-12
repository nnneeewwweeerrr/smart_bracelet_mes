package com.mes.kg.repository;

import com.mes.kg.entity.ProcessNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessRepository extends Neo4jRepository<ProcessNode, String> {

    List<ProcessNode> findByNameContaining(String name);

    @Query("MATCH (p:Process)-[:HAS_DEFECT]->(d:Defect) WHERE p.code = $code RETURN p, COLLECT(d) as defects")
    ProcessNode findProcessWithDefects(String code);
}
