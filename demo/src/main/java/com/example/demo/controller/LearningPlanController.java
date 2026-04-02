package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.LearningPlan;
import com.example.demo.repository.LearningPlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/learning-plans")
public class LearningPlanController {

    private final LearningPlanRepository learningPlanRepository;
    private final ObjectMapper objectMapper;

    public LearningPlanController(LearningPlanRepository learningPlanRepository, ObjectMapper objectMapper) {
        this.learningPlanRepository = learningPlanRepository;
        this.objectMapper = objectMapper;
    }

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

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentPlan(
            @RequestParam(defaultValue = "algo") String track) {
        Long currentUserId = 1L;

        Optional<LearningPlan> planOpt = learningPlanRepository
                .findFirstByUserIdAndTrackOrderByCreatedAtDesc(currentUserId, track);

        if (planOpt.isPresent()) {
            return Result.success(convertToMap(planOpt.get()));
        }

        return Result.success(generateDefaultPlan(track));
    }

    @PostMapping("/generate")
    public Result<Map<String, Object>> generateLearningPlan(@RequestBody Map<String, Object> request) {
        Long currentUserId = 1L;
        String track = (String) request.getOrDefault("track", "algo");
        String trackLabel = (String) request.getOrDefault("trackLabel", "算法思维赛道");
        Integer weeklyGoal = (Integer) request.getOrDefault("weeklyGoal", 10);

        Map<String, Object> planData = generatePlanData(track, weeklyGoal);

        LearningPlan plan = new LearningPlan();
        plan.setUserId(currentUserId);
        plan.setTrack(track);
        plan.setTrackLabel(trackLabel);
        plan.setWeeklyGoal(weeklyGoal);
        plan.setGeneratedAt(OffsetDateTime.now());
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setUpdatedAt(OffsetDateTime.now());

        try {
            plan.setWeekGoalsJson(objectMapper.writeValueAsString(planData.get("weekGoals")));
            plan.setDailyTasksJson(objectMapper.writeValueAsString(planData.get("dailyTasks")));
            plan.setRecommendationsJson(objectMapper.writeValueAsString(planData.get("recommendations")));
        } catch (JsonProcessingException e) {
            return Result.fail(50001, "计划生成失败: " + e.getMessage());
        }

        LearningPlan saved = learningPlanRepository.save(plan);
        Map<String, Object> result = convertToMap(saved);
        result.put("isNewGenerated", true);

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

    private Map<String, Object> convertToMap(LearningPlan plan) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", plan.getId());
        map.put("userId", plan.getUserId());
        map.put("track", plan.getTrack());
        map.put("trackLabel", plan.getTrackLabel());
        map.put("weeklyGoal", plan.getWeeklyGoal());
        map.put("generatedAt", plan.getGeneratedAt() != null ? plan.getGeneratedAt().toString() : null);
        map.put("createdAt", plan.getCreatedAt() != null ? plan.getCreatedAt().toString() : null);

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
        } catch (JsonProcessingException e) {
            // ignore
        }

        return map;
    }

    private Map<String, Object> generateDefaultPlan(String track) {
        return generatePlanData(track, 10);
    }

    private Map<String, Object> generatePlanData(String track, Integer weeklyGoal) {
        Map<String, Object> plan = new HashMap<>();

        // 生成周目标
        List<Map<String, Object>> weekGoals = new ArrayList<>();
        if ("algo".equals(track)) {
            weekGoals.add(createGoal(1, "掌握动态规划基础", 0, (int) Math.ceil(weeklyGoal * 0.4)));
            weekGoals.add(createGoal(2, "完成贪心算法入门", 0, (int) Math.ceil(weeklyGoal * 0.3)));
            weekGoals.add(createGoal(3, "每日坚持练习", 0, weeklyGoal));
        } else if ("ds".equals(track)) {
            weekGoals.add(createGoal(1, "掌握链表与栈队列", 0, (int) Math.ceil(weeklyGoal * 0.35)));
            weekGoals.add(createGoal(2, "完成二叉树基础", 0, (int) Math.ceil(weeklyGoal * 0.35)));
            weekGoals.add(createGoal(3, "每日坚持练习", 0, weeklyGoal));
        } else {
            weekGoals.add(createGoal(1, "掌握高频竞赛题型", 0, (int) Math.ceil(weeklyGoal * 0.4)));
            weekGoals.add(createGoal(2, "提升解题速度", 0, (int) Math.ceil(weeklyGoal * 0.3)));
            weekGoals.add(createGoal(3, "完成模拟赛训练", 0, (int) Math.ceil(weeklyGoal * 0.3)));
        }
        plan.put("weekGoals", weekGoals);

        // 生成每日任务
        List<Map<String, Object>> dailyTasks = new ArrayList<>();
        if ("algo".equals(track)) {
            dailyTasks.add(createDayTask("今天", Arrays.asList(
                    createTask("动态规划基础概念", 30, "learn"),
                    createTask("斐波那契数列DP解法", 25, "practice")
            )));
            dailyTasks.add(createDayTask("明天", Arrays.asList(
                    createTask("背包问题入门", 40, "learn"),
                    createTask("01背包练习题", 35, "practice")
            )));
            dailyTasks.add(createDayTask("后天", Arrays.asList(
                    createTask("贪心算法思想", 30, "learn"),
                    createTask("区间调度问题", 30, "practice")
            )));
        } else if ("ds".equals(track)) {
            dailyTasks.add(createDayTask("今天", Arrays.asList(
                    createTask("链表基础操作", 35, "learn"),
                    createTask("反转链表实现", 30, "practice")
            )));
            dailyTasks.add(createDayTask("明天", Arrays.asList(
                    createTask("栈与队列应用", 30, "learn"),
                    createTask("括号匹配问题", 25, "practice")
            )));
            dailyTasks.add(createDayTask("后天", Arrays.asList(
                    createTask("二叉树遍历", 40, "learn"),
                    createTask("前中后序遍历实现", 35, "practice")
            )));
        } else {
            dailyTasks.add(createDayTask("今天", Arrays.asList(
                    createTask("竞赛技巧与策略", 30, "learn"),
                    createTask("快速排序优化", 25, "practice")
            )));
            dailyTasks.add(createDayTask("明天", Arrays.asList(
                    createTask("二分查找进阶", 35, "learn"),
                    createTask("二分答案专题", 40, "practice")
            )));
            dailyTasks.add(createDayTask("后天", Arrays.asList(
                    createTask("模拟竞赛训练", 60, "practice"),
                    createTask("赛后复盘总结", 20, "review")
            )));
        }
        plan.put("dailyTasks", dailyTasks);

        // 生成推荐资源
        List<Map<String, Object>> recommendations = new ArrayList<>();
        if ("algo".equals(track)) {
            recommendations.add(createRecommendation("video", "动态规划入门讲解", "推荐"));
            recommendations.add(createRecommendation("article", "背包问题详解", "必读"));
            recommendations.add(createRecommendation("practice", "DP经典50题", "练习"));
        } else if ("ds".equals(track)) {
            recommendations.add(createRecommendation("video", "数据结构基础", "推荐"));
            recommendations.add(createRecommendation("article", "链表操作技巧", "必读"));
            recommendations.add(createRecommendation("practice", "树结构专项训练", "练习"));
        } else {
            recommendations.add(createRecommendation("video", "竞赛算法精讲", "推荐"));
            recommendations.add(createRecommendation("article", "ACM竞赛经验", "必读"));
            recommendations.add(createRecommendation("practice", "Codeforces真题", "练习"));
        }
        plan.put("recommendations", recommendations);

        return plan;
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

    private Map<String, Object> createRecommendation(String type, String title, String source) {
        Map<String, Object> rec = new HashMap<>();
        rec.put("type", type);
        rec.put("title", title);
        rec.put("source", source);
        return rec;
    }
}
