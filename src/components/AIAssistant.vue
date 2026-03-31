<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const activeSection = ref('overview')
const chatMessages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
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

const generateLearningPlan = async (preferences = {}) => {
  const today = new Date()
  learningPlan.value = {
    weekGoals: [
      { id: 1, title: '掌握动态规划基础', progress: 0, target: 5 },
      { id: 2, title: '完成图论入门', progress: 0, target: 3 },
      { id: 3, title: '每日坚持练习', progress: 0, target: 7 },
    ],
    dailyTasks: [
      { day: '今天', tasks: [{ title: '背包问题入门', duration: 30, type: 'learn' }] },
      { day: '明天', tasks: [{ title: 'DP练习题2道', duration: 45, type: 'practice' }] },
      { day: '后天', tasks: [{ title: '错题复盘', duration: 20, type: 'review' }] },
    ],
    recommendations: [
      { type: 'video', title: '动态规划入门讲解', source: '推荐' },
      { type: 'article', title: '背包问题详解', source: '必读' },
    ],
    generatedAt: today.toISOString(),
  }
  return learningPlan.value
}

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

  await new Promise(r => setTimeout(r, 600))

  const response = generateResponse(message)
  chatMessages.value.push({
    role: 'assistant',
    content: response,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })

  isTyping.value = false
  await nextTick()
  scrollToBottom()
}

const generateResponse = (input) => {
  if (input.includes('错题') || input.includes('分析')) {
    return `根据你的做题记录，我发现：

**主要问题集中在：**
- 动态规划（8次错误）- 状态转移方程定义不准确
- 图论（6次错误）- 边界条件容易遗漏

**建议改进方向：**
1. 做题前先画状态转移图
2. 写完代码后检查边界情况
3. 每天复习一道错题

需要我帮你制定针对性的练习计划吗？`
  }
  if (input.includes('计划') || input.includes('学习')) {
    return `好的，这是为你定制的本周学习计划：

**周一至周三：动态规划突破**
- 每天完成2道DP入门题
- 重点理解状态定义和转移

**周四至周五：图论基础**
- BFS/DFS模板练习
- 最短路径入门

**周末：综合练习**
- 参加周赛模拟
- 整理错题笔记

你觉得这个安排怎么样？`
  }
  return `我理解你的问题。让我想想...

根据你的学习数据，我建议：
- 先从基础概念入手
- 循序渐进提升难度
- 保持每日练习的习惯

有什么具体想了解的吗？`
}

const scrollToBottom = () => {
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
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
  chatMessages.value.push({
    role: 'assistant',
    content: `你好，${userStore.userInfo?.name || '同学'}！👋

我是你的学习小助手，可以帮你：
- 📊 分析错题，找出薄弱点
- 📋 制定个性化学习计划
- 💡 推荐适合的练习题目

有什么我可以帮你的吗？`,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
})
</script>

<template>
  <section class="study-helper">
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
          <p>根据你的情况定制</p>
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
          <button class="plan-btn primary" @click="generateLearningPlan()">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6a2 2 0 0 0-2 2z"/>
            </svg>
            保存计划
          </button>
          <button class="plan-btn secondary" @click="generateLearningPlan()">重新生成</button>
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
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

.study-helper {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  background: linear-gradient(135deg, #fefce8 0%, #fef9c3 30%, #fef08a 100%);
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(234, 179, 8, 0.2);
  box-shadow: 0 8px 32px rgba(234, 179, 8, 0.12);
}

.helper-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(180deg, rgba(255,255,255,0.8) 0%, rgba(254,249,195,0.6) 100%);
  border-bottom: 1px solid rgba(234, 179, 8, 0.15);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.helper-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.header-info h3 {
  font-size: 18px;
  font-weight: 700;
  color: #78350f;
  margin: 0;
}

.header-info p {
  font-size: 12px;
  color: #a16207;
  margin: 2px 0 0;
}

.section-tabs {
  display: flex;
  gap: 6px;
}

.tab {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(234, 179, 8, 0.2);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #92400e;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.9);
}

.tab.active {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
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
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(234, 179, 8, 0.15);
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
  background: #d1fae5;
  color: #059669;
}

.stat-icon.score {
  background: #dbeafe;
  color: #2563eb;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #78350f;
}

.stat-label {
  font-size: 12px;
  color: #a16207;
}

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
  border: 1px solid rgba(234, 179, 8, 0.15);
}

.overview-card h4 {
  font-size: 14px;
  font-weight: 600;
  color: #78350f;
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
  color: #92400e;
}

.topic-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.topic-item.weak .topic-dot {
  background: #f59e0b;
}

.topic-item.strong .topic-dot {
  background: #10b981;
}

.empty-hint {
  color: #a16207;
  font-size: 13px;
  opacity: 0.7;
  text-align: center;
  padding: 12px;
}

.quick-actions h4 {
  font-size: 14px;
  font-weight: 600;
  color: #78350f;
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
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(234, 179, 8, 0.2);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #78350f;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: white;
  border-color: rgba(234, 179, 8, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(234, 179, 8, 0.15);
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
  color: #78350f;
  margin: 0;
}

.section-title p {
  font-size: 13px;
  color: #a16207;
  margin: 4px 0 0;
}

.error-categories {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(234, 179, 8, 0.15);
  margin-bottom: 16px;
}

.error-categories h4 {
  font-size: 14px;
  font-weight: 600;
  color: #78350f;
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
  color: #78350f;
  font-weight: 500;
}

.bar-value {
  color: #a16207;
}

.bar-track {
  height: 8px;
  background: rgba(234, 179, 8, 0.15);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24 0%, #f59e0b 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.recent-errors {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(234, 179, 8, 0.15);
}

.recent-errors h4 {
  font-size: 14px;
  font-weight: 600;
  color: #78350f;
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
  background: #fef3c7;
  border-radius: 10px;
  border-left: 3px solid #f59e0b;
}

.error-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.error-title {
  font-size: 13px;
  font-weight: 500;
  color: #78350f;
}

.error-reason {
  font-size: 11px;
  color: #a16207;
}

.error-time {
  font-size: 11px;
  color: #a16207;
}

.week-goals {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(234, 179, 8, 0.15);
  margin-bottom: 16px;
}

.week-goals h4 {
  font-size: 14px;
  font-weight: 600;
  color: #78350f;
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
  color: #a16207;
  margin-top: 2px;
}

.goal-content {
  flex: 1;
}

.goal-title {
  font-size: 13px;
  font-weight: 500;
  color: #78350f;
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
  background: rgba(234, 179, 8, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24 0%, #f59e0b 100%);
  border-radius: 3px;
}

.progress-text {
  font-size: 11px;
  color: #a16207;
  min-width: 40px;
}

.generate-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.daily-tasks {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 16px;
  border: 1px solid rgba(234, 179, 8, 0.15);
  margin-bottom: 16px;
}

.daily-tasks h4 {
  font-size: 14px;
  font-weight: 600;
  color: #78350f;
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
  color: #a16207;
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
  border: 1px solid rgba(234, 179, 8, 0.15);
  border-radius: 10px;
}

.task-type {
  padding: 2px 8px;
  background: #fef3c7;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  color: #92400e;
}

.task-title {
  flex: 1;
  font-size: 13px;
  color: #78350f;
}

.task-duration {
  font-size: 11px;
  color: #a16207;
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
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.plan-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 158, 11, 0.4);
}

.plan-btn.secondary {
  background: white;
  border: 1px solid rgba(234, 179, 8, 0.3);
  color: #78350f;
}

.plan-btn.secondary:hover {
  background: #fefce8;
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
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  margin-bottom: 12px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(234, 179, 8, 0.3);
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
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.chat-message.user .message-avatar {
  background: #fef3c7;
  color: #78350f;
}

.message-body {
  flex: 1;
}

.message-text {
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.6;
  color: #451a03;
}

.chat-message.assistant .message-text {
  background: white;
  border: 1px solid rgba(234, 179, 8, 0.2);
  border-radius: 14px 14px 14px 4px;
}

.chat-message.user .message-text {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 14px 14px 4px 14px;
}

.message-text :deep(strong) {
  color: #92400e;
  font-weight: 600;
}

.message-time {
  font-size: 10px;
  color: #a16207;
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
  border: 1px solid rgba(234, 179, 8, 0.2);
  border-radius: 14px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: #f59e0b;
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
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(234, 179, 8, 0.2);
  border-radius: 16px;
  font-size: 12px;
  color: #78350f;
  cursor: pointer;
  transition: all 0.2s ease;
}

.prompt-btn:hover {
  background: white;
  border-color: rgba(234, 179, 8, 0.4);
}

.chat-input-area {
  display: flex;
  gap: 10px;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  background: white;
  border: 1px solid rgba(234, 179, 8, 0.2);
  border-radius: 12px;
  font-size: 13px;
  color: #451a03;
  outline: none;
  transition: all 0.2s ease;
}

.chat-input::placeholder {
  color: #a16207;
}

.chat-input:focus {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.1);
}

.send-btn {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
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
