package com.example.demo.service;

import com.example.demo.entity.KnowledgeArticle;
import com.example.demo.entity.KnowledgeBaseConfig;
import com.example.demo.repository.KnowledgeArticleRepository;
import com.example.demo.repository.KnowledgeArticleSummary;
import com.example.demo.repository.KnowledgeBaseConfigRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.stream.Collectors;

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

    /**
     * 获取知识库目录 - 带缓存优化
     * 使用摘要查询代替全字段查询，减少数据传输
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "knowledgeCatalog", key = "#keyword != null ? #keyword : 'all'", unless = "#result == null")
    public KnowledgeCatalog getCatalog(String keyword) {
        KnowledgeBaseConfig config = getOrCreateConfig();
        String normalizedKeyword = normalize(keyword);

        // 使用优化的摘要查询，避免加载大字段
        List<KnowledgeArticleSummary> articleSummaries;
        if (normalizedKeyword.isBlank()) {
            articleSummaries = knowledgeArticleRepository.findAllPublishedSummaries();
        } else {
            // 使用数据库级别的过滤，减少内存处理
            articleSummaries = knowledgeArticleRepository.findPublishedSummariesByKeyword(normalizedKeyword);
        }

        // 如果数据库过滤不够精确，在内存中二次过滤
        List<KnowledgeArticleSummary> visibleSummaries = normalizedKeyword.isBlank()
                ? articleSummaries
                : articleSummaries.stream()
                        .filter(summary -> summary.matchesKeyword(normalizedKeyword))
                        .collect(Collectors.toList());

        List<KnowledgeSection> sections = buildSectionsFromSummaries(visibleSummaries);
        Set<String> visibleSlugs = visibleSummaries.stream()
                .map(KnowledgeArticleSummary::slug)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        String defaultArticleSlug = visibleSlugs.contains(safe(config.getDefaultArticleSlug()))
                ? safe(config.getDefaultArticleSlug())
                : visibleSummaries.stream()
                        .findFirst()
                        .map(KnowledgeArticleSummary::slug)
                        .orElse("");

        return new KnowledgeCatalog(
                safe(config.getSiteTitle(), "AlgoMind 知识库"),
                safe(config.getSiteSubtitle(), "把高频算法与工程知识点整理成可持续维护的学习文档。"),
                safe(config.getEmptyStateTitle(), "没有找到匹配的知识主题"),
                safe(config.getEmptyStateDescription(), "试试换一个关键词，或者先从左侧目录选一篇文章开始阅读。"),
                defaultArticleSlug,
                splitTextList(config.getQuickSearchesText()),
                buildMetrics(visibleSummaries),
                buildSpotlightCards(visibleSummaries, visibleSlugs),
                sections);
    }

    /**
     * 获取文章详情 - 带缓存优化
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "knowledgeArticle", key = "#slug", unless = "#result == null")
    public KnowledgeArticleView getArticle(String slug) {
        KnowledgeArticle article = knowledgeArticleRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new NoSuchElementException("知识库文章不存在: " + slug));
        
        // 只获取必要的数据用于构建相关文章，使用摘要查询
        List<KnowledgeArticleSummary> allSummaries = knowledgeArticleRepository.findAllPublishedSummaries();
        return toArticleView(article, allSummaries);
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
    @CacheEvict(value = {"knowledgeCatalog", "knowledgeArticle"}, allEntries = true)
    public AdminArticleDetail createArticle(AdminArticleInput input, Long operatorUserId) {
        String slug = normalizeSlug(input.slug());
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug 不能为空");
        }
        if (knowledgeArticleRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("slug 已存在，请换一个");
        }

        validateArticleInput(input, slug, null, collectAvailableSlugs(List.of(slug)));

        KnowledgeArticle article = new KnowledgeArticle();
        applyArticleInput(article, input, operatorUserId, true);
        article.setSlug(slug);
        return toAdminArticleDetail(knowledgeArticleRepository.save(article));
    }

    @Transactional
    @CacheEvict(value = {"knowledgeCatalog", "knowledgeArticle"}, allEntries = true)
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
    @CacheEvict(value = {"knowledgeCatalog", "knowledgeArticle"}, allEntries = true)
    public void deleteArticle(Long id) {
        if (!knowledgeArticleRepository.existsById(id)) {
            throw new NoSuchElementException("知识库文章不存在: " + id);
        }

        KnowledgeArticle article = knowledgeArticleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("知识库文章不存在: " + id));
        String deletingSlug = article.getSlug();

        KnowledgeBaseConfig config = getOrCreateConfig();
        if (Objects.equals(config.getDefaultArticleSlug(), deletingSlug)) {
            String nextDefaultSlug = knowledgeArticleRepository.findAllByPublishedTrueOrderBySectionIdAscSortOrderAscUpdatedAtDesc().stream()
                    .filter(item -> !Objects.equals(item.getId(), id))
                    .map(KnowledgeArticle::getSlug)
                    .findFirst()
                    .orElse("");
            config.setDefaultArticleSlug(nextDefaultSlug);
            knowledgeBaseConfigRepository.save(config);
        }

        List<KnowledgeArticle> articlesToRepair = knowledgeArticleRepository.findAllByOrderBySectionIdAscSortOrderAscUpdatedAtDesc().stream()
                .filter(item -> !Objects.equals(item.getId(), id))
                .filter(item -> splitTextList(item.getRelatedSlugsText()).contains(deletingSlug))
                .toList();
        for (KnowledgeArticle target : articlesToRepair) {
            List<String> nextRelatedSlugs = normalizeSlugList(splitTextList(target.getRelatedSlugsText())).stream()
                    .filter(s -> !Objects.equals(s, deletingSlug))
                    .toList();
            target.setRelatedSlugsText(joinTextList(nextRelatedSlugs));
        }
        if (!articlesToRepair.isEmpty()) {
            knowledgeArticleRepository.saveAll(articlesToRepair);
        }

        knowledgeArticleRepository.delete(article);
    }

    @Transactional
    @CacheEvict(value = {"knowledgeCatalog", "knowledgeArticle"}, allEntries = true)
    public BatchUpsertResult batchUpsertArticles(List<AdminArticleInput> inputs, Long operatorUserId) {
        if (inputs == null || inputs.isEmpty()) {
            return new BatchUpsertResult(0, 0, 0);
        }

        List<String> slugs = inputs.stream()
                .map(AdminArticleInput::slug)
                .map(this::normalizeSlug)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
        if (slugs.isEmpty()) {
            throw new IllegalArgumentException("No article slugs available for batch upsert");
        }

        Map<String, KnowledgeArticle> existingBySlug = knowledgeArticleRepository.findAllBySlugIn(slugs).stream()
                .collect(LinkedHashMap::new, (map, article) -> map.put(article.getSlug(), article), Map::putAll);
        Set<String> availableSlugs = collectAvailableSlugs(slugs);

        int inserted = 0;
        int updated = 0;
        List<KnowledgeArticle> toSave = new ArrayList<>();
        for (AdminArticleInput input : inputs) {
            String slug = normalizeSlug(input.slug());
            if (slug.isBlank()) {
                throw new IllegalArgumentException("slug must not be blank");
            }
            KnowledgeArticle existingArticle = existingBySlug.get(slug);
            validateArticleInput(input, slug, existingArticle == null ? null : existingArticle.getId(), availableSlugs);

            KnowledgeArticle article = existingArticle;
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
    @CacheEvict(value = "knowledgeCatalog", allEntries = true)
    public AdminConfigView updateConfig(AdminConfigInput input) {
        validateConfigInput(input);
        KnowledgeBaseConfig config = getOrCreateConfig();
        config.setSiteTitle(safe(input.siteTitle(), "AlgoMind 知识库"));
        config.setSiteSubtitle(safe(input.siteSubtitle(), "把高频算法与工程知识点整理成可持续维护的学习文档。"));
        config.setEmptyStateTitle(safe(input.emptyStateTitle(), "没有找到匹配的知识主题"));
        config.setEmptyStateDescription(safe(input.emptyStateDescription(), "试试换一个关键词，或者先从左侧目录选一篇文章开始阅读。"));
        config.setDefaultArticleSlug(normalizeSlug(input.defaultArticleSlug()));
        config.setQuickSearchesText(joinTextList(normalizeTextList(input.quickSearches())));
        return toAdminConfig(knowledgeBaseConfigRepository.save(config));
    }

    // ==================== 私有辅助方法 ====================

    private void applyArticleInput(KnowledgeArticle article, AdminArticleInput input, Long operatorUserId, boolean isCreate) {
        article.setTitle(safe(input.title(), "未命名文章"));
        article.setEnglishTitle(trimToEmpty(input.englishTitle()));
        article.setSectionId(resolveSectionId(input.sectionTitle(), input.sectionId()));
        article.setSectionTitle(safe(input.sectionTitle(), "未分类"));
        article.setSectionDescription(trimToEmpty(input.sectionDescription()));
        article.setBadge(trimToEmpty(input.badge()));
        article.setSummary(trimToEmpty(input.summary()));
        article.setLead(trimToEmpty(input.lead()));
        article.setComplexity(trimToEmpty(input.complexity()));
        article.setReadTime(trimToEmpty(input.readTime()));
        article.setTagsText(joinTextList(normalizeTextList(input.tags())));
        article.setLearningObjectivesJson(toJson(input.learningObjectives()));
        article.setStrategyStepsJson(toJson(input.strategySteps()));
        article.setInsightsJson(toJson(input.insights()));
        article.setCodeBlocksJson(toJson(input.codeBlocks()));
        article.setChecklistJson(toJson(input.checklist()));
        article.setRelatedSlugsText(joinTextList(normalizeSlugList(input.relatedArticleSlugs())));
        article.setSpotlightEnabled(Boolean.TRUE.equals(input.spotlightEnabled()));
        article.setSpotlightEyebrow(trimToEmpty(input.spotlightEyebrow()));
        article.setSpotlightTitle(trimToEmpty(input.spotlightTitle()));
        article.setSpotlightDescription(trimToEmpty(input.spotlightDescription()));
        article.setSpotlightAccent(resolveSpotlightAccent(input.spotlightAccent()));
        article.setPublished(Boolean.TRUE.equals(input.published()));
        article.setSortOrder(input.sortOrder() == null ? 0 : input.sortOrder());
        if (isCreate) {
            article.setCreatedByUserId(operatorUserId);
        }
        article.setUpdatedByUserId(operatorUserId);
    }

    private void validateConfigInput(AdminConfigInput input) {
        if (trimToEmpty(input.siteTitle()).isBlank()) {
            throw new IllegalArgumentException("站点标题不能为空");
        }

        List<String> quickSearches = normalizeTextList(input.quickSearches());
        if (quickSearches.size() > 12) {
            throw new IllegalArgumentException("推荐搜索最多保留 12 个关键词");
        }

        String defaultArticleSlug = normalizeSlug(input.defaultArticleSlug());
        if (!defaultArticleSlug.isBlank()
                && knowledgeArticleRepository.countPublished() > 0
                && knowledgeArticleRepository.findBySlugAndPublishedTrue(defaultArticleSlug).isEmpty()) {
            throw new IllegalArgumentException("默认文章必须指向一篇已发布的知识文章");
        }
    }

    private void validateArticleInput(AdminArticleInput input, String slug, Long currentId, Set<String> availableSlugs) {
        if (slug.isBlank()) {
            throw new IllegalArgumentException("slug 不能为空");
        }
        if (trimToEmpty(input.title()).isBlank()) {
            throw new IllegalArgumentException("文章标题不能为空");
        }
        if (resolveSectionId(input.sectionTitle(), input.sectionId()).isBlank()) {
            throw new IllegalArgumentException("栏目 ID 不能为空");
        }
        if (trimToEmpty(input.sectionTitle()).isBlank()) {
            throw new IllegalArgumentException("栏目名称不能为空");
        }
        if (input.sortOrder() != null && input.sortOrder() < 0) {
            throw new IllegalArgumentException("排序值不能为负数");
        }

        List<String> relatedSlugs = normalizeSlugList(input.relatedArticleSlugs());
        if (relatedSlugs.contains(slug)) {
            throw new IllegalArgumentException("相关文章不能引用自己");
        }
        if (Boolean.TRUE.equals(input.spotlightEnabled())) {
            if (trimToEmpty(input.spotlightTitle()).isBlank()) {
                throw new IllegalArgumentException("启用推荐位时必须填写推荐标题");
            }
            if (trimToEmpty(input.spotlightDescription()).isBlank()) {
                throw new IllegalArgumentException("启用推荐位时必须填写推荐描述");
            }
        }

        for (KnowledgeStep step : safeList(input.strategySteps())) {
            boolean hasAnyValue = !trimToEmpty(step.index()).isBlank()
                    || !trimToEmpty(step.title()).isBlank()
                    || !trimToEmpty(step.description()).isBlank()
                    || !trimToEmpty(step.badge()).isBlank();
            if (hasAnyValue && (trimToEmpty(step.title()).isBlank() || trimToEmpty(step.description()).isBlank())) {
                throw new IllegalArgumentException("每个步骤都需要同时填写标题和描述");
            }
        }

        for (KnowledgeInsight insight : safeList(input.insights())) {
            boolean hasAnyValue = !trimToEmpty(insight.title()).isBlank()
                    || !trimToEmpty(insight.description()).isBlank();
            if (hasAnyValue && (trimToEmpty(insight.title()).isBlank() || trimToEmpty(insight.description()).isBlank())) {
                throw new IllegalArgumentException("每条洞察都需要同时填写标题和描述");
            }
            if (!trimToEmpty(insight.accent()).isBlank()
                    && !List.of("emerald", "cyan", "amber").contains(trimToEmpty(insight.accent()))) {
                throw new IllegalArgumentException("洞察主题色仅支持 emerald、cyan、amber");
            }
        }

        for (KnowledgeCodeBlock codeBlock : safeList(input.codeBlocks())) {
            boolean hasAnyValue = !trimToEmpty(codeBlock.language()).isBlank()
                    || !trimToEmpty(codeBlock.title()).isBlank()
                    || !trimToEmpty(codeBlock.code()).isBlank()
                    || !safeList(codeBlock.callouts()).isEmpty();
            if (hasAnyValue && (trimToEmpty(codeBlock.title()).isBlank() || trimToEmpty(codeBlock.code()).isBlank())) {
                throw new IllegalArgumentException("代码块需要同时填写标题和代码");
            }
        }
    }

    private Set<String> collectAvailableSlugs(Collection<String> currentInputSlugs) {
        Set<String> slugs = knowledgeArticleRepository.findAllByOrderBySectionIdAscSortOrderAscUpdatedAtDesc().stream()
                .map(KnowledgeArticle::getSlug)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        slugs.addAll(normalizeSlugList(currentInputSlugs));
        return slugs;
    }

    private List<String> normalizeSlugList(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::normalizeSlug)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private List<String> normalizeTextList(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::trimToEmpty)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private String resolveSpotlightAccent(String value) {
        String accent = trimToEmpty(value);
        if (accent.isBlank()) {
            return "emerald";
        }
        if (!List.of("emerald", "cyan", "amber").contains(accent)) {
            throw new IllegalArgumentException("推荐位主题色仅支持 emerald、cyan、amber");
        }
        return accent;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    /**
     * 从摘要构建目录分区 - 优化版本
     */
    private List<KnowledgeSection> buildSectionsFromSummaries(List<KnowledgeArticleSummary> summaries) {
        Map<String, List<KnowledgeArticleSummary>> grouped = new LinkedHashMap<>();
        for (KnowledgeArticleSummary summary : summaries) {
            grouped.computeIfAbsent(summary.sectionId(), ignored -> new ArrayList<>()).add(summary);
        }

        List<KnowledgeSection> sections = new ArrayList<>();
        for (Map.Entry<String, List<KnowledgeArticleSummary>> entry : grouped.entrySet()) {
            List<KnowledgeArticleSummary> sectionArticles = entry.getValue();
            KnowledgeArticleSummary first = sectionArticles.getFirst();
            sections.add(new KnowledgeSection(
                    safe(first.sectionId()),
                    safe(first.sectionTitle(), "未分类"),
                    safe(first.sectionDescription()),
                    sectionArticles.stream().map(this::toNavItem).toList()));
        }
        return sections;
    }

    private KnowledgeNavItem toNavItem(KnowledgeArticleSummary summary) {
        return new KnowledgeNavItem(
                summary.slug(),
                safe(summary.title()),
                safe(summary.lead()),
                safe(summary.badge()),
                splitTextList(summary.tagsText()),
                safe(summary.readTime()));
    }

    /**
     * 从摘要构建统计指标 - 优化版本
     */
    private List<KnowledgeMetric> buildMetrics(List<KnowledgeArticleSummary> summaries) {
        int articleCount = summaries.size();
        
        // 对于统计，我们只需要数量，不需要加载完整文章
        // 如果需要精确的代码块数量，可以考虑添加计数缓存
        return List.of(
                new KnowledgeMetric("文档主题", String.valueOf(articleCount), "支持持续维护和新增文章"),
                new KnowledgeMetric("示例代码", "-", "每篇都可以挂多段代码模板"),
                new KnowledgeMetric("复盘清单", "-", "帮助把知识点变成答题动作"));
    }

    /**
     * 从摘要构建推荐卡片 - 优化版本
     */
    private List<KnowledgeSpotlightCard> buildSpotlightCards(List<KnowledgeArticleSummary> summaries, Set<String> visibleSlugs) {
        return summaries.stream()
                .filter(summary -> Boolean.TRUE.equals(summary.spotlightEnabled()))
                .filter(summary -> visibleSlugs.contains(summary.slug()))
                .map(summary -> new KnowledgeSpotlightCard(
                        safe(summary.spotlightEyebrow(), "推荐主题"),
                        safe(summary.spotlightTitle(), summary.title()),
                        safe(summary.spotlightDescription(), summary.lead()),
                        summary.slug(),
                        safe(summary.spotlightAccent(), "emerald")))
                .limit(4)
                .toList();
    }

    /**
     * 优化后的文章视图转换 - 使用摘要列表代替完整文章列表
     */
    private KnowledgeArticleView toArticleView(KnowledgeArticle article, List<KnowledgeArticleSummary> sourceSummaries) {
        Map<String, KnowledgeArticleSummary> summaryMap = sourceSummaries.stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.slug(), item), Map::putAll);

        List<KnowledgeRelatedArticle> relatedArticles = splitTextList(article.getRelatedSlugsText()).stream()
                .map(summaryMap::get)
                .filter(Objects::nonNull)
                .map(related -> new KnowledgeRelatedArticle(related.slug(), related.title(), related.sectionTitle()))
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

    private String resolveSectionId(String sectionTitle, String fallbackSectionId) {
        String normalizedTitle = normalizeSlug(sectionTitle);
        if (!normalizedTitle.isBlank()) {
            return normalizedTitle;
        }

        String rawTitle = trimToEmpty(sectionTitle);
        if (!rawTitle.isBlank()) {
            return "section-" + buildStableSectionHash(rawTitle);
        }

        return normalizeSlug(fallbackSectionId);
    }

    private String buildStableSectionHash(String value) {
        long hash = 0L;
        String normalized = trimToEmpty(value).toLowerCase(Locale.ROOT);

        for (int index = 0; index < normalized.length(); index += 1) {
            hash = (hash * 131 + normalized.charAt(index)) & 0xffffffffL;
        }

        return Long.toString(hash, 36);
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

    // ==================== 记录类定义 ====================

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
