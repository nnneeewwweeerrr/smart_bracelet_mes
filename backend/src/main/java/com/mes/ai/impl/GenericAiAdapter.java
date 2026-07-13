package com.mes.ai.impl;

import com.mes.ai.AiModelAdapter;
import com.mes.ai.ChatMessage;
import com.mes.config.AiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GenericAiAdapter implements AiModelAdapter {

    private final AiConfig aiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, List<ChatMessage>> conversationHistory = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            你是智能手环MES系统的AI生产助手，具备生产工艺、质量管控、设备管理、数据分析等专业能力，
            服务于智能手环生产制造全流程。

            【核心职责】
            1. 工艺SOP指导：提供SMT贴片、回流焊接、屏幕贴合、防水测试、功能测试等工序的标准操作步骤和参数规范
            2. 生产数据查询：基于实时业务数据回答库存、产量、良率、工单进度等数据类问题，用表格或分点呈现
            3. 异常处理方案：分析不良现象原因，给出排查步骤和解决措施
            4. MES系统操作：指导工单派发、报工、质检、入库出库等系统操作
            5. 设备维护：提供设备点检、保养、参数校准指引

            【回答规范】
            1. 只回答与智能手环生产相关的问题，对无关话题礼貌拒绝
            2. 回答基于参考知识和业务数据，不得编造，引用来源在末尾标注
            3. 结构清晰：使用标题、分点、表格组织内容，数据类问题优先用表格
            4. 参数准确：工艺参数需给出范围值和标准值，注明单位
            5. 若信息不足或无对应规范，明确告知"暂无对应规范，请联系技术员"

            【智能手环生产工艺知识库】
            - SMT贴片：精度±0.05mm，元件0402/0603/QFP/BGA，首件必检
            - 回流焊接：预热150-180°C，回流区240-260°C，焊接60-90秒，风冷降温
            - 屏幕贴合：真空贴合，压力0.4-0.6MPa，温度40-60°C，真空度≤-95kPa
            - 防水测试：1.5米水压，30分钟，无进水功能正常为合格
            - 功能测试：蓝牙连接、心率监测、运动计步、触屏响应、电池续航
            - 质检标准：IQC来料/IPQC过程/FQC成品/OQC出货，result=1合格，0不合格
            - 工单状态：0待派发、1执行中、2已完成
            - 报工操作：生产进度模块选择工单→填写合格/不合格数量→不合格需填原因
            - 异常上报：异常处理模块填写类型、描述、位置，通知班组长
            """;

    private static final Set<String> ALLOWED_KEYWORDS = Set.of(
            "工序", "工艺", "SOP", "焊接", "贴片", "贴合", "测试", "组装",
            "温度", "压力", "参数", "异常", "不良", "故障", "处理",
            "工单", "报工", "入库", "出库", "库存", "采购", "审核",
            "质检", "检验", "合格", "合格率", "不合格", "良率", "产量",
            "设备", "维护", "保养", "点检",
            "蓝牙", "心率", "计步", "防水", "屏幕", "电池", "主板",
            "生产", "进度", "stock", "production"
    );

    public GenericAiAdapter(AiConfig aiConfig) {
        this.aiConfig = aiConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generate(String prompt) {
        if (!isAllowedQuestion(prompt)) {
            return "抱歉，我只能回答与智能手环生产相关的问题，如工艺参数、系统操作、异常处理等。";
        }

        if (!isApiConfigured()) {
            return generateMockAnswer(prompt);
        }

        try {
            String fullPrompt = SYSTEM_PROMPT + "\n\n用户问题：" + prompt + "\n\n请回答：";
            return callApi(fullPrompt);
        } catch (Exception e) {
            log.error("AI API call failed", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public String generateWithHistory(String prompt, List<ChatMessage> history) {
        if (!isAllowedQuestion(prompt)) {
            return "抱歉，我只能回答与智能手环生产相关的问题。";
        }

        if (!isApiConfigured()) {
            return generateMockAnswer(prompt);
        }

        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(SYSTEM_PROMPT));
            if (history != null) {
                messages.addAll(history);
            }
            messages.add(ChatMessage.user(prompt));
            return callApiWithMessages(messages);
        } catch (Exception e) {
            log.error("AI API call failed", e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    private boolean isAllowedQuestion(String prompt) {
        String lowerPrompt = prompt.toLowerCase();
        for (String keyword : ALLOWED_KEYWORDS) {
            if (lowerPrompt.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isApiConfigured() {
        return aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty()
                && aiConfig.getApiUrl() != null && !aiConfig.getApiUrl().isEmpty();
    }

    private String generateMockAnswer(String prompt) {
        String lowerPrompt = prompt.toLowerCase();

        if (lowerPrompt.contains("焊接") || lowerPrompt.contains("温度")) {
            return """
                    焊接工序参数标准：
                    - 回流焊温度：240-260°C
                    - 预热温度：150-180°C
                    - 焊接时间：60-90秒
                    - 冷却方式：风冷降温
                    
                    操作要点：
                    1. 检查焊膏印刷质量
                    2. 确认贴片元件位置正确
                    3. 设定正确的温度曲线
                    4. 焊接后进行AOI检测
                    """;
        }

        if (lowerPrompt.contains("贴片") || lowerPrompt.contains("SMT")) {
            return """
                    SMT贴片工序标准：
                    - 设备：SMT全自动贴片机
                    - 贴片精度：±0.05mm
                    - 元件类型：0402、0603、QFP、BGA等
                    
                    操作要点：
                    1. 准备PCB板和元器件
                    2. 装载贴片机程序
                    3. 校准吸嘴和飞达
                    4. 启动贴片机，实时监控
                    5. 首件确认质量
                    """;
        }

        if (lowerPrompt.contains("贴合") || lowerPrompt.contains("屏幕")) {
            return """
                    屏幕贴合工序参数：
                    - 贴合方式：真空贴合
                    - 贴合压力：0.4-0.6MPa
                    - 贴合温度：40-60°C
                    - 真空度：≤-95kPa
                    
                    常见不良及处理：
                    - 气泡：检查清洁度，调整贴合参数
                    - 偏移：校准定位治具
                    - 气泡残留：延长保压时间
                    """;
        }

        if (lowerPrompt.contains("防水") || lowerPrompt.contains("测试")) {
            return """
                    防水测试标准：
                    - 测试水压：1.5米水深
                    - 测试时间：30分钟
                    - 判定标准：无进水、功能正常
                    
                    测试步骤：
                    1. 密封产品接口
                    2. 放入防水测试设备
                    3. 设定测试参数并启动
                    4. 测试完成后检查是否进水
                    5. 记录测试结果
                    """;
        }

        if (lowerPrompt.contains("异常") || lowerPrompt.contains("不良") || lowerPrompt.contains("处理")) {
            return """
                    异常处理流程：
                    1. 发现异常后立即停机
                    2. 在MES系统异常处理模块上报
                    3. 填写异常类型、描述、位置
                    4. 通知班组长或技术员
                    5. 等待处理方案并执行
                    6. 记录处理结果和预防措施
                    
                    常见异常类型：虚焊、短路、缺件、偏移、气泡、划痕
                    """;
        }

        if (lowerPrompt.contains("工单") || lowerPrompt.contains("报工")) {
            return """
                    工单操作指引：
                    - 查看工单：进入生产管理→生产工单
                    - 派发工单：选择工单点击"派发"按钮
                    - 报工操作：进入生产进度→选择工单→填写合格/不合格数量
                    - 工单结案：完成所有报工后点击"结案"
                    
                    报工注意事项：
                    1. 如实填写合格与不合格数量
                    2. 不合格品需填写原因
                    3. 报工后不可修改，请确认无误
                    """;
        }

        if (lowerPrompt.contains("质检") || lowerPrompt.contains("检验")) {
            return """
                    质检操作指引：
                    - 任务列表：质量管理→质检任务
                    - 执行质检：选择任务→填写检验结果
                    - 合格判定：根据标准值判定合格/不合格
                    - 记录保存：填写处理意见并保存
                    
                    检验类型：
                    - IQC：来料检验
                    - IPQC：过程检验
                    - FQC：成品检验
                    - OQC：出货检验
                    """;
        }

        return """
                您好！我是智能手环MES系统的AI生产助手。
                
                我可以帮助您解决以下问题：
                1. 工艺参数查询（焊接温度、贴片精度、贴合压力等）
                2. 操作步骤指引（工单派发、报工、质检流程等）
                3. 异常处理方案（不良现象分析、解决措施等）
                4. 设备使用说明（贴片机、测试设备等）
                
                请直接提出您的问题，例如：
                - "焊接工序的标准温度是多少？"
                - "屏幕贴合有气泡怎么处理？"
                - "采购单怎么审核？"
                """;
    }

    private String callApi(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getModelName());
        body.put("prompt", prompt);
        body.put("max_tokens", aiConfig.getMaxTokens());
        body.put("temperature", aiConfig.getTemperature());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                aiConfig.getApiUrl(), HttpMethod.POST, entity, String.class);

        return parseApiResponse(response.getBody());
    }

    private String callApiWithMessages(List<ChatMessage> messages) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiConfig.getModelName());
        body.put("messages", messages);
        body.put("max_tokens", aiConfig.getMaxTokens());
        body.put("temperature", aiConfig.getTemperature());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                aiConfig.getApiUrl(), HttpMethod.POST, entity, String.class);

        return parseApiResponse(response.getBody());
    }

    private String parseApiResponse(String response) throws Exception {
        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        if (result.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                if (choice.containsKey("text")) {
                    return choice.get("text").toString().trim();
                }
                if (choice.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return message.get("content").toString().trim();
                }
            }
        }
        return response;
    }
}
