package com.example.demo.repository;

import java.time.OffsetDateTime;

/**
 * 知识库文章摘要DTO - 用于优化查询性能
 * 不包含大字段（JSON内容），减少数据传输量
 */
public record KnowledgeArticleSummary(
        Long id,
        String slug,
        String title,
        String sectionId,
        String sectionTitle,
        String sectionDescription,
        String badge,
        String lead,
        String readTime,
        String tagsText,
        Boolean spotlightEnabled,
        String spotlightEyebrow,
        String spotlightTitle,
        String spotlightDescription,
        String spotlightAccent,
        Integer sortOrder,
        OffsetDateTime updatedAt
) {
    
    /**
     * 获取标签列表
     */
    public String[] getTags() {
        if (tagsText == null || tagsText.isBlank()) {
            return new String[0];
        }
        return tagsText.split("[,，\\n]");
    }
    
    /**
     * 检查是否匹配关键词
     */
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase();
        return (title != null && title.toLowerCase().contains(lowerKeyword))
                || (sectionTitle != null && sectionTitle.toLowerCase().contains(lowerKeyword))
                || (lead != null && lead.toLowerCase().contains(lowerKeyword))
                || (tagsText != null && tagsText.toLowerCase().contains(lowerKeyword))
                || (badge != null && badge.toLowerCase().contains(lowerKeyword));
    }
}
