package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.dto.ai.ProblemAnalysisRequest;
import com.example.demo.dto.ai.ProblemAnalysisResponse;
import com.example.demo.entity.CompletedErrorItem;
import com.example.demo.entity.ErrorItem;
import com.example.demo.repository.CompletedErrorItemRepository;
import com.example.demo.repository.ErrorItemRepository;
import com.example.demo.service.AIService;
import com.example.demo.service.ErrorBookService;
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

    private static final String ANALYSIS_PENDING = "UNANALYZED";
    private static final String ANALYSIS_COMPLETED = "ANALYZED";

    private static final String PROBLEM_ANALYSIS_PROMPT = """
            You are an experienced algorithm coach.
            Please analyze the learner's wrong answer and return a structured JSON report.
            Focus on root cause, key knowledge points, practice suggestions, and a short study plan.
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
    private ErrorBookService errorBookService;

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
                .orElseGet(() -> Result.fail(40401, "error item not found"));
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
            return Result.fail(40401, "error item not found");
        }
    }

    @DeleteMapping("/errors/{id}")
    public Result<Map<String, Object>> deleteError(@PathVariable Long id) {
        Long userId = currentUserService.requireCurrentUserId();
        Optional<ErrorItem> errorItem = errorRepository.findByIdAndUserId(id, userId);
        if (errorItem.isEmpty()) {
            return Result.fail(40401, "error item not found");
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
            return Result.fail(40401, "error item not found");
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
        log.info("Start error analysis, errorId={}", request.getErrorId());

        ProblemAnalysisResponse analysisResponse;
        try {
            analysisResponse = callAIForAnalysis(request);
        } catch (Exception exception) {
            log.warn("AI analysis failed, fallback to local analysis: {}", exception.getMessage());
            analysisResponse = generateLocalAnalysis(request);
        }

        String analysisText = convertToText(analysisResponse);
        if (request.getErrorId() != null) {
            Long userId = currentUserService.requireCurrentUserId();
            errorRepository.findByIdAndUserId(request.getErrorId(), userId).ifPresent(errorItem -> {
                errorItem.setAnalysisStatus(ANALYSIS_COMPLETED);
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
        prompt.append("Please analyze the following wrong answer.\n\n");
        prompt.append("Question: ").append(nullToText(request.getQuestion())).append('\n');
        prompt.append("User answer: ").append(joinList(request.getUserAnswer())).append('\n');

        if (hasText(request.getDescription())) {
            prompt.append("Description: ").append(request.getDescription()).append('\n');
        }
        if (hasText(request.getDifficulty())) {
            prompt.append("Difficulty: ").append(request.getDifficulty()).append('\n');
        }
        if (hasText(request.getTrack())) {
            prompt.append("Track: ").append(request.getTrack()).append('\n');
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
        } catch (JsonProcessingException exception) {
            throw new AIService.AIException("Failed to parse AI analysis response", exception);
        }
    }

    private ProblemAnalysisResponse generateLocalAnalysis(ProblemAnalysisRequest request) {
        ProblemAnalysisResponse response = new ProblemAnalysisResponse();
        response.setSummary("Review the question intent first, then compare the wrong answer with the expected answer.");

        ProblemAnalysisResponse.ErrorAnalysis errorAnalysis = new ProblemAnalysisResponse.ErrorAnalysis();
        errorAnalysis.setRootCause("The learner likely missed a key condition, concept, or boundary case.");
        errorAnalysis.setDetailedExplanation("Re-read the prompt, identify the mismatch, and summarize the mistake in one sentence.");
        errorAnalysis.setCommonMistakes(List.of("Missed boundary condition", "Mixed up concepts", "Incomplete option comparison"));
        response.setErrorAnalysis(errorAnalysis);

        ProblemAnalysisResponse.KnowledgePoint knowledgePoint = new ProblemAnalysisResponse.KnowledgePoint();
        knowledgePoint.setName("Question intent analysis");
        knowledgePoint.setDescription("Clarify what the question is really asking before evaluating answers.");
        knowledgePoint.setMasteryLevel("beginner");
        knowledgePoint.setRelatedResources(List.of("Review the prompt", "Write down the error reason"));
        response.setKnowledgePoints(List.of(knowledgePoint));

        ProblemAnalysisResponse.ImprovementSuggestion suggestion = new ProblemAnalysisResponse.ImprovementSuggestion();
        suggestion.setTitle("Do one focused review");
        suggestion.setDescription("Write the correct answer, your answer, and the root cause side by side.");
        suggestion.setPriority("high");
        suggestion.setActionItems(List.of("Re-read the prompt", "Compare with the correct answer", "Record the mistake"));
        response.setSuggestions(List.of(suggestion));

        ProblemAnalysisResponse.RecommendedProblem recommendedProblem = new ProblemAnalysisResponse.RecommendedProblem();
        recommendedProblem.setTitle("Practice one similar basic question");
        recommendedProblem.setDifficulty("easy");
        recommendedProblem.setReason("Reinforce the same knowledge point before increasing difficulty.");
        recommendedProblem.setSource("AlgoMind");
        response.setRecommendedProblems(List.of(recommendedProblem));

        ProblemAnalysisResponse.StudyPlan studyPlan = new ProblemAnalysisResponse.StudyPlan();
        studyPlan.setShortTerm("Finish the review of this problem today.");
        studyPlan.setMidTerm("Practice three similar problems this week.");
        studyPlan.setLongTerm("Build a stable habit for reviewing wrong answers.");
        studyPlan.setDailyTasks(List.of("Review one wrong answer", "Write one mistake note", "Redo one similar problem"));
        response.setStudyPlan(studyPlan);

        return response;
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
