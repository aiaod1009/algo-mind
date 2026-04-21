# -*- coding: utf-8 -*-
with open('src/main/java/com/example/demo/controller/UserController.java', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if 'import java.util.*;' in line:
        new_lines.append(line)
        new_lines.append('import com.example.demo.repository.UserFollowRepository;\n')
        new_lines.append('import com.example.demo.entity.UserFollow;\n')
        new_lines.append('import org.springframework.transaction.annotation.Transactional;\n')
        new_lines.append('import org.springframework.data.domain.Sort;\n')
        continue
    
    if '@Autowired' in line and 'private UserProblemRecordRepository userProblemRecordRepository;' in lines[lines.index(line)+1]:
        new_lines.append(line)
        new_lines.append('    private UserProblemRecordRepository userProblemRecordRepository;\n\n')
        new_lines.append('    @Autowired\n')
        new_lines.append('    private UserFollowRepository userFollowRepository;\n')
        lines[lines.index(line)+1] = '' # Clear the original
        continue
        
    if 'Map<String, Object> stats = new HashMap<>();' in line:
        new_lines.append('        long followingCount = userFollowRepository.countByFollowerId(currentUserId);\n')
        new_lines.append('        long followerCount = userFollowRepository.countByFollowingId(currentUserId);\n\n')
        new_lines.append(line)
        continue

    if 'stats.put("totalPoints", user != null ? user.getPoints() : 0);' in line:
        new_lines.append(line)
        new_lines.append('        stats.put("followingCount", followingCount);\n')
        new_lines.append('        stats.put("followerCount", followerCount);\n')
        continue

    if 'public class UserController {' in line:
        new_lines.append(line)
        new_lines.append('''
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
        
        attempts.getContent().forEach(attempt -> {
            levelRepository.findById(attempt.getLevelId()).ifPresent(level -> {
                attempt.setLevelName(level.getName());
            });
        });
        
        return Result.success(attempts);
    }
''')
        continue
        
    new_lines.append(line)

with open('src/main/java/com/example/demo/controller/UserController.java', 'w', encoding='utf-8') as f:
    for line in new_lines:
        f.write(line)
