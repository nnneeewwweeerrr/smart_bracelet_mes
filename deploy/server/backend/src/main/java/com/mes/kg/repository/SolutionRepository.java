package com.mes.kg.repository;

import com.mes.kg.entity.SolutionNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends Neo4jRepository<SolutionNode, String> {
    List<SolutionNode> findByNameContaining(String name);
}
