const fs = require('fs');
const content = fs.readFileSync('src/main/java/com/example/demo/controller/UserController.java', 'utf-8');

let newContent = content;

if (!newContent.includes('UserFollowRepository')) {
    newContent = newContent.replace('import org.springframework.web.bind.annotation.*;', 
        'import org.springframework.web.bind.annotation.*;\\n' +
        'import com.example.demo.repository.UserFollowRepository;\\n' +
        'import com.example.demo.entity.UserFollow;\\n' +
        'import org.springframework.transaction.annotation.Transactional;\\n' +
        'import org.springframework.data.domain.Page;\\n' +
        'import org.springframework.data.domain.PageRequest;\\n' +
        'import org.springframework.data.domain.Sort;\\n' +
        'import java.time.LocalDate;\\n' +
        'import java.time.OffsetDateTime;\\n' +
        'import java.time.ZoneOffset;\\n' +
        'import java.util.HashMap;\\n' +
        'import java.util.ArrayList;\\n' +
        'import java.util.Map;\\n' +
        'import java.util.List;\\n' +
        'import com.example.demo.entity.User;\\n'
    );
}

if (!newContent.includes('userFollowRepository;')) {
    newContent = newContent.replace('private UserProblemRecordRepository userProblemRecordRepository;',
        'private UserProblemRecordRepository userProblemRecordRepository;\\n\\n' +
        '    @Autowired\\n' +
        '    private UserFollowRepository userFollowRepository;\\n'
    );
}

const endpoints = \
    private static final String STATUS_CORRECT = "correct";

    private long countByFollowerId(Long id) {
        if(userFollowRepository == null) return 0;
        return userFollowRepository.countByFollowerId(id);
    }
    private long countByFollowingId(Long id) {
        if(userFollowRepository == null) return 0;
        return userFollowRepository.countByFollowingId(id);
    }

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

        long followingCount = countByFollowerId(id);
        long followerCount = countByFollowingId(id);
        
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

        long followingCount = countByFollowerId(id);
        long followerCount = countByFollowingId(id);
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
\;

if (!newContent.includes('toggleFollow')) {
    newContent = newContent.replace('public class UserController {', 'public class UserController {\\n' + endpoints);
}

fs.writeFileSync('src/main/java/com/example/demo/controller/UserController.java', newContent, 'utf-8');
console.log("Patched successfully.");
