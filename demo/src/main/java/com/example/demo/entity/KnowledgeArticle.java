package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "t_knowledge_article")
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 160)
    private String title;

    private String englishTitle;

    @Column(nullable = false, length = 120)
    private String sectionId;

    @Column(nullable = false, length = 120)
    private String sectionTitle;

    @Column(columnDefinition = "TEXT")
    private String sectionDescription;

    private String badge;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String lead;

    private String complexity;

    private String readTime;

    @Column(columnDefinition = "TEXT")
    private String tagsText;

    @Column(columnDefinition = "LONGTEXT")
    private String learningObjectivesJson;

    @Column(columnDefinition = "LONGTEXT")
    private String strategyStepsJson;

    @Column(columnDefinition = "LONGTEXT")
    private String insightsJson;

    @Column(columnDefinition = "LONGTEXT")
    private String codeBlocksJson;

    @Column(columnDefinition = "LONGTEXT")
    private String checklistJson;

    @Column(columnDefinition = "TEXT")
    private String relatedSlugsText;

    private Boolean spotlightEnabled;

    private String spotlightEyebrow;

    private String spotlightTitle;

    @Column(columnDefinition = "TEXT")
    private String spotlightDescription;

    private String spotlightAccent;

    private Boolean published;

    private Integer sortOrder;

    private Long createdByUserId;

    private Long updatedByUserId;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (published == null) {
            published = true;
        }
        if (spotlightEnabled == null) {
            spotlightEnabled = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
