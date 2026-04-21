package com.example.demo.service;

import com.example.demo.entity.KnowledgeArticle;
import com.example.demo.entity.KnowledgeBaseConfig;
import com.example.demo.repository.KnowledgeArticleRepository;
import com.example.demo.repository.KnowledgeBaseConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

@Service
public class KnowledgeBaseService {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final KnowledgeBaseConfigRepository knowledgeBaseConfigRepository;
    private final ObjectMapper objectMapper;

    public KnowledgeBaseService(
            KnowledgeArticleRepository knowledgeArticleRepository,
            KnowledgeBaseConfigRepository knowledgeBaseConfigRepository,
            ObjectMapper objectMapper
    ) {
        this.knowledgeArticleRepository = knowledgeArticleRepository;
        this.knowledgeBaseConfigRepository = knowledgeBaseConfigRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public KnowledgeCatalog getCatalog(String keyword) {
        KnowledgeBaseConfig config = getOrCreateConfig();
        String normalizedKeyword = normalize(keyword);

        List<KnowledgeArticle> publishedArticles = knowledgeArticleRepository.findAllByPublishedTrueOrderBySectionIdAscSortOrderAscUpdatedAtDesc();
        List<KnowledgeArticle> visibleArticles = publishedArticles.stream()
                .filter(article -> matchesKeyword(article, normalizedKeyword))
                .toList();

        List<KnowledgeSection> sections = buildSections(visibleArticles);
        Set<String> visibleSlugs = visibleArticles.stream()
                .map(KnowledgeArticle::getSlug)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        String defaultArticleSlug = visibleSlugs.contains(safe(config.getDefaultArticleSlug()))
                ? safe(config.getDefaultArticleSlug())
                : visibleArticles.stream().map(KnowledgeArticle::getSlug).findFirst().orElse("");

        return new KnowledgeCatalog(
                safe(config.getSiteTitle(), "AlgoMind 知识库"),
                safe(config.getSiteSubtitle(), "把高频算法与工程知识点整理成可持续维护的学习文档。"),
                safe(config.getEmptyStateTitle(), "没有找到匹配的知识主题"),
                safe(config.getEmptyStateDescription(), "试试换一个关键词，或者先从左侧目录选一篇文章开始阅读。"),
                defaultArticleSlug,
                splitTextList(config.getQuickSearchesText()),
                buildMetrics(visibleArticles),
                buildSpotlightCards(visibleArticles, visibleSlugs),
                sections);
    }

    @Transactional(readOnly = true)
    public KnowledgeArticleView getArticle(String slug) {
        KnowledgeArticle article = knowledgeArticleRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new NoSuchElementException("知识库文章不存在: " + slug));
        return toArticleView(article, knowledgeArticleRepository.findAllByPublishedTrueOrderBySectionIdAscSortOrderAscUpdatedAtDesc());
    }

    @Transactional(readOnly = true)
    public AdminKnowledgeDashboard getAdminDashboard() {
        KnowledgeBaseConfig config = getOrCreateConfig();
        List<KnowledgeArticle> articles = knowledgeArticleRepository.findAllByOrderBySectionIdAscSortOrderAscUpdatedAtDesc();
        return new AdminKnowledgeDashboard(
                toAdminConfig(config),
                articles.stream().map(this::toAdminArticleSummary).toList());
    }

    @Transactional(readOnly = true)
    public AdminArticleDetail getAdminArticle(Long id) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("知识库文章不存在: " + id));
        return toAdminArticleDetail(article);
    }

    @Transactional
    public AdminArticleDetail createArticle(AdminArticleInput input, Long operatorUserId) {
        String slug = normalizeSlug(input.slug());
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug 不能为空");
        }
        if (knowledgeArticleRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("slug 已存在，请换一个");
        }

        KnowledgeArticle article = new KnowledgeArticle();
        applyArticleInput(article, input, operatorUserId, true);
        article.setSlug(slug);
        return toAdminArticleDetail(knowledgeArticleRepository.save(article));
    }

    @Transactional
    public AdminArticleDetail updateArticle(Long id, AdminArticleInput input, Long operatorUserId) {
        KnowledgeArticle article = knowledgeArticleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("知识库文章不存在: " + id));

        String slug = normalizeSlug(input.slug());
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug 不能为空");
        }
        if (knowledgeArticleRepository.existsBySlugAndIdNot(slug, id)) {
            throw new IllegalArgumentException("slug 已存在，请换一个");
        }

        article.setSlug(slug);
        applyArticleInput(article, input, operatorUserId, false);
        return toAdminArticleDetail(knowledgeArticleRepository.save(article));
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!knowledgeArticleRepository.existsById(id)) {
            throw new NoSuchElementException("知识库文章不存在: " + id);
        }
        knowledgeArticleRepository.deleteById(id);
    }

    @Transactional
    public BatchUpsertResult batchUpsertArticles(List<AdminArticleInput> inputs, Long operatorUserId) {
        if (inputs == null || inputs.isEmpty()) {
            return new BatchUpsertResult(0, 0, 0);
        }

        List<String> slugs = inputs.stream()
                .map(AdminArticleInput::slug)
                .map(this::normalizeSlug)
                .filter(slug -> !slug.isBlank())
                .distinct()
                .toList();
        if (slugs.isEmpty()) {
            throw new IllegalArgumentException("No article slugs available for batch upsert");
        }

        Map<String, KnowledgeArticle> existingBySlug = knowledgeArticleRepository.findAllBySlugIn(slugs).stream()
                .collect(LinkedHashMap::new, (map, article) -> map.put(article.getSlug(), article), Map::putAll);

        int inserted = 0;
        int updated = 0;
        List<KnowledgeArticle> toSave = new ArrayList<>();
        for (AdminArticleInput input : inputs) {
            String slug = normalizeSlug(input.slug());
            if (slug.isBlank()) {
                throw new IllegalArgumentException("slug must not be blank");
            }

            KnowledgeArticle article = existingBySlug.get(slug);
            boolean isCreate = article == null;
            if (isCreate) {
                article = new KnowledgeArticle();
                article.setSlug(slug);
                inserted++;
            } else {
                updated++;
            }

            applyArticleInput(article, input, operatorUserId, isCreate);
            article.setSlug(slug);
            toSave.add(article);
        }

        knowledgeArticleRepository.saveAll(toSave);
        return new BatchUpsertResult(inserted, updated, toSave.size());
    }

    @Transactional
    public AdminConfigView updateConfig(AdminConfigInput input) {
        KnowledgeBaseConfig config = getOrCreateConfig();
        config.setSiteTitle(safe(input.siteTitle(), "AlgoMind 知识库"));
        config.setSiteSubtitle(safe(input.siteSubtitle(), "把高频算法与工程知识点整理成可持续维护的学习文档。"));
        config.setEmptyStateTitle(safe(input.emptyStateTitle(), "没有找到匹配的知识主题"));
        config.setEmptyStateDescription(safe(input.emptyStateDescription(), "试试换一个关键词，或者先从左侧目录选一篇文章开始阅读。"));
        config.setDefaultArticleSlug(normalizeSlug(input.defaultArticleSlug()));
        config.setQuickSearchesText(joinTextList(input.quickSearches()));
        return toAdminConfig(knowledgeBaseConfigRepository.save(config));
    }

    private void applyArticleInput(KnowledgeArticle article, AdminArticleInput input, Long operatorUserId, boolean isCreate) {
        article.setTitle(safe(input.title(), "未命名文章"));
        article.setEnglishTitle(trimToEmpty(input.englishTitle()));
        article.setSectionId(normalizeSlug(input.sectionId()).isBlank() ? "general" : normalizeSlug(input.sectionId()));
        article.setSectionTitle(safe(input.sectionTitle(), "未分类"));
        article.setSectionDescription(trimToEmpty(input.sectionDescription()));
        article.setBadge(trimToEmpty(input.badge()));
        article.setSummary(trimToEmpty(input.summary()));
        article.setLead(trimToEmpty(input.lead()));
        article.setComplexity(trimToEmpty(input.complexity()));
        article.setReadTime(trimToEmpty(input.readTime()));
        article.setTagsText(joinTextList(input.tags()));
        article.setLearningObjectivesJson(toJson(input.learningObjectives()));
        article.setStrategyStepsJson(toJson(input.strategySteps()));
        article.setInsightsJson(toJson(input.insights()));
        article.setCodeBlocksJson(toJson(input.codeBlocks()));
        article.setChecklistJson(toJson(input.checklist()));
        article.setRelatedSlugsText(joinTextList(input.relatedArticleSlugs()));
        article.setSpotlightEnabled(Boolean.TRUE.equals(input.spotlightEnabled()));
        article.setSpotlightEyebrow(trimToEmpty(input.spotlightEyebrow()));
        article.setSpotlightTitle(trimToEmpty(input.spotlightTitle()));
        article.setSpotlightDescription(trimToEmpty(input.spotlightDescription()));
        article.setSpotlightAccent(trimToEmpty(input.spotlightAccent()));
        article.setPublished(Boolean.TRUE.equals(input.published()));
        article.setSortOrder(input.sortOrder() == null ? 0 : input.sortOrder());
        if (isCreate) {
            article.setCreatedByUserId(operatorUserId);
        }
        article.setUpdatedByUserId(operatorUserId);
    }

    private boolean matchesKeyword(KnowledgeArticle article, String normalizedKeyword) {
        if (normalizedKeyword.isBlank()) {
            return true;
        }

        String haystack = String.join(" ",
                safe(article.getTitle()),
                safe(article.getEnglishTitle()),
                safe(article.getSectionTitle()),
                safe(article.getSummary()),
                safe(article.getLead()),
                safe(article.getTagsText()),
                safe(article.getBadge()));
        return normalize(haystack).contains(normalizedKeyword);
    }

    private List<KnowledgeMetric> buildMetrics(List<KnowledgeArticle> articles) {
        int articleCount = articles.size();
        int codeBlockCount = articles.stream().mapToInt(article -> parseCodeBlocks(article.getCodeBlocksJson()).size()).sum();
        int checklistCount = articles.stream().mapToInt(article -> parseTextListJson(article.getChecklistJson()).size()).sum();

        return List.of(
                new KnowledgeMetric("文档主题", String.valueOf(articleCount), "支持持续维护和新增文章"),
                new KnowledgeMetric("示例代码", String.valueOf(codeBlockCount), "每篇都可以挂多段代码模板"),
                new KnowledgeMetric("复盘清单", String.valueOf(checklistCount), "帮助把知识点变成答题动作"));
    }

    private List<KnowledgeSpotlightCard> buildSpotlightCards(List<KnowledgeArticle> articles, Set<String> visibleSlugs) {
        return articles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getSpotlightEnabled()))
                .filter(article -> visibleSlugs.contains(article.getSlug()))
                .map(article -> new KnowledgeSpotlightCard(
                        safe(article.getSpotlightEyebrow(), "推荐主题"),
                        safe(article.getSpotlightTitle(), article.getTitle()),
                        safe(article.getSpotlightDescription(), article.getSummary()),
                        article.getSlug(),
                        safe(article.getSpotlightAccent(), "emerald")))
                .limit(4)
                .toList();
    }

    private List<KnowledgeSection> buildSections(List<KnowledgeArticle> articles) {
        Map<String, List<KnowledgeArticle>> grouped = new LinkedHashMap<>();
        for (KnowledgeArticle article : articles) {
            grouped.computeIfAbsent(article.getSectionId(), ignored -> new ArrayList<>()).add(article);
        }

        List<KnowledgeSection> sections = new ArrayList<>();
        for (Map.Entry<String, List<KnowledgeArticle>> entry : grouped.entrySet()) {
            List<KnowledgeArticle> sectionArticles = entry.getValue();
            KnowledgeArticle first = sectionArticles.getFirst();
            sections.add(new KnowledgeSection(
                    safe(first.getSectionId()),
                    safe(first.getSectionTitle(), "未分类"),
                    safe(first.getSectionDescription()),
                    sectionArticles.stream().map(this::toNavItem).toList()));
        }
        return sections;
    }

    private KnowledgeNavItem toNavItem(KnowledgeArticle article) {
        return new KnowledgeNavItem(
                article.getSlug(),
                safe(article.getTitle()),
                safe(article.getLead()),
                safe(article.getBadge()),
                splitTextList(article.getTagsText()),
                safe(article.getReadTime()));
    }

    private KnowledgeArticleView toArticleView(KnowledgeArticle article, List<KnowledgeArticle> sourceArticles) {
        Map<String, KnowledgeArticle> articleMap = sourceArticles.stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getSlug(), item), Map::putAll);

        List<KnowledgeRelatedArticle> relatedArticles = splitTextList(article.getRelatedSlugsText()).stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .map(related -> new KnowledgeRelatedArticle(related.getSlug(), related.getTitle(), related.getSectionTitle()))
                .toList();

        return new KnowledgeArticleView(
                article.getId(),
                article.getSlug(),
                safe(article.getTitle()),
                safe(article.getEnglishTitle()),
                safe(article.getSectionId()),
                safe(article.getSectionTitle()),
                safe(article.getSummary()),
                safe(article.getLead()),
                safe(article.getComplexity()),
                safe(article.getReadTime()),
                splitTextList(article.getTagsText()),
                parseTextListJson(article.getLearningObjectivesJson()),
                parseSteps(article.getStrategyStepsJson()),
                parseInsights(article.getInsightsJson()),
                parseCodeBlocks(article.getCodeBlocksJson()),
                parseTextListJson(article.getChecklistJson()),
                relatedArticles);
    }

    private AdminConfigView toAdminConfig(KnowledgeBaseConfig config) {
        return new AdminConfigView(
                config.getId(),
                safe(config.getSiteTitle(), "AlgoMind 知识库"),
                safe(config.getSiteSubtitle()),
                safe(config.getEmptyStateTitle(), "没有找到匹配的知识主题"),
                safe(config.getEmptyStateDescription()),
                safe(config.getDefaultArticleSlug()),
                splitTextList(config.getQuickSearchesText()));
    }

    private AdminArticleSummary toAdminArticleSummary(KnowledgeArticle article) {
        return new AdminArticleSummary(
                article.getId(),
                article.getSlug(),
                safe(article.getTitle()),
                safe(article.getSectionTitle()),
                Boolean.TRUE.equals(article.getPublished()),
                Boolean.TRUE.equals(article.getSpotlightEnabled()),
                article.getSortOrder() == null ? 0 : article.getSortOrder(),
                article.getUpdatedAt());
    }

    private AdminArticleDetail toAdminArticleDetail(KnowledgeArticle article) {
        return new AdminArticleDetail(
                article.getId(),
                article.getSlug(),
                safe(article.getTitle()),
                safe(article.getEnglishTitle()),
                safe(article.getSectionId()),
                safe(article.getSectionTitle()),
                safe(article.getSectionDescription()),
                safe(article.getBadge()),
                safe(article.getSummary()),
                safe(article.getLead()),
                safe(article.getComplexity()),
                safe(article.getReadTime()),
                splitTextList(article.getTagsText()),
                parseTextListJson(article.getLearningObjectivesJson()),
                parseSteps(article.getStrategyStepsJson()),
                parseInsights(article.getInsightsJson()),
                parseCodeBlocks(article.getCodeBlocksJson()),
                parseTextListJson(article.getChecklistJson()),
                splitTextList(article.getRelatedSlugsText()),
                Boolean.TRUE.equals(article.getSpotlightEnabled()),
                safe(article.getSpotlightEyebrow()),
                safe(article.getSpotlightTitle()),
                safe(article.getSpotlightDescription()),
                safe(article.getSpotlightAccent(), "emerald"),
                Boolean.TRUE.equals(article.getPublished()),
                article.getSortOrder() == null ? 0 : article.getSortOrder(),
                article.getCreatedAt(),
                article.getUpdatedAt());
    }

    private KnowledgeBaseConfig getOrCreateConfig() {
        return knowledgeBaseConfigRepository.findAll().stream().findFirst().orElseGet(() -> {
            KnowledgeBaseConfig config = new KnowledgeBaseConfig();
            config.setSiteTitle("AlgoMind 知识库");
            config.setSiteSubtitle("把高频算法与工程知识点整理成可持续维护的学习文档。");
            config.setEmptyStateTitle("没有找到匹配的知识主题");
            config.setEmptyStateDescription("试试换一个关键词，或者先从左侧目录选一篇文章开始阅读。");
            config.setQuickSearchesText("动态规划,图论最短路,线段树,二分答案,指针与内存");
            return knowledgeBaseConfigRepository.save(config);
        });
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyList() : value);
        } catch (IOException exception) {
            throw new IllegalStateException("知识库内容序列化失败", exception);
        }
    }

    private List<String> parseTextListJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException exception) {
            return List.of();
        }
    }

    private List<KnowledgeStep> parseSteps(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException exception) {
            return List.of();
        }
    }

    private List<KnowledgeInsight> parseInsights(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException exception) {
            return List.of();
        }
    }

    private List<KnowledgeCodeBlock> parseCodeBlocks(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException exception) {
            return List.of();
        }
    }

    private List<String> splitTextList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,，\\n]"))
                .map(this::trimToEmpty)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private String joinTextList(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(this::trimToEmpty)
                .filter(value -> !value.isBlank())
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private String normalize(String value) {
        return trimToEmpty(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeSlug(String value) {
        String normalized = trimToEmpty(value).toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9\\-]+", "-");
        normalized = normalized.replaceAll("-{2,}", "-");
        return normalized.replaceAll("(^-|-$)", "");
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return trimToEmpty(value);
    }

    private String safe(String value, String fallback) {
        String trimmed = trimToEmpty(value);
        return trimmed.isBlank() ? fallback : trimmed;
    }

    public record KnowledgeCatalog(
            String title,
            String subtitle,
            String emptyStateTitle,
            String emptyStateDescription,
            String defaultArticleSlug,
            List<String> quickSearches,
            List<KnowledgeMetric> metrics,
            List<KnowledgeSpotlightCard> spotlightCards,
            List<KnowledgeSection> sections) {
    }

    public record KnowledgeMetric(
            String label,
            String value,
            String description) {
    }

    public record KnowledgeSpotlightCard(
            String eyebrow,
            String title,
            String description,
            String slug,
            String accent) {
    }

    public record KnowledgeSection(
            String id,
            String title,
            String description,
            List<KnowledgeNavItem> articles) {
    }

    public record KnowledgeNavItem(
            String slug,
            String title,
            String subtitle,
            String badge,
            List<String> tags,
            String readTime) {
    }

    public record KnowledgeArticleView(
            Long id,
            String slug,
            String title,
            String englishTitle,
            String sectionId,
            String sectionTitle,
            String summary,
            String lead,
            String complexity,
            String readTime,
            List<String> tags,
            List<String> learningObjectives,
            List<KnowledgeStep> strategySteps,
            List<KnowledgeInsight> insights,
            List<KnowledgeCodeBlock> codeBlocks,
            List<String> checklist,
            List<KnowledgeRelatedArticle> relatedArticles) {
    }

    public record KnowledgeStep(
            String index,
            String title,
            String description,
            String badge) {
    }

    public record KnowledgeInsight(
            String title,
            String description,
            String accent) {
    }

    public record KnowledgeCodeBlock(
            String language,
            String title,
            String code,
            List<String> callouts) {
    }

    public record KnowledgeRelatedArticle(
            String slug,
            String title,
            String sectionTitle) {
    }

    public record AdminKnowledgeDashboard(
            AdminConfigView config,
            List<AdminArticleSummary> articles) {
    }

    public record AdminConfigView(
            Long id,
            String siteTitle,
            String siteSubtitle,
            String emptyStateTitle,
            String emptyStateDescription,
            String defaultArticleSlug,
            List<String> quickSearches) {
    }

    public record AdminArticleSummary(
            Long id,
            String slug,
            String title,
            String sectionTitle,
            Boolean published,
            Boolean spotlightEnabled,
            Integer sortOrder,
            java.time.OffsetDateTime updatedAt) {
    }

    public record AdminArticleDetail(
            Long id,
            String slug,
            String title,
            String englishTitle,
            String sectionId,
            String sectionTitle,
            String sectionDescription,
            String badge,
            String summary,
            String lead,
            String complexity,
            String readTime,
            List<String> tags,
            List<String> learningObjectives,
            List<KnowledgeStep> strategySteps,
            List<KnowledgeInsight> insights,
            List<KnowledgeCodeBlock> codeBlocks,
            List<String> checklist,
            List<String> relatedArticleSlugs,
            Boolean spotlightEnabled,
            String spotlightEyebrow,
            String spotlightTitle,
            String spotlightDescription,
            String spotlightAccent,
            Boolean published,
            Integer sortOrder,
            java.time.OffsetDateTime createdAt,
            java.time.OffsetDateTime updatedAt) {
    }

    public record AdminConfigInput(
            String siteTitle,
            String siteSubtitle,
            String emptyStateTitle,
            String emptyStateDescription,
            String defaultArticleSlug,
            List<String> quickSearches) {
    }

    public record AdminArticleInput(
            String slug,
            String title,
            String englishTitle,
            String sectionId,
            String sectionTitle,
            String sectionDescription,
            String badge,
            String summary,
            String lead,
            String complexity,
            String readTime,
            List<String> tags,
            List<String> learningObjectives,
            List<KnowledgeStep> strategySteps,
            List<KnowledgeInsight> insights,
            List<KnowledgeCodeBlock> codeBlocks,
            List<String> checklist,
            List<String> relatedArticleSlugs,
            Boolean spotlightEnabled,
            String spotlightEyebrow,
            String spotlightTitle,
            String spotlightDescription,
            String spotlightAccent,
            Boolean published,
            Integer sortOrder) {
    }

    public record BatchUpsertResult(
            int inserted,
            int updated,
            int total) {
    }
}
