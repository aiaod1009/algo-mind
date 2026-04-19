package com.example.demo.service;

import com.example.demo.config.AIConfig;
import com.example.demo.dto.ai.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * AI服务：封装豆包AI调用逻辑
 * 优化策略：
 * 1. 单次调用快速失败（15秒超时）
 * 2. AI失败立即降级到本地算法
 * 3. 异步预检查AI可用性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final RestTemplate restTemplate;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    
    private static final int AI_CALL_TIMEOUT_SECONDS = 60;
    
    private static final String LEARNING_PLAN_SYSTEM_PROMPT = "算法学习规划师，生成个性化学习计划。根据用户数据，生成计划包含：1. 本周目标（3-4个具体目标）2. 每日任务（今天、明天、后天）3. 学习分析和建议。输出JSON：{\"weekGoals\":[{\"id\":1,\"title\":\"目标标题\",\"progress\":0,\"target\":目标数量,\"description\":\"目标描述\"}],\"dailyTasks\":[{\"day\":\"今天\",\"tasks\":[{\"title\":\"任务标题\",\"duration\":时长(分钟),\"type\":\"learn/practice/review\",\"priority\":\"high/medium/low\",\"description\":\"任务描述\"}]}],\"analysis\":\"学习情况分析\",\"suggestions\":[\"建议1\",\"建议2\",\"建议3\"]}。要求：1. 仅JSON，无其他文字 2. 基于薄弱环节和错题安排 3. 根据坚持指数调整难度 4. 每日任务90-120分钟";

    /**
     * 调用AI生成学习计划（带快速失败机制）
     */
    public LearningPlanAIResponse generateLearningPlan(LearningPlanAIRequest request) {
        log.info("开始调用AI生成学习计划，用户赛道：{}，周目标：{}", request.getTrack(), request.getWeeklyGoal());
        
        try {
            String userPrompt = buildLearningPlanPrompt(request);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(LEARNING_PLAN_SYSTEM_PROMPT));
            messages.add(ChatMessage.user(userPrompt));
            
            ChatRequest chatRequest = ChatRequest.builder()
                    .model(aiConfig.getModel())
                    .messages(messages)
                    .temperature(aiConfig.getTemperature())
                    .maxTokens(aiConfig.getMaxTokens())
                    .stream(false)
                    .build();
            
            String responseContent = callAIWithTimeout(chatRequest);
            LearningPlanAIResponse planResponse = parseLearningPlanResponse(responseContent);
            
            log.info("AI学习计划生成成功");
            return planResponse;
            
        } catch (TimeoutException e) {
            log.warn("AI调用超时，将使用本地算法降级");
            throw new AIException("AI服务响应超时", e);
        } catch (Exception e) {
            log.warn("AI生成学习计划失败：{}，将使用本地算法降级", e.getMessage());
            throw new AIException("AI生成学习计划失败：" + e.getMessage(), e);
        }
    }

    /**
     * 通用AI对话方法
     */
    public String chat(List<ChatMessage> messages) {
        try {
            ChatRequest request = ChatRequest.builder()
                    .model(aiConfig.getModel())
                    .messages(messages)
                    .temperature(aiConfig.getTemperature())
                    .maxTokens(aiConfig.getMaxTokens())
                    .stream(false)
                    .build();
            
            return callAIWithTimeout(request);
        } catch (TimeoutException e) {
            log.warn("AI对话超时");
            throw new AIException("AI服务响应超时", e);
        }
    }
    
    /**
     * 带超时控制的AI调用（单次尝试，快速失败）
     */
    private String callAIWithTimeout(ChatRequest request) throws TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        long startTime = System.currentTimeMillis();
        String modelName = request.getModel() != null ? request.getModel() : aiConfig.getModel();
        
        try {
            Future<String> future = executor.submit(() -> callAIOnce(request));
            
            try {
                String result = future.get(AI_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                long duration = System.currentTimeMillis() - startTime;
                log.info("AI call successful, model={}, duration={}ms", modelName, duration);
                return result;
            } catch (TimeoutException e) {
                future.cancel(true);
                long duration = System.currentTimeMillis() - startTime;
                log.warn("AI call timeout, model={}, timeout={}s, waited={}ms", modelName, AI_CALL_TIMEOUT_SECONDS, duration);
                throw e;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                long duration = System.currentTimeMillis() - startTime;
                log.error("AI call failed, model={}, duration={}ms, error={}", modelName, duration, cause.getMessage());
                if (cause instanceof AIException) {
                    throw (AIException) cause;
                }
                throw new AIException("AI调用失败: " + cause.getMessage(), cause);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AIException("AI调用被中断", e);
            }
        } finally {
            executor.shutdownNow();
        }
    }
    
    /**
     * 单次AI调用（无重试）
     */
    private String callAIOnce(ChatRequest request) {
        try {
            log.debug("开始AI调用");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiConfig.getKey());
            
            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                    aiConfig.getUrl(),
                    entity,
                    ChatResponse.class
            );
            
            if (response.getBody() == null) {
                throw new AIException("AI响应为空");
            }
            
            String content = response.getBody().getContent();
            if (content == null || content.isBlank()) {
                throw new AIException("AI返回内容为空");
            }
            
            log.debug("AI调用成功，消耗token：{}", 
                    response.getBody().getUsage() != null ? 
                            response.getBody().getUsage().getTotalTokens() : "未知");
            
            return content;
            
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("AI HTTP error: {} {}, body={}", e.getStatusCode().value(), e.getStatusText(), responseBody);
            throw new AIException(
                    "AI service HTTP error: " + e.getStatusCode().value() + " " + e.getStatusText()
                            + (responseBody == null || responseBody.isBlank() ? "" : " - " + responseBody),
                    e);
        } catch (RestClientException e) {
            log.error("AI调用网络错误：{}", e.getMessage());
            throw new AIException("AI服务网络错误：" + e.getMessage(), e);
        }
    }
    
    /**
     * 构建学习计划提示词
     */
    private String buildLearningPlanPrompt(LearningPlanAIRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下学生生成个性化学习计划：\n\n");
        
        prompt.append("【基本信息】\n");
        prompt.append("- 目标赛道：").append(request.getTrackLabel()).append(" (").append(request.getTrack()).append(")\n");
        prompt.append("- 每周目标：完成 ").append(request.getWeeklyGoal()).append(" 个关卡\n");
        prompt.append("- 本周已完成：").append(request.getWeeklyCompleted()).append(" 题\n");
        prompt.append("- 累计解题：").append(request.getTotalSolved()).append(" 题\n");
        prompt.append("- 连续打卡：").append(request.getCurrentStreak()).append(" 天\n");
        prompt.append("- 坚持指数：").append(request.getPersistenceIndex()).append("/100\n\n");
        
        prompt.append("【擅长领域】\n");
        if (request.getStrongAreas() != null && !request.getStrongAreas().isEmpty()) {
            prompt.append(String.join("、", request.getStrongAreas())).append("\n\n");
        } else {
            prompt.append("暂无数据\n\n");
        }
        
        prompt.append("【薄弱环节】\n");
        if (request.getWeakAreas() != null && !request.getWeakAreas().isEmpty()) {
            prompt.append(String.join("、", request.getWeakAreas())).append("\n\n");
        } else {
            prompt.append("暂无数据\n\n");
        }
        
        prompt.append("【历史错题知识点】\n");
        if (request.getErrorTopics() != null && !request.getErrorTopics().isEmpty()) {
            prompt.append(String.join("、", request.getErrorTopics())).append("\n\n");
        } else {
            prompt.append("暂无数据\n\n");
        }
        
        if (request.getPreferences() != null && !request.getPreferences().isBlank()) {
            prompt.append("【用户偏好】\n");
            prompt.append(request.getPreferences()).append("\n\n");
        }
        
        prompt.append("请生成学习计划，严格按照JSON格式输出。");
        
        return prompt.toString();
    }
    
    /**
     * 解析AI响应为学习计划对象
     */
    private LearningPlanAIResponse parseLearningPlanResponse(String content) {
        try {
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            } else if (content.contains("```")) {
                jsonContent = content.replaceAll("(?s)```\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            }
            
            return objectMapper.readValue(jsonContent, LearningPlanAIResponse.class);
            
        } catch (JsonProcessingException e) {
            log.error("解析AI响应失败，原始内容：{}", content);
            throw new AIException("AI响应解析失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * AI异常类
     */
    public static class AIException extends RuntimeException {
        public AIException(String message) {
            super(message);
        }
        
        public AIException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
