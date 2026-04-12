package com.example.demo.author;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AuthorLevelProfile {
    private String code;
    private Integer rank;
    private String title;
    private String shortLabel;
    private String theme;
    private String description;
    private Integer score;
    private Integer currentLevelMinScore;
    private Integer currentLevelMaxScore;
    private Integer nextLevelMinScore;
    private Integer progressPercent;
    private Map<String, Object> colors;
    private Map<String, Object> effects;
    private Map<String, Object> breakdown;
}
