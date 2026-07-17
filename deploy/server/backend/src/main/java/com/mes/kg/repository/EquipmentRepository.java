package com.mes.kg.repository;

import com.mes.kg.entity.EquipmentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends Neo4jRepository<EquipmentNode, String> {
    List<EquipmentNode> findByNameContaining(String name);
}
