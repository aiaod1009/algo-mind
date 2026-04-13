package com.example.demo.dto.ai;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisQuotaStatus {
    private String currentSlot;
    private List<String> usedSlots;
    private int usedCount;
    private int remainingCount;
    private boolean canUseCurrentSlot;
    private boolean exhausted;
    private String nextRefreshAt;
}
