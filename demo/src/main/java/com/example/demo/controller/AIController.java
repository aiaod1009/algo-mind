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

    private static final int QUESTION_PROMPT_LIMIT = 600;
    private static final int DESCRIPTION_PROMPT_LIMIT = 1200;
    private static final int CODE_PROMPT_LIMIT = 12000;
    private static final int STDIN_PROMPT_LIMIT = 400;
    private static final String CODE_SEPARATOR = "---CODE---";
    private static final String JSON_SEPARATOR = "---JSON---";

    private static final String CODE_EVALUATION_SYSTEM_PROMPT_V2 = """
            角色：算法代码评测助手
            任务：中文评测代码

            输出格式：
            [3-6行中文分析]
            ---CODE---
            [完整代码，直接输出，不要 markdown]
            ---JSON---
            {
              "score": 0-100,
              "stars": 0-3,
              "shortComment": "120-220字详细分析",
              "suggestions": ["建议1", "建议2"],
              "recommendedCode": "同上方代码"
            }

            评分标准：
            - 90-100：完整高质量，3星
            - 70-89：基本正确，2星
            - 50-69：部分正确，1星
            - 0-49：严重错误，0星

            约束：
            1. 全部中文
            2. suggestions 固定 2 条，每条不超过 30 字
            3. 推荐代码必须放在 ---CODE--- 和 ---JSON--- 之间，按 100 分标准给出完整可运行代码，不要 markdown
            4. 仅保留 score、stars、shortComment、suggestions、recommendedCode
            5. JSON 前只能保留 3-6 行中文分析，JSON 后不要追加内容
            6. ---CODE--- 中必须保留语法所需空格、换行和缩进，禁止输出 publicclass、returnnew、newArrayList 这类连写错误
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
            String responseContent = douBaoAiService.sendToAiFastOrThrowForCodeEvaluation(buildCodeEvaluationMessages(request));
            CodeEvaluationResponse evalResponse = normalizeEvaluationResponse(parseCodeEvaluationResponse(responseContent));
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
        return douBaoAiService.sendToAiStreamFastForCodeEvaluation(buildCodeEvaluationMessages(request), fallbackJson);
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
        messages.add(ChatMessage.system(CODE_EVALUATION_SYSTEM_PROMPT_V2));
        messages.add(ChatMessage.user(buildCodeEvaluationPrompt(request)));
        return messages;
    }

    private String buildCodeEvaluationPrompt(CodeEvaluationRequest request) {
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请按要求评测以下算法代码：\n\n");
        appendPromptSection(userPrompt, "题目", request.getQuestion(), QUESTION_PROMPT_LIMIT);

        String description = normalizePromptText(request.getDescription());
        String question = normalizePromptText(request.getQuestion());
        if (hasText(description) && !description.equals(question)) {
            appendPromptSection(userPrompt, "题目描述", description, DESCRIPTION_PROMPT_LIMIT);
        }

        appendPromptSection(userPrompt, "编程语言", request.getLanguage(), 80);
        appendPromptSection(userPrompt, "学生代码", request.getCode(), CODE_PROMPT_LIMIT);

        if (hasText(request.getStdinInput())) {
            appendPromptSection(userPrompt, "测试输入", request.getStdinInput(), STDIN_PROMPT_LIMIT);
        }

        userPrompt.append("请严格按照既定格式输出。");
        return userPrompt.toString();
    }

    private void appendPromptSection(StringBuilder builder, String title, String value, int limit) {
        String normalized = normalizePromptText(value);
        if (!hasText(normalized)) {
            return;
        }
        builder.append("【").append(title).append("】\n")
                .append(truncatePromptText(normalized, limit))
                .append("\n\n");
    }

    private String normalizePromptText(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncatePromptText(String value, int limit) {
        if (!hasText(value) || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "\n...[内容过长，已截断]";
    }

    private CodeEvaluationResponse parseCodeEvaluationResponse(String content) {
        try {
            String jsonContent = extractEvaluationJson(content);
            CodeEvaluationResponse response = objectMapper.readValue(jsonContent, CodeEvaluationResponse.class);
            String extractedCode = extractEvaluationCode(content);
            if (hasText(extractedCode)) {
                response.setRecommendedCode(extractedCode);
            }
            return normalizeEvaluationResponse(response);
        } catch (Exception e) {
            log.error("解析代码评测响应失败，原始内容：{}", content, e);
            CodeEvaluationResponse response = new CodeEvaluationResponse();
            response.setScore(60);
            response.setStars(1);
            response.setShortComment("评测结果解析失败，已回退到基础中文分析。当前只能确认代码已成功提交，但 AI 返回内容没有按约定格式输出，建议重新发起一次评测，并重点检查边界条件、输入处理和核心逻辑是否完整。");
            response.setSuggestions(List.of("稍后重试一次 AI 评测", "检查代码是否完整提交"));
            response.setRecommendedCode("");
            return normalizeEvaluationResponse(response);
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
        return normalizeEvaluationResponse(response);
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
        if (content == null || content.isBlank()) {
            return "{}";
        }

        String normalized = stripMarkdownFence(content);

        int separatorIndex = normalized.indexOf(JSON_SEPARATOR);
        String afterSeparator = separatorIndex >= 0
                ? normalized.substring(separatorIndex + JSON_SEPARATOR.length()).trim()
                : normalized.trim();

        int braceStart = afterSeparator.indexOf('{');
        if (braceStart < 0) {
            return afterSeparator;
        }

        int depth = 0;
        int braceEnd = -1;
        boolean inString = false;
        char stringDelim = 0;
        boolean escape = false;

        for (int i = braceStart; i < afterSeparator.length(); i++) {
            char c = afterSeparator.charAt(i);

            if (escape) {
                escape = false;
                continue;
            }

            if (c == '\\' && inString) {
                escape = true;
                continue;
            }

            if (inString) {
                if (c == stringDelim) {
                    inString = false;
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                inString = true;
                stringDelim = c;
                continue;
            }

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    braceEnd = i;
                    break;
                }
            }
        }

        if (braceEnd >= 0) {
            return afterSeparator.substring(braceStart, braceEnd + 1);
        }

        return afterSeparator.substring(braceStart);
    }

    private String extractEvaluationCode(String content) {
        if (!hasText(content)) {
            return "";
        }

        String normalized = content.replace("\r\n", "\n");
        int codeStart = normalized.indexOf(CODE_SEPARATOR);
        if (codeStart < 0) {
            return "";
        }

        int contentStart = codeStart + CODE_SEPARATOR.length();
        int jsonStart = normalized.indexOf(JSON_SEPARATOR, contentStart);
        String codeSection = jsonStart >= 0
                ? normalized.substring(contentStart, jsonStart)
                : normalized.substring(contentStart);
        return cleanRecommendedCode(codeSection);
    }

    private String toJson(CodeEvaluationResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("序列化降级评测结果失败", e);
            return "{\"score\":60,\"stars\":1,\"shortComment\":\"降级评测结果生成失败，当前无法给出完整 AI 中文分析。建议稍后重试，并优先检查代码是否完整、输入输出是否正确。\",\"suggestions\":[\"稍后再试一次\",\"检查代码是否完整\"],\"recommendedCode\":\"\"}";
        }
    }

    private CodeEvaluationResponse normalizeEvaluationResponse(CodeEvaluationResponse response) {
        if (response == null) {
            return null;
        }

        int score = response.getScore() == null ? 0 : Math.max(0, Math.min(100, response.getScore()));
        int stars = response.getStars() == null ? 0 : Math.max(0, Math.min(3, response.getStars()));
        response.setScore(score);
        response.setStars(stars);

        if (response.getShortComment() != null) {
            response.setShortComment(response.getShortComment().trim());
        }

        response.setSuggestions(normalizeSuggestions(response.getSuggestions()));
        response.setRecommendedCode(cleanRecommendedCode(response.getRecommendedCode()));
        return response;
    }

    private List<String> normalizeSuggestions(List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (!hasText(suggestion)) {
                continue;
            }

            normalized.add(suggestion.trim());
            if (normalized.size() == 2) {
                break;
            }
        }
        return normalized;
    }

    private String cleanRecommendedCode(String recommendedCode) {
        String stripped = stripMarkdownFence(recommendedCode);
        if (!hasText(stripped)) {
            return "";
        }
        return dedentCommonIndent(trimBlankLines(stripped));
    }

    private String trimBlankLines(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\r\n", "\n")
                .replaceAll("^\\s*\\n+", "")
                .replaceAll("\\n+\\s*$", "");
    }

    private String dedentCommonIndent(String value) {
        if (!hasText(value)) {
            return "";
        }

        String[] lines = value.split("\n", -1);
        int minIndent = Integer.MAX_VALUE;
        for (String line : lines) {
            if (!hasText(line)) {
                continue;
            }

            int indent = 0;
            while (indent < line.length() && Character.isWhitespace(line.charAt(indent))) {
                indent++;
            }
            minIndent = Math.min(minIndent, indent);
        }

        if (minIndent == Integer.MAX_VALUE || minIndent == 0) {
            return value;
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (hasText(line)) {
                builder.append(line.substring(Math.min(minIndent, line.length())));
            }

            if (index < lines.length - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private String buildFallbackRecommendedCode(String code, String language) {
        String normalizedLanguage = language == null ? "" : language.trim().toLowerCase();
        return switch (normalizedLanguage) {
            case "java" -> """
                    public class Main {
                        public static void main(String[] args) {
                            // AI fallback could not produce a trusted reference solution.
                            // Please retry the full AI evaluation to get a 100-point example.
                        }
                    }
                    """.trim();
            case "cpp", "c++" -> """
                    #include <iostream>

                    int main() {
                        // AI fallback could not produce a trusted reference solution.
                        // Please retry the full AI evaluation to get a 100-point example.
                        return 0;
                    }
                    """.trim();
            case "javascript", "js" -> """
                    function solve() {
                      // AI fallback could not produce a trusted reference solution.
                      // Please retry the full AI evaluation to get a 100-point example.
                    }

                    solve()
                    """.trim();
            default -> """
                    def solve():
                        # AI fallback could not produce a trusted reference solution.
                        # Please retry the full AI evaluation to get a 100-point example.

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
