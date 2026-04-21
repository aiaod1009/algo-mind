<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'
import FlowerPagination from '../components/FlowerPagination.vue'

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

const routeKeyword = computed(() => String(route.query.keyword || '').trim())
const routeArticleSlug = computed(() => String(route.query.article || '').trim())
const sections = computed(() => catalog.value.sections || [])
const allNavItems = computed(() => sections.value.flatMap((section) => section.articles || []))
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
const activeSection = computed(() =>
  sections.value.find((section) => section.id === currentArticle.value?.sectionId)
  || sections.value.find((section) => section.articles?.some((item) => item.slug === currentSlug.value))
  || null,
)
const adminArticles = computed(() => adminDashboard.value.articles || [])
const isAdmin = computed(() =>
  Boolean(userStore.userInfo?.isAdmin) || userStore.userInfo?.email === 'admin@example.com',
)

const toTextLines = (value) => value
  .split('\n')
  .map((item) => item.trim())
  .filter(Boolean)

const toCommaItems = (value) => value
  .split(/[，,]/)
  .map((item) => item.trim())
  .filter(Boolean)

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

const articleObjectives = computed(() => currentArticle.value?.learningObjectives || [])
const articleChecklist = computed(() => currentArticle.value?.checklist || [])
const articleRelated = computed(() => currentArticle.value?.relatedArticles || [])

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
  void replaceQuery('', slug)
}

const loadAdminDashboard = async () => {
  if (!isAdmin.value) return

  adminLoading.value = true
  try {
    const response = await api.getKnowledgeAdminDashboard()
    const dashboard = normalizeAdminDashboard(response?.data?.data)
    adminDashboard.value = dashboard
    adminConfigForm.value = { ...dashboard.config }

    const selectedStillExists = dashboard.articles.some((item) => item.id === selectedAdminArticleId.value)
    if (!selectedStillExists && selectedAdminArticleId.value != null) {
      selectedAdminArticleId.value = null
      setAdminArticleForm()
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '知识库管理台加载失败。')
  } finally {
    adminLoading.value = false
  }
}

const loadAdminArticle = async (id) => {
  if (!id) return

  adminLoading.value = true
  try {
    const response = await api.getKnowledgeAdminArticle(id)
    const detail = response?.data?.data
    selectedAdminArticleId.value = detail.id
    setAdminArticleForm(detail)
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
  adminPanelOpen.value = true
}

const buildAdminArticlePayload = () => ({
  slug: adminArticleForm.value.slug.trim(),
  title: adminArticleForm.value.title.trim(),
  englishTitle: adminArticleForm.value.englishTitle.trim(),
  sectionId: adminArticleForm.value.sectionId.trim(),
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
  relatedArticleSlugs: toCommaItems(adminArticleForm.value.relatedSlugsText),
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

const saveAdminConfig = async () => {
  adminSavingConfig.value = true
  try {
    await api.updateKnowledgeAdminConfig({
      siteTitle: adminConfigForm.value.siteTitle.trim(),
      siteSubtitle: adminConfigForm.value.siteSubtitle.trim(),
      emptyStateTitle: adminConfigForm.value.emptyStateTitle.trim(),
      emptyStateDescription: adminConfigForm.value.emptyStateDescription.trim(),
      defaultArticleSlug: adminConfigForm.value.defaultArticleSlug.trim(),
      quickSearches: toCommaItems(adminConfigForm.value.quickSearchesText),
    })
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
  const payload = buildAdminArticlePayload()
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
    selectedAdminArticleId.value = detail.id
    setAdminArticleForm(detail)
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
    await api.deleteKnowledgeAdminArticle(selectedAdminArticleId.value)
    selectedAdminArticleId.value = null
    setAdminArticleForm()
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
    if (value) {
      void loadAdminDashboard()
    } else {
      adminPanelOpen.value = false
      selectedAdminArticleId.value = null
      adminDashboard.value = { config: createEmptyAdminConfig(), articles: [] }
      adminConfigForm.value = createEmptyAdminConfig()
      setAdminArticleForm()
    }
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
          <button
            v-if="isAdmin"
            type="button"
            class="hero-admin-btn"
            @click="adminPanelOpen = !adminPanelOpen"
          >
            {{ adminPanelOpen ? '收起管理台' : '打开管理台' }}
          </button>
        </form>

        <div class="hero-chip-group">
          <button
            v-for="quick in catalog.quickSearches"
            :key="quick"
            type="button"
            class="hero-chip"
            @click="applyQuickSearch(quick)"
          >
            {{ quick }}
          </button>
        </div>
      </div>

      <div class="hero-spotlights">
        <button
          v-for="card in catalog.spotlightCards"
          :key="card.slug"
          type="button"
          class="spotlight-card"
          :class="`is-${card.accent}`"
          @click="openSpotlight(card.slug)"
        >
          <span class="spotlight-eyebrow">{{ card.eyebrow }}</span>
          <h3>{{ card.title }}</h3>
          <p>{{ card.description }}</p>
        </button>
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
              <article
                v-for="block in currentArticle.codeBlocks"
                :key="`${currentArticle.slug}-${block.title}`"
                class="code-card"
              >
                <header class="code-card-head">
                  <div>
                    <span class="code-lang">{{ block.language }}</span>
                    <h4>{{ block.title }}</h4>
                  </div>
                </header>
                <pre><code>{{ block.code }}</code></pre>
                <div v-if="block.callouts.length" class="code-callouts">
                  <span v-for="(callout, index) in block.callouts" :key="`${block.title}-${index}`">
                    {{ callout }}
                  </span>
                </div>
              </article>
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
        </article>

        <section v-if="isAdmin && adminPanelOpen" class="admin-panel kb-panel">
          <div class="admin-panel-head">
            <div>
              <span class="sidebar-eyebrow">Admin Console</span>
              <h3>知识库管理台</h3>
              <p>这里可以维护首页运营配置、发布文章、下线文章和调整推荐位。</p>
            </div>
            <button type="button" class="hero-submit-btn compact" @click="openNewAdminArticle">新增文章</button>
          </div>

          <div class="admin-grid">
            <section class="admin-config-card">
              <header class="admin-card-head">
                <h4>运营配置</h4>
                <button
                  type="button"
                  class="hero-submit-btn compact"
                  :disabled="adminSavingConfig"
                  @click="saveAdminConfig"
                >
                  {{ adminSavingConfig ? '保存中...' : '保存配置' }}
                </button>
              </header>

              <div class="admin-form-grid two-column">
                <label class="admin-field">
                  <span>站点标题</span>
                  <input v-model="adminConfigForm.siteTitle" type="text" />
                </label>
                <label class="admin-field">
                  <span>默认文章 slug</span>
                  <input v-model="adminConfigForm.defaultArticleSlug" type="text" />
                </label>
              </div>

              <label class="admin-field">
                <span>站点副标题</span>
                <textarea v-model="adminConfigForm.siteSubtitle" rows="3" />
              </label>

              <div class="admin-form-grid two-column">
                <label class="admin-field">
                  <span>空状态标题</span>
                  <input v-model="adminConfigForm.emptyStateTitle" type="text" />
                </label>
                <label class="admin-field">
                  <span>推荐搜索词</span>
                  <input v-model="adminConfigForm.quickSearchesText" type="text" placeholder="逗号分隔" />
                </label>
              </div>

              <label class="admin-field">
                <span>空状态描述</span>
                <textarea v-model="adminConfigForm.emptyStateDescription" rows="3" />
              </label>
            </section>

            <section class="admin-list-card">
              <header class="admin-card-head">
                <h4>文章列表</h4>
                <span class="admin-meta">{{ adminArticles.length }} 篇</span>
              </header>

              <div v-if="adminLoading && !adminArticles.length" class="sidebar-state">正在加载管理数据...</div>

              <button
                v-for="item in adminArticles"
                :key="item.id"
                type="button"
                class="admin-article-row"
                :class="{ active: item.id === selectedAdminArticleId }"
                @click="loadAdminArticle(item.id)"
              >
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.slug }} · {{ item.sectionTitle }}</p>
                </div>
                <div class="admin-row-meta">
                  <span class="admin-chip" :class="{ success: item.published, muted: !item.published }">
                    {{ item.published ? '已发布' : '草稿' }}
                  </span>
                  <span class="admin-chip accent" v-if="item.spotlightEnabled">推荐位</span>
                </div>
              </button>
            </section>

            <section class="admin-editor-card">
              <header class="admin-card-head">
                <h4>{{ selectedAdminArticleId ? '编辑文章' : '新建文章' }}</h4>
                <div class="admin-card-actions">
                  <button
                    v-if="selectedAdminArticleId"
                    type="button"
                    class="hero-clear-btn compact danger"
                    :disabled="adminDeletingArticle"
                    @click="deleteAdminArticle"
                  >
                    {{ adminDeletingArticle ? '删除中...' : '删除文章' }}
                  </button>
                  <button
                    type="button"
                    class="hero-submit-btn compact"
                    :disabled="adminSavingArticle"
                    @click="saveAdminArticle"
                  >
                    {{ adminSavingArticle ? '保存中...' : '保存文章' }}
                  </button>
                </div>
              </header>

              <div class="admin-form-grid three-column">
                <label class="admin-field">
                  <span>slug</span>
                  <input v-model="adminArticleForm.slug" type="text" />
                </label>
                <label class="admin-field">
                  <span>标题</span>
                  <input v-model="adminArticleForm.title" type="text" />
                </label>
                <label class="admin-field">
                  <span>英文标题</span>
                  <input v-model="adminArticleForm.englishTitle" type="text" />
                </label>
              </div>

              <div class="admin-form-grid four-column">
                <label class="admin-field">
                  <span>栏目 ID</span>
                  <input v-model="adminArticleForm.sectionId" type="text" />
                </label>
                <label class="admin-field">
                  <span>栏目名称</span>
                  <input v-model="adminArticleForm.sectionTitle" type="text" />
                </label>
                <label class="admin-field">
                  <span>徽标</span>
                  <input v-model="adminArticleForm.badge" type="text" />
                </label>
                <label class="admin-field">
                  <span>排序</span>
                  <input v-model="adminArticleForm.sortOrder" type="number" />
                </label>
              </div>

              <label class="admin-field">
                <span>栏目描述</span>
                <textarea v-model="adminArticleForm.sectionDescription" rows="2" />
              </label>

              <div class="admin-form-grid two-column">
                <label class="admin-field">
                  <span>复杂度提示</span>
                  <input v-model="adminArticleForm.complexity" type="text" />
                </label>
                <label class="admin-field">
                  <span>阅读时长</span>
                  <input v-model="adminArticleForm.readTime" type="text" />
                </label>
              </div>

              <label class="admin-field">
                <span>摘要</span>
                <textarea v-model="adminArticleForm.summary" rows="3" />
              </label>

              <label class="admin-field">
                <span>导语</span>
                <textarea v-model="adminArticleForm.lead" rows="3" />
              </label>

              <div class="admin-form-grid two-column">
                <label class="admin-field">
                  <span>标签</span>
                  <input v-model="adminArticleForm.tagsText" type="text" placeholder="逗号分隔" />
                </label>
                <label class="admin-field">
                  <span>相关文章 slug</span>
                  <input v-model="adminArticleForm.relatedSlugsText" type="text" placeholder="逗号分隔" />
                </label>
              </div>

              <div class="admin-form-grid two-column">
                <label class="admin-field">
                  <span>学习目标</span>
                  <textarea v-model="adminArticleForm.learningObjectivesText" rows="5" placeholder="每行一条" />
                </label>
                <label class="admin-field">
                  <span>复盘清单</span>
                  <textarea v-model="adminArticleForm.checklistText" rows="5" placeholder="每行一条" />
                </label>
              </div>

              <div class="admin-switch-row">
                <label><input v-model="adminArticleForm.published" type="checkbox" /> 发布到公开页</label>
                <label><input v-model="adminArticleForm.spotlightEnabled" type="checkbox" /> 加入首页推荐位</label>
              </div>

              <div class="admin-form-grid four-column">
                <label class="admin-field">
                  <span>推荐位眉标题</span>
                  <input v-model="adminArticleForm.spotlightEyebrow" type="text" />
                </label>
                <label class="admin-field">
                  <span>推荐位标题</span>
                  <input v-model="adminArticleForm.spotlightTitle" type="text" />
                </label>
                <label class="admin-field">
                  <span>推荐位主题色</span>
                  <select v-model="adminArticleForm.spotlightAccent">
                    <option value="emerald">emerald</option>
                    <option value="cyan">cyan</option>
                    <option value="amber">amber</option>
                  </select>
                </label>
              </div>

              <label class="admin-field">
                <span>推荐位描述</span>
                <textarea v-model="adminArticleForm.spotlightDescription" rows="2" />
              </label>

              <section class="admin-subsection">
                <div class="admin-subsection-head">
                  <h5>步骤卡片</h5>
                  <button type="button" class="mini-action" @click="addStep">新增步骤</button>
                </div>
                <div
                  v-for="(step, index) in adminArticleForm.strategySteps"
                  :key="`step-${index}`"
                  class="admin-repeat-card"
                >
                  <div class="admin-form-grid four-column">
                    <label class="admin-field">
                      <span>序号</span>
                      <input v-model="step.index" type="text" />
                    </label>
                    <label class="admin-field">
                      <span>标题</span>
                      <input v-model="step.title" type="text" />
                    </label>
                    <label class="admin-field">
                      <span>徽标</span>
                      <input v-model="step.badge" type="text" />
                    </label>
                  </div>
                  <label class="admin-field">
                    <span>描述</span>
                    <textarea v-model="step.description" rows="2" />
                  </label>
                  <button type="button" class="mini-action danger" @click="removeStep(index)">删除步骤</button>
                </div>
              </section>

              <section class="admin-subsection">
                <div class="admin-subsection-head">
                  <h5>洞察卡片</h5>
                  <button type="button" class="mini-action" @click="addInsight">新增洞察</button>
                </div>
                <div
                  v-for="(insight, index) in adminArticleForm.insights"
                  :key="`insight-${index}`"
                  class="admin-repeat-card"
                >
                  <div class="admin-form-grid three-column">
                    <label class="admin-field">
                      <span>标题</span>
                      <input v-model="insight.title" type="text" />
                    </label>
                    <label class="admin-field">
                      <span>主题色</span>
                      <select v-model="insight.accent">
                        <option value="emerald">emerald</option>
                        <option value="cyan">cyan</option>
                        <option value="amber">amber</option>
                      </select>
                    </label>
                  </div>
                  <label class="admin-field">
                    <span>描述</span>
                    <textarea v-model="insight.description" rows="2" />
                  </label>
                  <button type="button" class="mini-action danger" @click="removeInsight(index)">删除洞察</button>
                </div>
              </section>

              <section class="admin-subsection">
                <div class="admin-subsection-head">
                  <h5>代码块</h5>
                  <button type="button" class="mini-action" @click="addCodeBlock">新增代码块</button>
                </div>
                <div
                  v-for="(block, index) in adminArticleForm.codeBlocks"
                  :key="`code-${index}`"
                  class="admin-repeat-card"
                >
                  <div class="admin-form-grid three-column">
                    <label class="admin-field">
                      <span>语言</span>
                      <input v-model="block.language" type="text" />
                    </label>
                    <label class="admin-field">
                      <span>标题</span>
                      <input v-model="block.title" type="text" />
                    </label>
                  </div>
                  <label class="admin-field">
                    <span>代码</span>
                    <textarea v-model="block.code" rows="8" class="code-editor" />
                  </label>
                  <label class="admin-field">
                    <span>讲解要点</span>
                    <textarea v-model="block.calloutsText" rows="3" placeholder="每行一条" />
                  </label>
                  <button type="button" class="mini-action danger" @click="removeCodeBlock(index)">删除代码块</button>
                </div>
              </section>
            </section>
          </div>
        </section>
      </main>

      <aside class="kb-aside">
        <div v-if="articleRelated.length" class="aside-card kb-panel">
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

.code-card {
  overflow: hidden;
}

.code-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px 0;
}

.code-lang {
  background: rgba(18, 48, 71, 0.08);
  color: var(--kb-text);
}

.code-card h4 {
  margin-top: 10px;
  font-size: 20px;
}

.code-card pre {
  margin: 16px 0 0;
  padding: 18px 20px;
  background: #0e2230;
  color: #e8f8ff;
  overflow: auto;
  font-size: 13px;
  line-height: 1.7;
}

.code-card code,
.code-editor {
  font-family: 'Fira Code', 'Consolas', monospace;
  white-space: pre;
}

.code-callouts {
  padding: 16px 20px 20px;
}

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
</style>
