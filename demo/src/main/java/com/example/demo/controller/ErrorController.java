package com.example.demo.controller;

import com.example.demo.Result;
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
import java.util.ArrayList;
import java.util.*;

@Slf4j
@RestController
public class ErrorController {

    @Resource
    private ErrorItemRepository errorRepository;

    @Resource
    private AIService aiService;

    @Resource
    private ObjectMapper objectMapper;

    private static final String PROBLEM_ANALYSIS_PROMPT = """
        你是一位资深的算法教练，擅长分析学生的错题并提供针对性的学习建议。
        
        请根据提供的错题信息，生成一份详细的分析报告。输出格式必须是标准的JSON格式，结构如下：
        {
          "summary": "简短总结这道题的核心难点和学生的问题",
          "errorAnalysis": {
            "rootCause": "根本原因（如：概念理解偏差、边界条件遗漏、算法选择不当等）",
            "detailedExplanation": "详细解释为什么会出现这种错误",
            "commonMistakes": ["常见错误1", "常见错误2", "常见错误3"]
          },
          "knowledgePoints": [
            {
              "name": "知识点名称",
              "description": "知识点说明",
              "masteryLevel": "beginner/intermediate/advanced",
              "relatedResources": ["推荐资源1", "推荐资源2"]
            }
          ],
          "suggestions": [
            {
              "title": "建议标题",
              "description": "详细说明",
              "priority": "high/medium/low",
              "actionItems": ["具体行动1", "具体行动2"]
            }
          ],
          "recommendedProblems": [
            {
              "title": "推荐题目名称",
              "difficulty": "easy/medium/hard",
              "reason": "推荐理由",
              "source": "来源（如LeetCode、牛客网等）"
            }
          ],
          "studyPlan": {
            "shortTerm": "短期目标（本周）",
            "midTerm": "中期目标（本月）",
            "longTerm": "长期目标（本季度）",
            "dailyTasks": ["每日任务1", "每日任务2", "每日任务3"]
          }
        }
        
        注意事项：
        1. 必须严格按照JSON格式输出，不要添加任何其他文字说明
        2. 根据学生的等级和题目难度调整建议的深度
        3. 如果题目通过率低，说明是难题，要给予鼓励
        4. 如果用户多次尝试失败，要分析可能的思维误区
        5. 推荐的练习题要与当前题目知识点相关
        """;

    @GetMapping("/errors")
    public Result<List<ErrorItem>> getErrors() {
        return Result.success(errorRepository.findAll());
    }

    @GetMapping("/errors/{id}")
    public Result<ErrorItem> getErrorById(@PathVariable Long id) {
        Optional<ErrorItem> errorItem = errorRepository.findById(id);
        if (errorItem.isEmpty()) {
            return Result.fail(40401, "错题不存在");
        }
        return Result.success(errorItem.get());
    }

    @PostMapping("/errors")
    public Result<ErrorItem> addError(@RequestBody ErrorItem errorItem) {
        errorItem.setCreatedAt(LocalDateTime.now());
        if (errorItem.getAnalysisStatus() == null) {
            errorItem.setAnalysisStatus("未分析");
        }
        ErrorItem saved = errorRepository.save(errorItem);
        return Result.success(saved);
    }

    @DeleteMapping("/errors/{id}")
    public Result<Map<String, Object>> deleteError(@PathVariable Long id) {
        Optional<ErrorItem> errorItem = errorRepository.findById(id);
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
        Optional<ErrorItem> optionalError = errorRepository.findById(id);
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
        
        ErrorItem saved = errorRepository.save(errorItem);
        return Result.success(saved);
    }

    @PostMapping("/error-analysis")
    public Result<Map<String, Object>> analyzeError(@RequestBody ProblemAnalysisRequest request) {
        log.info("开始AI分析，错题ID: {}, 题目: {}", request.getErrorId(), request.getQuestion());

        ProblemAnalysisResponse analysisResponse;
        
        try {
            analysisResponse = callAIForAnalysis(request);
        } catch (Exception e) {
            log.warn("AI分析失败，使用本地降级方案: {}", e.getMessage());
            analysisResponse = generateLocalAnalysis(request);
        }

        String analysisText = convertToText(analysisResponse);

        if (request.getErrorId() != null) {
            Optional<ErrorItem> optionalError = errorRepository.findById(request.getErrorId());
            if (optionalError.isPresent()) {
                ErrorItem errorItem = optionalError.get();
                errorItem.setAnalysisStatus("已分析");
                errorItem.setAnalysis(analysisText);
                errorRepository.save(errorItem);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("errorId", request.getErrorId());
        result.put("analysis", analysisText);
        result.put("analysisData", analysisResponse);
        result.put("analyzedAt", LocalDateTime.now().toString());

        return Result.success(result);
    }

    private ProblemAnalysisResponse callAIForAnalysis(ProblemAnalysisRequest request) {
        String userPrompt = buildAnalysisPrompt(request);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(PROBLEM_ANALYSIS_PROMPT));
        messages.add(ChatMessage.user(userPrompt));

        String responseContent = aiService.chat(messages);
        return parseAnalysisResponse(responseContent);
    }

    private String buildAnalysisPrompt(ProblemAnalysisRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下错题：\n\n");

        prompt.append("【题目信息】\n");
        prompt.append("- 题目：").append(request.getQuestion() != null ? request.getQuestion() : "未知").append("\n");
        prompt.append("- 难度：").append(request.getDifficulty() != null ? request.getDifficulty() : "未知").append("\n");
        prompt.append("- 赛道：").append(request.getTrack() != null ? request.getTrack() : "未知").append("\n");
        
        if (request.getSolveRate() != null) {
            prompt.append("- 全站通过率：").append(request.getSolveRate()).append("%\n");
        }
        if (request.getAvgTimeSeconds() != null) {
            int minutes = request.getAvgTimeSeconds() / 60;
            int seconds = request.getAvgTimeSeconds() % 60;
            prompt.append("- 平均做题时长：").append(minutes).append("分").append(seconds).append("秒\n");
        }
        
        prompt.append("\n【学生答题情况】\n");
        prompt.append("- 学生答案：").append(request.getUserAnswer() != null ? request.getUserAnswer() : "未提交").append("\n");
        prompt.append("- 学生等级：").append(request.getUserLevel() != null ? request.getUserLevel() : "未知").append("\n");
        
        if (request.getUserAttempts() != null) {
            prompt.append("- 尝试次数：").append(request.getUserAttempts()).append("次\n");
        }
        
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            prompt.append("\n【题目描述/错误详情】\n");
            prompt.append(request.getDescription()).append("\n");
        }

        if (request.getRelatedTopics() != null && !request.getRelatedTopics().isEmpty()) {
            prompt.append("\n【相关知识点】\n");
            prompt.append(String.join("、", request.getRelatedTopics())).append("\n");
        }

        prompt.append("\n请生成详细的分析报告，严格按照JSON格式输出。");
        
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
            log.error("解析AI响应失败: {}", e.getMessage());
            throw new AIService.AIException("AI响应解析失败", e);
        }
    }

    private ProblemAnalysisResponse generateLocalAnalysis(ProblemAnalysisRequest request) {
        ProblemAnalysisResponse response = new ProblemAnalysisResponse();
        
        response.setSummary("根据题目分析，这是一道需要仔细思考的算法题。");

        ProblemAnalysisResponse.ErrorAnalysis errorAnalysis = new ProblemAnalysisResponse.ErrorAnalysis();
        errorAnalysis.setRootCause("需要进一步分析题目条件和边界情况");
        errorAnalysis.setDetailedExplanation("建议仔细审题，注意题目中的约束条件和特殊情况。");
        errorAnalysis.setCommonMistakes(Arrays.asList(
            "边界条件处理不当",
            "时间复杂度估算错误",
            "特殊情况遗漏"
        ));
        response.setErrorAnalysis(errorAnalysis);

        List<ProblemAnalysisResponse.KnowledgePoint> knowledgePoints = new ArrayList<>();
        ProblemAnalysisResponse.KnowledgePoint kp = new ProblemAnalysisResponse.KnowledgePoint();
        kp.setName("算法基础");
        kp.setDescription("掌握基本算法思想和复杂度分析");
        kp.setMasteryLevel("intermediate");
        kp.setRelatedResources(Arrays.asList("算法导论", "LeetCode题解"));
        knowledgePoints.add(kp);
        response.setKnowledgePoints(knowledgePoints);

        List<ProblemAnalysisResponse.ImprovementSuggestion> suggestions = new ArrayList<>();
        ProblemAnalysisResponse.ImprovementSuggestion s = new ProblemAnalysisResponse.ImprovementSuggestion();
        s.setTitle("加强基础练习");
        s.setDescription("从简单题目开始，逐步提升难度");
        s.setPriority("high");
        s.setActionItems(Arrays.asList("每天完成2-3道简单题", "总结解题模板", "复习错题本"));
        suggestions.add(s);
        response.setSuggestions(suggestions);

        List<ProblemAnalysisResponse.RecommendedProblem> problems = new ArrayList<>();
        ProblemAnalysisResponse.RecommendedProblem p = new ProblemAnalysisResponse.RecommendedProblem();
        p.setTitle("两数之和");
        p.setDifficulty("easy");
        p.setReason("巩固哈希表基础");
        p.setSource("LeetCode");
        problems.add(p);
        response.setRecommendedProblems(problems);

        ProblemAnalysisResponse.StudyPlan studyPlan = new ProblemAnalysisResponse.StudyPlan();
        studyPlan.setShortTerm("本周完成5道相关题目");
        studyPlan.setMidTerm("本月掌握该类型题目的解题模板");
        studyPlan.setLongTerm("建立完整的知识体系");
        studyPlan.setDailyTasks(Arrays.asList("复习今日错题", "完成1道相关练习", "总结解题思路"));
        response.setStudyPlan(studyPlan);

        return response;
    }

    private String convertToText(ProblemAnalysisResponse response) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("【AI 错题分析报告】\n\n");
        sb.append("摘要：").append(response.getSummary()).append("\n\n");
        
        if (response.getErrorAnalysis() != null) {
            sb.append("═══ 错误原因分析 ═══\n");
            sb.append("根本原因：").append(response.getErrorAnalysis().getRootCause()).append("\n");
            sb.append("详细说明：").append(response.getErrorAnalysis().getDetailedExplanation()).append("\n");
            sb.append("常见错误：\n");
            for (String mistake : response.getErrorAnalysis().getCommonMistakes()) {
                sb.append("  • ").append(mistake).append("\n");
            }
            sb.append("\n");
        }

        if (response.getKnowledgePoints() != null && !response.getKnowledgePoints().isEmpty()) {
            sb.append("═══ 相关知识点 ═══\n");
            for (ProblemAnalysisResponse.KnowledgePoint kp : response.getKnowledgePoints()) {
                sb.append("• ").append(kp.getName()).append("（").append(kp.getMasteryLevel()).append("）\n");
                sb.append("  ").append(kp.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
            sb.append("═══ 改进建议 ═══\n");
            for (ProblemAnalysisResponse.ImprovementSuggestion s : response.getSuggestions()) {
                sb.append("【").append(s.getTitle()).append("】").append("（").append(s.getPriority()).append("）\n");
                sb.append("  ").append(s.getDescription()).append("\n");
            }
            sb.append("\n");
        }

        if (response.getRecommendedProblems() != null && !response.getRecommendedProblems().isEmpty()) {
            sb.append("═══ 推荐练习 ═══\n");
            for (ProblemAnalysisResponse.RecommendedProblem p : response.getRecommendedProblems()) {
                sb.append("• ").append(p.getTitle()).append("（").append(p.getDifficulty()).append("）\n");
                sb.append("  推荐理由：").append(p.getReason()).append("\n");
            }
            sb.append("\n");
        }

        if (response.getStudyPlan() != null) {
            sb.append("═══ 学习计划 ═══\n");
            sb.append("短期目标：").append(response.getStudyPlan().getShortTerm()).append("\n");
            sb.append("中期目标：").append(response.getStudyPlan().getMidTerm()).append("\n");
            sb.append("长期目标：").append(response.getStudyPlan().getLongTerm()).append("\n");
        }

        return sb.toString();
    }
}
