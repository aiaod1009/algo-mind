export const KNOWLEDGE_SPOTLIGHT_ACCENTS = ['emerald', 'cyan', 'amber']
export const KNOWLEDGE_ADMIN_DASHBOARD_CACHE_KEY = 'algo-mind:knowledge-admin-dashboard'
export const KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL = 60 * 1000

const trimToEmpty = (value) => (typeof value === 'string' ? value.trim() : '')

const normalizeCollection = (items, mapper) => (Array.isArray(items) ? items : [])
  .map(mapper)
  .filter(Boolean)

export const normalizeKnowledgeSlug = (value) => trimToEmpty(value)
  .toLowerCase()
  .replace(/[^a-z0-9-]+/g, '-')
  .replace(/-{2,}/g, '-')
  .replace(/(^-|-$)/g, '')

const buildStableSectionHash = (value) => {
  let hash = 0
  const normalized = trimToEmpty(value).toLowerCase()

  for (const char of normalized) {
    hash = (hash * 131 + char.charCodeAt(0)) >>> 0
  }

  return hash.toString(36)
}

export const deriveKnowledgeSectionId = (sectionTitle, fallbackSectionId = '') => {
  const normalizedTitle = normalizeKnowledgeSlug(sectionTitle)
  if (normalizedTitle) {
    return normalizedTitle
  }

  const rawTitle = trimToEmpty(sectionTitle)
  if (rawTitle) {
    return `section-${buildStableSectionHash(rawTitle)}`
  }

  return normalizeKnowledgeSlug(fallbackSectionId)
}

export const splitKnowledgeCommaItems = (value) => trimToEmpty(value)
  .split(/[,\n，]+/)
  .map((item) => item.trim())
  .filter(Boolean)

export const splitKnowledgeLineItems = (value) => trimToEmpty(value)
  .split(/\r?\n/)
  .map((item) => item.trim())
  .filter(Boolean)

export const serializeComparable = (value) => JSON.stringify(value ?? null)

export const buildKnowledgeAdminStats = (articles = []) => {
  const safeArticles = Array.isArray(articles) ? articles : []
  const published = safeArticles.filter((item) => item?.published).length
  const spotlight = safeArticles.filter((item) => item?.spotlightEnabled).length

  return {
    total: safeArticles.length,
    published,
    drafts: Math.max(safeArticles.length - published, 0),
    spotlight,
  }
}

export const validateKnowledgeAdminConfig = (form = {}, articleSummaries = []) => {
  const errors = []
  const quickSearches = splitKnowledgeCommaItems(form.quickSearchesText || '')
  const defaultArticleSlug = normalizeKnowledgeSlug(form.defaultArticleSlug || '')
  const publishedSlugs = new Set(
    normalizeCollection(
      articleSummaries.filter((item) => item?.published),
      (item) => normalizeKnowledgeSlug(item.slug),
    ),
  )

  if (!trimToEmpty(form.siteTitle)) {
    errors.push('站点标题不能为空。')
  }

  if (quickSearches.length > 12) {
    errors.push('推荐搜索最多保留 12 个关键词，避免首页过于拥挤。')
  }

  if (defaultArticleSlug && !publishedSlugs.has(defaultArticleSlug)) {
    errors.push('默认文章必须是已发布的知识文章。')
  }

  return {
    errors,
    normalized: {
      defaultArticleSlug,
      quickSearches,
    },
  }
}

export const validateKnowledgeAdminArticle = (form = {}, articleSummaries = [], selectedId = null) => {
  const errors = []
  const warnings = []
  const normalizedSlug = normalizeKnowledgeSlug(form.slug || '')
  const normalizedSectionId = deriveKnowledgeSectionId(form.sectionTitle || '', form.sectionId || '')
  const relatedSlugs = splitKnowledgeCommaItems(form.relatedSlugsText || '')
    .map(normalizeKnowledgeSlug)
    .filter(Boolean)
  const duplicateSlug = articleSummaries.some((item) =>
    normalizeKnowledgeSlug(item?.slug) === normalizedSlug && item?.id !== selectedId)
  const availableSlugs = new Set(
    normalizeCollection(articleSummaries, (item) => normalizeKnowledgeSlug(item?.slug)),
  )

  if (!normalizedSlug) {
    errors.push('slug 不能为空。')
  }

  if (!trimToEmpty(form.title)) {
    errors.push('文章标题不能为空。')
  }


  if (!trimToEmpty(form.sectionTitle)) {
    errors.push('栏目名称不能为空。')
  }

  if (duplicateSlug) {
    errors.push('当前 slug 已存在，请更换为新的唯一标识。')
  }

  if (trimToEmpty(form.slug) && normalizedSlug !== trimToEmpty(form.slug).toLowerCase()) {
    warnings.push(`slug 将被规范化为 ${normalizedSlug || '空值'}。`)
  }

  if (form.spotlightEnabled) {
    if (!trimToEmpty(form.spotlightTitle)) {
      errors.push('开启推荐位时，推荐标题不能为空。')
    }
    if (!trimToEmpty(form.spotlightDescription)) {
      errors.push('开启推荐位时，推荐描述不能为空。')
    }
    if (!KNOWLEDGE_SPOTLIGHT_ACCENTS.includes(trimToEmpty(form.spotlightAccent || 'emerald'))) {
      errors.push('推荐位主题色只支持 emerald、cyan、amber。')
    }
  }

  if (relatedSlugs.includes(normalizedSlug)) {
    errors.push('相关文章不能包含当前文章自身。')
  }

  const missingRelated = relatedSlugs.filter((slug) => !availableSlugs.has(slug))
  if (missingRelated.length) {
    errors.push(`以下相关文章 slug 尚不存在：${missingRelated.join('、')}。`)
  }

  ;(Array.isArray(form.strategySteps) ? form.strategySteps : []).forEach((step, index) => {
    const hasAnyValue = Boolean(
      trimToEmpty(step?.index) || trimToEmpty(step?.title) || trimToEmpty(step?.description) || trimToEmpty(step?.badge),
    )
    if (hasAnyValue && (!trimToEmpty(step?.title) || !trimToEmpty(step?.description))) {
      errors.push(`步骤 ${index + 1} 需要同时填写标题和描述。`)
    }
  })

  ;(Array.isArray(form.insights) ? form.insights : []).forEach((insight, index) => {
    const hasAnyValue = Boolean(trimToEmpty(insight?.title) || trimToEmpty(insight?.description))
    if (hasAnyValue && (!trimToEmpty(insight?.title) || !trimToEmpty(insight?.description))) {
      errors.push(`洞察 ${index + 1} 需要同时填写标题和描述。`)
    }
  })

  ;(Array.isArray(form.codeBlocks) ? form.codeBlocks : []).forEach((block, index) => {
    const hasAnyValue = Boolean(
      trimToEmpty(block?.language) || trimToEmpty(block?.title) || trimToEmpty(block?.code) || trimToEmpty(block?.calloutsText),
    )
    if (hasAnyValue && (!trimToEmpty(block?.title) || !trimToEmpty(block?.code))) {
      errors.push(`代码块 ${index + 1} 需要同时填写标题和代码。`)
    }
  })

  return {
    errors,
    warnings,
    normalized: {
      slug: normalizedSlug,
      sectionId: normalizedSectionId,
      relatedSlugs,
    },
  }
}

export const readTimedCache = (key, storage, maxAge = KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL) => {
  if (!storage?.getItem) {
    return null
  }

  try {
    const raw = storage.getItem(key)
    if (!raw) {
      return null
    }

    const parsed = JSON.parse(raw)
    if (!parsed?.timestamp || Date.now() - parsed.timestamp > maxAge) {
      storage.removeItem?.(key)
      return null
    }

    return parsed.data ?? null
  } catch (error) {
    storage.removeItem?.(key)
    return null
  }
}

export const writeTimedCache = (key, data, storage) => {
  if (!storage?.setItem) {
    return
  }

  storage.setItem(key, JSON.stringify({
    timestamp: Date.now(),
    data,
  }))
}
