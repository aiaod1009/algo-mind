package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "t_ai_analysis_usage",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ai_analysis_usage_user_date_slot",
                        columnNames = {"user_id", "quota_date", "slot_code"}
                )
        },
        indexes = {
                @Index(name = "idx_ai_analysis_usage_user_date", columnList = "user_id, quota_date"),
                @Index(name = "idx_ai_analysis_usage_user_time", columnList = "user_id, created_at")
        }
)
public class AiAnalysisUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "error_id")
    private Long errorId;

    @Column(name = "quota_date", nullable = false)
    private LocalDate quotaDate;

    @Column(name = "slot_code", nullable = false, length = 16)
    private String slotCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
