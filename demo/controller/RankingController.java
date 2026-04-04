package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行榜接口，完全匹配开发文档扩展接口要求
 */
@RestController
public class RankingController {

    private final UserRepository userRepository;

    // 构造器注入
    public RankingController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 6.5 排行榜
     * GET /api/ranking?track=all|algo|ds|contest&period=all|week|month
     * 响应data：[{id, name, points, targetTrack, rank}]
     */
    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getRanking(
            @RequestParam(defaultValue = "all") String track,
            @RequestParam(defaultValue = "all") String period) {

        // 1. 查询用户列表（按赛道筛选，按积分降序）
        List<User> userList = userRepository.findRankingByTrack(track);
        if (userList == null) {
            userList = new ArrayList<>();
        }

        // 2. 构造排行榜数据（Java8 兼容，无泛型报错）
        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (User user : userList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getName());
            // 修复空指针：points为null时赋值0
            map.put("points", user.getPoints() == null ? 0 : user.getPoints());
            map.put("targetTrack", user.getTargetTrack());
            rankingList.add(map);
        }

        // 3. 为每个用户添加排名
        for (int i = 0; i < rankingList.size(); i++) {
            rankingList.get(i).put("rank", i + 1);
        }

        // 4. 返回结果
        return Result.success(rankingList);
    }
}