package com.example.demo.entity;

import jakarta.persistence.*;
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
    private String bio;
    private String gender;
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

    // 社交链接
    private String github;
    private String website;
}