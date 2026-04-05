package com.example.demo.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class ProblemAnalysisResponse {
    private String summary;
    private ErrorAnalysis errorAnalysis;
    private List<KnowledgePoint> knowledgePoints;
    private List<ImprovementSuggestion> suggestions;
    private List<RecommendedProblem> recommendedProblems;
    private StudyPlan studyPlan;

    @Data
    public static class ErrorAnalysis {
        private String rootCause;
        private String detailedExplanation;
        private List<String> commonMistakes;
    }

    @Data
    public static class KnowledgePoint {
        private String name;
        private String description;
        private String masteryLevel;
        private List<String> relatedResources;
    }

    @Data
    public static class ImprovementSuggestion {
        private String title;
        private String description;
        private String priority;
        private List<String> actionItems;
    }

    @Data
    public static class RecommendedProblem {
        private String title;
        private String difficulty;
        private String reason;
        private String source;
    }

    @Data
    public static class StudyPlan {
        private String shortTerm;
        private String midTerm;
        private String longTerm;
        private List<String> dailyTasks;
    }
}
