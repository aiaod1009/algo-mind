package com.example.demo.service;

import com.example.demo.author.AuthorLevelProfile;
import com.example.demo.author.AuthorLevelTier;
import com.example.demo.entity.ForumComment;
import com.example.demo.entity.ForumPost;
import com.example.demo.entity.Level;
import com.example.demo.entity.User;
import com.example.demo.entity.UserAuthorLevelHistory;
import com.example.demo.repository.ForumPostRepository;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.QuestionAttemptRepository;
import com.example.demo.repository.UserAuthorLevelHistoryRepository;
import com.example.demo.repository.UserProblemRecordRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AuthorLevelService {

    private static final String STATUS_CORRECT = "CORRECT";
    private static final Pattern RANK_PATTERN = Pattern.compile("(\\d+)");

    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final LevelRepository levelRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final UserProblemRecordRepository userProblemRecordRepository;
    private final UserAuthorLevelHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public AuthorLevelService(UserRepository userRepository,
            ForumPostRepository forumPostRepository,
            LevelRepository levelRepository,
            QuestionAttemptRepository questionAttemptRepository,
            UserProblemRecordRepository userProblemRecordRepository,
            UserAuthorLevelHistoryRepository historyRepository,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.forumPostRepository = forumPostRepository;
        this.levelRepository = levelRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.userProblemRecordRepository = userProblemRecordRepository;
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuthorLevelProfile refreshAuthorLevel(Long userId,
            String triggerType,
            Long triggerRefId,
            String note) {
        if (userId == null) {
            return buildFallbackProfile(null);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return buildFallbackProfile(null);
        }

        ScoreSnapshot snapshot = calculateScore(userId);
        int previousScore = safeInt(user.getAuthorScore());
        String previousCode = StringUtils.hasText(user.getAuthorLevelCode()) ? user.getAuthorLevelCode() : "seed";

        int rawScore = snapshot.totalScore();
        int effectiveScore = Math.max(previousScore, rawScore);
        AuthorLevelTier nextTier = AuthorLevelTier.fromScore(effectiveScore);

        boolean shouldPersist = user.getAuthorScore() == null
                || !StringUtils.hasText(user.getAuthorLevelCode())
                || effectiveScore > previousScore
                || !Objects.equals(nextTier.getCode(), previousCode);

        if (shouldPersist) {
            user.setAuthorScore(effectiveScore);
            user.setAuthorLevelCode(nextTier.getCode());
            user.setAuthorLevelUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);

            if (effectiveScore > previousScore || !Objects.equals(nextTier.getCode(), previousCode)) {
                UserAuthorLevelHistory history = new UserAuthorLevelHistory();
                history.setUserId(userId);
                history.setTriggerType(defaultTriggerType(triggerType));
                history.setTriggerRefId(triggerRefId);
                history.setPreviousLevelCode(previousCode);
                history.setCurrentLevelCode(nextTier.getCode());
                history.setPreviousScore(previousScore);
                history.setCurrentScore(effectiveScore);
                history.setNote(note);
                history.setMetricsJson(writeMetricsJson(enrichBreakdown(snapshot.breakdown(), effectiveScore, rawScore)));
                historyRepository.save(history);
            }
        }

        AuthorLevelProfile profile = buildProfile(nextTier, effectiveScore,
                enrichBreakdown(snapshot.breakdown(), effectiveScore, rawScore));
        user.setAuthorLevelProfile(profile);
        return profile;
    }

    public AuthorLevelProfile attachProfile(User user) {
        if (user == null) {
            return buildFallbackProfile(null);
        }

        AuthorLevelTier tier = resolveTier(user.getAuthorLevelCode(), user.getAuthorScore(), null);
        int score = Math.max(safeInt(user.getAuthorScore()), tier.getMinScore());
        AuthorLevelProfile profile = buildProfile(tier, score, buildLightBreakdown(score));
        user.setAuthorLevelProfile(profile);
        return profile;
    }

    public void attachProfilesToPosts(List<ForumPost> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        Map<Long, User> userMap = loadUsers(posts.stream()
                .map(ForumPost::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        posts.forEach(post -> post.setAuthorLevelProfile(resolveProfile(userMap.get(post.getUserId()), post.getAuthorLevel())));
    }

    public void attachProfilesToComments(List<ForumComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        Map<Long, User> userMap = loadUsers(comments.stream()
                .map(ForumComment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        comments.forEach(comment -> comment.setAuthorLevelProfile(
                resolveProfile(userMap.get(comment.getUserId()), comment.getAuthorLevel())));
    }

    public Map<String, Object> getAuthorLevelView(Long userId) {
        AuthorLevelProfile profile = refreshAuthorLevel(userId, "PROFILE_VIEW", null, "查看作者等级");
        List<Map<String, Object>> history = historyRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toHistoryView)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profile", profile);
        result.put("history", history);
        return result;
    }

    public AuthorLevelProfile buildFallbackProfile(String rawLabel) {
        AuthorLevelTier tier = resolveTier(rawLabel, null, rawLabel);
        return buildProfile(tier, tier.getMinScore(), buildLightBreakdown(tier.getMinScore()));
    }

    private ScoreSnapshot calculateScore(Long userId) {
        int solvingScore = calculateSolvingScore(userId);
        int postingScore = calculatePostingScore(userId);
        int libraryScore = calculateLibraryScore(userId);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("total", solvingScore + postingScore + libraryScore);
        breakdown.put("solving", solvingBreakdown(userId, solvingScore));
        breakdown.put("posting", postingBreakdown(userId, postingScore));
        breakdown.put("questionBank", questionBankBreakdown(userId, libraryScore));

        return new ScoreSnapshot(solvingScore + postingScore + libraryScore, breakdown);
    }

    private int calculateSolvingScore(Long userId) {
        long solvedCount = questionAttemptRepository.countByUserIdAndLatestStatus(userId, STATUS_CORRECT);
        long firstTrySolved = questionAttemptRepository.countByUserIdAndLatestStatusAndSubmitCountLessThanEqual(
                userId,
                STATUS_CORRECT,
                1);
        long totalAttempts = userProblemRecordRepository.countByUserId(userId);
        long correctAttempts = userProblemRecordRepository.countByUserIdAndIsCorrect(userId, true);
        double accuracyRate = totalAttempts > 0 ? (double) correctAttempts / totalAttempts : 0D;

        int quantityScore = capped(solvedCount * 8D, 220);
        int qualityScore = capped(firstTrySolved * 10D, 120);
        int accuracyScore = capped(accuracyRate * 80D, 80);
        return quantityScore + qualityScore + accuracyScore;
    }

    private int calculatePostingScore(Long userId) {
        long postCount = forumPostRepository.countByUserId(userId);
        long totalLikes = safeLong(forumPostRepository.sumLikesByUserId(userId));
        long totalComments = safeLong(forumPostRepository.sumCommentsByUserId(userId));
        long highImpactPosts = forumPostRepository.countHighImpactByUserId(userId, 20, 8);

        int volumeScore = capped(postCount * 15D, 150);
        int engagementScore = capped(totalLikes * 0.55D + totalComments * 1.4D, 200);
        int impactScore = capped(highImpactPosts * 20D, 80);
        return volumeScore + engagementScore + impactScore;
    }

    private int calculateLibraryScore(Long userId) {
        long createdCount = levelRepository.countByCreatorId(userId);
        int rewardPoints = safeInt(levelRepository.sumRewardPointsByCreatorId(userId));

        List<Level> levels = levelRepository.findByCreatorId(userId);
        List<Long> levelIds = levels.stream().map(Level::getId).toList();

        long totalSolves = levelIds.isEmpty()
                ? 0
                : questionAttemptRepository.countByLevelIdInAndLatestStatus(levelIds, STATUS_CORRECT);
        long distinctSolvers = levelIds.isEmpty()
                ? 0
                : questionAttemptRepository.countDistinctUsersByLevelIdInAndLatestStatus(levelIds, STATUS_CORRECT);
        long firstTrySolvers = levelIds.isEmpty()
                ? 0
                : questionAttemptRepository.countByLevelIdInAndLatestStatusAndSubmitCountLessThanEqual(
                        levelIds,
                        STATUS_CORRECT,
                        1);

        int volumeScore = capped(createdCount * 30D, 160);
        int adoptionScore = capped(totalSolves * 8D + distinctSolvers * 5D, 200);
        int curationScore = capped(rewardPoints * 1.2D + firstTrySolvers * 3D, 90);
        return volumeScore + adoptionScore + curationScore;
    }

    private Map<String, Object> solvingBreakdown(Long userId, int totalScore) {
        long solvedCount = questionAttemptRepository.countByUserIdAndLatestStatus(userId, STATUS_CORRECT);
        long firstTrySolved = questionAttemptRepository.countByUserIdAndLatestStatusAndSubmitCountLessThanEqual(
                userId,
                STATUS_CORRECT,
                1);
        long totalAttempts = userProblemRecordRepository.countByUserId(userId);
        long correctAttempts = userProblemRecordRepository.countByUserIdAndIsCorrect(userId, true);
        double accuracyRate = totalAttempts > 0 ? (double) correctAttempts / totalAttempts : 0D;

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("solvedCount", solvedCount);
        metrics.put("firstTrySolved", firstTrySolved);
        metrics.put("accuracyRate", percent(accuracyRate));
        metrics.put("totalAttempts", totalAttempts);

        return component("做题成长", totalScore, metrics,
                "做题数量、首答质量和正确率越稳定，成长越快。");
    }

    private Map<String, Object> postingBreakdown(Long userId, int totalScore) {
        long postCount = forumPostRepository.countByUserId(userId);
        long totalLikes = safeLong(forumPostRepository.sumLikesByUserId(userId));
        long totalComments = safeLong(forumPostRepository.sumCommentsByUserId(userId));
        long highImpactPosts = forumPostRepository.countHighImpactByUserId(userId, 20, 8);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("postCount", postCount);
        metrics.put("receivedLikes", totalLikes);
        metrics.put("receivedComments", totalComments);
        metrics.put("highImpactPosts", highImpactPosts);

        return component("发帖影响力", totalScore, metrics,
                "帖子数量和互动质量共同决定社区影响力。");
    }

    private Map<String, Object> questionBankBreakdown(Long userId, int totalScore) {
        long createdCount = levelRepository.countByCreatorId(userId);
        int rewardPoints = safeInt(levelRepository.sumRewardPointsByCreatorId(userId));
        List<Level> levels = levelRepository.findByCreatorId(userId);
        List<Long> levelIds = levels.stream().map(Level::getId).toList();

        long totalSolves = levelIds.isEmpty()
                ? 0
                : questionAttemptRepository.countByLevelIdInAndLatestStatus(levelIds, STATUS_CORRECT);
        long distinctSolvers = levelIds.isEmpty()
                ? 0
                : questionAttemptRepository.countDistinctUsersByLevelIdInAndLatestStatus(levelIds, STATUS_CORRECT);
        long firstTrySolvers = levelIds.isEmpty()
                ? 0
                : questionAttemptRepository.countByLevelIdInAndLatestStatusAndSubmitCountLessThanEqual(
                        levelIds,
                        STATUS_CORRECT,
                        1);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("publishedCount", createdCount);
        metrics.put("rewardPoints", rewardPoints);
        metrics.put("solvedCount", totalSolves);
        metrics.put("distinctSolvers", distinctSolvers);
        metrics.put("firstTrySolvers", firstTrySolvers);

        return component("题库策展力", totalScore, metrics,
                "高质量题库越多人完成，你的作者等级提升越明显。");
    }

    private Map<String, Object> component(String label,
            int score,
            Map<String, Object> metrics,
            String tip) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("label", label);
        result.put("score", score);
        result.put("tip", tip);
        result.put("metrics", metrics);
        return result;
    }

    private Map<String, Object> enrichBreakdown(Map<String, Object> rawBreakdown, int effectiveScore, int rawScore) {
        Map<String, Object> breakdown = new LinkedHashMap<>(rawBreakdown);
        breakdown.put("total", effectiveScore);
        breakdown.put("rawTotal", rawScore);
        if (effectiveScore > rawScore) {
            breakdown.put("lockedBonus", effectiveScore - rawScore);
            breakdown.put("lockedTip", "作者等级采用累计升级制，已获得的成长值不会因短期波动回退。");
        }
        return breakdown;
    }

    private Map<String, Object> buildLightBreakdown(int score) {
        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("total", score);
        return breakdown;
    }

    private AuthorLevelProfile resolveProfile(User user, String rawLabel) {
        if (user == null) {
            return buildFallbackProfile(rawLabel);
        }
        return attachProfile(user);
    }

    private Map<Long, User> loadUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
    }

    private Map<String, Object> toHistoryView(UserAuthorLevelHistory history) {
        AuthorLevelTier previousTier = AuthorLevelTier.fromCode(history.getPreviousLevelCode());
        AuthorLevelTier currentTier = AuthorLevelTier.fromCode(history.getCurrentLevelCode());

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", history.getId());
        item.put("triggerType", history.getTriggerType());
        item.put("note", history.getNote());
        item.put("previousLevelCode", history.getPreviousLevelCode());
        item.put("currentLevelCode", history.getCurrentLevelCode());
        item.put("previousLevelLabel", previousTier.getShortLabel());
        item.put("currentLevelLabel", currentTier.getShortLabel());
        item.put("previousScore", history.getPreviousScore());
        item.put("currentScore", history.getCurrentScore());
        item.put("deltaScore", safeInt(history.getCurrentScore()) - safeInt(history.getPreviousScore()));
        item.put("createdAt", history.getCreatedAt());
        return item;
    }

    private AuthorLevelProfile buildProfile(AuthorLevelTier tier, int score, Map<String, Object> breakdown) {
        AuthorLevelTier nextTier = tier.next();
        int currentMaxScore = nextTier == null ? Math.max(score, tier.getMinScore()) : tier.getMaxScore();
        int progress = nextTier == null
                ? 100
                : Math.min(100, Math.max(0,
                        (int) Math.round((score - tier.getMinScore()) * 100.0
                                / Math.max(1, nextTier.getMinScore() - tier.getMinScore()))));

        return AuthorLevelProfile.builder()
                .code(tier.getCode())
                .rank(tier.getRank())
                .title(tier.getTitle())
                .shortLabel(tier.getShortLabel())
                .theme(tier.getTheme())
                .description(tier.getDescription())
                .score(score)
                .currentLevelMinScore(tier.getMinScore())
                .currentLevelMaxScore(currentMaxScore)
                .nextLevelMinScore(nextTier == null ? null : nextTier.getMinScore())
                .progressPercent(progress)
                .colors(buildThemeColors(tier))
                .effects(buildThemeEffects(tier))
                .breakdown(breakdown)
                .build();
    }

    private AuthorLevelTier resolveTier(String levelCode, Integer score, String rawLabel) {
        if (StringUtils.hasText(levelCode) && isTierCode(levelCode)) {
            return AuthorLevelTier.fromCode(levelCode);
        }
        if (score != null && score > 0) {
            return AuthorLevelTier.fromScore(score);
        }
        if (StringUtils.hasText(rawLabel)) {
            if (isTierCode(rawLabel)) {
                return AuthorLevelTier.fromCode(rawLabel);
            }
            Matcher matcher = RANK_PATTERN.matcher(rawLabel);
            if (matcher.find()) {
                int rank = Integer.parseInt(matcher.group(1));
                return switch (Math.max(1, Math.min(rank, 7))) {
                    case 1 -> AuthorLevelTier.SEED;
                    case 2 -> AuthorLevelTier.FLARE;
                    case 3 -> AuthorLevelTier.TIDE;
                    case 4 -> AuthorLevelTier.AURORA;
                    case 5 -> AuthorLevelTier.SOLAR;
                    case 6 -> AuthorLevelTier.NEBULA;
                    default -> AuthorLevelTier.CROWN;
                };
            }
        }
        return AuthorLevelTier.SEED;
    }

    private boolean isTierCode(String value) {
        return value != null && switch (value.trim().toLowerCase()) {
            case "seed", "flare", "tide", "aurora", "solar", "nebula", "crown" -> true;
            default -> false;
        };
    }

    private Map<String, Object> buildThemeColors(AuthorLevelTier tier) {
        Map<String, Object> colors = new LinkedHashMap<>();
        switch (tier) {
            case SEED -> {
                colors.put("primary", "#2f9e44");
                colors.put("secondary", "#74c69d");
                colors.put("accent", "#d8f3dc");
                colors.put("text", "#1b4332");
                colors.put("border", "rgba(47,158,68,0.28)");
                colors.put("surface", "linear-gradient(135deg, rgba(216,243,220,0.95) 0%, rgba(116,198,157,0.18) 100%)");
                colors.put("glow", "0 0 20px rgba(47,158,68,0.18)");
            }
            case FLARE -> {
                colors.put("primary", "#f76707");
                colors.put("secondary", "#ff922b");
                colors.put("accent", "#fff4e6");
                colors.put("text", "#7f2704");
                colors.put("border", "rgba(247,103,7,0.25)");
                colors.put("surface", "linear-gradient(135deg, rgba(255,244,230,0.98) 0%, rgba(255,146,43,0.2) 100%)");
                colors.put("glow", "0 0 24px rgba(247,103,7,0.22)");
            }
            case TIDE -> {
                colors.put("primary", "#0c8599");
                colors.put("secondary", "#3bc9db");
                colors.put("accent", "#e3fafc");
                colors.put("text", "#083344");
                colors.put("border", "rgba(12,133,153,0.24)");
                colors.put("surface", "linear-gradient(135deg, rgba(227,250,252,0.98) 0%, rgba(59,201,219,0.2) 100%)");
                colors.put("glow", "0 0 22px rgba(12,133,153,0.18)");
            }
            case AURORA -> {
                colors.put("primary", "#7c3aed");
                colors.put("secondary", "#06b6d4");
                colors.put("accent", "#f5f3ff");
                colors.put("text", "#312e81");
                colors.put("border", "rgba(124,58,237,0.24)");
                colors.put("surface", "linear-gradient(135deg, rgba(245,243,255,0.98) 0%, rgba(6,182,212,0.18) 52%, rgba(124,58,237,0.2) 100%)");
                colors.put("glow", "0 0 26px rgba(124,58,237,0.22)");
            }
            case SOLAR -> {
                colors.put("primary", "#d97706");
                colors.put("secondary", "#fbbf24");
                colors.put("accent", "#fffbeb");
                colors.put("text", "#78350f");
                colors.put("border", "rgba(217,119,6,0.26)");
                colors.put("surface", "linear-gradient(135deg, rgba(255,251,235,0.98) 0%, rgba(251,191,36,0.22) 100%)");
                colors.put("glow", "0 0 26px rgba(217,119,6,0.24)");
            }
            case NEBULA -> {
                colors.put("primary", "#4338ca");
                colors.put("secondary", "#db2777");
                colors.put("accent", "#eef2ff");
                colors.put("text", "#1e1b4b");
                colors.put("border", "rgba(67,56,202,0.24)");
                colors.put("surface", "linear-gradient(135deg, rgba(238,242,255,0.98) 0%, rgba(219,39,119,0.18) 50%, rgba(67,56,202,0.22) 100%)");
                colors.put("glow", "0 0 28px rgba(67,56,202,0.24)");
            }
            case CROWN -> {
                colors.put("primary", "#991b1b");
                colors.put("secondary", "#f59e0b");
                colors.put("accent", "#fff7ed");
                colors.put("text", "#450a0a");
                colors.put("border", "rgba(153,27,27,0.3)");
                colors.put("surface", "linear-gradient(135deg, rgba(255,247,237,0.98) 0%, rgba(245,158,11,0.25) 45%, rgba(153,27,27,0.18) 100%)");
                colors.put("glow", "0 0 30px rgba(153,27,27,0.24)");
            }
        }
        return colors;
    }

    private Map<String, Object> buildThemeEffects(AuthorLevelTier tier) {
        Map<String, Object> effects = new LinkedHashMap<>();
        switch (tier) {
            case SEED -> {
                effects.put("animation", "seed-sway");
                effects.put("halo", "leaf-halo");
                effects.put("particle", "sprout");
                effects.put("shine", "soft-dew");
            }
            case FLARE -> {
                effects.put("animation", "flare-flicker");
                effects.put("halo", "ember-ring");
                effects.put("particle", "ember");
                effects.put("shine", "heat-sweep");
            }
            case TIDE -> {
                effects.put("animation", "tide-ripple");
                effects.put("halo", "wave-ring");
                effects.put("particle", "foam");
                effects.put("shine", "waterline");
            }
            case AURORA -> {
                effects.put("animation", "aurora-drift");
                effects.put("halo", "polar-veil");
                effects.put("particle", "light-ribbon");
                effects.put("shine", "spectrum-sweep");
            }
            case SOLAR -> {
                effects.put("animation", "solar-pulse");
                effects.put("halo", "sunburst");
                effects.put("particle", "spark");
                effects.put("shine", "golden-arc");
            }
            case NEBULA -> {
                effects.put("animation", "nebula-float");
                effects.put("halo", "cosmic-ring");
                effects.put("particle", "stardust");
                effects.put("shine", "galaxy-trail");
            }
            case CROWN -> {
                effects.put("animation", "crown-rise");
                effects.put("halo", "royal-ray");
                effects.put("particle", "crown-shard");
                effects.put("shine", "victory-sweep");
            }
        }
        return effects;
    }

    private String defaultTriggerType(String triggerType) {
        return StringUtils.hasText(triggerType) ? triggerType : "MANUAL_REFRESH";
    }

    private String writeMetricsJson(Map<String, Object> metrics) {
        try {
            return objectMapper.writeValueAsString(metrics);
        } catch (JsonProcessingException ignored) {
            return "{}";
        }
    }

    private int capped(double value, int max) {
        return Math.min(max, Math.max(0, (int) Math.round(value)));
    }

    private int percent(double ratio) {
        return (int) Math.round(Math.max(0D, Math.min(1D, ratio)) * 100D);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private record ScoreSnapshot(int totalScore, Map<String, Object> breakdown) {
    }
}
