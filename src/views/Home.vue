<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLevelStore } from '../stores/level'

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

const trackPlanTemplates = {
  algo: [
    { title: '复杂度快读', detail: '10 分钟复盘 O(1)~O(nlogn) 的典型场景', to: '/challenge/1' },
    { title: '双指针模板', detail: '完成两数之和变体，记录移动不变量', to: '/challenge/2' },
    { title: 'DP 起手式', detail: '写出状态定义和转移方程，限时 20 分钟', to: '/challenge/3' },
  ],
  ds: [
    { title: '栈队列热身', detail: '完成括号匹配与单调栈各 1 题', to: '/challenge/4' },
    { title: '哈希冲突演练', detail: '对比链地址法与开放寻址法优缺点', to: '/challenge/5' },
    { title: '并查集专题', detail: '手写路径压缩并完成 2 次调试', to: '/challenge/6' },
  ],
  contest: [
    { title: '贪心判定', detail: '练习活动选择并总结可证明性', to: '/challenge/7' },
    { title: '区间调度', detail: '训练排序 + 扫描组合策略', to: '/challenge/8' },
    { title: '赛后复盘', detail: '按题意重述、约束、坑点输出复盘模板', to: '/challenge/9' },
  ],
}

const planList = computed(() => trackPlanTemplates[selectedTrack.value] || trackPlanTemplates.algo)

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

    <section class="stats-grid">
      <el-card class="stat-item" shadow="never">
        <div class="stat-title">当前积分</div>
        <div class="stat-value">{{ userStore.points }}</div>
      </el-card>
      <el-card class="stat-item" shadow="never">
        <div class="stat-title">计划完成度</div>
        <div class="stat-value">{{ progress }}%</div>
      </el-card>
      <el-card class="stat-item" shadow="never">
        <div class="stat-title">周目标达成</div>
        <div class="stat-value">{{ weeklyStatus.doneCount }} / {{ weeklyGoal }}</div>
      </el-card>
    </section>

    <section class="surface-card target-card">
      <h3 class="section-title">赛道目标设置</h3>
      <div class="target-grid">
        <el-select v-model="selectedTrack" class="target-input">
          <el-option v-for="option in trackOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-input-number v-model="weeklyGoal" :min="3" :max="40" class="target-input" />
        <el-button type="primary" @click="savePlanTarget">更新计划</el-button>
      </div>
      <el-progress :percentage="weeklyStatus.ratio" :stroke-width="14" />
    </section>

    <section class="surface-card plan-card">
      <h3 class="section-title">赛道学习计划</h3>
      <el-timeline>
        <el-timeline-item v-for="(item, index) in planList" :key="item.title" :timestamp="`步骤 ${index + 1}`"
          placement="top">
          <el-card class="plan-item" shadow="hover" @click="goTo(item.to)">
            <div class="plan-title">{{ item.title }}</div>
            <div class="plan-detail">{{ item.detail }}</div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </section>
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.stat-item {
  border-radius: 14px;
  border: 1px solid var(--line-soft);
}

.stat-title {
  color: var(--text-sub);
}

.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-title);
}

.plan-card {
  padding: 24px;
}

.target-card {
  padding: 20px 24px;
  display: grid;
  gap: 12px;
}

.target-grid {
  display: flex;
  align-items: center;
  gap: 10px;
}

.target-input {
  width: 240px;
}

.plan-item {
  cursor: pointer;
  border-radius: 12px;
}

.plan-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-title);
}

.plan-detail {
  margin-top: 6px;
  color: var(--text-sub);
}

@media (max-width: 900px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .target-grid {
    flex-wrap: wrap;
  }
}
</style>
