package com.example.demo.ai;

import com.example.demo.dto.ai.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DouBaoAiService {

    private static final Logger logger = LoggerFactory.getLogger(DouBaoAiService.class);
    private static final long STREAM_TIMEOUT_MS = 180_000L;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 120_000;
    private static final int DEFAULT_STREAM_READ_TIMEOUT_MS = 180_000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ExecutorService AI_STREAM_POOL = new ThreadPoolExecutor(
            Math.max(4, Runtime.getRuntime().availableProcessors() * 2),
            Math.max(8, Runtime.getRuntime().availableProcessors() * 4),
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "ai-stream-pool-" + r.hashCode())
    );

    private final DouBaoProperties properties;

    private record RequestOptions(
            String model,
            Integer maxTokens,
            Double temperature,
            Integer connectTimeoutMs,
            Integer readTimeoutMs
    ) {
    }

    public String sendToAi(String userText) {
        try {
            return requestText(buildSingleUserMessages(userText), buildDefaultRequestOptions(false));
        } catch (Exception e) {
            logger.error("DouBao request failed", e);
            return "调用失败：" + e.getMessage();
        }
    }

    public String sendToAiFast(String userText) {
        try {
            return requestTextFast(buildSingleUserMessages(userText));
        } catch (Exception e) {
            logger.error("DouBao fast request failed", e);
            return "调用失败：" + e.getMessage();
        }
    }

    public String sendToAiFast(List<ChatMessage> messages) {
        try {
            return requestTextFast(messages);
        } catch (Exception e) {
            logger.error("DouBao fast multi-turn request failed", e);
            return "调用失败：" + e.getMessage();
        }
    }

    public String sendToAiFastOrThrow(List<ChatMessage> messages) {
        return requestTextFast(messages, buildDefaultRequestOptions(false));
    }

    public String sendToAiFastOrThrowForCodeEvaluation(List<ChatMessage> messages) {
        return requestTextFast(messages, buildCodeEvaluationRequestOptions());
    }

    public SseEmitter sendToAiStream(String userText) {
        return sendToAiStream(buildSingleUserMessages(userText), null);
    }

    public SseEmitter sendToAiStreamFast(String userText) {
        SseEmitter emitter = createEmitter();
        sendToAiStreamFast(buildSingleUserMessages(userText), emitter, null);
        return emitter;
    }

    public void sendToAiStreamFast(String userText, SseEmitter emitter) {
        sendToAiStreamFast(buildSingleUserMessages(userText), emitter, null);
    }

    public SseEmitter sendToAiStreamFast(List<ChatMessage> messages) {
        SseEmitter emitter = createEmitter();
        sendToAiStreamFast(messages, emitter, null);
        return emitter;
    }

    public SseEmitter sendToAiStreamFast(List<ChatMessage> messages, String fallbackContent) {
        SseEmitter emitter = createEmitter();
        sendToAiStreamFast(messages, emitter, fallbackContent);
        return emitter;
    }

    public SseEmitter sendToAiStreamFastForCodeEvaluation(List<ChatMessage> messages, String fallbackContent) {
        SseEmitter emitter = createEmitter();
        sendToAiStreamFast(messages, emitter, fallbackContent, buildCodeEvaluationRequestOptions());
        return emitter;
    }

    public void sendToAiStreamFast(List<ChatMessage> messages, SseEmitter emitter) {
        sendToAiStreamFast(messages, emitter, null);
    }

    public void sendToAiStreamFast(List<ChatMessage> messages, SseEmitter emitter, String fallbackContent) {
        sendToAiStreamFast(messages, emitter, fallbackContent, buildDefaultRequestOptions(true));
    }

    private void sendToAiStreamFast(
            List<ChatMessage> messages,
            SseEmitter emitter,
            String fallbackContent,
            RequestOptions options
    ) {
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            completeWithError(emitter, fallbackContent != null ? fallbackContent : "请输入有效内容后重试");
            return;
        }

        AI_STREAM_POOL.execute(() -> streamToEmitter(sanitizedMessages, emitter, fallbackContent, options));
    }

    public SseEmitter sendToAiStream(List<ChatMessage> messages, String fallbackContent) {
        SseEmitter emitter = createEmitter();
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            completeWithError(emitter, fallbackContent != null ? fallbackContent : "请输入有效内容后重试");
            return emitter;
        }

        AI_STREAM_POOL.execute(() -> streamToEmitter(
                sanitizedMessages,
                emitter,
                fallbackContent,
                buildDefaultRequestOptions(true)
        ));
        return emitter;
    }

    private SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        emitter.onTimeout(() -> {
            logger.warn("AI streaming request timed out after {} ms", STREAM_TIMEOUT_MS);
            emitter.complete();
        });
        emitter.onError(error -> logger.warn("AI streaming emitter error", error));
        return emitter;
    }

    private void streamToEmitter(
            List<ChatMessage> messages,
            SseEmitter emitter,
            String fallbackContent,
            RequestOptions options
    ) {
        HttpURLConnection conn = null;
        long startedAt = System.currentTimeMillis();
        boolean firstChunkDelivered = false;
        try {
            String requestBody = buildRequestBody(messages, true, options);
            conn = openConnection(true, options);
            conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

            try (InputStream stream = getResponseStream(conn);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }

                    String content = extractStreamContent(data);
                    if (content != null && !content.isEmpty()) {
                        if (!firstChunkDelivered) {
                            firstChunkDelivered = true;
                            logger.info(
                                    "DouBao stream first chunk in {} ms, model={}, maxTokens={}",
                                    System.currentTimeMillis() - startedAt,
                                    options.model(),
                                    options.maxTokens()
                            );
                        }
                        emitter.send(content);
                    }
                }
            }
            logger.info(
                    "DouBao streaming request completed in {} ms, model={}, maxTokens={}",
                    System.currentTimeMillis() - startedAt,
                    options.model(),
                    options.maxTokens()
            );
            emitter.complete();
        } catch (Exception e) {
            logger.error("DouBao streaming request failed", e);
            completeWithError(emitter, fallbackContent != null ? fallbackContent : "调用失败：" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String requestText(List<ChatMessage> messages, RequestOptions options) {
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            throw new IllegalArgumentException("请输入有效内容后重试");
        }

        HttpURLConnection conn = null;
        long startedAt = System.currentTimeMillis();
        try {
            String requestBody = buildRequestBody(sanitizedMessages, false, options);
            conn = openConnection(false, options);
            conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

            try (InputStream stream = getResponseStream(conn);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                logger.info(
                        "DouBao request completed in {} ms, model={}, maxTokens={}",
                        System.currentTimeMillis() - startedAt,
                        options.model(),
                        options.maxTokens()
                );
                return extractContent(response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("请求失败", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String requestTextFast(List<ChatMessage> messages) {
        return requestText(messages, buildDefaultRequestOptions(false));
    }

    private String requestTextFast(List<ChatMessage> messages, RequestOptions options) {
        return requestText(messages, options);
    }

    private HttpURLConnection openConnection(boolean stream, RequestOptions options) throws IOException {
        URL url = new URL(properties.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + properties.getKey());
        if (stream) {
            conn.setRequestProperty("Accept", "text/event-stream");
        }
        conn.setDoOutput(true);
        conn.setReadTimeout(positiveOrDefault(
                options.readTimeoutMs(),
                stream ? DEFAULT_STREAM_READ_TIMEOUT_MS : DEFAULT_READ_TIMEOUT_MS
        ));
        conn.setConnectTimeout(positiveOrDefault(options.connectTimeoutMs(), DEFAULT_CONNECT_TIMEOUT_MS));
        return conn;
    }

    private InputStream getResponseStream(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        InputStream stream = responseCode >= 200 && responseCode < 300
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (stream == null) {
            throw new IOException("远程模型返回空响应，状态码: " + responseCode);
        }
        return stream;
    }

    private String buildRequestBody(List<ChatMessage> messages, boolean stream, RequestOptions options) throws IOException {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", firstNonBlank(options.model(), properties.getModel()));
        root.put("temperature", nonNullOrDefault(options.temperature(), properties.getTemperature(), 0.7));
        root.put("max_tokens", positiveOrDefault(options.maxTokens(), properties.getMaxTokens(), 1200));

        if (stream) {
            root.put("stream", true);
        }

        ArrayNode messagesArray = root.putArray("messages");
        for (ChatMessage message : messages) {
            ObjectNode msgNode = messagesArray.addObject();
            msgNode.put("role", message.getRole());
            msgNode.put("content", message.getContent());
        }

        return OBJECT_MAPPER.writeValueAsString(root);
    }

    private String extractContent(String response) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(response);
            if (root.has("error")) {
                return "错误: " + root.path("error").path("message").asText("未知错误");
            }

            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText("模型未返回有效内容");
            }

            return "模型未返回有效内容";
        } catch (Exception e) {
            logger.error("解析响应失败: {}", response, e);
            return "解析响应失败";
        }
    }

    private String extractStreamContent(String data) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }

            JsonNode delta = choices.get(0).path("delta");
            return delta.path("content").asText(null);
        } catch (Exception e) {
            logger.debug("解析流式数据失败: {}", data, e);
            return null;
        }
    }

    private List<ChatMessage> buildSingleUserMessages(String userText) {
        if (userText == null || userText.trim().isEmpty()) {
            return List.of();
        }
        return List.of(ChatMessage.user(userText.trim()));
    }

    private List<ChatMessage> sanitizeMessages(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }

        List<ChatMessage> sanitized = new ArrayList<>();
        for (ChatMessage message : messages) {
            if (message == null) {
                continue;
            }

            String content = message.getContent() == null ? "" : message.getContent().trim();
            if (content.isEmpty()) {
                continue;
            }

            sanitized.add(new ChatMessage(normalizeRole(message.getRole()), content));
        }
        return sanitized;
    }

    private String normalizeRole(String role) {
        if ("system".equalsIgnoreCase(role)) {
            return "system";
        }
        if ("assistant".equalsIgnoreCase(role)) {
            return "assistant";
        }
        return "user";
    }

    private RequestOptions buildDefaultRequestOptions(boolean stream) {
        return new RequestOptions(
                firstNonBlank(properties.getModel(), ""),
                positiveOrDefault(properties.getMaxTokens(), 1200),
                nonNullOrDefault(properties.getTemperature(), 0.7),
                DEFAULT_CONNECT_TIMEOUT_MS,
                stream ? DEFAULT_STREAM_READ_TIMEOUT_MS : DEFAULT_READ_TIMEOUT_MS
        );
    }

    private RequestOptions buildCodeEvaluationRequestOptions() {
        return new RequestOptions(
                firstNonBlank(properties.getCodeEvaluationModel(), properties.getModel()),
                positiveOrDefault(properties.getCodeEvaluationMaxTokens(), properties.getMaxTokens(), 1400),
                nonNullOrDefault(properties.getCodeEvaluationTemperature(), properties.getTemperature(), 0.2),
                positiveOrDefault(properties.getCodeEvaluationConnectTimeoutMs(), DEFAULT_CONNECT_TIMEOUT_MS),
                positiveOrDefault(properties.getCodeEvaluationReadTimeoutMs(), 45_000)
        );
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    private int positiveOrDefault(Integer value, int fallback) {
        return value != null && value > 0 ? value : fallback;
    }

    private int positiveOrDefault(Integer first, Integer second, int fallback) {
        if (first != null && first > 0) {
            return first;
        }
        if (second != null && second > 0) {
            return second;
        }
        return fallback;
    }

    private double nonNullOrDefault(Double first, Double fallback, double defaultValue) {
        if (first != null) {
            return first;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultValue;
    }

    private double nonNullOrDefault(Double value, double fallback) {
        return value != null ? value : fallback;
    }

    private void completeWithError(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(errorMsg == null ? "调用失败，请稍后重试" : errorMsg);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    // WebSocket 流式发送
    public void sendMessageStream(List<ChatMessage> messages, java.util.function.Consumer<String> callback) {
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            callback.accept("请输入有效内容后重试");
            return;
        }

        AI_STREAM_POOL.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String requestBody = buildRequestBody(sanitizedMessages, true, buildDefaultRequestOptions(true));
                conn = openConnection(true, buildDefaultRequestOptions(true));
                conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

                try (InputStream stream = getResponseStream(conn);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data:")) {
                            continue;
                        }

                        String data = line.substring(5).trim();
                        if (data.isEmpty() || "[DONE]".equals(data)) {
                            continue;
                        }

                        String content = extractStreamContent(data);
                        if (content != null && !content.isEmpty()) {
                            callback.accept(content);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("DouBao streaming request failed", e);
                callback.accept("调用失败：" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }
}
