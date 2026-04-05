package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_forum_comment")
public class ForumComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id")
    private Long userId;

    private String author;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    @Column(name = "author_level")
    private String authorLevel;

    @Column(length = 2000)
    private String content;

    private Integer likes = 0;

    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 用户公司/学校信息（如：阿里巴巴集团 服务端研发）
     */
    private String company;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (likes == null) {
            likes = 0;
        }
    }
}
