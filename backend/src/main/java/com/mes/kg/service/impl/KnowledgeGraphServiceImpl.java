package com.mes.kg.service.impl;

import com.mes.kg.service.KnowledgeGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    private final Map<String, Map<String, Object>> nodes = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> relationships = new ArrayList<>();

    @PostConstruct
    public void init() {
        initDemoData();
    }

    @Override
    public void initDemoData() {
        if (!nodes.isEmpty()) {
            return;
        }

        log.info("初始化智能手环生产知识图谱演示数据...");

        createNode(Map.of("type", "Process", "code", "P001", "name", "SMT贴片", "description", "表面贴装技术，将电子元件贴装到PCB板上", "params", "{\"temperature\": \"150-180°C\", \"speed\": \"8000CPH\"}"));
        createNode(Map.of("type", "Process", "code", "P002", "name", "回流焊", "description", "通过高温使焊膏熔化，实现元件与PCB的可靠连接", "params", "{\"peakTemp\": \"240-260°C\", \"preheatTemp\": \"150-180°C\"}"));
        createNode(Map.of("type", "Process", "code", "P003", "name", "AOI检测", "description", "自动光学检测，检测焊接缺陷", "params", "{\"accuracy\": \"±0.05mm\", \"speed\": \"1200mm/s\"}"));
        createNode(Map.of("type", "Process", "code", "P004", "name", "屏幕贴合", "description", "将触摸屏与显示屏贴合组装", "params", "{\"pressure\": \"0.4-0.6MPa\", \"temperature\": \"40-60°C\"}"));
        createNode(Map.of("type", "Process", "code", "P005", "name", "防水测试", "description", "检测产品防水性能", "params", "{\"waterPressure\": \"1.5m\", \"duration\": \"30min\"}"));
        createNode(Map.of("type", "Process", "code", "P006", "name", "功能测试", "description", "检测产品各项功能是否正常", "params", "{\"testItems\": \"蓝牙,心率,计步,防水\"}"));

        createNode(Map.of("type", "Defect", "code", "D001", "name", "虚焊", "description", "焊料未完全润湿元件引脚，导致连接不可靠", "severity", "高"));
        createNode(Map.of("type", "Defect", "code", "D002", "name", "短路", "description", "相邻焊盘之间被焊料桥接", "severity", "高"));
        createNode(Map.of("type", "Defect", "code", "D003", "name", "缺件", "description", "元件缺失", "severity", "中"));
        createNode(Map.of("type", "Defect", "code", "D004", "name", "偏移", "description", "元件位置偏移超过允许范围", "severity", "中"));
        createNode(Map.of("type", "Defect", "code", "D005", "name", "气泡", "description", "屏幕贴合时产生气泡", "severity", "中"));
        createNode(Map.of("type", "Defect", "code", "D006", "name", "划痕", "description", "屏幕表面出现划痕", "severity", "低"));
        createNode(Map.of("type", "Defect", "code", "D007", "name", "进水", "description", "防水测试后产品内部进水", "severity", "高"));

        createNode(Map.of("type", "Cause", "code", "C001", "name", "焊膏量不足", "description", "焊膏印刷量不够", "probability", 0.35));
        createNode(Map.of("type", "Cause", "code", "C002", "name", "回流温度偏低", "description", "回流焊峰值温度低于标准", "probability", 0.25));
        createNode(Map.of("type", "Cause", "code", "C003", "name", "元件引脚氧化", "description", "元件引脚表面氧化导致焊料无法润湿", "probability", 0.2));
        createNode(Map.of("type", "Cause", "code", "C004", "name", "PCB焊盘污染", "description", "PCB焊盘表面有污染物", "probability", 0.15));
        createNode(Map.of("type", "Cause", "code", "C005", "name", "贴片机偏移", "description", "贴片机吸嘴偏移", "probability", 0.3));
        createNode(Map.of("type", "Cause", "code", "C006", "name", "焊膏拉丝", "description", "焊膏印刷后相邻焊盘之间有拉丝", "probability", 0.25));
        createNode(Map.of("type", "Cause", "code", "C007", "name", "清洁度不够", "description", "贴合前屏幕或盖板清洁不彻底", "probability", 0.4));
        createNode(Map.of("type", "Cause", "code", "C008", "name", "贴合压力不均", "description", "贴合时压力分布不均匀", "probability", 0.3));
        createNode(Map.of("type", "Cause", "code", "C009", "name", "密封胶老化", "description", "防水密封胶过期或老化", "probability", 0.35));
        createNode(Map.of("type", "Cause", "code", "C010", "name", "螺丝松动", "description", "外壳螺丝未拧紧", "probability", 0.2));

        createNode(Map.of("type", "Solution", "code", "S001", "name", "增加焊膏量", "description", "调整印刷机参数增加焊膏量", "steps", "1.检查钢网开孔尺寸\n2.调整刮刀压力\n3.重新印刷验证", "effectiveness", 0.9));
        createNode(Map.of("type", "Solution", "code", "S002", "name", "提高回流温度", "description", "调整回流焊温度曲线", "steps", "1.测量当前温度曲线\n2.提高峰值温度5-10°C\n3.验证焊接效果", "effectiveness", 0.85));
        createNode(Map.of("type", "Solution", "code", "S003", "name", "更换元件", "description", "更换氧化的元件", "steps", "1.识别氧化元件\n2.更换新元件\n3.重新焊接", "effectiveness", 0.95));
        createNode(Map.of("type", "Solution", "code", "S004", "name", "清洁PCB", "description", "清洗PCB焊盘", "steps", "1.使用IPA清洗\n2.热风烘干\n3.检查清洁效果", "effectiveness", 0.8));
        createNode(Map.of("type", "Solution", "code", "S005", "name", "校准贴片机", "description", "校准贴片机吸嘴位置", "steps", "1.运行校准程序\n2.检查偏移量\n3.调整补偿参数", "effectiveness", 0.9));
        createNode(Map.of("type", "Solution", "code", "S006", "name", "更换钢网", "description", "更换新的印刷钢网", "steps", "1.检查钢网磨损情况\n2.制作新钢网\n3.安装调试", "effectiveness", 0.85));
        createNode(Map.of("type", "Solution", "code", "S007", "name", "重新清洁", "description", "重新清洁屏幕和盖板", "steps", "1.拆解产品\n2.使用无尘布和清洁剂清洁\n3.重新贴合", "effectiveness", 0.9));
        createNode(Map.of("type", "Solution", "code", "S008", "name", "调整贴合参数", "description", "调整贴合机压力参数", "steps", "1.检查压力分布\n2.调整压力参数\n3.测试验证", "effectiveness", 0.85));
        createNode(Map.of("type", "Solution", "code", "S009", "name", "更换密封胶", "description", "更换新的防水密封胶", "steps", "1.去除旧密封胶\n2.涂新密封胶\n3.重新组装", "effectiveness", 0.95));
        createNode(Map.of("type", "Solution", "code", "S010", "name", "重新拧紧螺丝", "description", "按标准扭矩拧紧外壳螺丝", "steps", "1.检查螺丝扭矩\n2.按标准重新拧紧\n3.验证密封性", "effectiveness", 0.85));

        createNode(Map.of("type", "Material", "code", "M001", "name", "焊膏", "specification", "SAC305", "supplier", "Alpha", "unit", "g"));
        createNode(Map.of("type", "Material", "code", "M002", "name", "PCB板", "specification", "FR-4", "supplier", "深南电路", "unit", "块"));
        createNode(Map.of("type", "Material", "code", "M003", "name", "触控屏", "specification", "IPS 1.4英寸", "supplier", "京东方", "unit", "片"));
        createNode(Map.of("type", "Material", "code", "M004", "name", "锂离子电芯", "specification", "3.7V 200mAh", "supplier", "宁德时代", "unit", "个"));
        createNode(Map.of("type", "Material", "code", "M005", "name", "蓝牙模块", "specification", "BLE 5.0", "supplier", "Nordic", "unit", "个"));
        createNode(Map.of("type", "Material", "code", "M006", "name", "传感器模块", "specification", "心率+加速度", "supplier", "ST", "unit", "个"));
        createNode(Map.of("type", "Material", "code", "M007", "name", "硅胶膜", "specification", "0.3mm", "supplier", "道康宁", "unit", "片"));

        createNode(Map.of("type", "Equipment", "code", "E001", "name", "SMT贴片机", "model", "JUKI RX-7", "location", "A车间1号线", "status", "运行中"));
        createNode(Map.of("type", "Equipment", "code", "E002", "name", "回流焊机", "model", "Vitronics XPM3", "location", "A车间1号线", "status", "运行中"));
        createNode(Map.of("type", "Equipment", "code", "E003", "name", "AOI检测仪", "model", "Omron VT-S500", "location", "A车间1号线", "status", "运行中"));
        createNode(Map.of("type", "Equipment", "code", "E004", "name", "真空贴合机", "model", "凯盛K-300", "location", "B车间2号线", "status", "运行中"));
        createNode(Map.of("type", "Equipment", "code", "E005", "name", "防水测试设备", "model", "海瑞思HS-500", "location", "B车间2号线", "status", "运行中"));

        createRelationship("Process", "P001", "Defect", "D001", "HAS_DEFECT");
        createRelationship("Process", "P001", "Defect", "D002", "HAS_DEFECT");
        createRelationship("Process", "P001", "Defect", "D003", "HAS_DEFECT");
        createRelationship("Process", "P001", "Defect", "D004", "HAS_DEFECT");
        createRelationship("Process", "P001", "Material", "M001", "USES_MATERIAL");
        createRelationship("Process", "P001", "Material", "M002", "USES_MATERIAL");
        createRelationship("Process", "P001", "Equipment", "E001", "USES_EQUIPMENT");

        createRelationship("Process", "P002", "Defect", "D001", "HAS_DEFECT");
        createRelationship("Process", "P002", "Defect", "D002", "HAS_DEFECT");
        createRelationship("Process", "P002", "Equipment", "E002", "USES_EQUIPMENT");

        createRelationship("Process", "P003", "Defect", "D001", "HAS_DEFECT");
        createRelationship("Process", "P003", "Defect", "D002", "HAS_DEFECT");
        createRelationship("Process", "P003", "Defect", "D003", "HAS_DEFECT");
        createRelationship("Process", "P003", "Equipment", "E003", "USES_EQUIPMENT");

        createRelationship("Process", "P004", "Defect", "D005", "HAS_DEFECT");
        createRelationship("Process", "P004", "Defect", "D006", "HAS_DEFECT");
        createRelationship("Process", "P004", "Material", "M003", "USES_MATERIAL");
        createRelationship("Process", "P004", "Equipment", "E004", "USES_EQUIPMENT");

        createRelationship("Process", "P005", "Defect", "D007", "HAS_DEFECT");
        createRelationship("Process", "P005", "Equipment", "E005", "USES_EQUIPMENT");

        createRelationship("Defect", "D001", "Cause", "C001", "CAUSED_BY");
        createRelationship("Defect", "D001", "Cause", "C002", "CAUSED_BY");
        createRelationship("Defect", "D001", "Cause", "C003", "CAUSED_BY");
        createRelationship("Defect", "D001", "Cause", "C004", "CAUSED_BY");
        createRelationship("Defect", "D001", "Solution", "S001", "SOLVED_BY");
        createRelationship("Defect", "D001", "Solution", "S002", "SOLVED_BY");
        createRelationship("Defect", "D001", "Solution", "S003", "SOLVED_BY");
        createRelationship("Defect", "D001", "Solution", "S004", "SOLVED_BY");

        createRelationship("Defect", "D002", "Cause", "C005", "CAUSED_BY");
        createRelationship("Defect", "D002", "Cause", "C006", "CAUSED_BY");
        createRelationship("Defect", "D002", "Solution", "S005", "SOLVED_BY");
        createRelationship("Defect", "D002", "Solution", "S006", "SOLVED_BY");

        createRelationship("Defect", "D003", "Cause", "C005", "CAUSED_BY");
        createRelationship("Defect", "D003", "Solution", "S005", "SOLVED_BY");

        createRelationship("Defect", "D004", "Cause", "C005", "CAUSED_BY");
        createRelationship("Defect", "D004", "Solution", "S005", "SOLVED_BY");

        createRelationship("Defect", "D005", "Cause", "C007", "CAUSED_BY");
        createRelationship("Defect", "D005", "Cause", "C008", "CAUSED_BY");
        createRelationship("Defect", "D005", "Solution", "S007", "SOLVED_BY");
        createRelationship("Defect", "D005", "Solution", "S008", "SOLVED_BY");

        createRelationship("Defect", "D006", "Cause", "C007", "CAUSED_BY");
        createRelationship("Defect", "D006", "Solution", "S007", "SOLVED_BY");

        createRelationship("Defect", "D007", "Cause", "C009", "CAUSED_BY");
        createRelationship("Defect", "D007", "Cause", "C010", "CAUSED_BY");
        createRelationship("Defect", "D007", "Solution", "S009", "SOLVED_BY");
        createRelationship("Defect", "D007", "Solution", "S010", "SOLVED_BY");

        log.info("知识图谱初始化完成，节点数: {}, 关系数: {}", nodes.size(), relationships.size());
    }

    @Override
    public List<Map<String, Object>> findAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    @Override
    public List<Map<String, Object>> findAllRelationships() {
        return new ArrayList<>(relationships);
    }

    @Override
    public Map<String, Object> queryDefectAnalysis(String defectName) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> defects = nodes.values().stream()
                .filter(n -> "Defect".equals(n.get("type")))
                .filter(n -> defectName == null || defectName.isEmpty() ||
                        n.get("name").toString().toLowerCase().contains(defectName.toLowerCase()))
                .toList();

        if (defects.isEmpty()) {
            result.put("defect", null);
            result.put("causes", Collections.emptyList());
            result.put("solutions", Collections.emptyList());
            return result;
        }

        Map<String, Object> defect = defects.get(0);
        String defectCode = defect.get("code").toString();

        List<Map<String, Object>> causes = relationships.stream()
                .filter(r -> "CAUSED_BY".equals(r.get("relationType")) && defectCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Cause:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();

        List<Map<String, Object>> solutions = relationships.stream()
                .filter(r -> "SOLVED_BY".equals(r.get("relationType")) && defectCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Solution:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();

        result.put("defect", defect);
        result.put("causes", causes);
        result.put("solutions", solutions);
        return result;
    }

    @Override
    public List<Map<String, Object>> queryProcessDefects(String processCode) {
        return relationships.stream()
                .filter(r -> "HAS_DEFECT".equals(r.get("relationType")) && processCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Defect:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Map<String, Object> queryNodeDetails(String nodeType, String nodeCode) {
        return nodes.get(nodeType + ":" + nodeCode);
    }

    @Override
    public void createNode(Map<String, Object> nodeData) {
        String type = nodeData.get("type").toString();
        String code = nodeData.get("code").toString();
        nodes.put(type + ":" + code, nodeData);
    }

    @Override
    public void createRelationship(String sourceType, String sourceCode, String targetType, String targetCode, String relationType) {
        Map<String, Object> rel = new LinkedHashMap<>();
        rel.put("sourceType", sourceType);
        rel.put("sourceCode", sourceCode);
        rel.put("targetType", targetType);
        rel.put("targetCode", targetCode);
        rel.put("relationType", relationType);
        relationships.add(rel);
    }

    @Override
    public void deleteNode(String nodeType, String nodeCode) {
        nodes.remove(nodeType + ":" + nodeCode);
        relationships.removeIf(r ->
                (nodeType.equals(r.get("sourceType")) && nodeCode.equals(r.get("sourceCode"))) ||
                (nodeType.equals(r.get("targetType")) && nodeCode.equals(r.get("targetCode"))));
    }

    @Override
    public long countNodesByType(String nodeType) {
        return nodes.values().stream().filter(n -> nodeType.equals(n.get("type"))).count();
    }

    @Override
    public Map<String, Object> queryMultiPathAnalysis(String defectName) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<Map<String, Object>> defects = nodes.values().stream()
                .filter(n -> "Defect".equals(n.get("type")))
                .filter(n -> defectName == null || defectName.isEmpty() ||
                        n.get("name").toString().toLowerCase().contains(defectName.toLowerCase()))
                .toList();

        if (defects.isEmpty()) {
            result.put("defect", null);
            result.put("paths", Collections.emptyList());
            return result;
        }

        Map<String, Object> defect = defects.get(0);
        String defectCode = defect.get("code").toString();

        List<Map<String, Object>> causes = relationships.stream()
                .filter(r -> "CAUSED_BY".equals(r.get("relationType")) && defectCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Cause:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();

        List<Map<String, Object>> solutions = relationships.stream()
                .filter(r -> "SOLVED_BY".equals(r.get("relationType")) && defectCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Solution:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();

        List<Map<String, Object>> relatedProcesses = relationships.stream()
                .filter(r -> "HAS_DEFECT".equals(r.get("relationType")) && defectCode.equals(r.get("targetCode")))
                .map(r -> nodes.get("Process:" + r.get("sourceCode")))
                .filter(Objects::nonNull)
                .toList();

        List<Map<String, Object>> paths = new ArrayList<>();
        for (Map<String, Object> cause : causes) {
            for (Map<String, Object> solution : solutions) {
                Map<String, Object> path = new LinkedHashMap<>();
                path.put("cause", cause);
                path.put("solution", solution);
                path.put("confidence", (Double) cause.getOrDefault("probability", 0.5) * 
                                      (Double) solution.getOrDefault("effectiveness", 0.8));
                paths.add(path);
            }
        }

        paths.sort((a, b) -> Double.compare((Double) b.get("confidence"), (Double) a.get("confidence")));

        result.put("defect", defect);
        result.put("relatedProcesses", relatedProcesses);
        result.put("causes", causes);
        result.put("solutions", solutions);
        result.put("paths", paths);
        return result;
    }

    @Override
    public List<Map<String, Object>> searchNodes(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return findAllNodes();
        }
        String lowerKeyword = keyword.toLowerCase();
        return nodes.values().stream()
                .filter(n -> n.get("name") != null && n.get("name").toString().toLowerCase().contains(lowerKeyword)
                        || n.get("description") != null && n.get("description").toString().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    @Override
    public List<Map<String, Object>> queryProcessMaterials(String processCode) {
        return relationships.stream()
                .filter(r -> "USES_MATERIAL".equals(r.get("relationType")) && processCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Material:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Map<String, Object>> queryProcessEquipments(String processCode) {
        return relationships.stream()
                .filter(r -> "USES_EQUIPMENT".equals(r.get("relationType")) && processCode.equals(r.get("sourceCode")))
                .map(r -> nodes.get("Equipment:" + r.get("targetCode")))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Map<String, Object> getGraphData() {
        Map<String, Object> graphData = new LinkedHashMap<>();
        
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (Map<String, Object> node : nodes.values()) {
            Map<String, Object> formattedNode = new LinkedHashMap<>(node);
            formattedNode.put("id", node.get("type") + ":" + node.get("code"));
            nodeList.add(formattedNode);
        }
        graphData.put("nodes", nodeList);
        
        List<Map<String, Object>> relationList = new ArrayList<>();
        for (Map<String, Object> rel : relationships) {
            Map<String, Object> formattedRel = new LinkedHashMap<>(rel);
            formattedRel.put("source", rel.get("sourceType") + ":" + rel.get("sourceCode"));
            formattedRel.put("target", rel.get("targetType") + ":" + rel.get("targetCode"));
            relationList.add(formattedRel);
        }
        graphData.put("edges", relationList);
        
        return graphData;
    }
}
