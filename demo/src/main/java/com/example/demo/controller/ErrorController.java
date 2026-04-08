package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.dto.ai.ProblemAnalysisRequest;
import com.example.demo.dto.ai.ProblemAnalysisResponse;
import com.example.demo.entity.ErrorItem;
import com.example.demo.repository.ErrorItemRepository;
import com.example.demo.service.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
public class ErrorController {

    @Resource
    private ErrorItemRepository errorRepository;

    @Resource
    private CurrentUserService currentUserService;

    @Resource
    private AIService aiService;

    @Resource
    private ObjectMapper objectMapper;

    private static final String PROBLEM_ANALYSIS_PROMPT = """
        你是一位资深的算法教练，擅长分析学生的错题并提供针对性的学习建议。
        请根据提供的错题信息，生成一份详细的分析报告。输出格式必须是标准 JSON。
        """;

    @GetMapping("/errors")
    public Result<List<ErrorItem>> getErrors() {
        Long userId = currentUserService.requireCurrentUserId();
        return Result.success(errorRepository.findByUserIdOrderByUpdatedAtDesc(userId));
    }

    @GetMapping("/errors/{id}")
    public Result<ErrorItem> getErrorById(@PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        return errorRepository.findByIdAndUserId(id, userId)
                .map(Result::success)
                .orElseGet(() -> Result.fail(40401, "错题不存在"));
    }

    @PostMapping("/errors")
    public Result<ErrorItem> addError(@RequestBody ErrorItem errorItem) {
        Long userId = currentUserService.requireCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        errorItem.setUserId(userId);
        errorItem.setCreatedAt(now);
        errorItem.setUpdatedAt(now);
        if (errorItem.getAnalysisStatus() == null) {
            errorItem.setAnalysisStatus("未分析");
        }
        return Result.success(errorRepository.save(errorItem));
    }

    @DeleteMapping("/errors/{id}")
    public Result<Map<String, Object>> deleteError(@PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        Optional<ErrorItem> errorItem = errorRepository.findByIdAndUserId(id, userId);
        if (errorItem.isEmpty()) {
            return Result.fail(40401, "错题不存在");
        }
        errorRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("deleted", true);
        return Result.success(result);
    }

    @PutMapping("/errors/{id}")
    public Result<ErrorItem> updateError(@PathVariable Long id, @RequestBody ErrorItem errorItemRequest) {
        Long userId = currentUserService.requireCurrentUserId();
        Optional<ErrorItem> optionalError = errorRepository.findByIdAndUserId(id, userId);
        if (optionalError.isEmpty()) {
            return Result.fail(40401, "错题不存在");
        }

        ErrorItem errorItem = optionalError.get();
        if (errorItemRequest.getQuestion() != null) {
            errorItem.setQuestion(errorItemRequest.getQuestion());
        }
        if (errorItemRequest.getUserAnswer() != null) {
            errorItem.setUserAnswer(errorItemRequest.getUserAnswer());
        }
        if (errorItemRequest.getDescription() != null) {
            errorItem.setDescription(errorItemRequest.getDescription());
        }
        if (errorItemRequest.getAnalysisStatus() != null) {
            errorItem.setAnalysisStatus(errorItemRequest.getAnalysisStatus());
        }
        if (errorItemRequest.getAnalysis() != null) {
            errorItem.setAnalysis(errorItemRequest.getAnalysis());
        }
        errorItem.setUpdatedAt(LocalDateTime.now());
        return Result.success(errorRepository.save(errorItem));
    }

    @PostMapping("/error-analysis")
    public Result<Map<String, Object>> analyzeError(@RequestBody ProblemAnalysisRequest request) {
        log.info("开始 AI 错题分析, errorId={}", request.getErrorId());

        ProblemAnalysisResponse analysisResponse;
        try {
            analysisResponse = callAIForAnalysis(request);
        } catch (Exception e) {
            log.warn("AI 分析失败，使用降级方案: {}", e.getMessage());
            analysisResponse = generateLocalAnalysis(request);
        }

        String analysisText = convertToText(analysisResponse);
        if (request.getErrorId() != null) {
            Long userId = currentUserService.requireCurrentUserId();
            errorRepository.findByIdAndUserId(request.getErrorId(), userId).ifPresent(errorItem -> {
                errorItem.setAnalysisStatus("已分析");
                errorItem.setAnalysis(analysisText);
                errorItem.setUpdatedAt(LocalDateTime.now());
                errorRepository.save(errorItem);
            });
        }

        Map<String, Object> result = new HashMap<>();
        result.put("errorId", request.getErrorId());
        result.put("analysis", analysisText);
        result.put("analysisData", analysisResponse);
        result.put("analyzedAt", LocalDateTime.now().toString());
        return Result.success(result);
    }

    private ProblemAnalysisResponse callAIForAnalysis(ProblemAnalysisRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(PROBLEM_ANALYSIS_PROMPT));
        messages.add(ChatMessage.user(buildAnalysisPrompt(request)));
        String responseContent = aiService.chat(messages);
        return parseAnalysisResponse(responseContent);
    }

    private String buildAnalysisPrompt(ProblemAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下错题：\n\n");
        prompt.append("题目：").append(request.getQuestion() == null ? "未知" : request.getQuestion()).append("\n");
        String userAnswerStr = request.getUserAnswer() == null ? "未提交" : String.join(", ", request.getUserAnswer());
        prompt.append("用户答案：").append(userAnswerStr).append("\n");
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            prompt.append("补充描述：").append(request.getDescription()).append("\n");
        }
        if (request.getDifficulty() != null && !request.getDifficulty().isBlank()) {
            prompt.append("难度：").append(request.getDifficulty()).append("\n");
        }
        if (request.getTrack() != null && !request.getTrack().isBlank()) {
            prompt.append("赛道：").append(request.getTrack()).append("\n");
        }
        return prompt.toString();
    }

    private ProblemAnalysisResponse parseAnalysisResponse(String content) {
        try {
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            } else if (content.contains("```")) {
                jsonContent = content.replaceAll("(?s)```\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            }
            return objectMapper.readValue(jsonContent, ProblemAnalysisResponse.class);
        } catch (JsonProcessingException e) {
            throw new AIService.AIException("AI 响应解析失败", e);
        }
    }

    private ProblemAnalysisResponse generateLocalAnalysis(ProblemAnalysisRequest request) {
        ProblemAnalysisResponse response = new ProblemAnalysisResponse();
        response.setSummary("这是一道需要回到题意和选项本身重新核对的题目。");

        ProblemAnalysisResponse.ErrorAnalysis errorAnalysis = new ProblemAnalysisResponse.ErrorAnalysis();
        errorAnalysis.setRootCause("对题意、边界或关键知识点的理解还不够稳定");
        errorAnalysis.setDetailedExplanation("建议先复盘题干，再对照自己的答案定位偏差点。");
        errorAnalysis.setCommonMistakes(List.of("忽略边界条件", "概念混淆", "选项理解不完整"));
        response.setErrorAnalysis(errorAnalysis);

        ProblemAnalysisResponse.KnowledgePoint knowledgePoint = new ProblemAnalysisResponse.KnowledgePoint();
        knowledgePoint.setName("基础题意分析");
        knowledgePoint.setDescription("先确认题目到底在考什么，再判断选项或解法。");
        knowledgePoint.setMasteryLevel("beginner");
        knowledgePoint.setRelatedResources(List.of("重看题目说明", "整理错因笔记"));
        response.setKnowledgePoints(List.of(knowledgePoint));

        ProblemAnalysisResponse.ImprovementSuggestion suggestion = new ProblemAnalysisResponse.ImprovementSuggestion();
        suggestion.setTitle("先做一次定向复盘");
        suggestion.setDescription("把正确答案、你的答案、错因三列写清楚。");
        suggestion.setPriority("high");
        suggestion.setActionItems(List.of("重读题干", "对比正确答案", "记录错因"));
        response.setSuggestions(List.of(suggestion));

        ProblemAnalysisResponse.RecommendedProblem recommendedProblem = new ProblemAnalysisResponse.RecommendedProblem();
        recommendedProblem.setTitle("同类型基础练习");
        recommendedProblem.setDifficulty("easy");
        recommendedProblem.setReason("先把当前知识点练熟，再提升难度");
        recommendedProblem.setSource("AlgoMind");
        response.setRecommendedProblems(List.of(recommendedProblem));

        ProblemAnalysisResponse.StudyPlan studyPlan = new ProblemAnalysisResponse.StudyPlan();
        studyPlan.setShortTerm("今天完成本题复盘");
        studyPlan.setMidTerm("本周再练 3 道同类题");
        studyPlan.setLongTerm("建立稳定的审题和答题习惯");
        studyPlan.setDailyTasks(List.of("复盘 1 道错题", "整理 1 条错因", "重做 1 道类似题"));
        response.setStudyPlan(studyPlan);

        return response;
    }

    private String convertToText(ProblemAnalysisResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.getSummary() != null) {
            builder.append("总结：").append(response.getSummary()).append("\n\n");
        }
        if (response.getErrorAnalysis() != null) {
            builder.append("根因：").append(response.getErrorAnalysis().getRootCause()).append("\n");
            builder.append("说明：").append(response.getErrorAnalysis().getDetailedExplanation()).append("\n");
        }
        if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
            builder.append("\n建议：\n");
            for (ProblemAnalysisResponse.ImprovementSuggestion suggestion : response.getSuggestions()) {
                builder.append("- ").append(suggestion.getTitle()).append("：").append(suggestion.getDescription()).append("\n");
            }
        }
        return builder.toString().trim();
    }
}
