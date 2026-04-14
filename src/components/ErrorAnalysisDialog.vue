<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: 'AI 智能分析',
  },
  content: {
    type: String,
    default: '',
  },
  analysisData: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const activeTab = ref('analysis')
const contentReady = ref(false)
const bufferTimer = ref(null)

// 5秒缓冲加载
const startBufferLoading = () => {
  contentReady.value = false
  if (bufferTimer.value) {
    clearTimeout(bufferTimer.value)
  }
  bufferTimer.value = setTimeout(() => {
    contentReady.value = true
  }, 5000)
}

watch(
  () => props.modelValue,
  (isVisible) => {
    if (isVisible) {
      activeTab.value = 'analysis'
      startBufferLoading()
    } else {
      contentReady.value = false
      if (bufferTimer.value) {
        clearTimeout(bufferTimer.value)
      }
    }
  }
)

const escapeRegExp = (value) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

const extractSectionText = (text, sectionTitles = []) => {
  for (const title of sectionTitles) {
    const match = text.match(
      new RegExp(`${escapeRegExp(title)}:\\s*([\\s\\S]*?)(?=\\n[A-Z][A-Za-z\\s]+:|$)`, 'i')
    )

    if (match?.[1]) {
      return match[1].trim()
    }
  }

  return ''
}

const parseSectionList = (text, sectionTitles = []) => {
  const sectionText = extractSectionText(text, sectionTitles)

  if (!sectionText) {
    return []
  }

  return sectionText
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .flatMap((line) =>
      line
        .split(/[、,，]/)
        .map((item) => item.replace(/^[-*•\d.\s]+/, '').trim())
        .filter(Boolean)
    )
}

const normalizeTopicList = (items = []) => {
  if (!Array.isArray(items)) {
    return []
  }

  return items
    .map((item) => {
      if (typeof item === 'string') {
        return {
          title: item,
          description: '',
        }
      }

      if (item && typeof item === 'object') {
        const title = item.title || item.name || item.label || item.topic || item.point || ''
        const description = item.description || item.detail || item.summary || item.content || ''

        if (!title) {
          return null
        }

        return {
          title,
          description,
        }
      }

      return null
    })
    .filter(Boolean)
}

// 解析纯文本内容为结构化数据
const parsedData = computed(() => {
  // 如果有结构化数据，直接使用
  if (props.analysisData && Object.keys(props.analysisData).length > 0) {
    return props.analysisData
  }
  
  // 否则解析 content 文本
  if (!props.content) {
    return {
      summary: '暂无分析结果',
      rootCause: '',
      explanation: '',
      knowledgePoints: [],
      relatedTopics: [],
      suggestions: []
    }
  }
  
  const text = props.content
  const data = {
    summary: '',
    rootCause: '',
    explanation: '',
    knowledgePoints: [],
    relatedTopics: [],
    suggestions: []
  }
  
  // 提取 Summary
  const summaryMatch = text.match(/Summary:\s*([^\n]+(?:\n(?![A-Z][a-z]+:)[^\n]+)*)/i)
  if (summaryMatch) {
    data.summary = summaryMatch[1].trim()
  }
  
  // 提取 Root cause
  const rootCauseMatch = text.match(/Root cause:\s*([^\n]+(?:\n(?![A-Z][a-z]+:)[^\n]+)*)/i)
  if (rootCauseMatch) {
    data.rootCause = rootCauseMatch[1].trim()
  }
  
  // 提取 Explanation
  const explanationMatch = text.match(/Explanation:\s*([^\n]+(?:\n(?![A-Z][a-z]+:)[^\n]+)*)/i)
  if (explanationMatch) {
    data.explanation = explanationMatch[1].trim()
  }
  data.knowledgePoints = parseSectionList(text, ['Knowledge points', 'Knowledge Points', 'Key concepts'])
  data.relatedTopics = parseSectionList(text, ['Related topics', 'Related Topics', 'Topics'])
  
  // 提取 Suggestions
  const suggestionsMatch = text.match(/Suggestions:\s*([\s\S]*?)(?=\n\n|$)/i)
  if (suggestionsMatch) {
    const suggestionsText = suggestionsMatch[1].trim()
    const suggestionLines = suggestionsText.split('\n').filter(line => line.trim().startsWith('-') || line.trim().startsWith('•'))
    data.suggestions = suggestionLines.map((line, index) => ({
      title: `改进建议 ${index + 1}`,
      description: line.replace(/^[-•]\s*/, '').trim(),
      priority: index === 0 ? 'high' : 'medium'
    }))
  }
  
  // 如果没有解析到 summary，使用整个内容作为 summary
  if (!data.summary && !data.rootCause) {
    data.summary = text.slice(0, 200) + (text.length > 200 ? '...' : '')
    data.rootCause = text
  }
  
  return data
})

const hasData = computed(() => {
  return parsedData.value.summary || parsedData.value.rootCause || parsedData.value.suggestions.length > 0
})

const normalizedKnowledgePoints = computed(() => normalizeTopicList(parsedData.value.knowledgePoints))
const normalizedRelatedTopics = computed(() => normalizeTopicList(parsedData.value.relatedTopics))
const hasKnowledgeContent = computed(() => {
  return normalizedKnowledgePoints.value.length > 0 || normalizedRelatedTopics.value.length > 0
})

const getPriorityColor = (priority) => {
  const colors = {
    high: '#ef4444',
    medium: '#f59e0b',
    low: '#22c55e',
  }
  return colors[priority] || '#6b7280'
}

const getPriorityLabel = (priority) => {
  const labels = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级',
  }
  return labels[priority] || priority
}

// ========== 假数据：同类型强化 ==========
const reinforcementData = ref({
  currentLevel: {
    title: '动态规划入门',
    description: '掌握动态规划的基本思想和解题步骤',
    progress: 65,
    totalProblems: 20,
    solvedProblems: 13,
  },
  similarProblems: [
    {
      id: 1,
      title: '爬楼梯问题',
      difficulty: 'easy',
      type: '经典例题',
      relevance: 95,
      estimatedTime: '10分钟',
      isRecommended: true,
    },
    {
      id: 2,
      title: '斐波那契数列',
      difficulty: 'easy',
      type: '基础练习',
      relevance: 90,
      estimatedTime: '8分钟',
      isRecommended: false,
    },
    {
      id: 3,
      title: '不同路径',
      difficulty: 'medium',
      type: '进阶挑战',
      relevance: 85,
      estimatedTime: '15分钟',
      isRecommended: true,
    },
    {
      id: 4,
      title: '最长递增子序列',
      difficulty: 'medium',
      type: '进阶挑战',
      relevance: 80,
      estimatedTime: '20分钟',
      isRecommended: false,
    },
  ],
  weakPoints: [
    { name: '状态转移方程', mastery: 45, suggestion: '多练习经典例题，理解递推关系' },
    { name: '初始条件设置', mastery: 60, suggestion: '注意边界条件的处理' },
    { name: '空间优化', mastery: 30, suggestion: '尝试滚动数组优化' },
  ],
})

// ========== 假数据：持续追踪 ==========
const trackingData = ref({
  learningPath: {
    currentStage: '基础巩固期',
    nextStage: '进阶提升期',
    stageProgress: 68,
    daysActive: 15,
    streakDays: 5,
  },
  recentMistakes: [
    {
      id: 101,
      title: '两数之和',
      mistakeType: '时间复杂度超标',
      date: '2024-01-15',
      isResolved: true,
      reviewCount: 3,
    },
    {
      id: 102,
      title: '合并两个有序数组',
      mistakeType: '边界条件错误',
      date: '2024-01-14',
      isResolved: false,
      reviewCount: 1,
    },
    {
      id: 103,
      title: '最长回文子串',
      mistakeType: '算法选择不当',
      date: '2024-01-12',
      isResolved: true,
      reviewCount: 2,
    },
  ],
  adaptiveRecommendations: [
    {
      id: 'r1',
      title: '针对性练习：数组遍历',
      reason: '基于你最近3次数组类错题分析',
      priority: 'high',
      linkedReinforcement: [1, 3], // 关联的同类型强化题目ID
    },
    {
      id: 'r2',
      title: '算法思维训练：双指针',
      reason: '检测到你在双指针类题目上反复出错',
      priority: 'medium',
      linkedReinforcement: [2, 4],
    },
  ],
  weeklyStats: {
    problemsSolved: 23,
    accuracy: 78,
    improvement: '+12%',
    weakTopics: ['动态规划', '二分查找'],
  },
})

// 获取难度标签颜色
const getDifficultyColor = (difficulty) => {
  const colors = {
    easy: '#22c55e',
    medium: '#f59e0b',
    hard: '#ef4444',
  }
  return colors[difficulty] || '#6b7280'
}

// 获取难度中文标签
const getDifficultyLabel = (difficulty) => {
  const labels = {
    easy: '简单',
    medium: '中等',
    hard: '困难',
  }
  return labels[difficulty] || difficulty
}

// 获取关联的强化题目
const getLinkedReinforcementProblems = (linkedIds) => {
  return reinforcementData.value.similarProblems.filter(p => linkedIds.includes(p.id))
}
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog-backdrop">
      <div v-if="visible" class="ai-analysis-modal" @click.self="visible = false">
        <Transition name="dialog-scale">
          <div v-if="visible" class="ai-analysis-card">
            <!-- 头部 -->
            <div class="ai-header">
              <div class="ai-title-wrapper">
                <div class="ai-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                  </svg>
                </div>
                <div class="ai-title-group">
                  <h2 class="ai-title">{{ title }}</h2>
                  <span class="ai-subtitle">智能错题分析助手</span>
                </div>
              </div>
              <button class="ai-close-btn" @click="visible = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="ai-loading">
              <div class="ai-loading-spinner"></div>
              <p>AI 正在分析中...</p>
            </div>

            <!-- 缓冲加载状态 -->
            <div v-else-if="!contentReady" class="ai-loading ai-buffer-loading">
              <div class="ai-loading-spinner"></div>
              <p>正在准备分析内容...</p>
              <div class="buffer-progress">
                <div class="buffer-progress-bar"></div>
              </div>
              <span class="buffer-hint">预计等待 5 秒</span>
            </div>

            <!-- 内容区域 -->
            <div v-else class="ai-content">
              <!-- 标签页 -->
              <div class="ai-tabs">
                <button 
                  :class="['ai-tab', { active: activeTab === 'analysis' }]"
                  @click="activeTab = 'analysis'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 16v-4M12 8h.01"/>
                  </svg>
                  <span>错误分析</span>
                </button>
                <button 
                  :class="['ai-tab', { active: activeTab === 'reinforcement' }]"
                  @click="activeTab = 'reinforcement'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                  </svg>
                  <span>同类型强化</span>
                </button>
                <button 
                  :class="['ai-tab', { active: activeTab === 'tracking' }]"
                  @click="activeTab = 'tracking'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                  </svg>
                  <span>持续追踪</span>
                </button>
              </div>

              <!-- 错误分析面板 -->
              <div v-if="activeTab === 'analysis'" class="ai-panel">
                <!-- 总结卡片 -->
                <div class="ai-summary-box">
                  <div class="ai-summary-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                    </svg>
                  </div>
                  <div class="ai-summary-content">
                    <h3>分析总结</h3>
                    <p>{{ parsedData.summary || '暂无分析总结' }}</p>
                  </div>
                </div>

                <!-- 错误原因 -->
                <div class="ai-section" v-if="parsedData.rootCause || parsedData.explanation">
                  <div class="ai-section-header">
                    <div class="ai-section-icon error">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="15" y1="9" x2="9" y2="15"/>
                        <line x1="9" y1="9" x2="15" y2="15"/>
                      </svg>
                    </div>
                    <h4>错误原因</h4>
                  </div>
                  <div class="ai-section-body">
                    <p>{{ parsedData.rootCause || parsedData.explanation }}</p>
                  </div>
                </div>

                <!-- 详细解释 -->
                <div class="ai-section" v-if="parsedData.explanation && parsedData.rootCause">
                  <div class="ai-section-header">
                    <div class="ai-section-icon info">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <path d="M12 16v-4M12 8h.01"/>
                      </svg>
                    </div>
                    <h4>详细解释</h4>
                  </div>
                  <div class="ai-section-body">
                    <p>{{ parsedData.explanation }}</p>
                  </div>
                </div>

                <!-- 知识点 -->
                <div class="ai-section" v-if="normalizedKnowledgePoints.length">
                  <div class="ai-section-header">
                    <div class="ai-section-icon knowledge">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                      </svg>
                    </div>
                    <h4>&#x6D89;&#x53CA;&#x77E5;&#x8BC6;&#x70B9;</h4>
                  </div>
                  <div class="ai-section-body">
                    <div class="knowledge-tags">
                      <span class="knowledge-tag" v-for="(point, index) in normalizedKnowledgePoints" :key="`${point.title}-${index}`">
                        {{ point.title }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 同类型强化面板 -->
              <div v-if="activeTab === 'reinforcement'" class="ai-panel">
                <!-- 当前学习进度 -->
                <div class="ai-summary-box ai-summary-box--reinforcement">
                  <div class="ai-summary-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                    </svg>
                  </div>
                  <div class="ai-summary-content">
                    <h3>{{ reinforcementData.currentLevel.title }}</h3>
                    <p>{{ reinforcementData.currentLevel.description }}</p>
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: reinforcementData.currentLevel.progress + '%' }"></div>
                      <span class="progress-text">{{ reinforcementData.currentLevel.solvedProblems }}/{{ reinforcementData.currentLevel.totalProblems }} 题</span>
                    </div>
                  </div>
                </div>

                <!-- 薄弱环节 -->
                <div class="ai-section">
                  <div class="ai-section-header">
                    <div class="ai-section-icon weak">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                      </svg>
                    </div>
                    <h4>薄弱环节分析</h4>
                  </div>
                  <div class="ai-section-body">
                    <div class="weak-points-list">
                      <div v-for="(point, index) in reinforcementData.weakPoints" :key="index" class="weak-point-item">
                        <div class="weak-point-header">
                          <span class="weak-point-name">{{ point.name }}</span>
                          <div class="mastery-bar">
                            <div class="mastery-fill" :style="{ width: point.mastery + '%', backgroundColor: point.mastery < 40 ? '#ef4444' : point.mastery < 70 ? '#f59e0b' : '#22c55e' }"></div>
                          </div>
                          <span class="mastery-text">{{ point.mastery }}%</span>
                        </div>
                        <p class="weak-point-suggestion">{{ point.suggestion }}</p>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 相似题目推荐 -->
                <div class="ai-section">
                  <div class="ai-section-header">
                    <div class="ai-section-icon practice">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                      </svg>
                    </div>
                    <h4>同类型强化练习</h4>
                  </div>
                  <div class="ai-section-body">
                    <div class="similar-problems-list">
                      <div v-for="problem in reinforcementData.similarProblems" :key="problem.id" 
                           class="similar-problem-card" :class="{ recommended: problem.isRecommended }">
                        <div class="problem-header">
                          <h5 class="problem-title">{{ problem.title }}</h5>
                          <span v-if="problem.isRecommended" class="recommend-badge">推荐</span>
                        </div>
                        <div class="problem-meta">
                          <span class="difficulty-badge" :style="{ backgroundColor: getDifficultyColor(problem.difficulty) + '20', color: getDifficultyColor(problem.difficulty) }">
                            {{ getDifficultyLabel(problem.difficulty) }}
                          </span>
                          <span class="problem-type">{{ problem.type }}</span>
                          <span class="relevance-score">相关度 {{ problem.relevance }}%</span>
                        </div>
                        <div class="problem-footer">
                          <span class="estimated-time">预计用时 {{ problem.estimatedTime }}</span>
                          <button class="practice-btn">去练习</button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 持续追踪面板 -->
              <div v-if="activeTab === 'tracking'" class="ai-panel">
                <!-- 学习路径进度 -->
                <div class="ai-summary-box ai-summary-box--tracking">
                  <div class="ai-summary-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
                    </svg>
                  </div>
                  <div class="ai-summary-content">
                    <h3>{{ trackingData.learningPath.currentStage }}</h3>
                    <p>下一阶段：{{ trackingData.learningPath.nextStage }}</p>
                    <div class="stats-row">
                      <div class="stat-item">
                        <span class="stat-value">{{ trackingData.learningPath.daysActive }}</span>
                        <span class="stat-label">活跃天数</span>
                      </div>
                      <div class="stat-item">
                        <span class="stat-value">{{ trackingData.learningPath.streakDays }}</span>
                        <span class="stat-label">连续打卡</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 本周统计 -->
                <div class="ai-section">
                  <div class="ai-section-header">
                    <div class="ai-section-icon stats">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                      </svg>
                    </div>
                    <h4>本周学习统计</h4>
                  </div>
                  <div class="ai-section-body">
                    <div class="weekly-stats-grid">
                      <div class="weekly-stat-card">
                        <span class="weekly-stat-value">{{ trackingData.weeklyStats.problemsSolved }}</span>
                        <span class="weekly-stat-label">解题数量</span>
                      </div>
                      <div class="weekly-stat-card">
                        <span class="weekly-stat-value">{{ trackingData.weeklyStats.accuracy }}%</span>
                        <span class="weekly-stat-label">正确率</span>
                      </div>
                      <div class="weekly-stat-card highlight">
                        <span class="weekly-stat-value">{{ trackingData.weeklyStats.improvement }}</span>
                        <span class="weekly-stat-label">较上周</span>
                      </div>
                    </div>
                    <div class="weak-topics-section">
                      <span class="weak-topics-label">需加强：</span>
                      <div class="weak-topics-tags">
                        <span v-for="(topic, index) in trackingData.weeklyStats.weakTopics" :key="index" class="weak-topic-tag">
                          {{ topic }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 近期错题追踪 -->
                <div class="ai-section">
                  <div class="ai-section-header">
                    <div class="ai-section-icon history">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                      </svg>
                    </div>
                    <h4>近期错题追踪</h4>
                  </div>
                  <div class="ai-section-body">
                    <div class="recent-mistakes-list">
                      <div v-for="mistake in trackingData.recentMistakes" :key="mistake.id" class="mistake-item">
                        <div class="mistake-status" :class="{ resolved: mistake.isResolved }">
                          <svg v-if="mistake.isResolved" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M5 13l4 4L19 7"/>
                          </svg>
                          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"/>
                            <path d="M12 6v6l4 2"/>
                          </svg>
                        </div>
                        <div class="mistake-info">
                          <h5 class="mistake-title">{{ mistake.title }}</h5>
                          <p class="mistake-type">{{ mistake.mistakeType }}</p>
                        </div>
                        <div class="mistake-meta">
                          <span class="review-count">已复习 {{ mistake.reviewCount }} 次</span>
                          <span class="mistake-date">{{ mistake.date }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 智能推荐（联动同类型强化） -->
                <div class="ai-section">
                  <div class="ai-section-header">
                    <div class="ai-section-icon smart">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
                      </svg>
                    </div>
                    <h4>智能推荐</h4>
                  </div>
                  <div class="ai-section-body">
                    <div class="adaptive-recommendations">
                      <div v-for="rec in trackingData.adaptiveRecommendations" :key="rec.id" class="adaptive-rec-card">
                        <div class="rec-header">
                          <h5 class="rec-title">{{ rec.title }}</h5>
                          <span class="rec-priority" :class="rec.priority">{{ getPriorityLabel(rec.priority) }}</span>
                        </div>
                        <p class="rec-reason">{{ rec.reason }}</p>
                        <div class="linked-problems">
                          <span class="linked-label">关联强化题目：</span>
                          <div class="linked-tags">
                            <span v-for="problem in getLinkedReinforcementProblems(rec.linkedReinforcement)" :key="problem.id" class="linked-tag">
                              {{ problem.title }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部 -->
            <div class="ai-footer">
              <button class="ai-btn-secondary" @click="visible = false">关闭</button>
              <button class="ai-btn-refresh" @click="emit('refresh')" :disabled="loading">
                <svg v-if="!loading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
                </svg>
                <span v-else class="ai-spinner-small"></span>
                {{ loading ? '分析中...' : '重新分析' }}
              </button>
              <button class="ai-btn-primary" @click="activeTab = 'knowledge'" v-if="activeTab === 'analysis' && hasKnowledgeContent">
                &#x67E5;&#x770B;&#x77E5;&#x8BC6;&#x70B9;
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              </button>
              <button class="ai-btn-primary" @click="activeTab = 'suggestions'" v-if="activeTab === 'analysis' && parsedData.suggestions.length > 0">
                查看建议
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              </button>
              <button class="ai-btn-primary" @click="activeTab = 'analysis'" v-if="activeTab === 'knowledge'">
                &#x8FD4;&#x56DE;&#x5206;&#x6790;
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M19 12H5M12 19l-7-7 7-7"/>
                </svg>
              </button>
              <button class="ai-btn-primary" @click="activeTab = 'analysis'" v-else-if="activeTab === 'suggestions'">
                返回分析
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M19 12H5M12 19l-7-7 7-7"/>
                </svg>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ================================
   AI 智能分析对话框 - 全新设计
   玻璃拟态 + 渐变 + 动画
   ================================ */

/* 遮罩层 */
.ai-analysis-modal {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
}

/* 对话框卡片 */
.ai-analysis-card {
  width: 100%;
  max-width: 640px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 24px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  overflow: hidden;
}

/* 头部 */
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.ai-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.3);
}

.ai-icon svg {
  width: 24px;
  height: 24px;
}

.ai-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ai-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.ai-subtitle {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.ai-close-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ai-close-btn:hover {
  background: rgba(226, 232, 240, 0.8);
  color: #0f172a;
  transform: rotate(90deg);
}

.ai-close-btn svg {
  width: 20px;
  height: 20px;
}

/* 加载状态 */
.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  gap: 20px;
}

.ai-loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(79, 70, 229, 0.1);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-loading p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
}

/* 内容区域 */
.ai-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.5) transparent;
}

.ai-content::-webkit-scrollbar {
  width: 6px;
}

.ai-content::-webkit-scrollbar-track {
  background: transparent;
}

.ai-content::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.5);
  border-radius: 3px;
}

/* 标签页 */
.ai-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 6px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.ai-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  background: transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ai-tab svg {
  width: 18px;
  height: 18px;
}

.ai-tab:hover {
  color: #475569;
  background: rgba(255, 255, 255, 0.6);
}

.ai-tab.active {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  font-weight: 600;
}

/* 面板 */
.ai-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 总结卡片 */
.ai-summary-box {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.ai-summary-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  color: #f59e0b;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.25);
}

.ai-summary-icon svg {
  width: 22px;
  height: 22px;
}

.ai-summary-content h3 {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
}

.ai-summary-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #78350f;
}

/* 内容区块 */
.ai-section {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.ai-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.ai-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.ai-section-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.ai-section-icon svg {
  width: 18px;
  height: 18px;
}

.ai-section-icon.error {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #ef4444;
}

.ai-section-icon.info {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #3b82f6;
}

.ai-section-icon.knowledge {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  color: #22c55e;
}

.ai-section-icon.topic {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  color: #f97316;
}

.ai-section-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.ai-section-body p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #475569;
}

/* 知识点标签 */
.knowledge-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.knowledge-tag {
  display: inline-flex;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  color: #166534;
  border-radius: 24px;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #bbf7d0;
  transition: all 0.3s ease;
}

.knowledge-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(34, 197, 94, 0.25);
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
}

.knowledge-tags.secondary {
  gap: 12px;
}

.knowledge-tag.related {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-color: #fdba74;
  color: #9a3412;
}

.ai-summary-box--knowledge {
  background: linear-gradient(135deg, #ecfeff 0%, #cffafe 100%);
  border-color: rgba(34, 211, 238, 0.28);
}

.ai-summary-box--knowledge .ai-summary-icon {
  color: #0891b2;
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.18);
}

.ai-summary-box--knowledge .ai-summary-content h3 {
  color: #155e75;
}

.ai-summary-box--knowledge .ai-summary-content p {
  color: #164e63;
}

.ai-knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.ai-knowledge-card {
  padding: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 250, 252, 0.92) 100%);
  border: 1px solid rgba(186, 230, 253, 0.9);
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(14, 116, 144, 0.08);
}

.ai-knowledge-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-knowledge-card-head h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.ai-knowledge-index {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #0891b2 0%, #06b6d4 100%);
  color: white;
  font-size: 13px;
  font-weight: 700;
}

.ai-knowledge-description {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: #475569;
}

.ai-section-body--stack {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-section-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #475569;
}

/* 建议列表 */
.ai-suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-suggestion-item {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.ai-suggestion-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.ai-suggestion-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.ai-suggestion-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
}

.ai-suggestion-header h4 {
  flex: 1;
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.ai-priority-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.ai-suggestion-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
}

/* 空状态 */
.ai-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  color: #94a3b8;
}

.ai-empty svg {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.ai-empty p {
  margin: 0;
  font-size: 14px;
}

/* 底部 */
.ai-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 28px;
  background: linear-gradient(180deg, #fafbfc 0%, #f8fafc 100%);
  border-top: 1px solid rgba(226, 232, 240, 0.8);
}

.ai-btn-secondary {
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ai-btn-secondary:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.ai-btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.ai-btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
}

.ai-btn-primary svg {
  width: 16px;
  height: 16px;
}

/* 重新分析按钮 */
.ai-btn-refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ai-btn-refresh:hover:not(:disabled) {
  border-color: #4f46e5;
  color: #4f46e5;
  background: #f5f3ff;
}

.ai-btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-btn-refresh svg {
  width: 16px;
  height: 16px;
}

.ai-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(79, 70, 229, 0.2);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 过渡动画 */
.dialog-backdrop-enter-active,
.dialog-backdrop-leave-active {
  transition: opacity 0.3s ease;
}

.dialog-backdrop-enter-from,
.dialog-backdrop-leave-to {
  opacity: 0;
}

.dialog-scale-enter-active,
.dialog-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dialog-scale-enter-from,
.dialog-scale-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

/* 响应式 */
@media (max-width: 640px) {
  .ai-analysis-modal {
    padding: 16px;
  }
  
  .ai-analysis-card {
    max-height: 90vh;
  }
  
  .ai-header {
    padding: 20px;
  }
  
  .ai-icon {
    width: 40px;
    height: 40px;
  }
  
  .ai-title {
    font-size: 18px;
  }
  
  .ai-content {
    padding: 20px;
  }
  
  .ai-tabs {
    flex-wrap: wrap;
  }
  
  .ai-tab {
    flex: 1;
    justify-content: center;
    padding: 10px 16px;
  }
  
  .ai-summary-box {
    flex-direction: column;
    text-align: center;
  }
  
  .ai-footer {
    padding: 16px 20px;
    flex-direction: column;
  }
  
  .ai-btn-secondary,
  .ai-btn-primary {
    width: 100%;
    justify-content: center;
  }
}

/* ================================
   同类型强化 & 持续追踪 样式
   ================================ */

/* 同类型强化 - 总结卡片 */
.ai-summary-box--reinforcement {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-color: rgba(251, 191, 36, 0.3);
}

.ai-summary-box--reinforcement .ai-summary-icon {
  color: #d97706;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.25);
}

.ai-summary-box--reinforcement .ai-summary-content h3 {
  color: #92400e;
}

.ai-summary-box--reinforcement .ai-summary-content p {
  color: #78350f;
}

/* 进度条 */
.progress-bar {
  margin-top: 12px;
  height: 8px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b, #d97706);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-text {
  position: absolute;
  right: 0;
  top: -20px;
  font-size: 12px;
  color: #92400e;
  font-weight: 600;
}

/* 薄弱环节 */
.ai-section-icon.weak {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #ef4444;
}

.ai-section-icon.practice {
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  color: #22c55e;
}

.weak-points-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.weak-point-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.weak-point-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.weak-point-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  min-width: 100px;
}

.mastery-bar {
  flex: 1;
  height: 6px;
  background: rgba(226, 232, 240, 0.8);
  border-radius: 3px;
  overflow: hidden;
}

.mastery-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
}

.mastery-text {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  min-width: 36px;
  text-align: right;
}

.weak-point-suggestion {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  padding-left: 112px;
}

/* 相似题目卡片 */
.similar-problems-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.similar-problem-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.3s ease;
}

.similar-problem-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.similar-problem-card.recommended {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 20%, rgba(255, 255, 255, 0.6) 100%);
  border-color: rgba(251, 191, 36, 0.4);
}

.problem-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.problem-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.recommend-badge {
  padding: 4px 10px;
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.problem-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.difficulty-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.problem-type {
  font-size: 12px;
  color: #64748b;
  padding: 4px 10px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 12px;
}

.relevance-score {
  font-size: 12px;
  color: #0891b2;
  font-weight: 600;
}

.problem-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.estimated-time {
  font-size: 12px;
  color: #64748b;
}

.practice-btn {
  padding: 6px 16px;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.practice-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

/* ================================
   持续追踪 样式
   ================================ */

/* 持续追踪 - 总结卡片 */
.ai-summary-box--tracking {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  border-color: rgba(139, 92, 246, 0.3);
}

.ai-summary-box--tracking .ai-summary-icon {
  color: #7c3aed;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.25);
}

.ai-summary-box--tracking .ai-summary-content h3 {
  color: #5b21b6;
}

.ai-summary-box--tracking .ai-summary-content p {
  color: #6d28d9;
}

.stats-row {
  display: flex;
  gap: 24px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #5b21b6;
}

.stat-label {
  font-size: 12px;
  color: #7c3aed;
}

/* 统计图标 */
.ai-section-icon.stats {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #3b82f6;
}

.ai-section-icon.history {
  background: linear-gradient(135deg, #f5f3ff 0%, #ede9fe 100%);
  color: #8b5cf6;
}

.ai-section-icon.smart {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

/* 本周统计网格 */
.weekly-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.weekly-stat-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  text-align: center;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.weekly-stat-card.highlight {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  border-color: rgba(34, 197, 94, 0.3);
}

.weekly-stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.weekly-stat-card.highlight .weekly-stat-value {
  color: #166534;
}

.weekly-stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

/* 薄弱主题标签 */
.weak-topics-section {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.6);
}

.weak-topics-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.weak-topics-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.weak-topic-tag {
  padding: 6px 12px;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #991b1b;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #fecaca;
}

/* 近期错题列表 */
.recent-mistakes-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mistake-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.mistake-status {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #fef3c7;
  color: #d97706;
  flex-shrink: 0;
}

.mistake-status.resolved {
  background: #dcfce7;
  color: #22c55e;
}

.mistake-status svg {
  width: 16px;
  height: 16px;
}

.mistake-info {
  flex: 1;
}

.mistake-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.mistake-type {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.mistake-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.review-count {
  font-size: 11px;
  color: #0891b2;
  font-weight: 600;
  padding: 2px 8px;
  background: rgba(8, 145, 178, 0.1);
  border-radius: 10px;
}

.mistake-date {
  font-size: 11px;
  color: #94a3b8;
}

/* 智能推荐卡片 */
.adaptive-recommendations {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.adaptive-rec-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-left: 4px solid #8b5cf6;
}

.rec-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.rec-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.rec-priority {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.rec-priority.high {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.rec-priority.medium {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.rec-reason {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #64748b;
}

.linked-problems {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.linked-label {
  font-size: 12px;
  color: #64748b;
}

.linked-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.linked-tag {
  padding: 4px 10px;
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  color: #5b21b6;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.linked-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.2);
}

/* ================================
   缓冲加载样式
   ================================ */

.ai-buffer-loading {
  gap: 16px;
}

.ai-buffer-loading .ai-loading-spinner {
  width: 56px;
  height: 56px;
  border-width: 4px;
}

.buffer-progress {
  width: 200px;
  height: 4px;
  background: rgba(226, 232, 240, 0.6);
  border-radius: 2px;
  overflow: hidden;
  position: relative;
}

.buffer-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5, #7c3aed, #4f46e5);
  background-size: 200% 100%;
  border-radius: 2px;
  animation: bufferProgress 5s ease-in-out forwards,
             shimmer 1.5s ease-in-out infinite;
}

@keyframes bufferProgress {
  0% {
    width: 0%;
  }
  100% {
    width: 100%;
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.buffer-hint {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
}

/* 内容区域淡入动画 */
.ai-content {
  animation: contentFadeIn 0.5s ease;
}

@keyframes contentFadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 640px) {
  .weekly-stats-grid {
    grid-template-columns: 1fr;
  }
  
  .weak-point-header {
    flex-wrap: wrap;
  }
  
  .weak-point-suggestion {
    padding-left: 0;
    margin-top: 8px;
  }
  
  .problem-meta {
    flex-wrap: wrap;
  }
  
  .stats-row {
    gap: 16px;
  }
  
  .mistake-item {
    flex-wrap: wrap;
  }
  
  .mistake-meta {
    flex-direction: row;
    width: 100%;
    justify-content: space-between;
    margin-top: 8px;
  }
  
  .buffer-progress {
    width: 160px;
  }
}
</style>
