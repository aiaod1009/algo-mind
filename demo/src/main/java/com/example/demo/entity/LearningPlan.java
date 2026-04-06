package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_learning_plan")
public class LearningPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String track;
    private String trackLabel;
    private Integer weeklyGoal;

    @Column(name = "week_goals_json", columnDefinition = "TEXT")
    private String weekGoalsJson;

    @Column(name = "daily_tasks_json", columnDefinition = "TEXT")
    private String dailyTasksJson;

    @Column(name = "recommendations_json", columnDefinition = "TEXT")
    private String recommendationsJson;

    private OffsetDateTime generatedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    /**
     * 是否由AI生成
     */
    @Column(name = "is_ai_generated")
    private boolean aiGenerated = false;
    
    /**
     * AI分析内容
     */
    @Column(name = "ai_analysis", length = 4000)
    private String aiAnalysis;
}
