package com.mes.kg.repository;

import com.mes.kg.entity.DefectNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DefectRepository extends Neo4jRepository<DefectNode, String> {

    List<DefectNode> findByNameContaining(String name);

    @Query("MATCH (d:Defect)-[:CAUSED_BY]->(c:Cause) WHERE d.code = $code RETURN d, COLLECT(c) as causes")
    DefectNode findDefectWithCauses(String code);

    @Query("MATCH (d:Defect)-[:SOLVED_BY]->(s:Solution) WHERE d.code = $code RETURN d, COLLECT(s) as solutions")
    DefectNode findDefectWithSolutions(String code);

    @Query("MATCH (d:Defect)-[:CAUSED_BY]->(c:Cause), (d)-[:SOLVED_BY]->(s:Solution) WHERE d.code = $code RETURN d.name as defectName, COLLECT(DISTINCT {code: c.code, name: c.name, description: c.description, probability: c.probability}) as causes, COLLECT(DISTINCT {code: s.code, name: s.name, description: s.description, steps: s.steps, effectiveness: s.effectiveness}) as solutions")
    Map<String, Object> findDefectWithFullInfo(String code);
}
