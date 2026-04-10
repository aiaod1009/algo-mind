package com.example.demo.repository;

import com.example.demo.entity.UserProblemRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface UserProblemRecordRepository extends JpaRepository<UserProblemRecord, Long> {

  @Query(value = """
      SELECT DATE_FORMAT(solved_at, '%Y-%m-%d') AS day, COUNT(*) AS cnt
      FROM t_user_problem_record
      WHERE user_id = :userId
        AND solved_at >= :start
        AND solved_at < :end
      GROUP BY DATE(solved_at)
      ORDER BY day
      """, nativeQuery = true)
  List<Object[]> countDailyAttempts(@Param("userId") Long userId,
      @Param("start") OffsetDateTime start,
      @Param("end") OffsetDateTime end);

  long countByUserIdAndSolvedAtBetween(Long userId, OffsetDateTime start, OffsetDateTime end);

  Page<UserProblemRecord> findByUserIdOrderBySolvedAtDesc(Long userId, Pageable pageable);
}
