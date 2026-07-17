package com.mes.kg.repository;

import com.mes.kg.entity.CauseNode;
import com.mes.kg.entity.SolutionNode;
import com.mes.kg.entity.MaterialNode;
import com.mes.kg.entity.EquipmentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CauseRepository extends Neo4jRepository<CauseNode, String> {
    List<CauseNode> findByNameContaining(String name);
}
