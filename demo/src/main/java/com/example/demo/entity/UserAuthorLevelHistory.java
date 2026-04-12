package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_user_author_level_history", indexes = {
        @Index(name = "idx_author_history_user_created", columnList = "user_id, created_at")
})
public class UserAuthorLevelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "trigger_type", length = 50, nullable = false)
    private String triggerType;

    @Column(name = "trigger_ref_id")
    private Long triggerRefId;

    @Column(name = "previous_level_code", length = 32)
    private String previousLevelCode;

    @Column(name = "current_level_code", length = 32, nullable = false)
    private String currentLevelCode;

    @Column(name = "previous_score", nullable = false)
    private Integer previousScore;

    @Column(name = "current_score", nullable = false)
    private Integer currentScore;

    @Column(length = 255)
    private String note;

    @Column(name = "metrics_json", columnDefinition = "TEXT")
    private String metricsJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (previousScore == null) {
            previousScore = 0;
        }
        if (currentScore == null) {
            currentScore = 0;
        }
    }
}
