<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const emit = defineEmits(['complete'])
const router = useRouter()
const userStore = useUserStore()

const currentStep = ref(0)
const answers = ref({
  goal: '',
  level: '',
  time: '',
  track: '',
})

const goalOptions = [
  { value: 'job', icon: '💼', label: '求职准备', desc: '备战校招/社招，拿下技术岗 offer' },
  { value: 'contest', icon: '🏆', label: '竞赛冲刺', desc: '蓝桥杯、ACM 等算法竞赛' },
  { value: 'skill', icon: '🧠', label: '能力提升', desc: '系统学习算法，提升编程思维' },
  { value: 'interest', icon: '🎯', label: '兴趣探索', desc: '试试看，了解一下算法世界' },
]

const levelOptions = [
  { value: 'zero', icon: '🌱', label: '零基础', desc: '没接触过算法和数据结构' },
  { value: 'beginner', icon: '🌿', label: '入门阶段', desc: '了解基础概念，刷题不到 50 道' },
  { value: 'intermediate', icon: '🌳', label: '进阶中', desc: '刷过一些题，中等难度有思路' },
  { value: 'advanced', icon: '🏔️', label: '冲刺阶段', desc: '刷题 200+，冲击难题' },
]

const timeOptions = [
  { value: 'casual', icon: '☕', label: '轻松模式', desc: '每周 1-3 小时', goal: 3 },
  { value: 'steady', icon: '📚', label: '稳步推进', desc: '每周 3-7 小时', goal: 7 },
  { value: 'intensive', icon: '🔥', label: '高强度训练', desc: '每周 7-15 小时', goal: 14 },
  { value: 'hardcore', icon: '🚀', label: '冲刺模式', desc: '每周 15 小时以上', goal: 21 },
]

const trackOptions = [
  { value: 'algo', icon: '🧩', label: '算法思维赛道', desc: '排序、搜索、动态规划、贪心等核心算法' },
  { value: 'ds', icon: '🏗️', label: '数据结构赛道', desc: '链表、树、图、哈希等数据结构' },
  { value: 'contest', icon: '🏆', label: '竞赛冲刺赛道', desc: '蓝桥杯/ACM 真题，竞赛技巧' },
]

const steps = [
  { key: 'welcome', title: '欢迎来到 AlgoMind', subtitle: '让我们先了解一下你，为你定制专属学习路径' },
  { key: 'goal', title: '你的目标是什么？', subtitle: '选择最符合你当前状态的一项' },
  { key: 'level', title: '你目前的水平？', subtitle: '不用谦虚，如实选择即可' },
  { key: 'time', title: '每周能投入多少时间？', subtitle: '我们会据此安排每日任务量' },
  { key: 'track', title: '选择你的赛道', subtitle: '不同赛道有不同的侧重点和题目' },
  { key: 'done', title: '准备就绪！', subtitle: '你的专属学习路径已生成' },
]

const current = computed(() => steps[currentStep.value])
const isFirst = computed(() => currentStep.value === 0)
const isLast = computed(() => currentStep.value === steps.length - 1)
const progress = computed(() => ((currentStep.value + 1) / steps.length) * 100)

const canNext = computed(() => {
  const key = current.value.key
  if (key === 'welcome') return true
  if (key === 'done') return true
  return !!answers.value[key]
})

const selectedGoal = computed(() => goalOptions.find(o => o.value === answers.value.goal))
const selectedLevel = computed(() => levelOptions.find(o => o.value === answers.value.level))
const selectedTime = computed(() => timeOptions.find(o => o.value === answers.value.time))
const selectedTrack = computed(() => trackOptions.find(o => o.value === answers.value.track))

const next = () => {
  if (!canNext.value) return
  if (!isLast.value) currentStep.value++
}

const prev = () => {
  if (!isFirst.value) currentStep.value--
}

const selectOption = (key, value) => {
  answers.value[key] = answers.value[key] === value ? '' : value
}

const finish = () => {
  const timeOption = timeOptions.find(o => o.value === answers.value.time)
  const result = {
    goal: answers.value.goal,
    level: answers.value.level,
    time: answers.value.time,
    track: answers.value.track || 'algo',
    weeklyGoal: timeOption?.goal || 7,
  }
  emit('complete', result)
}

const skip = () => {
  emit('complete', {
    goal: '',
    level: '',
    time: '',
    track: 'algo',
    weeklyGoal: 7,
  })
}
</script>

<template>
  <Transition name="overlay">
    <div class="onboarding-overlay">
      <Transition name="card" mode="out-in">
        <div class="onboarding-card" :key="currentStep" @click.stop>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>

          <button class="btn-skip _target" @click="skip">跳过</button>

          <div class="step-header">
            <h2 class="step-title">{{ current.title }}</h2>
            <p class="step-subtitle">{{ current.subtitle }}</p>
          </div>

          <div class="step-body">
            <!-- Welcome -->
            <div v-if="current.key === 'welcome'" class="welcome-content">
              <div class="welcome-icon">🚀</div>
              <p class="welcome-text">
                AlgoMind 是一个为算法学习者打造的成长平台。<br>
                回答几个小问题，我们将为你定制专属学习路径。
              </p>
            </div>

            <!-- Goal -->
            <div v-else-if="current.key === 'goal'" class="option-grid">
              <div v-for="opt in goalOptions" :key="opt.value" class="option-card"
                :class="{ selected: answers.goal === opt.value }" @click="selectOption('goal', opt.value)">
                <span class="option-icon">{{ opt.icon }}</span>
                <span class="option-label">{{ opt.label }}</span>
                <span class="option-desc">{{ opt.desc }}</span>
              </div>
            </div>

            <!-- Level -->
            <div v-else-if="current.key === 'level'" class="option-grid">
              <div v-for="opt in levelOptions" :key="opt.value" class="option-card"
                :class="{ selected: answers.level === opt.value }" @click="selectOption('level', opt.value)">
                <span class="option-icon">{{ opt.icon }}</span>
                <span class="option-label">{{ opt.label }}</span>
                <span class="option-desc">{{ opt.desc }}</span>
              </div>
            </div>

            <!-- Time -->
            <div v-else-if="current.key === 'time'" class="option-grid">
              <div v-for="opt in timeOptions" :key="opt.value" class="option-card"
                :class="{ selected: answers.time === opt.value }" @click="selectOption('time', opt.value)">
                <span class="option-icon">{{ opt.icon }}</span>
                <span class="option-label">{{ opt.label }}</span>
                <span class="option-desc">{{ opt.desc }}</span>
              </div>
            </div>

            <!-- Track -->
            <div v-else-if="current.key === 'track'" class="option-grid track-grid">
              <div v-for="opt in trackOptions" :key="opt.value" class="option-card track-card"
                :class="{ selected: answers.track === opt.value }" @click="selectOption('track', opt.value)">
                <span class="option-icon">{{ opt.icon }}</span>
                <span class="option-label">{{ opt.label }}</span>
                <span class="option-desc">{{ opt.desc }}</span>
              </div>
            </div>

            <!-- Done -->
            <div v-else-if="current.key === 'done'" class="done-content">
              <div class="done-icon">✨</div>
              <div class="done-summary">
                <div v-if="selectedGoal" class="summary-item">
                  <span class="summary-label">目标</span>
                  <span class="summary-value">{{ selectedGoal.icon }} {{ selectedGoal.label }}</span>
                </div>
                <div v-if="selectedLevel" class="summary-item">
                  <span class="summary-label">水平</span>
                  <span class="summary-value">{{ selectedLevel.icon }} {{ selectedLevel.label }}</span>
                </div>
                <div v-if="selectedTime" class="summary-item">
                  <span class="summary-label">节奏</span>
                  <span class="summary-value">{{ selectedTime.icon }} {{ selectedTime.label }}</span>
                </div>
                <div v-if="selectedTrack" class="summary-item">
                  <span class="summary-label">赛道</span>
                  <span class="summary-value">{{ selectedTrack.icon }} {{ selectedTrack.label }}</span>
                </div>
              </div>
              <p class="done-hint">AI 助手将根据你的选择生成个性化学习计划</p>
            </div>
          </div>

          <div class="step-actions">
            <button v-if="!isFirst" class="btn-prev _target" @click="prev">上一步</button>
            <div v-else></div>

            <button v-if="!isLast" class="btn-next _target" :class="{ disabled: !canNext }" @click="next">
              {{ current.key === 'welcome' ? '开始' : '下一步' }}
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"></polyline>
              </svg>
            </button>

            <button v-else class="btn-finish _target" @click="finish">
              开启学习之旅
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </button>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<style scoped>
.onboarding-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.onboarding-card {
  position: relative;
  width: 540px;
  max-width: calc(100vw - 32px);
  max-height: calc(100vh - 48px);
  overflow-y: auto;
  background: white;
  border-radius: 24px;
  padding: 36px 36px 28px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
}

.progress-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #f0f0f0;
  border-radius: 24px 24px 0 0;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4a90d9, #722ed1);
  border-radius: 24px 24px 0 0;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-skip {
  position: absolute;
  top: 16px;
  right: 20px;
  border: none;
  background: transparent;
  color: var(--text-muted, #667789);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
  z-index: 1;
}

.btn-skip:hover {
  color: var(--text-main, #2f425b);
}

.step-header {
  text-align: center;
  margin-bottom: 24px;
}

.step-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-title, #1f2f44);
  margin: 0 0 8px;
}

.step-subtitle {
  font-size: 14px;
  color: var(--text-muted, #667789);
  margin: 0;
}

.step-body {
  min-height: 200px;
}

.welcome-content {
  text-align: center;
  padding: 20px 0;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.welcome-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-sub, #5f7286);
  margin: 0;
}

.option-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.track-grid {
  grid-template-columns: 1fr;
}

.option-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 18px 12px;
  border: 2px solid #eef1f6;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.option-card:hover {
  border-color: #c5d4ea;
  background: #f8faff;
  transform: translateY(-2px);
}

.option-card.selected {
  border-color: #4a90d9;
  background: linear-gradient(135deg, rgba(74, 144, 217, 0.06) 0%, rgba(114, 46, 209, 0.04) 100%);
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.12);
}

.option-icon {
  font-size: 28px;
  line-height: 1;
}

.option-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-title, #1f2f44);
}

.option-desc {
  font-size: 12px;
  color: var(--text-muted, #667789);
  line-height: 1.4;
}

.track-card {
  flex-direction: row;
  gap: 14px;
  padding: 16px 20px;
  text-align: left;
}

.track-card .option-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.track-card .option-label {
  font-size: 16px;
}

.track-card .option-desc {
  font-size: 13px;
}

.done-content {
  text-align: center;
  padding: 12px 0;
}

.done-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.done-summary {
  display: inline-flex;
  flex-direction: column;
  gap: 10px;
  background: #f8faff;
  border-radius: 14px;
  padding: 16px 24px;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-label {
  font-size: 13px;
  color: var(--text-muted, #667789);
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-title, #1f2f44);
}

.done-hint {
  font-size: 13px;
  color: var(--text-muted, #667789);
  margin: 0;
}

.step-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
}

.btn-prev {
  padding: 10px 20px;
  border: 1px solid var(--line-soft, #e1e8f1);
  border-radius: 12px;
  background: transparent;
  color: var(--text-sub, #5f7286);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-prev:hover {
  background: #f5f7fa;
  border-color: #c0c8d4;
}

.btn-next {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-next:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-next.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}

.btn-finish {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 28px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #4a90d9 0%, #722ed1 100%);
  color: white;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 14px rgba(74, 144, 217, 0.3);
}

.btn-finish:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 144, 217, 0.4);
}

.overlay-enter-active {
  transition: opacity 0.3s ease;
}

.overlay-leave-active {
  transition: opacity 0.2s ease;
}

.overlay-enter-from,
.overlay-leave-to {
  opacity: 0;
}

.card-enter-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.card-leave-active {
  transition: all 0.15s ease;
}

.card-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(12px);
}

.card-leave-to {
  opacity: 0;
  transform: scale(0.97) translateY(-6px);
}
</style>
