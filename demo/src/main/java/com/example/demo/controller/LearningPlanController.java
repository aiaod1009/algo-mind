package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.dto.ai.LearningPlanAIRequest;
import com.example.demo.dto.ai.LearningPlanAIResponse;
import com.example.demo.entity.LearningPlan;
import com.example.demo.repository.LearningPlanRepository;
import com.example.demo.service.AIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 学习计划接口，支持基于AI的个性化计划生成
 */
@Slf4j
@RestController
@RequestMapping("/learning-plans")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanRepository learningPlanRepository;
    private final ObjectMapper objectMapper;
    private final AIService aiService;

    @GetMapping
    public Result<List<Map<String, Object>>> getUserLearningPlans() {
        Long currentUserId = 1L;
        List<LearningPlan> plans = learningPlanRepository.findByUserIdOrderByCreatedAtDesc(currentUserId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (LearningPlan plan : plans) {
            result.add(convertToMap(plan));
        }
        return Result.success(result);
    }

    /**
     * 获取当前学习计划（包含用户学习进度和画像数据）
     */
    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentPlan(
            @RequestParam(defaultValue = "algo") String track) {
        Long currentUserId = 1L;

        Optional<LearningPlan> planOpt = learningPlanRepository
                .findFirstByUserIdAndTrackOrderByCreatedAtDesc(currentUserId, track);

        Map<String, Object> planData;
        if (planOpt.isPresent()) {
            planData = convertToMap(planOpt.get());
        } else {
            // 使用本地算法生成默认计划（降级策略）
            planData = generateDefaultPlan(track);
        }

        // 补充用户学习进度数据
        Map<String, Object> enrichedData = enrichWithUserProgress(planData, currentUserId, track);

        return Result.success(enrichedData);
    }

    /**
     * 生成个性化学习计划（AI驱动）
     * 优先调用AI服务生成计划，失败时降级到本地算法
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateLearningPlan(@RequestBody Map<String, Object> request) {
        Long currentUserId = 1L;
        
        // 基础参数
        String track = (String) request.getOrDefault("track", "algo");
        String trackLabel = (String) request.getOrDefault("trackLabel", "算法思维赛道");
        Integer weeklyGoal = getIntValue(request, "weeklyGoal", 10);
        
        // 用户画像参数
        Integer weeklyCompleted = getIntValue(request, "weeklyCompleted", 0);
        Integer totalSolved = getIntValue(request, "totalSolved", 0);
        Integer currentStreak = getIntValue(request, "currentStreak", 0);
        Integer persistenceIndex = getIntValue(request, "persistenceIndex", 50);
        String preferences = (String) request.get("preferences");
        
        // 知识点标签
        @SuppressWarnings("unchecked")
        List<String> errorTopics = (List<String>) request.getOrDefault("errorTopics", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> strongAreas = (List<String>) request.getOrDefault("strongAreas", new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<String> weakAreas = (List<String>) request.getOrDefault("weakAreas", new ArrayList<>());

        Map<String, Object> planData;
        boolean isAIGenerated = false;
        String aiAnalysis = null;
        List<String> aiSuggestions = null;

        try {
            // 尝试使用AI生成学习计划
            log.info("尝试使用AI生成学习计划，用户：{}，赛道：{}", currentUserId, track);
            
            LearningPlanAIRequest aiRequest = LearningPlanAIRequest.builder()
                    .track(track)
                    .trackLabel(trackLabel)
                    .weeklyGoal(weeklyGoal)
                    .weeklyCompleted(weeklyCompleted)
                    .totalSolved(totalSolved)
                    .currentStreak(currentStreak)
                    .persistenceIndex(persistenceIndex)
                    .errorTopics(errorTopics)
                    .strongAreas(strongAreas)
                    .weakAreas(weakAreas)
                    .preferences(preferences)
                    .build();
            
            LearningPlanAIResponse aiResponse = aiService.generateLearningPlan(aiRequest);
            
            // 转换AI响应为计划数据
            planData = convertAIResponseToPlanData(aiResponse);
            isAIGenerated = true;
            aiAnalysis = aiResponse.getAnalysis();
            aiSuggestions = aiResponse.getSuggestions();
            
            log.info("AI学习计划生成成功");
            
        } catch (Exception e) {
            // AI调用失败，降级到本地算法
            log.warn("AI生成计划失败，降级到本地算法：{}", e.getMessage());
            
            UserProfile profile = new UserProfile(
                track, weeklyGoal, weeklyCompleted, 
                errorTopics, strongAreas, weakAreas, 
                persistenceIndex, totalSolved, currentStreak
            );
            planData = generatePersonalizedPlan(profile);
        }

        // 保存学习计划
        LearningPlan plan = new LearningPlan();
        plan.setUserId(currentUserId);
        plan.setTrack(track);
        plan.setTrackLabel(trackLabel);
        plan.setWeeklyGoal(weeklyGoal);
        plan.setGeneratedAt(OffsetDateTime.now());
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setUpdatedAt(OffsetDateTime.now());
        plan.setAiGenerated(isAIGenerated);

        try {
            plan.setWeekGoalsJson(objectMapper.writeValueAsString(planData.get("weekGoals")));
            plan.setDailyTasksJson(objectMapper.writeValueAsString(planData.get("dailyTasks")));
            plan.setRecommendationsJson(objectMapper.writeValueAsString(planData.get("recommendations")));
            if (aiAnalysis != null) {
                plan.setAiAnalysis(aiAnalysis);
            }
        } catch (JsonProcessingException e) {
            return Result.fail(50001, "计划生成失败: " + e.getMessage());
        }

        LearningPlan saved = learningPlanRepository.save(plan);
        Map<String, Object> result = convertToMap(saved);
        result.put("isNewGenerated", true);
        result.put("isAIGenerated", isAIGenerated);
        
        // 添加用户画像分析结果
        UserProfile profile = new UserProfile(
            track, weeklyGoal, weeklyCompleted, 
            errorTopics, strongAreas, weakAreas, 
            persistenceIndex, totalSolved, currentStreak
        );
        result.put("profileAnalysis", generateProfileAnalysis(profile));
        
        // 如果是AI生成的，添加AI分析
        if (isAIGenerated && aiSuggestions != null) {
            result.put("aiSuggestions", aiSuggestions);
        }

        return Result.success(result);
    }

    @PostMapping
    public Result<Map<String, Object>> saveLearningPlan(@RequestBody Map<String, Object> request) {
        Long currentUserId = 1L;

        LearningPlan plan = new LearningPlan();
        plan.setUserId(currentUserId);
        plan.setTrack((String) request.get("track"));
        plan.setTrackLabel((String) request.get("trackLabel"));
        plan.setWeeklyGoal((Integer) request.get("weeklyGoal"));
        plan.setGeneratedAt(OffsetDateTime.now());
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setUpdatedAt(OffsetDateTime.now());
        plan.setAiGenerated(false);

        try {
            plan.setWeekGoalsJson(objectMapper.writeValueAsString(request.get("weekGoals")));
            plan.setDailyTasksJson(objectMapper.writeValueAsString(request.get("dailyTasks")));
            plan.setRecommendationsJson(objectMapper.writeValueAsString(request.get("recommendations")));
        } catch (JsonProcessingException e) {
            return Result.fail(50001, "保存失败: " + e.getMessage());
        }

        LearningPlan saved = learningPlanRepository.save(plan);
        return Result.success(convertToMap(saved));
    }

    @DeleteMapping("/{id}")
    public Result<Map<String, Object>> deleteLearningPlan(@PathVariable Long id) {
        Long currentUserId = 1L;

        Optional<LearningPlan> planOpt = learningPlanRepository.findById(id);
        if (planOpt.isEmpty()) {
            return Result.fail(40401, "学习计划不存在");
        }

        LearningPlan plan = planOpt.get();
        if (!plan.getUserId().equals(currentUserId)) {
            return Result.fail(40301, "无权删除此计划");
        }

        learningPlanRepository.deleteById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("deleted", true);
        return Result.success(result);
    }

    /**
     * 将AI响应转换为计划数据格式
     */
    private Map<String, Object> convertAIResponseToPlanData(LearningPlanAIResponse aiResponse) {
        Map<String, Object> planData = new HashMap<>();
        
        // 转换周目标
        List<Map<String, Object>> weekGoals = new ArrayList<>();
        if (aiResponse.getWeekGoals() != null) {
            for (LearningPlanAIResponse.WeekGoal goal : aiResponse.getWeekGoals()) {
                Map<String, Object> goalMap = new HashMap<>();
                goalMap.put("id", goal.getId());
                goalMap.put("title", goal.getTitle());
                goalMap.put("progress", goal.getProgress());
                goalMap.put("target", goal.getTarget());
                if (goal.getDescription() != null) {
                    goalMap.put("description", goal.getDescription());
                }
                weekGoals.add(goalMap);
            }
        }
        planData.put("weekGoals", weekGoals);
        
        // 转换每日任务
        List<Map<String, Object>> dailyTasks = new ArrayList<>();
        if (aiResponse.getDailyTasks() != null) {
            for (LearningPlanAIResponse.DailyTask dayTask : aiResponse.getDailyTasks()) {
                Map<String, Object> dayMap = new HashMap<>();
                dayMap.put("day", dayTask.getDay());
                
                List<Map<String, Object>> tasks = new ArrayList<>();
                if (dayTask.getTasks() != null) {
                    for (LearningPlanAIResponse.Task task : dayTask.getTasks()) {
                        Map<String, Object> taskMap = new HashMap<>();
                        taskMap.put("title", task.getTitle());
                        taskMap.put("duration", task.getDuration());
                        taskMap.put("type", task.getType());
                        taskMap.put("priority", task.getPriority());
                        if (task.getDescription() != null) {
                            taskMap.put("description", task.getDescription());
                        }
                        tasks.add(taskMap);
                    }
                }
                dayMap.put("tasks", tasks);
                dailyTasks.add(dayMap);
            }
        }
        planData.put("dailyTasks", dailyTasks);
        
        // 转换推荐资源
        List<Map<String, Object>> recommendations = new ArrayList<>();
        if (aiResponse.getRecommendations() != null) {
            for (LearningPlanAIResponse.Recommendation rec : aiResponse.getRecommendations()) {
                Map<String, Object> recMap = new HashMap<>();
                recMap.put("type", rec.getType());
                recMap.put("title", rec.getTitle());
                recMap.put("source", rec.getSource());
                recMap.put("priority", rec.getPriority());
                if (rec.getDescription() != null) {
                    recMap.put("description", rec.getDescription());
                }
                recommendations.add(recMap);
            }
        }
        planData.put("recommendations", recommendations);
        
        return planData;
    }

    private Map<String, Object> convertToMap(LearningPlan plan) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", plan.getId());
        map.put("userId", plan.getUserId());
        map.put("track", plan.getTrack());
        map.put("trackLabel", plan.getTrackLabel());
        map.put("weeklyGoal", plan.getWeeklyGoal());
        map.put("generatedAt", plan.getGeneratedAt() != null ? plan.getGeneratedAt().toString() : null);
        map.put("createdAt", plan.getCreatedAt() != null ? plan.getCreatedAt().toString() : null);
        map.put("aiGenerated", plan.isAiGenerated());

        try {
            if (plan.getWeekGoalsJson() != null) {
                map.put("weekGoals", objectMapper.readValue(plan.getWeekGoalsJson(), List.class));
            }
            if (plan.getDailyTasksJson() != null) {
                map.put("dailyTasks", objectMapper.readValue(plan.getDailyTasksJson(), List.class));
            }
            if (plan.getRecommendationsJson() != null) {
                map.put("recommendations", objectMapper.readValue(plan.getRecommendationsJson(), List.class));
            }
            if (plan.getAiAnalysis() != null) {
                map.put("aiAnalysis", plan.getAiAnalysis());
            }
        } catch (JsonProcessingException e) {
            // ignore
        }

        return map;
    }

    private Map<String, Object> generateDefaultPlan(String track) {
        UserProfile defaultProfile = new UserProfile(
            track, 10, 0, 
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 
            50, 0, 0
        );
        return generatePersonalizedPlan(defaultProfile);
    }

    /**
     * 补充用户学习进度数据到计划
     */
    private Map<String, Object> enrichWithUserProgress(Map<String, Object> planData, Long userId, String track) {
        Map<String, Object> enriched = new HashMap<>(planData);

        // 1. 本周计划完成情况
        Map<String, Object> weeklyProgress = generateWeeklyProgress(userId, track);
        enriched.put("weeklyProgress", weeklyProgress);

        // 2. 每日安排（实际完成 vs 计划）
        List<Map<String, Object>> dailySchedule = generateDailySchedule(userId, track);
        enriched.put("dailySchedule", dailySchedule);

        // 3. 攻强补弱分析
        Map<String, Object> strengthWeakness = generateStrengthWeaknessAnalysis(userId, track);
        enriched.put("strengthWeakness", strengthWeakness);

        // 4. 坚持天数统计
        Map<String, Object> persistenceStats = generatePersistenceStats(userId);
        enriched.put("persistenceStats", persistenceStats);

        // 5. 学习数据概览
        Map<String, Object> learningOverview = generateLearningOverview(userId, track);
        enriched.put("learningOverview", learningOverview);

        return enriched;
    }

    /**
     * 生成本周计划完成情况
     */
    private Map<String, Object> generateWeeklyProgress(Long userId, String track) {
        Map<String, Object> progress = new HashMap<>();
        
        // 模拟数据（实际应从数据库查询）
        int weeklyGoal = 10;
        int completed = 7;
        int remaining = weeklyGoal - completed;
        double completionRate = (double) completed / weeklyGoal;
        
        progress.put("weeklyGoal", weeklyGoal);
        progress.put("completed", completed);
        progress.put("remaining", remaining);
        progress.put("completionRate", Math.round(completionRate * 100) / 100.0);
        progress.put("status", completionRate >= 1.0 ? "已完成" : completionRate >= 0.6 ? "进行中" : "需加油");
        
        // 按题型统计
        Map<String, Integer> byType = new HashMap<>();
        byType.put("single", 3);
        byType.put("multi", 2);
        byType.put("code", 2);
        byType.put("fill", 0);
        progress.put("completedByType", byType);
        
        // 最近7天完成情况
        List<Map<String, Object>> last7Days = new ArrayList<>();
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        int[] dailyCompleted = {1, 2, 1, 0, 2, 1, 0};
        for (int i = 0; i < 7; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("day", days[i]);
            day.put("completed", dailyCompleted[i]);
            day.put("target", Math.ceil((double) weeklyGoal / 7));
            day.put("isDone", dailyCompleted[i] >= Math.ceil((double) weeklyGoal / 7));
            last7Days.add(day);
        }
        progress.put("last7Days", last7Days);
        
        return progress;
    }

    /**
     * 生成每日安排（实际 vs 计划）
     */
    private List<Map<String, Object>> generateDailySchedule(Long userId, String track) {
        List<Map<String, Object>> schedule = new ArrayList<>();
        
        String[] days = {"今天", "明天", "后天"};
        String[] dates = getNext3Days();
        
        for (int i = 0; i < 3; i++) {
            Map<String, Object> daySchedule = new HashMap<>();
            daySchedule.put("day", days[i]);
            daySchedule.put("date", dates[i]);
            daySchedule.put("isToday", i == 0);
            
            // 计划任务
            List<Map<String, Object>> plannedTasks = new ArrayList<>();
            if (i == 0) {
                // 今天的任务（包含完成状态）
                plannedTasks.add(createTaskWithStatus("薄弱专项突破练习", 40, "practice", "high", true));
                plannedTasks.add(createTaskWithStatus("错题回顾与总结", 30, "review", "high", false));
                plannedTasks.add(createTaskWithStatus("算法思维强化训练", 30, "learn", "medium", false));
            } else {
                // 未来任务
                plannedTasks.add(createTask("新知识点学习", 40, "learn", "medium"));
                plannedTasks.add(createTask("综合练习题", 35, "practice", "medium"));
            }
            daySchedule.put("plannedTasks", plannedTasks);
            
            // 统计
            long completedCount = plannedTasks.stream()
                .filter(t -> (Boolean) t.getOrDefault("isCompleted", false))
                .count();
            daySchedule.put("totalTasks", plannedTasks.size());
            daySchedule.put("completedTasks", completedCount);
            daySchedule.put("progress", plannedTasks.isEmpty() ? 0 : (int) (completedCount * 100 / plannedTasks.size()));
            
            schedule.add(daySchedule);
        }
        
        return schedule;
    }

    /**
     * 生成攻强补弱分析
     */
    private Map<String, Object> generateStrengthWeaknessAnalysis(Long userId, String track) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 擅长领域（正确率 > 80%）
        List<Map<String, Object>> strongAreas = new ArrayList<>();
        strongAreas.add(createAreaStats("数组", 95, 45));
        strongAreas.add(createAreaStats("链表", 88, 32));
        strongAreas.add(createAreaStats("栈队列", 85, 28));
        analysis.put("strongAreas", strongAreas);
        
        // 薄弱环节（正确率 < 60%）
        List<Map<String, Object>> weakAreas = new ArrayList<>();
        weakAreas.add(createAreaStats("图论", 45, 15));
        weakAreas.add(createAreaStats("树形DP", 52, 12));
        weakAreas.add(createAreaStats("贪心算法", 58, 20));
        analysis.put("weakAreas", weakAreas);
        
        // 建议重点突破的顺序（按正确率排序）
        List<String> priorityOrder = weakAreas.stream()
            .sorted((a, b) -> (Integer) a.get("accuracy") - (Integer) b.get("accuracy"))
            .map(a -> (String) a.get("name"))
            .toList();
        analysis.put("priorityOrder", priorityOrder);
        
        // 攻强补弱建议
        List<String> suggestions = new ArrayList<>();
        if (!weakAreas.isEmpty()) {
            suggestions.add("优先攻克：" + weakAreas.get(0).get("name") + "（正确率仅" + weakAreas.get(0).get("accuracy") + "%）");
        }
        suggestions.add("保持优势：继续巩固" + strongAreas.get(0).get("name") + "的解题能力");
        suggestions.add("均衡发展：适当分配时间给薄弱环节");
        analysis.put("suggestions", suggestions);
        
        return analysis;
    }

    /**
     * 生成坚持天数统计
     */
    private Map<String, Object> generatePersistenceStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 连续打卡
        int currentStreak = 7;
        int longestStreak = 23;
        stats.put("currentStreak", currentStreak);
        stats.put("longestStreak", longestStreak);
        stats.put("streakStatus", currentStreak >= 7 ? "连续打卡达人" : currentStreak >= 3 ? "保持良好" : "继续加油");
        
        // 本月统计
        int monthlyTotalDays = 30;
        int monthlyActiveDays = 22;
        stats.put("monthlyActiveDays", monthlyActiveDays);
        stats.put("monthlyAttendanceRate", Math.round((double) monthlyActiveDays / monthlyTotalDays * 100));
        
        // 坚持指数 (0-100)
        int persistenceIndex = calculatePersistenceIndex(currentStreak, monthlyActiveDays, monthlyTotalDays);
        stats.put("persistenceIndex", persistenceIndex);
        stats.put("persistenceLevel", getPersistenceLevel(persistenceIndex));
        
        // 里程碑
        List<Map<String, Object>> milestones = new ArrayList<>();
        milestones.add(createMilestone("连续3天", currentStreak >= 3, currentStreak >= 3 ? "已获得" : "待完成"));
        milestones.add(createMilestone("连续7天", currentStreak >= 7, currentStreak >= 7 ? "已获得" : "待完成"));
        milestones.add(createMilestone("连续30天", currentStreak >= 30, currentStreak >= 30 ? "已获得" : "待完成"));
        milestones.add(createMilestone("累计100题", true, "已获得"));
        stats.put("milestones", milestones);
        
        return stats;
    }

    /**
     * 生成学习数据概览
     */
    private Map<String, Object> generateLearningOverview(Long userId, String track) {
        Map<String, Object> overview = new HashMap<>();
        
        // 总体统计
        overview.put("totalSolved", 156);
        overview.put("totalProblems", 200);
        overview.put("overallAccuracy", 78);
        overview.put("totalPoints", 2500);
        
        // 赛道统计
        Map<String, Object> trackStats = new HashMap<>();
        trackStats.put("algo", Map.of("solved", 65, "total", 80, "accuracy", 81));
        trackStats.put("ds", Map.of("solved", 52, "total", 70, "accuracy", 74));
        trackStats.put("contest", Map.of("solved", 39, "total", 50, "accuracy", 78));
        overview.put("trackStats", trackStats);
        
        // 最近活动
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        recentActivities.add(createActivity("完成了题目「二分查找」", "solve", 18, "10分钟前"));
        recentActivities.add(createActivity("错题回顾「动态规划」", "review", 0, "1小时前"));
        recentActivities.add(createActivity("连续打卡7天", "achievement", 50, "昨天"));
        overview.put("recentActivities", recentActivities);
        
        return overview;
    }

    // 辅助方法
    private String[] getNext3Days() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return new String[]{
            today.toString(),
            today.plusDays(1).toString(),
            today.plusDays(2).toString()
        };
    }

    private Map<String, Object> createTaskWithStatus(String title, Integer duration, String type, String priority, Boolean isCompleted) {
        Map<String, Object> task = createTask(title, duration, type, priority);
        task.put("isCompleted", isCompleted);
        return task;
    }

    private Map<String, Object> createAreaStats(String name, int accuracy, int solvedCount) {
        Map<String, Object> area = new HashMap<>();
        area.put("name", name);
        area.put("accuracy", accuracy);
        area.put("solvedCount", solvedCount);
        area.put("level", accuracy >= 80 ? "优秀" : accuracy >= 60 ? "良好" : "需加强");
        return area;
    }

    private int calculatePersistenceIndex(int currentStreak, int monthlyActiveDays, int monthlyTotalDays) {
        double streakScore = Math.min(currentStreak / 14.0, 1.0) * 40;
        double attendanceScore = (double) monthlyActiveDays / monthlyTotalDays * 40;
        double persistenceScore = Math.min(monthlyActiveDays / 30.0, 1.0) * 20;
        return (int) Math.round(streakScore + attendanceScore + persistenceScore);
    }

    private String getPersistenceLevel(int index) {
        if (index >= 80) return "优秀";
        if (index >= 60) return "良好";
        if (index >= 40) return "一般";
        return "需努力";
    }

    private Map<String, Object> createMilestone(String name, boolean achieved, String status) {
        Map<String, Object> milestone = new HashMap<>();
        milestone.put("name", name);
        milestone.put("achieved", achieved);
        milestone.put("status", status);
        return milestone;
    }

    private Map<String, Object> createActivity(String description, String type, int points, String time) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("description", description);
        activity.put("type", type);
        activity.put("points", points);
        activity.put("time", time);
        return activity;
    }

    /**
     * 基于用户画像生成个性化学习计划（本地算法，作为AI失败时的降级方案）
     */
    private Map<String, Object> generatePersonalizedPlan(UserProfile profile) {
        Map<String, Object> plan = new HashMap<>();
        
        // 根据坚持指数调整目标难度
        int adjustedWeeklyGoal = adjustGoalByPersistence(profile.weeklyGoal, profile.persistenceIndex);
        double completionRate = profile.weeklyGoal > 0 ? (double) profile.weeklyCompleted / profile.weeklyGoal : 0;
        
        // 生成周目标（基于用户强弱项和错题）
        List<Map<String, Object>> weekGoals = generateWeekGoals(profile, adjustedWeeklyGoal, completionRate);
        plan.put("weekGoals", weekGoals);

        // 生成每日任务（基于错题集和薄弱环节重点安排）
        List<Map<String, Object>> dailyTasks = generateDailyTasks(profile, completionRate);
        plan.put("dailyTasks", dailyTasks);

        // 生成推荐资源（针对性推荐）
        List<Map<String, Object>> recommendations = generateRecommendations(profile);
        plan.put("recommendations", recommendations);

        return plan;
    }

    /**
     * 根据坚持指数调整目标
     */
    private int adjustGoalByPersistence(int baseGoal, int persistenceIndex) {
        if (persistenceIndex >= 80) {
            return (int) (baseGoal * 1.2); // 高坚持指数，可适当增加目标
        } else if (persistenceIndex >= 50) {
            return baseGoal; // 正常目标
        } else {
            return (int) (baseGoal * 0.8); // 低坚持指数，降低目标避免挫败
        }
    }

    /**
     * 生成周目标（个性化）
     */
    private List<Map<String, Object>> generateWeekGoals(UserProfile profile, int adjustedGoal, double completionRate) {
        List<Map<String, Object>> goals = new ArrayList<>();
        String track = profile.track;
        
        // 目标1：基于薄弱环节的专项突破
        if (!profile.weakAreas.isEmpty()) {
            String weakArea = profile.weakAreas.get(0);
            goals.add(createGoal(1, "薄弱专项突破：" + weakArea, 0, (int) Math.ceil(adjustedGoal * 0.35)));
        } else {
            goals.add(createGoal(1, "基础巩固练习", 0, (int) Math.ceil(adjustedGoal * 0.35)));
        }
        
        // 目标2：错题回顾（如果有错题）
        if (!profile.errorTopics.isEmpty()) {
            goals.add(createGoal(2, "错题回顾与总结", 0, (int) Math.ceil(adjustedGoal * 0.25)));
        } else {
            goals.add(createGoal(2, "新知识点学习", 0, (int) Math.ceil(adjustedGoal * 0.25)));
        }
        
        // 目标3：赛道核心目标
        if ("algo".equals(track)) {
            goals.add(createGoal(3, "算法思维强化", 0, (int) Math.ceil(adjustedGoal * 0.25)));
        } else if ("ds".equals(track)) {
            goals.add(createGoal(3, "数据结构应用", 0, (int) Math.ceil(adjustedGoal * 0.25)));
        } else {
            goals.add(createGoal(3, "竞赛技巧提升", 0, (int) Math.ceil(adjustedGoal * 0.25)));
        }
        
        // 目标4：每日坚持（根据完成率调整）
        int dailyTarget = completionRate < 0.5 ? Math.max(1, adjustedGoal / 7) : adjustedGoal;
        goals.add(createGoal(4, "每日坚持打卡", profile.weeklyCompleted, dailyTarget));

        return goals;
    }

    /**
     * 生成每日任务（基于错题集和薄弱环节）
     */
    private List<Map<String, Object>> generateDailyTasks(UserProfile profile, double completionRate) {
        List<Map<String, Object>> dailyTasks = new ArrayList<>();
        String track = profile.track;
        
        // 今天：重点攻克薄弱环节 + 错题回顾
        List<Map<String, Object>> todayTasks = new ArrayList<>();
        
        // 如果有薄弱领域，优先安排
        if (!profile.weakAreas.isEmpty()) {
            todayTasks.add(createTask(profile.weakAreas.get(0) + " 专项练习", 40, "practice", "high"));
        }
        
        // 如果有错题，安排错题回顾
        if (!profile.errorTopics.isEmpty()) {
            todayTasks.add(createTask("错题回顾：" + profile.errorTopics.get(0), 30, "review", "high"));
        }
        
        // 根据赛道安排学习内容
        if ("algo".equals(track)) {
            todayTasks.add(createTask("动态规划思想理解", 30, "learn", "medium"));
            if (profile.strongAreas.contains("动态规划")) {
                todayTasks.add(createTask("DP进阶挑战", 25, "practice", "medium"));
            }
        } else if ("ds".equals(track)) {
            todayTasks.add(createTask("树结构遍历方法", 35, "learn", "medium"));
            if (profile.strongAreas.contains("二叉树")) {
                todayTasks.add(createTask("高级树结构应用", 25, "practice", "medium"));
            }
        } else {
            todayTasks.add(createTask("竞赛策略与技巧", 30, "learn", "medium"));
            todayTasks.add(createTask("限时模拟训练", 30, "practice", "high"));
        }
        
        dailyTasks.add(createDayTask("今天", todayTasks));
        
        // 明天：新内容学习 + 巩固
        List<Map<String, Object>> tomorrowTasks = new ArrayList<>();
        if (completionRate < 0.3) {
            // 完成率低，降低难度
            tomorrowTasks.add(createTask("基础概念复习", 30, "review", "low"));
            tomorrowTasks.add(createTask("简单练习题", 25, "practice", "low"));
        } else {
            tomorrowTasks.add(createTask(track + " 进阶知识点", 40, "learn", "medium"));
            tomorrowTasks.add(createTask("综合练习题", 35, "practice", "medium"));
        }
        dailyTasks.add(createDayTask("明天", tomorrowTasks));
        
        // 后天：综合练习 + 周总结
        List<Map<String, Object>> day3Tasks = new ArrayList<>();
        if (profile.currentStreak >= 7) {
            day3Tasks.add(createTask("连续打卡奖励：挑战题", 45, "challenge", "high"));
        }
        day3Tasks.add(createTask("本周知识点总结", 30, "review", "medium"));
        day3Tasks.add(createTask("综合模拟测试", 40, "practice", "medium"));
        dailyTasks.add(createDayTask("后天", day3Tasks));

        return dailyTasks;
    }

    /**
     * 生成推荐资源（针对性）
     */
    private List<Map<String, Object>> generateRecommendations(UserProfile profile) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        String track = profile.track;
        
        // 基于薄弱环节的推荐
        if (!profile.weakAreas.isEmpty()) {
            String weakArea = profile.weakAreas.get(0);
            recommendations.add(createRecommendation("video", weakArea + " 专项讲解", "针对薄弱点", "high"));
            recommendations.add(createRecommendation("practice", weakArea + " 专项练习20题", "强化训练", "high"));
        }
        
        // 基于错题的推荐
        if (!profile.errorTopics.isEmpty()) {
            String errorTopic = profile.errorTopics.get(0);
            recommendations.add(createRecommendation("article", errorTopic + " 常见错误分析", "避免再犯", "medium"));
        }
        
        // 基于坚持指数的推荐
        if (profile.persistenceIndex < 50) {
            recommendations.add(createRecommendation("article", "如何建立刷题习惯", "习惯养成", "high"));
        }
        
        // 赛道基础推荐
        if ("algo".equals(track)) {
            recommendations.add(createRecommendation("video", "动态规划入门讲解", "基础巩固", "medium"));
            recommendations.add(createRecommendation("practice", "DP经典50题", "必刷题集", "medium"));
        } else if ("ds".equals(track)) {
            recommendations.add(createRecommendation("video", "数据结构基础", "系统学习", "medium"));
            recommendations.add(createRecommendation("practice", "树结构专项训练", "重点突破", "medium"));
        } else {
            recommendations.add(createRecommendation("video", "竞赛算法精讲", "技巧提升", "medium"));
            recommendations.add(createRecommendation("practice", "Codeforces真题", "实战演练", "medium"));
        }

        return recommendations;
    }

    /**
     * 生成用户画像分析报告
     */
    private Map<String, Object> generateProfileAnalysis(UserProfile profile) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 学习状态评估
        String status;
        if (profile.persistenceIndex >= 80) {
            status = "学习状态优秀，保持当前节奏";
        } else if (profile.persistenceIndex >= 50) {
            status = "学习状态良好，可适当提升强度";
        } else {
            status = "学习状态需调整，建议降低目标建立信心";
        }
        analysis.put("learningStatus", status);
        
        // 重点关注
        List<String> focusAreas = new ArrayList<>();
        if (!profile.weakAreas.isEmpty()) {
            focusAreas.add("薄弱环节：" + String.join(", ", profile.weakAreas));
        }
        if (!profile.errorTopics.isEmpty()) {
            focusAreas.add("错题知识点：" + String.join(", ", profile.errorTopics.subList(0, Math.min(3, profile.errorTopics.size()))));
        }
        if (focusAreas.isEmpty()) {
            focusAreas.add("全面均衡发展");
        }
        analysis.put("focusAreas", focusAreas);
        
        // 建议
        List<String> suggestions = new ArrayList<>();
        if (profile.weeklyCompleted < profile.weeklyGoal * 0.5) {
            suggestions.add("本周完成率较低，建议优先完成基础任务");
        }
        if (profile.currentStreak < 3) {
            suggestions.add("坚持连续打卡，培养学习习惯");
        }
        if (!profile.weakAreas.isEmpty()) {
            suggestions.add("针对薄弱环节进行专项突破");
        }
        analysis.put("suggestions", suggestions);
        
        return analysis;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Map<String, Object> createGoal(Integer id, String title, Integer progress, Integer target) {
        Map<String, Object> goal = new HashMap<>();
        goal.put("id", id);
        goal.put("title", title);
        goal.put("progress", progress);
        goal.put("target", target);
        return goal;
    }

    private Map<String, Object> createDayTask(String day, List<Map<String, Object>> tasks) {
        Map<String, Object> dayTask = new HashMap<>();
        dayTask.put("day", day);
        dayTask.put("tasks", tasks);
        return dayTask;
    }

    private Map<String, Object> createTask(String title, Integer duration, String type) {
        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("duration", duration);
        task.put("type", type);
        return task;
    }

    private Map<String, Object> createTask(String title, Integer duration, String type, String priority) {
        Map<String, Object> task = createTask(title, duration, type);
        task.put("priority", priority);
        return task;
    }

    private Map<String, Object> createRecommendation(String type, String title, String source) {
        Map<String, Object> rec = new HashMap<>();
        rec.put("type", type);
        rec.put("title", title);
        rec.put("source", source);
        return rec;
    }

    private Map<String, Object> createRecommendation(String type, String title, String source, String priority) {
        Map<String, Object> rec = createRecommendation(type, title, source);
        rec.put("priority", priority);
        return rec;
    }

    /**
     * 用户画像内部类
     */
    private static class UserProfile {
        final String track;
        final int weeklyGoal;
        final int weeklyCompleted;
        final List<String> errorTopics;
        final List<String> strongAreas;
        final List<String> weakAreas;
        final int persistenceIndex;
        final int totalSolved;
        final int currentStreak;

        UserProfile(String track, int weeklyGoal, int weeklyCompleted,
                    List<String> errorTopics, List<String> strongAreas, List<String> weakAreas,
                    int persistenceIndex, int totalSolved, int currentStreak) {
            this.track = track;
            this.weeklyGoal = weeklyGoal;
            this.weeklyCompleted = weeklyCompleted;
            this.errorTopics = errorTopics != null ? errorTopics : new ArrayList<>();
            this.strongAreas = strongAreas != null ? strongAreas : new ArrayList<>();
            this.weakAreas = weakAreas != null ? weakAreas : new ArrayList<>();
            this.persistenceIndex = persistenceIndex;
            this.totalSolved = totalSolved;
            this.currentStreak = currentStreak;
        }
    }
}
