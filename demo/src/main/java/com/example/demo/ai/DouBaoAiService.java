package com.example.demo.ai;

import com.example.demo.dto.ai.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ExecutorService AI_STREAM_POOL = new ThreadPoolExecutor(
            Math.max(4, Runtime.getRuntime().availableProcessors() * 2),
            Math.max(8, Runtime.getRuntime().availableProcessors() * 4),
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> new Thread(r, "ai-stream-pool-" + r.hashCode())
    );

    private final RestTemplate restTemplate;
    private final DouBaoProperties properties;

    public String sendToAi(String userText) {
        try {
            return requestText(buildSingleUserMessages(userText));
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
        return requestTextFast(messages);
    }

    public SseEmitter sendToAiStream(String userText) {
        return sendToAiStream(buildSingleUserMessages(userText), null);
    }

    public SseEmitter sendToAiStreamFast(String userText) {
        SseEmitter emitter = new SseEmitter(60000L);
        sendToAiStreamFast(buildSingleUserMessages(userText), emitter, null);
        return emitter;
    }

    public void sendToAiStreamFast(String userText, SseEmitter emitter) {
        sendToAiStreamFast(buildSingleUserMessages(userText), emitter, null);
    }

    public SseEmitter sendToAiStreamFast(List<ChatMessage> messages) {
        SseEmitter emitter = new SseEmitter(60000L);
        sendToAiStreamFast(messages, emitter, null);
        return emitter;
    }

    public SseEmitter sendToAiStreamFast(List<ChatMessage> messages, String fallbackContent) {
        SseEmitter emitter = new SseEmitter(60000L);
        sendToAiStreamFast(messages, emitter, fallbackContent);
        return emitter;
    }

    public void sendToAiStreamFast(List<ChatMessage> messages, SseEmitter emitter) {
        sendToAiStreamFast(messages, emitter, null);
    }

    public void sendToAiStreamFast(List<ChatMessage> messages, SseEmitter emitter, String fallbackContent) {
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            completeWithError(emitter, fallbackContent != null ? fallbackContent : "请输入有效内容后重试");
            return;
        }

        AI_STREAM_POOL.execute(() -> streamToEmitter(sanitizedMessages, emitter, fallbackContent, true));
    }

    public SseEmitter sendToAiStream(List<ChatMessage> messages, String fallbackContent) {
        SseEmitter emitter = new SseEmitter(60000L);
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            completeWithError(emitter, fallbackContent != null ? fallbackContent : "请输入有效内容后重试");
            return emitter;
        }

        AI_STREAM_POOL.execute(() -> streamToEmitter(sanitizedMessages, emitter, fallbackContent, false));
        return emitter;
    }

    private void streamToEmitter(
            List<ChatMessage> messages,
            SseEmitter emitter,
            String fallbackContent,
            boolean fastMode
    ) {
        try {
            HttpHeaders headers = buildHeaders();
            headers.set("Accept", "text/event-stream");

            String requestBody = buildRequestBody(messages, true);
            restTemplate.execute(
                    properties.getUrl(),
                    HttpMethod.POST,
                    clientHttpRequest -> {
                        clientHttpRequest.getHeaders().putAll(headers);
                        clientHttpRequest.getBody().write(requestBody.getBytes(StandardCharsets.UTF_8));
                    },
                    clientHttpResponse -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(clientHttpResponse.getBody(), StandardCharsets.UTF_8)
                        )) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (fastMode) {
                                    fastProcessStreamLine(line, emitter);
                                } else {
                                    processAiStreamLine(line, emitter);
                                }
                            }
                        }
                        return null;
                    }
            );

            emitter.complete();
        } catch (Exception e) {
            logger.error("DouBao streaming request failed", e);
            completeWithError(emitter, fallbackContent != null ? fallbackContent : "调用失败：" + e.getMessage());
        }
    }

    private String requestText(List<ChatMessage> messages) {
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            throw new IllegalArgumentException("请输入有效内容后重试");
        }

        HttpHeaders headers = buildHeaders();
        HttpEntity<String> request = new HttpEntity<>(buildRequestBody(sanitizedMessages, false), headers);
        String response = restTemplate.postForObject(properties.getUrl(), request, String.class);

        logger.info("DouBao normal request completed");
        return extractContentFromResponse(response);
    }

    private String requestTextFast(List<ChatMessage> messages) {
        List<ChatMessage> sanitizedMessages = sanitizeMessages(messages);
        if (sanitizedMessages.isEmpty()) {
            throw new IllegalArgumentException("请输入有效内容后重试");
        }

        HttpHeaders headers = buildHeaders();
        HttpEntity<String> request = new HttpEntity<>(buildRequestBody(sanitizedMessages, false), headers);
        String response = restTemplate.postForObject(properties.getUrl(), request, String.class);

        logger.info("DouBao fast request completed");
        return extractContentFromResponse(response);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getKey());
        return headers;
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

    private String buildRequestBody(List<ChatMessage> messages, boolean stream) {
        StringBuilder builder = new StringBuilder(256 + messages.size() * 128);
        builder.append("{\"model\":\"")
                .append(escapeJson(properties.getModel()))
                .append("\"");

        if (stream) {
            builder.append(",\"stream\":true");
        }

        builder.append(",\"messages\":[");
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append("{\"role\":\"")
                    .append(escapeJson(message.getRole()))
                    .append("\",\"content\":[{\"type\":\"text\",\"text\":\"")
                    .append(escapeJson(message.getContent()))
                    .append("\"}]}");
        }
        builder.append("]}");
        return builder.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(current);
            }
        }
        return escaped.toString();
    }

    private String extractContentFromResponse(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("AI响应为空");
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new IllegalStateException("AI没有返回结果");
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new IllegalStateException("AI响应格式异常");
            }

            String content = readTextContent(message.get("content"));
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("AI返回内容为空");
            }

            return content;
        } catch (Exception parseError) {
            logger.warn("Failed to parse normal AI response as JSON, falling back to substring extraction", parseError);
            String content = extractContentBySubstring(response);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("AI没有返回结果", parseError);
            }
            return content;
        }
    }

    private void processAiStreamLine(String line, SseEmitter emitter) {
        pushStreamContent(line, emitter, true);
    }

    private void fastProcessStreamLine(String line, SseEmitter emitter) {
        pushStreamContent(line, emitter, false);
    }

    private void pushStreamContent(String line, SseEmitter emitter, boolean logFailure) {
        String content = extractStreamContent(line);
        if (content == null || content.isEmpty()) {
            return;
        }

        try {
            emitter.send(content);
        } catch (IOException e) {
            if (logFailure) {
                logger.error("Normal AI streaming push failed", e);
            }
            emitter.completeWithError(e);
        }
    }

    private String extractStreamContent(String line) {
        if (line == null || line.isBlank() || !line.startsWith("data:")) {
            return null;
        }

        String data = line.substring(5).stripLeading();
        if (data.isBlank() || "[DONE]".equals(data)) {
            return null;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }

            JsonNode choice = choices.get(0);
            return firstNonEmptyText(
                    choice.path("delta").path("content"),
                    choice.path("message").path("content"),
                    choice.path("content")
            );
        } catch (Exception parseError) {
            logger.debug("Failed to parse AI streaming JSON chunk, falling back to substring extraction: {}", data, parseError);
            return extractContentBySubstring(data);
        }
    }

    private String firstNonEmptyText(JsonNode... candidates) {
        for (JsonNode candidate : candidates) {
            String text = readTextContent(candidate);
            if (text != null && !text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    private String readTextContent(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return node.asText();
        }

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : node) {
                String text = readTextContent(item);
                if (text != null) {
                    builder.append(text);
                }
            }
            return builder.length() == 0 ? null : builder.toString();
        }

        if (node.isObject()) {
            if (node.has("text")) {
                return readTextContent(node.get("text"));
            }
            if (node.has("content")) {
                return readTextContent(node.get("content"));
            }
        }

        return null;
    }

    private String extractContentBySubstring(String data) {
        int contentIdx = data.indexOf("\"content\":\"");
        if (contentIdx == -1) {
            return null;
        }

        int start = contentIdx + 11;
        int end = data.indexOf('"', start);
        while (end > start && data.charAt(end - 1) == '\\') {
            end = data.indexOf('"', end + 1);
        }
        if (end == -1) {
            return null;
        }

        String content = data.substring(start, end);
        if (content.indexOf('\\') != -1) {
            content = content.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }

        return content.isEmpty() ? null : content;
    }

    private void completeWithError(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(errorMsg == null ? "调用失败，请稍后重试" : errorMsg);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
