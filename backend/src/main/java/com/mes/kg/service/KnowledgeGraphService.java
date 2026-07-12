package com.mes.kg.service;

import java.util.List;
import java.util.Map;

public interface KnowledgeGraphService {

    void initDemoData();

    List<Map<String, Object>> findAllNodes();

    List<Map<String, Object>> findAllRelationships();

    Map<String, Object> queryDefectAnalysis(String defectName);

    List<Map<String, Object>> queryProcessDefects(String processCode);

    Map<String, Object> queryNodeDetails(String nodeType, String nodeCode);

    void createNode(Map<String, Object> nodeData);

    void createRelationship(String sourceType, String sourceCode, String targetType, String targetCode, String relationType);

    void deleteNode(String nodeType, String nodeCode);

    long countNodesByType(String nodeType);

    Map<String, Object> queryMultiPathAnalysis(String defectName);

    List<Map<String, Object>> searchNodes(String keyword);

    List<Map<String, Object>> queryProcessMaterials(String processCode);

    List<Map<String, Object>> queryProcessEquipments(String processCode);

    Map<String, Object> getGraphData();
}
