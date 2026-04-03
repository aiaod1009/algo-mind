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

/**
 * AI服务：封装豆包AI调用逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {
    
    private final RestTemplate restTemplate;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    
    /**
     * 系统提示词：学习计划生成专家
     */
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
     * 调用AI生成学习计划
     */
    public LearningPlanAIResponse generateLearningPlan(LearningPlanAIRequest request) {
        log.info("开始调用AI生成学习计划，用户赛道：{}，周目标：{}", request.getTrack(), request.getWeeklyGoal());
        
        try {
            // 构建用户提示词
            String userPrompt = buildLearningPlanPrompt(request);
            
            // 构建对话消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(LEARNING_PLAN_SYSTEM_PROMPT));
            messages.add(ChatMessage.user(userPrompt));
            
            // 构建请求
            ChatRequest chatRequest = ChatRequest.builder()
                    .model(aiConfig.getModel())
                    .messages(messages)
                    .temperature(aiConfig.getTemperature())
                    .maxTokens(aiConfig.getMaxTokens())
                    .stream(false)
                    .build();
            
            // 调用AI
            String responseContent = callAI(chatRequest);
            
            // 解析响应
            LearningPlanAIResponse planResponse = parseLearningPlanResponse(responseContent);
            
            log.info("AI学习计划生成成功");
            return planResponse;
            
        } catch (Exception e) {
            log.error("AI生成学习计划失败：{}", e.getMessage(), e);
            throw new AIException("AI生成学习计划失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 通用AI对话方法
     */
    public String chat(List<ChatMessage> messages) {
        ChatRequest request = ChatRequest.builder()
                .model(aiConfig.getModel())
                .messages(messages)
                .temperature(aiConfig.getTemperature())
                .maxTokens(aiConfig.getMaxTokens())
                .stream(false)
                .build();
        
        return callAI(request);
    }
    
    /**
     * 调用AI接口（带重试机制）
     */
    private String callAI(ChatRequest request) {
        int maxRetries = aiConfig.getMaxRetries();
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                attempt++;
                log.debug("AI调用第{}次尝试", attempt);
                
                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + aiConfig.getKey());
                
                // 发送请求
                HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
                ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                        aiConfig.getUrl(),
                        entity,
                        ChatResponse.class
                );
                
                // 处理响应
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
                log.warn("AI调用第{}次尝试失败：{}", attempt, e.getMessage());
                if (attempt >= maxRetries) {
                    throw new AIException("AI服务调用失败，已重试" + maxRetries + "次：" + e.getMessage(), e);
                }
                // 等待后重试
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AIException("重试被中断", ie);
                }
            }
        }
        
        throw new AIException("AI调用失败，超出最大重试次数");
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
            // 清理可能的markdown代码块标记
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            } else if (content.contains("```")) {
                jsonContent = content.replaceAll("(?s)```\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            }
            
            // 尝试解析JSON
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
