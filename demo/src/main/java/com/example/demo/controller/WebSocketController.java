package com.example.demo.controller;

import com.example.demo.ai.DouBaoAiService;
import com.example.demo.dto.ai.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
public class WebSocketController extends TextWebSocketHandler {

    private final DouBaoAiService douBaoAiService;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public WebSocketController(DouBaoAiService douBaoAiService, ObjectMapper objectMapper) {
        this.douBaoAiService = douBaoAiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket 连接建立: {}", session.getId());
        
        // 发送连接成功消息
        Map<String, Object> response = Map.of(
                "type", "system",
                "status", "connected",
                "content", "连接成功"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到 WebSocket 消息: {}", payload);

        try {
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            String type = (String) request.get("type");

            switch (type) {
                case "chat":
                case "message":
                    handleChatMessage(session, request);
                    break;
                case "clear":
                    handleClearMessage(session);
                    break;
                case "cancel":
                    handleCancelMessage(session, request);
                    break;
                case "ping":
                    handlePingMessage(session);
                    break;
                default:
                    log.warn("未知消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
            sendErrorMessage(session, "处理消息失败: " + e.getMessage());
        }
    }

    private void handleChatMessage(WebSocketSession session, Map<String, Object> request) throws Exception {
        String content = (String) request.get("content");
        String messageId = (String) request.get("messageId");

        // 发送思考中消息
        Map<String, Object> thinkingResponse = Map.of(
                "type", "assistant",
                "status", "thinking",
                "content", "思考中"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(thinkingResponse)));

        // 准备 AI 消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", content));

        // 调用 AI 服务
        try {
            // 使用流式调用
            douBaoAiService.sendMessageStream(messages, response -> {
                try {
                    Map<String, Object> aiResponse = Map.of(
                            "type", "assistant",
                            "status", "message",
                            "content", response
                    );
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(aiResponse)));
                } catch (IOException e) {
                    log.error("发送 AI 响应失败", e);
                }
            });
        } catch (Exception e) {
            log.error("调用 AI 服务失败", e);
            sendErrorMessage(session, "AI 服务调用失败: " + e.getMessage());
        }
    }

    private void handleClearMessage(WebSocketSession session) throws Exception {
        Map<String, Object> response = Map.of(
                "type", "system",
                "status", "cleared",
                "content", "对话已清空"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleCancelMessage(WebSocketSession session, Map<String, Object> request) throws Exception {
        // 这里可以实现取消 AI 生成的逻辑
        Map<String, Object> response = Map.of(
                "type", "system",
                "status", "cancelled",
                "content", "已取消生成"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handlePingMessage(WebSocketSession session) throws Exception {
        // 回复 pong 消息
        Map<String, Object> response = Map.of(
                "type", "system",
                "status", "pong",
                "content", "pong"
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws Exception {
        Map<String, Object> response = Map.of(
                "type", "error",
                "content", errorMessage
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("WebSocket 连接关闭: {}, 状态: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误: {}", session.getId(), exception);
        sessions.remove(session.getId());
        session.close();
    }
}
