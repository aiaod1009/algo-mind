package com.example.demo.dto;

import lombok.Data;

@Data
public class SubmitAnswerRequest {

    private Long levelId;

    private Object answer;

    private SubmitMeta meta;

    private String language;

    private String stdinInput;

    private String code;

    @Data
    public static class SubmitMeta {
        private Integer attempts;
        private Long timeMs;
    }
}
