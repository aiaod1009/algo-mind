package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.ErrorItem;
import com.example.demo.repository.ErrorItemRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class ErrorController {

    @Resource
    private ErrorItemRepository errorRepository;

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
    public Result<Map<String, Object>> analyzeError(@RequestBody Map<String, Object> request) {
        Long errorId = null;
        String description = "";
        
        if (request.containsKey("errorId")) {
            try {
                errorId = Long.valueOf(request.get("errorId").toString());
            } catch (Exception e) {
                // ignore
            }
        }
        if (request.containsKey("description")) {
            description = (String) request.get("description");
        }

        String analysis = generateAIAnalysis(description);

        if (errorId != null) {
            Optional<ErrorItem> optionalError = errorRepository.findById(errorId);
            if (optionalError.isPresent()) {
                ErrorItem errorItem = optionalError.get();
                errorItem.setAnalysisStatus("已分析");
                errorItem.setAnalysis(analysis);
                errorRepository.save(errorItem);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("errorId", errorId);
        result.put("analysis", analysis);
        result.put("analyzedAt", LocalDateTime.now().toString());

        return Result.success(result);
    }

    private String generateAIAnalysis(String description) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("【AI 错题分析报告】\n\n");
        analysis.append("一、错误原因分析\n");
        analysis.append("根据题目描述分析，可能的错误原因包括：\n");
        analysis.append("1. 对题目条件理解不完整，可能遗漏了边界情况\n");
        analysis.append("2. 算法复杂度估算不准确，导致时间/空间超限\n");
        analysis.append("3. 代码实现细节有误，如索引越界、类型转换错误等\n\n");
        
        analysis.append("二、知识点关联\n");
        analysis.append("本题涉及的核心知识点：\n");
        analysis.append("- 数据结构选择与优化\n");
        analysis.append("- 算法复杂度分析\n");
        analysis.append("- 边界条件处理\n\n");
        
        analysis.append("三、改进建议\n");
        analysis.append("1. 建议先写出已知条件和约束，用样例逐步演算\n");
        analysis.append("2. 总结可复用的解题模板，建立知识体系\n");
        analysis.append("3. 针对相似题型进行专项练习，巩固薄弱环节\n\n");
        
        analysis.append("四、推荐练习\n");
        analysis.append("- 相关基础题：建议从简单难度开始巩固\n");
        analysis.append("- 进阶挑战：尝试中等难度的变体题目\n");
        analysis.append("- 专题训练：参加相关专题的训练营\n\n");
        
        if (description != null && !description.isEmpty()) {
            analysis.append("【题目描述参考】\n");
            analysis.append(description.length() > 200 ? description.substring(0, 200) + "..." : description);
        }

        return analysis.toString();
    }
}
