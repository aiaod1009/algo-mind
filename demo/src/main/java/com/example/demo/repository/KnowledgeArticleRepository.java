package com.example.demo.repository;

import com.example.demo.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Optional<KnowledgeArticle> findBySlugAndPublishedTrue(String slug);

    Optional<KnowledgeArticle> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<KnowledgeArticle> findAllBySlugIn(Collection<String> slugs);

    List<KnowledgeArticle> findAllByPublishedTrueOrderBySectionIdAscSortOrderAscUpdatedAtDesc();

    List<KnowledgeArticle> findAllByOrderBySectionIdAscSortOrderAscUpdatedAtDesc();

    /**
     * 使用数据库全文搜索查询文章 - 性能优化版本
     * 使用 MATCH AGAINST 进行高效的全文搜索（MySQL）
     */
    @Query(value = """
            SELECT * FROM t_knowledge_article 
            WHERE published = 1 
            AND (
                MATCH(title, summary, lead, tags_text, badge) AGAINST(:keyword IN BOOLEAN MODE)
                OR section_title LIKE CONCAT('%', :keyword, '%')
                OR english_title LIKE CONCAT('%', :keyword, '%')
            )
            ORDER BY section_id ASC, sort_order ASC, updated_at DESC
            """, nativeQuery = true)
    List<KnowledgeArticle> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 获取已发布文章的摘要信息（用于目录展示，减少数据传输）
     */
    @Query("""
            SELECT new com.example.demo.repository.KnowledgeArticleSummary(
                a.id, a.slug, a.title, a.sectionId, a.sectionTitle, 
                a.sectionDescription, a.badge, a.lead, a.readTime, a.tagsText,
                a.spotlightEnabled, a.spotlightEyebrow, a.spotlightTitle, 
                a.spotlightDescription, a.spotlightAccent, a.sortOrder, a.updatedAt
            )
            FROM KnowledgeArticle a 
            WHERE a.published = true 
            ORDER BY a.sectionId ASC, a.sortOrder ASC, a.updatedAt DESC
            """)
    List<KnowledgeArticleSummary> findAllPublishedSummaries();

    /**
     * 带关键词过滤的摘要查询
     */
    @Query(value = """
            SELECT 
                a.id as id, a.slug as slug, a.title as title, 
                a.section_id as sectionId, a.section_title as sectionTitle,
                a.section_description as sectionDescription, a.badge as badge,
                a.lead as lead, a.read_time as readTime, a.tags_text as tagsText,
                a.spotlight_enabled as spotlightEnabled, a.spotlight_eyebrow as spotlightEyebrow,
                a.spotlight_title as spotlightTitle, a.spotlight_description as spotlightDescription,
                a.spotlight_accent as spotlightAccent, a.sort_order as sortOrder, a.updated_at as updatedAt
            FROM t_knowledge_article a
            WHERE a.published = 1 
            AND (
                MATCH(a.title, a.summary, a.lead, a.tags_text, a.badge) AGAINST(:keyword IN BOOLEAN MODE)
                OR a.section_title LIKE CONCAT('%', :keyword, '%')
                OR a.english_title LIKE CONCAT('%', :keyword, '%')
            )
            ORDER BY a.section_id ASC, a.sort_order ASC, a.updated_at DESC
            """, nativeQuery = true)
    List<KnowledgeArticleSummary> searchPublishedSummaries(@Param("keyword") String keyword);

    /**
     * 仅获取轻量级目录数据（不含大字段）
     */
    @Query("""
            SELECT new com.example.demo.repository.KnowledgeArticleSummary(
                a.id, a.slug, a.title, a.sectionId, a.sectionTitle, 
                a.sectionDescription, a.badge, a.lead, a.readTime, a.tagsText,
                a.spotlightEnabled, a.spotlightEyebrow, a.spotlightTitle, 
                a.spotlightDescription, a.spotlightAccent, a.sortOrder, a.updatedAt
            )
            FROM KnowledgeArticle a 
            WHERE a.published = true 
            AND (:keyword IS NULL OR :keyword = '' 
                OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.sectionTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.tagsText) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            ORDER BY a.sectionId ASC, a.sortOrder ASC, a.updatedAt DESC
            """)
    List<KnowledgeArticleSummary> findPublishedSummariesByKeyword(@Param("keyword") String keyword);

    /**
     * 统计已发布文章数量
     */
    @Query("SELECT COUNT(a) FROM KnowledgeArticle a WHERE a.published = true")
    long countPublished();

    /**
     * 批量检查slug是否存在
     */
    @Query("SELECT a.slug FROM KnowledgeArticle a WHERE a.slug IN :slugs")
    List<String> findExistingSlugs(@Param("slugs") Collection<String> slugs);
}
