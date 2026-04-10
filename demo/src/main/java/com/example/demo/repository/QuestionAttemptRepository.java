package com.example.demo.repository;

import com.example.demo.entity.QuestionAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

    Optional<QuestionAttempt> findByUserIdAndLevelId(Long userId, Long levelId);

    long countByUserIdAndLatestStatus(Long userId, String latestStatus);

    long countByLevelIdAndLatestStatus(Long levelId, String latestStatus);

    Page<QuestionAttempt> findByUserIdAndLatestStatusOrderByLastSubmittedAtDesc(Long userId,
            String latestStatus,
            Pageable pageable);
}
