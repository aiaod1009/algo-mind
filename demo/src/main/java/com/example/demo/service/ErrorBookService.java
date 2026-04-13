package com.example.demo.service;

import com.example.demo.entity.CompletedErrorItem;
import com.example.demo.entity.ErrorItem;
import com.example.demo.repository.CompletedErrorItemRepository;
import com.example.demo.repository.ErrorItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ErrorBookService {

    private final ErrorItemRepository errorItemRepository;
    private final CompletedErrorItemRepository completedErrorItemRepository;

    public ErrorBookService(ErrorItemRepository errorItemRepository,
            CompletedErrorItemRepository completedErrorItemRepository) {
        this.errorItemRepository = errorItemRepository;
        this.completedErrorItemRepository = completedErrorItemRepository;
    }

    @Transactional
    public Optional<CompletedErrorItem> archiveResolvedLevelError(Long userId, Long levelId) {
        if (userId == null || levelId == null) {
            return Optional.empty();
        }

        return errorItemRepository.findByUserIdAndLevelId(userId, levelId)
                .map(this::archiveAndDelete);
    }

    @Transactional
    public CompletedErrorItem completeError(Long userId, Long errorId) {
        ErrorItem errorItem = errorItemRepository.findByIdAndUserId(errorId, userId)
                .orElseThrow(() -> new NoSuchElementException("错题不存在: " + errorId));
        return archiveAndDelete(errorItem);
    }

    @Transactional
    public void clearCompletedByLevel(Long userId, Long levelId) {
        if (userId == null || levelId == null) {
            return;
        }
        completedErrorItemRepository.deleteByUserIdAndLevelId(userId, levelId);
    }

    private CompletedErrorItem archiveAndDelete(ErrorItem errorItem) {
        CompletedErrorItem completedErrorItem = loadCompletedTarget(errorItem);
        copyFields(errorItem, completedErrorItem);
        completedErrorItem.setCompletedAt(LocalDateTime.now());
        CompletedErrorItem saved = completedErrorItemRepository.save(completedErrorItem);
        errorItemRepository.delete(errorItem);
        return saved;
    }

    private CompletedErrorItem loadCompletedTarget(ErrorItem errorItem) {
        if (errorItem.getLevelId() == null) {
            return new CompletedErrorItem();
        }

        return completedErrorItemRepository.findByUserIdAndLevelId(errorItem.getUserId(), errorItem.getLevelId())
                .orElseGet(CompletedErrorItem::new);
    }

    private void copyFields(ErrorItem source, CompletedErrorItem target) {
        target.setUserId(source.getUserId());
        target.setLevelId(source.getLevelId());
        target.setTitle(source.getTitle());
        target.setLevelType(source.getLevelType());
        target.setQuestion(source.getQuestion());
        target.setUserAnswer(source.getUserAnswer());
        target.setDescription(source.getDescription());
        target.setCorrectAnswer(source.getCorrectAnswer());
        target.setAnalysisStatus(source.getAnalysisStatus());
        target.setAnalysis(source.getAnalysis());
        target.setAnalysisDataJson(source.getAnalysisDataJson());
        target.setAnalyzedAt(source.getAnalyzedAt());
        target.setSourceErrorId(source.getId());
        target.setCreatedAt(source.getCreatedAt());
        target.setLastErrorAt(source.getUpdatedAt());
    }
}
