package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_error_item")
public class ErrorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long levelId;

    @Column(length = 1000)
    private String question;

    @Column(length = 500)
    private String userAnswer;

    @Column(length = 500)
    private String description;

    // 🚀 核心：只返回、不接收！前端不传完全没问题
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String analysisStatus = "未分析";

    @Column(length = 2000)
    private String analysis;

    // 后端自动生成时间（前端不用管）
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}