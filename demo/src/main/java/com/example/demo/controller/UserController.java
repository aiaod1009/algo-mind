package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.entity.Level;
import com.example.demo.entity.QuestionAttempt;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProblemRecord;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.QuestionAttemptRepository;
import com.example.demo.repository.UserProblemRecordRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final String MANAGED_AVATAR_PREFIX = "/api/uploads/avatars/";
    private static final String LEGACY_MANAGED_AVATAR_PREFIX = "/uploads/avatars/";
    private static final String STATUS_CORRECT = "CORRECT";
    private final UserRepository userRepository;
    private final LevelRepository levelRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final UserProblemRecordRepository userProblemRecordRepository;
    private final CurrentUserService currentUserService;
    private static final List<String> VALID_TRACKS = List.of("algo", "ds", "contest");

    public UserController(UserRepository userRepository,
            LevelRepository levelRepository,
            QuestionAttemptRepository questionAttemptRepository,
            UserProblemRecordRepository userProblemRecordRepository,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.levelRepository = levelRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.userProblemRecordRepository = userProblemRecordRepository;
        this.currentUserService = currentUserService;
    }

    @PutMapping("/me")
    public Result<User> updateUserProfile(@RequestBody User userRequest) {
        User user = currentUserService.requireCurrentUserEntity();

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
            String requestedAvatar = normalizeAvatarInput(userRequest.getAvatar());
            if (requestedAvatar == null || requestedAvatar.isEmpty()) {
                user.setAvatar(null);
            } else {
                String managedAvatar = normalizeManagedAvatarPath(requestedAvatar);
                if (managedAvatar != null) {
                    user.setAvatar(managedAvatar);
                } else if (requestedAvatar.equals(user.getAvatar())) {
                    user.setAvatar(user.getAvatar());
                } else {
                    return Result.fail(40001, "头像仅支持站内已上传图片，请先上传本地图片文件");
                }
            }
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
        Long currentUserId = currentUserService.requireCurrentUserId();

        LocalDate startOfYear = LocalDate.of(targetYear, 1, 1);
        LocalDate endExclusive = startOfYear.plusYears(1);
        OffsetDateTime from = startOfYear.atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        OffsetDateTime to = endExclusive.atStartOfDay().atOffset(ZoneOffset.ofHours(8));

        List<Object[]> rows = userProblemRecordRepository.countDailyAttempts(currentUserId, from, to);
        Map<LocalDate, Integer> dailyCountMap = new HashMap<>();
        List<Map<String, Object>> records = new ArrayList<>();
        int totalCount = 0;

        for (Object[] row : rows) {
            LocalDate date = LocalDate.parse(String.valueOf(row[0]));
            int count = ((Number) row[1]).intValue();
            dailyCountMap.put(date, count);
            totalCount += count;

            Map<String, Object> entry = new HashMap<>();
            entry.put("date", date.toString());
            entry.put("count", count);
            entry.put("level", toHeatLevel(count));
            records.add(entry);
        }

        int activeDays = dailyCountMap.size();
        int longestStreak = calculateLongestStreak(dailyCountMap.keySet(), startOfYear, endExclusive.minusDays(1));

        LocalDate today = LocalDate.now();
        int currentStreak = targetYear == today.getYear()
                ? calculateCurrentStreak(dailyCountMap.keySet(), today)
                : 0;

        int monthlyActiveDays = 0;
        int monthlyTotalCount = 0;
        if (targetYear == today.getYear()) {
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            for (LocalDate date = firstDayOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
                Integer cnt = dailyCountMap.get(date);
                if (cnt != null && cnt > 0) {
                    monthlyActiveDays++;
                    monthlyTotalCount += cnt;
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
        Long currentUserId = currentUserService.requireCurrentUserId();

        User user = userRepository.findById(currentUserId).orElse(null);
        LocalDate today = LocalDate.now();
        OffsetDateTime todayStart = today.atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        OffsetDateTime tomorrowStart = today.plusDays(1).atStartOfDay().atOffset(ZoneOffset.ofHours(8));

        LocalDate weekStartDate = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        OffsetDateTime weekStart = weekStartDate.atStartOfDay().atOffset(ZoneOffset.ofHours(8));

        LocalDate monthStartDate = today.withDayOfMonth(1);
        OffsetDateTime monthStart = monthStartDate.atStartOfDay().atOffset(ZoneOffset.ofHours(8));

        long totalSolved = questionAttemptRepository.countByUserIdAndLatestStatus(currentUserId, STATUS_CORRECT);
        long totalCreated = levelRepository.countByCreatorId(currentUserId);
        long todaySolved = userProblemRecordRepository.countByUserIdAndSolvedAtBetween(currentUserId, todayStart,
                tomorrowStart);
        long weeklySolved = userProblemRecordRepository.countByUserIdAndSolvedAtBetween(currentUserId, weekStart,
                tomorrowStart);
        long monthlySolved = userProblemRecordRepository.countByUserIdAndSolvedAtBetween(currentUserId, monthStart,
                tomorrowStart);

        int weeklyGoal = user != null && user.getWeeklyGoal() != null ? user.getWeeklyGoal() : 10;

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalProblems", totalSolved);
        stats.put("solvedProblems", totalSolved);
        stats.put("createdProblems", totalCreated);
        stats.put("totalPoints", user != null ? user.getPoints() : 0);
        stats.put("accuracy", 0.0);
        stats.put("weeklyGoal", weeklyGoal);
        stats.put("weeklyProgress", Math.min(weeklyGoal, (int) weeklySolved));

        stats.put("totalSolved", totalSolved);
        stats.put("totalCreated", totalCreated);
        stats.put("todaySolved", todaySolved);
        stats.put("weeklySolved", weeklySolved);
        stats.put("monthlySolved", monthlySolved);

        return Result.success(stats);
    }

    @GetMapping("/me/activities")
    public Result<Map<String, Object>> getUserActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = currentUserService.requireCurrentUserId();

        Page<UserProblemRecord> activityPage = userProblemRecordRepository.findByUserIdOrderBySolvedAtDesc(
                currentUserId,
                PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1)));

        List<Map<String, Object>> pageActivities = new ArrayList<>();
        for (UserProblemRecord record : activityPage.getContent()) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", record.getId());
            activity.put("type", Boolean.TRUE.equals(record.getIsCorrect()) ? "solve" : "retry");
            activity.put("description", buildActivityDescription(record));
            activity.put("points", Boolean.TRUE.equals(record.getIsCorrect()) ? 1 : 0);
            activity.put("createdAt", record.getSolvedAt() != null ? record.getSolvedAt().toString() : null);
            pageActivities.add(activity);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageActivities);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", activityPage.getTotalElements());

        return Result.success(result);
    }

    @GetMapping("/me/status")
    public Result<Map<String, Object>> getUserStatus() {
        Long currentUserId = currentUserService.requireCurrentUserId();

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
        Long currentUserId = currentUserService.requireCurrentUserId();

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
        Long currentUserId = currentUserService.requireCurrentUserId();

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

        Long currentUserId = currentUserService.requireCurrentUserId();

        Page<UserProblemRecord> historyPage = userProblemRecordRepository.findByUserIdOrderBySolvedAtDesc(
                currentUserId,
                PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1)));

        Map<Long, Integer> rewardCache = new HashMap<>();
        List<Map<String, Object>> pageHistory = new ArrayList<>();
        for (UserProblemRecord record : historyPage.getContent()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", record.getId());
            int points = resolveRewardPoints(record, rewardCache);
            entry.put("points", points);
            entry.put("source", Boolean.TRUE.equals(record.getIsCorrect()) ? "题目奖励" : "提交记录");
            entry.put("createdAt", record.getSolvedAt() != null ? record.getSolvedAt().toString() : null);
            pageHistory.add(entry);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageHistory);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", historyPage.getTotalElements());

        return Result.success(result);
    }

    @GetMapping("/me/created-problems")
    public Result<Map<String, Object>> getUserCreatedProblems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long currentUserId = currentUserService.requireCurrentUserId();

        Page<Level> createdPage = levelRepository.findByCreatorIdOrderByIdDesc(
                currentUserId,
                PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1)));

        List<Map<String, Object>> pageProblems = new ArrayList<>();
        for (Level level : createdPage.getContent()) {
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", level.getId());
            problem.put("name", level.getName());
            problem.put("description", level.getDescription() == null ? level.getQuestion() : level.getDescription());
            problem.put("type", level.getType());
            problem.put("track", level.getTrack());
            problem.put("rewardPoints", level.getRewardPoints() == null ? 0 : level.getRewardPoints());
            problem.put("isPublic", true);
            problem.put("createdAt", null);
            problem.put("solvedCount",
                    questionAttemptRepository.countByLevelIdAndLatestStatus(level.getId(), STATUS_CORRECT));
            pageProblems.add(problem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageProblems);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", createdPage.getTotalElements());

        return Result.success(result);
    }

    @GetMapping("/me/solved-problems")
    public Result<Map<String, Object>> getUserSolvedProblems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = currentUserService.requireCurrentUserId();

        Page<QuestionAttempt> solvedPage = questionAttemptRepository
                .findByUserIdAndLatestStatusOrderByLastSubmittedAtDesc(
                        currentUserId,
                        STATUS_CORRECT,
                        PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1)));

        List<Map<String, Object>> pageProblems = new ArrayList<>();
        for (QuestionAttempt attempt : solvedPage.getContent()) {
            Map<String, Object> problem = new HashMap<>();
            problem.put("id", attempt.getLevelId());
            problem.put("name", attempt.getQuestionTitle());
            problem.put("type", attempt.getLevelType());
            problem.put("track", resolveTrackByLevelId(attempt.getLevelId()));
            problem.put("stars", 1);
            problem.put("timeMs", null);
            problem.put("solvedAt", attempt.getLastSubmittedAt() == null ? null
                    : attempt.getLastSubmittedAt().atOffset(ZoneOffset.ofHours(8)).toString());
            problem.put("attempts", attempt.getSubmitCount() == null ? 0 : attempt.getSubmitCount());
            pageProblems.add(problem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageProblems);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", solvedPage.getTotalElements());

        return Result.success(result);
    }

    private Integer resolveRewardPoints(UserProblemRecord record, Map<Long, Integer> rewardCache) {
        if (!Boolean.TRUE.equals(record.getIsCorrect())) {
            return 0;
        }
        if (record.getLevelId() == null) {
            return 0;
        }
        return rewardCache.computeIfAbsent(record.getLevelId(), levelId -> levelRepository.findById(levelId)
                .map(level -> Math.max(0, level.getRewardPoints() == null ? 0 : level.getRewardPoints()))
                .orElse(0));
    }

    private String resolveTrackByLevelId(Long levelId) {
        if (levelId == null) {
            return "algo";
        }
        return levelRepository.findById(levelId)
                .map(Level::getTrack)
                .orElse("algo");
    }

    private String buildActivityDescription(UserProblemRecord record) {
        String levelName = record.getLevelName() == null || record.getLevelName().isBlank()
                ? "未命名题目"
                : record.getLevelName();
        if (Boolean.TRUE.equals(record.getIsCorrect())) {
            return "完成了题目「" + levelName + "」";
        }
        return "尝试了题目「" + levelName + "」";
    }

    private int toHeatLevel(int count) {
        if (count <= 0)
            return 0;
        if (count <= 2)
            return 1;
        if (count <= 4)
            return 2;
        if (count <= 7)
            return 3;
        return 4;
    }

    private int calculateCurrentStreak(Set<LocalDate> activeDates, LocalDate fromDate) {
        int streak = 0;
        LocalDate cursor = fromDate;
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int calculateLongestStreak(Set<LocalDate> activeDates, LocalDate start, LocalDate end) {
        int best = 0;
        int current = 0;
        for (LocalDate cursor = start; !cursor.isAfter(end); cursor = cursor.plusDays(1)) {
            if (activeDates.contains(cursor)) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 0;
            }
        }
        return best;
    }

    private String normalizeAvatarInput(String avatar) {
        if (avatar == null) {
            return null;
        }
        return avatar.trim();
    }

    private String normalizeManagedAvatarPath(String avatar) {
        String normalized = normalizeAvatarInput(avatar);
        if (normalized == null || normalized.isEmpty()) {
            return normalized;
        }

        if (normalized.startsWith(MANAGED_AVATAR_PREFIX)) {
            return normalized;
        }

        if (normalized.startsWith(LEGACY_MANAGED_AVATAR_PREFIX)) {
            return MANAGED_AVATAR_PREFIX + normalized.substring(LEGACY_MANAGED_AVATAR_PREFIX.length());
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            try {
                URI uri = URI.create(normalized);
                return normalizeManagedAvatarPath(uri.getPath());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }

        return null;
    }
}
