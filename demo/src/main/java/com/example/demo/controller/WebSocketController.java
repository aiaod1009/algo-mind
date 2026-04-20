package com.example.demo.controller;

import com.example.demo.ai.DouBaoAiService;
import com.example.demo.dto.ai.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        log.info("WebSocket connection established: {}", session.getId());
        sendJson(
                session,
                Map.of(
                        "type", "system",
                        "status", "connected",
                        "content", "连接成功"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> request = objectMapper.readValue(message.getPayload(), Map.class);
            String type = stringValue(request.get("type"));

            switch (type) {
                case "chat":
                case "message":
                    handleChatMessage(session, request);
                    break;
                case "clear":
                    handleClearMessage(session);
                    break;
                case "cancel":
                    handleCancelMessage(session);
                    break;
                case "ping":
                    handlePingMessage(session);
                    break;
                default:
                    log.warn("Unknown websocket message type: {}", type);
            }
        } catch (Exception exception) {
            log.error("Failed to handle websocket message", exception);
            sendErrorMessage(session, "处理消息失败: " + exception.getMessage());
        }
    }

    private void handleChatMessage(WebSocketSession session, Map<String, Object> request) throws Exception {
        String content = stringValue(request.get("content"));
        String messageId = resolveMessageId(request.get("messageId"));

        if (content == null || content.isBlank()) {
            sendErrorMessage(session, "消息内容不能为空");
            return;
        }

        sendJson(
                session,
                Map.of(
                        "type", "assistant",
                        "status", "thinking",
                        "id", messageId,
                        "content", "AI 正在思考..."));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", content));

        StringBuilder accumulatedContent = new StringBuilder();
        try {
            douBaoAiService.sendMessageStream(messages, chunk -> {
                if (chunk == null || chunk.isBlank() || !session.isOpen()) {
                    return;
                }

                accumulatedContent.append(chunk);
                try {
                    sendJson(
                            session,
                            Map.of(
                                    "type", "assistant",
                                    "status", "streaming",
                                    "id", messageId,
                                    "content", accumulatedContent.toString()));
                } catch (IOException ioException) {
                    log.error("Failed to stream websocket assistant response", ioException);
                }
            });

            sendJson(
                    session,
                    Map.of(
                            "type", "assistant",
                            "status", "completed",
                            "id", messageId,
                            "content", accumulatedContent.toString()));
        } catch (Exception exception) {
            log.error("Failed to call AI service", exception);
            sendErrorMessage(session, "AI 服务调用失败: " + exception.getMessage());
        }
    }

    private void handleClearMessage(WebSocketSession session) throws Exception {
        sendJson(
                session,
                Map.of(
                        "type", "system",
                        "status", "cleared",
                        "content", "对话已清空"));
    }

    private void handleCancelMessage(WebSocketSession session) throws Exception {
        sendJson(
                session,
                Map.of(
                        "type", "system",
                        "status", "cancelled",
                        "content", "已取消生成"));
    }

    private void handlePingMessage(WebSocketSession session) throws Exception {
        sendJson(
                session,
                Map.of(
                        "type", "system",
                        "status", "pong",
                        "content", "pong"));
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws Exception {
        sendJson(
                session,
                Map.of(
                        "type", "error",
                        "content", errorMessage));
    }

    private void sendJson(WebSocketSession session, Map<String, Object> payload) throws IOException {
        if (!session.isOpen()) {
            return;
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private String resolveMessageId(Object rawMessageId) {
        String messageId = stringValue(rawMessageId);
        return messageId == null || messageId.isBlank() ? UUID.randomUUID().toString() : messageId;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", session.getId(), exception);
        sessions.remove(session.getId());
        if (session.isOpen()) {
            session.close();
        }
    }
}
