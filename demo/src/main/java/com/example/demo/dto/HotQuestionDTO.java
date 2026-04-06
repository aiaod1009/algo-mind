package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotQuestionDTO {
    private Long id;
    private String title;
    private String difficulty;
    private String passRate;
    private List<String> tags;
    private Integer hot;
}
