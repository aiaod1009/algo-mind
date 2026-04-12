package com.example.demo.entity;

import com.example.demo.author.AuthorLevelProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_forum_post")
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String author;

    @Column(name = "author_level")
    private String authorLevel;

    @Column(columnDefinition = "TEXT")
    private String avatar;

    private String topic;

    @Column(length = 2000)
    private String content;

    private String quote;

    private String tag;

    private Integer likes = 0;

    private Integer comments = 0;

    @Transient
    private Boolean liked = false;

    @Transient
    private AuthorLevelProfile authorLevelProfile;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    private String location;
}
