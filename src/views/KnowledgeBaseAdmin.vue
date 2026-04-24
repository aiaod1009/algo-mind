<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../stores/user'
import FlowerPagination from '../components/FlowerPagination.vue'
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

const router = useRouter()
const userStore = useUserStore()

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

const adminDashboard = ref({ config: createEmptyAdminConfig(), articles: [] })
const adminConfigForm = ref(createEmptyAdminConfig())
const adminArticleForm = ref(createEmptyArticleForm())
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

const isAdmin = computed(() =>
  Boolean(userStore.userInfo?.isAdmin) || userStore.userInfo?.email === 'admin@example.com',
)
const adminArticles = computed(() => adminDashboard.value.articles || [])
const adminStats = computed(() => buildKnowledgeAdminStats(adminArticles.value))

const ADMIN_ARTICLE_PAGE_SIZE = 5
const adminPage = ref(1)

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

const totalAdminPages = computed(() =>
  Math.max(1, Math.ceil(filteredAdminArticles.value.length / ADMIN_ARTICLE_PAGE_SIZE)),
)
const hasAdminPagination = computed(() => filteredAdminArticles.value.length > ADMIN_ARTICLE_PAGE_SIZE)
const adminVisibleRange = computed(() => {
  if (!filteredAdminArticles.value.length) {
    return { start: 0, end: 0 }
  }

  const start = (adminPage.value - 1) * ADMIN_ARTICLE_PAGE_SIZE + 1
  const end = Math.min(adminPage.value * ADMIN_ARTICLE_PAGE_SIZE, filteredAdminArticles.value.length)
  return { start, end }
})
const pagedAdminArticles = computed(() => {
  const startIndex = (adminPage.value - 1) * ADMIN_ARTICLE_PAGE_SIZE
  return filteredAdminArticles.value.slice(startIndex, startIndex + ADMIN_ARTICLE_PAGE_SIZE)
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
      Array.isArray(payload.strategySteps)
        ? payload.strategySteps.map((step) => ({ ...createEmptyStep(), ...step }))
        : [],
      createEmptyStep,
    ),
    insights: ensureArrayItems(
      Array.isArray(payload.insights)
        ? payload.insights.map((insight) => ({ ...createEmptyInsight(), ...insight }))
        : [],
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
    return '知识库管理台数据同步中...'
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

const resetAdminState = () => {
  selectedAdminArticleId.value = null
  adminDashboard.value = { config: createEmptyAdminConfig(), articles: [] }
  adminConfigForm.value = createEmptyAdminConfig()
  adminErrorMessage.value = ''
  adminListKeyword.value = ''
  adminStatusFilter.value = 'all'
  adminPage.value = 1
  setAdminArticleForm()
  syncAdminConfigBaseline()
  syncAdminArticleBaseline()
  adminArticleDetailCache.clear()
}

const refreshAdminDashboard = async () => {
  if ((adminHasDirtyConfig.value || adminHasDirtyArticle.value)
    && !window.confirm('刷新会覆盖当前未保存的管理台修改，确认继续吗？')) {
    return
  }

  await loadAdminDashboard({ preferCache: false })
}

const goToAdminPage = (page) => {
  adminPage.value = Math.min(Math.max(page, 1), totalAdminPages.value)
}

async function loadAdminDashboard({ preferCache = true } = {}) {
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
    ElMessage.error(adminErrorMessage.value)
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
    await loadAdminDashboard({ preferCache: false })
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
  adminArticleForm.value.sectionId = validation.normalized.sectionId
    || normalizeKnowledgeSlug(adminArticleForm.value.sectionId)
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
    await loadAdminDashboard({ preferCache: false })
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
    adminArticleDetailCache.delete(selectedAdminArticleId.value)
    await api.deleteKnowledgeAdminArticle(selectedAdminArticleId.value)
    selectedAdminArticleId.value = null
    setAdminArticleForm()
    syncAdminArticleBaseline()
    await loadAdminDashboard({ preferCache: false })
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

const goBackToKnowledgeBase = () => {
  router.push({ name: 'knowledge-base' })
}

const previewArticle = (slug) => {
  if (!slug) return
  router.push({
    name: 'knowledge-base',
    query: { article: slug },
  })
}

watch(
  [adminListKeyword, adminStatusFilter],
  () => {
    adminPage.value = 1
  },
)

watch(
  filteredAdminArticles,
  () => {
    if (adminPage.value > totalAdminPages.value) {
      adminPage.value = totalAdminPages.value
    }
  },
)

watch(
  isAdmin,
  (value) => {
    if (value) {
      void loadAdminDashboard()
      return
    }

    resetAdminState()
    router.replace({ name: 'knowledge-base' })
  },
  { immediate: true },
)
</script>

<template>
  <div class="knowledge-admin-page">
    <section class="admin-hero admin-panel-shell">
      <div>
        <span class="admin-eyebrow">Knowledge Base Admin</span>
        <h1>知识库管理台</h1>
        <p class="admin-hero-copy">
          这是独立于“AlgoMind 算法知识库”的后台路由页面。所有知识库配置维护、文章增删改查和推荐位管理都只在这里进行，不会再嵌套在阅读页内部。
        </p>
      </div>

      <div class="admin-hero-actions">
        <button type="button" class="ghost-btn" @click="goBackToKnowledgeBase">返回知识库</button>
        <button type="button" class="primary-btn" @click="openNewAdminArticle">新增文章</button>
      </div>
    </section>

    <section class="admin-overview admin-panel-shell">
      <div class="status-copy">
        <strong>{{ adminStatusText }}</strong>
        <p>文章列表已接入花瓣分页控件，每页固定显示 5 条内容，支持快速切换和清晰的页码反馈。</p>
      </div>
      <div class="chip-row">
        <span class="admin-chip success">{{ adminStats.published }} Published</span>
        <span class="admin-chip muted">{{ adminStats.drafts }} Drafts</span>
        <span class="admin-chip accent">{{ adminStats.spotlight }} Spotlights</span>
        <span v-if="adminPendingChangesCount" class="admin-chip warning">
          {{ adminPendingChangesCount }} Unsaved
        </span>
        <button type="button" class="mini-action" :disabled="adminLoading" @click="refreshAdminDashboard">
          Refresh
        </button>
      </div>
    </section>

    <div v-if="adminErrorMessage" class="admin-status-banner error">
      <div>
        <strong>管理台数据加载失败</strong>
        <p>{{ adminErrorMessage }}</p>
      </div>
      <button type="button" class="mini-action" :disabled="adminLoading" @click="refreshAdminDashboard">
        重试
      </button>
    </div>

    <div class="admin-grid">
      <section class="admin-config-card">
        <header class="admin-card-head">
          <h4>运营配置</h4>
          <button
            type="button"
            class="primary-btn compact"
            :disabled="adminSavingConfig"
            @click="saveAdminConfig"
          >
            {{ adminSavingConfig ? '保存中...' : '保存配置' }}
          </button>
        </header>

        <ul v-if="adminConfigValidationMessages.length" class="admin-validation-list">
          <li v-for="message in adminConfigValidationMessages" :key="`config-${message}`">{{ message }}</li>
        </ul>

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
          <span class="admin-meta">{{ filteredAdminArticles.length }} 条</span>
        </header>

        <div v-if="adminLoading && !adminArticles.length" class="sidebar-state">正在加载管理数据...</div>

        <div class="admin-list-toolbar">
          <label class="admin-field compact">
            <span>Filter</span>
            <input v-model="adminListKeyword" type="text" placeholder="Search by title / slug / section" />
          </label>
          <label class="admin-field compact">
            <span>Status</span>
            <select v-model="adminStatusFilter">
              <option value="all">All</option>
              <option value="published">Published</option>
              <option value="draft">Draft</option>
              <option value="spotlight">Spotlight</option>
            </select>
          </label>
        </div>

        <div v-if="filteredAdminArticles.length" class="admin-pagination-summary">
          <div class="admin-pagination-copy">
            <strong>当前展示第 {{ adminVisibleRange.start }} - {{ adminVisibleRange.end }} 条</strong>
            <p>共 {{ filteredAdminArticles.length }} 条结果，每页 5 条</p>
          </div>
          <span class="admin-page-progress">第 {{ adminPage }} / {{ totalAdminPages }} 页</span>
        </div>

        <div v-if="!adminLoading && !filteredAdminArticles.length" class="sidebar-state">当前筛选条件下没有文章。</div>

        <button
          v-for="item in pagedAdminArticles"
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

        <div v-if="hasAdminPagination" class="admin-pagination">
          <FlowerPagination
            :total="totalAdminPages"
            :defaultPage="adminPage"
            @change="goToAdminPage"
          />
        </div>
      </section>

      <section class="admin-editor-card">
        <header class="admin-card-head">
          <h4>{{ selectedAdminArticleId ? '编辑文章' : '新建文章' }}</h4>
          <div class="admin-card-actions">
            <button
              v-if="selectedAdminArticleId"
              type="button"
              class="ghost-btn compact"
              :disabled="!adminArticleForm.slug"
              @click="previewArticle(adminArticleForm.slug)"
            >
              预览文章
            </button>
            <button
              v-if="selectedAdminArticleId"
              type="button"
              class="ghost-btn compact danger"
              :disabled="adminDeletingArticle"
              @click="deleteAdminArticle"
            >
              {{ adminDeletingArticle ? '删除中...' : '删除文章' }}
            </button>
            <button
              type="button"
              class="primary-btn compact"
              :disabled="adminSavingArticle"
              @click="saveAdminArticle"
            >
              {{ adminSavingArticle ? '保存中...' : '保存文章' }}
            </button>
          </div>
        </header>

        <ul v-if="adminEditorValidationMessages.length" class="admin-validation-list">
          <li v-for="message in adminEditorValidationMessages" :key="`editor-${message}`">{{ message }}</li>
        </ul>

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

        <div class="admin-form-grid three-column">
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
            <div class="admin-form-grid three-column">
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
            <div class="admin-form-grid two-column">
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
            <div class="admin-form-grid two-column">
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
  </div>
</template>

<style scoped>
.knowledge-admin-page {
  --admin-bg: linear-gradient(180deg, rgba(244, 250, 247, 0.96) 0%, rgba(248, 251, 255, 0.98) 100%);
  --panel-bg: rgba(255, 255, 255, 0.9);
  --panel-border: rgba(154, 178, 196, 0.28);
  --panel-shadow: 0 24px 60px rgba(16, 42, 67, 0.08);
  --text-main: #123047;
  --text-muted: #5f7286;
  --emerald: #1ea97c;
  --cyan: #1f94a8;
  --amber: #d48c24;
  max-width: 1480px;
  margin: 0 auto;
  padding: 32px 24px 72px;
  color: var(--text-main);
}

.admin-panel-shell,
.admin-status-banner,
.admin-config-card,
.admin-list-card,
.admin-editor-card {
  background: var(--panel-bg);
  border: 1px solid var(--panel-border);
  box-shadow: var(--panel-shadow);
  backdrop-filter: blur(18px) saturate(140%);
}

.admin-hero,
.admin-overview,
.admin-config-card,
.admin-list-card,
.admin-editor-card,
.admin-status-banner {
  border-radius: 28px;
  padding: 24px;
}

.admin-hero,
.admin-overview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.admin-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--cyan);
}

.admin-hero h1 {
  margin: 12px 0 10px;
  font-size: 40px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.admin-hero-copy,
.status-copy p,
.admin-status-banner p,
.admin-article-row p {
  margin: 0;
  line-height: 1.7;
  color: var(--text-muted);
}

.admin-hero-actions,
.chip-row,
.admin-card-actions,
.admin-row-meta,
.admin-switch-row,
.admin-subsection-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-copy strong {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
}

.admin-overview {
  margin-top: 18px;
}

.admin-status-banner {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-color: rgba(189, 87, 87, 0.24);
  background: rgba(255, 246, 246, 0.94);
}

.admin-status-banner strong {
  display: block;
  margin-bottom: 6px;
}

.primary-btn,
.ghost-btn,
.mini-action,
.admin-article-row {
  border: none;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.primary-btn,
.ghost-btn {
  border-radius: 999px;
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 700;
}

.primary-btn {
  background: linear-gradient(135deg, var(--emerald), #167a65);
  color: #fff;
  box-shadow: 0 12px 24px rgba(30, 169, 124, 0.24);
}

.ghost-btn {
  background: rgba(255, 255, 255, 0.72);
  color: var(--text-main);
  border: 1px solid rgba(154, 178, 196, 0.28);
}

.primary-btn.compact,
.ghost-btn.compact {
  padding: 10px 14px;
  font-size: 13px;
}

.ghost-btn.danger,
.mini-action.danger {
  color: #b54747;
}

.primary-btn:disabled,
.ghost-btn:disabled,
.mini-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.mini-action {
  border-radius: 999px;
  padding: 9px 12px;
  background: rgba(18, 48, 71, 0.06);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 700;
}

.primary-btn:hover:not(:disabled),
.ghost-btn:hover:not(:disabled),
.mini-action:hover:not(:disabled),
.admin-article-row:hover {
  transform: translateY(-1px);
}

.admin-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 700;
  background: rgba(18, 48, 71, 0.08);
  color: var(--text-main);
}

.admin-chip.success {
  background: rgba(30, 169, 124, 0.12);
  color: #148564;
}

.admin-chip.muted {
  background: rgba(18, 48, 71, 0.08);
  color: var(--text-muted);
}

.admin-chip.accent {
  background: rgba(31, 148, 168, 0.12);
  color: #15798b;
}

.admin-chip.warning {
  background: rgba(212, 140, 36, 0.12);
  color: #a76416;
}

.admin-grid {
  margin-top: 22px;
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(320px, 0.75fr);
  gap: 18px;
}

.admin-editor-card {
  grid-column: 1 / -1;
}

.admin-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.admin-card-head h4,
.admin-subsection-head h5 {
  margin: 0;
}

.admin-meta {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-muted);
}

.admin-list-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(180px, 0.8fr);
  gap: 12px;
  margin-top: 16px;
}

.admin-pagination-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 16px;
  padding: 14px 16px;
  border: 1px solid rgba(132, 156, 176, 0.16);
  border-radius: 18px;
  background: rgba(247, 250, 255, 0.92);
}

.admin-pagination-copy strong {
  display: block;
  font-size: 14px;
  color: var(--text-main);
}

.admin-pagination-copy p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-muted);
}

.admin-page-progress {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 92px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(18, 48, 71, 0.08);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.sidebar-state {
  margin-top: 16px;
  color: var(--text-muted);
  font-size: 14px;
}

.admin-article-row {
  width: 100%;
  margin-top: 14px;
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(248, 251, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  text-align: left;
}

.admin-article-row.active {
  border: 1px solid rgba(31, 148, 168, 0.28);
  box-shadow: 0 12px 28px rgba(31, 148, 168, 0.12);
}

.admin-article-row strong {
  display: block;
  margin-bottom: 6px;
  font-size: 15px;
}

.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid rgba(132, 156, 176, 0.14);
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
}

.admin-field input,
.admin-field textarea,
.admin-field select {
  border: 1px solid rgba(135, 158, 175, 0.18);
  border-radius: 14px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.94);
  color: var(--text-main);
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
  font-size: 14px;
  font-weight: 600;
}

.admin-switch-row label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.admin-subsection {
  margin-top: 18px;
}

.admin-repeat-card {
  margin-top: 14px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(248, 251, 255, 0.96);
  border: 1px solid rgba(154, 178, 196, 0.18);
}

.code-editor {
  min-height: 220px;
  font-family: 'Consolas', 'Courier New', monospace;
}

@media (max-width: 1100px) {
  .admin-hero,
  .admin-overview,
  .admin-grid,
  .admin-pagination-summary,
  .admin-list-toolbar,
  .admin-form-grid.two-column,
  .admin-form-grid.three-column,
  .admin-form-grid.four-column {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .admin-card-head,
  .admin-card-actions,
  .admin-status-banner,
  .admin-article-row,
  .admin-pagination-summary {
    flex-direction: column;
    align-items: stretch;
  }

  .admin-row-meta,
  .chip-row,
  .admin-hero-actions {
    justify-content: flex-start;
  }

  .admin-page-progress {
    align-self: flex-start;
  }
}

@media (max-width: 768px) {
  .knowledge-admin-page {
    padding: 24px 14px 56px;
  }

  .admin-hero,
  .admin-overview,
  .admin-config-card,
  .admin-list-card,
  .admin-editor-card,
  .admin-status-banner {
    padding: 18px;
    border-radius: 22px;
  }

  .admin-hero h1 {
    font-size: 30px;
  }
}
</style>
