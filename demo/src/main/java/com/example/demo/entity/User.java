package com.example.demo.entity;

import com.example.demo.author.AuthorLevelProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private Integer points;
    private Integer authorScore;
    private String authorLevelCode;
    private String bio;
    private String gender;

    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String avatar;

    private String targetTrack;
    private Integer weeklyGoal;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private String statusEmoji;
    private String statusMood;
    private Boolean isBusy;
    private String busyAutoReply;
    private OffsetDateTime busyEndTime;
    private OffsetDateTime authorLevelUpdatedAt;

    private String github;
    private String website;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Transient
    private AuthorLevelProfile authorLevelProfile;

    public String getLevel() {
        int p = points != null ? points : 0;
        if (p >= 3601) return "Lv.9";
        if (p >= 2801) return "Lv.8";
        if (p >= 2101) return "Lv.7";
        if (p >= 1501) return "Lv.6";
        if (p >= 1001) return "Lv.5";
        if (p >= 601) return "Lv.4";
        if (p >= 301) return "Lv.3";
        if (p >= 101) return "Lv.2";
        return "Lv.1";
    }

    @PrePersist
    public void prePersist() {
        if (points == null) {
            points = 0;
        }
        if (authorScore == null) {
            authorScore = 0;
        }
        if (authorLevelCode == null || authorLevelCode.isBlank()) {
            authorLevelCode = "seed";
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
        if (authorLevelUpdatedAt == null) {
            authorLevelUpdatedAt = OffsetDateTime.now();
        }
        if (isAdmin == null) {
            isAdmin = false;
        }
    }
}
