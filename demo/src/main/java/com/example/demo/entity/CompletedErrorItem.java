package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_completed_error_item", indexes = {
        @Index(name = "idx_completed_error_user_completed", columnList = "user_id, completed_at"),
        @Index(name = "idx_completed_error_user_level", columnList = "user_id, level_id")
})
public class CompletedErrorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "level_id")
    private Long levelId;

    @Column(length = 200)
    private String title;

    @Column(name = "level_type", length = 50)
    private String levelType;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String question;

    @Lob
    @Column(name = "user_answer", columnDefinition = "LONGTEXT")
    private String userAnswer;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(name = "correct_answer", columnDefinition = "LONGTEXT")
    private String correctAnswer;

    @Column(name = "analysis_status", length = 50)
    private String analysisStatus;

    @Column(length = 2000)
    private String analysis;

    @Lob
    @Column(name = "analysis_data_json", columnDefinition = "LONGTEXT")
    private String analysisDataJson;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "source_error_id")
    private Long sourceErrorId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_error_at")
    private LocalDateTime lastErrorAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
}
