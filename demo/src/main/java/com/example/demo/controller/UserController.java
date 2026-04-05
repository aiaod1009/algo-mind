package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private static final List<String> VALID_TRACKS = List.of("algo", "ds", "contest");

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PutMapping("/me")
    public Result<User> updateUserProfile(@RequestBody User userRequest) {
        Long currentUserId = 1L;
        
        // 使用 UserService 获取或创建用户
        User user = userService.getOrCreateUser(currentUserId);

        if (userRequest.getName() != null && !userRequest.getName().isBlank()) {
            user.setName(userRequest.getName());
        }
        if (userRequest.getBio() != null) {
            user.setBio(userRequest.getBio());
        }
        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
        if (userRequest.getGender() != null && !userRequest.getGender().isBlank()) {
            if (List.of("unknown", "male", "female").contains(userRequest.getGender())) {
                user.setGender(userRequest.getGender());
            } else {
                return Result.fail(40001, "gender仅支持：unknown|male|female");
            }
        }
        if (userRequest.getAvatar() != null) {
            user.setAvatar(userRequest.getAvatar());
        }
        if (userRequest.getTargetTrack() != null && !userRequest.getTargetTrack().isBlank()) {
            if (VALID_TRACKS.contains(userRequest.getTargetTrack())) {
                user.setTargetTrack(userRequest.getTargetTrack());
            } else {
                return Result.fail(40001, "targetTrack仅支持：algo|ds|contest");
            }
        }
        if (userRequest.getWeeklyGoal() != null) {
            if (userRequest.getWeeklyGoal() > 0) {
                user.setWeeklyGoal(userRequest.getWeeklyGoal());
            } else {
                return Result.fail(40001, "weeklyGoal必须为正整数");
            }
        }
        if (userRequest.getGithub() != null) {
            user.setGithub(userRequest.getGithub());
        }
        if (userRequest.getWebsite() != null) {
            user.setWebsite(userRequest.getWebsite());
        }

        user.setUpdatedAt(OffsetDateTime.now());
        User updatedUser = userRepository.save(user);
        return Result.success(updatedUser);
    }

    @GetMapping("/me/heatmap")
    public Result<Map<String, Object>> getUserHeatmap(
            @RequestParam(required = false) Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        Long currentUserId = 1L;

        List<Map<String, Object>> records = new ArrayList<>();
        Random random = new Random(targetYear + currentUserId);

        LocalDate startOfYear = LocalDate.of(targetYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(targetYear, 12, 31);

        int totalCount = 0;
        int activeDays = 0;
        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 0;
        LocalDate today = LocalDate.now();

        for (LocalDate date = startOfYear; !date.isAfter(endOfYear); date = date.plusDays(1)) {
            int count = 0;
            java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
            int month = date.getMonthValue();

            if (random.nextDouble() < 0.28) {
                count = 0;
            } else {
                if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                    count = 2 + random.nextInt(7);
                } else {
                    count = random.nextInt(4);
                }

                if (month == 3 || month == 4 || month == 10 || month == 11) {
                    count += random.nextInt(3);
                }

                if (month == 1 && date.getDayOfMonth() <= 7) {
                    count += 2;
                }

                if (month == 6 && random.nextDouble() < 0.3) {
                    count = 0;
                }
            }

            int level;
            if (count == 0) level = 0;
            else if (count <= 2) level = 1;
            else if (count <= 4) level = 2;
            else if (count <= 7) level = 3;
            else level = 4;

            Map<String, Object> entry = new HashMap<>();
            entry.put("date", date.toString());
            entry.put("count", count);
            entry.put("level", level);
            records.add(entry);

            if (count > 0) {
                totalCount += count;
                activeDays++;
                tempStreak++;
                longestStreak = Math.max(longestStreak, tempStreak);
            } else {
                tempStreak = 0;
            }

            if (date.equals(today.minusDays(currentStreak))) {
                currentStreak++;
            }
        }

        int monthlyActiveDays = 0;
        int monthlyTotalCount = 0;
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        for (LocalDate date = firstDayOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
            int idx = (int) java.time.temporal.ChronoUnit.DAYS.between(startOfYear, date);
            if (idx >= 0 && idx < records.size()) {
                Map<String, Object> rec = records.get(idx);
                int c = (int) rec.get("count");
                if (c > 0) {
                    monthlyActiveDays++;
                    monthlyTotalCount += c;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("year", targetYear);
        result.put("totalCount", totalCount);
        result.put("activeDays", activeDays);
        result.put("longestStreak", longestStreak);
        result.put("currentStreak", currentStreak);
        result.put("monthlyActiveDays", monthlyActiveDays);
        result.put("monthlyTotalCount", monthlyTotalCount);
        result.put("records", records);

        return Result.success(result);
    }

    @GetMapping("/me/stats")
    public Result<Map<String, Object>> getUserStats() {
        Long currentUserId = 1L;

        User user = userRepository.findById(currentUserId).orElse(null);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalProblems", 156);
        stats.put("solvedProblems", 89);
        stats.put("createdProblems", 12);
        stats.put("totalPoints", user != null ? user.getPoints() : 0);
        stats.put("currentStreak", 7);
        stats.put("longestStreak", 23);
        stats.put("accuracy", 78.5);
        stats.put("weeklyGoal", user != null ? user.getWeeklyGoal() : 10);
        stats.put("weeklyProgress", 5);
        stats.put("trackStats", Map.of(
                "algo", Map.of("solved", 45, "total", 60, "accuracy", 82.3),
                "ds", Map.of("solved", 28, "total", 50, "accuracy", 75.0),
                "contest", Map.of("solved", 16, "total", 46, "accuracy", 71.2)
        ));

        return Result.success(stats);
    }

    @GetMapping("/me/activities")
    public Result<Map<String, Object>> getUserActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = 1L;

        List<Map<String, Object>> allActivities = new ArrayList<>();
        String[] types = {"solve", "create", "achievement", "comment", "like"};
        String[] descriptions = {
                "完成了题目「二分查找」",
                "创建了新题目「滑动窗口进阶」",
                "获得成就「连续打卡7天」",
                "评论了帖子「算法学习心得」",
                "点赞了帖子「动态规划详解」",
                "完成了题目「链表反转」",
                "创建了新题目「并查集应用」",
                "获得成就「刷题100道」"
        };

        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", (long) (i + 1));
            activity.put("type", types[random.nextInt(types.length)]);
            activity.put("description", descriptions[random.nextInt(descriptions.length)]);
            activity.put("points", random.nextInt(20) + 5);
            activity.put("createdAt", OffsetDateTime.now().minusDays(random.nextInt(30)).toString());
            allActivities.add(activity);
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allActivities.size());
        List<Map<String, Object>> pageActivities = start < allActivities.size() 
                ? allActivities.subList(start, end) 
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageActivities);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", allActivities.size());

        return Result.success(result);
    }

    @GetMapping("/me/status")
    public Result<Map<String, Object>> getUserStatus() {
        Long currentUserId = 1L;

        User user = userRepository.findById(currentUserId).orElse(null);
        Map<String, Object> status = new HashMap<>();

        if (user != null) {
            status.put("emoji", user.getStatusEmoji() != null ? user.getStatusEmoji() : "😊");
            status.put("mood", user.getStatusMood() != null ? user.getStatusMood() : "happy");
            status.put("isBusy", user.getIsBusy() != null ? user.getIsBusy() : false);
            status.put("busyAutoReply", user.getBusyAutoReply() != null ? user.getBusyAutoReply() : "");
            status.put("busyEndTime", user.getBusyEndTime() != null ? user.getBusyEndTime().toString() : null);
        } else {
            status.put("emoji", "😊");
            status.put("mood", "happy");
            status.put("isBusy", false);
            status.put("busyAutoReply", "");
            status.put("busyEndTime", null);
        }

        return Result.success(status);
    }

    @PutMapping("/me/status")
    public Result<Map<String, Object>> updateUserStatus(@RequestBody Map<String, Object> statusRequest) {
        Long currentUserId = 1L;

        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) {
            return Result.fail(40401, "用户不存在");
        }

        if (statusRequest.containsKey("emoji")) {
            user.setStatusEmoji((String) statusRequest.get("emoji"));
        }
        if (statusRequest.containsKey("mood")) {
            user.setStatusMood((String) statusRequest.get("mood"));
        }
        if (statusRequest.containsKey("isBusy")) {
            user.setIsBusy((Boolean) statusRequest.get("isBusy"));
        }
        if (statusRequest.containsKey("busyAutoReply")) {
            user.setBusyAutoReply((String) statusRequest.get("busyAutoReply"));
        }
        if (statusRequest.containsKey("busyEndTime")) {
            String endTimeStr = (String) statusRequest.get("busyEndTime");
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                try {
                    user.setBusyEndTime(OffsetDateTime.parse(endTimeStr));
                } catch (Exception e) {
                    user.setBusyEndTime(OffsetDateTime.now().plusHours(1));
                }
            }
        }

        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("emoji", user.getStatusEmoji());
        result.put("mood", user.getStatusMood());
        result.put("isBusy", user.getIsBusy());
        result.put("busyAutoReply", user.getBusyAutoReply());
        result.put("busyEndTime", user.getBusyEndTime() != null ? user.getBusyEndTime().toString() : null);

        return Result.success(result);
    }

    @GetMapping("/me/ranking")
    public Result<Map<String, Object>> getUserRanking(
            @RequestParam(defaultValue = "all") String track) {
        Long currentUserId = 1L;

        List<User> allUsers = userRepository.findRankingByTrack(track);
        int rank = 1;
        for (User u : allUsers) {
            if (u.getId().equals(currentUserId)) {
                break;
            }
            rank++;
        }

        User currentUser = userRepository.findById(currentUserId).orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("rank", rank);
        result.put("totalUsers", allUsers.size());
        result.put("points", currentUser != null ? currentUser.getPoints() : 0);
        result.put("track", currentUser != null ? currentUser.getTargetTrack() : "algo");
        result.put("percentile", Math.round((1 - (double) rank / allUsers.size()) * 100));

        return Result.success(result);
    }

    @GetMapping("/me/points-history")
    public Result<Map<String, Object>> getUserPointsHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = 1L;

        List<Map<String, Object>> allHistory = new ArrayList<>();
        String[] sources = {"题目奖励", "连续打卡", "成就奖励", "活动奖励", "每日签到"};
        Random random = new Random();

        for (int i = 0; i < 100; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", (long) (i + 1));
            entry.put("points", random.nextInt(30) + 5);
            entry.put("source", sources[random.nextInt(sources.length)]);
            entry.put("createdAt", OffsetDateTime.now().minusDays(random.nextInt(60)).toString());
            allHistory.add(entry);
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allHistory.size());
        List<Map<String, Object>> pageHistory = start < allHistory.size()
                ? allHistory.subList(start, end)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageHistory);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", allHistory.size());

        return Result.success(result);
    }

    @GetMapping("/me/created-problems")
    public Result<Map<String, Object>> getUserCreatedProblems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = 1L;

        List<Map<String, Object>> allProblems = new ArrayList<>();
        String[] types = {"single", "multi", "code", "fill"};
        String[] tracks = {"algo", "ds", "contest"};
        String[] names = {
                "二分查找变体", "滑动窗口进阶", "动态规划优化",
                "图论基础", "并查集应用", "最短路径模板",
                "贪心策略", "回溯剪枝", "数位DP入门"
        };
        Random random = new Random();

        for (int i = 0; i < 30; i++) {
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", (long) (i + 1));
            problem.put("name", names[random.nextInt(names.length)] + " #" + (i + 1));
            problem.put("type", types[random.nextInt(types.length)]);
            problem.put("track", tracks[random.nextInt(tracks.length)]);
            problem.put("rewardPoints", random.nextInt(30) + 10);
            problem.put("solvedCount", random.nextInt(100));
            problem.put("createdAt", OffsetDateTime.now().minusDays(random.nextInt(90)).toString());
            allProblems.add(problem);
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allProblems.size());
        List<Map<String, Object>> pageProblems = start < allProblems.size()
                ? allProblems.subList(start, end)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageProblems);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", allProblems.size());

        return Result.success(result);
    }

    @GetMapping("/me/solved-problems")
    public Result<Map<String, Object>> getUserSolvedProblems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = 1L;

        List<Map<String, Object>> allProblems = new ArrayList<>();
        String[] types = {"single", "multi", "code", "fill"};
        String[] tracks = {"algo", "ds", "contest"};
        String[] names = {
                "时间复杂度基础", "双指针技巧", "二分模板",
                "栈与括号匹配", "哈希冲突策略", "链表反转",
                "贪心选择判断", "区间调度", "差分数组应用"
        };
        Random random = new Random();

        for (int i = 0; i < 80; i++) {
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", (long) (i + 1));
            problem.put("name", names[random.nextInt(names.length)]);
            problem.put("type", types[random.nextInt(types.length)]);
            problem.put("track", tracks[random.nextInt(tracks.length)]);
            problem.put("stars", random.nextInt(4));
            problem.put("timeMs", random.nextInt(60000) + 5000);
            problem.put("solvedAt", OffsetDateTime.now().minusDays(random.nextInt(60)).toString());
            allProblems.add(problem);
        }

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allProblems.size());
        List<Map<String, Object>> pageProblems = start < allProblems.size()
                ? allProblems.subList(start, end)
                : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageProblems);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", allProblems.size());

        return Result.success(result);
    }
}
