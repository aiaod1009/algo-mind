package com.example.demo.dto.ai;

import lombok.Data;

@Data
public class ProblemAnalysisResult {
    private Long errorId;
    private String analysis;
    private ProblemAnalysisResponse analysisData;
    private String analyzedAt;
    private boolean reusedLastAnalysis;
    private boolean limitReached;
    private String message;
    private AnalysisQuotaStatus quota;
}
