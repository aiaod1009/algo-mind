package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.dto.HotQuestionDTO;
import com.example.demo.entity.Level;
import com.example.demo.repository.LevelRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@RestController
public class HotQuestionController {

    private final LevelRepository levelRepository;

    public HotQuestionController(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    @GetMapping("/hot-questions")
    public Result<List<HotQuestionDTO>> getHotQuestions() {
        List<Level> levels = levelRepository.findAll();
        List<HotQuestionDTO> hotQuestions = new ArrayList<>();
        Random random = new Random(42);
        
        for (Level level : levels) {
            if (level.getQuestion() == null || level.getQuestion().isBlank()) {
                continue;
            }
            
            String difficulty = inferDifficulty(level);
            String passRate = generatePassRate(random);
            List<String> tags = inferTags(level);
            int hotScore = 3000 + random.nextInt(7000);
            
            HotQuestionDTO dto = new HotQuestionDTO(
                level.getId(),
                level.getName() != null ? level.getName() : "题目" + level.getId(),
                difficulty,
                passRate,
                tags,
                hotScore
            );
            hotQuestions.add(dto);
        }
        
        hotQuestions.sort(Comparator.comparing(HotQuestionDTO::getHot).reversed());
        
        if (hotQuestions.size() > 20) {
            hotQuestions = hotQuestions.subList(0, 20);
        }
        
        return Result.success(hotQuestions);
    }
    
    private String inferDifficulty(Level level) {
        Integer reward = level.getRewardPoints();
        if (reward == null) reward = 10;
        if (reward <= 10) return "简单";
        if (reward <= 20) return "中等";
        return "困难";
    }
    
    private String generatePassRate(Random random) {
        double rate = 20 + random.nextDouble() * 60;
        return String.format("%.1f%%", rate);
    }
    
    private List<String> inferTags(Level level) {
        List<String> tags = new ArrayList<>();
        String question = level.getQuestion() != null ? level.getQuestion() : "";
        String name = level.getName() != null ? level.getName() : "";
        String text = question + " " + name;
        
        if (text.contains("数组") || text.contains("两数") || text.contains("三数") || text.contains("子数组")) {
            tags.add("数组");
        }
        if (text.contains("链表") || text.contains("节点")) {
            tags.add("链表");
        }
        if (text.contains("树") || text.contains("二叉") || text.contains("遍历")) {
            tags.add("树");
        }
        if (text.contains("动态规划") || text.contains("DP") || text.contains("子串")) {
            tags.add("DP");
        }
        if (text.contains("栈") || text.contains("队列")) {
            tags.add("栈/队列");
        }
        if (text.contains("图") || text.contains("BFS") || text.contains("DFS")) {
            tags.add("图");
        }
        if (text.contains("哈希") || text.contains("Hash")) {
            tags.add("哈希表");
        }
        if (text.contains("排序") || text.contains("归并") || text.contains("快排")) {
            tags.add("排序");
        }
        if (text.contains("二分") || text.contains("查找")) {
            tags.add("二分");
        }
        if (text.contains("双指针") || text.contains("滑动窗口")) {
            tags.add("双指针");
        }
        
        if (tags.isEmpty()) {
            tags.add("基础");
        }
        
        return tags;
    }
}
