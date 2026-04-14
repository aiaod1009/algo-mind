package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.dto.ai.CodeComparisonRequest;
import com.example.demo.dto.ai.CodeComparisonResponse;
import com.example.demo.entity.CodeSnapshot;
import com.example.demo.repository.CodeSnapshotRepository;
import com.example.demo.service.AIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
public class CodeComparisonController {

    private static final String COMPARISON_PROMPT = """
            你是一位经验丰富的代码审查专家。
            请对比分析两段代码，并返回结构化的 JSON 报告。
            所有内容必须使用中文返回。
            
            请严格按照以下 JSON 格式返回：
            
            {
              "summary": "对比总结（一句话概括两段代码的主要差异）",
              "diffAnalysis": [
                "差异点1：具体描述",
                "差异点2：具体描述",
                "差异点3：具体描述"
              ],
              "improvements": [
                {
                  "type": "current",
                  "description": "当前代码的优势点"
                },
                {
                  "type": "history",
                  "description": "历史代码的优势点"
                }
              ],
              "suggestions": [
                "优化建议1",
                "优化建议2"
              ]
            }
            
            分析维度：
            1. 算法复杂度：时间复杂度、空间复杂度的差异
            2. 代码质量：可读性、命名规范、注释完整性
            3. 边界处理：边界条件检查、异常处理
            4. 代码风格：代码结构、设计模式使用
            
            请确保返回的是有效的 JSON 格式，不要包含 markdown 代码块标记。
            """;

    @Resource
    private CurrentUserService currentUserService;

    @Resource
    private CodeSnapshotRepository codeSnapshotRepository;

    @Resource
    private AIService aiService;

    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("/code-comparison")
    public Result<CodeComparisonResponse> compareCode(@RequestBody CodeComparisonRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        
        Optional<CodeSnapshot> optionalSnapshot = codeSnapshotRepository
            .findById(request.getHistorySnapshotId())
            .filter(snapshot -> Objects.equals(snapshot.getUserId(), userId));
        
        if (optionalSnapshot.isEmpty()) {
            return Result.fail(40401, "历史代码不存在");
        }
        
        CodeSnapshot historySnapshot = optionalSnapshot.get();
        
        CodeComparisonResponse response;
        try {
            response = callAIForComparison(request, historySnapshot);
        } catch (Exception e) {
            log.error("AI 代码对比失败", e);
            response = generateLocalComparison(request, historySnapshot);
        }
        
        response.setHistorySnapshot(buildSnapshotInfo(historySnapshot));
        response.setHistoryScore(historySnapshot.getScore());
        
        return Result.success(response);
    }

    private CodeComparisonResponse callAIForComparison(
            CodeComparisonRequest request, 
            CodeSnapshot historySnapshot
    ) {
        String prompt = buildComparisonPrompt(request, historySnapshot);
        String aiResponse = aiService.chat(List.of(
            ChatMessage.system(COMPARISON_PROMPT),
            ChatMessage.user(prompt)
        ));
        
        return parseComparisonResponse(aiResponse);
    }

    private String buildComparisonPrompt(
            CodeComparisonRequest request, 
            CodeSnapshot historySnapshot
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请对比分析以下两段代码：\n\n");
        
        prompt.append("【当前代码】\n");
        prompt.append("语言: ").append(request.getCurrentLanguage()).append("\n");
        prompt.append("代码:\n```").append(request.getCurrentLanguage()).append("\n");
        prompt.append(request.getCurrentCode()).append("\n```\n\n");
        
        prompt.append("【历史代码】\n");
        prompt.append("语言: ").append(historySnapshot.getLanguage()).append("\n");
        prompt.append("评分: ").append(historySnapshot.getScore()).append("分\n");
        prompt.append("代码:\n```").append(historySnapshot.getLanguage()).append("\n");
        prompt.append(historySnapshot.getCode()).append("\n```\n\n");
        
        prompt.append("请从算法复杂度、代码质量、边界处理、代码风格等维度进行对比分析。");
        
        return prompt.toString();
    }

    private CodeComparisonResponse parseComparisonResponse(String content) {
        try {
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)\\s*```", "").trim();
            } else if (content.contains("```")) {
                jsonContent = content.replaceAll("(?s)```\\s*", "")
                    .replaceAll("(?s)\\s*```", "").trim();
            }
            return objectMapper.readValue(jsonContent, CodeComparisonResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI comparison response", e);
        }
    }

    private CodeComparisonResponse generateLocalComparison(
            CodeComparisonRequest request, 
            CodeSnapshot historySnapshot
    ) {
        CodeComparisonResponse response = new CodeComparisonResponse();
        response.setSummary("AI 分析暂时不可用，以下是基础对比结果。");
        
        int currentLines = request.getCurrentCode() != null ? request.getCurrentCode().split("\n").length : 0;
        int historyLines = historySnapshot.getCode() != null ? historySnapshot.getCode().split("\n").length : 0;
        
        response.setDiffAnalysis(List.of(
            "当前代码行数: " + currentLines,
            "历史代码行数: " + historyLines,
            "行数差异: " + (currentLines - historyLines)
        ));
        response.setImprovements(List.of());
        response.setSuggestions(List.of("建议稍后重试 AI 分析功能"));
        return response;
    }

    private CodeComparisonResponse.CodeSnapshotInfo buildSnapshotInfo(CodeSnapshot snapshot) {
        CodeComparisonResponse.CodeSnapshotInfo info = new CodeComparisonResponse.CodeSnapshotInfo();
        info.setId(snapshot.getId());
        info.setLanguage(snapshot.getLanguage());
        info.setScore(snapshot.getScore());
        info.setStars(snapshot.getStars());
        info.setSavedAt(snapshot.getSavedAt() != null ? snapshot.getSavedAt().toString() : null);
        
        String code = snapshot.getCode() != null ? snapshot.getCode() : "";
        info.setCodePreview(code.length() > 100 ? code.substring(0, 100) + "..." : code);
        
        return info;
    }
}
