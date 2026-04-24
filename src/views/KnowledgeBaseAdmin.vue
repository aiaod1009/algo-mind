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
  deriveKnowledgeSectionId,
  readTimedCache,
  serializeComparable,
  validateKnowledgeAdminArticle,
  validateKnowledgeAdminConfig,
  writeTimedCache,
} from '../utils/knowledgeBaseAdmin'

const router = useRouter()
const userStore = useUserStore()

// 新增知识点相关状态
const showAddKnowledgeModal = ref(false)
const importType = ref('') // 'batch' | 'single'
const editMode = ref('') // 'template' | 'custom'
const markdownContent = ref('')

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
  language: '',
  title: '',
  code: '',
  calloutsText: '',
})

// 代码语言检测函数
const detectCodeLanguage = (code) => {
  if (!code || !code.trim()) return ''

  const codeTrimmed = code.trim()

  // Java 特征
  if (codeTrimmed.includes('public class ') ||
      codeTrimmed.includes('private ') ||
      codeTrimmed.includes('System.out.println') ||
      codeTrimmed.includes('import java.') ||
      /public\s+static\s+void\s+main/.test(codeTrimmed)) {
    return 'Java'
  }

  // Python 特征
  if (codeTrimmed.startsWith('#!') ||
      codeTrimmed.includes('def ') ||
      codeTrimmed.includes('import ') && codeTrimmed.includes(':') ||
      codeTrimmed.includes('print(') ||
      codeTrimmed.includes('self.') ||
      /^\s*def\s+\w+\s*\(/.test(codeTrimmed)) {
    return 'Python'
  }

  // JavaScript/TypeScript 特征
  if (codeTrimmed.includes('console.log') ||
      codeTrimmed.includes('const ') ||
      codeTrimmed.includes('let ') ||
      codeTrimmed.includes('var ') ||
      codeTrimmed.includes('function ') ||
      codeTrimmed.includes('=>') ||
      codeTrimmed.includes('async ') ||
      codeTrimmed.includes('await ')) {
    if (codeTrimmed.includes(': ') && (codeTrimmed.includes('interface ') || codeTrimmed.includes(': string') || codeTrimmed.includes(': number'))) {
      return 'TypeScript'
    }
    return 'JavaScript'
  }

  // C/C++ 特征
  if (codeTrimmed.includes('#include') ||
      codeTrimmed.includes('int main') ||
      codeTrimmed.includes('printf(') ||
      codeTrimmed.includes('std::') ||
      codeTrimmed.includes('cout <<') ||
      codeTrimmed.includes('cin >>')) {
    if (codeTrimmed.includes('cout') || codeTrimmed.includes('cin') || codeTrimmed.includes('std::')) {
      return 'C++'
    }
    return 'C'
  }

  // Go 特征
  if (codeTrimmed.includes('package main') ||
      codeTrimmed.includes('func main()') ||
      codeTrimmed.includes('fmt.Println') ||
      codeTrimmed.includes('go func')) {
    return 'Go'
  }

  // Rust 特征
  if (codeTrimmed.includes('fn main()') ||
      codeTrimmed.includes('let mut') ||
      codeTrimmed.includes('pub fn ') ||
      codeTrimmed.includes('impl ') ||
      codeTrimmed.includes('use std::')) {
    return 'Rust'
  }

  // SQL 特征
  if (/^\s*(SELECT|INSERT|UPDATE|DELETE|CREATE|ALTER|DROP)/i.test(codeTrimmed) ||
      codeTrimmed.includes('FROM ') ||
      codeTrimmed.includes('WHERE ')) {
    return 'SQL'
  }

  // HTML 特征
  if (codeTrimmed.startsWith('<!DOCTYPE') ||
      codeTrimmed.startsWith('<html') ||
      codeTrimmed.includes('<div') ||
      codeTrimmed.includes('<span') ||
      codeTrimmed.includes('<body')) {
    return 'HTML'
  }

  // CSS 特征
  if (codeTrimmed.includes('{') && codeTrimmed.includes(':') && codeTrimmed.includes(';') && codeTrimmed.includes('}') ||
      codeTrimmed.startsWith('@media') ||
      codeTrimmed.startsWith('.')) {
    return 'CSS'
  }

  // Shell/Bash 特征
  if (codeTrimmed.startsWith('#!/bin/bash') ||
      codeTrimmed.startsWith('#!/bin/sh') ||
      codeTrimmed.includes('echo ') ||
      codeTrimmed.includes('$(') ||
      /^\s*(ls|cd|mkdir|rm|cp|mv|cat|grep)\s/.test(codeTrimmed)) {
    return 'Shell'
  }

  // 默认返回空，让用户手动选择
  return ''
}

// 检测代码块语言
const detectBlockLanguage = (block) => {
  if (!block.code) return
  const detected = detectCodeLanguage(block.code)
  if (detected) {
    block.language = detected
  }
}

const getCodeBlockLineCount = (code = '') => {
  const normalized = String(code || '').replace(/\r\n/g, '\n')
  return Math.max(normalized.split('\n').length, 8)
}

const getCodeBlockLineNumbers = (code = '') =>
  Array.from({ length: getCodeBlockLineCount(code) }, (_, index) => index + 1)
// 让伪行号栏和 textarea 保持同一滚动位置，维持编辑器式的阅读感。
const syncCodeEditorScroll = (event) => {
  const shell = event.target.closest('.admin-code-editor-shell')
  const gutter = shell?.querySelector('.admin-code-editor-gutter')

  if (gutter) {
    gutter.scrollTop = event.target.scrollTop
  }
}

const createEmptyArticleForm = () => ({
  id: null,
  slug: '',
  title: '',
  englishTitle: '',
  sectionId: '',
  sectionTitle: '',
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
const generatedSectionId = computed(() =>
  deriveKnowledgeSectionId(adminArticleForm.value.sectionTitle, adminArticleForm.value.sectionId),
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

// 新增知识点相关函数
const resetAddKnowledge = () => {
  showAddKnowledgeModal.value = false
  importType.value = ''
  editMode.value = ''
  markdownContent.value = ''
}

const insertMarkdown = (before, after) => {
  const textarea = document.querySelector('.markdown-textarea')
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = markdownContent.value
  const selected = text.substring(start, end)

  markdownContent.value = text.substring(0, start) + before + selected + after + text.substring(end)

  setTimeout(() => {
    textarea.focus()
    const newCursor = start + before.length + selected.length
    textarea.setSelectionRange(newCursor, newCursor)
  }, 0)
}

const saveMarkdownArticle = async () => {
  if (!markdownContent.value.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }

  try {
    // 解析 Markdown 内容创建文章
    const article = {
      title: extractTitleFromMarkdown(markdownContent.value),
      slug: generateSlugFromTitle(extractTitleFromMarkdown(markdownContent.value)),
      content: markdownContent.value,
      sectionTitle: '自定义文章',
      sectionDescription: '',
      steps: [],
      insights: [],
      codeBlocks: [],
      spotlightEnabled: false,
      spotlightTitle: '',
      spotlightAccent: 'emerald',
      spotlightDescription: '',
      sortOrder: (adminArticles.value[adminArticles.value.length - 1]?.sortOrder || 0) + 10,
    }

    await api.createKnowledgeArticle(article)
    await loadAdminDashboard({ preferCache: false })
    ElMessage.success('文章创建成功')
    resetAddKnowledge()
    resetAdminForm()
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '创建文章失败')
  }
}

const extractTitleFromMarkdown = (markdown) => {
  const match = markdown.match(/^#\s+(.+)$/m)
  return match ? match[1] : '未命名文章'
}

const generateSlugFromTitle = (title) => {
  return title
    .toLowerCase()
    .replace(/[^\w\s-]/g, '')
    .replace(/\s+/g, '-')
    .substring(0, 50)
}

// 监听模板模式选择
watch(() => editMode.value, (newMode) => {
  if (newMode === 'template') {
    resetAddKnowledge()
    resetAdminForm()
    // 滚动到表单区域
    setTimeout(() => {
      const formElement = document.querySelector('.admin-article-form')
      if (formElement) {
        formElement.scrollIntoView({ behavior: 'smooth' })
      }
    }, 100)
  }
})

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
      </div>

      <div class="admin-hero-actions">
        <button type="button" class="ghost-btn" @click="goBackToKnowledgeBase">返回知识库</button>
        <button type="button" class="primary-btn" @click="openNewAdminArticle">新增文章</button>
      </div>
    </section>

    <section class="admin-overview admin-panel-shell">
      <div class="status-copy">
        <strong>{{ adminStatusText }}</strong>
      </div>
      <div class="chip-row">
        <span class="admin-chip success">{{ adminStats.published }} 已发布</span>
        <span class="admin-chip muted">{{ adminStats.drafts }} 草稿</span>
        <span class="admin-chip accent">{{ adminStats.spotlight }} 精选</span>
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
      <!-- 左侧：新增知识点 + 运营配置 -->
      <div class="admin-left-panel">
        <!-- 新增知识点卡片 -->
        <section class="admin-config-card add-knowledge-card">
        <header class="admin-card-head">
          <h4>新增知识点</h4>
          <button
            type="button"
            class="primary-btn compact"
            @click="showAddKnowledgeModal = true"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"></line>
              <line x1="5" y1="12" x2="19" y2="12"></line>
            </svg>
            添加知识点
          </button>
        </header>

        <!-- 导入方式选择弹窗 -->
        <div v-if="showAddKnowledgeModal && !importType" class="add-knowledge-modal">
          <div class="modal-overlay" @click="showAddKnowledgeModal = false"></div>
          <div class="modal-content">
            <div class="modal-header">
              <h3>选择导入方式</h3>
              <button type="button" class="close-btn" @click="showAddKnowledgeModal = false">×</button>
            </div>
            <div class="modal-body">
              <div class="import-options">
                <button
                  type="button"
                  class="import-option-card"
                  @click="importType = 'single'"
                >
                  <div class="option-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                      <line x1="16" y1="13" x2="8" y2="13"></line>
                      <line x1="16" y1="17" x2="8" y2="17"></line>
                      <polyline points="10 9 9 9 8 9"></polyline>
                    </svg>
                  </div>
                  <h4>单篇导入</h4>
                  <p>创建一篇新的知识文章</p>
                </button>

                <button
                  type="button"
                  class="import-option-card"
                  @click="importType = 'batch'"
                >
                  <div class="option-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                      <polyline points="14 2 14 8 20 8"></polyline>
                      <path d="M12 18v-6"></path>
                      <path d="M8 15l4-4 4 4"></path>
                    </svg>
                  </div>
                  <h4>批量导入</h4>
                  <p>通过文件批量导入多篇知识文章</p>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 编辑模式选择弹窗 -->
        <div v-if="importType && !editMode" class="add-knowledge-modal">
          <div class="modal-overlay" @click="resetAddKnowledge"></div>
          <div class="modal-content">
            <div class="modal-header">
              <h3>选择编辑模式</h3>
              <button type="button" class="close-btn" @click="resetAddKnowledge">×</button>
            </div>
            <div class="modal-body">
              <div class="edit-options">
                <button
                  type="button"
                  class="edit-option-card"
                  @click="editMode = 'template'"
                >
                  <div class="option-icon template">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                      <line x1="3" y1="9" x2="21" y2="9"></line>
                      <line x1="9" y1="21" x2="9" y2="9"></line>
                    </svg>
                  </div>
                  <h4>按模板填写</h4>
                  <p>使用结构化表单创建文章，包含步骤、代码块等</p>
                </button>

                <button
                  type="button"
                  class="edit-option-card"
                  @click="editMode = 'custom'"
                >
                  <div class="option-icon custom">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M12 19l7-7 3 3-7 7-3-3z"></path>
                      <path d="M18 13l-1.5-7.5L2 2l3.5 14.5L13 18l5-5z"></path>
                      <path d="M2 2l7.586 7.586"></path>
                      <circle cx="11" cy="11" r="2"></circle>
                    </svg>
                  </div>
                  <h4>自定义编辑</h4>
                  <p>使用 Markdown 自由编辑文章内容</p>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Markdown 编辑器弹窗 -->
        <div v-if="editMode === 'custom'" class="add-knowledge-modal large">
          <div class="modal-overlay" @click="resetAddKnowledge"></div>
          <div class="modal-content">
            <div class="modal-header">
              <h3>Markdown 编辑器</h3>
              <button type="button" class="close-btn" @click="resetAddKnowledge">×</button>
            </div>
            <div class="modal-body">
              <div class="markdown-editor">
                <div class="editor-toolbar">
                  <button type="button" @click="insertMarkdown('**', '**')">粗体</button>
                  <button type="button" @click="insertMarkdown('*', '*')">斜体</button>
                  <button type="button" @click="insertMarkdown('# ', '')">标题</button>
                  <button type="button" @click="insertMarkdown('- ', '')">列表</button>
                  <button type="button" @click="insertMarkdown('```\n', '\n```')">代码块</button>
                  <button type="button" @click="insertMarkdown('[', '](url)')">链接</button>
                </div>
                <textarea
                  v-model="markdownContent"
                  class="markdown-textarea"
                  placeholder="在此输入 Markdown 内容..."
                  rows="20"
                ></textarea>
              </div>
            </div>
            <div class="modal-footer">
              <button type="button" class="secondary-btn" @click="resetAddKnowledge">取消</button>
              <button type="button" class="primary-btn" @click="saveMarkdownArticle">
                <img src="../assets/mini_icons/保存.png" alt="save" style="width: 20px; height: 20px; margin-right: 6px; border-radius: 4px; object-fit: cover;" />
                保存文章
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="admin-config-card">
        <header class="admin-card-head">
          <h4>运营配置</h4>
          <button
            type="button"
            class="primary-btn compact"
            :disabled="adminSavingConfig"
            @click="saveAdminConfig"
          >
            <img v-if="!adminSavingConfig" src="../assets/微信图片_20260422225456_55_112.jpg" alt="save" style="width: 16px; height: 16px; margin-right: 4px; border-radius: 3px; object-fit: cover;" />
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
      </div>

      <!-- 右侧：文章列表 -->
      <section class="admin-list-card">
        <header class="admin-card-head">
          <h4>文章列表</h4>
          <span class="admin-meta">{{ filteredAdminArticles.length }} 条</span>
        </header>

        <div v-if="adminLoading && !adminArticles.length" class="sidebar-state">正在加载管理数据...</div>

        <div class="admin-list-toolbar">
          <label class="admin-field compact">
            <span>筛选</span>
            <input v-model="adminListKeyword" type="text" placeholder="搜索标题 / 标识 / 章节" />
          </label>
          <label class="admin-field compact">
            <span>状态</span>
            <select v-model="adminStatusFilter">
              <option value="all">全部</option>
              <option value="published">已发布</option>
              <option value="draft">草稿</option>
              <option value="spotlight">精选</option>
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
              <img v-if="!adminSavingArticle" src="../assets/微信图片_20260422225456_55_112.jpg" alt="save" style="width: 16px; height: 16px; margin-right: 4px; border-radius: 3px; object-fit: cover;" />
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
        </div>

        <div class="admin-form-grid four-column">
          <label class="admin-field">
            <span>栏目 ID（系统生成）</span>
            <input
              :value="generatedSectionId || '填写栏目名称后自动生成'"
              type="text"
              readonly
              class="admin-system-input"
            />
            <small class="admin-field-hint">系统会根据栏目名称自动生成稳定的栏目 ID，无需手动填写。</small>
          </label>
          <label class="admin-field">
            <span>栏目名称</span>
            <input v-model="adminArticleForm.sectionTitle" type="text" />
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
              <option value="emerald">翠绿</option>
              <option value="cyan">青色</option>
              <option value="amber">琥珀</option>
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
                  <option value="emerald">翠绿</option>
                  <option value="cyan">青色</option>
                  <option value="amber">琥珀</option>
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
                <div class="language-display">
                  <input
                    :value="block.language || '自动检测中...'"
                    type="text"
                    readonly
                    class="language-auto-input"
                    :class="{ 'is-detected': block.language }"
                  />
                </div>
              </label>
              <label class="admin-field">
                <span>标题</span>
                <input v-model="block.title" type="text" />
              </label>
            </div>
            <label class="admin-field">
              <span>代码</span>
              <div class="admin-code-editor-shell" :class="{ 'is-detected': block.language }">
                <div class="admin-code-editor-topbar">
                  <div class="admin-code-editor-dots" aria-hidden="true">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                  <div class="admin-code-editor-meta">
                    <span class="admin-code-editor-chip">{{ block.language || 'Auto Detect' }}</span>
                    <span class="admin-code-editor-lines">{{ getCodeBlockLineCount(block.code) }} lines</span>
                  </div>
                </div>
                <div class="admin-code-editor-body">
                  <div class="admin-code-editor-gutter" aria-hidden="true">
                    <span
                      v-for="line in getCodeBlockLineNumbers(block.code)"
                      :key="`code-${index}-line-${line}`"
                    >
                      {{ line }}
                    </span>
                  </div>
                  <textarea
                    v-model="block.code"
                    rows="8"
                    class="code-editor"
                    spellcheck="false"
                    placeholder="// 输入可运行的示例代码、模板或关键片段"
                    @input="detectBlockLanguage(block)"
                    @scroll="syncCodeEditorScroll"
                  />
                </div>
              </div>
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
  grid-template-columns: minmax(0, 0.6fr) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.admin-left-panel {
  display: flex;
  flex-direction: column;
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

.admin-field-hint {
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-muted);
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

.admin-field input.admin-system-input {
  background: linear-gradient(180deg, rgba(244, 248, 252, 0.98), rgba(237, 244, 250, 0.96));
  color: #446177;
  font-weight: 600;
  cursor: default;
}

.admin-validation-list {
  margin: 20px 0 0;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid rgba(239, 68, 68, 0.12);
  background: linear-gradient(135deg, #fef2f2 0%, #fff5f5 100%);
  box-shadow: 0 4px 20px rgba(239, 68, 68, 0.06);
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  list-style: none;
  position: relative;
  overflow: hidden;
}

.admin-validation-list::before {
  content: '⚠️';
  position: absolute;
  top: 12px;
  right: 16px;
  font-size: 24px;
  opacity: 0.15;
  filter: grayscale(100%);
}

.admin-validation-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  color: #991b1b;
  font-weight: 500;
  line-height: 1.5;
  transition: all 0.2s ease;
  animation: slideInError 0.3s ease-out;
}

.admin-validation-list li:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.1);
}

.admin-validation-list li::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #ef4444;
  border-radius: 50%;
  margin-top: 8px;
  flex-shrink: 0;
}

@keyframes slideInError {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
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

.language-display {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.language-auto-input {
  flex: 1;
  padding: 10px 14px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: not-allowed;
}

.language-auto-input.is-detected {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-color: #3b82f6;
  color: #1e40af;
  font-weight: 600;
}

.admin-code-editor-shell {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid rgba(30, 41, 59, 0.76);
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98) 0%, rgba(9, 14, 27, 0.98) 100%);
  box-shadow:
    inset 0 1px 0 rgba(148, 163, 184, 0.08),
    0 16px 30px rgba(15, 23, 42, 0.16);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.admin-code-editor-shell::after {
  content: '';
  position: absolute;
  inset: 54px 0 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(148, 163, 184, 0.05) 1px, transparent 1px);
  background-size: 100% 29px;
  opacity: 0.34;
}

.admin-code-editor-shell:hover {
  border-color: rgba(51, 65, 85, 0.95);
  box-shadow:
    inset 0 1px 0 rgba(148, 163, 184, 0.08),
    0 18px 34px rgba(15, 23, 42, 0.22);
}

.admin-code-editor-shell:focus-within {
  border-color: rgba(56, 189, 248, 0.52);
  box-shadow:
    inset 0 1px 0 rgba(148, 163, 184, 0.08),
    0 0 0 3px rgba(56, 189, 248, 0.12),
    0 22px 38px rgba(15, 23, 42, 0.22);
}

.admin-code-editor-shell.is-detected .admin-code-editor-chip {
  background: rgba(37, 99, 235, 0.18);
  color: #93c5fd;
  border-color: rgba(59, 130, 246, 0.3);
}

.admin-code-editor-topbar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  min-height: 54px;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(51, 65, 85, 0.72);
  background: linear-gradient(180deg, rgba(30, 41, 59, 0.96), rgba(15, 23, 42, 0.92));
}

.admin-code-editor-dots {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  flex-shrink: 0;
}

.admin-code-editor-dots span {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}

.admin-code-editor-dots span:nth-child(1) {
  background: #fb7185;
}

.admin-code-editor-dots span:nth-child(2) {
  background: #fbbf24;
}

.admin-code-editor-dots span:nth-child(3) {
  background: #34d399;
}

.admin-code-editor-meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.admin-code-editor-chip,
.admin-code-editor-lines {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.admin-code-editor-chip {
  border: 1px solid rgba(100, 116, 139, 0.26);
  background: rgba(51, 65, 85, 0.42);
  color: #cbd5e1;
  text-transform: uppercase;
}

.admin-code-editor-lines {
  color: #94a3b8;
  background: rgba(15, 23, 42, 0.62);
}

.admin-code-editor-body {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr);
  align-items: stretch;
  min-height: 280px;
}

.admin-code-editor-gutter {
  overflow: hidden;
  padding: 16px 10px 16px 0;
  border-right: 1px solid rgba(51, 65, 85, 0.72);
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98), rgba(8, 11, 22, 0.98));
  color: #64748b;
  text-align: right;
  user-select: none;
}

.admin-code-editor-gutter span {
  display: block;
  height: 29px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 29px;
  font-variant-numeric: tabular-nums;
}

.admin-field textarea.code-editor {
  min-height: 280px;
  padding: 16px 18px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 29px;
  color: #e2e8f0;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
  resize: vertical;
  transition: all 0.2s ease;
  tab-size: 4;
  -moz-tab-size: 4;
  caret-color: #38bdf8;
  white-space: pre;
  overflow-wrap: normal;
  word-break: normal;
}

.admin-field textarea.code-editor:hover {
  box-shadow: none;
}

.admin-field textarea.code-editor:focus {
  outline: none;
  box-shadow: none;
}

.admin-field textarea.code-editor::placeholder {
  color: #475569;
  font-style: normal;
}

/* 滚动条样式 */
.code-editor::-webkit-scrollbar {
  width: 10px;
  height: 10px;
}

.code-editor::-webkit-scrollbar-track {
  background: rgba(8, 11, 22, 0.92);
  border-radius: 0;
}

.code-editor::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 5px;
  border: 2px solid rgba(8, 11, 22, 0.92);
}

.code-editor::-webkit-scrollbar-thumb:hover {
  background: #475569;
}

.code-editor::-webkit-scrollbar-corner {
  background: rgba(8, 11, 22, 0.92);
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

  .admin-left-panel {
    width: 100%;
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

  .admin-code-editor-topbar {
    padding: 10px 12px;
  }

  .admin-code-editor-meta {
    gap: 8px;
  }

  .admin-code-editor-body {
    grid-template-columns: 48px minmax(0, 1fr);
  }

  .admin-code-editor-gutter {
    padding-right: 8px;
  }

  .admin-code-editor-gutter span {
    font-size: 11px;
  }

  .admin-field textarea.code-editor {
    padding: 14px 14px 14px 12px;
    font-size: 13px;
  }
}

/* 新增知识点相关样式 */
.add-knowledge-card {
  border: 2px dashed #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.02) 0%, rgba(59, 130, 246, 0.05) 100%);
}

.add-knowledge-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal-content {
  position: relative;
  width: 90%;
  max-width: 560px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.add-knowledge-modal.large .modal-content {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #f9fafb 0%, #ffffff 100%);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border: none;
  border-radius: 50%;
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
  color: #111827;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.import-options,
.edit-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.import-option-card,
.edit-option-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 24px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.import-option-card:hover,
.edit-option-card:hover {
  border-color: #3b82f6;
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(59, 130, 246, 0.15);
}

.option-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 16px;
  margin-bottom: 16px;
}

.option-icon svg {
  width: 32px;
  height: 32px;
  color: #3b82f6;
}

.option-icon.template {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.option-icon.template svg {
  color: #f59e0b;
}

.option-icon.custom {
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
}

.option-icon.custom svg {
  color: #10b981;
}

.import-option-card h4,
.edit-option-card h4 {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 8px;
}

.import-option-card p,
.edit-option-card p {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  text-align: center;
  line-height: 1.5;
}

.markdown-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 12px;
  flex-wrap: wrap;
}

.editor-toolbar button {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.editor-toolbar button:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

.markdown-textarea {
  width: 100%;
  padding: 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.7;
  color: #374151;
  resize: vertical;
  outline: none;
  transition: border-color 0.2s;
}

.markdown-textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.markdown-textarea::placeholder {
  color: #9ca3af;
}

@media (max-width: 640px) {
  .import-options,
  .edit-options {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 95%;
    margin: 16px;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 16px;
  }

  .editor-toolbar {
    justify-content: center;
  }
}

@media (max-width: 560px) {
  .admin-code-editor-topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .admin-code-editor-meta {
    justify-content: flex-start;
  }

  .admin-code-editor-body {
    grid-template-columns: 42px minmax(0, 1fr);
  }
}
</style>
