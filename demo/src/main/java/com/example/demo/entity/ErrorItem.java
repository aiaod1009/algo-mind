package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_error_item")
public class ErrorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long levelId;

    @Column(length = 1000, nullable = false)
    private String question;

    @Column(length = 500)
    private String userAnswer;

    @Column(length = 500)
    private String description;

    private Long createdAt;

    private String analysisStatus = "未分析";

    @Column(length = 2000)
    private String analysis;
}