package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_course_comment")
public class CourseComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;

    private Long userId;

    private String userName;

    private String userAvatar;

    @Column(length = 1000)
    private String content;

    private Integer likes = 0;

    private Long parentId;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (likes == null) {
            likes = 0;
        }
    }
}
