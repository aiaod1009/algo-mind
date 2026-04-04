package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

/**
 * 论坛帖子实体类，完全匹配开发文档的ForumPost模型
 */
@Data
@Entity
@Table(name = "t_forum_post")
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 作者名称
     */
    private String author;

    /**
     * 作者等级
     */
    @Column(name = "author_level")
    private String authorLevel;

    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 帖子标题
     */
    private String topic;

    /**
     * 帖子内容
     */
    @Column(length = 2000)
    private String content;

    /**
     * 引用内容
     */
    private String quote;

    /**
     * 帖子标签
     */
    private String tag;

    /**
     * 点赞数
     */
    private Integer likes = 0;

    /**
     * 评论数
     */
    private Integer comments = 0;

    /**
     * 是否已点赞（前端展示用，数据库可不存，临时字段）
     */
    @Transient  // 不映射到数据库
    private Boolean liked = false;

    /**
     * 创建时间（ISO 8601格式）
     */
    private OffsetDateTime createdAt = OffsetDateTime.now();
}