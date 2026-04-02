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
    private String weekGoalsJson;
    private String dailyTasksJson;
    private String recommendationsJson;
    private OffsetDateTime generatedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
