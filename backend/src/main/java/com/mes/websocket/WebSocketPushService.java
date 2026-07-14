package com.mes.websocket;

import com.mes.config.MesWebSocketEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebSocketPushService {

    @Autowired
    private ObjectMapper objectMapper;

    public void pushExceptionMessage(String type, Object payload) {
        try {
            ObjectNode message = objectMapper.createObjectNode();
            message.put("type", type);
            message.set("payload", objectMapper.valueToTree(payload));
            String jsonStr = objectMapper.writeValueAsString(message);
            MesWebSocketEndpoint.sendMessageToAll(jsonStr);
            log.info("推送WebSocket消息: type={}", type);
        } catch (Exception e) {
            log.error("推送WebSocket消息失败: {}", e.getMessage());
        }
    }

    public void pushNewException(Long exceptionId, String exceptionNo, String exceptionType, String description) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("id", exceptionId);
        payload.put("exceptionNo", exceptionNo);
        payload.put("exceptionType", exceptionType);
        payload.put("exceptionDesc", description);
        payload.put("status", 0);
        payload.put("statusText", "待处理");
        payload.put("createTime", java.time.LocalDateTime.now().toString());
        pushExceptionMessage("NEW_EXCEPTION", payload);
    }

    public void pushExceptionHandled(Long exceptionId, Integer status, String handler) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("id", exceptionId);
        payload.put("status", status);
        payload.put("handler", handler != null ? handler : "");
        payload.put("handleTime", java.time.LocalDateTime.now().toString());
        pushExceptionMessage("EXCEPTION_HANDLED", payload);
    }

    public void pushExceptionResolved(Long exceptionId) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("id", exceptionId);
        payload.put("status", 3);
        payload.put("statusText", "已闭环");
        payload.put("resolveTime", java.time.LocalDateTime.now().toString());
        pushExceptionMessage("EXCEPTION_RESOLVED", payload);
    }

    public void pushProgressUpdate(Long workOrderId, String workOrderNo, String processName, Integer addQuantity) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("workOrderId", workOrderId);
        payload.put("workOrderNo", workOrderNo);
        payload.put("processName", processName);
        payload.put("addQuantity", addQuantity);
        payload.put("reportTime", java.time.LocalDateTime.now().toString());
        pushExceptionMessage("PROGRESS_UPDATE", payload);
    }
}