﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'
import FlowerPagination from '../components/FlowerPagination.vue'
import CodeBlock from '../components/CodeBlock.vue'
import AlgorithmKnowledgeGraph from '../components/AlgorithmKnowledgeGraph.vue'
import {
  KNOWLEDGE_ADMIN_DASHBOARD_CACHE_KEY,
  KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL,
  buildKnowledgeAdminStats,
  normalizeKnowledgeSlug,
  readTimedCache,
  serializeComparable,
  validateKnowledgeAdminArticle,
  validateKnowledgeAdminConfig,
  writeTimedCache,
} from '../utils/knowledgeBaseAdmin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const createEmptyCatalog = () => ({
  title: 'AlgoMind 算法知识专区',
  subtitle: '',
  emptyStateTitle: '暂无内容',
  emptyStateDescription: '',
  defaultArticleSlug: '',
  quickSearches: [],
  metrics: [],
  spotlightCards: [],
  sections: [],
})

const createEmptyAdminConfig = () => ({
  siteTitle: 'AlgoMind 算法知识专区',
  siteSubtitle: '',
  emptyStateTitle: '没有找到匹配的知识主题',
  emptyStateDescription: '',
  defaultArticleSlug: '',
  quickSearchesText: '',
})

const createEmptyStep = () => ({
  index: '01',
  title: '',
  description: '',
  badge: '',
})

const createEmptyInsight = () => ({
  title: '',
  description: '',
  accent: 'emerald',
})

const createEmptyCodeBlock = () => ({
  language: 'Java',
  title: '',
  code: '',
  calloutsText: '',
})

const createEmptyArticleForm = () => ({
  id: null,
  slug: '',
  title: '',
  englishTitle: '',
  sectionId: 'algorithm-basics',
  sectionTitle: '算法基础',
  sectionDescription: '',
  badge: '',
  summary: '',
  lead: '',
  complexity: '',
  readTime: '',
  tagsText: '',
  learningObjectivesText: '',
  checklistText: '',
  relatedSlugsText: '',
  spotlightEnabled: false,
  spotlightEyebrow: '',
  spotlightTitle: '',
  spotlightDescription: '',
  spotlightAccent: 'emerald',
  published: true,
  sortOrder: 10,
  strategySteps: [createEmptyStep()],
  insights: [createEmptyInsight()],
  codeBlocks: [createEmptyCodeBlock()],
})

const ensureArrayItems = (items, factory) => (items && items.length ? items : [factory()])

const normalizeCatalog = (payload = {}) => ({
  ...createEmptyCatalog(),
  ...payload,
  quickSearches: Array.isArray(payload.quickSearches) ? payload.quickSearches : [],
  metrics: Array.isArray(payload.metrics) ? payload.metrics : [],
  spotlightCards: Array.isArray(payload.spotlightCards) ? payload.spotlightCards : [],
  sections: Array.isArray(payload.sections)
    ? payload.sections.map((section) => ({
      ...section,
      articles: Array.isArray(section.articles) ? section.articles : [],
    }))
    : [],
})

const normalizeArticle = (payload = null) => {
  if (!payload) return null

  return {
    ...payload,
    tags: Array.isArray(payload.tags) ? payload.tags : [],
    learningObjectives: Array.isArray(payload.learningObjectives) ? payload.learningObjectives : [],
    strategySteps: Array.isArray(payload.strategySteps) ? payload.strategySteps : [],
    insights: Array.isArray(payload.insights) ? payload.insights : [],
    codeBlocks: Array.isArray(payload.codeBlocks)
      ? payload.codeBlocks.map((block) => ({
        ...block,
        callouts: Array.isArray(block.callouts) ? block.callouts : [],
      }))
      : [],
    checklist: Array.isArray(payload.checklist) ? payload.checklist : [],
    relatedArticles: Array.isArray(payload.relatedArticles) ? payload.relatedArticles : [],
  }
}

const normalizeAdminDashboard = (payload = {}) => ({
  config: {
    ...createEmptyAdminConfig(),
    ...(payload.config || {}),
    quickSearchesText: Array.isArray(payload.config?.quickSearches)
      ? payload.config.quickSearches.join(', ')
      : '',
  },
  articles: Array.isArray(payload.articles) ? payload.articles : [],
})

const catalog = ref(createEmptyCatalog())
const currentArticle = ref(null)
const currentSlug = ref('')
const searchDraft = ref('')
const loadingCatalog = ref(false)
const loadingArticle = ref(false)
const errorMessage = ref('')
const showRelatedPanel = ref(false)
let relatedPanelTimer = null
const graphOverlayOpen = ref(false)

const articleCache = new Map()
let catalogRequestId = 0
let articleRequestId = 0

const adminDashboard = ref({ config: createEmptyAdminConfig(), articles: [] })
const adminConfigForm = ref(createEmptyAdminConfig())
const adminArticleForm = ref(createEmptyArticleForm())
const adminPanelOpen = ref(false)
const adminLoading = ref(false)
const adminSavingConfig = ref(false)
const adminSavingArticle = ref(false)
const adminDeletingArticle = ref(false)
const selectedAdminArticleId = ref(null)
const adminErrorMessage = ref('')
const adminListKeyword = ref('')
const adminStatusFilter = ref('all')
const adminConfigBaseline = ref(serializeComparable(createEmptyAdminConfig()))
const adminArticleBaseline = ref(serializeComparable(createEmptyArticleForm()))

const adminArticleDetailCache = new Map()

const routeKeyword = computed(() => String(route.query.keyword || '').trim())
const routeArticleSlug = computed(() => String(route.query.article || '').trim())
const sections = computed(() => catalog.value.sections || [])
const allNavItems = computed(() => sections.value.flatMap((section) => section.articles || []))
const KNOWLEDGE_MAP_HISTORY_KEY = 'knowledge-base-recent-reads'
const KNOWLEDGE_MAP_SORT_OPTIONS = [
  { value: 'path', label: '学习路径' },
  { value: 'recent', label: '最近阅读' },
  { value: 'alpha', label: '字母排序' },
]
const KNOWLEDGE_PATH_STAGES = [
  {
    id: 'basics',
    title: '基础入口',
    subtitle: '数组、字符串、哈希与基础技巧',
    accent: 'sunrise',
  },
  {
    id: 'linear',
    title: '线性结构',
    subtitle: '链表、栈、队列与双指针',
    accent: 'azure',
  },
  {
    id: 'search',
    title: '搜索遍历',
    subtitle: '二分、滑窗、DFS 与 BFS',
    accent: 'violet',
  },
  {
    id: 'trees',
    title: '树图结构',
    subtitle: '树、图、堆与图论建模',
    accent: 'mint',
  },
  {
    id: 'strategy',
    title: '策略模型',
    subtitle: '递归、分治、贪心与动态规划',
    accent: 'amber',
  },
  {
    id: 'advanced',
    title: '综合专题',
    subtitle: '位运算、数学、状态与区间问题',
    accent: 'ocean',
  },
]
const knowledgeMapSort = ref('path')
const SIDEBAR_PAGE_SIZE = 5
const sidebarPage = ref(1)
const visibleArticleCount = computed(() =>
  sections.value.reduce((total, section) => total + (section.articles?.length || 0), 0),
)
const sidebarItems = computed(() =>
  sections.value.flatMap((section) =>
    (section.articles || []).map((item) => ({
      ...item,
      sectionId: section.id,
      sectionTitle: section.title,
      sectionDescription: section.description,
    })),
  ),
)
const totalSidebarPages = computed(() =>
  Math.max(1, Math.ceil(sidebarItems.value.length / SIDEBAR_PAGE_SIZE)),
)
const hasSidebarPagination = computed(() => visibleArticleCount.value > SIDEBAR_PAGE_SIZE)
const sidebarVisibleRange = computed(() => {
  if (!sidebarItems.value.length) {
    return { start: 0, end: 0 }
  }

  const start = (sidebarPage.value - 1) * SIDEBAR_PAGE_SIZE + 1
  const end = Math.min(sidebarPage.value * SIDEBAR_PAGE_SIZE, sidebarItems.value.length)
  return { start, end }
})
const sidebarRemainingCount = computed(() =>
  Math.max(visibleArticleCount.value - sidebarVisibleRange.value.end, 0),
)
const pagedSidebarSections = computed(() => {
  const startIndex = (sidebarPage.value - 1) * SIDEBAR_PAGE_SIZE
  const pagedItems = sidebarItems.value.slice(startIndex, startIndex + SIDEBAR_PAGE_SIZE)
  const groupedSections = []
  const sectionMap = new Map()

  for (const item of pagedItems) {
    if (!sectionMap.has(item.sectionId)) {
      const sectionGroup = {
        id: item.sectionId,
        title: item.sectionTitle,
        description: item.sectionDescription,
        articles: [],
      }
      sectionMap.set(item.sectionId, sectionGroup)
      groupedSections.push(sectionGroup)
    }

    sectionMap.get(item.sectionId).articles.push(item)
  }

  return groupedSections
})
const sidebarPageItems = computed(() => {
  const total = totalSidebarPages.value
  const current = sidebarPage.value

  if (total <= 7) {
    return Array.from({ length: total }, (_, index) => ({
      type: 'page',
      value: index + 1,
      key: `page-${index + 1}`,
    }))
  }

  const items = [{ type: 'page', value: 1, key: 'page-1' }]
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)

  if (start > 2) {
    items.push({ type: 'ellipsis', value: 'start', key: 'ellipsis-start' })
  }

  for (let page = start; page <= end; page += 1) {
    items.push({ type: 'page', value: page, key: `page-${page}` })
  }

  if (end < total - 1) {
    items.push({ type: 'ellipsis', value: 'end', key: 'ellipsis-end' })
  }

  items.push({ type: 'page', value: total, key: `page-${total}` })
  return items
})
const activeNavItem = computed(() =>
  allNavItems.value.find((item) => item.slug === currentSlug.value) || null,
)
const recentReadSlugs = ref([])

const readKnowledgeMapHistory = () => {
  if (typeof window === 'undefined') {
    return []
  }

  try {
    const raw = window.localStorage.getItem(KNOWLEDGE_MAP_HISTORY_KEY)
    const parsed = JSON.parse(raw || '[]')
    return Array.isArray(parsed) ? parsed.filter((item) => typeof item === 'string' && item.trim()) : []
  } catch {
    return []
  }
}

const writeKnowledgeMapHistory = (slugs) => {
  if (typeof window === 'undefined') {
    return
  }

  window.localStorage.setItem(KNOWLEDGE_MAP_HISTORY_KEY, JSON.stringify(slugs))
}

recentReadSlugs.value = readKnowledgeMapHistory()

const knowledgeMapSortLabel = computed(() =>
  KNOWLEDGE_MAP_SORT_OPTIONS.find((option) => option.value === knowledgeMapSort.value)?.label || '学习路径',
)

// 将搜索词和文章节点整合成一份“知识地图”数据源，保持视觉地图与现有搜索/文章路由同步。
const knowledgeMapCandidates = computed(() => {
  const candidates = []
  const seen = new Set()

  const appendCandidate = (candidate) => {
    const label = String(candidate.label || '').trim()
    if (!label || seen.has(label)) {
      return
    }

    seen.add(label)
    candidates.push({
      ...candidate,
      label,
      keyword: String(candidate.keyword || label).trim(),
    })
  }

  catalog.value.quickSearches.forEach((quick, index) => {
    appendCandidate({
      id: `quick-${index}-${quick}`,
      label: quick,
      keyword: quick,
      slug: '',
      sectionTitle: '快捷检索',
      priority: index,
      source: 'quick',
    })
  })

  sidebarItems.value.forEach((item, index) => {
    appendCandidate({
      id: `article-${item.slug}`,
      label: item.title,
      keyword: item.title,
      slug: item.slug,
      sectionTitle: item.sectionTitle,
      priority: catalog.value.quickSearches.length + index,
      source: 'article',
    })
  })

  return candidates.slice(0, 18)
})

const includesAnyKeyword = (text, keywords) => keywords.some((keyword) => text.includes(keyword))

// 按算法学习路径分层：先基础与线性结构，再到搜索、树图、策略模型，最后汇总到综合专题。
const resolveKnowledgeStageId = (candidate) => {
  const stageText = `${candidate.label} ${candidate.keyword} ${candidate.sectionTitle}`.toLowerCase()

  if (includesAnyKeyword(stageText, ['位运算', '数学', '计数', '数位', '区间', '状态', '概率', '博弈'])) {
    return 'advanced'
  }

  if (includesAnyKeyword(stageText, ['动态规划', '贪心', '递归', '分治', '背包'])) {
    return 'strategy'
  }

  if (includesAnyKeyword(stageText, ['树形', '图论', '普通树', '树', '图', '堆', '最短路', '拓扑', '并查集', '线段树'])) {
    return 'trees'
  }

  if (includesAnyKeyword(stageText, ['二分', '滑动窗口', '搜索', 'dfs', 'bfs', '深度优先', '广度优先'])) {
    return 'search'
  }

  if (includesAnyKeyword(stageText, ['链表', '栈', '队列', '双指针', '指针'])) {
    return 'linear'
  }

  return 'basics'
}

const sortKnowledgeStageNodes = (nodes) => {
  const stageNodes = [...nodes]

  if (knowledgeMapSort.value === 'alpha') {
    return stageNodes.sort((left, right) => left.label.localeCompare(right.label, 'zh-CN'))
  }

  if (knowledgeMapSort.value === 'recent') {
    const historyRank = new Map(
      [currentSlug.value, ...recentReadSlugs.value]
        .filter(Boolean)
        .map((slug, index) => [slug, index]),
    )

    return stageNodes.sort((left, right) => {
      const leftRank = historyRank.has(left.slug) ? historyRank.get(left.slug) : Number.MAX_SAFE_INTEGER
      const rightRank = historyRank.has(right.slug) ? historyRank.get(right.slug) : Number.MAX_SAFE_INTEGER

      if (leftRank !== rightRank) {
        return leftRank - rightRank
      }

      if (left.source !== right.source) {
        return left.source === 'article' ? -1 : 1
      }

      return left.priority - right.priority
    })
  }

  return stageNodes.sort((left, right) => left.priority - right.priority)
}

const knowledgeMapStages = computed(() => {
  const stageMap = new Map(
    KNOWLEDGE_PATH_STAGES.map((stage, index) => [
      stage.id,
      {
        ...stage,
        stepLabel: `${index + 1}`.padStart(2, '0'),
        nodes: [],
      },
    ]),
  )

  knowledgeMapCandidates.value.forEach((candidate) => {
    const stageId = resolveKnowledgeStageId(candidate)
    stageMap.get(stageId)?.nodes.push(candidate)
  })

  return KNOWLEDGE_PATH_STAGES
    .map((stage, index) => {
      const stageBucket = stageMap.get(stage.id)
      return {
        ...stageBucket,
        stepLabel: `${index + 1}`.padStart(2, '0'),
        nodes: sortKnowledgeStageNodes(stageBucket?.nodes || []),
      }
    })
    .filter((stage) => stage.nodes.length)
})

const handleKnowledgeMapNodeClick = (node) => {
  if (node.slug) {
    selectArticle(node.slug)
    return
  }

  applyQuickSearch(node.keyword)
}
const activeSection = computed(() =>
  sections.value.find((section) => section.id === currentArticle.value?.sectionId)
  || sections.value.find((section) => section.articles?.some((item) => item.slug === currentSlug.value))
  || null,
)
const adminArticles = computed(() => adminDashboard.value.articles || [])
const isAdmin = computed(() =>
  Boolean(userStore.userInfo?.isAdmin) || userStore.userInfo?.email === 'admin@example.com',
)
const adminStats = computed(() => buildKnowledgeAdminStats(adminArticles.value))
const filteredAdminArticles = computed(() => {
  const keyword = adminListKeyword.value.trim().toLowerCase()

  return adminArticles.value.filter((item) => {
    const matchesKeyword = !keyword
      || item.title?.toLowerCase().includes(keyword)
      || item.slug?.toLowerCase().includes(keyword)
      || item.sectionTitle?.toLowerCase().includes(keyword)
    const matchesStatus = adminStatusFilter.value === 'all'
      || (adminStatusFilter.value === 'published' && item.published)
      || (adminStatusFilter.value === 'draft' && !item.published)
      || (adminStatusFilter.value === 'spotlight' && item.spotlightEnabled)

    return matchesKeyword && matchesStatus
  })
})
const adminConfigValidation = computed(() =>
  validateKnowledgeAdminConfig(adminConfigForm.value, adminArticles.value),
)
const adminArticleValidation = computed(() =>
  validateKnowledgeAdminArticle(
    adminArticleForm.value,
    adminArticles.value,
    selectedAdminArticleId.value,
  ),
)

const toTextLines = (value) => value
  .split('\n')
  .map((item) => item.trim())
  .filter(Boolean)

const toCommaItems = (value) => value
  .split(/[，,]/)
  .map((item) => item.trim())
  .filter(Boolean)

const getSessionStorage = () => (typeof window !== 'undefined' ? window.sessionStorage : null)

const buildAdminConfigPayload = (validation = adminConfigValidation.value) => ({
  siteTitle: adminConfigForm.value.siteTitle.trim(),
  siteSubtitle: adminConfigForm.value.siteSubtitle.trim(),
  emptyStateTitle: adminConfigForm.value.emptyStateTitle.trim(),
  emptyStateDescription: adminConfigForm.value.emptyStateDescription.trim(),
  defaultArticleSlug: validation.normalized.defaultArticleSlug,
  quickSearches: validation.normalized.quickSearches,
})

const setAdminArticleForm = (payload = {}) => {
  adminArticleForm.value = {
    ...createEmptyArticleForm(),
    ...payload,
    tagsText: Array.isArray(payload.tags) ? payload.tags.join(', ') : (payload.tagsText || ''),
    learningObjectivesText: Array.isArray(payload.learningObjectives)
      ? payload.learningObjectives.join('\n')
      : (payload.learningObjectivesText || ''),
    checklistText: Array.isArray(payload.checklist)
      ? payload.checklist.join('\n')
      : (payload.checklistText || ''),
    relatedSlugsText: Array.isArray(payload.relatedArticleSlugs)
      ? payload.relatedArticleSlugs.join(', ')
      : (payload.relatedSlugsText || ''),
    strategySteps: ensureArrayItems(
      Array.isArray(payload.strategySteps) ? payload.strategySteps.map((step) => ({ ...createEmptyStep(), ...step })) : [],
      createEmptyStep,
    ),
    insights: ensureArrayItems(
      Array.isArray(payload.insights) ? payload.insights.map((insight) => ({ ...createEmptyInsight(), ...insight })) : [],
      createEmptyInsight,
    ),
    codeBlocks: ensureArrayItems(
      Array.isArray(payload.codeBlocks)
        ? payload.codeBlocks.map((block) => ({
          ...createEmptyCodeBlock(),
          ...block,
          calloutsText: Array.isArray(block.callouts) ? block.callouts.join('\n') : (block.calloutsText || ''),
        }))
        : [],
      createEmptyCodeBlock,
    ),
  }
}

const buildAdminArticlePayload = (validation = adminArticleValidation.value) => ({
  slug: validation.normalized.slug,
  title: adminArticleForm.value.title.trim(),
  englishTitle: adminArticleForm.value.englishTitle.trim(),
  sectionId: validation.normalized.sectionId,
  sectionTitle: adminArticleForm.value.sectionTitle.trim(),
  sectionDescription: adminArticleForm.value.sectionDescription.trim(),
  badge: adminArticleForm.value.badge.trim(),
  summary: adminArticleForm.value.summary.trim(),
  lead: adminArticleForm.value.lead.trim(),
  complexity: adminArticleForm.value.complexity.trim(),
  readTime: adminArticleForm.value.readTime.trim(),
  tags: toCommaItems(adminArticleForm.value.tagsText),
  learningObjectives: toTextLines(adminArticleForm.value.learningObjectivesText),
  checklist: toTextLines(adminArticleForm.value.checklistText),
  relatedArticleSlugs: validation.normalized.relatedSlugs,
  spotlightEnabled: Boolean(adminArticleForm.value.spotlightEnabled),
  spotlightEyebrow: adminArticleForm.value.spotlightEyebrow.trim(),
  spotlightTitle: adminArticleForm.value.spotlightTitle.trim(),
  spotlightDescription: adminArticleForm.value.spotlightDescription.trim(),
  spotlightAccent: adminArticleForm.value.spotlightAccent.trim(),
  published: Boolean(adminArticleForm.value.published),
  sortOrder: Number(adminArticleForm.value.sortOrder || 0),
  strategySteps: adminArticleForm.value.strategySteps
    .map((step, index) => ({
      index: (step.index || `${index + 1}`.padStart(2, '0')).trim(),
      title: step.title.trim(),
      description: step.description.trim(),
      badge: step.badge.trim(),
    }))
    .filter((step) => step.title || step.description),
  insights: adminArticleForm.value.insights
    .map((insight) => ({
      title: insight.title.trim(),
      description: insight.description.trim(),
      accent: (insight.accent || 'emerald').trim(),
    }))
    .filter((insight) => insight.title || insight.description),
  codeBlocks: adminArticleForm.value.codeBlocks
    .map((block) => ({
      language: block.language.trim(),
      title: block.title.trim(),
      code: block.code,
      callouts: toTextLines(block.calloutsText || ''),
    }))
    .filter((block) => block.title || block.code),
})

const snapshotAdminConfig = () => serializeComparable(buildAdminConfigPayload())
const snapshotAdminArticle = () => serializeComparable(buildAdminArticlePayload())

const syncAdminConfigBaseline = () => {
  adminConfigBaseline.value = snapshotAdminConfig()
}

const syncAdminArticleBaseline = () => {
  adminArticleBaseline.value = snapshotAdminArticle()
}

const adminHasDirtyConfig = computed(() => snapshotAdminConfig() !== adminConfigBaseline.value)
const adminHasDirtyArticle = computed(() => snapshotAdminArticle() !== adminArticleBaseline.value)
const adminPendingChangesCount = computed(() =>
  Number(adminHasDirtyConfig.value) + Number(adminHasDirtyArticle.value),
)
const adminStatusText = computed(() => {
  if (adminLoading.value) {
    return '管理台数据同步中...'
  }
  if (adminErrorMessage.value) {
    return adminErrorMessage.value
  }
  if (adminHasDirtyArticle.value && adminArticleValidation.value.errors.length) {
    return `当前文章还有 ${adminArticleValidation.value.errors.length} 个待修正项`
  }
  if (adminHasDirtyConfig.value && adminConfigValidation.value.errors.length) {
    return `运营配置还有 ${adminConfigValidation.value.errors.length} 个待修正项`
  }
  if (adminPendingChangesCount.value) {
    return `当前有 ${adminPendingChangesCount.value} 处未保存修改`
  }
  return `已发布 ${adminStats.value.published} 篇，草稿 ${adminStats.value.drafts} 篇`
})
const adminEditorValidationMessages = computed(() => {
  if (!adminHasDirtyArticle.value && !selectedAdminArticleId.value) {
    return []
  }
  return adminArticleValidation.value.errors
})
const adminConfigValidationMessages = computed(() =>
  adminHasDirtyConfig.value ? adminConfigValidation.value.errors : [],
)

const applyAdminDashboard = (payload) => {
  const dashboard = normalizeAdminDashboard(payload)
  adminDashboard.value = dashboard
  adminConfigForm.value = { ...dashboard.config }
  syncAdminConfigBaseline()

  const selectedStillExists = dashboard.articles.some((item) => item.id === selectedAdminArticleId.value)
  if (!selectedStillExists && selectedAdminArticleId.value != null) {
    selectedAdminArticleId.value = null
    setAdminArticleForm()
    syncAdminArticleBaseline()
  }
}

const refreshAdminDashboard = async () => {
  if ((adminHasDirtyConfig.value || adminHasDirtyArticle.value)
    && !window.confirm('刷新会覆盖当前未保存的管理台修改，确认继续吗？')) {
    return
  }

  await loadAdminDashboard({ preferCache: false })
}

const toggleAdminPanel = () => {
  adminPanelOpen.value = !adminPanelOpen.value
  if (adminPanelOpen.value && !adminDashboard.value.articles.length) {
    void loadAdminDashboard()
  }
}

const articleObjectives = computed(() => currentArticle.value?.learningObjectives || [])
const articleChecklist = computed(() => currentArticle.value?.checklist || [])
const articleRelated = computed(() => currentArticle.value?.relatedArticles || [])

watch(articleRelated, (related) => {
  if (relatedPanelTimer) {
    clearTimeout(relatedPanelTimer)
    relatedPanelTimer = null
  }
  if (related?.length) {
    showRelatedPanel.value = true
    relatedPanelTimer = setTimeout(() => {
      showRelatedPanel.value = false
    }, 10000)
  } else {
    showRelatedPanel.value = false
  }
})

const getSidebarPageForSlug = (slug) => {
  const itemIndex = sidebarItems.value.findIndex((item) => item.slug === slug)
  return itemIndex >= 0 ? Math.floor(itemIndex / SIDEBAR_PAGE_SIZE) + 1 : 1
}

const setSidebarPage = (page) => {
  sidebarPage.value = Math.min(Math.max(page, 1), totalSidebarPages.value)
}

const goToSidebarPage = (page) => {
  setSidebarPage(page)
}

const goToPreviousSidebarPage = () => {
  setSidebarPage(sidebarPage.value - 1)
}

const goToNextSidebarPage = () => {
  setSidebarPage(sidebarPage.value + 1)
}

const buildQuery = (keyword, articleSlug) => {
  const nextQuery = { ...route.query }

  if (keyword) nextQuery.keyword = keyword
  else delete nextQuery.keyword

  if (articleSlug) nextQuery.article = articleSlug
  else delete nextQuery.article

  return nextQuery
}

const replaceQuery = async (keyword, articleSlug) => {
  const normalizedKeyword = (keyword || '').trim()
  const normalizedSlug = (articleSlug || '').trim()

  if (normalizedKeyword === routeKeyword.value && normalizedSlug === routeArticleSlug.value) {
    return
  }

  await router.replace({ query: buildQuery(normalizedKeyword, normalizedSlug) })
}

const loadArticle = async (slug) => {
  if (!slug) {
    currentSlug.value = ''
    currentArticle.value = null
    return
  }

  currentSlug.value = slug

  if (articleCache.has(slug)) {
    currentArticle.value = articleCache.get(slug)
    return
  }

  loadingArticle.value = true
  const requestId = ++articleRequestId

  try {
    const response = await api.getKnowledgeArticle(slug)
    if (requestId !== articleRequestId) return

    const article = normalizeArticle(response?.data?.data)
    articleCache.set(slug, article)
    if (currentSlug.value === slug) {
      currentArticle.value = article
    }
  } catch (error) {
    if (requestId !== articleRequestId) return
    currentArticle.value = null
    ElMessage.error('知识详情加载失败，请稍后重试。')
  } finally {
    if (requestId === articleRequestId) {
      loadingArticle.value = false
    }
  }
}

const loadKnowledgeBase = async (keyword, desiredSlug) => {
  loadingCatalog.value = true
  errorMessage.value = ''
  const requestId = ++catalogRequestId

  try {
    const response = await api.getKnowledgeCatalog(keyword ? { keyword } : {})
    if (requestId !== catalogRequestId) return

    const nextCatalog = normalizeCatalog(response?.data?.data)
    catalog.value = nextCatalog
    searchDraft.value = keyword
    articleCache.clear()

    const visibleSlugs = new Set(
      nextCatalog.sections.flatMap((section) => section.articles.map((item) => item.slug)),
    )
    const resolvedSlug = visibleSlugs.has(desiredSlug)
      ? desiredSlug
      : (nextCatalog.defaultArticleSlug || '')

    if (!resolvedSlug) {
      currentSlug.value = ''
      currentArticle.value = null
      if (routeArticleSlug.value) {
        void replaceQuery(keyword, '')
      }
      return
    }

    if (resolvedSlug !== routeArticleSlug.value || keyword !== routeKeyword.value) {
      void replaceQuery(keyword, resolvedSlug)
    }

    await loadArticle(resolvedSlug)
  } catch (error) {
    if (requestId !== catalogRequestId) return
    catalog.value = createEmptyCatalog()
    currentSlug.value = ''
    currentArticle.value = null
    errorMessage.value = '知识库数据加载失败，请稍后重试。'
    ElMessage.error(errorMessage.value)
  } finally {
    if (requestId === catalogRequestId) {
      loadingCatalog.value = false
    }
  }
}

const handleSearchSubmit = () => {
  void replaceQuery(searchDraft.value, '')
}

const clearSearch = () => {
  searchDraft.value = ''
  void replaceQuery('', currentSlug.value)
}

const selectArticle = (slug) => {
  void replaceQuery(routeKeyword.value, slug)
}

const openSpotlight = (slug) => {
  void replaceQuery('', slug)
}

const applyQuickSearch = (term) => {
  searchDraft.value = term
  void replaceQuery(term, '')
}

const openRelatedArticle = (slug) => {
  showRelatedPanel.value = false
  if (relatedPanelTimer) {
    clearTimeout(relatedPanelTimer)
    relatedPanelTimer = null
  }
  void replaceQuery('', slug)
}

const goToKnowledgeBaseAdmin = () => {
  void router.push({ name: 'knowledge-base-admin' })
}

const loadAdminDashboard = async ({ preferCache = true } = {}) => {
  if (!isAdmin.value) return

  const storage = getSessionStorage()
  const cachedDashboard = preferCache
    ? readTimedCache(
      KNOWLEDGE_ADMIN_DASHBOARD_CACHE_KEY,
      storage,
      KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL,
    )
    : null

  if (cachedDashboard && !adminDashboard.value.articles.length) {
    applyAdminDashboard(cachedDashboard)
  }

  adminLoading.value = true
  adminErrorMessage.value = ''
  try {
    const response = await api.getKnowledgeAdminDashboard()
    const dashboard = response?.data?.data || {}
    applyAdminDashboard(dashboard)
    writeTimedCache(KNOWLEDGE_ADMIN_DASHBOARD_CACHE_KEY, dashboard, storage)
  } catch (error) {
    adminErrorMessage.value = error?.response?.data?.message || '知识库管理台加载失败。'
    ElMessage.error(error?.response?.data?.message || '知识库管理台加载失败。')
  } finally {
    adminLoading.value = false
  }
}

const loadAdminArticle = async (id) => {
  if (!id) return

  if (adminArticleDetailCache.has(id)) {
    const detail = adminArticleDetailCache.get(id)
    selectedAdminArticleId.value = detail.id
    setAdminArticleForm(detail)
    syncAdminArticleBaseline()
    adminPanelOpen.value = true
    return
  }

  adminLoading.value = true
  try {
    const response = await api.getKnowledgeAdminArticle(id)
    const detail = response?.data?.data
    adminArticleDetailCache.set(id, detail)
    selectedAdminArticleId.value = detail.id
    setAdminArticleForm(detail)
    syncAdminArticleBaseline()
    adminPanelOpen.value = true
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '文章详情加载失败。')
  } finally {
    adminLoading.value = false
  }
}

const openNewAdminArticle = () => {
  selectedAdminArticleId.value = null
  setAdminArticleForm({
    sortOrder: (adminArticles.value[adminArticles.value.length - 1]?.sortOrder || 0) + 10,
  })
  syncAdminArticleBaseline()
  adminPanelOpen.value = true
}

const saveAdminConfig = async () => {
  const validation = adminConfigValidation.value
  if (validation.errors.length) {
    ElMessage.warning(validation.errors[0])
    return
  }

  adminSavingConfig.value = true
  try {
    adminConfigForm.value.defaultArticleSlug = validation.normalized.defaultArticleSlug
    await api.updateKnowledgeAdminConfig(buildAdminConfigPayload(validation))
    await Promise.all([
      loadAdminDashboard(),
      loadKnowledgeBase(routeKeyword.value, routeArticleSlug.value),
    ])
    ElMessage.success('知识库运营配置已更新。')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '运营配置保存失败。')
  } finally {
    adminSavingConfig.value = false
  }
}

const saveAdminArticle = async () => {
  const validation = adminArticleValidation.value
  if (validation.errors.length) {
    ElMessage.warning(validation.errors[0])
    return
  }

  adminArticleForm.value.slug = validation.normalized.slug
  adminArticleForm.value.sectionId = validation.normalized.sectionId || normalizeKnowledgeSlug(adminArticleForm.value.sectionId)
  adminArticleForm.value.relatedSlugsText = validation.normalized.relatedSlugs.join(', ')

  const payload = buildAdminArticlePayload(validation)
  if (!payload.slug || !payload.title) {
    ElMessage.warning('slug 和标题不能为空。')
    return
  }

  adminSavingArticle.value = true
  const isEditing = Boolean(selectedAdminArticleId.value)
  try {
    const response = selectedAdminArticleId.value
      ? await api.updateKnowledgeAdminArticle(selectedAdminArticleId.value, payload)
      : await api.createKnowledgeAdminArticle(payload)

    const detail = response?.data?.data
    adminArticleDetailCache.set(detail.id, detail)
    selectedAdminArticleId.value = detail.id
    setAdminArticleForm(detail)
    syncAdminArticleBaseline()
    await loadAdminDashboard()

    if (detail.published) {
      await replaceQuery(routeKeyword.value, detail.slug)
    } else if (routeArticleSlug.value === detail.slug) {
      await replaceQuery(routeKeyword.value, '')
    } else {
      await loadKnowledgeBase(routeKeyword.value, routeArticleSlug.value)
    }

    ElMessage.success(isEditing ? '文章已保存。' : '文章已创建。')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '文章保存失败。')
  } finally {
    adminSavingArticle.value = false
  }
}

const deleteAdminArticle = async () => {
  if (!selectedAdminArticleId.value) return
  if (!window.confirm('确定删除这篇知识库文章吗？删除后无法恢复。')) return

  adminDeletingArticle.value = true
  try {
    const deletingSlug = adminArticleForm.value.slug
    adminArticleDetailCache.delete(selectedAdminArticleId.value)
    await api.deleteKnowledgeAdminArticle(selectedAdminArticleId.value)
    selectedAdminArticleId.value = null
    setAdminArticleForm()
    syncAdminArticleBaseline()
    await loadAdminDashboard()

    if (routeArticleSlug.value === deletingSlug) {
      await replaceQuery(routeKeyword.value, '')
    } else {
      await loadKnowledgeBase(routeKeyword.value, routeArticleSlug.value)
    }

    ElMessage.success('文章已删除。')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '文章删除失败。')
  } finally {
    adminDeletingArticle.value = false
  }
}

const addStep = () => {
  adminArticleForm.value.strategySteps.push(createEmptyStep())
}

const removeStep = (index) => {
  if (adminArticleForm.value.strategySteps.length === 1) {
    adminArticleForm.value.strategySteps[0] = createEmptyStep()
    return
  }
  adminArticleForm.value.strategySteps.splice(index, 1)
}

const addInsight = () => {
  adminArticleForm.value.insights.push(createEmptyInsight())
}

const removeInsight = (index) => {
  if (adminArticleForm.value.insights.length === 1) {
    adminArticleForm.value.insights[0] = createEmptyInsight()
    return
  }
  adminArticleForm.value.insights.splice(index, 1)
}

const addCodeBlock = () => {
  adminArticleForm.value.codeBlocks.push(createEmptyCodeBlock())
}

const removeCodeBlock = (index) => {
  if (adminArticleForm.value.codeBlocks.length === 1) {
    adminArticleForm.value.codeBlocks[0] = createEmptyCodeBlock()
    return
  }
  adminArticleForm.value.codeBlocks.splice(index, 1)
}

watch(
  [routeKeyword, routeArticleSlug],
  ([keyword, slug]) => {
    searchDraft.value = keyword
    void loadKnowledgeBase(keyword, slug)
  },
  { immediate: true },
)

watch(
  visibleArticleCount,
  (count) => {
    if (!count) {
      sidebarPage.value = 1
      return
    }

    setSidebarPage(sidebarPage.value)
  },
  { immediate: true },
)

watch(
  [currentSlug, sidebarItems],
  ([slug]) => {
    if (!slug) return

    const targetPage = getSidebarPageForSlug(slug)
    if (targetPage !== sidebarPage.value) {
      sidebarPage.value = targetPage
    }
  },
  { immediate: true },
)

watch(
  isAdmin,
  (value) => {
    if (!value) {
      adminPanelOpen.value = false
      selectedAdminArticleId.value = null
      adminDashboard.value = { config: createEmptyAdminConfig(), articles: [] }
      adminConfigForm.value = createEmptyAdminConfig()
      adminErrorMessage.value = ''
      adminListKeyword.value = ''
      adminStatusFilter.value = 'all'
      setAdminArticleForm()
      syncAdminConfigBaseline()
      syncAdminArticleBaseline()
      adminArticleDetailCache.clear()
    }
  },
  { immediate: true },
)

watch(
  currentSlug,
  (slug) => {
    if (!slug) {
      return
    }

    const nextHistory = [slug, ...recentReadSlugs.value.filter((item) => item !== slug)].slice(0, 8)
    recentReadSlugs.value = nextHistory
    writeKnowledgeMapHistory(nextHistory)
  },
  { immediate: true },
)
</script>

<template>
  <div class="knowledge-base-page">
    <section class="kb-hero kb-panel">
      <div class="hero-copy">
        <span class="hero-pill">ALGO DOCS</span>
        <h1>{{ catalog.title }}</h1>
        <p class="hero-subtitle">{{ catalog.subtitle }}</p>

        <form class="hero-search" @submit.prevent="handleSearchSubmit">
          <div class="hero-search-input">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20L17 17" />
            </svg>
            <input
              v-model="searchDraft"
              type="text"
              placeholder="搜索知识点、算法名、数据结构或面试场景"
            />
          </div>
          <button v-if="routeKeyword" type="button" class="hero-clear-btn" @click="clearSearch">清空</button>
          <button type="submit" class="hero-submit-btn">搜索主题</button>
          <div v-if="isAdmin" class="hero-admin-entry">
            <button
              type="button"
              class="hero-admin-btn"
              @click="goToKnowledgeBaseAdmin"
            >
              进入知识库管理台
            </button>
            <div class="hero-admin-meta">
              <span class="admin-chip accent">独立路由</span>
            </div>
          </div>
        </form>

        </div>
    </section>

    <div class="kb-shell">
      <aside class="kb-sidebar kb-panel">
        <div class="sidebar-head">
          <div>
            <span class="sidebar-eyebrow">目录导航</span>
            <h2>知识主题</h2>
          </div>
          <span class="sidebar-count">{{ visibleArticleCount }} 篇</span>
        </div>

        <div v-if="hasSidebarPagination && sections.length" class="sidebar-pagination-summary">
          <div class="sidebar-pagination-copy">
            <strong>当前展示第 {{ sidebarVisibleRange.start }} - {{ sidebarVisibleRange.end }} 条</strong>
            <p>
              共 {{ visibleArticleCount }} 条内容
              <template v-if="sidebarRemainingCount > 0">，还有 {{ sidebarRemainingCount }} 条可继续浏览</template>
            </p>
          </div>
          <span class="sidebar-page-progress">第 {{ sidebarPage }} / {{ totalSidebarPages }} 页</span>
        </div>

        <div v-if="loadingCatalog && !sections.length" class="sidebar-state">正在整理知识目录...</div>
        <div v-else-if="!sections.length" class="sidebar-state">{{ catalog.emptyStateTitle }}</div>

        <div v-else class="sidebar-sections">
          <section v-for="section in pagedSidebarSections" :key="section.id" class="sidebar-section">
            <header class="sidebar-section-head">
              <div class="sidebar-section-title-row">
                <h3>{{ section.title }}</h3>
                <span class="sidebar-section-count">{{ section.articles.length }} 条</span>
              </div>
              <p>{{ section.description }}</p>
            </header>

            <button
              v-for="item in section.articles"
              :key="item.slug"
              type="button"
              class="nav-item"
              :class="{ active: item.slug === currentSlug }"
              @click="selectArticle(item.slug)"
            >
              <div class="nav-item-main">
                <span class="nav-badge">{{ item.badge }}</span>
                <strong>{{ item.title }}</strong>
                <p>{{ item.subtitle }}</p>
              </div>
              <div class="nav-item-footer">
                <div class="nav-tags">
                  <span v-for="tag in item.tags" :key="`${item.slug}-${tag}`">{{ tag }}</span>
                </div>
                <span class="nav-readtime">{{ item.readTime }}</span>
              </div>
            </button>
          </section>
        </div>

        <div v-if="hasSidebarPagination && sections.length" class="sidebar-pagination">
          <FlowerPagination
            :total="totalSidebarPages"
            :defaultPage="sidebarPage"
            @change="goToSidebarPage"
          />
        </div>
      </aside>

      <main class="kb-main">
        <div v-if="errorMessage" class="state-card kb-panel">
          <h3>加载失败</h3>
          <p>{{ errorMessage }}</p>
        </div>

        <div v-else-if="loadingArticle && !currentArticle" class="state-card kb-panel">
          <h3>正在加载知识内容...</h3>
          <p>页面正在拉取当前主题的详细内容，请稍等一下。</p>
        </div>

        <div v-else-if="!visibleArticleCount" class="state-card kb-panel empty">
          <h3>{{ catalog.emptyStateTitle }}</h3>
          <p>{{ catalog.emptyStateDescription }}</p>
        </div>

        <article v-else-if="currentArticle" class="article-card kb-panel">
          <header class="article-hero">
            <div class="article-hero-copy">
              <span class="article-breadcrumb">
                {{ currentArticle.sectionTitle }}
                <template v-if="activeNavItem?.badge"> · {{ activeNavItem.badge }}</template>
              </span>
              <h2>{{ currentArticle.title }}</h2>
              <p class="article-en">{{ currentArticle.englishTitle }}</p>
            </div>

            <div class="article-meta-pills">
              <span class="meta-pill">{{ currentArticle.readTime }}</span>
              <span class="meta-pill accent">{{ currentArticle.complexity }}</span>
            </div>
          </header>

          <p class="article-summary">{{ currentArticle.summary }}</p>
          <p class="article-lead">{{ currentArticle.lead }}</p>

          <div class="article-tags">
            <span v-for="tag in currentArticle.tags" :key="`${currentArticle.slug}-${tag}`">{{ tag }}</span>
          </div>

          <section class="article-section">
            <div class="section-title-row">
              <span class="section-kicker">Learning Objectives</span>
              <h3>这篇要带走什么</h3>
            </div>
            <div class="objective-grid">
              <article
                v-for="(objective, index) in articleObjectives"
                :key="`${currentArticle.slug}-objective-${index}`"
                class="objective-card"
              >
                <span>{{ `0${index + 1}`.slice(-2) }}</span>
                <p>{{ objective }}</p>
              </article>
            </div>
          </section>

          <section class="article-section">
            <div class="section-title-row">
              <span class="section-kicker">Method</span>
              <h3>理解路径</h3>
            </div>
            <div class="step-list">
              <article
                v-for="step in currentArticle.strategySteps"
                :key="`${currentArticle.slug}-${step.index}`"
                class="step-card"
              >
                <div class="step-index">{{ step.index }}</div>
                <div class="step-content">
                  <div class="step-heading">
                    <h4>{{ step.title }}</h4>
                    <span>{{ step.badge }}</span>
                  </div>
                  <p>{{ step.description }}</p>
                </div>
              </article>
            </div>
          </section>

          <section class="article-section">
            <div class="section-title-row">
              <span class="section-kicker">Interview Notes</span>
              <h3>追问与提醒</h3>
            </div>
            <div class="insight-grid">
              <article
                v-for="insight in currentArticle.insights"
                :key="`${currentArticle.slug}-${insight.title}`"
                class="insight-card"
                :class="`is-${insight.accent}`"
              >
                <h4>{{ insight.title }}</h4>
                <p>{{ insight.description }}</p>
              </article>
            </div>
          </section>

          <section class="article-section">
            <div class="section-title-row">
              <span class="section-kicker">Code Templates</span>
              <h3>可直接复习的代码片段</h3>
            </div>
            <div class="code-stack">
              <CodeBlock
                v-for="block in currentArticle.codeBlocks"
                :key="`${currentArticle.slug}-${block.title}`"
                :language="block.language"
                :title="block.title"
                :code="block.code"
                :callouts="block.callouts"
                :max-lines="15"
                show-line-numbers
              />
            </div>
          </section>

          <section v-if="articleChecklist.length" class="article-section">
            <div class="section-title-row">
              <span class="section-kicker">Checklist</span>
              <h3>读完后自查</h3>
            </div>
            <div class="checklist-card kb-panel">
              <ul class="checklist">
                <li v-for="(item, index) in articleChecklist" :key="`${currentSlug}-check-${index}`">
                  {{ item }}
                </li>
              </ul>
            </div>
          </section>

          <button
            class="graph-trigger-bar"
            :class="{ 'is-active': graphOverlayOpen }"
            type="button"
            @click="graphOverlayOpen = !graphOverlayOpen"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
              <circle cx="6" cy="6" r="2.5"/>
              <circle cx="18" cy="6" r="2.5"/>
              <circle cx="12" cy="18" r="2.5"/>
              <line x1="7.8" y1="7.5" x2="10.5" y2="16"/>
              <line x1="16.2" y1="7.5" x2="13.5" y2="16"/>
              <line x1="8.5" y1="6" x2="15.5" y2="6"/>
            </svg>
          </button>

          <Transition name="graph-overlay">
            <div v-if="graphOverlayOpen" class="graph-overlay">
              <button class="graph-overlay-close" type="button" @click="graphOverlayOpen = false">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="6" y1="6" x2="18" y2="18"/>
                  <line x1="18" y1="6" x2="6" y2="18"/>
                </svg>
              </button>
              <AlgorithmKnowledgeGraph />
            </div>
          </Transition>
        </article>

      </main>

      <aside class="kb-aside">
        <div v-if="articleRelated.length && showRelatedPanel" class="aside-card kb-panel">
          <span class="sidebar-eyebrow">延伸阅读</span>
          <h3>继续往下看</h3>
          <button
            v-for="related in articleRelated"
            :key="related.slug"
            type="button"
            class="related-item"
            @click="openRelatedArticle(related.slug)"
          >
            <div>
              <strong>{{ related.title }}</strong>
              <p>{{ related.sectionTitle }}</p>
            </div>
            <span>打开</span>
          </button>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.knowledge-base-page {
  --kb-bg: linear-gradient(180deg, rgba(242, 250, 248, 0.95) 0%, rgba(249, 252, 255, 0.98) 100%);
  --kb-panel-bg: rgba(255, 255, 255, 0.82);
  --kb-border: rgba(154, 178, 196, 0.26);
  --kb-shadow: 0 24px 60px rgba(16, 42, 67, 0.08);
  --kb-text: #123047;
  --kb-muted: #5f7286;
  --kb-emerald: #1ea97c;
  --kb-cyan: #1f94a8;
  --kb-amber: #d48c24;
  max-width: 1480px;
  margin: 0 auto;
  padding: 32px 24px 72px;
  color: var(--kb-text);
  position: relative;
}

.knowledge-base-page::before,
.knowledge-base-page::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
}

.knowledge-base-page::before {
  width: 280px;
  height: 280px;
  top: 32px;
  right: 18px;
  background: rgba(59, 203, 164, 0.18);
}

.knowledge-base-page::after {
  width: 220px;
  height: 220px;
  left: 8px;
  top: 220px;
  background: rgba(71, 173, 219, 0.12);
}

.knowledge-base-page > * {
  position: relative;
  z-index: 1;
}

.kb-panel {
  background: var(--kb-panel-bg);
  border: 1px solid var(--kb-border);
  box-shadow: var(--kb-shadow);
  backdrop-filter: blur(18px) saturate(140%);
}

.kb-hero {
  background: var(--kb-bg);
  border-radius: 30px;
  padding: 30px;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.9fr);
  gap: 24px;
  overflow: hidden;
}

.hero-copy h1 {
  margin: 12px 0 10px;
  font-size: 40px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.hero-subtitle {
  max-width: 720px;
  margin: 0;
  font-size: 16px;
  line-height: 1.8;
  color: var(--kb-muted);
}

.hero-pill,
.sidebar-eyebrow,
.section-kicker,
.spotlight-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #000000;
}

.hero-search {
  margin-top: 22px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-search-input {
  flex: 1;
  min-width: 280px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  height: 56px;
  border-radius: 18px;
  border: 1px solid rgba(127, 150, 167, 0.26);
  background: rgba(255, 255, 255, 0.88);
  color: var(--kb-muted);
}

.hero-search-input input,
.admin-field input,
.admin-field textarea,
.admin-field select {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--kb-text);
  font-size: 15px;
  outline: none;
}

.hero-submit-btn,
.hero-clear-btn,
.hero-chip,
.mini-chip,
.spotlight-card,
.nav-item,
.related-item,
.hero-admin-btn,
.admin-article-row,
.mini-action {
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.hero-submit-btn,
.hero-clear-btn,
.hero-admin-btn {
  height: 56px;
  border-radius: 18px;
  border: none;
  padding: 0 22px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.hero-submit-btn,
.hero-admin-btn {
  background: linear-gradient(135deg, #1ea97c, #2ac09b);
  color: #fff;
  box-shadow: 0 14px 30px rgba(30, 169, 124, 0.24);
}

.hero-submit-btn.compact,
.hero-clear-btn.compact {
  height: 42px;
  padding: 0 16px;
  border-radius: 14px;
}

.hero-clear-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(136, 158, 174, 0.24);
  color: var(--kb-text);
}

.hero-clear-btn.danger,
.mini-action.danger {
  color: #b84444;
  border-color: rgba(184, 68, 68, 0.2);
}

.hero-submit-btn:hover,
.hero-clear-btn:hover,
.hero-chip:hover,
.mini-chip:hover,
.spotlight-card:hover,
.nav-item:hover,
.related-item:hover,
.hero-admin-btn:hover,
.admin-article-row:hover,
.mini-action:hover {
  transform: translateY(-2px);
}

.hero-chip-group,
.mini-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.hero-admin-entry {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-admin-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hero-admin-feedback {
  margin: 14px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--kb-muted);
}

.hero-admin-feedback.error {
  color: #b84444;
}

.knowledge-map-card {
  margin-top: 22px;
  padding: 26px 28px 28px;
  border-radius: 28px;
  border: 1px solid rgba(28, 42, 66, 0.08);
  width: 1025px;
  background:
    linear-gradient(rgba(107, 127, 152, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(107, 127, 152, 0.08) 1px, transparent 1px),
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 253, 0.92));
  background-size: 28px 28px, 28px 28px, 100% 100%;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 20px 46px rgba(17, 38, 58, 0.08);
  overflow: hidden;
}

.knowledge-map-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 26px;
}

.knowledge-map-copy h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.05;
  letter-spacing: -0.04em;
  color: #161d29;
}

.knowledge-map-copy p {
  margin: 14px 0 0;
  font-size: 15px;
  line-height: 1.8;
  color: #758295;
}

.knowledge-map-sort {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  min-width: 142px;
  padding: 14px 18px;
  border-radius: 16px;
  border: 1px solid rgba(28, 42, 66, 0.12);
  background: rgba(255, 255, 255, 0.92);
  color: #1d2635;
  font-size: 14px;
  font-weight: 700;
  box-shadow: 0 12px 24px rgba(18, 48, 71, 0.06);
}

.knowledge-map-sort select {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}

.knowledge-map-sort svg {
  color: #1d2635;
}

.knowledge-map-board {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 18px 26px;
  align-items: stretch;
}

.knowledge-map-stage {
  position: relative;
  display: grid;
  gap: 14px;
  min-width: 0;
  --map-stage-accent: #4d82ff;
  --map-stage-accent-soft: rgba(77, 130, 255, 0.12);
  --map-stage-track: rgba(77, 130, 255, 0.28);
}

.knowledge-map-stage::after {
  content: '';
  position: absolute;
  top: 42px;
  right: -26px;
  width: 26px;
  border-top: 1px dashed var(--map-stage-track);
}

.knowledge-map-stage::before {
  content: '';
  position: absolute;
  top: 38px;
  right: -4px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--map-stage-accent);
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.9);
}

.knowledge-map-stage:last-child::before,
.knowledge-map-stage:last-child::after {
  display: none;
}

.knowledge-map-stage.is-sunrise {
  --map-stage-accent: #ff8d6d;
  --map-stage-accent-soft: rgba(255, 141, 109, 0.14);
  --map-stage-track: rgba(255, 141, 109, 0.34);
}

.knowledge-map-stage.is-azure {
  --map-stage-accent: #4d82ff;
  --map-stage-accent-soft: rgba(77, 130, 255, 0.14);
  --map-stage-track: rgba(77, 130, 255, 0.32);
}

.knowledge-map-stage.is-violet {
  --map-stage-accent: #6653e7;
  --map-stage-accent-soft: rgba(102, 83, 231, 0.14);
  --map-stage-track: rgba(102, 83, 231, 0.3);
}

.knowledge-map-stage.is-mint {
  --map-stage-accent: #2db57f;
  --map-stage-accent-soft: rgba(45, 181, 127, 0.14);
  --map-stage-track: rgba(45, 181, 127, 0.3);
}

.knowledge-map-stage.is-amber {
  --map-stage-accent: #f0a227;
  --map-stage-accent-soft: rgba(240, 162, 39, 0.16);
  --map-stage-track: rgba(240, 162, 39, 0.3);
}

.knowledge-map-stage.is-ocean {
  --map-stage-accent: #2faabd;
  --map-stage-accent-soft: rgba(47, 170, 189, 0.14);
  --map-stage-track: rgba(47, 170, 189, 0.3);
}

.knowledge-map-stage-head {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 56px;
}

.knowledge-map-stage-step {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: var(--map-stage-accent-soft);
  color: var(--map-stage-accent);
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
}

.knowledge-map-stage-copy {
  min-width: 0;
}

.knowledge-map-stage-copy h3 {
  margin: 0;
  font-size: 15px;
  line-height: 1.2;
  color: #16202d;
}

.knowledge-map-stage-copy p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.55;
  color: #7c8797;
}

.knowledge-map-column {
  position: relative;
  display: grid;
  gap: 10px;
  align-content: start;
  padding-left: 2px;
}

.knowledge-map-column::before {
  content: '';
  position: absolute;
  top: 4px;
  bottom: 4px;
  left: 12px;
  border-left: 1px dashed var(--map-stage-track);
}

.knowledge-map-node {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  width: 100%;
  padding: 9px 12px;
  border-radius: 12px;
  border: 1px solid rgba(28, 42, 66, 0.22);
  background: rgba(255, 255, 255, 0.94);
  color: #5f6b7b;
  box-shadow: 0 8px 18px rgba(18, 48, 71, 0.05);
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.knowledge-map-node-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--map-stage-accent);
  box-shadow: 0 0 0 6px rgba(255, 255, 255, 0.92), 0 4px 10px var(--map-stage-accent-soft);
}

.knowledge-map-node-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.4;
}

.knowledge-map-node:hover {
  transform: translateY(-2px);
  border-color: var(--map-stage-track);
  box-shadow: 0 14px 24px rgba(18, 48, 71, 0.08);
  color: #2b3443;
}

.knowledge-map-node.active {
  border-color: var(--map-stage-track);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), var(--map-stage-accent-soft));
  color: #183041;
  box-shadow: 0 16px 28px rgba(18, 48, 71, 0.08);
}

.knowledge-map-empty {
  padding: 18px 20px;
  border-radius: 18px;
  border: 1px dashed rgba(120, 140, 160, 0.24);
  background: rgba(255, 255, 255, 0.72);
  color: var(--kb-muted);
  font-size: 14px;
}

.hero-chip,
.mini-chip,
.mini-action {
  border: 1px solid rgba(31, 148, 168, 0.16);
  border-radius: 999px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.72);
  color: #1b5164;
  font-size: 13px;
  cursor: pointer;
}

.hero-spotlights {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  align-content: start;
}

.spotlight-card {
  text-align: left;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  padding: 20px;
  cursor: pointer;
  color: #fff;
}

.spotlight-card h3 {
  margin: 10px 0 8px;
  font-size: 22px;
}

.spotlight-card p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  opacity: 0.9;
}

.spotlight-card.is-emerald {
  background: linear-gradient(135deg, rgba(25, 140, 112, 0.92), rgba(27, 184, 146, 0.92));
}

.spotlight-card.is-cyan {
  background: linear-gradient(135deg, rgba(20, 104, 134, 0.92), rgba(35, 170, 191, 0.92));
}

.spotlight-card.is-amber {
  background: linear-gradient(135deg, rgba(160, 108, 25, 0.92), rgba(221, 156, 42, 0.92));
}

.kb-metrics {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.metric-card {
  border-radius: 22px;
  padding: 22px;
}

.metric-card strong {
  display: block;
  margin-top: 10px;
  font-size: 30px;
  line-height: 1;
}

.metric-card p,
.metric-label,
.sidebar-head p,
.sidebar-section-head p,
.nav-item-main p,
.article-en,
.article-lead,
.step-content p,
.insight-card p,
.aside-card p,
.related-item p,
.state-card p,
.admin-panel-head p,
.admin-article-row p {
  color: var(--kb-muted);
}

.metric-label {
  font-size: 13px;
  font-weight: 700;
}

.metric-card p {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.7;
}

.kb-shell {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 300px;
  gap: 20px;
  align-items: start;
}

.kb-sidebar,
.kb-aside {
  position: sticky;
  top: 96px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.kb-sidebar::-webkit-scrollbar,
.kb-aside::-webkit-scrollbar {
  display: none;
}

.kb-sidebar,
.kb-aside,
.article-card,
.state-card,
.admin-panel {
  border-radius: 26px;
}

.kb-sidebar,
.kb-aside,
.article-card,
.admin-panel {
  padding: 24px;
}

.article-card {
  width: 1100px;
}

.sidebar-head,
.admin-card-head,
.admin-panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.sidebar-head {
  margin-bottom: 20px;
}

.sidebar-head h2,
.admin-panel-head h3 {
  margin: 8px 0 0;
  font-size: 24px;
}

.sidebar-count,
.admin-meta {
  min-width: 74px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(30, 169, 124, 0.1);
  color: var(--kb-emerald);
  text-align: center;
  font-size: 13px;
  font-weight: 700;
}

.sidebar-state {
  padding: 18px;
  border-radius: 18px;
  background: rgba(246, 250, 253, 0.9);
  color: var(--kb-muted);
  text-align: center;
  font-size: 14px;
}

.admin-status-banner {
  margin: 18px 0 0;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(31, 148, 168, 0.14);
  background: rgba(244, 250, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-status-banner strong {
  display: block;
  font-size: 14px;
}

.admin-status-banner p {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--kb-muted);
}

.admin-status-banner.error {
  border-color: rgba(184, 68, 68, 0.18);
  background: rgba(255, 245, 245, 0.92);
}

.sidebar-sections {
  display: grid;
  gap: 22px;
}

.sidebar-pagination-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border: 1px solid rgba(132, 156, 176, 0.16);
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(30, 169, 124, 0.08), rgba(31, 148, 168, 0.03)),
    rgba(255, 255, 255, 0.74);
  width: 240px;
}

.sidebar-pagination-copy strong {
  display: block;
  font-size: 14px;
  color: var(--kb-text);
  width: 120px;
}

.sidebar-pagination-copy p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--kb-muted);
}

.sidebar-page-progress,
.sidebar-section-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 6px 10px;
  background: rgba(18, 48, 71, 0.06);
  color: var(--kb-muted);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.sidebar-section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.sidebar-section-head h3 {
  margin: 0;
  font-size: 18px;
}

.sidebar-section-head p {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.7;
}

.sidebar-section {
  display: grid;
  gap: 14px;
}

.sidebar-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid rgba(132, 156, 176, 0.14);
}

.sidebar-page-list {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.sidebar-page-btn {
  border: 1px solid rgba(132, 156, 176, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--kb-muted);
  min-width: 40px;
  height: 40px;
  padding: 0 14px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, color 0.2s ease, background 0.2s ease;
}

.sidebar-page-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: rgba(30, 169, 124, 0.28);
  color: var(--kb-text);
  box-shadow: 0 10px 24px rgba(18, 48, 71, 0.08);
}

.sidebar-page-btn.active {
  border-color: rgba(30, 169, 124, 0.4);
  background: linear-gradient(135deg, rgba(30, 169, 124, 0.16), rgba(31, 148, 168, 0.08));
  color: var(--kb-emerald);
  box-shadow: 0 12px 24px rgba(30, 169, 124, 0.12);
}

.sidebar-page-btn.nav {
  min-width: 82px;
}

.sidebar-page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
  box-shadow: none;
}

.sidebar-page-ellipsis {
  color: var(--kb-muted);
  font-size: 18px;
  line-height: 1;
  padding: 0 4px;
}

.nav-item,
.admin-article-row {
  border: 1px solid rgba(132, 156, 176, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  padding: 16px;
  cursor: pointer;
  text-align: left;
}

.nav-item.active,
.admin-article-row.active {
  border-color: rgba(30, 169, 124, 0.35);
  background: linear-gradient(145deg, rgba(30, 169, 124, 0.12), rgba(255, 255, 255, 0.92));
  box-shadow: 0 16px 32px rgba(30, 169, 124, 0.14);
}

.nav-item-main strong,
.admin-article-row strong {
  display: block;
  margin-top: 10px;
  font-size: 16px;
}

.nav-item-main p,
.admin-article-row p {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.7;
}

.nav-badge,
.nav-readtime,
.meta-pill,
.code-lang,
.admin-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.nav-badge,
.admin-chip.accent {
  background: rgba(31, 148, 168, 0.1);
  color: var(--kb-cyan);
}

.admin-chip.warning {
  background: rgba(212, 140, 36, 0.12);
  color: var(--kb-amber);
}

.nav-item-footer,
.admin-row-meta {
  margin-top: 14px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.nav-tags,
.article-tags,
.code-callouts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.nav-tags span,
.article-tags span,
.code-callouts span {
  border-radius: 999px;
  background: rgba(18, 48, 71, 0.06);
  color: var(--kb-muted);
  padding: 6px 10px;
  font-size: 12px;
}

.nav-readtime,
.admin-chip.success {
  background: rgba(212, 140, 36, 0.1);
  color: var(--kb-amber);
}

.admin-chip.muted {
  background: rgba(18, 48, 71, 0.08);
  color: var(--kb-muted);
}

.state-card {
  padding: 32px;
}

.state-card h3 {
  margin: 0 0 10px;
  font-size: 26px;
}

.state-card p {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
}

.article-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.article-breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #2c846d;
}

.article-hero-copy h2 {
  margin: 12px 0 10px;
  font-size: 36px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.article-en {
  margin: 0;
  font-size: 14px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.article-meta-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.meta-pill {
  background: rgba(18, 48, 71, 0.06);
  color: var(--kb-text);
}

.meta-pill.accent {
  background: rgba(30, 169, 124, 0.12);
  color: var(--kb-emerald);
}

.article-summary {
  margin: 22px 0 12px;
  font-size: 19px;
  line-height: 1.7;
  color: var(--kb-text);
}

.article-lead {
  margin: 0;
  font-size: 15px;
  line-height: 1.9;
}

.article-tags {
  margin-top: 18px;
}

.article-section,
.admin-subsection {
  margin-top: 34px;
}

.section-title-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
}

.section-title-row h3 {
  margin: 0;
  font-size: 28px;
  letter-spacing: -0.03em;
}

.objective-grid,
.insight-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.objective-card,
.insight-card,
.code-card,
.aside-card,
.step-card,
.admin-config-card,
.admin-list-card,
.admin-editor-card,
.admin-repeat-card {
  border: 1px solid rgba(135, 158, 175, 0.18);
  background: rgba(252, 254, 255, 0.84);
  border-radius: 22px;
}

.objective-card,
.insight-card,
.aside-card,
.admin-config-card,
.admin-list-card,
.admin-editor-card,
.admin-repeat-card {
  padding: 18px;
}

.objective-card span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: rgba(30, 169, 124, 0.12);
  color: var(--kb-emerald);
  font-weight: 700;
}

.objective-card p {
  margin: 14px 0 0;
  font-size: 14px;
  line-height: 1.8;
}

.step-list,
.code-stack {
  display: grid;
  gap: 16px;
}

.step-card {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 16px;
  padding: 18px;
}

.step-index {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(30, 169, 124, 0.9), rgba(35, 170, 191, 0.9));
  color: #fff;
  font-size: 22px;
  font-weight: 800;
}

.step-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.step-heading h4,
.insight-card h4,
.code-card h4,
.aside-card h3,
.admin-card-head h4,
.admin-subsection-head h5 {
  margin: 0;
}

.step-heading span {
  border-radius: 999px;
  background: rgba(18, 48, 71, 0.08);
  color: var(--kb-muted);
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 700;
}

.step-content p {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.8;
}

.insight-card h4 {
  font-size: 18px;
}

.insight-card p {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.8;
}

.insight-card.is-emerald {
  background: linear-gradient(180deg, rgba(30, 169, 124, 0.14), rgba(255, 255, 255, 0.92));
}

.insight-card.is-amber {
  background: linear-gradient(180deg, rgba(212, 140, 36, 0.14), rgba(255, 255, 255, 0.92));
}

.insight-card.is-cyan {
  background: linear-gradient(180deg, rgba(31, 148, 168, 0.14), rgba(255, 255, 255, 0.92));
}

/* CodeBlock 组件样式已移至组件内部 */

.aside-card + .aside-card {
  margin-top: 16px;
}

.aside-card h3 {
  margin-top: 10px;
  font-size: 22px;
}

.aside-kv {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(135, 158, 175, 0.16);
}

.aside-kv span {
  color: var(--kb-muted);
  font-size: 14px;
}

.aside-kv strong {
  text-align: right;
  font-size: 14px;
}

.checklist-card {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(135, 158, 175, 0.18);
  border-radius: 18px;
  padding: 20px 24px;
}

.checklist-card .checklist {
  margin: 0;
}

.checklist {
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
}

.checklist li {
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr);
  gap: 10px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--kb-text);
}

.checklist li::before {
  content: '';
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1ea97c, #2ac09b);
  margin-top: 7px;
}

.related-item {
  width: 100%;
  border: 1px solid rgba(135, 158, 175, 0.18);
  background: rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  padding: 14px 16px;
  text-align: left;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  cursor: pointer;
}

.related-item + .related-item {
  margin-top: 12px;
}

.related-item strong {
  display: block;
  font-size: 15px;
}

.related-item p {
  margin: 6px 0 0;
  font-size: 13px;
}

.related-item span {
  color: var(--kb-emerald);
  font-size: 13px;
  font-weight: 700;
}

.admin-panel {
  margin-top: 24px;
}

.admin-panel-head p {
  margin: 10px 0 0;
  max-width: 640px;
  line-height: 1.7;
}

.admin-grid {
  margin-top: 22px;
  display: grid;
  grid-template-columns: 1.05fr 0.8fr;
  gap: 18px;
}

.admin-list-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(180px, 0.8fr);
  gap: 12px;
  margin-top: 16px;
}

.admin-editor-card {
  grid-column: 1 / -1;
}

.admin-form-grid {
  display: grid;
  gap: 14px;
}

.admin-form-grid.two-column {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.admin-form-grid.three-column {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.admin-form-grid.four-column {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.admin-field {
  display: grid;
  gap: 8px;
  margin-top: 14px;
}

.admin-field.compact {
  margin-top: 0;
}

.admin-field span {
  font-size: 13px;
  font-weight: 700;
  color: var(--kb-text);
}

.admin-field input,
.admin-field textarea,
.admin-field select {
  border: 1px solid rgba(135, 158, 175, 0.18);
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.9);
  resize: vertical;
}

.admin-validation-list {
  margin: 16px 0 0;
  padding: 14px 18px;
  border-radius: 18px;
  border: 1px solid rgba(212, 140, 36, 0.16);
  background: rgba(255, 250, 241, 0.92);
  color: #9b6420;
  display: grid;
  gap: 6px;
  font-size: 13px;
}

.admin-switch-row {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: var(--kb-text);
  font-size: 14px;
  font-weight: 600;
}

.admin-switch-row label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.admin-subsection-head,
.admin-card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.admin-repeat-card + .admin-repeat-card {
  margin-top: 14px;
}

@media (max-width: 1320px) {
  .kb-shell {
    grid-template-columns: 280px minmax(0, 1fr);
  }

  .kb-aside {
    position: static;
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  .aside-card + .aside-card {
    margin-top: 0;
  }
}

@media (max-width: 1100px) {
  .kb-hero,
  .kb-shell,
  .objective-grid,
  .insight-grid,
  .kb-metrics,
  .admin-grid,
  .admin-list-toolbar,
  .admin-form-grid.two-column,
  .admin-form-grid.three-column,
  .admin-form-grid.four-column {
    grid-template-columns: 1fr;
  }

  .kb-sidebar,
  .kb-aside {
    position: static;
  }

  .sidebar-pagination-summary,
  .sidebar-pagination {
    flex-direction: column;
    align-items: stretch;
  }

  .sidebar-page-progress {
    align-self: flex-start;
  }

  .sidebar-page-list {
    justify-content: flex-start;
  }

  .kb-aside {
    display: grid;
    grid-template-columns: 1fr;
  }

  .knowledge-map-board {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 20px;
  }

  .knowledge-map-stage::before,
  .knowledge-map-stage::after {
    display: none;
  }
}

@media (max-width: 768px) {
  .knowledge-base-page {
    padding: 24px 14px 56px;
  }

  .kb-hero,
  .kb-sidebar,
  .kb-aside,
  .article-card,
  .admin-panel {
    padding: 18px;
    border-radius: 22px;
  }

  .hero-copy h1,
  .article-hero-copy h2 {
    font-size: 30px;
  }

  .sidebar-section-title-row {
    align-items: flex-start;
  }

  .sidebar-page-btn.nav {
    width: 100%;
  }

  .hero-search {
    flex-direction: column;
    align-items: stretch;
  }

  .knowledge-map-card {
    padding: 20px 18px 22px;
    border-radius: 24px;
    background-size: 22px 22px, 22px 22px, 100% 100%;
  }

  .knowledge-map-heading {
    flex-direction: column;
    align-items: stretch;
    margin-bottom: 20px;
  }

  .knowledge-map-copy h2 {
    font-size: 24px;
  }

  .knowledge-map-sort {
    width: 100%;
    justify-content: space-between;
  }

  .knowledge-map-board {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px 14px;
  }

  .knowledge-map-stage {
    gap: 12px;
  }

  .knowledge-map-stage-head {
    min-height: auto;
  }

  .knowledge-map-stage-step {
    width: 34px;
    height: 34px;
    border-radius: 10px;
  }

  .knowledge-map-stage-copy h3 {
    font-size: 14px;
  }

  .knowledge-map-stage-copy p {
    font-size: 11px;
  }

  .knowledge-map-column {
    gap: 14px;
  }

  .knowledge-map-node {
    min-height: 38px;
    padding: 8px 12px;
  }

  .hero-admin-entry,
  .hero-admin-meta,
  .admin-status-banner {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-search-input {
    min-width: 0;
  }

  .article-hero,
  .admin-panel-head,
  .admin-card-head,
  .admin-card-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .article-meta-pills {
    justify-content: flex-start;
  }

  .step-card {
    grid-template-columns: 1fr;
  }

  .step-index {
    width: 56px;
    height: 56px;
    border-radius: 16px;
  }
}

@media (max-width: 560px) {
  .knowledge-map-board {
    grid-template-columns: 1fr;
  }
}

.article-card {
  position: relative;
  overflow: hidden;
}

.graph-trigger-bar {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 6px;
  height: 72px;
  border: none;
  border-radius: 6px 0 0 6px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: -2px 0 12px rgba(16, 42, 67, 0.08);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1),
             background 0.25s ease,
             box-shadow 0.25s ease;
  color: var(--kb-muted, #5f7286);
  outline: none;
}

.graph-trigger-bar:hover,
.graph-trigger-bar.is-active {
  width: 32px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  box-shadow: -4px 0 20px rgba(16, 42, 67, 0.12);
  color: var(--kb-emerald, #1ea97c);
}

.graph-trigger-bar svg {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.graph-trigger-bar:hover svg,
.graph-trigger-bar.is-active svg {
  opacity: 1;
}

.graph-overlay {
  position: absolute;
  inset: 0;
  z-index: 20;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  box-shadow: inset 0 0 0 1px rgba(154, 178, 196, 0.18);
  overflow: hidden;
}

.graph-overlay-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 30;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 2px 12px rgba(16, 42, 67, 0.1);
  cursor: pointer;
  color: var(--kb-muted, #5f7286);
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
  outline: none;
}

.graph-overlay-close:hover {
  background: rgba(255, 255, 255, 1);
  color: var(--kb-text, #123047);
  transform: scale(1.05);
}

.graph-overlay-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.graph-placeholder {
  margin: 0;
  font-size: 15px;
  color: var(--kb-muted, #5f7286);
  letter-spacing: 0.02em;
}

.graph-overlay-enter-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.graph-overlay-leave-active {
  transition: opacity 0.2s cubic-bezier(0.4, 0, 0.2, 1),
              transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.graph-overlay-enter-from {
  opacity: 0;
  transform: translateX(40px);
}

.graph-overlay-leave-to {
  opacity: 0;
  transform: translateX(40px);
}

@media (max-width: 768px) {
  .graph-trigger-bar:hover,
  .graph-trigger-bar.is-active {
    width: 28px;
  }
}
</style>
