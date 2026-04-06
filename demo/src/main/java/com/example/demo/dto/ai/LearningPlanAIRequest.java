package com.example.demo.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成学习计划请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPlanAIRequest {
    
    /**
     * 目标赛道
     */
    private String track;
    
    /**
     * 赛道标签
     */
    private String trackLabel;
    
    /**
     * 每周目标题数
     */
    private Integer weeklyGoal;
    
    /**
     * 本周已完成题数
     */
    private Integer weeklyCompleted;
    
    /**
     * 历史错题知识点标签
     */
    private List<String> errorTopics;
    
    /**
     * 擅长领域
     */
    private List<String> strongAreas;
    
    /**
     * 薄弱环节
     */
    private List<String> weakAreas;
    
    /**
     * 坚持指数 (0-100)
     */
    private Integer persistenceIndex;
    
    /**
     * 累计解题数
     */
    private Integer totalSolved;
    
    /**
     * 当前连续打卡天数
     */
    private Integer currentStreak;
    
    /**
     * 用户偏好/备注
     */
    private String preferences;
}
