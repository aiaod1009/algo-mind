import assert from 'node:assert/strict'

import {
  KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL,
  buildKnowledgeAdminStats,
  normalizeKnowledgeSlug,
  readTimedCache,
  validateKnowledgeAdminArticle,
  validateKnowledgeAdminConfig,
  writeTimedCache,
} from '../utils/knowledgeBaseAdmin.js'

const run = () => {
  assert.equal(normalizeKnowledgeSlug(' Graph / Shortest Path '), 'graph-shortest-path')
  assert.equal(normalizeKnowledgeSlug('DP__Intro'), 'dp-intro')

  const configValidation = validateKnowledgeAdminConfig(
    {
      siteTitle: 'AlgoMind',
      defaultArticleSlug: 'missing-article',
      quickSearchesText: 'a,b,c,d,e,f,g,h,i,j,k,l,m',
    },
    [{ slug: 'published-article', published: true }],
  )
  assert.equal(configValidation.errors.length, 2)
  assert.match(configValidation.errors[0], /推荐搜索/)
  assert.match(configValidation.errors[1], /默认文章/)

  const articleValidation = validateKnowledgeAdminArticle(
    {
      slug: 'Graph Intro',
      title: '图论',
      sectionId: 'graph',
      sectionTitle: '图论',
      relatedSlugsText: 'graph-intro,missing-one',
      spotlightEnabled: true,
      spotlightTitle: '',
      spotlightDescription: '',
      strategySteps: [{ index: '01', title: '只填标题', description: '', badge: '' }],
      insights: [{ title: '', description: '只有描述', accent: 'emerald' }],
      codeBlocks: [{ language: 'Java', title: '模板', code: '', calloutsText: '' }],
    },
    [{ id: 1, slug: 'graph-intro', published: true }],
    null,
  )
  assert.match(articleValidation.warnings[0], /graph-intro/)
  assert.equal(articleValidation.errors.length, 8)
  assert.ok(articleValidation.errors.some((message) => message.includes('相关文章')))
  assert.ok(articleValidation.errors.some((message) => message.includes('推荐标题')))
  assert.ok(articleValidation.errors.some((message) => message.includes('推荐描述')))
  assert.ok(articleValidation.errors.some((message) => message.includes('步骤')))
  assert.ok(articleValidation.errors.some((message) => message.includes('洞察')))
  assert.ok(articleValidation.errors.some((message) => message.includes('代码块')))

  const stats = buildKnowledgeAdminStats([
    { published: true, spotlightEnabled: true },
    { published: false, spotlightEnabled: false },
    { published: true, spotlightEnabled: false },
  ])
  assert.deepEqual(stats, {
    total: 3,
    published: 2,
    drafts: 1,
    spotlight: 1,
  })

  const storage = {
    value: new Map(),
    getItem(key) {
      return this.value.get(key) ?? null
    },
    setItem(key, value) {
      this.value.set(key, value)
    },
    removeItem(key) {
      this.value.delete(key)
    },
  }

  writeTimedCache('dashboard', { total: 1 }, storage)
  assert.deepEqual(readTimedCache('dashboard', storage, KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL), { total: 1 })

  storage.setItem('expired', JSON.stringify({
    timestamp: Date.now() - KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL - 10,
    data: { total: 2 },
  }))
  assert.equal(readTimedCache('expired', storage, KNOWLEDGE_ADMIN_DASHBOARD_CACHE_TTL), null)
}

run()
console.log('knowledgeBaseAdmin tests passed')
