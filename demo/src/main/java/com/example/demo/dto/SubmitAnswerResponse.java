package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitAnswerResponse {

    private boolean correct;

    private int pointsEarned;

    private boolean nextLevelUnlocked;

    private int starsEarned;

    private int bestStars;

    private String latestStatus;

    private Long errorItemId;
}
