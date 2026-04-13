<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLevelStore } from '../stores/level'
import AIAssistant from '../components/AIAssistant.vue'
import DailyTasks from '../components/DailyTasks.vue'

const router = useRouter()
const userStore = useUserStore()
const levelStore = useLevelStore()

const selectedTrack = ref(userStore.userInfo?.targetTrack || userStore.selectedTrack)
const weeklyGoal = ref(Number(userStore.userInfo?.weeklyGoal || 10))

// 存储生成的学习计划
const currentLearningPlan = ref(null)

// 处理学习计划生成完成事件
const handlePlanGenerated = (plan) => {
  currentLearningPlan.value = plan
  ElMessage.success({
    message: '学习计划已生成并同步到每日任务',
    duration: 3000,
    offset: 80,
  })
}

const trackOptions = [
  { label: '算法思维赛道', value: 'algo' },
  { label: '数据结构赛道', value: 'ds' },
  { label: '竞赛冲刺赛道', value: 'contest' },
]

const weeklyStatus = computed(() => {
  const doneCount = levelStore.levels.filter((item) => item.isUnlocked).length
  const ratio = Math.min(100, Math.round((doneCount / Math.max(weeklyGoal.value, 1)) * 100))
  return {
    doneCount,
    ratio,
  }
})

const progress = computed(() => {
  const score = userStore.points
  if (score >= 220) return 100
  return Math.max(18, Math.round((score / 220) * 100))
})

const goTo = (path) => {
  router.push(path)
}

const savePlanTarget = () => {
  userStore.updateProfile({ targetTrack: selectedTrack.value, weeklyGoal: weeklyGoal.value })
  userStore.setTrack(selectedTrack.value)
  ElMessage.success('赛道与目标已更新，计划已重算')
}

if (!levelStore.levels.length) {
  levelStore.fetchLevels()
}
</script>

<template>
  <div class="page-container home-page">
    <section class="hero surface-card">
      <div>
        <h2>欢迎回来，{{ userStore.userInfo?.name || '同学' }}</h2>
        <p>今天继续挑战算法关卡，稳步提升思维与代码实现能力。</p>
      </div>
      <button class="start-project-btn" @click="goTo('/projects')">
        <span class="btn-content">
          <svg class="btn-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor"
            stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="5 3 19 12 5 21 5 3"></polygon>
          </svg>
          开始训练
        </span>
        <div class="hover-effect"></div>
      </button>
    </section>

    <DailyTasks :learning-plan="currentLearningPlan" />

    <AIAssistant :selected-track="selectedTrack" :weekly-goal="weeklyGoal" :track-options="trackOptions"
      @plan-generated="handlePlanGenerated" />
  </div>
</template>

<style scoped>
.home-page {
  display: grid;
  gap: 18px;
  padding-bottom: 28px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px;
  border-radius: 18px;
}

.hero h2 {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-title);
}

.hero p {
  margin-top: 8px;
  color: var(--text-sub);
  font-size: 15px;
}

.start-project-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14px 28px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(14, 165, 233, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.start-project-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.4);
}

.btn-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-icon {
  transition: transform 0.3s ease;
  fill: currentColor;
}

.start-project-btn:hover .btn-icon {
  transform: scale(1.1);
}

.hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.2), transparent);
  transform: translateX(-100%);
}

.start-project-btn:hover .hover-effect {
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}

@media (max-width: 900px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }
}

@media (max-width: 600px) {
  .hero {
    margin-top: 10px;
  }

  .hero h2 {
    font-size: 24px;
  }

  .hero p {
    font-size: 14px;
  }

  .start-project-btn {
    width: 100%;
  }
}
</style>
