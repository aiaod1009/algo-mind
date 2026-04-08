package com.example.demo.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class ProblemAnalysisRequest {
    private Long errorId;
    private String question;
    private List<String> userAnswer;
    private String description;
    private String difficulty;
    private String track;
    private Integer solveRate;
    private Integer avgTimeSeconds;
    private Integer userAttempts;
    private List<String> relatedTopics;
    private String userLevel;
}
