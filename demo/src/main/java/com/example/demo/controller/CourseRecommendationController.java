package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.ai.ChatMessage;
import com.example.demo.entity.Course;
import com.example.demo.entity.ErrorItem;
import com.example.demo.entity.LearningPlan;
import com.example.demo.entity.QuestionAttempt;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ErrorItemRepository;
import com.example.demo.repository.LearningPlanRepository;
import com.example.demo.repository.QuestionAttemptRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AIService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/course-recommendations")
public class CourseRecommendationController {

    @Resource
    private CurrentUserService currentUserService;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ErrorItemRepository errorItemRepository;

    @Resource
    private LearningPlanRepository learningPlanRepository;

    @Resource
    private QuestionAttemptRepository questionAttemptRepository;

    @Resource
    private CourseRepository courseRepository;

    @Resource
    private AIService aiService;

    @Resource
    private ObjectMapper objectMapper;

    private static final String RECOMMENDATION_PROMPT = "算法学习顾问，快速推荐课程。分析：1. 错题→薄弱点 2. 学习计划→目标方向 3. 做题记录→进度状态。推荐2门课程，输出JSON数组：[{\"title\":\"课程标题\",\"reason\":\"30-50字理由\",\"matchScore\":70-99,\"teacher\":\"老师\",\"duration\":\"课时\",\"tags\":[\"标签\"]}]。要求：1. 仅JSON，无其他文字 2. 理由具体，结合错题/计划 3. 匹配度合理 4. 仅2个推荐";

    @GetMapping
    public Result<List<CourseRecommendation>> getRecommendations() {
        Long userId = currentUserService.requireCurrentUserId();
        
        try {
            List<CourseRecommendation> recommendations = generateRecommendations(userId);
            return Result.success(recommendations);
        } catch (Exception e) {
            log.error("生成课程推荐失败: {}", e.getMessage(), e);
            List<CourseRecommendation> fallbackRecommendations = generateFallbackRecommendations(userId);
            return Result.success(fallbackRecommendations);
        }
    }

    private List<CourseRecommendation> generateRecommendations(Long userId) {
        UserDataContext context = collectUserData(userId);
        
        String aiPrompt = buildAIPrompt(context);
        
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(RECOMMENDATION_PROMPT));
        messages.add(ChatMessage.user(aiPrompt));
        
        String responseContent = aiService.chat(messages);
        
        return parseRecommendations(responseContent);
    }

    private UserDataContext collectUserData(Long userId) {
        UserDataContext context = new UserDataContext();
        
        Optional<User> userOpt = userRepository.findById(userId);
        userOpt.ifPresent(user -> {
            context.setUserName(user.getName());
            context.setPoints(user.getPoints());
            context.setLevel(user.getLevel());
            context.setTargetTrack(user.getTargetTrack());
            context.setWeeklyGoal(user.getWeeklyGoal());
        });
        
        List<ErrorItem> errors = errorItemRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        context.setErrorCount(errors.size());
        
        Map<String, Long> errorTypeCount = errors.stream()
                .filter(e -> e.getLevelType() != null)
                .collect(Collectors.groupingBy(ErrorItem::getLevelType, Collectors.counting()));
        context.setErrorTypeDistribution(errorTypeCount);
        
        List<String> recentErrorTitles = errors.stream()
                .limit(5)
                .map(ErrorItem::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        context.setRecentErrorTitles(recentErrorTitles);
        
        List<LearningPlan> plans = learningPlanRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (!plans.isEmpty()) {
            LearningPlan latestPlan = plans.get(0);
            context.setPlanTrack(latestPlan.getTrack());
            context.setPlanTrackLabel(latestPlan.getTrackLabel());
            context.setPlanWeeklyGoal(latestPlan.getWeeklyGoal());
        }
        
        return context;
    }

    private String buildAIPrompt(UserDataContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下学生生成课程推荐：\n\n");
        
        prompt.append("【学生基本信息】\n");
        prompt.append("- 用户名：").append(context.getUserName() != null ? context.getUserName() : "未知").append("\n");
        prompt.append("- 等级：").append(context.getLevel() != null ? context.getLevel() : "Lv.1").append("\n");
        prompt.append("- 积分：").append(context.getPoints() != null ? context.getPoints() : 0).append("\n");
        
        if (context.getTargetTrack() != null) {
            prompt.append("- 目标赛道：").append(context.getTargetTrack()).append("\n");
        }
        if (context.getWeeklyGoal() != null) {
            prompt.append("- 周目标：完成 ").append(context.getWeeklyGoal()).append(" 个关卡\n");
        }
        
        prompt.append("\n【错题分析】\n");
        prompt.append("- 累计错题数：").append(context.getErrorCount()).append("\n");
        
        if (!context.getErrorTypeDistribution().isEmpty()) {
            prompt.append("- 错题类型分布：\n");
            context.getErrorTypeDistribution().forEach((type, count) -> {
                prompt.append("  * ").append(type).append(": ").append(count).append(" 道\n");
            });
        }
        
        if (!context.getRecentErrorTitles().isEmpty()) {
            prompt.append("- 最近错题：").append(String.join("、", context.getRecentErrorTitles())).append("\n");
        }
        
        if (context.getPlanTrack() != null) {
            prompt.append("\n【学习计划】\n");
            prompt.append("- 当前赛道：").append(context.getPlanTrackLabel() != null ? context.getPlanTrackLabel() : context.getPlanTrack()).append("\n");
            if (context.getPlanWeeklyGoal() != null) {
                prompt.append("- 计划周目标：").append(context.getPlanWeeklyGoal()).append(" 个关卡\n");
            }
        }
        
        prompt.append("\n请根据以上数据，推荐2门最适合该学生的课程。");
        
        return prompt.toString();
    }

    private List<CourseRecommendation> parseRecommendations(String content) {
        try {
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            } else if (content.contains("```")) {
                jsonContent = content.replaceAll("(?s)```\\s*", "").replaceAll("(?s)\\s*```", "").trim();
            }
            
            if (jsonContent.startsWith("[") && jsonContent.endsWith("]")) {
                return objectMapper.readValue(jsonContent, new TypeReference<List<CourseRecommendation>>() {});
            }
            
            return generateFallbackRecommendations(null);
        } catch (Exception e) {
            log.error("解析AI推荐结果失败: {}", e.getMessage());
            return generateFallbackRecommendations(null);
        }
    }

    private List<CourseRecommendation> generateFallbackRecommendations(Long userId) {
        List<CourseRecommendation> recommendations = new ArrayList<>();
        
        CourseRecommendation rec1 = new CourseRecommendation();
        rec1.setId(101L);
        rec1.setTitle("动态规划强化训练");
        rec1.setReason("根据您的学习数据，建议系统学习动态规划，提升算法能力");
        rec1.setMatchScore(85);
        TeacherInfo teacher1 = new TeacherInfo();
        teacher1.setName("张老师");
        teacher1.setTitle("DP专家");
        rec1.setTeacher(teacher1);
        rec1.setDuration("12课时");
        rec1.setTags(Arrays.asList("DP优化", "状态压缩"));
        recommendations.add(rec1);
        
        CourseRecommendation rec2 = new CourseRecommendation();
        rec2.setId(102L);
        rec2.setTitle("图论算法精讲");
        rec2.setReason("您的学习计划中包含图论内容，此课程与您的学习目标匹配");
        rec2.setMatchScore(80);
        TeacherInfo teacher2 = new TeacherInfo();
        teacher2.setName("刘老师");
        teacher2.setTitle("图论专家");
        rec2.setTeacher(teacher2);
        rec2.setDuration("15课时");
        rec2.setTags(Arrays.asList("BFS", "DFS", "最短路"));
        recommendations.add(rec2);
        
        return recommendations;
    }

    @Data
    public static class UserDataContext {
        private String userName;
        private Integer points;
        private String level;
        private String targetTrack;
        private Integer weeklyGoal;
        private Integer errorCount;
        private Map<String, Long> errorTypeDistribution = new HashMap<>();
        private List<String> recentErrorTitles = new ArrayList<>();
        private String planTrack;
        private String planTrackLabel;
        private Integer planWeeklyGoal;
    }

    @Data
    public static class CourseRecommendation {
        private Long id;
        private String title;
        private String reason;
        private Integer matchScore;
        private TeacherInfo teacher;
        private String duration;
        private List<String> tags;
    }

    @Data
    public static class TeacherInfo {
        private String name;
        private String title;
    }
}
