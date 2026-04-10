package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * 关卡实体类，完全匹配开发文档的Level模型
 */
@Data
@Entity
@Table(name = "t_level")
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 赛道：algo|ds|contest
     */
    private String track;

    /**
     * 关卡排序（order是SQL关键字，改名sort_order）
     */
    @Column(name = "sort_order")
    private Integer order;

    /**
     * 关卡名称
     */
    private String name;

    /**
     * 是否解锁
     */
    private Boolean isUnlocked;

    /**
     * 通关奖励积分
     */
    private Integer rewardPoints;

    /**
     * 题目类型：single|multi|judge|fill|code
     */
    private String type;

    /**
     * 题目题干
     */
    @Column(length = 1000)
    private String question;

    /**
     * 选项（单选/多选题用）
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_level_options", joinColumns = @JoinColumn(name = "level_id"))
    @Column(name = "options")
    private List<String> options;

    /**
     * 正确答案
     */
    private String answer;

    /**
     * 关卡描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 创建者用户ID（用于“我的题目”）
     */
    @Column(name = "creator_id")
    private Long creatorId;
}