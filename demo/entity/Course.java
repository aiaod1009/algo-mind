package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * 课程实体类，完全匹配开发文档的Course模型
 */
@Data
@Entity
@Table(name = "t_course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 课程标题
     */
    private String title;

    /**
     * 授课老师
     */
    private String teacher;

    /**
     * 赛道：algo|ds|contest
     */
    private String track;

    /**
     * 课时时长
     */
    private String duration;

    /**
     * 学习进度（百分比）
     */
    private Integer progress;

    /**
     * 课程标签
     */
    @ElementCollection
    private List<String> tags;
}