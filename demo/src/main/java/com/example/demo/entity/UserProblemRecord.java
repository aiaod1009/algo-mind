package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_user_problem_record", indexes = {
    @Index(name = "idx_user_solved_at", columnList = "user_id, solved_at"),
    @Index(name = "idx_user_level", columnList = "user_id, level_id")
})
public class UserProblemRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "level_id", nullable = false)
  private Long levelId;

  @Column(name = "level_name", length = 200)
  private String levelName;

  @Column(length = 20)
  private String track;

  @Column(name = "is_correct", nullable = false)
  private Boolean isCorrect;

  @Column(length = 20)
  private String status;

  @Column(name = "stars", nullable = false)
  private Integer stars;

  @Column(name = "attempt_no", nullable = false)
  private Integer attemptNo;

  @Column(name = "solve_time_ms")
  private Integer solveTimeMs;

  @Column(name = "solved_at", nullable = false)
  private OffsetDateTime solvedAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  public void prePersist() {
    OffsetDateTime now = OffsetDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (solvedAt == null) {
      solvedAt = now;
    }
    if (stars == null) {
      stars = 0;
    }
    if (attemptNo == null) {
      attemptNo = 1;
    }
    if (isCorrect == null) {
      isCorrect = false;
    }
  }
}
