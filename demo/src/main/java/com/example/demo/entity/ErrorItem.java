package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "t_error_item")
public class ErrorItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long levelId;

    @Column(length = 200)
    private String title;

    @Column(length = 50)
    private String levelType;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String question;

    @Lob
    @Column(name = "user_answer", columnDefinition = "LONGTEXT")
    private String userAnswer;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(name = "correct_answer", columnDefinition = "LONGTEXT")
    private String correctAnswer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String analysisStatus = "未分析";

    @Column(length = 2000)
    private String analysis;

    @Lob
    @Column(name = "analysis_data_json", columnDefinition = "LONGTEXT")
    private String analysisDataJson;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @OneToMany(mappedBy = "errorItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<ErrorItemOptionSnapshot> optionSnapshots = new ArrayList<>();

    public void replaceOptionSnapshots(List<ErrorItemOptionSnapshot> snapshots) {
        optionSnapshots.clear();
        if (snapshots == null) {
            return;
        }
        for (ErrorItemOptionSnapshot snapshot : snapshots) {
            snapshot.setErrorItem(this);
            optionSnapshots.add(snapshot);
        }
    }
}
