package com.example.demo.repository;

import com.example.demo.entity.CodeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeSnapshotRepository extends JpaRepository<CodeSnapshot, Long> {

    List<CodeSnapshot> findByUserIdOrderBySavedAtDesc(Long userId);

    List<CodeSnapshot> findByUserIdAndLevelIdOrderBySavedAtDesc(Long userId, Long levelId);

    Optional<CodeSnapshot> findByUserIdAndLevelIdAndIsBestTrue(Long userId, Long levelId);

    Optional<CodeSnapshot> findTopByUserIdAndLevelIdOrderByScoreDescSavedAtDesc(Long userId, Long levelId);

    List<CodeSnapshot> findByUserIdAndAiEvalPassedTrueOrderBySavedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndAiEvalPassedTrue(Long userId);
}
