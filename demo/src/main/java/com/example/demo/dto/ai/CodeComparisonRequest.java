package com.example.demo.dto.ai;

import lombok.Data;

@Data
public class CodeComparisonRequest {
    private String currentCode;
    private String currentLanguage;
    private Integer currentScore;
    private Long historySnapshotId;
    private Long levelId;
}
