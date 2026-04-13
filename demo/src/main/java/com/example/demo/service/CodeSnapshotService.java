package com.example.demo.service;

import com.example.demo.entity.CodeSnapshot;
import com.example.demo.repository.CodeSnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CodeSnapshotService {

    private static final int AI_PASS_SCORE = 70;

    private final CodeSnapshotRepository codeSnapshotRepository;
    private final ObjectMapper objectMapper;

    public CodeSnapshotService(CodeSnapshotRepository codeSnapshotRepository, ObjectMapper objectMapper) {
        this.codeSnapshotRepository = codeSnapshotRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CodeSnapshot saveSnapshot(CodeSnapshot snapshot) {
        Long userId = snapshot.getUserId();
        Long levelId = snapshot.getLevelId();

        if (userId == null || levelId == null) {
            throw new IllegalArgumentException("userId 和 levelId 不能为空");
        }

        boolean aiEvalPassed = snapshot.getScore() != null && snapshot.getScore() >= AI_PASS_SCORE;
        snapshot.setAiEvalPassed(aiEvalPassed);
        snapshot.setSavedAt(OffsetDateTime.now());

        updateBestFlag(snapshot);

        return codeSnapshotRepository.save(snapshot);
    }

    private void updateBestFlag(CodeSnapshot snapshot) {
        if (!Boolean.TRUE.equals(snapshot.getAiEvalPassed())) {
            snapshot.setIsBest(false);
            return;
        }

        Optional<CodeSnapshot> currentBest = codeSnapshotRepository
                .findByUserIdAndLevelIdAndIsBestTrue(snapshot.getUserId(), snapshot.getLevelId());

        if (currentBest.isEmpty()) {
            snapshot.setIsBest(true);
            return;
        }

        CodeSnapshot existing = currentBest.get();
        int newScore = snapshot.getScore() != null ? snapshot.getScore() : 0;
        int existingScore = existing.getScore() != null ? existing.getScore() : 0;

        if (newScore > existingScore || (newScore == existingScore
                && (snapshot.getStars() != null ? snapshot.getStars() : 0) > (existing.getStars() != null ? existing.getStars() : 0))) {
            existing.setIsBest(false);
            codeSnapshotRepository.saveAndFlush(existing);
            snapshot.setIsBest(true);
        } else {
            snapshot.setIsBest(false);
        }
    }

    public List<CodeSnapshot> getUserSnapshots(Long userId) {
        return codeSnapshotRepository.findByUserIdOrderBySavedAtDesc(userId);
    }

    public List<CodeSnapshot> getUserPassedSnapshots(Long userId) {
        return codeSnapshotRepository.findByUserIdAndAiEvalPassedTrueOrderBySavedAtDesc(userId);
    }

    public List<CodeSnapshot> getLevelSnapshots(Long userId, Long levelId) {
        return codeSnapshotRepository.findByUserIdAndLevelIdOrderBySavedAtDesc(userId, levelId);
    }

    public Optional<CodeSnapshot> getBestSnapshot(Long userId, Long levelId) {
        return codeSnapshotRepository.findByUserIdAndLevelIdAndIsBestTrue(userId, levelId);
    }

    public Optional<CodeSnapshot> getHighestScoreSnapshot(Long userId, Long levelId) {
        return codeSnapshotRepository.findTopByUserIdAndLevelIdOrderByScoreDescSavedAtDesc(userId, levelId);
    }

    public Map<String, Object> getUserStats(Long userId) {
        long total = codeSnapshotRepository.countByUserId(userId);
        long passed = codeSnapshotRepository.countByUserIdAndAiEvalPassedTrue(userId);
        return Map.of(
                "totalSnapshots", total,
                "passedSnapshots", passed
        );
    }

    @Transactional
    public boolean deleteSnapshot(Long id, Long userId) {
        Optional<CodeSnapshot> snapshot = codeSnapshotRepository.findById(id);
        if (snapshot.isEmpty() || !snapshot.get().getUserId().equals(userId)) {
            return false;
        }
        codeSnapshotRepository.deleteById(id);
        return true;
    }
}
