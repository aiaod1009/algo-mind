<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { useLevelStore } from '../stores/level'

const userStore = useUserStore()
const levelStore = useLevelStore()

const chatMessages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const chatContainerRef = ref(null)
const activeTab = ref('chat')

const wrongQuestions = ref([
  { id: 1, title: '动态规划-背包问题', errorType: '状态转移错误', count: 3, lastError: '2024-01-15' },
  { id: 2, title: '二叉树遍历', errorType: '边界条件遗漏', count: 2, lastError: '2024-01-14' },
  { id: 3, title: '图的最短路径', errorType: '算法选择不当', count: 4, lastError: '2024-01-13' },
  { id: 4, title: '字符串匹配', errorType: '复杂度分析错误', count: 1, lastError: '2024-01-12' },
])

const habits = ref({
  avgTimePerQuestion: 12.5,
  preferredTime: '晚间 20:00-22:00',
  weakAreas: ['动态规划', '图论'],
  strongAreas: ['数组', '字符串'],
  streakDays: 7,
  weeklyTrend: 'up',
})

const aiSuggestions = ref([
  { icon: '🎯', title: '重点突破动态规划', desc: '根据错题分析，建议本周完成3道DP专题训练' },
  { icon: '⏰', title: '优化做题节奏', desc: '平均用时偏长，建议先完成思路梳理再编码' },
  { icon: '🔥', title: '保持连续学习', desc: '已连续学习7天，继续保持！' },
])

const quickActions = [
  { label: '分析我的错题', action: 'analyze_errors' },
  { label: '生成学习计划', action: 'generate_plan' },
  { label: '推荐练习题目', action: 'recommend' },
  { label: '解释这个概念', action: 'explain' },
]

const predefinedResponses = {
  analyze_errors: `📊 **错题分析报告**

根据您最近的做题记录，我发现以下规律：

**高频错误类型：**
1. **状态转移错误** (占比40%) - 主要出现在DP问题中
2. **边界条件遗漏** (占比30%) - 树和图的遍历问题
3. **算法选择不当** (占比20%) - 复杂问题拆解不足

**建议改进方向：**
- 写代码前先画状态转移图
- 养成检查边界条件的习惯
- 多练习算法复杂度分析

需要我针对某个具体问题详细讲解吗？`,
  generate_plan: `📅 **本周个性化学习计划**

**周一-周二：巩固基础**
- 完成数组专题2题（预计用时30分钟）
- 复习二分查找模板

**周三-周四：重点突破**
- 动态规划专题训练3题
- 观看DP状态定义讲解视频

**周五-周六：综合提升**
- 完成1道中等难度综合题
- 参与周赛模拟

**周日：复盘总结**
- 整理本周错题笔记
- 预习下周内容

是否需要我调整计划难度或时间安排？`,
  recommend: `🎯 **为您推荐的练习题目**

**适合当前水平：**
1. **LeetCode 300. 最长递增子序列** - DP入门经典
2. **LeetCode 1143. 最长公共子序列** - 二维DP练习
3. **LeetCode 139. 单词拆分** - DP+记忆化

**挑战提升：**
4. **LeetCode 322. 零钱兑换** - 完全背包变体
5. **LeetCode 72. 编辑距离** - 经典DP难题

每道题都配有详细的思路引导，需要我讲解哪一题？`,
  explain: `💡 我可以为您解释各种算法概念：

**当前推荐：动态规划核心思想**

动态规划的本质是将复杂问题分解为重叠子问题，通过存储子问题的解来避免重复计算。

**三要素：**
1. **最优子结构** - 问题的最优解包含子问题的最优解
2. **重叠子问题** - 子问题会被多次计算
3. **状态转移方程** - 描述子问题之间的关系

您想深入了解哪个方面？或者有其他概念需要解释？`,
}

const handleQuickAction = (action) => {
  inputMessage.value = quickActions.find(a => a.action === action)?.label || ''
  sendMessage(action)
}

const sendMessage = async (quickAction = null) => {
  const messageText = inputMessage.value.trim()
  if (!messageText && !quickAction) return

  const action = quickAction || quickActions.find(a => a.label === messageText)?.action

  if (messageText && !quickAction) {
    chatMessages.value.push({
      role: 'user',
      content: messageText,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    })
  }

  inputMessage.value = ''
  isTyping.value = true

  await nextTick()
  scrollToBottom()

  await new Promise(resolve => setTimeout(resolve, 800 + Math.random() * 700))

  let response = ''
  if (action && predefinedResponses[action]) {
    response = predefinedResponses[action]
  } else {
    response = generateAIResponse(messageText)
  }

  chatMessages.value.push({
    role: 'assistant',
    content: response,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })

  isTyping.value = false
  await nextTick()
  scrollToBottom()
}

const generateAIResponse = (input) => {
  const responses = [
    `我理解您的问题："${input}"。\n\n让我为您分析一下...根据您的学习数据，我建议您先从基础概念入手，然后逐步提升难度。需要我提供更具体的指导吗？`,
    `这是一个很好的问题！\n\n"${input}"\n\n根据您的做题习惯分析，我建议采用"先思考后编码"的方式。您可以先尝试用伪代码描述思路，然后再实现。`,
    `关于"${input}"，我注意到您在这个领域还有一些提升空间。\n\n要不要我为您生成一份针对性的练习计划？`,
  ]
  return responses[Math.floor(Math.random() * responses.length)]
}

const scrollToBottom = () => {
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

const habitScore = computed(() => {
  let score = 50
  if (habits.value.streakDays >= 7) score += 20
  if (habits.value.weeklyTrend === 'up') score += 15
  if (habits.value.avgTimePerQuestion < 15) score += 15
  return Math.min(100, score)
})

onMounted(() => {
  chatMessages.value.push({
    role: 'assistant',
    content: `👋 你好，${userStore.userInfo?.name || '同学'}！\n\n我是你的AI学习助手，我可以帮你：\n\n• 📊 分析错题原因和薄弱环节\n• 📅 生成个性化学习计划\n• 🎯 推荐适合的练习题目\n• 💡 解释算法概念和解题思路\n\n有什么我可以帮助你的吗？`,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
  })
})
</script>

<template>
  <section class="ai-assistant">
    <div class="assistant-header">
      <div class="header-glow"></div>
      <div class="header-content">
        <div class="ai-avatar">
          <div class="avatar-ring">
            <svg class="ring-svg" viewBox="0 0 100 100">
              <defs>
                <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#63b3ed" />
                  <stop offset="50%" stop-color="#805ad5" />
                  <stop offset="100%" stop-color="#63b3ed" />
                </linearGradient>
              </defs>
              <circle class="ring-track" cx="50" cy="50" r="45" />
              <circle class="ring-progress" cx="50" cy="50" r="45" />
            </svg>
          </div>
          <div class="avatar-core">
            <svg class="brain-icon" viewBox="0 0 32 32" width="28" height="28">
              <defs>
                <linearGradient id="brainGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#63b3ed" />
                  <stop offset="50%" stop-color="#805ad5" />
                  <stop offset="100%" stop-color="#63b3ed" />
                </linearGradient>
              </defs>
              <g fill="none" stroke="url(#brainGrad)" stroke-width="1.5" stroke-linecap="round">
                <circle cx="16" cy="10" r="3" class="node node-1" />
                <circle cx="8" cy="16" r="2.5" class="node node-2" />
                <circle cx="24" cy="16" r="2.5" class="node node-3" />
                <circle cx="12" cy="22" r="2" class="node node-4" />
                <circle cx="20" cy="22" r="2" class="node node-5" />
                <circle cx="16" cy="16" r="4" class="node node-center" />
                <path d="M16 10 L16 12" class="connection c1" />
                <path d="M8 16 L12 16" class="connection c2" />
                <path d="M24 16 L20 16" class="connection c3" />
                <path d="M12 22 L14 18" class="connection c4" />
                <path d="M20 22 L18 18" class="connection c5" />
                <path d="M14 14 L12 22" class="connection c6" opacity="0.5" />
                <path d="M18 14 L20 22" class="connection c7" opacity="0.5" />
              </g>
            </svg>
            <div class="pulse-rings">
              <span class="pulse-ring"></span>
              <span class="pulse-ring"></span>
            </div>
          </div>
        </div>
        <div class="header-text">
          <h3>AI 学习助手</h3>
          <p>智能分析 · 个性化指导</p>
        </div>
      </div>
      <div class="header-tabs">
        <button :class="['tab-btn', { active: activeTab === 'chat' }]" @click="activeTab = 'chat'">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          对话
        </button>
        <button :class="['tab-btn', { active: activeTab === 'analysis' }]" @click="activeTab = 'analysis'">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 21l-6-6m2-5a7 7 0 1 1-14 0 7 7 0 0 1 14 0z"/>
          </svg>
          分析
        </button>
        <button :class="['tab-btn', { active: activeTab === 'plan' }]" @click="activeTab = 'plan'">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          计划
        </button>
      </div>
    </div>

    <div class="assistant-body">
      <div v-show="activeTab === 'chat'" class="chat-panel">
        <div ref="chatContainerRef" class="chat-messages">
          <div v-for="(msg, idx) in chatMessages" :key="idx" :class="['message', msg.role]">
            <div class="message-avatar">
              <template v-if="msg.role === 'assistant'">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="10"/>
                  <circle cx="12" cy="12" r="3"/>
                  <path d="M12 2v4m0 12v4M2 12h4m12 0h4"/>
                </svg>
              </template>
              <template v-else>
                <span>{{ (userStore.userInfo?.name || '同学').slice(0, 1) }}</span>
              </template>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="msg.content.replace(/\n/g, '<br>').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="isTyping" class="message assistant">
            <div class="message-avatar">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <div class="quick-actions">
          <button v-for="action in quickActions" :key="action.action" class="quick-btn" @click="handleQuickAction(action.action)">
            {{ action.label }}
          </button>
        </div>

        <div class="chat-input-wrap">
          <input v-model="inputMessage" type="text" class="chat-input" placeholder="输入你的问题..." @keyup.enter="sendMessage()" />
          <button class="send-btn" :disabled="!inputMessage.trim() && !isTyping" @click="sendMessage()">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>

      <div v-show="activeTab === 'analysis'" class="analysis-panel">
        <div class="analysis-grid">
          <div class="analysis-card habit-card">
            <div class="card-header">
              <h4>学习习惯评分</h4>
              <div class="score-badge">{{ habitScore }}分</div>
            </div>
            <div class="habit-stats">
              <div class="stat-row">
                <span class="stat-label">平均做题时间</span>
                <span class="stat-value">{{ habits.avgTimePerQuestion }}分钟</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">最佳学习时段</span>
                <span class="stat-value">{{ habits.preferredTime }}</span>
              </div>
              <div class="stat-row">
                <span class="stat-label">连续学习天数</span>
                <span class="stat-value highlight">{{ habits.streakDays }}天 🔥</span>
              </div>
            </div>
            <div class="habit-areas">
              <div class="area-item">
                <span class="area-label">优势领域</span>
                <div class="area-tags strong">
                  <span v-for="area in habits.strongAreas" :key="area" class="tag">{{ area }}</span>
                </div>
              </div>
              <div class="area-item">
                <span class="area-label">待提升领域</span>
                <div class="area-tags weak">
                  <span v-for="area in habits.weakAreas" :key="area" class="tag">{{ area }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="analysis-card errors-card">
            <div class="card-header">
              <h4>错题分析</h4>
              <span class="error-count">{{ wrongQuestions.length }}类问题</span>
            </div>
            <div class="error-list">
              <div v-for="item in wrongQuestions" :key="item.id" class="error-item">
                <div class="error-info">
                  <div class="error-title">{{ item.title }}</div>
                  <div class="error-type">{{ item.errorType }}</div>
                </div>
                <div class="error-meta">
                  <span class="error-count-badge">{{ item.count }}次</span>
                </div>
              </div>
            </div>
          </div>

          <div class="analysis-card suggestions-card">
            <div class="card-header">
              <h4>AI 建议</h4>
            </div>
            <div class="suggestions-list">
              <div v-for="(suggestion, idx) in aiSuggestions" :key="idx" class="suggestion-item">
                <span class="suggestion-icon">{{ suggestion.icon }}</span>
                <div class="suggestion-content">
                  <div class="suggestion-title">{{ suggestion.title }}</div>
                  <div class="suggestion-desc">{{ suggestion.desc }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'plan'" class="plan-panel">
        <div class="plan-header">
          <h4>AI 生成的学习计划</h4>
          <p>基于你的学习数据和目标智能生成</p>
        </div>
        <div class="plan-timeline">
          <div class="timeline-item">
            <div class="timeline-marker">
              <span class="marker-day">周一</span>
            </div>
            <div class="timeline-content">
              <div class="plan-task">
                <span class="task-icon">📚</span>
                <div class="task-info">
                  <div class="task-title">动态规划基础</div>
                  <div class="task-desc">完成斐波那契、爬楼梯等入门题目</div>
                </div>
                <span class="task-time">30分钟</span>
              </div>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">
              <span class="marker-day">周二</span>
            </div>
            <div class="timeline-content">
              <div class="plan-task">
                <span class="task-icon">🎯</span>
                <div class="task-info">
                  <div class="task-title">背包问题专题</div>
                  <div class="task-desc">0-1背包、完全背包模板练习</div>
                </div>
                <span class="task-time">45分钟</span>
              </div>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">
              <span class="marker-day">周三</span>
            </div>
            <div class="timeline-content">
              <div class="plan-task">
                <span class="task-icon">🔄</span>
                <div class="task-info">
                  <div class="task-title">错题复盘</div>
                  <div class="task-desc">重做本周错题，总结错误原因</div>
                </div>
                <span class="task-time">30分钟</span>
              </div>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">
              <span class="marker-day">周四</span>
            </div>
            <div class="timeline-content">
              <div class="plan-task">
                <span class="task-icon">🧩</span>
                <div class="task-info">
                  <div class="task-title">图论基础</div>
                  <div class="task-desc">BFS、DFS遍历与最短路径入门</div>
                </div>
                <span class="task-time">40分钟</span>
              </div>
            </div>
          </div>
          <div class="timeline-item">
            <div class="timeline-marker">
              <span class="marker-day">周五</span>
            </div>
            <div class="timeline-content">
              <div class="plan-task highlight">
                <span class="task-icon">🏆</span>
                <div class="task-info">
                  <div class="task-title">周赛模拟</div>
                  <div class="task-desc">限时完成3道题目，模拟真实比赛</div>
                </div>
                <span class="task-time">90分钟</span>
              </div>
            </div>
          </div>
        </div>
        <div class="plan-actions">
          <button class="plan-btn primary">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6a2 2 0 0 0-2 2z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            保存计划
          </button>
          <button class="plan-btn secondary">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="23 4 23 10 17 10"/>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
            重新生成
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600&family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

.ai-assistant {
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 50%, #f1f5f9 100%);
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  font-family: 'Noto Sans SC', sans-serif;
  border: 1px solid rgba(99, 179, 237, 0.15);
}

.ai-assistant::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: radial-gradient(ellipse at 50% 0%, rgba(99, 179, 237, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

.assistant-header {
  position: relative;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(99, 179, 237, 0.15);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.header-glow {
  position: absolute;
  top: -50px;
  left: 50%;
  transform: translateX(-50%);
  width: 400px;
  height: 120px;
  background: radial-gradient(ellipse, rgba(99, 179, 237, 0.12) 0%, rgba(128, 90, 213, 0.08) 40%, transparent 70%);
  pointer-events: none;
  animation: glowPulse 4s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.6; transform: translateX(-50%) scale(1); }
  50% { opacity: 1; transform: translateX(-50%) scale(1.1); }
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-avatar {
  position: relative;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #e0f2fe 0%, #ede9fe 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 4px 16px rgba(99, 179, 237, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  overflow: visible;
  border: 1px solid rgba(99, 179, 237, 0.2);
}

.avatar-ring {
  position: absolute;
  inset: -4px;
  border-radius: 20px;
  pointer-events: none;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-track {
  fill: none;
  stroke: rgba(99, 179, 237, 0.1);
  stroke-width: 2;
}

.ring-progress {
  fill: none;
  stroke: url(#ringGrad);
  stroke-width: 2;
  stroke-dasharray: 283;
  stroke-dashoffset: 283;
  stroke-linecap: round;
  animation: ringDraw 3s ease-in-out infinite;
}

@keyframes ringDraw {
  0%, 100% { 
    stroke-dashoffset: 283; 
    opacity: 0.3;
  }
  50% { 
    stroke-dashoffset: 70; 
    opacity: 1;
  }
}

.avatar-core {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brain-icon {
  position: relative;
  z-index: 2;
}

.brain-icon .node {
  fill: rgba(99, 179, 237, 0.2);
  animation: nodeGlow 2s ease-in-out infinite;
}

.brain-icon .node-center {
  fill: rgba(128, 90, 213, 0.3);
  animation: nodeGlow 2s ease-in-out infinite 0.3s;
}

.brain-icon .node-1 { animation-delay: 0s; }
.brain-icon .node-2 { animation-delay: 0.1s; }
.brain-icon .node-3 { animation-delay: 0.2s; }
.brain-icon .node-4 { animation-delay: 0.3s; }
.brain-icon .node-5 { animation-delay: 0.4s; }

@keyframes nodeGlow {
  0%, 100% { 
    opacity: 0.6;
    filter: drop-shadow(0 0 2px rgba(99, 179, 237, 0.5));
  }
  50% { 
    opacity: 1;
    filter: drop-shadow(0 0 6px rgba(99, 179, 237, 0.8));
  }
}

.brain-icon .connection {
  stroke-dasharray: 20;
  stroke-dashoffset: 20;
  animation: connectionFlow 2.5s ease-in-out infinite;
}

.brain-icon .c1 { animation-delay: 0s; }
.brain-icon .c2 { animation-delay: 0.15s; }
.brain-icon .c3 { animation-delay: 0.3s; }
.brain-icon .c4 { animation-delay: 0.45s; }
.brain-icon .c5 { animation-delay: 0.6s; }
.brain-icon .c6 { animation-delay: 0.75s; }
.brain-icon .c7 { animation-delay: 0.9s; }

@keyframes connectionFlow {
  0%, 100% { 
    stroke-dashoffset: 20;
    opacity: 0.3;
  }
  50% { 
    stroke-dashoffset: 0;
    opacity: 1;
  }
}

.pulse-rings {
  position: absolute;
  inset: -8px;
  pointer-events: none;
}

.pulse-ring {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(99, 179, 237, 0.4);
  border-radius: 20px;
  animation: pulseExpand 3s ease-out infinite;
}

.pulse-ring:nth-child(2) {
  animation-delay: 1.5s;
}

@keyframes pulseExpand {
  0% {
    transform: scale(1);
    opacity: 0.6;
  }
  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}

.header-text h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: 0.5px;
}

.header-text p {
  font-size: 13px;
  color: #64748b;
  margin: 4px 0 0;
}

.header-tabs {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8fafc;
  border: 1px solid rgba(99, 179, 237, 0.15);
  border-radius: 10px;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  background: #f1f5f9;
  color: #475569;
  border-color: rgba(99, 179, 237, 0.3);
}

.tab-btn.active {
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border-color: rgba(99, 179, 237, 0.4);
  color: #2563eb;
}

.assistant-body {
  position: relative;
  min-height: 480px;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  height: 480px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: 
    radial-gradient(ellipse at 20% 80%, rgba(99, 179, 237, 0.05) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(128, 90, 213, 0.04) 0%, transparent 50%),
    linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  position: relative;
}

.chat-messages::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
  opacity: 0.02;
  pointer-events: none;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border: 1px solid rgba(99, 179, 237, 0.3);
  color: #2563eb;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: 1px solid rgba(99, 179, 237, 0.3);
  color: #ffffff;
}

.message-content {
  flex: 1;
}

.message-text {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.7;
  color: #334155;
}

.message.assistant .message-text {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid rgba(99, 179, 237, 0.2);
  border-radius: 16px 16px 16px 4px;
}

.message.user .message-text {
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: 1px solid rgba(99, 179, 237, 0.3);
  border-radius: 16px 16px 4px 16px;
  color: #ffffff;
}

.message-text :deep(strong) {
  color: #2563eb;
  font-weight: 600;
}

.message-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 6px;
  padding: 0 4px;
}

.message.user .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid rgba(99, 179, 237, 0.2);
  border-radius: 16px;
  width: fit-content;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border-radius: 50%;
  animation: typingBounce 1.4s ease-in-out infinite;
  box-shadow: 0 0 8px rgba(99, 179, 237, 0.4);
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

.quick-actions {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  flex-wrap: wrap;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  border-top: 1px solid rgba(99, 179, 237, 0.1);
}

.quick-btn {
  padding: 8px 14px;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border: 1px solid rgba(99, 179, 237, 0.25);
  border-radius: 20px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.quick-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #bfdbfe 0%, #ddd6fe 100%);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.quick-btn:hover {
  border-color: rgba(99, 179, 237, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 179, 237, 0.2);
  color: #1d4ed8;
}

.quick-btn:hover::before {
  opacity: 1;
}

.chat-input-wrap {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(99, 179, 237, 0.1);
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  position: relative;
}

.chat-input-wrap::before {
  content: '';
  position: absolute;
  top: 0;
  left: 20px;
  right: 20px;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(99, 179, 237, 0.3) 50%, transparent 100%);
}

.chat-input {
  flex: 1;
  padding: 12px 18px;
  background: #ffffff;
  border: 1px solid rgba(99, 179, 237, 0.2);
  border-radius: 14px;
  color: #1e293b;
  font-size: 14px;
  outline: none;
  transition: all 0.25s ease;
}

.chat-input::placeholder {
  color: #94a3b8;
}

.chat-input:focus {
  border-color: #3b82f6;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.send-btn {
  width: 46px;
  height: 46px;
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: none;
  border-radius: 14px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.analysis-panel {
  padding: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.analysis-grid {
  display: grid;
  gap: 16px;
}

.analysis-card {
  background: #ffffff;
  border: 1px solid rgba(99, 179, 237, 0.15);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-header h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.score-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #dcfce7 0%, #d1fae5 100%);
  border: 1px solid rgba(34, 197, 94, 0.3);
  border-radius: 20px;
  color: #16a34a;
  font-size: 14px;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
}

.habit-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid rgba(99, 179, 237, 0.1);
}

.stat-label {
  color: #64748b;
  font-size: 13px;
}

.stat-value {
  color: #334155;
  font-size: 13px;
  font-weight: 500;
}

.stat-value.highlight {
  color: #f59e0b;
}

.habit-areas {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.area-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.area-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.area-tags .tag {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.area-tags.strong .tag {
  background: #dcfce7;
  color: #16a34a;
}

.area-tags.weak .tag {
  background: #ffedd5;
  color: #ea580c;
}

.error-count {
  font-size: 12px;
  color: #64748b;
}

.error-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.error-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #fef2f2;
  border-radius: 12px;
  border-left: 3px solid #ef4444;
}

.error-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.error-type {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.error-count-badge {
  padding: 4px 10px;
  background: #fee2e2;
  border-radius: 20px;
  color: #dc2626;
  font-size: 12px;
  font-weight: 500;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid rgba(99, 179, 237, 0.1);
  transition: all 0.2s ease;
}

.suggestion-item:hover {
  background: #f1f5f9;
  border-color: rgba(99, 179, 237, 0.2);
}

.suggestion-icon {
  font-size: 24px;
  line-height: 1;
}

.suggestion-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.suggestion-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.plan-panel {
  padding: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.plan-header {
  text-align: center;
  margin-bottom: 24px;
}

.plan-header h4 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}

.plan-header p {
  font-size: 13px;
  color: #64748b;
  margin: 0;
}

.plan-timeline {
  position: relative;
  padding-left: 80px;
}

.plan-timeline::before {
  content: '';
  position: absolute;
  left: 60px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #3b82f6 0%, #8b5cf6 100%);
}

.timeline-item {
  position: relative;
  margin-bottom: 16px;
}

.timeline-marker {
  position: absolute;
  left: -80px;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  text-align: right;
}

.marker-day {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  font-family: 'JetBrains Mono', monospace;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: -26px;
  top: 50%;
  transform: translateY(-50%);
  width: 12px;
  height: 12px;
  background: #ffffff;
  border: 2px solid #3b82f6;
  border-radius: 50%;
  z-index: 1;
}

.timeline-content {
  padding-left: 20px;
}

.plan-task {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #ffffff;
  border: 1px solid rgba(99, 179, 237, 0.15);
  border-radius: 14px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.plan-task:hover {
  background: #f8fafc;
  border-color: rgba(59, 130, 246, 0.3);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
}

.plan-task.highlight {
  background: linear-gradient(135deg, #eff6ff 0%, #f5f3ff 100%);
  border-color: rgba(59, 130, 246, 0.3);
}

.task-icon {
  font-size: 24px;
  line-height: 1;
}

.task-info {
  flex: 1;
}

.task-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.task-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 3px;
}

.task-time {
  padding: 4px 10px;
  background: #dbeafe;
  border-radius: 20px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 500;
  font-family: 'JetBrains Mono', monospace;
}

.plan-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
}

.plan-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.plan-btn.primary {
  background: linear-gradient(135deg, #3b82f6 0%, #6366f1 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.plan-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.plan-btn.secondary {
  background: #ffffff;
  border: 1px solid rgba(99, 179, 237, 0.2);
  color: #475569;
}

.plan-btn.secondary:hover {
  background: #f8fafc;
  border-color: rgba(59, 130, 246, 0.3);
  color: #3b82f6;
}

@media (max-width: 640px) {
  .header-tabs {
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .tab-btn {
    padding: 6px 12px;
    font-size: 12px;
  }

  .quick-actions {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 16px;
  }

  .quick-btn {
    flex-shrink: 0;
  }

  .plan-timeline {
    padding-left: 60px;
  }

  .timeline-marker {
    left: -60px;
    width: 40px;
  }

  .plan-timeline::before {
    left: 40px;
  }

  .timeline-item::before {
    left: -46px;
  }
}
</style>
