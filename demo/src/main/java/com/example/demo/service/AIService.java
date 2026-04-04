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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    
    private static final int AI_CALL_TIMEOUT_SECONDS = 15;
    
    private static final String LEARNING_PLAN_SYSTEM_PROMPT = """
        你是一位专业的算法学习规划师，擅长根据学生的学习数据制定个性化的学习计划。
        
        请根据提供的用户画像数据，生成一份详细的学习计划，包括：
        1. 本周目标（3-4个具体目标）
        2. 每日任务安排（今天、明天、后天）
        3. 推荐学习资源
        4. 学习分析和建议
        
        输出格式必须是标准的JSON格式，结构如下：
        {
          "weekGoals": [
            {"id": 1, "title": "目标标题", "progress": 0, "target": 目标数量, "description": "目标描述"}
          ],
          "dailyTasks": [
            {
              "day": "今天",
              "tasks": [
                {"title": "任务标题", "duration": 时长(分钟), "type": "learn/practice/review", "priority": "high/medium/low", "description": "任务描述"}
              ]
            }
          ],
          "recommendations": [
            {"type": "video/article/practice", "title": "资源标题", "source": "来源", "priority": "high/medium", "description": "资源描述"}
          ],
          "analysis": "对学生学习情况的分析总结",
          "suggestions": ["建议1", "建议2", "建议3"]
        }
        
        注意事项：
        1. 必须严格按照JSON格式输出，不要添加任何其他文字说明
        2. 根据用户的薄弱环节和错题重点安排学习内容
        3. 考虑用户的坚持指数调整任务难度
        4. 每日任务总时长控制在90-120分钟
        5. 推荐资源要针对用户的薄弱点
        """;
    
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
            
        } catch (AIException e) {
            log.warn("AI生成学习计划失败：{}，将使用本地算法降级", e.getMessage());
            throw e;
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
        } catch (Exception e) {
            if (e instanceof AIException) {
                throw (AIException) e;
            }
            throw new AIException("AI对话失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 带超时控制的AI调用（单次尝试，快速失败）
     * 只抛出 AIException（非受检异常），方便调用方处理
     */
    private String callAIWithTimeout(ChatRequest request) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            Future<String> future = executor.submit(() -> callAIOnce(request));
            
            try {
                return future.get(AI_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                throw new AIException("AI服务响应超时", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
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
     * AI服务异常（非受检异常）
     * 
     * 使用说明：
     * 1. 此异常继承 RuntimeException，不强制捕获
     * 2. Controller 层应捕获此异常并降级到本地算法
     * 3. 异常链（cause）保留了原始异常信息，便于排查问题
     * 
     * 常见异常原因：
     * - AI服务响应超时
     * - AI服务网络错误
     * - AI响应解析失败
     */
    public static class AIException extends RuntimeException {
        public AIException(String message) {
            super(message);
        }
        
        public AIException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    /**
     * 检查AI服务可用性（快速检查，3秒超时）
     */
    public boolean checkAvailability() {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.user("你好"));
            
            ChatRequest request = ChatRequest.builder()
                    .model(aiConfig.getModel())
                    .messages(messages)
                    .temperature(0.1)
                    .maxTokens(10)
                    .stream(false)
                    .build();
            
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<String> future = executor.submit(() -> callAIOnce(request));
                future.get(3, TimeUnit.SECONDS);
                return true;
            } catch (TimeoutException | ExecutionException | InterruptedException e) {
                log.debug("AI服务不可用：{}", e.getMessage());
                return false;
            } finally {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            log.debug("AI服务检查失败：{}", e.getMessage());
            return false;
        }
    }
}
