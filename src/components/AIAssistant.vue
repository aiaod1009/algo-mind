<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { withNonBlockingAuth } from '../api/requestOptions'
import errorAnalysisIcon from '../assets/mini_icons/错题分析.png'
import studyPlanIcon from '../assets/mini_icons/学习计划.png'
import startChatIcon from '../assets/mini_icons/开始对话.png'

const userStore = useUserStore()

const emit = defineEmits(['planGenerated', 'planLoaded'])

const props = defineProps({
  selectedTrack: {
    type: String,
    default: 'algo'
  },
  weeklyGoal: {
    type: Number,
    default: 10
  },
  trackOptions: {
    type: Array,
    default: () => [
      { label: '算法思维赛道', value: 'algo' },
      { label: '数据结构赛道', value: 'ds' },
      { label: '竞赛冲刺赛道', value: 'contest' }
    ]
  }
})

const activeSection = ref('overview')
const isJumping = ref(false)
const chatMessages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const isGenerating = ref(false)
const isGenerated = ref(false)
const chatContainerRef = ref(null)
const textareaRef = ref(null)

// 多模态文件上传相关
const fileInputRef = ref(null)
const selectedFiles = ref([])
const isUploading = ref(false)
const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
const ALLOWED_FILE_TYPES = [
  ...ALLOWED_IMAGE_TYPES,
  'text/plain',
  'text/markdown',
  'text/csv',
  'application/json',
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'application/vnd.ms-powerpoint',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  'application/zip',
  'application/x-zip-compressed'
]

const errorAnalysis = ref({
  totalErrors: 0,
  categories: [],
  recentErrors: [],
  improvementTrend: '',
})

const studyHabits = ref({
  weeklyStudyTime: 0,
  averageTimePerQuestion: 0,
  preferredTimeSlot: '',
  consistencyScore: 0,
  strongTopics: [],
  weakTopics: [],
})

const learningPlan = ref({
  weekGoals: [],
  dailyTasks: [],
  recommendations: [],
  generatedAt: null,
})

const currentTrackLabel = computed(
  () => props.trackOptions.find((item) => item.value === props.selectedTrack)?.label || '算法思维赛道',
)

const quickPrompts = [
  { text: '分析我最近的错题', icon: '🔍' },
  { text: '帮我制定学习计划', icon: '📋' },
  { text: '推荐练习题目', icon: '💡' },
  { text: '解答我的疑问', icon: '❓' },
]

const typeDisplayMap = {
  'practice': '练习',
  'review': '复习',
  'learn': '学习',
  'single': '单项',
}

const getTaskTypeLabel = (type) => {
  return typeDisplayMap[type] || type
}

const taskTypeColors = {
  'practice': { bg: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }, // Red
  'review': { bg: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }, // Blue
  'learn': { bg: 'rgba(16, 185, 129, 0.1)', color: '#10b981' },  // Green
  // fallback for old chinese ones
  '数组': { bg: 'rgba(37, 99, 235, 0.1)', color: '#2563eb' },
  '字符串': { bg: 'rgba(22, 163, 74, 0.1)', color: '#16a34a' },
  '动态规划': { bg: 'rgba(217, 119, 6, 0.1)', color: '#d97706' },
  '二叉树': { bg: 'rgba(124, 58, 237, 0.1)', color: '#7c3aed' },
  '图论': { bg: 'rgba(220, 38, 38, 0.1)', color: '#dc2626' },
  '排序': { bg: 'rgba(8, 145, 178, 0.1)', color: '#0891b2' },
  '贪心': { bg: 'rgba(219, 39, 119, 0.1)', color: '#db2777' },
  '复盘': { bg: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' },
}

const getDayMarkerStyle = (day, index) => {
  return {
    backgroundColor: '#1cb0f6', // 统一的蓝色背景
  }
}

const unknownTaskColors = [
  { bg: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }, // Red
  { bg: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }, // Blue
  { bg: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }, // Green
  { bg: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' }, // Amber
]

const getTaskTypeStyle = (type) => {
  if (taskTypeColors[type]) {
    return {
      backgroundColor: taskTypeColors[type].bg,
      color: taskTypeColors[type].color,
    }
  }
  // 未知类型随机但固定分配一个好看的颜色
  let hash = 0
  const str = String(type)
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  const colorObj = unknownTaskColors[Math.abs(hash) % unknownTaskColors.length]
  return {
    backgroundColor: colorObj.bg,
    color: colorObj.color,
  }
}

const formatDuration = (val) => {
  if (!val) return '1h'
  const str = String(val)
  if (str.includes('h') || str.includes('m') || str.includes('分钟') || str.includes('小时')) {
    return str
  }
  return str + 'h'
}

const normalizeMessageContent = (content) => {
  if (typeof content === 'string') {
    return content
  }

  if (content === null || content === undefined) {
    return 'AI did not return any content. Please try again later.'
  }

  if (typeof content === 'object') {
    try {
      return JSON.stringify(content, null, 2)
    } catch (error) {
      console.warn('Failed to serialize AI message content, falling back to string output.', error)
    }
  }

  return String(content)
}

const escapeHtml = (content) => normalizeMessageContent(content)
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

const renderMessageContent = (content) => {
  if (!content) return ''
  
  // 1. 先提取代码块，避免内部内容被处理
  const codeBlocks = []
  let html = escapeHtml(content).replace(/```(?:([\w-]+)\n)?([\s\S]*?)```/g, (match, language, code) => {
    const placeholder = `__CODE_BLOCK_${codeBlocks.length}__`
    codeBlocks.push({
      language: language || '',
      code: code.replace(/\n$/, ''),
    })
    return placeholder
  })

  // 2. 处理行内格式（粗体、行内代码）
  const processInline = (text) => {
    return text
      .replace(/`([^`\n]+)`/g, '<code class="ai-code">$1</code>')
      .replace(/\*\*(.*?)\*\*/g, '<strong class="ai-highlight">$1</strong>')
  }

  // 3. 按行解析，构建结构化内容
  const lines = html.split('\n')
  const result = []
  let currentParagraph = []

  const flushParagraph = () => {
    if (currentParagraph.length > 0) {
      const text = currentParagraph.join(' ').trim()
      if (text) {
        result.push(`<p class="ai-paragraph">${processInline(text)}</p>`)
      }
      currentParagraph = []
    }
  }

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()

    // 空行：结束当前段落
    if (!line) {
      flushParagraph()
      continue
    }

    // 代码块占位符
    if (/^__CODE_BLOCK_\d+__$/.test(line)) {
      flushParagraph()
      result.push(line)
      continue
    }

    // 分隔线
    if (line === '---') {
      flushParagraph()
      result.push('<div class="ai-divider"></div>')
      continue
    }

    // ### 标题
    const h3Match = line.match(/^###\s*(.+)$/)
    if (h3Match) {
      flushParagraph()
      result.push(`<div class="ai-section-bubble ai-section-highlight"><span class="ai-section-icon">✦</span><span class="ai-section-title">${processInline(h3Match[1])}</span></div>`)
      continue
    }

    // 数字标题（如 1. 2. 标题）
    const numMatch = line.match(/^(\d+)\.\s*(.+)$/)
    if (numMatch) {
      flushParagraph()
      result.push(`<div class="ai-section-bubble"><span class="ai-section-num">${numMatch[1]}</span><span class="ai-section-title">${processInline(numMatch[2])}</span></div>`)
      continue
    }

    // - 列表项
    const listMatch = line.match(/^[-*]\s*(.+)$/)
    if (listMatch) {
      flushParagraph()
      result.push(`<div class="ai-point"><span class="ai-point-dot"></span><span class="ai-point-text">${processInline(listMatch[1])}</span></div>`)
      continue
    }

    // 普通文本，加入当前段落
    currentParagraph.push(line)
  }

  // 处理最后剩余的段落
  flushParagraph()

  // 4. 恢复代码块
  html = result.join('')
  codeBlocks.forEach((item, index) => {
    const languageBadge = item.language
      ? `<div class="ai-code-header"><span class="ai-code-lang">${item.language}</span></div>`
      : ''
    html = html.replace(
      `__CODE_BLOCK_${index}__`,
      `<div class="ai-code-block">${languageBadge}<pre><code>${item.code}</code></pre></div>`
    )
  })

  return html
}

const extractAIResponseContent = (response, fallbackInput) => {
  const content = response?.data?.data?.content
  return normalizeMessageContent(content || fallbackInput)
}

/*
const buildFallbackErrorAnalysis = () => ({
  totalErrors: 23,
  categories: [
    { name: '鍔ㄦ€佽鍒?, count: 8 },
    { name: '鍥捐', count: 6 },
    { name: '浜屽弶鏍?, count: 5 },
    { name: '瀛楃涓?, count: 4 },
  ],
  recentErrors: [
    { title: '鑳屽寘闂', reason: '鐘舵€佽浆绉绘柟绋嬮敊璇?, time: '2灏忔椂鍓? },
    { title: '鏈€鐭矾寰?, reason: '杈圭晫鏉′欢閬楁紡', time: '鏄ㄥぉ' },
    { title: '浜屽弶鏍戦亶鍘?, reason: '閫掑綊缁堟鏉′欢閿欒', time: '2澶╁墠' },
  ],
  improvementTrend: '杩戞湡鍦ㄥ姩鎬佽鍒掓柟闈㈡湁杩涙锛屼絾鍥捐浠嶉渶鍔犲己',
})

const buildFallbackStudyHabits = () => ({
  weeklyStudyTime: 12.5,
  averageTimePerQuestion: 25,
  preferredTimeSlot: '鏅氫笂',
  consistencyScore: 78,
  strongTopics: ['鏁扮粍', '瀛楃涓?, '閾捐〃'],
  weakTopics: ['鍔ㄦ€佽鍒?, '鍥捐'],
})

const formatRelativeTime = (value) => {
  if (!value) return '鍒氬垰'

  const target = new Date(value)
  if (Number.isNaN(target.getTime())) {
    return '鍒氬垰'
  }

  const diffMs = Date.now() - target.getTime()
  const diffMinutes = Math.max(1, Math.floor(diffMs / 60000))
  if (diffMinutes < 60) return `${diffMinutes}鍒嗛挓鍓?`

  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}灏忔椂鍓?`

  const diffDays = Math.floor(diffHours / 24)
  return `${diffDays}澶╁墠`
}

const buildErrorAnalysisFromItems = (items = []) => {
  if (!Array.isArray(items) || items.length === 0) {
    return buildFallbackErrorAnalysis()
  }

  const categoryCounts = new Map()
  items.forEach((item) => {
    const category = item?.levelType?.trim() || '鏈垎绫?'
    categoryCounts.set(category, (categoryCounts.get(category) || 0) + 1)
  })

  const categories = Array.from(categoryCounts.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)

  const recentErrors = items.slice(0, 3).map((item) => ({
    title: item?.title || item?.question || '鏈懡鍚嶉敊棰?,
    reason: item?.description || item?.analysisStatus || '闇€瑕佸洖椤惧師棰?,
    time: formatRelativeTime(item?.updatedAt || item?.createdAt),
  }))

  const topCategory = categories[0]?.name || '鍩虹棰樺瀷'
  return {
    totalErrors: items.length,
    categories,
    recentErrors,
    improvementTrend: `褰撳墠閿欓涓昏闆嗕腑鍦?${topCategory}锛屽缓璁噸鐐瑰洖椤俱€?`,
  }
}

*/

const buildFallbackErrorAnalysis = () => ({
  totalErrors: 23,
  categories: [
    { name: '动态规划', count: 8 },
    { name: '图论', count: 6 },
    { name: '二叉树', count: 5 },
    { name: '字符串', count: 4 },
  ],
  recentErrors: [
    { title: '背包问题', reason: '状态转移方程需要复习', time: '2小时前' },
    { title: '最短路径', reason: '边界条件处理遗漏', time: '昨天' },
    { title: '树的遍历', reason: '递归终止条件错误', time: '2天前' },
  ],
  improvementTrend: '动态规划正在进步，但图论部分仍需加强',
})

const buildFallbackStudyHabits = () => ({
  weeklyStudyTime: 12.5,
  averageTimePerQuestion: 25,
  preferredTimeSlot: '晚上',
  consistencyScore: 78,
  strongTopics: ['数组', '字符串', '链表'],
  weakTopics: ['动态规划', '图论'],
})

const formatRelativeTime = (value) => {
  if (!value) return '刚刚'

  const target = new Date(value)
  if (Number.isNaN(target.getTime())) {
    return '刚刚'
  }

  const diffMs = Date.now() - target.getTime()
  const diffMinutes = Math.max(1, Math.floor(diffMs / 60000))
  if (diffMinutes < 60) return `${diffMinutes}分钟前`

  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}小时前`

  const diffDays = Math.floor(diffHours / 24)
  if (diffDays === 1) return '昨天'
  if (diffDays === 2) return '前天'
  return `${diffDays}天前`
}

const buildErrorAnalysisFromItems = (items = []) => {
  if (!Array.isArray(items) || items.length === 0) {
    return buildFallbackErrorAnalysis()
  }

  const categoryCounts = new Map()
  items.forEach((item) => {
    const category = item?.levelType?.trim() || 'Uncategorized'
    categoryCounts.set(category, (categoryCounts.get(category) || 0) + 1)
  })

  const categories = Array.from(categoryCounts.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)

  const recentErrors = items.slice(0, 3).map((item) => ({
    title: item?.title || item?.question || 'Untitled Error',
    reason: item?.description || item?.analysisStatus || 'Review the original problem statement.',
    time: formatRelativeTime(item?.updatedAt || item?.createdAt),
  }))

  const topCategory = categories[0]?.name || 'Core Topics'
  return {
    totalErrors: items.length,
    categories,
    recentErrors,
    improvementTrend: `Current mistakes are mostly concentrated in ${topCategory}. Focus there first.`,
  }
}

const buildStudyHabitsFromStats = (stats = {}) => {
  const fallback = buildFallbackStudyHabits()
  const accuracy = Number(stats?.accuracy || 0)
  const currentStreak = Number(stats?.currentStreak || 0)
  const weeklyProgress = Number(stats?.weeklyProgress || 0)

  // 使用后端返回的强弱项数据
  const strongTopics = stats?.strongTopics?.length ? stats.strongTopics : fallback.strongTopics
  const weakTopics = stats?.weakTopics?.length ? stats.weakTopics : fallback.weakTopics

  return {
    ...fallback,
    weeklyStudyTime: Math.max(fallback.weeklyStudyTime, Number((weeklyProgress * 1.5).toFixed(1))),
    consistencyScore: Math.min(100, Math.max(0, Math.round(accuracy * 0.7 + currentStreak * 3))),
    strongTopics,
    weakTopics,
  }
}

const loadErrorAnalysis = async () => {
  if (!userStore.isLogin) {
    errorAnalysis.value = buildFallbackErrorAnalysis()
    return
  }

  try {
    const response = await api.get('/errors', withNonBlockingAuth())
    errorAnalysis.value = buildErrorAnalysisFromItems(response.data?.data)
  } catch (error) {
    console.warn('获取错误分析失败:', error)
    errorAnalysis.value = buildFallbackErrorAnalysis()
  }
}

const loadStudyHabits = async () => {
  if (!userStore.isLogin) {
    studyHabits.value = buildFallbackStudyHabits()
    return
  }

  try {
    const response = await api.get('/users/me/stats', withNonBlockingAuth())
    studyHabits.value = buildStudyHabitsFromStats(response.data?.data)
  } catch (error) {
    console.warn('获取学习习惯数据失败:', error)
    studyHabits.value = buildFallbackStudyHabits()
  }
}

// 检查是否已有当前赛道的学习计划
const checkExistingPlan = async () => {
  if (!userStore.isLogin) return false

  try {
    const response = await api.getCurrentLearningPlan(props.selectedTrack)
    if (response.data?.code === 0 && response.data?.data) {
      const planData = response.data.data
      // 检查是否有有效的每日任务数据
      if (planData.dailyTasks && planData.dailyTasks.length > 0) {
        learningPlan.value = {
          weekGoals: planData.weekGoals || [],
          dailyTasks: planData.dailyTasks || [],
          recommendations: planData.recommendations || [],
          generatedAt: planData.generatedAt,
        }
        isGenerated.value = true
        return true
      }
    }
  } catch (error) {
    console.warn('获取已有学习计划失败:', error)
  }
  return false
}

const showConfirmDialog = ref(false)
let confirmDialogResolve = null

const requestGenerateConfirm = () => {
  showConfirmDialog.value = true
  return new Promise((resolve, reject) => {
    confirmDialogResolve = { resolve, reject }
  })
}

const handleConfirmGenerate = () => {
  showConfirmDialog.value = false
  if (confirmDialogResolve) {
    confirmDialogResolve.resolve()
    confirmDialogResolve = null
  }
}

const handleConfirmCancel = () => {
  showConfirmDialog.value = false
  if (confirmDialogResolve) {
    confirmDialogResolve.reject(new Error('cancel'))
    confirmDialogResolve = null
  }
}

const generateLearningPlan = async (forceRegenerate = false) => {
  if (isGenerating.value) return

  // 如果不是强制重新生成，先检查是否已有计划
  if (!forceRegenerate) {
    const hasExistingPlan = await checkExistingPlan()
    if (hasExistingPlan) {
      ElMessage.info({
        message: '已加载保存的学习计划',
        duration: 2000,
        offset: 80,
      })
      return
    }
  }

  try {
    await requestGenerateConfirm()
  } catch (error) {
    return
  }

  isGenerating.value = true
  isGenerated.value = false

  try {
    if (!userStore.isLogin) {
      throw new Error('Login required for remote learning plan generation.')
    }

    const response = await api.generateLearningPlan({
      track: props.selectedTrack,
      trackLabel: props.trackOptions.find(t => t.value === props.selectedTrack)?.label || '算法思维赛道',
      weeklyGoal: props.weeklyGoal,
      weakAreas: studyHabits.value.weakTopics,
      strongAreas: studyHabits.value.strongTopics,
      errorTopics: errorAnalysis.value.categories.map((category) => category.name),
    })

    if (response.data?.data) {
      learningPlan.value = response.data.data
      isGenerated.value = true
      ElMessage.success({
        message: '学习计划已生成',
        duration: 3000,
        offset: 80,
      })

      // 触发自定义事件，通知父组件学习计划已更新
      emit('planGenerated', learningPlan.value)
    }
  } catch (error) {
    console.warn('生成学习计划失败:', error)
    learningPlan.value = {
      weekGoals: [
        { title: '基础巩固练习', target: 4, current: 0 },
        { title: '新知识点学习', target: 3, current: 0 },
        { title: '算法思维强化', target: 3, current: 0 },
        { title: '每日坚持打卡', target: 1, current: 0 },
      ],
      dailyTasks: [
        {
          day: '周一',
          tasks: [
            { type: '数组', title: '数组基础练习', duration: '1h' },
            { type: '字符串', title: '字符串处理', duration: '1.5h' },
            { type: '排序', title: '排序算法复习', duration: '1.5h' },
          ]
        },
        {
          day: '周二',
          tasks: [
            { type: '二叉树', title: '二叉树遍历', duration: '1.5h' },
            { type: '二叉树', title: 'BST与平衡树', duration: '1.5h' },
          ]
        },
        {
          day: '周三',
          tasks: [
            { type: '动态规划', title: 'DP基础入门', duration: '1h' },
            { type: '动态规划', title: '背包问题专题', duration: '1.5h' },
            { type: '动态规划', title: '线性DP练习', duration: '1.5h' },
          ]
        },
        {
          day: '周四',
          tasks: [
            { type: '图论', title: '图的表示与遍历', duration: '1.5h' },
            { type: '图论', title: '最短路径算法', duration: '1.5h' },
            { type: '贪心', title: '贪心算法入门', duration: '1.5h' },
          ]
        },
        {
          day: '周五',
          tasks: [
            { type: '数组', title: '双指针专题', duration: '1.5h' },
            { type: '字符串', title: '字符串匹配', duration: '1.5h' },
            { type: '动态规划', title: '区间DP练习', duration: '1.5h' },
          ]
        },
        {
          day: '周六',
          tasks: [
            { type: '复盘', title: '周赛模拟', duration: '2h' },
            { type: '二叉树', title: '树形DP专题', duration: '1.5h' },
            { type: '图论', title: '拓扑排序', duration: '1.5h' },
          ]
        },
        {
          day: '周日',
          tasks: [
            { type: '复盘', title: '本周知识点总结', duration: '2h' },
            { type: '排序', title: '高级排序算法', duration: '1.5h' },
            { type: '贪心', title: '贪心经典问题', duration: '1.5h' },
          ]
        },
      ],
      recommendations: [
        '建议每天保持1-2小时的算法练习时间',
        '重点关注动态规划和图论这两个薄弱环节',
        '可以尝试参加每周的算法竞赛来检验学习成果',
      ],
      generatedAt: new Date().toISOString(),
    }
    isGenerated.value = true
  } finally {
    isGenerating.value = false
  }
}

const saveLearningPlan = () => {
  alert('学习计划已保存！')
}

const switchSection = (section) => {
  activeSection.value = section
  if (section === 'errors' && errorAnalysis.value.totalErrors === 0) {
    loadErrorAnalysis()
  } else if (section === 'plan' && !isGenerated.value && !isGenerating.value) {
    // 切换到计划页面时，如果没有已生成的计划，则检查/加载已有计划或生成新计划
    generateLearningPlan()
  }
}

const getCurrentTimeLabel = () => new Date().toLocaleTimeString('zh-CN', {
  hour: '2-digit',
  minute: '2-digit',
})

const autoResizeTextarea = async () => {
  await nextTick()
  if (!textareaRef.value) {
    return
  }

  textareaRef.value.style.height = 'auto'
  textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 120)}px`
}

const buildChatHistory = () => chatMessages.value
  .filter((item) => item?.role === 'user' || item?.role === 'assistant')
  .slice(-8)
  .map((item) => ({
    role: item.role,
    content: normalizeMessageContent(item.content),
  }))

const buildChatContext = () => ({
  track: props.selectedTrack,
  trackLabel: currentTrackLabel.value,
  weeklyGoal: props.weeklyGoal,
  weakTopics: studyHabits.value.weakTopics,
  strongTopics: studyHabits.value.strongTopics,
  totalErrors: errorAnalysis.value.totalErrors,
  consistencyScore: studyHabits.value.consistencyScore,
})

const finalizeStreamingMessage = async (index, content) => {
  if (!chatMessages.value[index]) {
    return
  }

  chatMessages.value[index].content = normalizeMessageContent(content || '抱歉，我现在无法回答，请稍后再试。')
  chatMessages.value[index].streaming = false
  isTyping.value = false
  await scrollToBottom()
}

const handleInputKeyDown = async (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    await sendMessage()
    return
  }

  autoResizeTextarea()
}

const scrollToTop = () => {
  if (isJumping.value) return
  isJumping.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
  setTimeout(() => {
    isJumping.value = false
  }, 600)
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

let scrollToBottomScheduled = false
const scheduleScrollToBottom = () => {
  if (scrollToBottomScheduled) {
    return
  }

  scrollToBottomScheduled = true
  requestAnimationFrame(async () => {
    scrollToBottomScheduled = false
    await scrollToBottom()
  })
}

// 文件上传相关函数
const isImageFile = (file) => ALLOWED_IMAGE_TYPES.includes(file.type)

const getFileIcon = (fileType) => {
  if (ALLOWED_IMAGE_TYPES.includes(fileType)) return '🖼️'
  if (fileType.includes('pdf')) return '📄'
  if (fileType.includes('word') || fileType.includes('document')) return '📝'
  if (fileType.includes('excel') || fileType.includes('sheet')) return '📊'
  if (fileType.includes('powerpoint') || fileType.includes('presentation')) return '📽️'
  if (fileType.includes('zip') || fileType.includes('compressed')) return '📦'
  if (fileType.includes('json')) return '🔧'
  if (fileType.includes('csv')) return '📈'
  if (fileType.includes('text') || fileType.includes('markdown')) return '📃'
  return '📎'
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event) => {
  const files = Array.from(event.target.files || [])
  validateAndAddFiles(files)
  // 重置input，允许重复选择相同文件
  event.target.value = ''
}

const validateAndAddFiles = (files) => {
  for (const file of files) {
    // 检查文件大小
    if (file.size > MAX_FILE_SIZE) {
      ElMessage.warning(`文件 "${file.name}" 超过10MB限制`)
      continue
    }
    
    // 检查文件类型
    if (!ALLOWED_FILE_TYPES.includes(file.type)) {
      ElMessage.warning(`不支持的文件类型: ${file.name}`)
      continue
    }
    
    // 检查是否已存在
    const exists = selectedFiles.value.some(f => f.name === file.name && f.size === file.size)
    if (exists) {
      ElMessage.warning(`文件 "${file.name}" 已添加`)
      continue
    }
    
    // 限制最多5个文件
    if (selectedFiles.value.length >= 5) {
      ElMessage.warning('最多只能上传5个文件')
      break
    }
    
    // 创建预览URL
    const fileWithPreview = {
      file,
      name: file.name,
      size: file.size,
      type: file.type,
      previewUrl: isImageFile(file) ? URL.createObjectURL(file) : null,
      icon: getFileIcon(file.type)
    }
    
    selectedFiles.value.push(fileWithPreview)
  }
}

const removeFile = (index) => {
  const file = selectedFiles.value[index]
  if (file.previewUrl) {
    URL.revokeObjectURL(file.previewUrl)
  }
  selectedFiles.value.splice(index, 1)
}

const clearAllFiles = () => {
  selectedFiles.value.forEach(file => {
    if (file.previewUrl) {
      URL.revokeObjectURL(file.previewUrl)
    }
  })
  selectedFiles.value = []
}

// 上传文件到服务器
const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) return []
  
  isUploading.value = true
  const uploadedFiles = []
  
  try {
    for (const fileInfo of selectedFiles.value) {
      const formData = new FormData()
      formData.append('file', fileInfo.file)
      
      // 调用实际上传API
      const response = await api.uploadChatFile(formData)
      
      if (response.data?.code === 0 && response.data?.data) {
        uploadedFiles.push({
          name: fileInfo.name,
          size: fileInfo.size,
          type: fileInfo.type,
          url: response.data.data.url,
          isImage: isImageFile(fileInfo.file)
        })
      } else {
        throw new Error(response.data?.message || '上传失败')
      }
    }
  } catch (error) {
    ElMessage.error('文件上传失败，请重试')
    console.error('File upload error:', error)
    return []
  } finally {
    isUploading.value = false
  }
  
  return uploadedFiles
}

// 图片预览
const previewImageUrl = ref('')
const showImagePreview = ref(false)

const openImagePreview = (url) => {
  previewImageUrl.value = url
  showImagePreview.value = true
}

const closeImagePreview = () => {
  showImagePreview.value = false
  previewImageUrl.value = ''
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  const hasFiles = selectedFiles.value.length > 0
  
  // 如果没有文字且没有文件，不发送
  if ((!message && !hasFiles) || isTyping.value) return

  const historyMessages = buildChatHistory()
  
  // 上传文件
  let uploadedFiles = []
  if (hasFiles) {
    uploadedFiles = await uploadFiles()
    if (selectedFiles.value.length > 0 && uploadedFiles.length === 0) {
      // 上传失败，不继续发送消息
      return
    }
  }

  // 构建用户消息内容
  const userMessageContent = message || (hasFiles ? '请分析我上传的文件' : '')

  chatMessages.value.push({
    role: 'user',
    content: userMessageContent,
    files: uploadedFiles,
    time: getCurrentTimeLabel(),
  })

  inputMessage.value = ''
  clearAllFiles()
  isTyping.value = true
  await autoResizeTextarea()

  chatMessages.value.push({
    role: 'assistant',
    content: '',
    time: getCurrentTimeLabel(),
    streaming: true,
  })

  const assistantMessageIndex = chatMessages.value.length - 1
  let streamedContent = ''

  await scrollToBottom()

  try {
    // 如果有文件，使用多模态接口
    const hasUploadedFiles = uploadedFiles.length > 0
    
    if (hasUploadedFiles) {
      // 使用多模态流式接口
      await api.aiChatMultimodalStream({
        content: userMessageContent,
        files: uploadedFiles.map(f => ({
          name: f.name,
          size: f.size,
          type: f.type,
          url: f.url,
          image: f.isImage
        }))
      }, (chunk) => {
        streamedContent += chunk
        if (chatMessages.value[assistantMessageIndex]) {
          chatMessages.value[assistantMessageIndex].content = streamedContent
        }
        scheduleScrollToBottom()
      }, async () => {
        await finalizeStreamingMessage(assistantMessageIndex, streamedContent)
      }, async () => {
        // 流式失败，尝试非流式
        try {
          const response = await api.aiChatMultimodal({
            content: userMessageContent,
            files: uploadedFiles.map(f => ({
              name: f.name,
              size: f.size,
              type: f.type,
              url: f.url,
              image: f.isImage
            }))
          })
          await finalizeStreamingMessage(assistantMessageIndex, response.data || '抱歉，我现在无法回答，请稍后再试。')
        } catch (error) {
          await finalizeStreamingMessage(
            assistantMessageIndex,
            streamedContent || '抱歉，服务暂时不可用，请稍后再试。',
          )
        }
      })
    } else {
      // 无文件，使用WebSocket接口
      await api.aiChatWebSocket({
        message: userMessageContent,
        messages: historyMessages,
        context: buildChatContext(),
      }, (chunk) => {
        streamedContent += chunk
        if (chatMessages.value[assistantMessageIndex]) {
          chatMessages.value[assistantMessageIndex].content = streamedContent
        }
        scheduleScrollToBottom()
      }, async () => {
        await finalizeStreamingMessage(assistantMessageIndex, streamedContent)
      }, async () => {
        if (streamedContent) {
          await finalizeStreamingMessage(assistantMessageIndex, streamedContent)
          return
        }

        try {
          const response = await api.aiChat({
            message: userMessageContent,
            messages: historyMessages,
            context: buildChatContext(),
          })

          const fallbackContent = extractAIResponseContent(
            response,
            '抱歉，我现在无法回答，请稍后再试。',
          )
          await finalizeStreamingMessage(assistantMessageIndex, fallbackContent)
        } catch (error) {
          await finalizeStreamingMessage(
            assistantMessageIndex,
            streamedContent || '抱歉，服务暂时不可用，请稍后再试。',
          )
        }
      })
    }

    const aiContent = extractAIResponseContent(response, '抱歉，我现在无法回答，请稍后再试。')


  } catch (error) {
    console.warn('发送消息失败:', error)
    await finalizeStreamingMessage(assistantMessageIndex, '抱歉，服务暂时不可用，请稍后再试。')
  } finally {
    if (chatMessages.value[assistantMessageIndex]) {
      chatMessages.value[assistantMessageIndex].streaming = false
    }
    isTyping.value = false
    await scrollToBottom()
  }
}

const useQuickPrompt = (prompt) => {
  inputMessage.value = prompt.text
  sendMessage()
}

onMounted(async () => {
  await loadErrorAnalysis()
  await loadStudyHabits()

  chatMessages.value.push({
    role: 'assistant',
    content: `你好，${userStore.userInfo?.name || '同学'}！👋\n\n我是你的算法学习助手，可以帮你：\n- 📊 分析错题，找出薄弱点\n- 📋 制定个性化学习计划\n- 💡 推荐适合的练习题目\n\n当前赛道：**${props.trackOptions.find(t => t.value === props.selectedTrack)?.label || '算法思维赛道'}**\n周目标：**${props.weeklyGoal} 个关卡**\n\n有什么我可以帮你的吗？`,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
})

watch(() => userStore.isLogin, async (isLoggedIn) => {
  if (isLoggedIn) {
    await loadErrorAnalysis()
    await loadStudyHabits()
    return
  }

  errorAnalysis.value = buildFallbackErrorAnalysis()
  studyHabits.value = buildFallbackStudyHabits()
}, { immediate: false })

watch(activeSection, async (newSection) => {
  if (newSection === 'chat') {
    await autoResizeTextarea()
    await scrollToBottom()
  }
})

watch(inputMessage, () => {
  autoResizeTextarea()
})
</script>

<template>
  <section class="study-helper">
    <div class="helper-header" @click="scrollToTop" style="cursor: pointer;">
      <div class="header-left">
        <div class="helper-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
          </svg>
        </div>
        <div class="header-info">
          <h3>AI 学习助手</h3>
          <p>
            <span class="status-dot"></span>
            在线 · 陪你刷题
          </p>
        </div>
      </div>
      <div class="section-tabs">
        <button v-for="tab in [
          { key: 'overview', label: '总览' },
          { key: 'errors', label: '错题' },
          { key: 'plan', label: '计划' },
          { key: 'chat', label: '对话' },
        ]" :key="tab.key" class="tab _target" :class="{ active: activeSection === tab.key }"
          @click.stop="switchSection(tab.key)">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="helper-content">
      <!-- 总览 -->
      <div v-if="activeSection === 'overview'" class="overview-section">
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-icon errors">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ errorAnalysis.totalErrors }}</span>
              <span class="stat-label">累计错题</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon time">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ studyHabits.weeklyStudyTime }}h</span>
              <span class="stat-label">本周学习</span>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon score">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                <polyline points="22 4 12 14.01 9 11.01" />
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ studyHabits.consistencyScore }}</span>
              <span class="stat-label">坚持指数</span>
            </div>
          </div>
        </div>

        <div class="overview-grid">
          <div class="overview-card weak-card">
            <h4><img src="../assets/mini_icons/薄弱.png" alt="薄弱环节" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />薄弱环节</h4>
            <div class="topic-list">
              <div v-for="topic in studyHabits.weakTopics" :key="topic" class="topic-item weak">
                <span class="topic-dot"></span>
                {{ topic }}
              </div>
              <div v-if="!studyHabits.weakTopics.length" class="empty-hint">暂无数据</div>
            </div>
          </div>
          <div class="overview-card strong-card">
            <h4><img src="../assets/mini_icons/擅长.png" alt="擅长领域" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />擅长领域</h4>
            <div class="topic-list">
              <div v-for="topic in studyHabits.strongTopics" :key="topic" class="topic-item strong">
                <span class="topic-dot"></span>
                {{ topic }}
              </div>
              <div v-if="!studyHabits.strongTopics.length" class="empty-hint">暂无数据</div>
            </div>
          </div>
        </div>

        <div class="quick-actions">
          <h4><img src="../assets/mini_icons/快速开始.png" alt="快速开始" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />快速开始</h4>
          <div class="action-buttons">
            <button class="action-btn _target" @click.stop="switchSection('errors')">
              <span class="action-icon">📊</span>
              <span>查看错题分析</span>
            </button>
            <button class="action-btn _target" @click.stop="switchSection('plan')">
              <span class="action-icon">📋</span>
              <span>生成学习计划</span>
            </button>
            <button class="action-btn _target" @click.stop="switchSection('chat')">
              <span class="action-icon">💬</span>
              <span>开始对话</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 错题分析 -->
      <div v-if="activeSection === 'errors'" class="errors-section">
        <div class="section-title">
          <h3>错题分析</h3>
          <p>找出薄弱环节，针对性提升</p>
        </div>
        <div class="error-categories">
          <h4><img src="../assets/mini_icons/错题分析.png" alt="错误类型分布" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />错误类型分布</h4>
          <div class="category-bars">
            <div v-for="cat in errorAnalysis.categories" :key="cat.name" class="category-bar">
              <div class="bar-header">
                <span class="bar-label">{{ cat.name }}</span>
                <span class="bar-value">{{ cat.count }}次</span>
              </div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: `${(cat.count / errorAnalysis.totalErrors) * 100}%` }"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="recent-errors">
          <h4><img src="../assets/mini_icons/待复盘.png" alt="最近错题" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />最近错题</h4>
          <div class="error-list">
            <div v-for="error in errorAnalysis.recentErrors" :key="error.title" class="error-item">
              <div class="error-main">
                <span class="error-title">{{ error.title }}</span>
                <span class="error-reason">{{ error.reason }}</span>
              </div>
              <span class="error-time">{{ error.time }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 学习计划 -->
      <div v-if="activeSection === 'plan'" class="plan-section">
        <div class="section-title">
          <h3>学习计划</h3>
          <p>算法思维赛道 · 周目标 {{ weeklyGoal }} 关</p>
        </div>

        <div v-if="isGenerating" class="generating-overlay" @click.stop>
          <div class="generating-content">
            <div class="ai-brain">
              <div class="brain-core"></div>
              <div class="orbit orbit-1"></div>
              <div class="orbit orbit-2"></div>
              <div class="orbit orbit-3"></div>
            </div>
            <p class="generating-text">AI 正在为你制定专属学习计划...</p>
          </div>
        </div>

        <div v-else-if="isGenerated" class="plan-content">
          <div class="week-goals">
            <h4><img src="../assets/mini_icons/学习计划.png" alt="本周目标" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />本周目标</h4>
            <div class="goal-list">
              <div v-for="(goal, index) in learningPlan.weekGoals" :key="goal.title" class="goal-item">
                <div class="goal-check">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10" />
                  </svg>
                </div>
                <div class="goal-content">
                  <span class="goal-title">{{ goal.title }}</span>
                  <div class="goal-progress">
                    <div class="progress-bar">
                      <div class="progress-fill" :class="`progress-fill-${index % 4}`"
                        :style="{ width: `${(goal.current / goal.target) * 100}%` }"></div>
                    </div>
                    <span class="progress-text">{{ goal.current }}/{{ goal.target }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="daily-tasks">
            <h4><img src="../assets/mini_icons/学习计划.png" alt="每日安排" style="width: 20px; height: 20px; vertical-align: middle; margin-right: 6px;" />每日安排</h4>
            <div class="task-timeline">
              <div v-for="(day, index) in learningPlan.dailyTasks" :key="day.day" class="timeline-day">
                <div class="day-marker" :style="getDayMarkerStyle(day.day, index)">{{ day.day }}</div>
                <div class="day-tasks">
                  <div v-for="task in day.tasks" :key="task.title" class="task-card">
                    <span class="task-type" :style="getTaskTypeStyle(task.type)">{{ getTaskTypeLabel(task.type)
                    }}</span>
                    <span class="task-title">{{ task.title }}</span>
                    <span class="task-duration">{{ formatDuration(task.duration) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="plan-actions">
            <button class="plan-btn primary _target" @click.stop="saveLearningPlan">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6a2 2 0 0 0-2 2z" />
              </svg>
              保存计划
            </button>
            <button class="plan-btn secondary _target" @click.stop="generateLearningPlan(true)">
              重新生成
            </button>
          </div>
        </div>

        <div v-else class="generate-prompt">
          <button class="generate-btn _target" @click.stop="generateLearningPlan">
            <span class="btn-icon">✨</span>
            生成学习计划
          </button>
        </div>
      </div>

      <!-- 对话 -->
      <div v-if="activeSection === 'chat'" class="chat-section">
        <div ref="chatContainerRef" class="chat-messages">
          <div v-for="(msg, index) in chatMessages" :key="index" class="chat-message"
            :class="[msg.role, { streaming: msg.streaming }]">
            <div class="message-avatar">
              <span>{{ msg.role === 'assistant' ? 'AI' : '我' }}</span>
            </div>
            <div class="message-body">
              <div v-if="msg.streaming && !msg.content" class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div v-else class="message-content">
                <!-- 消息文字内容 -->
                <div class="message-text" v-html="msg.content ? renderMessageContent(msg.content) : ''"></div>
                <!-- 文件附件展示 -->
                <div v-if="msg.files && msg.files.length > 0" class="message-files">
                  <div v-for="(file, fileIndex) in msg.files" :key="fileIndex" class="file-attachment">
                    <!-- 图片预览 -->
                    <div v-if="file.isImage" class="image-preview">
                      <img :src="file.url" :alt="file.name" @click="openImagePreview(file.url)" />
                    </div>
                    <!-- 文件卡片 -->
                    <div v-else class="file-card">
                      <span class="file-icon">{{ getFileIcon(file.type) }}</span>
                      <div class="file-info">
                        <span class="file-name">{{ file.name }}</span>
                        <span class="file-size">{{ formatFileSize(file.size) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>

        <div class="quick-prompts">
          <button v-for="prompt in quickPrompts" :key="prompt.text" class="prompt-btn _target"
            @click.stop="useQuickPrompt(prompt)">
            <span>{{ prompt.icon }}</span>
            {{ prompt.text }}
          </button>
        </div>

        <!-- 已选文件预览区域 -->
        <div v-if="selectedFiles.length > 0" class="selected-files-area">
          <div class="selected-files-header">
            <span class="selected-files-title">已选择 {{ selectedFiles.length }} 个文件</span>
            <button class="clear-files-btn _target" @click.stop="clearAllFiles">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="selected-files-list">
            <div v-for="(file, index) in selectedFiles" :key="index" class="selected-file-item">
              <!-- 图片缩略图 -->
              <div v-if="file.previewUrl" class="selected-file-thumb">
                <img :src="file.previewUrl" :alt="file.name" />
              </div>
              <!-- 文件图标 -->
              <div v-else class="selected-file-icon">{{ file.icon }}</div>
              <div class="selected-file-info">
                <span class="selected-file-name">{{ file.name }}</span>
                <span class="selected-file-size">{{ formatFileSize(file.size) }}</span>
              </div>
              <button class="remove-file-btn _target" @click.stop="removeFile(index)">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <!-- 文件上传按钮 -->
          <input
            ref="fileInputRef"
            type="file"
            multiple
            accept="image/*,.txt,.md,.csv,.json,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip"
            style="display: none"
            @change="handleFileSelect"
          />
          <button 
            class="upload-btn _target" 
            :disabled="isTyping || isUploading || selectedFiles.length >= 5"
            @click.stop="triggerFileInput"
            title="上传文件或图片"
          >
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
            </svg>
          </button>
          
          <textarea ref="textareaRef" v-model="inputMessage" rows="1" class="chat-input" placeholder="输入你的问题，或上传文件..."
            @keydown="handleInputKeyDown" @click.stop></textarea>
          <button class="send-btn _target" :disabled="(!inputMessage.trim() && selectedFiles.length === 0) || isTyping || isUploading" @click.stop="sendMessage">
            <svg v-if="!isUploading" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
            <span v-else class="uploading-spinner"></span>
          </button>
        </div>
      </div>
    </div>

    <!-- 自定义确认弹窗 -->
    <div v-if="showConfirmDialog" class="confirm-overlay" @click.stop="handleConfirmCancel">
      <div class="confirm-dialog" @click.stop>
        <div class="confirm-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <div class="confirm-content">
          <h3>生成学习计划</h3>
          <p>每天仅限生成 2 次学习计划，是否确认继续生成并消耗配额？</p>
        </div>
        <div class="confirm-actions">
          <button class="btn-cancel" @click="handleConfirmCancel">取消</button>
          <button class="btn-confirm" @click="handleConfirmGenerate">确认生成</button>
        </div>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <div v-if="showImagePreview" class="image-preview-overlay" @click.stop="closeImagePreview">
      <div class="image-preview-container" @click.stop>
        <img :src="previewImageUrl" alt="预览图片" />
        <button class="close-preview-btn" @click.stop="closeImagePreview">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

/* 明亮科技蓝主题 */
.study-helper {
  position: relative;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  background: var(--card-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.04),
    0 1px 3px rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.study-helper:hover {
  border-color: rgba(0, 0, 0, 0.1);
  box-shadow:
    0 8px 40px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.02);
}

/* 头部 */
.helper-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.4) 100%);
  border-bottom: 1px solid rgba(14, 165, 233, 0.15);
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.helper-icon {
  position: relative;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow:
    0 8px 24px rgba(14, 165, 233, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.header-info h3 {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.header-info p {
  font-size: 13px;
  color: #64748b;
  margin: 4px 0 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 8px #10b981;
  animation: statusBlink 2s ease-in-out infinite;
}

@keyframes statusBlink {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }
}

/* 标签页 */
.section-tabs {
  display: flex;
  gap: 8px;
  padding: 4px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  backdrop-filter: blur(10px);
}

.tab {
  padding: 10px 20px;
  background: transparent;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab:hover {
  color: #0ea5e9;
  background: rgba(14, 165, 233, 0.1);
}

.tab.active {
  color: #fff;
  font-weight: 600;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

/* 内容区域 */
.helper-content {
  padding: 24px;
  position: relative;
  z-index: 1;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(14, 165, 233, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.1);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.errors {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.stat-icon.time {
  background: rgba(14, 165, 233, 0.15);
  color: #0ea5e9;
}

.stat-icon.score {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  font-family: 'JetBrains Mono', monospace;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

/* 概览卡片 */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  backdrop-filter: blur(10px);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -2px rgba(0, 0, 0, 0.1),
    0 10px 15px -3px rgba(0, 0, 0, 0.05);
  transform: translateZ(0);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px) translateZ(10px);
  box-shadow:
    0 20px 25px -5px rgba(0, 0, 0, 0.1),
    0 8px 10px -6px rgba(0, 0, 0, 0.1),
    0 20px 30px -10px rgba(0, 0, 0, 0.08);
}

.weak-card {
  border-left: none;
}

.strong-card {
  border-left: none;
}

.overview-card h4 {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 16px;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #475569;
}

.topic-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.topic-item.weak .topic-dot {
  background: #ef4444;
}

.topic-item.strong .topic-dot {
  background: #10b981;
}

/* 快速操作 */
.quick-actions h4 {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 16px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #0ea5e9;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: #0ea5e9;
  border-color: #0ea5e9;
  color: #fff;
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.3);
  transform: translateY(-2px);
}

.action-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

.action-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 错题分析 */
.section-title h3 {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.section-title p {
  font-size: 13px;
  color: #64748b;
  margin: 4px 0 0;
}

.error-categories {
  margin: 20px 0;
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  backdrop-filter: blur(10px);
}

.error-categories h4 {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 16px;
}

.category-bar {
  margin-bottom: 12px;
}

.bar-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}

.bar-label {
  color: #475569;
}

.bar-value {
  color: #0ea5e9;
  font-family: 'JetBrains Mono', monospace;
}

.bar-track {
  height: 6px;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #0ea5e9, #38bdf8);
  border-radius: 3px;
  transition: width 0.5s ease;
}

/* 聊天区域 */
.chat-messages {
  height: 400px;
  overflow-y: auto;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  margin-bottom: 16px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  backdrop-filter: blur(10px);
}

.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.chat-message.assistant .message-avatar {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: #fff;
}

.chat-message.user .message-avatar {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: #fff;
}

.message-body {
  max-width: 70%;
}

.message-text {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
  word-wrap: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  overflow-x: hidden;
}

.message-text > :first-child {
  margin-top: 0;
}

.message-text > :last-child {
  margin-bottom: 0;
}

/* ========== 智能体风格样式 ========== */

/* 基础段落 */
.ai-paragraph {
  margin: 0 0 10px;
  line-height: 1.7;
  color: #334155;
}

.ai-paragraph:last-child {
  margin-bottom: 0;
}

/* 智能体高亮文字 */
.ai-highlight {
  color: #0ea5e9;
  font-weight: 600;
  background: linear-gradient(180deg, transparent 60%, rgba(14, 165, 233, 0.12) 60%);
  padding: 0 2px;
}

/* 行内代码 */
.ai-code {
  display: inline;
  padding: 1px 5px;
  border-radius: 5px;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #0f172a;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  font-weight: 500;
  border: 1px solid rgba(148, 163, 184, 0.3);
  word-break: break-all;
}

/* 分隔线 */
.ai-divider {
  margin: 16px 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(14, 165, 233, 0.3), transparent);
  position: relative;
}

.ai-divider::after {
  content: '◆';
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: #0ea5e9;
  font-size: 8px;
  background: white;
  padding: 0 8px;
}

/* 智能体章节气泡 - 数字标题 */
.ai-section-bubble {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0 8px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 20px;
  box-shadow: 0 2px 6px rgba(14, 165, 233, 0.06);
}

.ai-section-bubble.ai-section-highlight {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  border-color: rgba(139, 92, 246, 0.2);
}

.ai-section-num {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(14, 165, 233, 0.25);
}

.ai-section-icon {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
  font-size: 12px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(139, 92, 246, 0.25);
}

.ai-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

/* 智能体要点 */
.ai-point {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 6px 0;
  padding: 8px 12px;
  background: rgba(241, 245, 249, 0.6);
  border-radius: 10px;
  border-left: 3px solid #0ea5e9;
}

.ai-point-dot {
  width: 6px;
  height: 6px;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  border-radius: 50%;
  flex-shrink: 0;
}

.ai-point-text {
  flex: 1;
  font-size: 13px;
  line-height: 1.5;
  color: #475569;
}

/* 代码块 */
.ai-code-block {
  margin: 12px 0;
  border-radius: 12px;
  overflow: hidden;
  background: #0f172a;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.3);
  max-width: 100%;
}

.ai-code-header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: linear-gradient(90deg, #1e293b 0%, #334155 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.ai-code-lang {
  padding: 4px 10px;
  background: rgba(14, 165, 233, 0.15);
  color: #7dd3fc;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-radius: 6px;
}

.ai-code-block pre {
  margin: 0;
  padding: 12px 16px;
  overflow-x: auto;
  max-width: 100%;
}

.ai-code-block code {
  display: block;
  color: #e2e8f0;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  overflow-wrap: break-word;
}

.chat-message.assistant .message-text {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(14, 165, 233, 0.2);
}

.chat-message.user .message-text {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  border: 1px solid rgba(139, 92, 246, 0.2);
}

.message-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  gap: 6px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 14px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #0ea5e9;
  border-radius: 50%;
  animation: typingBounce 1.4s ease-in-out infinite;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {

  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  30% {
    transform: translateY(-8px);
    opacity: 1;
  }
}

/* 输入区域 */
.chat-input-area {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 14px;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.6;
  color: #334155;
  outline: none;
  resize: none;
  min-height: 48px;
  max-height: 120px;
  overflow-y: auto;
  transition: all 0.3s ease;
}

.chat-input::placeholder {
  color: #94a3b8;
}

.chat-input:focus {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

/* 上传按钮 */
.upload-btn {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 14px;
  color: #0ea5e9;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.upload-btn:hover:not(:disabled) {
  background: rgba(14, 165, 233, 0.1);
  border-color: #0ea5e9;
  transform: scale(1.05);
}

.upload-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-btn {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  border-radius: 14px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 上传中动画 */
.uploading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 已选文件区域 */
.selected-files-area {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
}

.selected-files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.selected-files-title {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.clear-files-btn {
  width: 24px;
  height: 24px;
  background: rgba(239, 68, 68, 0.1);
  border: none;
  border-radius: 6px;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-files-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

.selected-files-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selected-file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  border: 1px solid rgba(14, 165, 233, 0.1);
}

.selected-file-thumb {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.selected-file-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-file-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 6px;
  flex-shrink: 0;
}

.selected-file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.selected-file-name {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selected-file-size {
  font-size: 11px;
  color: #94a3b8;
}

.remove-file-btn {
  width: 24px;
  height: 24px;
  background: transparent;
  border: none;
  border-radius: 6px;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.remove-file-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* 消息中的文件展示 */
.message-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-files {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-attachment {
  display: flex;
  flex-direction: column;
}

.image-preview {
  max-width: 200px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.image-preview:hover {
  transform: scale(1.02);
}

.image-preview img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 10px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(14, 165, 233, 0.15);
  max-width: 250px;
}

.file-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 11px;
  color: #94a3b8;
}

/* 快速提示 */
.quick-prompts {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.prompt-btn {
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(14, 165, 233, 0.15);
  border-radius: 20px;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
}

.prompt-btn:hover {
  background: rgba(14, 165, 233, 0.1);
  border-color: rgba(14, 165, 233, 0.3);
  color: #0ea5e9;
}

/* 响应式 */
@media (max-width: 640px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .section-tabs {
    width: 100%;
    overflow-x: auto;
  }
}

/* 学习计划部分 */
.plan-section {
  position: relative;
}

.section-title {
  margin-bottom: 20px;
}

.plan-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.week-goals h4,
.daily-tasks h4 {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 12px;
}

.goal-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goal-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  border: 1px solid rgba(14, 165, 233, 0.1);
}

.goal-check {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(14, 165, 233, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0ea5e9;
  flex-shrink: 0;
}

.goal-content {
  flex: 1;
}

.goal-title {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  display: block;
  margin-bottom: 8px;
}

.goal-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: rgba(14, 165, 233, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-fill-0 {
  background: #3b82f6;
  /* Blue */
}

.progress-fill-1 {
  background: #10b981;
  /* Emerald */
}

.progress-fill-2 {
  background: #f59e0b;
  /* Amber */
}

.progress-fill-3 {
  background: #8b5cf6;
  /* Violet */
}

.progress-text {
  font-size: 12px;
  color: #64748b;
  font-family: 'JetBrains Mono', monospace;
  min-width: 40px;
}

.task-timeline {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.timeline-day {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.day-marker {
  width: 48px;
  height: 48px;
  background: #1cb0f6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  flex-shrink: 0;
}

.day-tasks {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.day-tasks .task-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #f1f5f9;
}

.task-type {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.day-tasks .task-title {
  flex: 1;
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

.day-tasks .task-duration {
  font-size: 13px;
  color: #94a3b8;
  font-family: inherit;
}

.plan-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.plan-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.plan-btn.primary {
  background: #1cb0f6;
  border: none;
  color: white;
}

.plan-btn.primary:hover {
  background: #1899d6;
  box-shadow: 0 4px 12px rgba(28, 176, 246, 0.3);
}

.plan-btn.secondary {
  background: #ffffff;
  border: 1px solid #1cb0f6;
  color: #1cb0f6;
}

.plan-btn.secondary:hover {
  background: #f0f9ff;
}

/* 生成提示 */
.generate-prompt {
  display: flex;
  justify-content: center;
  padding: 40px 20px;
}

.generate-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 32px;
  background: #0ea5e9;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-btn:hover {
  background: #0284c7;
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(14, 165, 233, 0.4);
}

.btn-icon {
  font-size: 18px;
}

/* 生成中动画 */
.generating-overlay {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 20px;
}

.generating-content {
  text-align: center;
}

.ai-brain {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
}

.brain-core {
  position: absolute;
  inset: 20px;
  background: #0ea5e9;
  border-radius: 50%;
  animation: brainPulse 2s ease-in-out infinite;
}

.orbit {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(14, 165, 233, 0.3);
  border-radius: 50%;
  animation: orbitSpin 3s linear infinite;
}

.orbit::after {
  content: '';
  position: absolute;
  top: -4px;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #0ea5e9;
  border-radius: 50%;
  transform: translateX(-50%);
}

.orbit-1 {
  animation-duration: 2s;
}

.orbit-2 {
  animation-duration: 3s;
  animation-direction: reverse;
}

.orbit-3 {
  animation-duration: 4s;
}

@keyframes brainPulse {

  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

@keyframes orbitSpin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.generating-text {
  font-size: 14px;
  color: #64748b;
}

/* 最近错题 */
.recent-errors {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  backdrop-filter: blur(10px);
}

.recent-errors h4 {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin: 0 0 16px;
}

.error-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.error-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
}

.error-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.error-title {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.error-reason {
  font-size: 12px;
  color: #64748b;
}

.error-time {
  font-size: 12px;
  color: #94a3b8;
}

.empty-hint {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
  padding: 12px;
}

/* 自定义弹窗 */
.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}

.confirm-dialog {
  width: 90%;
  max-width: 320px;
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.confirm-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-content h3 {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.confirm-content p {
  margin: 0 0 24px;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.confirm-actions button {
  flex: 1;
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #f1f5f9;
  border: none;
  color: #64748b;
}

.btn-cancel:hover {
  background: #e2e8f0;
}

.btn-confirm {
  background: #1cb0f6;
  border: none;
  color: #ffffff;
}

.btn-confirm:hover {
  background: #1899d6;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 图片预览弹窗 */
.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(8px);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
  padding: 20px;
}

.image-preview-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview-container img {
  max-width: 100%;
  max-height: 90vh;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.close-preview-btn {
  position: absolute;
  top: -50px;
  right: 0;
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-preview-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}
</style>
