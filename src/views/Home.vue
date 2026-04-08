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
      <el-button type="primary" size="large" @click="goTo('/levels')">开始闯关</el-button>
    </section>

    <DailyTasks />

    <AIAssistant
      :selected-track="selectedTrack"
      :weekly-goal="weeklyGoal"
      :track-options="trackOptions"
    />
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
}

@media (max-width: 900px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }
}
</style>
