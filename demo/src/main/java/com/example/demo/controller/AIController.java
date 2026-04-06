package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private AIService aiService;
    
    @Resource
    private ObjectMapper objectMapper;

    private static final String CODE_EVALUATION_SYSTEM_PROMPT = """
        你是一位专业的代码评估专家，负责评估学生提交的代码。
        
        请根据以下维度评估代码：
        1. 代码正确性：是否正确实现了题目要求
        2. 代码质量：代码结构、命名规范、可读性
        3. 算法效率：时间复杂度和空间复杂度
        4. 边界处理：是否考虑了边界情况和异常处理
        5. 代码风格：注释、缩进、代码组织
        
        输出格式必须是标准的JSON格式，结构如下：
        {
          "score": 分数(0-100的整数),
          "stars": 星级(1-3的整数),
          "output": "程序运行输出或模拟输出",
          "analysis": "详细的代码分析，包括优点和改进建议",
          "correctness": "正确性评价",
          "quality": "代码质量评价",
          "efficiency": "效率评价",
          "suggestions": ["改进建议1", "改进建议2"]
        }
        
        评分标准：
        - 90-100分：代码完美，给3星
        - 70-89分：代码良好，有小问题，给2星
        - 50-69分：代码基本正确但有明显不足，给1星
        - 0-49分：代码有严重问题或未完成，给0星
        
        注意事项：
        1. 必须严格按照JSON格式输出，不要添加任何其他文字说明
        2. 如果代码无法运行，根据代码逻辑给出模拟输出
        3. 分析要具体，指出代码的优缺点
        """;

    private static final String ASSISTANT_SYSTEM_PROMPT = """
        你是一位专业的算法学习助手，名叫"学习小助手"。你的职责是帮助学生提升算法能力。
        
        你可以：
        - 分析学生的错题，找出薄弱点
        - 制定个性化学习计划
        - 推荐适合的练习题目
        - 解答学生的疑问
        
        回答要求：
        1. 使用中文回答
        2. 回答要简洁明了，重点突出
        3. 如果涉及知识点，给出具体的解释和示例
        4. 适当使用emoji让回答更友好
        5. 如果学生问的是错题分析，给出具体的错误原因和改进建议
        6. 如果学生问的是学习计划，给出具体的时间安排和目标
        7. 如果学生问的是题目推荐，根据学生的薄弱点推荐合适的题目
        """;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("收到AI对话请求，消息数量：{}", request.getMessages() != null ? request.getMessages().size() : 0);
        
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(ASSISTANT_SYSTEM_PROMPT));
            
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                messages.addAll(request.getMessages());
            }
            
            if (request.getMessage() != null && !request.getMessage().isBlank()) {
                messages.add(ChatMessage.user(request.getMessage()));
            }
            
            if (request.getContext() != null) {
                StringBuilder contextPrompt = new StringBuilder("\n\n【学生信息】\n");
                if (request.getContext().getTrack() != null) {
                    contextPrompt.append("- 当前赛道：").append(request.getContext().getTrackLabel() != null ? request.getContext().getTrackLabel() : request.getContext().getTrack()).append("\n");
                }
                if (request.getContext().getWeeklyGoal() != null) {
                    contextPrompt.append("- 周目标：完成 ").append(request.getContext().getWeeklyGoal()).append(" 个关卡\n");
                }
                if (request.getContext().getWeakTopics() != null && !request.getContext().getWeakTopics().isEmpty()) {
                    contextPrompt.append("- 薄弱环节：").append(String.join("、", request.getContext().getWeakTopics())).append("\n");
                }
                if (request.getContext().getStrongTopics() != null && !request.getContext().getStrongTopics().isEmpty()) {
                    contextPrompt.append("- 擅长领域：").append(String.join("、", request.getContext().getStrongTopics())).append("\n");
                }
                if (request.getContext().getTotalErrors() != null) {
                    contextPrompt.append("- 累计错题：").append(request.getContext().getTotalErrors()).append(" 道\n");
                }
                if (request.getContext().getConsistencyScore() != null) {
                    contextPrompt.append("- 坚持指数：").append(request.getContext().getConsistencyScore()).append("/100\n");
                }
                
                ChatMessage contextMessage = ChatMessage.system(contextPrompt.toString());
                messages.add(1, contextMessage);
            }
            
            String response = aiService.chat(messages);
            
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setContent(response);
            chatResponse.setRole("assistant");
            
            log.info("AI对话响应成功");
            return Result.success(chatResponse);
            
        } catch (AIService.AIException e) {
            log.warn("AI对话失败，使用降级回复：{}", e.getMessage());
            String fallbackResponse = generateFallbackResponse(request);
            
            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setContent(fallbackResponse);
            chatResponse.setRole("assistant");
            
            return Result.success(chatResponse);
        } catch (Exception e) {
            log.error("AI对话异常：{}", e.getMessage(), e);
            return Result.fail(500, "AI服务暂时不可用，请稍后重试");
        }
    }

    @PostMapping("/evaluate-code")
    public Result<CodeEvaluationResponse> evaluateCode(@RequestBody CodeEvaluationRequest request) {
        log.info("收到代码评估请求，语言：{}，题目：{}", request.getLanguage(), request.getQuestion());
        
        try {
            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("请评估以下代码：\n\n");
            userPrompt.append("【题目要求】\n").append(request.getQuestion()).append("\n\n");
            userPrompt.append("【题目描述】\n").append(request.getDescription() != null ? request.getDescription() : "无").append("\n\n");
            userPrompt.append("【编程语言】\n").append(request.getLanguage()).append("\n\n");
            userPrompt.append("【学生代码】\n").append(request.getCode()).append("\n\n");
            
            if (request.getStdinInput() != null && !request.getStdinInput().isBlank()) {
                userPrompt.append("【测试输入】\n").append(request.getStdinInput()).append("\n\n");
            }
            
            userPrompt.append("请按照评分标准评估代码，并严格按照JSON格式输出结果。");
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.system(CODE_EVALUATION_SYSTEM_PROMPT));
            messages.add(ChatMessage.user(userPrompt.toString()));
            
            String responseContent = aiService.chat(messages);
            CodeEvaluationResponse evalResponse = parseCodeEvaluationResponse(responseContent);
            
            log.info("代码评估成功，得分：{}，星级：{}", evalResponse.getScore(), evalResponse.getStars());
            return Result.success(evalResponse);
            
        } catch (AIService.AIException e) {
            log.warn("AI代码评估失败，使用降级评估：{}", e.getMessage());
            CodeEvaluationResponse fallbackResponse = generateFallbackEvaluation(request);
            return Result.success(fallbackResponse);
        } catch (Exception e) {
            log.error("代码评估异常：{}", e.getMessage(), e);
            return Result.fail(500, "代码评估服务暂时不可用，请稍后重试");
        }
    }
    
    private CodeEvaluationResponse parseCodeEvaluationResponse(String content) {
        try {
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            } else if (content.contains("```")) {
                jsonContent = content.replaceAll("(?s)```\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            }
            
            return objectMapper.readValue(jsonContent, CodeEvaluationResponse.class);
        } catch (Exception e) {
            log.error("解析代码评估响应失败，原始内容：{}", content);
            CodeEvaluationResponse response = new CodeEvaluationResponse();
            response.setScore(60);
            response.setStars(1);
            response.setOutput("解析评估结果失败");
            response.setAnalysis("AI返回的评估结果格式不正确，已使用默认评分。原始响应：" + content);
            return response;
        }
    }
    
    private CodeEvaluationResponse generateFallbackEvaluation(CodeEvaluationRequest request) {
        CodeEvaluationResponse response = new CodeEvaluationResponse();
        
        String code = request.getCode() != null ? request.getCode() : "";
        int codeLength = code.length();
        int score = 50;
        int stars = 1;
        
        if (codeLength > 100) {
            score = 65;
            stars = 1;
        }
        if (codeLength > 200) {
            score = 70;
            stars = 2;
        }
        if (codeLength > 300) {
            score = 75;
            stars = 2;
        }
        if (code.contains("for") || code.contains("while")) {
            score += 5;
        }
        if (code.contains("if")) {
            score += 5;
        }
        if (code.contains("return")) {
            score += 5;
        }
        
        score = Math.min(100, Math.max(0, score));
        if (score >= 90) stars = 3;
        else if (score >= 70) stars = 2;
        else if (score >= 50) stars = 1;
        else stars = 0;
        
        response.setScore(score);
        response.setStars(stars);
        response.setOutput("（AI服务暂时不可用，使用基础评估）");
        response.setAnalysis("代码长度：" + codeLength + " 字符。由于AI服务暂时不可用，已根据代码基本特征进行初步评估。建议稍后重新评估获取详细分析。");
        response.setCorrectness("代码已提交，等待详细评估");
        response.setQuality("代码包含基本结构");
        response.setEfficiency("需要进一步分析");
        
        return response;
    }

    private String generateFallbackResponse(ChatRequest request) {
        String message = request.getMessage();
        if (message == null && request.getMessages() != null && !request.getMessages().isEmpty()) {
            message = request.getMessages().get(request.getMessages().size() - 1).getContent();
        }
        
        if (message == null) {
            return "你好！我是你的学习小助手，有什么可以帮助你的吗？";
        }
        
        String trackLabel = "算法思维赛道";
        int weeklyGoal = 10;
        
        if (request.getContext() != null) {
            if (request.getContext().getTrackLabel() != null) {
                trackLabel = request.getContext().getTrackLabel();
            }
            if (request.getContext().getWeeklyGoal() != null) {
                weeklyGoal = request.getContext().getWeeklyGoal();
            }
        }
        
        if (message.contains("错题") || message.contains("分析")) {
            return "根据你的做题记录，建议重点关注以下薄弱环节：\n\n" +
                   "- 动态规划：注意状态转移方程的定义\n" +
                   "- 图论：注意边界条件的处理\n\n" +
                   "建议每天复习一道错题，巩固知识点。💪";
        }
        
        if (message.contains("计划") || message.contains("学习")) {
            return "好的，这是为你定制的 **" + trackLabel + "** 学习计划：\n\n" +
                   "**本周目标：完成 " + weeklyGoal + " 个关卡**\n\n" +
                   "**建议安排：**\n" +
                   "- 每天1-2小时学习时间\n" +
                   "- 先攻克薄弱知识点\n" +
                   "- 保持每日练习的习惯\n\n" +
                   "加油！坚持就是胜利！💪";
        }
        
        if (message.contains("推荐") || message.contains("题目")) {
            return "根据你的学习情况，推荐以下练习：\n\n" +
                   "**基础练习：**\n" +
                   "- 数组：两数之和、三数之和\n" +
                   "- 字符串：最长无重复子串\n\n" +
                   "**进阶练习：**\n" +
                   "- 动态规划：爬楼梯、背包问题\n" +
                   "- 图论：最短路径、拓扑排序\n\n" +
                   "建议从基础开始，循序渐进！📚";
        }
        
        return "我理解你的问题。根据你在 **" + trackLabel + "** 的学习数据，我建议：\n\n" +
               "- 先从基础概念入手\n" +
               "- 循序渐进提升难度\n" +
               "- 保持每日练习的习惯\n\n" +
               "有什么具体想了解的吗？😊";
    }

    @Data
    public static class ChatRequest {
        private String message;
        private List<ChatMessage> messages;
        private ChatContext context;
    }

    @Data
    public static class ChatContext {
        private String track;
        private String trackLabel;
        private Integer weeklyGoal;
        private List<String> weakTopics;
        private List<String> strongTopics;
        private Integer totalErrors;
        private Integer consistencyScore;
    }

    @Data
    public static class ChatResponse {
        private String content;
        private String role;
    }

    @Data
    public static class CodeEvaluationRequest {
        private String code;
        private String language;
        private String question;
        private String description;
        private String stdinInput;
    }

    @Data
    public static class CodeEvaluationResponse {
        private Integer score;
        private Integer stars;
        private String output;
        private String analysis;
        private String correctness;
        private String quality;
        private String efficiency;
        private List<String> suggestions;
    }
}
