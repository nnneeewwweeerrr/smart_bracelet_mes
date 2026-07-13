package com.mes.controller;

import com.mes.common.Result;
import com.mes.kg.service.KnowledgeGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/kg")
public class KnowledgeGraphController {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @GetMapping("/nodes")
    public Result<List<Map<String, Object>>> getAllNodes() {
        return Result.success(knowledgeGraphService.findAllNodes());
    }

    @GetMapping("/relationships")
    public Result<List<Map<String, Object>>> getAllRelationships() {
        return Result.success(knowledgeGraphService.findAllRelationships());
    }

    @GetMapping("/defect/{defectName}")
    public Result<Map<String, Object>> analyzeDefect(@PathVariable String defectName) {
        return Result.success(knowledgeGraphService.queryDefectAnalysis(defectName));
    }

    @GetMapping("/process/{processCode}/defects")
    public Result<List<Map<String, Object>>> getProcessDefects(@PathVariable String processCode) {
        return Result.success(knowledgeGraphService.queryProcessDefects(processCode));
    }

    @GetMapping("/node/{nodeType}/{nodeCode}")
    public Result<Map<String, Object>> getNodeDetails(@PathVariable String nodeType, @PathVariable String nodeCode) {
        Map<String, Object> node = knowledgeGraphService.queryNodeDetails(nodeType, nodeCode);
        if (node == null) {
            return Result.error("节点不存在");
        }
        return Result.success(node);
    }

    @PostMapping("/node")
    public Result<Void> createNode(@RequestBody Map<String, Object> nodeData) {
        knowledgeGraphService.createNode(nodeData);
        return Result.success();
    }

    @PostMapping("/relationship")
    public Result<Void> createRelationship(@RequestBody Map<String, Object> params) {
        String sourceType = params.get("sourceType").toString();
        String sourceCode = params.get("sourceCode").toString();
        String targetType = params.get("targetType").toString();
        String targetCode = params.get("targetCode").toString();
        String relationType = params.get("relationType").toString();
        knowledgeGraphService.createRelationship(sourceType, sourceCode, targetType, targetCode, relationType);
        return Result.success();
    }

    @DeleteMapping("/node/{nodeType}/{nodeCode}")
    public Result<Void> deleteNode(@PathVariable String nodeType, @PathVariable String nodeCode) {
        knowledgeGraphService.deleteNode(nodeType, nodeCode);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("processCount", knowledgeGraphService.countNodesByType("Process"));
        stats.put("defectCount", knowledgeGraphService.countNodesByType("Defect"));
        stats.put("causeCount", knowledgeGraphService.countNodesByType("Cause"));
        stats.put("solutionCount", knowledgeGraphService.countNodesByType("Solution"));
        stats.put("materialCount", knowledgeGraphService.countNodesByType("Material"));
        stats.put("equipmentCount", knowledgeGraphService.countNodesByType("Equipment"));
        stats.put("totalNodes", knowledgeGraphService.findAllNodes().size());
        stats.put("totalRelationships", knowledgeGraphService.findAllRelationships().size());
        return Result.success(stats);
    }

    @PostMapping("/init")
    public Result<Void> initDemoData() {
        knowledgeGraphService.initDemoData();
        return Result.success();
    }

    @GetMapping("/multi-path/{defectName}")
    public Result<Map<String, Object>> multiPathAnalysis(@PathVariable String defectName) {
        return Result.success(knowledgeGraphService.queryMultiPathAnalysis(defectName));
    }

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchNodes(@RequestParam String keyword) {
        return Result.success(knowledgeGraphService.searchNodes(keyword));
    }

    @GetMapping("/process/{processCode}/materials")
    public Result<List<Map<String, Object>>> getProcessMaterials(@PathVariable String processCode) {
        return Result.success(knowledgeGraphService.queryProcessMaterials(processCode));
    }

    @GetMapping("/process/{processCode}/equipments")
    public Result<List<Map<String, Object>>> getProcessEquipments(@PathVariable String processCode) {
        return Result.success(knowledgeGraphService.queryProcessEquipments(processCode));
    }

    @GetMapping("/graph-data")
    public Result<Map<String, Object>> getGraphData() {
        return Result.success(knowledgeGraphService.getGraphData());
    }
}
