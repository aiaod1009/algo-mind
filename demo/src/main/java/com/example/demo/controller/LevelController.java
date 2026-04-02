package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.Level;
import com.example.demo.repository.LevelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 关卡接口，修正参数处理、默认值、解锁逻辑
 */
@RestController
public class LevelController {

    private final LevelRepository levelRepository;

    public LevelController(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    /**
     * 获取关卡列表
     * GET /api/levels?track=algo|ds|contest
     */
    @GetMapping("/levels")
    public Result<List<Level>> getLevels(@RequestParam(required = false) String track) {
        List<Level> levelList;
        if (track != null && !track.isBlank()) {
            levelList = levelRepository.findByTrack(track);
        } else {
            levelList = levelRepository.findAll();
        }
        // 补全默认值（避免null）
        levelList.forEach(level -> {
            if (level.getIsUnlocked() == null) {
                level.setIsUnlocked(false); // 默认未解锁
            }
            if (level.getRewardPoints() == null) {
                level.setRewardPoints(0); // 默认奖励0积分
            }
        });
        return Result.success(levelList == null ? List.of() : levelList);
    }

    /**
     * 提交答案判题
     * POST /api/submit
     * 支持answer为字符串/数组类型
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAnswer(@RequestBody Map<String, Object> submitRequest) {
        // 1. 校验必传参数
        if (!submitRequest.containsKey("levelId")) {
            return Result.fail(40001, "缺少必传参数：levelId");
        }
        if (!submitRequest.containsKey("answer")) {
            return Result.fail(40001, "缺少必传参数：answer");
        }

        // 2. 解析levelId
        Long levelId;
        try {
            levelId = Long.valueOf(submitRequest.get("levelId").toString());
        } catch (Exception e) {
            return Result.fail(40001, "levelId必须为数字");
        }

        // 3. 解析answer（兼容字符串/数组）
        String userAnswer;
        Object answerObj = submitRequest.get("answer");
        if (answerObj instanceof List<?>) {
            // 数组转字符串（如["A","B"] → "A,B"）
            userAnswer = String.join(",", ((List<?>) answerObj).stream().map(Object::toString).toList());
        } else {
            // 字符串直接使用
            userAnswer = answerObj.toString().trim();
        }

        // 4. 查询关卡（不存在则抛异常）
        Level currentLevel = levelRepository.findById(levelId)
                .orElseThrow(() -> new NoSuchElementException("关卡ID：" + levelId));

        // 补全关卡默认值
        if (currentLevel.getAnswer() == null) {
            currentLevel.setAnswer("");
        }
        if (currentLevel.getRewardPoints() == null) {
            currentLevel.setRewardPoints(0);
        }

        // 5. 判题逻辑
        boolean isCorrect = userAnswer.equals(currentLevel.getAnswer().trim());
        Map<String, Object> responseData = new HashMap<>();

        if (isCorrect) {
            // 答对：返回积分、解锁下一关
            responseData.put("correct", true);
            responseData.put("pointsEarned", currentLevel.getRewardPoints());

            // 查询下一关（按order+1）
            Level nextLevel = levelRepository.findByOrder(currentLevel.getOrder() + 1);
            boolean nextUnlocked = false;
            if (nextLevel != null) {
                // 解锁下一关（仅当未解锁时）
                if (nextLevel.getIsUnlocked() == null || !nextLevel.getIsUnlocked()) {
                    nextLevel.setIsUnlocked(true);
                    levelRepository.save(nextLevel);
                }
                nextUnlocked = true;
            }
            responseData.put("nextLevelUnlocked", nextUnlocked);

        } else {
            // 答错
            responseData.put("correct", false);
            responseData.put("pointsEarned", 0);
            responseData.put("nextLevelUnlocked", false);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        return Result.success(map);
    }
}