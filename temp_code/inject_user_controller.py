# -*- coding: utf-8 -*-
with open('src/main/java/com/example/demo/controller/UserController.java', 'r', encoding='utf-8') as f:
    text = f.read()

# 1. Imports
imports = '''import com.example.demo.repository.UserFollowRepository;
import com.example.demo.entity.UserFollow;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
'''
text = text.replace('import org.springframework.web.bind.annotation.*;\n', 'import org.springframework.web.bind.annotation.*;\n' + imports)

# 2. Injection
inj = '''    @Autowired
    private UserProblemRecordRepository userProblemRecordRepository;

    @Autowired
    private UserFollowRepository userFollowRepository;
'''
text = text.replace('    @Autowired\n    private UserProblemRecordRepository userProblemRecordRepository;\n', inj)

# 3. Add to /me/stats
stats_add = '''
        long followingCount = userFollowRepository.countByFollowerId(currentUserId);
        long followerCount = userFollowRepository.countByFollowingId(currentUserId);

        Map<String, Object> stats = new HashMap<>();
'''
text = text.replace('        Map<String, Object> stats = new HashMap<>();', stats_add)

stats_put = '''        stats.put("totalPoints", user != null ? user.getPoints() : 0);
        stats.put("followingCount", followingCount);
        stats.put("followerCount", followerCount);'''
text = text.replace('        stats.put("totalPoints", user != null ? user.getPoints() : 0);', stats_put)

# 4. Add endpoints:
endpoints = '''
    @PostMapping("/{id}/follow")
    @Transactional
    public Result<Map<String, Object>> toggleFollow(@PathVariable Long id) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (currentUserId.equals(id)) {
            return Result.fail(40001, "不能关注自己");
        }
        User target = userRepository.findById(id).orElse(null);
        if (target == null) return Result.fail(40401, "用户不存在");

        boolean isFollowing = userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, id);
        if (isFollowing) {
            userFollowRepository.deleteByFollowerIdAndFollowingId(currentUserId, id);
            isFollowing = false;
        } else {
            UserFollow uf = new UserFollow();
            uf.setFollowerId(currentUserId);
            uf.setFollowingId(id);
            userFollowRepository.save(uf);
            isFollowing = true;
        }

        long followingCount = userFollowRepository.countByFollowerId(id);
        long followerCount = userFollowRepository.countByFollowingId(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        result.put("followerCount", followerCount);
        result.put("followingCount", followingCount);
        return Result.success(result);
    }

    @GetMapping("/{id}/profile")
    public Result<Map<String, Object>> getUserProfile(@PathVariable Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Result.fail(40401, "用户不存在");
        }
        
        user.setAuthorLevelProfile(authorLevelService.attachProfile(user));

        long totalSolved = questionAttemptRepository.countByUserIdAndLatestStatus(id, STATUS_CORRECT);
        long totalCreated = levelRepository.countByCreatorId(id);

        long followingCount = userFollowRepository.countByFollowerId(id);
        long followerCount = userFollowRepository.countByFollowingId(id);
        boolean isFollowing = false;

        if (currentUserId != null && !currentUserId.equals(id)) {
            isFollowing = userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, id);
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("avatar", user.getAvatar());
        profile.put("bio", user.getBio());
        profile.put("points", user.getPoints());
        profile.put("authorLevelProfile", user.getAuthorLevelProfile());
        profile.put("totalSolved", totalSolved);
        profile.put("totalCreated", totalCreated);
        profile.put("followingCount", followingCount);
        profile.put("followerCount", followerCount);
        profile.put("isFollowing", isFollowing);
        
        return Result.success(profile);
    }

    @GetMapping("/{id}/heatmap")
    public Result<Map<String, Object>> getUserHeatmapById(
            @PathVariable Long id,
            @RequestParam(required = false) Integer year) {
        
        int targetYear = year != null ? year : LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(targetYear, 1, 1);
        LocalDate endExclusive = LocalDate.of(targetYear + 1, 1, 1);

        OffsetDateTime start = startOfYear.atStartOfDay().atOffset(ZoneOffset.ofHours(8));
        OffsetDateTime end = endExclusive.atStartOfDay().atOffset(ZoneOffset.ofHours(8));

        List<Object[]> dailyCounts = userProblemRecordRepository.countDailyAttempts(id, start, end);

        List<Map<String, Object>> records = new ArrayList<>();
        Map<LocalDate, Integer> dailyCountMap = new HashMap<>();
        int totalCount = 0;

        for (Object[] row : dailyCounts) {
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

        LocalDate currentDate = startOfYear;
        LocalDate endDate = endExclusive.minusDays(1);
        
        while (!currentDate.isAfter(endDate)) {
            if (!dailyCountMap.containsKey(currentDate) && currentDate.isBefore(LocalDate.now().plusDays(1))) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("date", currentDate.toString());
                entry.put("count", 0);
                entry.put("level", 0);
                records.add(entry);
            }
            currentDate = currentDate.plusDays(1);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalActivity", totalCount);
        return Result.success(result);
    }

    @GetMapping("/{id}/activities")
    public Result<Page<UserProblemRecord>> getUserActivitiesById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Page in Spring Data JPA is 0-indexed
        int pageIndex = page > 0 ? page - 1 : 0;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<UserProblemRecord> records = userProblemRecordRepository.findByUserId(id, pageRequest);
        return Result.success(records);
    }

    @GetMapping("/{id}/solved-problems")
    public Result<Page<QuestionAttempt>> getUserSolvedProblemsById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        int pageIndex = page > 0 ? page - 1 : 0;
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<QuestionAttempt> attempts = questionAttemptRepository.findByUserIdAndLatestStatus(id, STATUS_CORRECT, pageRequest);
        
        // Populate level names
        attempts.getContent().forEach(attempt -> {
            levelRepository.findById(attempt.getLevelId()).ifPresent(level -> {
                attempt.setLevelName(level.getName());
            });
        });
        
        return Result.success(attempts);
    }
'''
text = text.replace('public class UserController {', 'public class UserController {\n' + endpoints)

with open('src/main/java/com/example/demo/controller/UserController.java', 'w', encoding='utf-8') as f:
    f.write(text)

