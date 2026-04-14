package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.ai.AnalysisQuotaStatus;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.dto.ai.ProblemAnalysisRequest;
import com.example.demo.dto.ai.ProblemAnalysisResponse;
import com.example.demo.dto.ai.ProblemAnalysisResult;
import com.example.demo.entity.CompletedErrorItem;
import com.example.demo.entity.ErrorItem;
import com.example.demo.entity.Level;
import com.example.demo.repository.CompletedErrorItemRepository;
import com.example.demo.repository.ErrorItemRepository;
import com.example.demo.repository.LevelRepository;
import com.example.demo.service.AIService;
import com.example.demo.service.AiAnalysisQuotaService;
import com.example.demo.service.ErrorBookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

@Slf4j
@RestController
public class ErrorController {

    private static final String ANALYSIS_PENDING = "UNANALYZED";
    private static final String ANALYSIS_COMPLETED = "ANALYZED";

    private static final String PROBLEM_ANALYSIS_PROMPT = """
            你是一位经验丰富的算法教练。
            请分析学习者的错误答案，并返回结构化的 JSON 报告。
            所有内容必须使用中文返回。

            请严格按照以下 JSON 格式返回四个模块：

            {
              "summary": "分析总结（一句话概括核心问题）",
              "errorAnalysis": {
                "rootCause": "错误根因（分析导致错误的根本原因）",
                "detailedExplanation": "详细解释（对错误原因的详细说明）",
                "commonMistakes": ["常见错误1", "常见错误2", "常见错误3"]
              },
              "knowledgePoints": [
                {
                  "name": "关键知识点名称",
                  "description": "知识点描述（简明扼要地解释该知识点）",
                  "masteryLevel": "掌握程度（beginner/intermediate/advanced）"
                }
              ],
              "suggestions": [
                {
                  "title": "改进建议标题",
                  "description": "建议详细描述",
                  "priority": "优先级（high/medium/low）",
                  "actionItems": ["具体行动1", "具体行动2"]
                }
              ],
              "recommendedProblems": [
                {
                  "title": "题目名称",
                  "question": "题目内容",
                  "type": "题目类型（practice/review/challenge）",
                  "difficulty": "难度（easy/medium/hard）",
                  "reason": "推荐理由（为什么推荐这道题）"
                }
              ]
            }

            四个核心模块说明：
            1. errorAnalysis（错误根因）：深入分析用户答案错误的根本原因，包括概念误解、思路偏差、细节疏忽等
            2. knowledgePoints（关键知识点）：列出与错题相关的核心知识点，帮助用户查漏补缺
            3. suggestions（改进建议）：提供具体、可执行的改进措施，按优先级排序
            4. recommendedProblems（题库优先的练习）：优先从题库角度推荐适合巩固的练习题，包含推荐理由

            请确保返回的是有效的 JSON 格式，不要包含 markdown 代码块标记。
            """;

    @Resource
    private ErrorItemRepository errorRepository;

    @Resource
    private CompletedErrorItemRepository completedErrorItemRepository;

    @Resource
    private CurrentUserService currentUserService;

    @Resource
    private AIService aiService;

    @Resource
    private AiAnalysisQuotaService aiAnalysisQuotaService;

    @Resource
    private ErrorBookService errorBookService;

    @Resource
    private LevelRepository levelRepository;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping("/errors")
    public Result<List<ErrorItem>> getErrors() {
        Long userId = currentUserService.requireCurrentUserId();
        return Result.success(errorRepository.findByUserIdOrderByUpdatedAtDesc(userId));
    }

    @GetMapping("/errors/completed")
    public Result<List<CompletedErrorItem>> getCompletedErrors() {
        Long userId = currentUserService.requireCurrentUserId();
        return Result.success(completedErrorItemRepository.findByUserIdOrderByCompletedAtDesc(userId));
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
            errorItem.setAnalysisStatus(ANALYSIS_PENDING);
        }

        ErrorItem saved = errorRepository.save(errorItem);
        if (saved.getLevelId() != null) {
            errorBookService.clearCompletedByLevel(userId, saved.getLevelId());
        }
        return Result.success(saved);
    }

    @PostMapping("/errors/{id}/complete")
    public Result<CompletedErrorItem> completeError(@PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        try {
            return Result.success(errorBookService.completeError(userId, id));
        } catch (NoSuchElementException exception) {
            return Result.fail(40401, "错题不存在");
        }
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
        if (errorItemRequest.getAnalysisDataJson() != null) {
            errorItem.setAnalysisDataJson(errorItemRequest.getAnalysisDataJson());
        }
        if (errorItemRequest.getAnalyzedAt() != null) {
            errorItem.setAnalyzedAt(errorItemRequest.getAnalyzedAt());
        }
        errorItem.setUpdatedAt(LocalDateTime.now());
        return Result.success(errorRepository.save(errorItem));
    }

    @PostMapping("/error-analysis")
    public Result<ProblemAnalysisResult> analyzeError(@RequestBody ProblemAnalysisRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        long startTime = System.currentTimeMillis();
        log.info("Start error analysis, userId={}, errorId={}", userId, request.getErrorId());

        Optional<ErrorItem> optionalErrorItem = loadErrorItem(userId, request.getErrorId());
        if (request.getErrorId() != null && optionalErrorItem.isEmpty()) {
            return Result.fail(40401, "错题不存在");
        }

        AiAnalysisQuotaService.SlotReservation reservation = aiAnalysisQuotaService.reserveCurrentSlot(userId, request.getErrorId());
        if (!reservation.acquired()) {
            AnalysisQuotaStatus quotaStatus = reservation.quotaStatus();
            if (optionalErrorItem.isPresent() && hasStoredAnalysis(optionalErrorItem.get())) {
                ProblemAnalysisResult reusedResult = buildStoredAnalysisResult(
                        optionalErrorItem.get(),
                        quotaStatus,
                        buildReuseMessage(quotaStatus)
                );
                return Result.success(reusedResult);
            }
            return Result.fail(42901, buildLimitExceededMessage(quotaStatus));
        }

        try {
            ProblemAnalysisResponse analysisResponse;
            try {
                analysisResponse = callAIForAnalysis(request);
            } catch (Exception exception) {
                log.warn("AI analysis failed, fallback to local analysis: {}", exception.getMessage());
                analysisResponse = generateLocalAnalysis(request);
            }

            Long currentLevelId = optionalErrorItem.map(ErrorItem::getLevelId).orElse(null);
            analysisResponse.setRecommendedProblems(buildRecommendedProblems(request, analysisResponse, currentLevelId));

            String analysisText = convertToText(analysisResponse);
            String analysisDataJson = serializeAnalysisData(analysisResponse);
            LocalDateTime analyzedAt = LocalDateTime.now();

            optionalErrorItem.ifPresent(errorItem -> {
                errorItem.setAnalysisStatus(ANALYSIS_COMPLETED);
                errorItem.setAnalysis(analysisText);
                errorItem.setAnalysisDataJson(analysisDataJson);
                errorItem.setAnalyzedAt(analyzedAt);
                errorItem.setUpdatedAt(analyzedAt);
                errorRepository.save(errorItem);
            });

            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;
            log.info("AI analysis completed, userId={}, errorId={}, duration={}ms", userId, request.getErrorId(), durationMs);

            ProblemAnalysisResult result = new ProblemAnalysisResult();
            result.setErrorId(request.getErrorId());
            result.setAnalysis(analysisText);
            result.setAnalysisData(analysisResponse);
            result.setAnalyzedAt(analyzedAt.toString());
            result.setReusedLastAnalysis(false);
            result.setLimitReached(false);
            result.setMessage("分析生成成功 (耗时 " + durationMs + "ms)");
            result.setQuota(reservation.quotaStatus());
            return Result.success(result);
        } catch (Exception exception) {
            aiAnalysisQuotaService.releaseReservation(userId, reservation);
            log.error("Failed to analyze error, userId={}, errorId={}", userId, request.getErrorId(), exception);
            return Result.fail(50001, "AI 分析失败，请稍后重试");
        }
    }

    private Optional<ErrorItem> loadErrorItem(Long userId, Long errorId) {
        if (errorId == null) {
            return Optional.empty();
        }
        return errorRepository.findByIdAndUserId(errorId, userId);
    }

    private boolean hasStoredAnalysis(ErrorItem errorItem) {
        return hasText(errorItem.getAnalysis()) || hasText(errorItem.getAnalysisDataJson());
    }

    private String buildReuseMessage(AnalysisQuotaStatus quotaStatus) {
        if (quotaStatus.isExhausted()) {
            return "今日 AI 分析次数已用完，已返回这道错题最后一次生成的分析结果。下次可用时间：" + quotaStatus.getNextRefreshAt();
        }
        return "当前时间段的 AI 分析机会已使用，已返回这道错题最后一次生成的分析结果。下次可用时间：" + quotaStatus.getNextRefreshAt();
    }

    private String buildLimitExceededMessage(AnalysisQuotaStatus quotaStatus) {
        if (quotaStatus.isExhausted()) {
            return "今日 AI 分析次数已用完，且当前错题暂无可回退的历史分析结果。下次可用时间：" + quotaStatus.getNextRefreshAt();
        }
        return "当前时间段的 AI 分析机会已使用，且当前错题暂无可回退的历史分析结果。下次可用时间：" + quotaStatus.getNextRefreshAt();
    }

    private ProblemAnalysisResult buildStoredAnalysisResult(
            ErrorItem errorItem,
            AnalysisQuotaStatus quotaStatus,
            String message
    ) {
        ProblemAnalysisResult result = new ProblemAnalysisResult();
        result.setErrorId(errorItem.getId());
        result.setAnalysis(errorItem.getAnalysis());
        result.setAnalysisData(parseStoredAnalysisData(errorItem.getAnalysisDataJson()));
        result.setAnalyzedAt(errorItem.getAnalyzedAt() != null ? errorItem.getAnalyzedAt().toString() : null);
        result.setReusedLastAnalysis(true);
        result.setLimitReached(true);
        result.setMessage(message);
        result.setQuota(quotaStatus);
        return result;
    }

    private ProblemAnalysisResponse parseStoredAnalysisData(String analysisDataJson) {
        if (!hasText(analysisDataJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(analysisDataJson, ProblemAnalysisResponse.class);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to parse stored analysis data", exception);
            return null;
        }
    }

    private String serializeAnalysisData(ProblemAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new AIService.AIException("Failed to serialize analysis response", exception);
        }
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
        prompt.append("请分析以下错题，所有回复必须使用中文，并严格按照系统提示词中定义的 JSON 格式返回。\n\n");
        prompt.append("题目: ").append(nullToText(request.getQuestion())).append('\n');
        prompt.append("用户答案: ").append(joinList(request.getUserAnswer())).append('\n');

        if (hasText(request.getDescription())) {
            prompt.append("题目描述: ").append(request.getDescription()).append('\n');
        }
        if (hasText(request.getDifficulty())) {
            prompt.append("难度: ").append(request.getDifficulty()).append('\n');
        }
        if (hasText(request.getTrack())) {
            prompt.append("赛道: ").append(request.getTrack()).append('\n');
        }
        if (request.getRelatedTopics() != null && !request.getRelatedTopics().isEmpty()) {
            prompt.append("相关知识点: ").append(String.join("、", request.getRelatedTopics())).append('\n');
        }

        prompt.append("\n请重点分析以下四个模块并返回对应 JSON 字段：\n");
        prompt.append("1. errorAnalysis（错误根因）：分析用户答案错误的根本原因\n");
        prompt.append("2. knowledgePoints（关键知识点）：列出与错题相关的核心知识点\n");
        prompt.append("3. suggestions（改进建议）：提供具体、可执行的改进措施\n");
        prompt.append("4. recommendedProblems（题库优先的练习）：优先从题库角度推荐适合巩固的练习题\n");
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
        } catch (JsonProcessingException exception) {
            throw new AIService.AIException("Failed to parse AI analysis response", exception);
        }
    }

    private ProblemAnalysisResponse generateLocalAnalysis(ProblemAnalysisRequest request) {
        ProblemAnalysisResponse response = new ProblemAnalysisResponse();
        response.setSummary("先重新审题，再对比你的答案和正确思路之间的差异，优先找出最关键的误区。");

        ProblemAnalysisResponse.ErrorAnalysis errorAnalysis = new ProblemAnalysisResponse.ErrorAnalysis();
        errorAnalysis.setRootCause("可能遗漏了题目中的关键条件，或者对知识点理解还不够稳定。");
        errorAnalysis.setDetailedExplanation("建议重新梳理题目要求，定位答案不匹配的具体步骤，并总结成一句可以复用的纠错提醒。");
        errorAnalysis.setCommonMistakes(List.of("遗漏边界条件", "概念混淆", "解题步骤不完整"));
        response.setErrorAnalysis(errorAnalysis);

        ProblemAnalysisResponse.KnowledgePoint knowledgePoint = new ProblemAnalysisResponse.KnowledgePoint();
        knowledgePoint.setName("题意拆解");
        knowledgePoint.setDescription("在动手作答前，先明确题目真正要求的目标、限制条件和判定标准。");
        knowledgePoint.setMasteryLevel("beginner");
        response.setKnowledgePoints(List.of(knowledgePoint));

        ProblemAnalysisResponse.ImprovementSuggestion suggestion = new ProblemAnalysisResponse.ImprovementSuggestion();
        suggestion.setTitle("做一次针对性复盘");
        suggestion.setDescription("把正确思路、你的答案和错误原因并排写出来，找出最容易重复出错的步骤。");
        suggestion.setPriority("high");
        suggestion.setActionItems(List.of("重新审题", "对照正确思路", "记录错误模式"));
        response.setSuggestions(List.of(suggestion));

        ProblemAnalysisResponse.RecommendedProblem recommendedProblem = new ProblemAnalysisResponse.RecommendedProblem();
        recommendedProblem.setTitle("补一题同类型基础练习");
        recommendedProblem.setQuestion("请围绕这道错题的核心知识点，再完成一题同类型但难度更基础的练习。");
        recommendedProblem.setType("practice");
        recommendedProblem.setDifficulty("easy");
        recommendedProblem.setReason("先把相同知识点的基础题做稳，再继续提升难度。");
        recommendedProblem.setFromQuestionBank(false);
        response.setRecommendedProblems(List.of(recommendedProblem));

        return response;
    }

    private List<ProblemAnalysisResponse.RecommendedProblem> buildRecommendedProblems(
            ProblemAnalysisRequest request,
            ProblemAnalysisResponse analysisResponse,
            Long currentLevelId
    ) {
        List<ProblemAnalysisResponse.RecommendedProblem> bankProblems = findQuestionBankRecommendations(
                request,
                analysisResponse,
                currentLevelId
        );
        if (!bankProblems.isEmpty()) {
            return bankProblems;
        }

        List<ProblemAnalysisResponse.RecommendedProblem> aiProblems = normalizeAiGeneratedProblems(
                analysisResponse.getRecommendedProblems(),
                request
        );
        if (!aiProblems.isEmpty()) {
            return aiProblems;
        }

        return List.of(buildLocalGeneratedProblem(request, analysisResponse));
    }

    private List<ProblemAnalysisResponse.RecommendedProblem> findQuestionBankRecommendations(
            ProblemAnalysisRequest request,
            ProblemAnalysisResponse analysisResponse,
            Long currentLevelId
    ) {
        List<Level> candidateLevels = hasText(request.getTrack())
                ? levelRepository.findByTrack(request.getTrack())
                : levelRepository.findAll();

        List<String> keywords = collectRecommendationKeywords(request, analysisResponse);

        return candidateLevels.stream()
                .filter(level -> level.getId() != null && !Objects.equals(level.getId(), currentLevelId))
                .map(level -> new AbstractMap.SimpleEntry<>(level, scoreLevelRecommendation(level, keywords, request)))
                .filter(entry -> entry.getValue() > 0)
                .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
                .limit(3)
                .map(entry -> toQuestionBankRecommendation(entry.getKey(), request, analysisResponse))
                .toList();
    }

    private List<String> collectRecommendationKeywords(
            ProblemAnalysisRequest request,
            ProblemAnalysisResponse analysisResponse
    ) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();

        addKeyword(keywords, request.getQuestion());
        addKeyword(keywords, request.getDescription());
        addKeyword(keywords, request.getDifficulty());

        if (request.getRelatedTopics() != null) {
            request.getRelatedTopics().forEach(topic -> addKeyword(keywords, topic));
        }

        if (analysisResponse.getKnowledgePoints() != null) {
            analysisResponse.getKnowledgePoints().forEach(point -> {
                addKeyword(keywords, point.getName());
                addKeyword(keywords, point.getDescription());
            });
        }

        if (analysisResponse.getRecommendedProblems() != null) {
            analysisResponse.getRecommendedProblems().forEach(problem -> {
                addKeyword(keywords, problem.getTitle());
                addKeyword(keywords, problem.getReason());
            });
        }

        return new ArrayList<>(keywords);
    }

    private void addKeyword(Set<String> keywords, String text) {
        if (!hasText(text)) {
            return;
        }

        String normalized = text.trim();
        keywords.add(normalized);

        for (String token : normalized.split("[\\s,;:，、；。]+")) {
            if (token != null && token.trim().length() >= 2) {
                keywords.add(token.trim());
            }
        }
    }

    private int scoreLevelRecommendation(Level level, List<String> keywords, ProblemAnalysisRequest request) {
        int score = 0;
        String haystack = String.join(" ",
                safeLower(level.getName()),
                safeLower(level.getQuestion()),
                safeLower(level.getDescription()),
                safeLower(level.getType())
        );

        for (String keyword : keywords) {
            String normalizedKeyword = safeLower(keyword);
            if (!hasText(normalizedKeyword)) {
                continue;
            }
            if (haystack.contains(normalizedKeyword)) {
                score += Math.min(6, Math.max(2, normalizedKeyword.length() / 2));
            }
        }

        if (hasText(request.getDifficulty()) && safeLower(level.getDescription()).contains(safeLower(request.getDifficulty()))) {
            score += 2;
        }

        if (hasText(request.getTrack()) && Objects.equals(request.getTrack(), level.getTrack())) {
            score += 3;
        }

        return score;
    }

    private ProblemAnalysisResponse.RecommendedProblem toQuestionBankRecommendation(
            Level level,
            ProblemAnalysisRequest request,
            ProblemAnalysisResponse analysisResponse
    ) {
        ProblemAnalysisResponse.RecommendedProblem recommendedProblem = new ProblemAnalysisResponse.RecommendedProblem();
        recommendedProblem.setLevelId(level.getId());
        recommendedProblem.setTitle(hasText(level.getName()) ? level.getName() : "题库推荐练习题");
        recommendedProblem.setQuestion(level.getQuestion());
        recommendedProblem.setType(level.getType());
        recommendedProblem.setDifficulty(resolveLevelDifficulty(level, request));
        recommendedProblem.setReason(buildQuestionBankReason(level, analysisResponse));
        recommendedProblem.setFromQuestionBank(true);
        recommendedProblem.setOptions(level.getOptions());
        return recommendedProblem;
    }

    private String resolveLevelDifficulty(Level level, ProblemAnalysisRequest request) {
        if (hasText(request.getDifficulty())) {
            return request.getDifficulty();
        }

        Integer rewardPoints = level.getRewardPoints();
        if (rewardPoints == null) {
            return "medium";
        }
        if (rewardPoints <= 20) {
            return "easy";
        }
        if (rewardPoints <= 60) {
            return "medium";
        }
        return "hard";
    }

    private String buildQuestionBankReason(Level level, ProblemAnalysisResponse analysisResponse) {
        if (analysisResponse.getKnowledgePoints() != null && !analysisResponse.getKnowledgePoints().isEmpty()) {
            String pointName = analysisResponse.getKnowledgePoints().stream()
                    .map(ProblemAnalysisResponse.KnowledgePoint::getName)
                    .filter(this::hasText)
                    .findFirst()
                    .orElse(null);
            if (hasText(pointName)) {
                return "题库中找到了和「" + pointName + "」相关的练习题，适合立刻巩固。";
            }
        }

        if (hasText(level.getDescription())) {
            return "题库中有一道与当前错题场景接近的题，建议继续练习巩固：" + level.getDescription();
        }

        return "题库中找到了相近题型，适合继续练习。";
    }

    private List<ProblemAnalysisResponse.RecommendedProblem> normalizeAiGeneratedProblems(
            List<ProblemAnalysisResponse.RecommendedProblem> aiProblems,
            ProblemAnalysisRequest request
    ) {
        if (aiProblems == null || aiProblems.isEmpty()) {
            return List.of();
        }

        return aiProblems.stream()
                .filter(Objects::nonNull)
                .map(problem -> {
                    if (!hasText(problem.getTitle()) && !hasText(problem.getQuestion())) {
                        return null;
                    }
                    problem.setFromQuestionBank(false);
                    if (!hasText(problem.getDifficulty())) {
                        problem.setDifficulty(hasText(request.getDifficulty()) ? request.getDifficulty() : "medium");
                    }
                    if (!hasText(problem.getType())) {
                        problem.setType("practice");
                    }
                    if (!hasText(problem.getReason())) {
                        problem.setReason("题库中暂时没有足够匹配的题目，AI 为你补充了一道针对性练习题。");
                    }
                    if (!hasText(problem.getQuestion())) {
                        problem.setQuestion(problem.getTitle());
                    }
                    return problem;
                })
                .filter(Objects::nonNull)
                .limit(3)
                .toList();
    }

    private ProblemAnalysisResponse.RecommendedProblem buildLocalGeneratedProblem(
            ProblemAnalysisRequest request,
            ProblemAnalysisResponse analysisResponse
    ) {
        ProblemAnalysisResponse.RecommendedProblem recommendedProblem = new ProblemAnalysisResponse.RecommendedProblem();
        String focus = analysisResponse.getKnowledgePoints() != null && !analysisResponse.getKnowledgePoints().isEmpty()
                ? nullToText(analysisResponse.getKnowledgePoints().get(0).getName())
                : firstNonBlank(request.getRelatedTopics());

        if (!hasText(focus)) {
            focus = "当前错题的核心知识点";
        }

        recommendedProblem.setTitle("AI 自适应练习题");
        recommendedProblem.setQuestion("请围绕「" + focus + "」设计解题思路，并完成一道同类型练习。要求先写出关键步骤，再给出最终答案。");
        recommendedProblem.setType("practice");
        recommendedProblem.setDifficulty(hasText(request.getDifficulty()) ? request.getDifficulty() : "medium");
        recommendedProblem.setReason("题库中暂时没有足够匹配的题目，已根据你的错题知识点自动生成一题。");
        recommendedProblem.setFromQuestionBank(false);
        return recommendedProblem;
    }

    private String firstNonBlank(List<String> values) {
        if (values == null) {
            return null;
        }
        return values.stream().filter(this::hasText).findFirst().orElse(null);
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String convertToText(ProblemAnalysisResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.getSummary() != null) {
            builder.append("Summary: ").append(response.getSummary()).append("\n\n");
        }
        if (response.getErrorAnalysis() != null) {
            builder.append("Root cause: ").append(nullToText(response.getErrorAnalysis().getRootCause())).append('\n');
            builder.append("Explanation: ")
                    .append(nullToText(response.getErrorAnalysis().getDetailedExplanation()))
                    .append('\n');
        }
        if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
            builder.append("\nSuggestions:\n");
            for (ProblemAnalysisResponse.ImprovementSuggestion suggestion : response.getSuggestions()) {
                builder.append("- ")
                        .append(nullToText(suggestion.getTitle()))
                        .append(": ")
                        .append(nullToText(suggestion.getDescription()))
                        .append('\n');
            }
        }
        return builder.toString().trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToText(String value) {
        return hasText(value) ? value : "N/A";
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "N/A";
        }
        return String.join(", ", values);
    }
}
