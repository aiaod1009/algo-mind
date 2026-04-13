package com.example.demo.service;

import com.example.demo.dto.ai.AnalysisQuotaStatus;
import com.example.demo.entity.AiAnalysisUsage;
import com.example.demo.repository.AiAnalysisUsageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiAnalysisQuotaService {

    private static final String SLOT_AM = "am";
    private static final String SLOT_PM = "pm";

    private final AiAnalysisUsageRepository aiAnalysisUsageRepository;

    public AiAnalysisQuotaService(AiAnalysisUsageRepository aiAnalysisUsageRepository) {
        this.aiAnalysisUsageRepository = aiAnalysisUsageRepository;
    }

    public AnalysisQuotaStatus getQuotaStatus(Long userId) {
        return buildQuotaStatus(userId, LocalDateTime.now());
    }

    @Transactional
    public SlotReservation reserveCurrentSlot(Long userId, Long errorId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate quotaDate = now.toLocalDate();
        String slotCode = resolveCurrentSlot(now);

        AiAnalysisUsage usage = new AiAnalysisUsage();
        usage.setUserId(userId);
        usage.setErrorId(errorId);
        usage.setQuotaDate(quotaDate);
        usage.setSlotCode(slotCode);
        usage.setCreatedAt(now);

        try {
            aiAnalysisUsageRepository.saveAndFlush(usage);
            return new SlotReservation(true, slotCode, quotaDate, buildQuotaStatus(userId, now));
        } catch (DataIntegrityViolationException exception) {
            return new SlotReservation(false, slotCode, quotaDate, buildQuotaStatus(userId, now));
        }
    }

    @Transactional
    public void releaseReservation(Long userId, SlotReservation reservation) {
        if (reservation == null || !reservation.acquired()) {
            return;
        }

        aiAnalysisUsageRepository.deleteByUserIdAndQuotaDateAndSlotCode(
                userId,
                reservation.quotaDate(),
                reservation.slotCode()
        );
    }

    private AnalysisQuotaStatus buildQuotaStatus(Long userId, LocalDateTime now) {
        LocalDate quotaDate = now.toLocalDate();
        String currentSlot = resolveCurrentSlot(now);
        List<String> usedSlots = aiAnalysisUsageRepository.findByUserIdAndQuotaDateOrderByCreatedAtAsc(userId, quotaDate)
                .stream()
                .map(AiAnalysisUsage::getSlotCode)
                .distinct()
                .toList();

        AnalysisQuotaStatus status = new AnalysisQuotaStatus();
        status.setCurrentSlot(currentSlot);
        status.setUsedSlots(usedSlots);
        status.setUsedCount(usedSlots.size());
        status.setRemainingCount(Math.max(0, 2 - usedSlots.size()));
        status.setCanUseCurrentSlot(!usedSlots.contains(currentSlot));
        status.setExhausted(usedSlots.size() >= 2);
        status.setNextRefreshAt(resolveNextRefresh(now).toString());
        return status;
    }

    private String resolveCurrentSlot(LocalDateTime now) {
        return now.getHour() < 12 ? SLOT_AM : SLOT_PM;
    }

    private LocalDateTime resolveNextRefresh(LocalDateTime now) {
        if (now.getHour() < 12) {
            return now.toLocalDate().atTime(12, 0);
        }
        return now.toLocalDate().plusDays(1).atStartOfDay();
    }

    public record SlotReservation(
            boolean acquired,
            String slotCode,
            LocalDate quotaDate,
            AnalysisQuotaStatus quotaStatus
    ) {
    }
}
