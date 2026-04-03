package com.example.demo.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成学习计划响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPlanAIResponse {
    
    /**
     * 本周目标列表
     */
    private List<WeekGoal> weekGoals;
    
    /**
     * 每日任务安排
     */
    private List<DailyTask> dailyTasks;
    
    /**
     * 推荐资源
     */
    private List<Recommendation> recommendations;
    
    /**
     * AI分析总结
     */
    private String analysis;
    
    /**
     * 学习建议
     */
    private List<String> suggestions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeekGoal {
        private Integer id;
        private String title;
        private Integer progress;
        private Integer target;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTask {
        private String day;
        private List<Task> tasks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Task {
        private String title;
        private Integer duration;
        private String type;
        private String priority;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String type;
        private String title;
        private String source;
        private String priority;
        private String description;
    }
}
