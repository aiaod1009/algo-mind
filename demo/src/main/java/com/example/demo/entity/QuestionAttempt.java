package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "t_question_attempt",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_question_attempt_user_level", columnNames = {"user_id", "level_id"})
        }
)
public class QuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(length = 200)
    private String questionTitle;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String questionContent;

    @Column(length = 50)
    private String levelType;

    @Column(length = 20, nullable = false)
    private String latestStatus;

    @Lob
    @Column(name = "latest_user_answer", columnDefinition = "LONGTEXT")
    private String latestUserAnswer;

    private Integer submitCount;

    private LocalDateTime firstSubmittedAt;

    private LocalDateTime lastSubmittedAt;
}
