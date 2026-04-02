package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    // 后端自动生成时间，前端不用传！彻底解决格式错误
    private LocalDateTime createdAt;

    private String analysisStatus = "未分析";

    @Column(length = 2000)
    private String analysis;
}