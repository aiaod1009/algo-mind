package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_code_snapshot", indexes = {
    @Index(name = "idx_code_snapshot_user", columnList = "user_id"),
    @Index(name = "idx_code_snapshot_level", columnList = "level_id"),
    @Index(name = "idx_code_snapshot_user_level", columnList = "user_id, level_id"),
    @Index(name = "idx_code_snapshot_saved_at", columnList = "user_id, saved_at")
})
public class CodeSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(name = "level_name", length = 200)
    private String levelName;

    @Column(length = 30, nullable = false)
    private String language;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String code;

    @Lob
    @Column(name = "stdin_input")
    private String stdinInput;

    @Column(nullable = false)
    private Integer score = 0;

    @Column(nullable = false)
    private Integer stars = 0;

    @Column(name = "compile_passed", nullable = false)
    private Boolean compilePassed = false;

    @Column(name = "ai_eval_passed", nullable = false)
    private Boolean aiEvalPassed = false;

    @Lob
    @Column(name = "ai_analysis")
    private String aiAnalysis;

    @Column(name = "ai_correctness", length = 500)
    private String aiCorrectness;

    @Column(name = "ai_quality", length = 500)
    private String aiQuality;

    @Column(name = "ai_efficiency", length = 500)
    private String aiEfficiency;

    @Lob
    @Column(name = "ai_suggestions_json")
    private String aiSuggestionsJson;

    @Lob
    @Column(name = "run_output")
    private String runOutput;

    @Column(name = "is_best", nullable = false)
    private Boolean isBest = false;

    @Column(name = "saved_at", nullable = false)
    private OffsetDateTime savedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (savedAt == null) {
            savedAt = now;
        }
        if (score == null) {
            score = 0;
        }
        if (stars == null) {
            stars = 0;
        }
        if (compilePassed == null) {
            compilePassed = false;
        }
        if (aiEvalPassed == null) {
            aiEvalPassed = false;
        }
        if (isBest == null) {
            isBest = false;
        }
    }
}
