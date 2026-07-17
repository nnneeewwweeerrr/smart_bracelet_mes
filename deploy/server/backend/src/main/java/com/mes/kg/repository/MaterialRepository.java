package com.mes.kg.repository;

import com.mes.kg.entity.MaterialNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends Neo4jRepository<MaterialNode, String> {
    List<MaterialNode> findByNameContaining(String name);
}
