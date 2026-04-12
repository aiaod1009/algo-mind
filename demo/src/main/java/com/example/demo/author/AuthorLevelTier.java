package com.example.demo.author;

import java.util.Arrays;

public enum AuthorLevelTier {
    SEED("seed", 1, "青藤新芽", "青藤 Lv.1", "seed", 0, 119, "从零起步，开始建立自己的解题与创作节奏。"),
    FLARE("flare", 2, "焰光解题者", "焰光 Lv.2", "flare", 120, 259, "做题稳定输出，开始在社区留下鲜明痕迹。"),
    TIDE("tide", 3, "潮汐创作者", "潮汐 Lv.3", "tide", 260, 429, "内容产出与答题成长同步推进，进入持续进阶期。"),
    AURORA("aurora", 4, "极光布道师", "极光 Lv.4", "aurora", 430, 649, "开始具备带动讨论与影响他人的能力。"),
    SOLAR("solar", 5, "曜金出题官", "曜金 Lv.5", "solar", 650, 919, "不仅能做题，也能用高质量题目和帖子反哺社区。"),
    NEBULA("nebula", 6, "星云策展人", "星云 Lv.6", "nebula", 920, 1239, "拥有成熟的内容品味与稳定的社区影响力。"),
    CROWN("crown", 7, "王冠领航者", "王冠 Lv.7", "crown", 1240, Integer.MAX_VALUE, "顶级创作者与解题者，用内容与实力定义社区上限。");

    private final String code;
    private final int rank;
    private final String title;
    private final String shortLabel;
    private final String theme;
    private final int minScore;
    private final int maxScore;
    private final String description;

    AuthorLevelTier(String code,
            int rank,
            String title,
            String shortLabel,
            String theme,
            int minScore,
            int maxScore,
            String description) {
        this.code = code;
        this.rank = rank;
        this.title = title;
        this.shortLabel = shortLabel;
        this.theme = theme;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public int getRank() {
        return rank;
    }

    public String getTitle() {
        return title;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getTheme() {
        return theme;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getDescription() {
        return description;
    }

    public static AuthorLevelTier fromScore(int score) {
        return Arrays.stream(values())
                .filter(tier -> score >= tier.minScore && score <= tier.maxScore)
                .findFirst()
                .orElse(SEED);
    }

    public static AuthorLevelTier fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SEED;
        }
        return Arrays.stream(values())
                .filter(tier -> tier.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(SEED);
    }

    public AuthorLevelTier next() {
        AuthorLevelTier[] tiers = values();
        return rank >= tiers.length ? null : tiers[rank];
    }
}
