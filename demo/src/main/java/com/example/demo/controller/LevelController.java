package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.dto.SubmitAnswerRequest;
import com.example.demo.dto.SubmitAnswerResponse;
import com.example.demo.entity.Level;
import com.example.demo.entity.QuestionAttempt;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.QuestionAttemptRepository;
import com.example.demo.service.LevelSubmissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
public class LevelController {

    private static final int CORRECT_STARS = 3;

    private final LevelRepository levelRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final CurrentUserService currentUserService;
    private final LevelSubmissionService levelSubmissionService;

    public LevelController(LevelRepository levelRepository,
            QuestionAttemptRepository questionAttemptRepository,
            CurrentUserService currentUserService,
            LevelSubmissionService levelSubmissionService) {
        this.levelRepository = levelRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.currentUserService = currentUserService;
        this.levelSubmissionService = levelSubmissionService;
    }

    @GetMapping("/levels")
    public Result<List<Level>> getLevels(@RequestParam(required = false) String track) {
        List<Level> levelList;
        if (track != null && !track.isBlank()) {
            levelList = levelRepository.findByTrack(track);
        } else {
            levelList = levelRepository.findAll();
        }

        Map<Long, QuestionAttempt> attemptByLevelId = new HashMap<>();
        currentUserService.getCurrentUserId().ifPresent(userId -> {
            List<Long> levelIds = levelList.stream()
                    .map(Level::getId)
                    .filter(id -> id != null)
                    .toList();
            if (!levelIds.isEmpty()) {
                attemptByLevelId.putAll(questionAttemptRepository.findByUserIdAndLevelIdIn(userId, levelIds).stream()
                        .collect(Collectors.toMap(QuestionAttempt::getLevelId, attempt -> attempt,
                                (left, right) -> left)));
            }
        });

        levelList.forEach(level -> {
            if (level.getIsUnlocked() == null) {
                level.setIsUnlocked(false);
            }
            if (level.getRewardPoints() == null) {
                level.setRewardPoints(0);
            }

            QuestionAttempt attempt = attemptByLevelId.get(level.getId());
            int bestStars = (attempt != null && "CORRECT".equalsIgnoreCase(attempt.getLatestStatus()))
                    ? CORRECT_STARS
                    : 0;
            int attemptCount = attempt != null && attempt.getSubmitCount() != null
                    ? Math.max(0, attempt.getSubmitCount())
                    : 0;

            level.setBestStars(bestStars);
            level.setStars(bestStars);
            level.setAttemptCount(attemptCount);
            level.setIsCompleted(bestStars > 0);
        });
        return Result.success(levelList == null ? List.of() : levelList);
    }

    @PostMapping("/submit")
    public Result<SubmitAnswerResponse> submitAnswer(@RequestBody SubmitAnswerRequest submitRequest) {
        try {
            return Result.success(levelSubmissionService.submit(submitRequest));
        } catch (IllegalArgumentException e) {
            return Result.fail(40001, e.getMessage());
        } catch (NoSuchElementException e) {
            return Result.fail(40401, e.getMessage());
        }
    }
}
