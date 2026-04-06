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

    @Column(columnDefinition = "TEXT")
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

    /**
     * 获取用户等级（根据积分计算）
     * Lv.1: 0-100分
     * Lv.2: 101-300分
     * Lv.3: 301-600分
     * Lv.4: 601-1000分
     * Lv.5: 1001-1500分
     * Lv.6: 1501-2100分
     * Lv.7: 2101-2800分
     * Lv.8: 2801-3600分
     * Lv.9: 3601分以上
     */
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
}