package com.example.demo.service;

import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.SubmitAnswerRequest;
import com.example.demo.dto.SubmitAnswerResponse;
import com.example.demo.entity.ErrorItem;
import com.example.demo.entity.ErrorItemOptionSnapshot;
import com.example.demo.entity.Level;
import com.example.demo.entity.QuestionAttempt;
import com.example.demo.entity.User;
import com.example.demo.entity.UserProblemRecord;
import com.example.demo.repository.ErrorItemRepository;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.QuestionAttemptRepository;
import com.example.demo.repository.UserProblemRecordRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class LevelSubmissionService {

    private static final String STATUS_CORRECT = "CORRECT";
    private static final String STATUS_WRONG = "WRONG";

    private final LevelRepository levelRepository;
    private final ErrorItemRepository errorItemRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final UserProblemRecordRepository userProblemRecordRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public LevelSubmissionService(LevelRepository levelRepository,
            ErrorItemRepository errorItemRepository,
            QuestionAttemptRepository questionAttemptRepository,
            UserProblemRecordRepository userProblemRecordRepository,
            CurrentUserService currentUserService,
            UserRepository userRepository) {
        this.levelRepository = levelRepository;
        this.errorItemRepository = errorItemRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.userProblemRecordRepository = userProblemRecordRepository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @Transactional
    public SubmitAnswerResponse submit(SubmitAnswerRequest request) {
        if (request == null || request.getLevelId() == null) {
            throw new IllegalArgumentException("levelId不能为空");
        }
        if (request.getAnswer() == null) {
            throw new IllegalArgumentException("answer不能为空");
        }

        Long userId = currentUserService.requireCurrentUserId();
        Level level = levelRepository.findById(request.getLevelId())
                .orElseThrow(() -> new NoSuchElementException("关卡不存在: " + request.getLevelId()));

        List<String> levelOptions = safeOptions(level.getOptions());
        String rawUserAnswer = stringifyAnswer(request.getAnswer());
        String normalizedUserAnswer = normalizeAnswer(rawUserAnswer, levelOptions);
        String normalizedCorrectAnswer = normalizeAnswer(defaultString(level.getAnswer()), levelOptions);
        boolean correct = Objects.equals(normalizedUserAnswer, normalizedCorrectAnswer);

        QuestionAttempt attempt = questionAttemptRepository.findByUserIdAndLevelId(userId, level.getId())
                .orElseGet(QuestionAttempt::new);
        boolean alreadySolved = STATUS_CORRECT.equalsIgnoreCase(attempt.getLatestStatus());
        updateAttempt(attempt, userId, level, rawUserAnswer, correct);
        questionAttemptRepository.save(attempt);
        saveProblemRecord(userId, level, correct, request, attempt.getSubmitCount());

        boolean nextLevelUnlocked = unlockNextLevel(level);
        int pointsEarned = 0;
        Long errorItemId = null;

        if (correct) {
            if (!alreadySolved) {
                pointsEarned = increaseUserPoints(userId, level.getRewardPoints());
            }
            errorItemRepository.deleteByUserIdAndLevelId(userId, level.getId());
        } else {
            ErrorItem errorItem = upsertErrorItem(userId, level, rawUserAnswer, normalizedUserAnswer,
                    normalizedCorrectAnswer, levelOptions);
            errorItemId = errorItem.getId();
        }

        return SubmitAnswerResponse.builder()
                .correct(correct)
                .pointsEarned(pointsEarned)
                .nextLevelUnlocked(nextLevelUnlocked)
                .starsEarned(correct ? 1 : 0)
                .bestStars(correct ? 1 : 0)
                .latestStatus(correct ? STATUS_CORRECT : STATUS_WRONG)
                .errorItemId(errorItemId)
                .build();
    }

    private void saveProblemRecord(Long userId,
            Level level,
            boolean correct,
            SubmitAnswerRequest request,
            Integer attemptNo) {
        UserProblemRecord record = new UserProblemRecord();
        record.setUserId(userId);
        record.setLevelId(level.getId());
        record.setLevelName(level.getName());
        record.setTrack(defaultTrack(level.getTrack()));
        record.setIsCorrect(correct);
        record.setStatus(correct ? STATUS_CORRECT : STATUS_WRONG);
        record.setStars(correct ? 1 : 0);
        record.setAttemptNo(Math.max(1, attemptNo == null ? 1 : attemptNo));
        record.setSolveTimeMs(toSolveTimeMs(request));
        record.setSolvedAt(OffsetDateTime.now());
        userProblemRecordRepository.save(record);
    }

    private void updateAttempt(QuestionAttempt attempt, Long userId, Level level, String rawUserAnswer,
            boolean correct) {
        LocalDateTime now = LocalDateTime.now();
        if (attempt.getId() == null) {
            attempt.setUserId(userId);
            attempt.setLevelId(level.getId());
            attempt.setFirstSubmittedAt(now);
            attempt.setSubmitCount(0);
        }
        attempt.setQuestionTitle(level.getName());
        attempt.setQuestionContent(level.getQuestion());
        attempt.setLevelType(defaultType(level.getType()));
        attempt.setLatestStatus(correct ? STATUS_CORRECT : STATUS_WRONG);
        attempt.setLatestUserAnswer(rawUserAnswer);
        attempt.setLastSubmittedAt(now);
        attempt.setSubmitCount((attempt.getSubmitCount() == null ? 0 : attempt.getSubmitCount()) + 1);
    }

    private ErrorItem upsertErrorItem(Long userId,
            Level level,
            String rawUserAnswer,
            String normalizedUserAnswer,
            String normalizedCorrectAnswer,
            List<String> options) {
        LocalDateTime now = LocalDateTime.now();
        ErrorItem errorItem = errorItemRepository.findByUserIdAndLevelId(userId, level.getId())
                .orElseGet(ErrorItem::new);

        if (errorItem.getId() == null) {
            errorItem.setUserId(userId);
            errorItem.setLevelId(level.getId());
            errorItem.setCreatedAt(now);
        }

        errorItem.setTitle(level.getName());
        errorItem.setLevelType(defaultType(level.getType()));
        errorItem.setQuestion(level.getQuestion());
        errorItem.setUserAnswer(rawUserAnswer);
        errorItem.setDescription(level.getDescription());
        errorItem.setCorrectAnswer(level.getAnswer());
        errorItem.setUpdatedAt(now);
        errorItem.setAnalysisStatus("未分析");
        errorItem.setAnalysis(null);
        errorItem.replaceOptionSnapshots(
                buildOptionSnapshots(errorItem, options, normalizedCorrectAnswer, normalizedUserAnswer));

        return errorItemRepository.save(errorItem);
    }

    private List<ErrorItemOptionSnapshot> buildOptionSnapshots(ErrorItem errorItem,
            List<String> options,
            String normalizedCorrectAnswer,
            String normalizedUserAnswer) {
        if (options.isEmpty()) {
            return List.of();
        }

        Set<String> correctValues = new HashSet<>(splitAnswer(normalizedCorrectAnswer));
        Set<String> selectedValues = new HashSet<>(splitAnswer(normalizedUserAnswer));
        List<ErrorItemOptionSnapshot> snapshots = new ArrayList<>(options.size());

        for (int i = 0; i < options.size(); i++) {
            String optionContent = defaultString(options.get(i)).trim();
            ErrorItemOptionSnapshot snapshot = new ErrorItemOptionSnapshot();
            snapshot.setErrorItem(errorItem);
            snapshot.setSortOrder(i + 1);
            snapshot.setOptionKey(toOptionKey(i));
            snapshot.setOptionContent(optionContent);
            snapshot.setIsCorrect(correctValues.contains(optionContent));
            snapshot.setIsSelected(selectedValues.contains(optionContent));
            snapshots.add(snapshot);
        }

        return snapshots;
    }

    private boolean unlockNextLevel(Level currentLevel) {
        if (currentLevel.getOrder() == null || !StringUtils.hasText(currentLevel.getTrack())) {
            return false;
        }
        return levelRepository.findByTrackAndOrder(currentLevel.getTrack(), currentLevel.getOrder() + 1)
                .map(nextLevel -> {
                    if (!Boolean.TRUE.equals(nextLevel.getIsUnlocked())) {
                        nextLevel.setIsUnlocked(true);
                        levelRepository.save(nextLevel);
                    }
                    return true;
                })
                .orElse(false);
    }

    private int increaseUserPoints(Long userId, Integer rewardPoints) {
        int delta = rewardPoints == null ? 0 : Math.max(rewardPoints, 0);
        if (delta == 0) {
            return 0;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在: " + userId));
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        user.setPoints(currentPoints + delta);
        userRepository.save(user);
        return delta;
    }

    private Integer toSolveTimeMs(SubmitAnswerRequest request) {
        if (request == null || request.getMeta() == null || request.getMeta().getTimeMs() == null) {
            return null;
        }
        long timeMs = request.getMeta().getTimeMs();
        if (timeMs <= 0) {
            return null;
        }
        if (timeMs > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) timeMs;
    }

    private String defaultTrack(String track) {
        return StringUtils.hasText(track) ? track : "algo";
    }

    private List<String> safeOptions(List<String> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String stringifyAnswer(Object answer) {
        if (answer instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
        }
        return defaultString(answer == null ? "" : answer.toString()).trim();
    }

    private String normalizeAnswer(String rawAnswer, List<String> options) {
        List<String> normalized = splitAnswer(rawAnswer).stream()
                .map(token -> toOptionContent(token, options))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return String.join(",", normalized);
    }

    private List<String> splitAnswer(String answer) {
        if (!StringUtils.hasText(answer)) {
            return List.of();
        }
        return Arrays.stream(answer.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String toOptionContent(String token, List<String> options) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String trimmed = token.trim();
        if (trimmed.length() == 1) {
            char upper = Character.toUpperCase(trimmed.charAt(0));
            int index = upper - 'A';
            if (index >= 0 && index < options.size()) {
                return defaultString(options.get(index));
            }
        }
        return trimmed;
    }

    private String toOptionKey(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private String defaultType(String type) {
        return StringUtils.hasText(type) ? type : "single";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
