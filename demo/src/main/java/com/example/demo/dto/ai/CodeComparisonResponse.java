package com.example.demo.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class CodeComparisonResponse {
    private Integer currentScore;
    private Integer historyScore;
    private CodeSnapshotInfo historySnapshot;
    private List<String> diffAnalysis;
    private List<ImprovementItem> improvements;
    private List<String> suggestions;
    private String summary;

    @Data
    public static class CodeSnapshotInfo {
        private Long id;
        private String language;
        private Integer score;
        private Integer stars;
        private String savedAt;
        private String codePreview;
    }

    @Data
    public static class ImprovementItem {
        private String type;
        private String description;
    }
}
