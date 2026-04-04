<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useUserStore } from '../stores/user'
import api from '../api'

const userStore = useUserStore()

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

const quickPrompts = [
  { text: '分析我最近的错题', icon: '🔍' },
  { text: '帮我制定学习计划', icon: '📋' },
  { text: '推荐练习题目', icon: '💡' },
  { text: '解答我的疑问', icon: '❓' },
]

const fetchErrorAnalysis = async () => {
  errorAnalysis.value = {
    totalErrors: 23,
    categories: [
      { name: '动态规划', count: 8, percentage: 35 },
      { name: '图论', count: 6, percentage: 26 },
      { name: '二叉树', count: 5, percentage: 22 },
      { name: '字符串', count: 4, percentage: 17 },
    ],
    recentErrors: [
      { id: 1, title: '背包问题', reason: '状态转移方程错误', time: '2小时前' },
      { id: 2, title: '最短路径', reason: '边界条件遗漏', time: '昨天' },
      { id: 3, title: '二叉树遍历', reason: '递归终止条件错误', time: '2天前' },
    ],
    improvementTrend: 'up',
  }
  return errorAnalysis.value
}

const fetchStudyHabits = async () => {
  studyHabits.value = {
    weeklyStudyTime: 12.5,
    averageTimePerQuestion: 15,
    preferredTimeSlot: '晚间 20:00-22:00',
    consistencyScore: 78,
    strongTopics: ['数组', '字符串', '链表'],
    weakTopics: ['动态规划', '图论'],
  }
  return studyHabits.value
}

const getTrackLabel = (value) => {
  const option = props.trackOptions.find(opt => opt.value === value)
  return option?.label || '算法思维赛道'
}

const getTrackPlan = (track) => {
  const plans = {
    algo: {
      weekGoals: [
        { id: 1, title: '掌握动态规划基础', progress: 0, target: Math.ceil(props.weeklyGoal * 0.4) },
        { id: 2, title: '完成贪心算法入门', progress: 0, target: Math.ceil(props.weeklyGoal * 0.3) },
        { id: 3, title: '每日坚持练习', progress: 0, target: props.weeklyGoal },
      ],
      dailyTasks: [
        { day: '今天', tasks: [
          { title: '动态规划基础概念', duration: 30, type: 'learn' },
          { title: '斐波那契数列DP解法', duration: 25, type: 'practice' }
        ]},
        { day: '明天', tasks: [
          { title: '背包问题入门', duration: 40, type: 'learn' },
          { title: '01背包练习题', duration: 35, type: 'practice' }
        ]},
        { day: '后天', tasks: [
          { title: '贪心算法思想', duration: 30, type: 'learn' },
          { title: '区间调度问题', duration: 30, type: 'practice' }
        ]},
      ],
      recommendations: [
        { type: 'video', title: '动态规划入门讲解', source: '推荐' },
        { type: 'article', title: '背包问题详解', source: '必读' },
        { type: 'practice', title: 'DP经典50题', source: '练习' },
      ]
    },
    ds: {
      weekGoals: [
        { id: 1, title: '掌握链表与栈队列', progress: 0, target: Math.ceil(props.weeklyGoal * 0.35) },
        { id: 2, title: '完成二叉树基础', progress: 0, target: Math.ceil(props.weeklyGoal * 0.35) },
        { id: 3, title: '每日坚持练习', progress: 0, target: props.weeklyGoal },
      ],
      dailyTasks: [
        { day: '今天', tasks: [
          { title: '链表基础操作', duration: 35, type: 'learn' },
          { title: '反转链表实现', duration: 30, type: 'practice' }
        ]},
        { day: '明天', tasks: [
          { title: '栈与队列应用', duration: 30, type: 'learn' },
          { title: '括号匹配问题', duration: 25, type: 'practice' }
        ]},
        { day: '后天', tasks: [
          { title: '二叉树遍历', duration: 40, type: 'learn' },
          { title: '前中后序遍历实现', duration: 35, type: 'practice' }
        ]},
      ],
      recommendations: [
        { type: 'video', title: '数据结构基础', source: '推荐' },
        { type: 'article', title: '链表操作技巧', source: '必读' },
        { type: 'practice', title: '树结构专项训练', source: '练习' },
      ]
    },
    contest: {
      weekGoals: [
        { id: 1, title: '掌握高频竞赛题型', progress: 0, target: Math.ceil(props.weeklyGoal * 0.4) },
        { id: 2, title: '提升解题速度', progress: 0, target: Math.ceil(props.weeklyGoal * 0.3) },
        { id: 3, title: '完成模拟赛训练', progress: 0, target: Math.ceil(props.weeklyGoal * 0.3) },
      ],
      dailyTasks: [
        { day: '今天', tasks: [
          { title: '竞赛技巧与策略', duration: 30, type: 'learn' },
          { title: '快速排序优化', duration: 25, type: 'practice' }
        ]},
        { day: '明天', tasks: [
          { title: '二分查找进阶', duration: 35, type: 'learn' },
          { title: '二分答案专题', duration: 40, type: 'practice' }
        ]},
        { day: '后天', tasks: [
          { title: '模拟竞赛训练', duration: 60, type: 'practice' },
          { title: '赛后复盘总结', duration: 20, type: 'review' }
        ]},
      ],
      recommendations: [
        { type: 'video', title: '竞赛算法精讲', source: '推荐' },
        { type: 'article', title: 'ACM竞赛经验', source: '必读' },
        { type: 'practice', title: 'Codeforces真题', source: '练习' },
      ]
    }
  }
  return plans[track] || plans.algo
}

const generateLearningPlan = async (preferences = {}) => {
  isGenerating.value = true
  isGenerated.value = false
  
  try {
    const response = await api.generateLearningPlan({
      track: props.selectedTrack,
      trackLabel: getTrackLabel(props.selectedTrack),
      weeklyGoal: props.weeklyGoal,
      weeklyCompleted: userStore.userInfo?.weeklyCompleted || 0,
      totalSolved: userStore.userInfo?.totalSolved || 0,
      currentStreak: userStore.userInfo?.currentStreak || 0,
      persistenceIndex: userStore.userInfo?.persistenceIndex || 50,
      errorTopics: studyHabits.value.weakTopics || [],
      strongAreas: studyHabits.value.strongTopics || [],
      weakAreas: studyHabits.value.weakTopics || [],
      ...preferences
    })

    if (response.data && response.data.code === 0) {
      const planData = response.data.data
      learningPlan.value = {
        weekGoals: planData.weekGoals || [],
        dailyTasks: planData.dailyTasks || [],
        recommendations: planData.recommendations || [],
        generatedAt: planData.generatedAt || new Date().toISOString(),
        track: planData.track || props.selectedTrack,
        trackLabel: planData.trackLabel || getTrackLabel(props.selectedTrack),
        weeklyGoal: planData.weeklyGoal || props.weeklyGoal,
        id: planData.id,
        isAIGenerated: planData.isAIGenerated || false,
        aiSuggestions: planData.aiSuggestions || [],
        profileAnalysis: planData.profileAnalysis || null
      }
      
      isGenerated.value = true
      
      if (planData.isAIGenerated) {
        chatMessages.value.push({
          role: 'assistant',
          content: `✨ **AI已为您生成个性化学习计划！**\n\n根据您的学习数据，已为您定制了专属的学习路径。`,
          time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        })
      } else {
        chatMessages.value.push({
          role: 'assistant',
          content: `📋 **学习计划已生成！**\n\n已根据您的赛道和目标生成了学习计划。`,
          time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
        })
      }
      
      await nextTick()
      scrollToBottom()
      
      await new Promise(r => setTimeout(r, 1500))
      return learningPlan.value
    }
  } catch (error) {
    console.error('生成学习计划失败:', error)
    
    let errorMessage = '生成学习计划时出现问题'
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      errorMessage = 'AI服务响应超时，已使用本地算法生成计划'
    } else if (error.response?.status >= 500) {
      errorMessage = '服务器暂时不可用，已使用本地算法生成计划'
    }
    
    chatMessages.value.push({
      role: 'assistant',
      content: `⚠️ ${errorMessage}\n\n已为您生成基础学习计划，您可以继续使用。`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
    
    const trackPlan = getTrackPlan(props.selectedTrack)
    const today = new Date()
    
    learningPlan.value = {
      weekGoals: trackPlan.weekGoals,
      dailyTasks: trackPlan.dailyTasks,
      recommendations: trackPlan.recommendations,
      generatedAt: today.toISOString(),
      track: props.selectedTrack,
      trackLabel: getTrackLabel(props.selectedTrack),
      weeklyGoal: props.weeklyGoal,
      isAIGenerated: false
    }
    
    isGenerated.value = true
    await nextTick()
    scrollToBottom()
    await new Promise(r => setTimeout(r, 1500))
  } finally {
    isGenerating.value = false
  }
  return learningPlan.value
}

const saveLearningPlan = async () => {
  if (!learningPlan.value.id) {
    try {
      const response = await api.saveLearningPlan({
        track: learningPlan.value.track,
        trackLabel: learningPlan.value.trackLabel,
        weeklyGoal: learningPlan.value.weeklyGoal,
        weekGoals: learningPlan.value.weekGoals,
        dailyTasks: learningPlan.value.dailyTasks,
        recommendations: learningPlan.value.recommendations
      })
      
      if (response.data && response.data.code === 0) {
        learningPlan.value.id = response.data.data.id
        return true
      }
    } catch (error) {
      console.error('保存学习计划失败:', error)
    }
  }
  return false
}

const fetchCurrentLearningPlan = async () => {
  try {
    const response = await api.getCurrentLearningPlan(props.selectedTrack)
    
    if (response.data && response.data.code === 0) {
      const planData = response.data.data
      learningPlan.value = {
        weekGoals: planData.weekGoals || [],
        dailyTasks: planData.dailyTasks || [],
        recommendations: planData.recommendations || [],
        generatedAt: planData.generatedAt || new Date().toISOString(),
        track: planData.track || props.selectedTrack,
        trackLabel: planData.trackLabel || getTrackLabel(props.selectedTrack),
        weeklyGoal: planData.weeklyGoal || props.weeklyGoal,
        id: planData.id
      }
      return learningPlan.value
    }
  } catch (error) {
    console.error('获取学习计划失败:', error)
  }
  return null
}

// 监听赛道和目标变化，自动更新计划
watch(() => props.selectedTrack, async (newTrack, oldTrack) => {
  if (newTrack !== oldTrack && learningPlan.value.generatedAt) {
    await generateLearningPlan()
    // 添加系统消息通知用户计划已更新
    chatMessages.value.push({
      role: 'assistant',
      content: `已切换至 **${getTrackLabel(newTrack)}**，学习计划已根据新赛道自动更新！\n\n本周目标：完成 ${props.weeklyGoal} 个关卡\n建议按照生成的每日安排进行学习。`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
    await nextTick()
    scrollToBottom()
  }
}, { immediate: false })

watch(() => props.weeklyGoal, async (newGoal, oldGoal) => {
  if (newGoal !== oldGoal && learningPlan.value.generatedAt) {
    await generateLearningPlan()
    // 更新目标数值
    chatMessages.value.push({
      role: 'assistant',
      content: `周目标已更新为 **${newGoal} 个关卡**，学习计划已重新调整！`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
    await nextTick()
    scrollToBottom()
  }
}, { immediate: false })

const analyzeErrors = async () => {
  const errors = await fetchErrorAnalysis()
  const habits = await fetchStudyHabits()
  return {
    errorAnalysis: errors,
    studyHabits: habits,
    summary: generateAnalysisSummary(errors, habits),
  }
}

const generateAnalysisSummary = (errors, habits) => {
  const topErrorCategory = errors.categories[0]?.name || '未知'
  return {
    mainIssue: `${topErrorCategory}是你的主要薄弱点`,
    suggestion: `建议本周重点攻克${topErrorCategory}，每天练习2-3题`,
    encouragement: `你已经连续学习${Math.floor(habits.consistencyScore / 10)}天，继续保持！`,
  }
}

const sendMessage = async (message) => {
  if (!message?.trim()) return

  chatMessages.value.push({
    role: 'user',
    content: message,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })

  inputMessage.value = ''
  isTyping.value = true
  await nextTick()
  scrollToBottom()

  try {
    const history = chatMessages.value.slice(-10).map(msg => ({
      role: msg.role,
      content: msg.content
    }))

    const response = await api.aiChat(message, history)
    
    if (response.data && response.data.code === 0) {
      chatMessages.value.push({
        role: 'assistant',
        content: response.data.data.response,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      })
    } else {
      chatMessages.value.push({
        role: 'assistant',
        content: `❌ AI服务暂时不可用\n\n请稍后再试。`,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      })
    }
  } catch (error) {
    console.error('AI对话失败:', error)
    
    let errorMsg = 'AI服务暂时不可用，请稍后再试'
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      errorMsg = 'AI服务响应超时，请稍后再试'
    } else if (error.response?.status >= 500) {
      errorMsg = '服务器暂时不可用，请稍后再试'
    }
    
    chatMessages.value.push({
      role: 'assistant',
      content: `❌ ${errorMsg}`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  } finally {
    isTyping.value = false
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

const handleJump = (e) => {
  // 如果点击的是内部交互元素，不触发跳转
  if (e.target.closest('button') || e.target.closest('input') || e.target.closest('textarea')) {
    return
  }
  
  if (isJumping.value) return
  isJumping.value = true
  
  // 获取当前元素位置
  const section = document.querySelector('.study-helper')
  if (section) {
    const rect = section.getBoundingClientRect()
    const scrollY = window.scrollY + rect.top - 20 // 留出20px边距
    
    // 平滑滚动到页面顶部
    window.scrollTo({
      top: scrollY,
      behavior: 'smooth'
    })
  }
  
  // 动画结束后重置状态
  setTimeout(() => {
    isJumping.value = false
  }, 600)
}

const updateErrorAnalysis = (data) => {
  errorAnalysis.value = { ...errorAnalysis.value, ...data }
}

const updateStudyHabits = (data) => {
  studyHabits.value = { ...studyHabits.value, ...data }
}

const updateLearningPlan = (data) => {
  learningPlan.value = { ...learningPlan.value, ...data }
}

const resetData = () => {
  errorAnalysis.value = { totalErrors: 0, categories: [], recentErrors: [], improvementTrend: '' }
  studyHabits.value = { weeklyStudyTime: 0, averageTimePerQuestion: 0, preferredTimeSlot: '', consistencyScore: 0, strongTopics: [], weakTopics: [] }
  learningPlan.value = { weekGoals: [], dailyTasks: [], recommendations: [], generatedAt: null }
}

const closeGeneratingOverlay = () => {
  // 只有在生成完成后才能关闭
  if (isGenerated.value) {
    isGenerating.value = false
    isGenerated.value = false
  }
}

defineExpose({
  fetchErrorAnalysis,
  fetchStudyHabits,
  generateLearningPlan,
  analyzeErrors,
  sendMessage,
  updateErrorAnalysis,
  updateStudyHabits,
  updateLearningPlan,
  resetData,
})

onMounted(async () => {
  await fetchErrorAnalysis()
  await fetchStudyHabits()
  await fetchCurrentLearningPlan()
  
  const trackLabel = getTrackLabel(props.selectedTrack)
  chatMessages.value.push({
    role: 'assistant',
    content: `你好，${userStore.userInfo?.name || '同学'}！👋

我是你的学习小助手，可以帮你：
- 📊 分析错题，找出薄弱点
- 📋 制定个性化学习计划
- 💡 推荐适合的练习题目

当前赛道：**${trackLabel}**
周目标：**${props.weeklyGoal} 个关卡**

有什么我可以帮你的吗？`,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
})
</script>

<template>
  <section class="study-helper" :class="{ jumping: isJumping }" @click="handleJump">
    <div class="helper-header">
      <div class="header-left">
        <div class="helper-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
          </svg>
        </div>
        <div class="header-info">
          <h3>学习助手</h3>
          <p>陪你一起进步</p>
        </div>
      </div>
      <div class="section-tabs">
        <button :class="['tab', { active: activeSection === 'overview' }]" @click="activeSection = 'overview'">总览</button>
        <button :class="['tab', { active: activeSection === 'errors' }]" @click="activeSection = 'errors'">错题</button>
        <button :class="['tab', { active: activeSection === 'plan' }]" @click="activeSection = 'plan'">计划</button>
        <button :class="['tab', { active: activeSection === 'chat' }]" @click="activeSection = 'chat'">对话</button>
      </div>
    </div>

    <div class="helper-content">
      <div v-show="activeSection === 'overview'" class="overview-section">
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-icon errors">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
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
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
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
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ studyHabits.consistencyScore }}</span>
              <span class="stat-label">坚持指数</span>
            </div>
          </div>
        </div>

        <div class="overview-grid">
          <div class="overview-card">
            <h4>薄弱环节</h4>
            <div class="topic-list">
              <div v-for="topic in studyHabits.weakTopics" :key="topic" class="topic-item weak">
                <span class="topic-dot"></span>
                {{ topic }}
              </div>
              <div v-if="!studyHabits.weakTopics.length" class="empty-hint">暂无数据</div>
            </div>
          </div>
          <div class="overview-card">
            <h4>擅长领域</h4>
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
          <h4>快速开始</h4>
          <div class="action-buttons">
            <button class="action-btn" @click="activeSection = 'errors'">
              <span class="action-icon">📊</span>
              <span>查看错题分析</span>
            </button>
            <button class="action-btn" @click="activeSection = 'plan'">
              <span class="action-icon">📋</span>
              <span>生成学习计划</span>
            </button>
            <button class="action-btn" @click="activeSection = 'chat'">
              <span class="action-icon">💬</span>
              <span>开始对话</span>
            </button>
          </div>
        </div>
      </div>

      <div v-show="activeSection === 'errors'" class="errors-section">
        <div class="section-title">
          <h3>错题分析</h3>
          <p>找出薄弱环节，针对性提升</p>
        </div>

        <div class="error-categories">
          <h4>错误类型分布</h4>
          <div class="category-bars">
            <div v-for="cat in errorAnalysis.categories" :key="cat.name" class="category-bar">
              <div class="bar-header">
                <span class="bar-label">{{ cat.name }}</span>
                <span class="bar-value">{{ cat.count }}次</span>
              </div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: cat.percentage + '%' }"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="recent-errors">
          <h4>最近错题</h4>
          <div class="error-list">
            <div v-for="err in errorAnalysis.recentErrors" :key="err.id" class="error-item">
              <div class="error-main">
                <span class="error-title">{{ err.title }}</span>
                <span class="error-reason">{{ err.reason }}</span>
              </div>
              <span class="error-time">{{ err.time }}</span>
            </div>
            <div v-if="!errorAnalysis.recentErrors.length" class="empty-hint">暂无错题记录</div>
          </div>
        </div>
      </div>

      <div v-show="activeSection === 'plan'" class="plan-section">
        <div class="section-title">
          <h3>学习计划</h3>
          <p v-if="learningPlan.trackLabel">{{ learningPlan.trackLabel }} · 周目标 {{ learningPlan.weeklyGoal || weeklyGoal }} 关</p>
          <p v-else>根据你的情况定制</p>
        </div>

        <div class="week-goals">
          <h4>本周目标</h4>
          <div class="goal-list">
            <div v-for="goal in learningPlan.weekGoals" :key="goal.id" class="goal-item">
              <div class="goal-check">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                </svg>
              </div>
              <div class="goal-content">
                <span class="goal-title">{{ goal.title }}</span>
                <div class="goal-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: (goal.progress / goal.target * 100) + '%' }"></div>
                  </div>
                  <span class="progress-text">{{ goal.progress }}/{{ goal.target }}</span>
                </div>
              </div>
            </div>
            <div v-if="!learningPlan.weekGoals.length" class="empty-hint">
              <button class="generate-btn" @click="generateLearningPlan()">生成学习计划</button>
            </div>
          </div>
        </div>

        <div class="daily-tasks">
          <h4>每日安排</h4>
          <div class="task-timeline">
            <div v-for="day in learningPlan.dailyTasks" :key="day.day" class="timeline-day">
              <div class="day-marker">{{ day.day }}</div>
              <div class="day-tasks">
                <div v-for="task in day.tasks" :key="task.title" class="task-card">
                  <span class="task-type">{{ task.type === 'learn' ? '学习' : task.type === 'practice' ? '练习' : '复习' }}</span>
                  <span class="task-title">{{ task.title }}</span>
                  <span class="task-duration">{{ task.duration }}分钟</span>
                </div>
              </div>
            </div>
            <div v-if="!learningPlan.dailyTasks.length" class="empty-hint">暂无计划</div>
          </div>
        </div>

        <div class="plan-actions">
          <button class="plan-btn primary" @click="saveLearningPlan()" :disabled="isGenerating">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6a2 2 0 0 0-2 2z"/>
            </svg>
            保存计划
          </button>
          <button class="plan-btn secondary" @click="generateLearningPlan()" :disabled="isGenerating">
            <span v-if="isGenerating" class="btn-spinner"></span>
            <span v-else>重新生成</span>
          </button>
        </div>

      </div>

      <div v-show="activeSection === 'chat'" class="chat-section">
        <div ref="chatContainerRef" class="chat-messages">
          <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['chat-message', msg.role]">
            <div class="message-avatar">
              <span v-if="msg.role === 'assistant'">学</span>
              <span v-else>{{ (userStore.userInfo?.name || '我').slice(0, 1) }}</span>
            </div>
            <div class="message-body">
              <div class="message-text" v-html="msg.content.replace(/\n/g, '<br>').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="isTyping" class="chat-message assistant">
            <div class="message-avatar"><span>学</span></div>
            <div class="message-body">
              <div class="typing-dots">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="quick-prompts">
          <button v-for="prompt in quickPrompts" :key="prompt.text" class="prompt-btn" @click="sendMessage(prompt.text)">
            <span>{{ prompt.icon }}</span>
            {{ prompt.text }}
          </button>
        </div>

        <div class="chat-input-area">
          <input v-model="inputMessage" type="text" class="chat-input" placeholder="输入你的问题..." @keyup.enter="sendMessage(inputMessage)" />
          <button class="send-btn" :disabled="!inputMessage.trim()" @click="sendMessage(inputMessage)">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 容器内加载遮罩 - 固定在section内，跨页面保持 -->
    <div v-if="isGenerating" class="generating-overlay" @click="closeGeneratingOverlay">
      <div class="generating-content" :class="{ 'success-state': isGenerated }" @click.stop>
        <!-- 加载中状态 -->
        <template v-if="!isGenerated">
          <div class="ai-brain">
            <div class="brain-core"></div>
            <div class="orbit orbit-1"></div>
            <div class="orbit orbit-2"></div>
            <div class="orbit orbit-3"></div>
            <div class="particles">
              <span v-for="n in 12" :key="n" class="particle" :style="{ '--i': n }"></span>
            </div>
          </div>
          <h3 class="generating-title">AI 正在思考</h3>
          <p class="generating-subtitle">为您定制专属学习计划</p>
          <div class="progress-bar">
            <div class="progress-fill"></div>
          </div>
          <div class="generating-tips">
            <span class="tip-icon">💡</span>
            <span class="tip-text">根据您的学习数据智能分析中...</span>
          </div>
        </template>

        <!-- 生成完成状态 -->
        <template v-else>
          <div class="success-icon">
            <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8 12l2.5 2.5L16 9"/>
            </svg>
          </div>
          <h3 class="generating-title success">生成完成！</h3>
          <p class="generating-subtitle">您的专属学习计划已准备就绪</p>
          <div class="progress-bar completed">
            <div class="progress-fill" style="width: 100%;"></div>
          </div>
          <div class="generating-tips success-tip">
            <span class="tip-icon">🎉</span>
            <span class="tip-text">点击任意处关闭提示</span>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

.study-helper {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  background: linear-gradient(135deg, #f8fafc 0%, #f0f4f8 50%, #e8ecf4 100%);
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(74, 111, 157, 0.15);
  box-shadow: 0 8px 32px rgba(74, 111, 157, 0.08);
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55), box-shadow 0.3s ease;
  position: relative;
}

.study-helper:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(74, 111, 157, 0.15);
}

.study-helper.jumping {
  animation: jumpToTop 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes jumpToTop {
  0% {
    transform: scale(1) translateY(0);
  }
  30% {
    transform: scale(0.95) translateY(10px);
  }
  50% {
    transform: scale(1.02) translateY(-20px);
  }
  70% {
    transform: scale(0.98) translateY(-10px);
  }
  100% {
    transform: scale(1) translateY(0);
  }
}

.study-helper::before {
  content: '点击跳转到顶部';
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 16px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  color: white;
  font-size: 12px;
  font-weight: 500;
  border-radius: 20px;
  opacity: 0;
  pointer-events: none;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.3);
}

.study-helper:hover::before {
  opacity: 1;
  top: -50px;
}

.helper-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(180deg, rgba(255,255,255,0.9) 0%, rgba(248,250,252,0.8) 100%);
  border-bottom: 1px solid rgba(74, 111, 157, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.helper-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.25);
}

.header-info h3 {
  font-size: 18px;
  font-weight: 700;
  color: #3f5d85;
  margin: 0;
}

.header-info p {
  font-size: 12px;
  color: #64748b;
  margin: 2px 0 0;
}

.section-tabs {
  display: flex;
  gap: 6px;
}

.tab {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #4a6f9d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.95);
}

.tab.active {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 2px 8px rgba(74, 111, 157, 0.25);
}

.helper-content {
  min-height: 400px;
  padding: 24px;
}

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
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(74, 111, 157, 0.1);
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.errors {
  background: #fef3c7;
  color: #d97706;
}

.stat-icon.time {
  background: #dbeafe;
  color: #4a6f9d;
}

.stat-icon.score {
  background: #ede9fe;
  color: #6672cb;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #3f5d85;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(74, 111, 157, 0.1);
}

.overview-card h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3f5d85;
  margin: 0 0 12px;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}

.topic-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.topic-item.weak .topic-dot {
  background: #6672cb;
}

.topic-item.strong .topic-dot {
  background: #10b981;
}

.empty-hint {
  color: #64748b;
  font-size: 13px;
  opacity: 0.7;
  text-align: center;
  padding: 12px;
}

.quick-actions h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3f5d85;
  margin: 0 0 12px;
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
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #4a6f9d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: white;
  border-color: rgba(74, 111, 157, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.12);
}

.action-icon {
  font-size: 16px;
}

.section-title {
  margin-bottom: 20px;
}

.section-title h3 {
  font-size: 18px;
  font-weight: 700;
  color: #3f5d85;
  margin: 0;
}

.section-title p {
  font-size: 13px;
  color: #64748b;
  margin: 4px 0 0;
}

.error-categories {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(74, 111, 157, 0.1);
  margin-bottom: 16px;
}

.error-categories h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3f5d85;
  margin: 0 0 16px;
}

.category-bars {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.category-bar {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bar-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.bar-label {
  color: #3f5d85;
  font-weight: 500;
}

.bar-value {
  color: #64748b;
}

.bar-track {
  height: 8px;
  background: rgba(74, 111, 157, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.recent-errors {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(74, 111, 157, 0.1);
}

.recent-errors h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3f5d85;
  margin: 0 0 14px;
}

.error-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.error-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: #f0f4f8;
  border-radius: 10px;
  border-left: 3px solid #6672cb;
}

.error-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.error-title {
  font-size: 13px;
  font-weight: 500;
  color: #3f5d85;
}

.error-reason {
  font-size: 11px;
  color: #64748b;
}

.error-time {
  font-size: 11px;
  color: #64748b;
}

.week-goals {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(74, 111, 157, 0.1);
  margin-bottom: 16px;
}

.week-goals h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3f5d85;
  margin: 0 0 14px;
}

.goal-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goal-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.goal-check {
  color: #64748b;
  margin-top: 2px;
}

.goal-content {
  flex: 1;
}

.goal-title {
  font-size: 13px;
  font-weight: 500;
  color: #3f5d85;
}

.goal-progress {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: rgba(74, 111, 157, 0.15);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 3px;
}

.progress-text {
  font-size: 11px;
  color: #64748b;
  min-width: 40px;
}

.generate-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.daily-tasks {
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(74, 111, 157, 0.1);
  margin-bottom: 16px;
}

.daily-tasks h4 {
  font-size: 14px;
  font-weight: 600;
  color: #3f5d85;
  margin: 0 0 14px;
}

.task-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-day {
  display: flex;
  gap: 14px;
}

.day-marker {
  width: 50px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  padding-top: 8px;
}

.day-tasks {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: white;
  border: 1px solid rgba(74, 111, 157, 0.1);
  border-radius: 10px;
}

.task-type {
  padding: 2px 8px;
  background: #dbeafe;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  color: #4a6f9d;
}

.task-title {
  flex: 1;
  font-size: 13px;
  color: #3f5d85;
}

.task-duration {
  font-size: 11px;
  color: #64748b;
}

.plan-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.plan-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 24px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.plan-btn.primary {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.25);
}

.plan-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 111, 157, 0.3);
}

.plan-btn.secondary {
  background: white;
  border: 1px solid rgba(74, 111, 157, 0.2);
  color: #4a6f9d;
}

.plan-btn.secondary:hover {
  background: #f8fafc;
}

.plan-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(74, 111, 157, 0.2);
  border-top-color: #4a6f9d;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 容器内加载遮罩 - 固定在section内 */
.generating-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  animation: fadeIn 0.3s ease;
  border-radius: 24px;
  cursor: pointer;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.generating-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 48px 64px;
  background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(248,250,252,0.95) 100%);
  border-radius: 32px;
  box-shadow: 0 32px 64px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255,255,255,0.1);
  animation: slideUp 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* AI大脑动画 */
.ai-brain {
  position: relative;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brain-core {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #6672cb 0%, #4a6f9d 50%, #3b5998 100%);
  border-radius: 50%;
  box-shadow:
    0 0 30px rgba(102, 114, 203, 0.6),
    0 0 60px rgba(102, 114, 203, 0.3),
    inset 0 -2px 10px rgba(0,0,0,0.2);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 30px rgba(102, 114, 203, 0.6), 0 0 60px rgba(102, 114, 203, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 0 40px rgba(102, 114, 203, 0.8), 0 0 80px rgba(102, 114, 203, 0.4);
  }
}

.orbit {
  position: absolute;
  border: 2px solid transparent;
  border-radius: 50%;
}

.orbit-1 {
  width: 70px;
  height: 70px;
  border-top-color: rgba(102, 114, 203, 0.6);
  border-right-color: rgba(102, 114, 203, 0.3);
  animation: rotate 3s linear infinite;
}

.orbit-2 {
  width: 90px;
  height: 90px;
  border-bottom-color: rgba(74, 111, 157, 0.5);
  border-left-color: rgba(74, 111, 157, 0.2);
  animation: rotate 4s linear infinite reverse;
}

.orbit-3 {
  width: 110px;
  height: 110px;
  border-top-color: rgba(102, 114, 203, 0.3);
  border-bottom-color: rgba(74, 111, 157, 0.4);
  animation: rotate 5s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: linear-gradient(135deg, #6672cb, #4a6f9d);
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: rotate(calc(var(--i) * 30deg)) translateX(55px);
  animation: particleFloat 2s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.15s);
  opacity: 0;
}

@keyframes particleFloat {
  0%, 100% {
    opacity: 0;
    transform: rotate(calc(var(--i) * 30deg)) translateX(55px) scale(0.5);
  }
  50% {
    opacity: 1;
    transform: rotate(calc(var(--i) * 30deg)) translateX(65px) scale(1);
  }
}

.generating-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.generating-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  font-weight: 500;
}

.progress-bar {
  width: 200px;
  height: 4px;
  background: rgba(74, 111, 157, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

/* 生成遮罩中的进度条动画 */
.generating-content .progress-fill {
  height: 100%;
  width: 30%;
  background: linear-gradient(90deg, #4a6f9d 0%, #6672cb 50%, #4a6f9d 100%);
  background-size: 200% 100%;
  border-radius: 2px;
  animation: progressMove 2s ease-in-out infinite, shimmer 1.5s linear infinite;
}

@keyframes progressMove {
  0% { width: 0%; margin-left: 0%; }
  50% { width: 60%; margin-left: 20%; }
  100% { width: 30%; margin-left: 70%; }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.generating-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(102, 114, 203, 0.08);
  border-radius: 12px;
  border: 1px solid rgba(102, 114, 203, 0.15);
}

.generating-tips.success-tip {
  background: rgba(16, 185, 129, 0.08);
  border-color: rgba(16, 185, 129, 0.2);
}

.tip-icon {
  font-size: 16px;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.tip-text {
  font-size: 13px;
  color: #4a6f9d;
  font-weight: 500;
}

.generating-tips.success-tip .tip-text {
  color: #059669;
}

/* 成功状态样式 */
.success-state {
  animation: successPulse 0.5s ease;
}

@keyframes successPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

.success-icon {
  color: #10b981;
  animation: successPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes successPop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon svg {
  filter: drop-shadow(0 4px 12px rgba(16, 185, 129, 0.4));
}

.generating-title.success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 生成遮罩中的完成状态进度条 */
.generating-content .progress-bar.completed {
  background: rgba(16, 185, 129, 0.15);
}

.generating-content .progress-bar.completed .progress-fill {
  background: linear-gradient(90deg, #10b981 0%, #34d399 100%);
  animation: none;
  width: 100% !important;
  transition: width 0.5s ease;
}

.chat-section {
  display: flex;
  flex-direction: column;
  height: 380px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  margin-bottom: 12px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(74, 111, 157, 0.2);
  border-radius: 2px;
}

.chat-message {
  display: flex;
  gap: 10px;
  max-width: 85%;
}

.chat-message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.chat-message.assistant .message-avatar {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  color: white;
}

.chat-message.user .message-avatar {
  background: #dbeafe;
  color: #4a6f9d;
}

.message-body {
  flex: 1;
}

.message-text {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.6;
  color: #334155;
}

.chat-message.assistant .message-text {
  background: white;
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 14px 14px 14px 4px;
}

.chat-message.user .message-text {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-radius: 14px 14px 4px 14px;
}

.message-text :deep(strong) {
  color: #4a6f9d;
  font-weight: 600;
}

.message-time {
  font-size: 10px;
  color: #64748b;
  margin-top: 4px;
  opacity: 0.7;
}

.chat-message.user .message-time {
  text-align: right;
}

.typing-dots {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: white;
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 14px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: #6672cb;
  border-radius: 50%;
  animation: bounce 1s ease-in-out infinite;
}

.typing-dots span:nth-child(2) { animation-delay: 0.15s; }
.typing-dots span:nth-child(3) { animation-delay: 0.3s; }

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.quick-prompts {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.prompt-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 16px;
  font-size: 12px;
  color: #4a6f9d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.prompt-btn:hover {
  background: white;
  border-color: rgba(74, 111, 157, 0.3);
}

.chat-input-area {
  display: flex;
  gap: 10px;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  background: white;
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 12px;
  font-size: 13px;
  color: #334155;
  outline: none;
  transition: all 0.2s ease;
}

.chat-input::placeholder {
  color: #64748b;
}

.chat-input:focus {
  border-color: #4a6f9d;
  box-shadow: 0 0 0 3px rgba(74, 111, 157, 0.1);
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(74, 111, 157, 0.25);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .helper-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .section-tabs {
    width: 100%;
    overflow-x: auto;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
