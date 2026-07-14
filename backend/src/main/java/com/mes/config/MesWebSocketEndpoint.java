package com.mes.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@ServerEndpoint("/ws/mes")
public class MesWebSocketEndpoint {

    private Session session;

    private static CopyOnWriteArraySet<MesWebSocketEndpoint> webSocketSet = new CopyOnWriteArraySet<>();

    private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketSet.add(this);
        sessionMap.put(session.getId(), session);
        log.info("WebSocket连接已建立，当前在线数: {}", webSocketSet.size());
    }

    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        sessionMap.remove(this.session.getId());
        log.info("WebSocket连接已关闭，当前在线数: {}", webSocketSet.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到WebSocket消息: {}", message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: {}", error.getMessage());
    }

    public static void sendMessageToAll(String message) {
        for (MesWebSocketEndpoint item : webSocketSet) {
            try {
                item.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("发送消息失败: {}", e.getMessage());
            }
        }
    }

    public static int getOnlineCount() {
        return webSocketSet.size();
    }
}
