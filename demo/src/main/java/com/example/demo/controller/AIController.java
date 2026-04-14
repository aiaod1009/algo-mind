package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.ai.DouBaoAiService;
import com.example.demo.dto.ai.ChatMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    private static final String CODE_EVALUATION_SYSTEM_PROMPT = """
            你是一位专业的算法代码评测助手，需要给出清晰、详细、可执行的中文评测。

            你的目标：
            1. 给出 0-100 的总分
            2. 输出一段详细中文分析，说明代码的正确性、主要问题、可读性或复杂度表现
            3. 给出 2 条最重要的改进建议
            4. 给出一份可以直接参考或替换的推荐代码

            输出格式要求：
            1. 先输出 3-6 行中文分析，内容要具体，避免空话
            2. 然后输出分隔符：---JSON---
            3. 最后输出标准 JSON，结构如下：
            {
              "score": 0-100 的整数,
              "stars": 0-3 的整数,
              "shortComment": "详细中文分析，建议 120-220 字，直接写分析内容，不要 Markdown 列表",
              "suggestions": ["建议1", "建议2"],
              "recommendedCode": "完整推荐代码，不要 Markdown 代码块"
            }

            评分要求：
            - 90-100：实现完整、质量高、效率合理，3 星
            - 70-89：基本正确，有少量问题，2 星
            - 50-69：部分正确或存在明显问题，1 星
            - 0-49：实现错误严重或明显未完成，0 星

            额外要求：
            - 必须使用中文
            - 纯文本分析在前，JSON 在后，用 ---JSON--- 分隔
            - JSON 必须合法，不能附加 Markdown 代码块
            - 只保留 score、stars、shortComment、suggestions、recommendedCode 这几个字段
            - shortComment 字段名虽然叫 shortComment，但内容必须是详细中文分析
            - suggestions 固定输出 2 条，每条尽量不超过 30 字
            - recommendedCode 必须是完整代码，直接输出代码文本
            """;

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

    @Resource
    private DouBaoAiService douBaoAiService;

    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("收到AI对话请求，消息数量：{}", request.getMessages() != null ? request.getMessages().size() : 0);

        try {
            List<ChatMessage> messages = buildAssistantMessages(request);
            String response = douBaoAiService.sendToAiFastOrThrow(messages);

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setContent(response);
            chatResponse.setRole("assistant");
            return Result.success(chatResponse);
        } catch (Exception e) {
            log.warn("AI对话失败，使用降级回复：{}", e.getMessage());

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setContent(buildChatFailureResponse(request, e));
            chatResponse.setRole("assistant");
            return Result.success(chatResponse);
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        List<ChatMessage> messages = buildAssistantMessages(request);
        String fallbackContent = buildChatFailureResponse(request, new RuntimeException("AI服务不可用"));
        return douBaoAiService.sendToAiStreamFast(messages, fallbackContent);
    }

    @PostMapping("/evaluate-code")
    public Result<CodeEvaluationResponse> evaluateCode(@RequestBody CodeEvaluationRequest request) {
        log.info("收到代码评测请求，语言：{}，题目：{}", request.getLanguage(), request.getQuestion());

        try {
            String responseContent = douBaoAiService.sendToAiFastOrThrow(buildCodeEvaluationMessages(request));
            CodeEvaluationResponse evalResponse = parseCodeEvaluationResponse(responseContent);
            log.info("代码评测成功，得分：{}，星级：{}", evalResponse.getScore(), evalResponse.getStars());
            return Result.success(evalResponse);
        } catch (Exception e) {
            log.warn("AI代码评测失败，使用降级评测：{}", e.getMessage());
            return Result.success(generateFallbackEvaluation(request));
        }
    }

    @PostMapping(value = "/evaluate-code/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter evaluateCodeStream(@RequestBody CodeEvaluationRequest request) {
        String fallbackJson = toJson(generateFallbackEvaluation(request));
        return douBaoAiService.sendToAiStreamFast(buildCodeEvaluationMessages(request), fallbackJson);
    }

    private List<ChatMessage> buildAssistantMessages(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(ASSISTANT_SYSTEM_PROMPT));

        ChatMessage contextMessage = buildContextMessage(request.getContext());
        if (contextMessage != null) {
            messages.add(contextMessage);
        }

        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            messages.addAll(request.getMessages());
        }

        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            messages.add(ChatMessage.user(request.getMessage()));
        }

        return messages;
    }

    private ChatMessage buildContextMessage(ChatContext context) {
        if (context == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder("以下是当前学生画像，请结合这些信息回答，但不要逐条复述：\n");
        boolean hasContent = false;

        if (hasText(context.getTrack())) {
            builder.append("- 当前赛道：")
                    .append(hasText(context.getTrackLabel()) ? context.getTrackLabel() : context.getTrack())
                    .append('\n');
            hasContent = true;
        }
        if (context.getWeeklyGoal() != null) {
            builder.append("- 周目标：").append(context.getWeeklyGoal()).append(" 个关卡\n");
            hasContent = true;
        }
        if (context.getWeakTopics() != null && !context.getWeakTopics().isEmpty()) {
            builder.append("- 薄弱点：").append(String.join("、", context.getWeakTopics())).append('\n');
            hasContent = true;
        }
        if (context.getStrongTopics() != null && !context.getStrongTopics().isEmpty()) {
            builder.append("- 擅长点：").append(String.join("、", context.getStrongTopics())).append('\n');
            hasContent = true;
        }
        if (context.getTotalErrors() != null) {
            builder.append("- 累计错题：").append(context.getTotalErrors()).append(" 道\n");
            hasContent = true;
        }
        if (context.getConsistencyScore() != null) {
            builder.append("- 坚持指数：").append(context.getConsistencyScore()).append("/100\n");
            hasContent = true;
        }

        return hasContent ? ChatMessage.system(builder.toString()) : null;
    }

    private List<ChatMessage> buildCodeEvaluationMessages(CodeEvaluationRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(CODE_EVALUATION_SYSTEM_PROMPT));
        messages.add(ChatMessage.user(buildCodeEvaluationPrompt(request)));
        return messages;
    }

    private String buildCodeEvaluationPrompt(CodeEvaluationRequest request) {
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请评估以下代码：\n\n");
        userPrompt.append("【题目】\n").append(nullSafe(request.getQuestion())).append("\n\n");
        userPrompt.append("【题目描述】\n").append(nullSafe(request.getDescription())).append("\n\n");
        userPrompt.append("【编程语言】\n").append(nullSafe(request.getLanguage())).append("\n\n");
        userPrompt.append("【学生代码】\n").append(nullSafe(request.getCode())).append("\n\n");

        if (hasText(request.getStdinInput())) {
            userPrompt.append("【测试输入】\n").append(request.getStdinInput()).append("\n\n");
        }

        userPrompt.append("请输出详细中文分析、建议和推荐代码，不要输出额外字段。");
        return userPrompt.toString();
    }

    private CodeEvaluationResponse parseCodeEvaluationResponse(String content) {
        try {
            String jsonContent = extractEvaluationJson(content);
            return objectMapper.readValue(jsonContent, CodeEvaluationResponse.class);
        } catch (Exception e) {
            log.error("解析代码评测响应失败，原始内容：{}", content, e);
            CodeEvaluationResponse response = new CodeEvaluationResponse();
            response.setScore(60);
            response.setStars(1);
            response.setShortComment("评测结果解析失败，已回退到基础中文分析。当前只能确认代码已成功提交，但 AI 返回内容没有按约定格式输出，建议重新发起一次评测，并重点检查边界条件、输入处理和核心逻辑是否完整。");
            response.setSuggestions(List.of("稍后重试一次 AI 评测", "检查代码是否完整提交"));
            response.setRecommendedCode("");
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
        }
        if (codeLength > 200) {
            score = 70;
            stars = 2;
        }
        if (codeLength > 300) {
            score = 75;
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
        if (score >= 90) {
            stars = 3;
        } else if (score >= 70) {
            stars = 2;
        } else if (score >= 50) {
            stars = 1;
        } else {
            stars = 0;
        }

        response.setScore(score);
        response.setStars(stars);
        response.setShortComment("已使用本地快速评测。当前代码已经具备基础结构，但还无法像完整 AI 评测那样确认逻辑是否覆盖题目要求、边界条件和复杂度约束。建议先自查输入输出、循环分支和返回结果，再结合推荐代码继续完善。");
        response.setSuggestions(List.of("补充边界条件测试", "检查复杂度是否满足题目要求"));
        response.setRecommendedCode(buildFallbackRecommendedCode(code, request.getLanguage()));
        return response;
    }

    private String buildChatFailureResponse(ChatRequest request, Exception e) {
        String errorMessage = e.getMessage() == null ? "" : e.getMessage();
        if (errorMessage.contains("401")) {
            return """
                    AI 服务鉴权失败。

                    请检查：
                    1. `doubao.api.key` 是否有效
                    2. 当前 key 是否有目标模型的调用权限
                    3. 部署环境是否加载了正确的配置文件
                    """;
        }

        return generateFallbackResponse(request);
    }

    private String generateFallbackResponse(ChatRequest request) {
        String message = extractLastUserMessage(request);
        if (!hasText(message)) {
            return "你好，我是你的算法学习助手。你可以直接问我算法思路、错题分析或练习建议。";
        }

        String trackLabel = "算法思维赛道";
        int weeklyGoal = 10;
        if (request.getContext() != null) {
            if (hasText(request.getContext().getTrackLabel())) {
                trackLabel = request.getContext().getTrackLabel();
            }
            if (request.getContext().getWeeklyGoal() != null) {
                weeklyGoal = request.getContext().getWeeklyGoal();
            }
        }

        if (message.contains("错题") || message.contains("分析")) {
            return """
                    结合你当前的学习情况，我建议先从最近高频出错的知识点回看：
                    1. 先定位是哪一类题最容易卡住
                    2. 再总结一条自己的错误模式
                    3. 最后用 2-3 道同类型题做针对性巩固

                    如果你愿意，我也可以继续帮你拆解某一道具体错题。
                    """;
        }

        if (message.contains("计划") || message.contains("学习")) {
            return "你当前在 `" + trackLabel + "`，周目标是 `" + weeklyGoal + "` 个关卡。建议先保证每天 1-2 小时稳定练习，优先补弱项，再做一小部分综合题巩固。";
        }

        if (message.contains("推荐") || message.contains("题目")) {
            return """
                    可以先按这个顺序练：
                    1. 一道基础题，确认思路
                    2. 一道同类型变式题，巩固模板
                    3. 一道稍难题，训练迁移能力

                    如果你告诉我当前最薄弱的专题，我可以直接给你更具体的练习建议。
                    """;
        }

        return "我已经收到你的问题。你可以继续补充题目、代码、卡住的步骤，或者告诉我你想要“思路讲解 / 错题分析 / 练习推荐”中的哪一种帮助。";
    }

    private String extractLastUserMessage(ChatRequest request) {
        if (hasText(request.getMessage())) {
            return request.getMessage();
        }

        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return null;
        }

        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            ChatMessage message = request.getMessages().get(i);
            if (message != null && "user".equalsIgnoreCase(message.getRole()) && hasText(message.getContent())) {
                return message.getContent();
            }
        }
        return null;
    }

    private String stripMarkdownFence(String content) {
        if (content == null) {
            return "";
        }

        String jsonContent = content.trim();
        if (jsonContent.startsWith("```json")) {
            jsonContent = jsonContent.replaceFirst("^```json\\s*", "").replaceFirst("\\s*```$", "");
        } else if (jsonContent.startsWith("```")) {
            jsonContent = jsonContent.replaceFirst("^```\\s*", "").replaceFirst("\\s*```$", "");
        }
        return jsonContent.trim();
    }

    private String extractEvaluationJson(String content) {
        String normalized = stripMarkdownFence(content);
        int separatorIndex = normalized.indexOf("---JSON---");
        if (separatorIndex == -1) {
            return normalized;
        }
        return normalized.substring(separatorIndex + "---JSON---".length()).trim();
    }

    private String toJson(CodeEvaluationResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("序列化降级评测结果失败", e);
            return "{\"score\":60,\"stars\":1,\"shortComment\":\"降级评测结果生成失败，当前无法给出完整 AI 中文分析。建议稍后重试，并优先检查代码是否完整、输入输出是否正确。\",\"suggestions\":[\"稍后再试一次\",\"检查代码是否完整\"],\"recommendedCode\":\"\"}";
        }
    }

    private String buildFallbackRecommendedCode(String code, String language) {
        if (hasText(code)) {
            return code.trim();
        }

        String normalizedLanguage = language == null ? "" : language.trim().toLowerCase();
        return switch (normalizedLanguage) {
            case "java" -> """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("TODO");
                        }
                    }
                    """.trim();
            case "cpp", "c++" -> """
                    #include <iostream>

                    int main() {
                        std::cout << "TODO" << std::endl;
                        return 0;
                    }
                    """.trim();
            case "javascript", "js" -> """
                    function solve() {
                      console.log('TODO')
                    }

                    solve()
                    """.trim();
            default -> """
                    def solve():
                        print("TODO")

                    solve()
                    """.trim();
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullSafe(String value) {
        return hasText(value) ? value : "无";
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CodeEvaluationResponse {
        private Integer score;
        private Integer stars;
        private String shortComment;
        private List<String> suggestions;
        private String recommendedCode;
    }
}
