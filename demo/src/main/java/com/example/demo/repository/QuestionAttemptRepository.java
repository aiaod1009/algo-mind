package com.example.demo.repository;

import com.example.demo.entity.QuestionAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

    Optional<QuestionAttempt> findByUserIdAndLevelId(Long userId, Long levelId);

    List<QuestionAttempt> findByUserId(Long userId);

    List<QuestionAttempt> findByUserIdAndLevelIdIn(Long userId, Collection<Long> levelIds);

    long countByUserIdAndLatestStatus(Long userId, String latestStatus);

    long countByLevelIdAndLatestStatus(Long levelId, String latestStatus);

    long countByUserIdAndLatestStatusAndSubmitCountLessThanEqual(Long userId,
            String latestStatus,
            Integer submitCount);

    long countByLevelIdInAndLatestStatus(Collection<Long> levelIds, String latestStatus);

    long countByLevelIdInAndLatestStatusAndSubmitCountLessThanEqual(Collection<Long> levelIds,
            String latestStatus,
            Integer submitCount);

    @Query("""
            select count(distinct qa.userId)
            from QuestionAttempt qa
            where qa.levelId in :levelIds and qa.latestStatus = :latestStatus
            """)
    long countDistinctUsersByLevelIdInAndLatestStatus(@Param("levelIds") Collection<Long> levelIds,
            @Param("latestStatus") String latestStatus);

    Page<QuestionAttempt> findByUserIdAndLatestStatusOrderByLastSubmittedAtDesc(Long userId,
            String latestStatus,
            Pageable pageable);
}
