package com.mes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mes.ai.AiModelAdapter;
import com.mes.ai.ChatMessage;
import com.mes.common.Result;
import com.mes.entity.AiChatHistory;
import com.mes.entity.ProductionReport;
import com.mes.entity.QualityResult;
import com.mes.entity.StockInfo;
import com.mes.entity.WorkOrder;
import com.mes.kg.service.KnowledgeGraphService;
import com.mes.service.AiChatHistoryService;
import com.mes.service.AiDocumentService;
import com.mes.service.ProductionReportService;
import com.mes.service.QualityResultService;
import com.mes.service.StockInfoService;
import com.mes.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private AiModelAdapter aiModelAdapter;

    @Autowired
    private AiChatHistoryService aiChatHistoryService;

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Autowired
    private AiDocumentService documentService;

    @Autowired
    private StockInfoService stockInfoService;

    @Autowired
    private ProductionReportService productionReportService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private QualityResultService qualityResultService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> params) {
        String question = (String) params.get("question");
        String userId = (String) params.get("userId");
        String userName = (String) params.get("userName");
        List<Map<String, Object>> history = (List<Map<String, Object>>) params.get("history");

        if (question == null || question.trim().isEmpty()) {
            return Result.error("请输入问题");
        }

        // 敏感数据脱敏：手机号、邮箱替换为***
        String maskedQuestion = maskSensitiveData(question);

        try {
            List<ChatMessage> chatHistory = new ArrayList<>();
            if (history != null) {
                for (Map<String, Object> item : history) {
                    String role = (String) item.get("role");
                    String content = (String) item.get("content");
                    if (role != null && content != null) {
                        chatHistory.add(new ChatMessage(role, content));
                    }
                }
            }

            // 业务数据意图识别
            String scene = detectScene(question);
            String businessData = buildBusinessData(scene);

            Map<String, Object> ragResult = performRagRetrieval(maskedQuestion);
            String enrichedQuestion = buildEnrichedQuestion(maskedQuestion, ragResult, businessData, scene);

            String answer = aiModelAdapter.generateWithHistory(enrichedQuestion, chatHistory);

            AiChatHistory record = new AiChatHistory();
            record.setUserId(userId);
            record.setUserName(userName);
            record.setQuestion(question);
            record.setAnswer(answer);
            record.setChatStatus(0);
            record.setCreateTime(LocalDateTime.now());
            record.setContext(buildContextJson(scene, null));
            aiChatHistoryService.save(record);

            Map<String, Object> resultData = new HashMap<>();
            resultData.put("answer", answer);
            resultData.put("chatId", record.getId());
            resultData.put("sources", ragResult.get("sources"));
            resultData.put("scene", scene == null ? "general" : scene);
            return Result.success(resultData);
        } catch (Exception e) {
            log.error("AI chat error", e);

            AiChatHistory record = new AiChatHistory();
            record.setUserId(userId);
            record.setUserName(userName);
            record.setQuestion(question);
            record.setAnswer("抱歉，系统出错，请稍后再试。");
            record.setChatStatus(1);
            record.setCreateTime(LocalDateTime.now());
            aiChatHistoryService.save(record);

            return Result.error("AI服务暂时不可用");
        }
    }

    /**
     * 敏感数据脱敏：手机号、邮箱替换为***
     */
    private String maskSensitiveData(String text) {
        if (text == null) return null;
        String masked = PHONE_PATTERN.matcher(text).replaceAll("***");
        masked = EMAIL_PATTERN.matcher(masked).replaceAll("***");
        return masked;
    }

    /**
     * 业务数据意图识别
     * @return 场景：stock/production/yield/workorder，未识别返回null
     */
    private String detectScene(String question) {
        if (question == null) return null;
        String lower = question.toLowerCase();
        if (lower.contains("库存") || lower.contains("stock")) {
            return "stock";
        }
        if (lower.contains("产量") || lower.contains("production")) {
            return "production";
        }
        if (lower.contains("良率") || lower.contains("合格率")) {
            return "yield";
        }
        if (lower.contains("工单进度") || lower.contains("生产进度")) {
            return "workorder";
        }
        return null;
    }

    /**
     * 根据场景调用对应Service获取业务数据，汇总统计后拼入Prompt
     */
    private String buildBusinessData(String scene) {
        if (scene == null) return "";
        StringBuilder sb = new StringBuilder();
        try {
            switch (scene) {
                case "stock":
                    List<StockInfo> stocks = stockInfoService.list();
                    sb.append("【实时库存数据】\n");
                    sb.append("库存记录总数: ").append(stocks.size()).append("\n");
                    BigDecimal totalQty = stocks.stream()
                            .map(StockInfo::getQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    sb.append("库存总量: ").append(totalQty).append("\n");
                    sb.append("明细(物料ID | 仓库ID | 数量 | 库位):\n");
                    for (StockInfo s : stocks) {
                        sb.append(s.getMaterialId()).append(" | ")
                                .append(s.getWarehouseId()).append(" | ")
                                .append(s.getQuantity()).append(" | ")
                                .append(s.getLocation()).append("\n");
                    }
                    break;
                case "production":
                    List<ProductionReport> reports = productionReportService.list();
                    sb.append("【实时产量数据】\n");
                    sb.append("报工记录总数: ").append(reports.size()).append("\n");
                    int totalGood = reports.stream()
                            .filter(r -> r.getGoodQuantity() != null)
                            .mapToInt(ProductionReport::getGoodQuantity).sum();
                    int totalBad = reports.stream()
                            .filter(r -> r.getBadQuantity() != null)
                            .mapToInt(ProductionReport::getBadQuantity).sum();
                    sb.append("合格品总量: ").append(totalGood).append("\n");
                    sb.append("不合格品总量: ").append(totalBad).append("\n");
                    sb.append("总产量: ").append(totalGood + totalBad).append("\n");
                    break;
                case "yield":
                    List<QualityResult> results = qualityResultService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 100)).getRecords();
                    sb.append("【实时良率数据】\n");
                    sb.append("质检记录总数: ").append(results.size()).append("\n");
                    long passCount = results.stream()
                            .filter(r -> r.getResult() != null && r.getResult() == 1).count();
                    long failCount = results.stream()
                            .filter(r -> r.getResult() != null && r.getResult() == 0).count();
                    long total = passCount + failCount;
                    double yieldRate = total > 0 ? (double) passCount / total * 100 : 0;
                    sb.append("合格数: ").append(passCount).append("\n");
                    sb.append("不合格数: ").append(failCount).append("\n");
                    sb.append(String.format("良率: %.2f%%\n", yieldRate));
                    break;
                case "workorder":
                    List<WorkOrder> orders = workOrderService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 100)).getRecords();
                    sb.append("【实时工单进度数据】\n");
                    sb.append("工单总数: ").append(orders.size()).append("\n");
                    long pending = orders.stream().filter(w -> w.getStatus() != null && w.getStatus() == 0).count();
                    long running = orders.stream().filter(w -> w.getStatus() != null && w.getStatus() == 1).count();
                    long completed = orders.stream().filter(w -> w.getStatus() != null && w.getStatus() == 2).count();
                    sb.append("待派发: ").append(pending).append("\n");
                    sb.append("执行中: ").append(running).append("\n");
                    sb.append("已完成: ").append(completed).append("\n");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("获取业务数据失败 scene={}: {}", scene, e.getMessage());
            return "";
        }
        return sb.toString();
    }

    /**
     * 构建context字段JSON：存储场景信息和反馈信息
     */
    private String buildContextJson(String scene, Map<String, Object> feedback) {
        try {
            Map<String, Object> ctx = new LinkedHashMap<>();
            if (scene != null) {
                ctx.put("scene", scene);
            }
            if (feedback != null) {
                ctx.put("feedback", feedback);
            }
            if (ctx.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(ctx);
        } catch (Exception e) {
            log.warn("构建context JSON失败: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> performRagRetrieval(String question) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> sources = new ArrayList<>();

        List<Map<String, Object>> kgResults = knowledgeGraphService.searchNodes(question);
        if (!kgResults.isEmpty()) {
            for (Map<String, Object> node : kgResults) {
                Map<String, Object> source = new LinkedHashMap<>();
                source.put("type", "knowledge_graph");
                source.put("title", node.get("name"));
                source.put("content", node.get("description"));
                source.put("nodeType", node.get("type"));
                sources.add(source);
            }
        }

        Map<String, Object> defectAnalysis = knowledgeGraphService.queryDefectAnalysis(question);
        if (defectAnalysis.get("defect") != null) {
            Map<String, Object> defect = (Map<String, Object>) defectAnalysis.get("defect");
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("type", "knowledge_graph");
            source.put("title", "不良分析: " + defect.get("name"));
            source.put("content", defect.get("description"));
            sources.add(source);

            List<Map<String, Object>> causes = (List<Map<String, Object>>) defectAnalysis.get("causes");
            for (Map<String, Object> cause : causes) {
                Map<String, Object> causeSource = new LinkedHashMap<>();
                causeSource.put("type", "knowledge_graph");
                causeSource.put("title", "可能原因: " + cause.get("name"));
                causeSource.put("content", cause.get("description"));
                sources.add(causeSource);
            }

            List<Map<String, Object>> solutions = (List<Map<String, Object>>) defectAnalysis.get("solutions");
            for (Map<String, Object> solution : solutions) {
                Map<String, Object> solutionSource = new LinkedHashMap<>();
                solutionSource.put("type", "knowledge_graph");
                solutionSource.put("title", "解决方案: " + solution.get("name"));
                solutionSource.put("content", solution.get("description"));
                sources.add(solutionSource);
            }
        }

        List<Map<String, Object>> docResults = documentService.searchKnowledge(question, 5);
        if (!docResults.isEmpty()) {
            for (Map<String, Object> doc : docResults) {
                Map<String, Object> source = new LinkedHashMap<>();
                source.put("type", "document");
                source.put("title", doc.get("fileName"));
                source.put("content", doc.get("content"));
                sources.add(source);
            }
        }

        result.put("sources", sources);
        result.put("hasKnowledge", !sources.isEmpty());
        return result;
    }

    private String buildEnrichedQuestion(String question, Map<String, Object> ragResult,
                                         String businessData, String scene) {
        List<Map<String, Object>> sources = (List<Map<String, Object>>) ragResult.get("sources");
        boolean hasKnowledge = (Boolean) ragResult.get("hasKnowledge");

        StringBuilder enrichedQuestion = new StringBuilder();
        enrichedQuestion.append("请基于以下知识回答用户问题。\n\n");

        // 注入业务数据
        if (businessData != null && !businessData.isEmpty()) {
            enrichedQuestion.append(businessData).append("\n");
        }

        if (hasKnowledge) {
            enrichedQuestion.append("【参考知识】\n");
            int index = 1;
            for (Map<String, Object> source : sources) {
                String type = (String) source.get("type");
                String title = (String) source.get("title");
                String content = (String) source.get("content");

                enrichedQuestion.append(index++).append(". [")
                        .append(type.equals("knowledge_graph") ? "知识图谱" : "文档")
                        .append("] ")
                        .append(title).append("\n")
                        .append(content).append("\n\n");
            }
        } else {
            enrichedQuestion.append("【参考知识】暂无匹配的内部知识\n\n");
        }

        enrichedQuestion.append("【回答规则】\n");
        enrichedQuestion.append("1. 必须基于上述参考知识和业务数据回答，不得编造信息\n");
        enrichedQuestion.append("2. 如果参考知识中没有相关信息，明确告知\"暂无对应规范，请联系技术员\"\n");
        enrichedQuestion.append("3. 在回答末尾标注引用来源，格式：\n");
        enrichedQuestion.append("   来源：[知识图谱/文档/业务数据] 标题\n");
        enrichedQuestion.append("4. 回答要清晰、有条理，使用分点说明\n");
        enrichedQuestion.append("5. 只回答与智能手环生产相关的问题\n\n");

        enrichedQuestion.append("【用户问题】\n");
        enrichedQuestion.append(question);

        return enrichedQuestion.toString();
    }

    /**
     * 反馈接口：用户对AI回答进行有用/无用评价
     */
    @PostMapping("/feedback")
    public Result<Void> feedback(@RequestBody Map<String, Object> params) {
        Object chatIdObj = params.get("chatId");
        Object feedbackObj = params.get("feedback");
        String comment = (String) params.get("comment");

        if (chatIdObj == null) {
            return Result.error("chatId不能为空");
        }
        Long chatId = Long.valueOf(chatIdObj.toString());
        AiChatHistory record = aiChatHistoryService.getById(chatId);
        if (record == null) {
            return Result.error("对话记录不存在");
        }

        Map<String, Object> feedbackInfo = new LinkedHashMap<>();
        feedbackInfo.put("feedback", feedbackObj);
        feedbackInfo.put("comment", comment);
        feedbackInfo.put("feedbackTime", LocalDateTime.now().toString());

        // 合并已有context中的scene信息
        String scene = null;
        if (record.getContext() != null && !record.getContext().isEmpty()) {
            try {
                Map<String, Object> existing = objectMapper.readValue(record.getContext(), Map.class);
                Object sceneObj = existing.get("scene");
                if (sceneObj != null) {
                    scene = sceneObj.toString();
                }
            } catch (Exception e) {
                log.warn("解析已有context失败: {}", e.getMessage());
            }
        }
        record.setContext(buildContextJson(scene, feedbackInfo));
        aiChatHistoryService.updateById(record);
        return Result.success();
    }

    /**
     * 问答统计接口：按日统计提问量、高频问题TOP10、解决率
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        List<AiChatHistory> allRecords = aiChatHistoryService.list();

        Map<String, Object> stats = new LinkedHashMap<>();

        // 总数与解决率
        int totalCount = allRecords.size();
        long resolvedCount = allRecords.stream()
                .filter(r -> r.getChatStatus() != null && r.getChatStatus() == 0).count();
        double resolutionRate = totalCount > 0 ? (double) resolvedCount / totalCount * 100 : 0;
        stats.put("totalCount", totalCount);
        stats.put("resolvedCount", resolvedCount);
        stats.put("resolutionRate", Math.round(resolutionRate * 100) / 100.0);

        // 按日统计提问量
        Map<LocalDate, Long> dailyMap = allRecords.stream()
                .filter(r -> r.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getCreateTime().toLocalDate(),
                        Collectors.counting()));
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        dailyMap.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, Long>comparingByKey().reversed())
                .forEach(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("date", e.getKey().toString());
                    item.put("count", e.getValue());
                    dailyStats.add(item);
                });
        stats.put("dailyStats", dailyStats);

        // 高频问题TOP10
        Map<String, Long> questionFreq = allRecords.stream()
                .filter(r -> r.getQuestion() != null && !r.getQuestion().isEmpty())
                .collect(Collectors.groupingBy(AiChatHistory::getQuestion, Collectors.counting()));
        List<Map<String, Object>> topQuestions = questionFreq.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("question", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
        stats.put("topQuestions", topQuestions);

        return Result.success(stats);
    }

    @GetMapping("/history")
    public Result<IPage<AiChatHistory>> history(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userId) {
        Page<AiChatHistory> page = new Page<>(current, size);
        return Result.success(aiChatHistoryService.pageList(page, userId));
    }

    @GetMapping("/history/recent")
    public Result<List<AiChatHistory>> recentHistory(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(aiChatHistoryService.getRecentHistory(userId, limit));
    }

    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        aiChatHistoryService.removeById(id);
        return Result.success();
    }
}
