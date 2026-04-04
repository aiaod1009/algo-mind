package com.example.demo.ai;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DouBaoAiService {

    private static final Logger logger = LoggerFactory.getLogger(DouBaoAiService.class);
    private final RestTemplate restTemplate;
    private final DouBaoProperties properties;

    /**
     * 调用豆包大模型，支持自定义传入用户文本
     * @param userText 自定义传入的用户提问内容
     * @return AI 返回的响应内容
     */
    public String sendToAi(String userText) {
        // 前置校验：用户输入为空直接返回
        if (userText == null || userText.trim().isEmpty()) {
            logger.warn("用户传入的内容为空");
            return "请输入有效内容后重试";
        }

        try {
            // 1. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + properties.getKey());

            // 2. 构建请求体（复用原有逻辑，仅userText为自定义传入）
            Map<String, Object> requestBody = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", List.of(Map.of(
                                    "type", "text",
                                    "text", userText.trim()
                            ))
                    ))
            );

            // 3. 发送POST请求
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(
                    properties.getUrl(),
                    request,
                    Map.class
            );

            // 4. 打印完整响应（调试用）
            logger.info("=== 豆包API完整返回 ===");
            logger.info("{}", response);

            // 5. 解析响应（增强空指针防护）
            if (response == null) {
                return "AI响应为空";
            }
            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "AI无返回结果";
            }
            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            if (choice == null) {
                return "AI响应choices格式异常";
            }
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            if (message == null) {
                return "AI响应message格式异常";
            }
            Object content = message.get("content");
            return content == null ? "AI返回内容为空" : content.toString();

        } catch (Exception e) {
            logger.error("豆包API调用失败", e);
            return "调用失败：" + e.getMessage();
        }
    }
}