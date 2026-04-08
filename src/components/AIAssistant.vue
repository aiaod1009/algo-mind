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
const isStreaming = ref(false)
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

const normalizeMessageContent = (content) => {
  if (typeof content === 'string') {
    return content.length ? content : 'AI did not return any content. Please try again later.'
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

const renderMessageContent = (content) => escapeHtml(content)
  .replace(/\n/g, '<br>')
  .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')

const extractAIResponseContent = (response, fallbackInput) => {
  const content = response?.data?.data?.content
  return normalizeMessageContent(content || fallbackInput)
}

const loadErrorAnalysis = async () => {
  try {
    const response = await api.get('/api/ai/error-analysis')
    if (response.data?.data) {
      errorAnalysis.value = response.data.data
    }
  } catch (error) {
    errorAnalysis.value = {
      totalErrors: 23,
      categories: [
        { name: '动态规划', count: 8 },
        { name: '图论', count: 6 },
        { name: '二叉树', count: 5 },
        { name: '字符串', count: 4 },
      ],
      recentErrors: [
        { title: '背包问题', reason: '状态转移方程错误', time: '2小时前' },
        { title: '最短路径', reason: '边界条件遗漏', time: '昨天' },
        { title: '二叉树遍历', reason: '递归终止条件错误', time: '2天前' },
      ],
      improvementTrend: '近期在动态规划方面有进步，但图论仍需加强',
    }
  }
}

const loadStudyHabits = async () => {
  try {
    const response = await api.get('/api/ai/study-habits')
    if (response.data?.data) {
      studyHabits.value = response.data.data
    }
  } catch (error) {
    studyHabits.value = {
      weeklyStudyTime: 12.5,
      averageTimePerQuestion: 25,
      preferredTimeSlot: '晚上',
      consistencyScore: 78,
      strongTopics: ['数组', '字符串', '链表'],
      weakTopics: ['动态规划', '图论'],
    }
  }
}

const generateLearningPlan = async () => {
  if (isGenerating.value) return

  isGenerating.value = true
  isGenerated.value = false

  try {
    const response = await api.post('/api/ai/learning-plan', {
      track: props.selectedTrack,
      weeklyGoal: props.weeklyGoal,
    })

    if (response.data?.data) {
      learningPlan.value = response.data.data
      isGenerated.value = true
    }
  } catch (error) {
    learningPlan.value = {
      weekGoals: [
        { title: '基础巩固练习', target: 4, current: 0 },
        { title: '新知识点学习', target: 3, current: 0 },
        { title: '算法思维强化', target: 3, current: 0 },
        { title: '每日坚持打卡', target: 1, current: 0 },
      ],
      dailyTasks: [
        { day: '今天', tasks: [{ type: '学习', title: '动态规划思想理解', duration: '30分钟' }] },
        { day: '明天', tasks: [
          { type: '复习', title: '基础概念复习', duration: '30分钟' },
          { type: '练习', title: '简单练习题', duration: '25分钟' },
        ]},
        { day: '后天', tasks: [
          { type: '复习', title: '本周知识点总结', duration: '30分钟' },
          { type: '练习', title: '综合模拟测试', duration: '40分钟' },
        ]},
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
  } else if (section === 'plan' && !learningPlan.value.generatedAt && !isGenerating.value) {
    generateLearningPlan()
  }
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

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || isTyping.value) return

  chatMessages.value.push({
    role: 'user',
    content: message,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })

  inputMessage.value = ''
  isTyping.value = true
  isStreaming.value = true

  await scrollToBottom()

  try {
    const response = await api.post('/api/ai/chat', {
      message,
      context: {
        track: props.selectedTrack,
        weeklyGoal: props.weeklyGoal,
      },
    })

    const aiContent = extractAIResponseContent(response, '抱歉，我现在无法回答，请稍后再试。')

    chatMessages.value.push({
      role: 'assistant',
      content: aiContent,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  } catch (error) {
    chatMessages.value.push({
      role: 'assistant',
      content: '抱歉，服务暂时不可用，请稍后再试。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  } finally {
    isTyping.value = false
    isStreaming.value = false
    await scrollToBottom()
  }
}

const useQuickPrompt = (prompt) => {
  inputMessage.value = prompt.text
  sendMessage()
}

onMounted(() => {
  loadErrorAnalysis()
  loadStudyHabits()

  chatMessages.value.push({
    role: 'assistant',
    content: `你好，${userStore.userInfo?.name || '同学'}！👋\n\n我是你的算法学习助手，可以帮你：\n- 📊 分析错题，找出薄弱点\n- 📋 制定个性化学习计划\n- 💡 推荐适合的练习题目\n\n当前赛道：**${props.trackOptions.find(t => t.value === props.selectedTrack)?.label || '算法思维赛道'}**\n周目标：**${props.weeklyGoal} 个关卡**\n\n有什么我可以帮你的吗？`,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
})

watch(activeSection, async (newSection) => {
  if (newSection === 'chat') {
    await scrollToBottom()
  }
})
</script>

<template>
  <section class="study-helper" @click="scrollToTop">
    <!-- 柔和渐变背景 -->
    <div class="soft-gradient">
      <div class="gradient-blob blob-1"></div>
      <div class="gradient-blob blob-2"></div>
      <div class="gradient-blob blob-3"></div>
    </div>

    <div class="helper-header">
      <div class="header-left">
        <div class="helper-icon">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
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
        <button
          v-for="tab in [
            { key: 'overview', label: '总览' },
            { key: 'errors', label: '错题' },
            { key: 'plan', label: '计划' },
            { key: 'chat', label: '对话' },
          ]"
          :key="tab.key"
          class="tab"
          :class="{ active: activeSection === tab.key }"
          @click.stop="switchSection(tab.key)"
        >
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
          <div class="overview-card weak-card">
            <h4>⚠️ 薄弱环节</h4>
            <div class="topic-list">
              <div v-for="topic in studyHabits.weakTopics" :key="topic" class="topic-item weak">
                <span class="topic-dot"></span>
                {{ topic }}
              </div>
              <div v-if="!studyHabits.weakTopics.length" class="empty-hint">暂无数据</div>
            </div>
          </div>
          <div class="overview-card strong-card">
            <h4>✨ 擅长领域</h4>
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
          <h4>🚀 快速开始</h4>
          <div class="action-buttons">
            <button class="action-btn" @click.stop="switchSection('errors')">
              <span class="action-icon">📊</span>
              <span>查看错题分析</span>
            </button>
            <button class="action-btn" @click.stop="switchSection('plan')">
              <span class="action-icon">📋</span>
              <span>生成学习计划</span>
            </button>
            <button class="action-btn" @click.stop="switchSection('chat')">
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
          <h4>错误类型分布</h4>
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
          <h4>最近错题</h4>
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
            <h4>本周目标</h4>
            <div class="goal-list">
              <div v-for="goal in learningPlan.weekGoals" :key="goal.title" class="goal-item">
                <div class="goal-check">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                  </svg>
                </div>
                <div class="goal-content">
                  <span class="goal-title">{{ goal.title }}</span>
                  <div class="goal-progress">
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: `${(goal.current / goal.target) * 100}%` }"></div>
                    </div>
                    <span class="progress-text">{{ goal.current }}/{{ goal.target }}</span>
                  </div>
                </div>
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
                    <span class="task-type">{{ task.type }}</span>
                    <span class="task-title">{{ task.title }}</span>
                    <span class="task-duration">{{ task.duration }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="plan-actions">
            <button class="plan-btn primary" @click.stop="saveLearningPlan">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6a2 2 0 0 0-2 2z"/>
              </svg>
              保存计划
            </button>
            <button class="plan-btn secondary" @click.stop="generateLearningPlan">
              重新生成
            </button>
          </div>
        </div>

        <div v-else class="generate-prompt">
          <button class="generate-btn" @click.stop="generateLearningPlan">
            <span class="btn-icon">✨</span>
            生成学习计划
          </button>
        </div>
      </div>

      <!-- 对话 -->
      <div v-if="activeSection === 'chat'" class="chat-section">
        <div ref="chatContainerRef" class="chat-messages">
          <div
            v-for="(msg, index) in chatMessages"
            :key="index"
            class="chat-message"
            :class="msg.role"
          >
            <div class="message-avatar">
              <span>{{ msg.role === 'assistant' ? 'AI' : '我' }}</span>
            </div>
            <div class="message-body">
              <div class="message-text" v-html="renderMessageContent(msg.content)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="isTyping" class="chat-message assistant typing">
            <div class="message-avatar">
              <span>AI</span>
            </div>
            <div class="message-body">
              <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="quick-prompts">
          <button
            v-for="prompt in quickPrompts"
            :key="prompt.text"
            class="prompt-btn"
            @click.stop="useQuickPrompt(prompt)"
          >
            <span>{{ prompt.icon }}</span>
            {{ prompt.text }}
          </button>
        </div>

        <div class="chat-input-area">
          <input
            v-model="inputMessage"
            type="text"
            class="chat-input"
            placeholder="输入你的问题..."
            @keyup.enter="sendMessage"
            @click.stop
          />
          <button
            class="send-btn"
            :disabled="!inputMessage.trim() || isTyping"
            @click.stop="sendMessage"
          >
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
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

/* 明亮科技蓝主题 */
.study-helper {
  position: relative;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 50%, #f0f9ff 100%);
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(14, 165, 233, 0.2);
  box-shadow:
    0 4px 24px rgba(14, 165, 233, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.study-helper:hover {
  border-color: rgba(14, 165, 233, 0.4);
  box-shadow:
    0 8px 40px rgba(14, 165, 233, 0.15),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
}

/* 柔和渐变背景 */
.soft-gradient {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.gradient-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.blob-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #7dd3fc 0%, #38bdf8 100%);
  top: -150px;
  right: -100px;
  animation: blobFloat 10s ease-in-out infinite;
}

.blob-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #c4b5fd 0%, #a78bfa 100%);
  bottom: -100px;
  left: -100px;
  animation: blobFloat 10s ease-in-out infinite reverse;
}

.blob-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #fbcfe8 0%, #f9a8d4 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.2;
  animation: blobFloat 15s ease-in-out infinite;
}

@keyframes blobFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, -30px) scale(1.1); }
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
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
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
}

.weak-card {
  border-left: 4px solid #ef4444;
}

.strong-card {
  border-left: 4px solid #10b981;
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
  height: 300px;
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
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
  color: #334155;
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

/* 输入区域 */
.chat-input-area {
  display: flex;
  gap: 12px;
}

.chat-input {
  flex: 1;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(14, 165, 233, 0.2);
  border-radius: 14px;
  font-size: 14px;
  color: #334155;
  outline: none;
  transition: all 0.3s ease;
}

.chat-input::placeholder {
  color: #94a3b8;
}

.chat-input:focus {
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
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
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
</style>
