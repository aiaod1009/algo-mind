package com.example.demo.repository;

import com.example.demo.entity.AiAnalysisUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiAnalysisUsageRepository extends JpaRepository<AiAnalysisUsage, Long> {

    List<AiAnalysisUsage> findByUserIdAndQuotaDateOrderByCreatedAtAsc(Long userId, LocalDate quotaDate);

    boolean existsByUserIdAndQuotaDateAndSlotCode(Long userId, LocalDate quotaDate, String slotCode);

    void deleteByUserIdAndQuotaDateAndSlotCode(Long userId, LocalDate quotaDate, String slotCode);
}
