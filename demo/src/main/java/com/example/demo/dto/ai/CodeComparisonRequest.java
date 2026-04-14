package com.example.demo.dto.ai;

import lombok.Data;

@Data
public class CodeComparisonRequest {
    private String currentCode;
    private String currentLanguage;
    private Long historySnapshotId;
    private Long levelId;
}
