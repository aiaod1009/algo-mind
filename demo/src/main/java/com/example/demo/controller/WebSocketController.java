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

    private static final String ASSISTANT_SYSTEM_PROMPT = """
            你是一位专业、耐心、鼓励式的算法学习助手。

            你的职责：
            - 分析学生当前的薄弱点和学习节奏
            - 解答算法与数据结构相关问题
            - 根据学生画像给出更有针对性的练习建议
            - 结合上下文保持连续对话，不要每次都重新开始

            回答要求：
            - 使用中文
            - 语气友好、直接、清晰
            - 优先给出结论，再给简短解释
            - 需要分步骤时用清晰的编号或短列表
            - 涉及知识点时尽量给出简短示例
            - 如果用户问的是学习计划或错题分析，要结合提供的上下文回答
            """;

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

        // 构建消息列表，包含历史对话上下文
        List<ChatMessage> messages = new ArrayList<>();

        messages.add(ChatMessage.system(ASSISTANT_SYSTEM_PROMPT));

        ChatMessage contextMessage = buildContextMessage(request.get("context"));
        if (contextMessage != null) {
            messages.add(contextMessage);
        }

        // 添加历史消息（如果存在）
        Object historyObj = request.get("messages");
        if (historyObj instanceof List) {
            try {
                List<?> historyList = (List<?>) historyObj;
                for (Object item : historyList) {
                    if (item instanceof Map) {
                        Map<?, ?> msgMap = (Map<?, ?>) item;
                        String role = stringValue(msgMap.get("role"));
                        String msgContent = stringValue(msgMap.get("content"));
                        if (role != null && !role.isBlank() && msgContent != null && !msgContent.isBlank()) {
                            messages.add(new ChatMessage(role, msgContent));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析历史消息失败，将只使用当前消息", e);
            }
        }

        // 添加当前用户消息
        messages.add(new ChatMessage("user", content));

        StringBuilder accumulatedContent = new StringBuilder();
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
        }, () -> {
            try {
                sendJson(
                        session,
                        Map.of(
                                "type", "assistant",
                                "status", "completed",
                                "id", messageId,
                                "content", accumulatedContent.toString()));
            } catch (IOException ioException) {
                log.error("Failed to send completed websocket message", ioException);
            }
        }, exception -> {
            try {
                sendErrorMessage(session, "AI 服务调用失败: " + exception.getMessage());
            } catch (Exception e) {
                log.error("Failed to send error websocket message", e);
            }
        });
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

    @SuppressWarnings("unchecked")
    private ChatMessage buildContextMessage(Object contextObj) {
        if (!(contextObj instanceof Map)) {
            return null;
        }

        Map<String, Object> context = (Map<String, Object>) contextObj;
        StringBuilder builder = new StringBuilder("以下是当前学生画像，请结合这些信息回答，但不要逐条复述：\n");
        boolean hasContent = false;

        String track = stringValue(context.get("track"));
        if (track != null && !track.isBlank()) {
            String trackLabel = stringValue(context.get("trackLabel"));
            builder.append("- 当前赛道：").append(trackLabel != null && !trackLabel.isBlank() ? trackLabel : track).append('\n');
            hasContent = true;
        }

        Object weeklyGoal = context.get("weeklyGoal");
        if (weeklyGoal != null) {
            builder.append("- 周目标：").append(weeklyGoal).append(" 个关卡\n");
            hasContent = true;
        }

        Object weakTopics = context.get("weakTopics");
        if (weakTopics instanceof List) {
            List<?> topics = (List<?>) weakTopics;
            if (!topics.isEmpty()) {
                builder.append("- 薄弱点：").append(String.join("、", topics.stream().map(String::valueOf).toList())).append('\n');
                hasContent = true;
            }
        }

        Object strongTopics = context.get("strongTopics");
        if (strongTopics instanceof List) {
            List<?> topics = (List<?>) strongTopics;
            if (!topics.isEmpty()) {
                builder.append("- 擅长点：").append(String.join("、", topics.stream().map(String::valueOf).toList())).append('\n');
                hasContent = true;
            }
        }

        Object totalErrors = context.get("totalErrors");
        if (totalErrors != null) {
            builder.append("- 累计错题：").append(totalErrors).append(" 道\n");
            hasContent = true;
        }

        Object consistencyScore = context.get("consistencyScore");
        if (consistencyScore != null) {
            builder.append("- 坚持指数：").append(consistencyScore).append("/100\n");
            hasContent = true;
        }

        return hasContent ? ChatMessage.system(builder.toString()) : null;
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
