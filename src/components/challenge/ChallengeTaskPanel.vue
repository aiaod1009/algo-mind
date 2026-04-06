<script setup>
const props = defineProps({
  level: {
    type: Object,
    required: true,
  },
  practiceRuleText: {
    type: String,
    default: '',
  },
  practiceWindowText: {
    type: String,
    default: '',
  },
  maxAttempts: {
    type: Number,
    default: 5,
  },
  typeLabel: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <aside class="surface-card task-panel">
    <h3>任务要求</h3>
    <div class="task-name">{{ level.name }}</div>
    <p class="task-rule">达标标准：{{ practiceRuleText }}</p>
    <p class="task-rule">练习时间：{{ practiceWindowText }}</p>
    <p class="task-rule">可作答次数：{{ maxAttempts }} 次</p>

    <div class="task-section-title">任务说明</div>
    <p class="task-desc">{{ level.description }}</p>
    <p class="task-desc">{{ level.question }}</p>

    <div class="task-tags">
      <el-tag type="primary" effect="plain">{{ typeLabel || level.type }}</el-tag>
      <el-tag type="warning" effect="plain">奖励 {{ level.rewardPoints }} 分</el-tag>
    </div>
  </aside>
</template>

<style scoped>
.task-panel {
  padding: 16px;
  border: 1px solid var(--line-soft);
  display: grid;
  gap: 8px;
}

.task-panel h3 {
  color: var(--text-title);
  font-size: 28px;
  font-weight: 700;
}

.task-name {
  color: var(--brand-blue-strong);
  font-size: 20px;
  font-weight: 700;
  margin-top: 4px;
}

.task-rule,
.task-desc {
  color: var(--text-sub);
  margin: 0;
  line-height: 1.7;
}

.task-section-title {
  margin-top: 10px;
  color: var(--text-title);
  font-size: 18px;
  font-weight: 700;
}

.task-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 980px) {
  .task-panel h3 {
    font-size: 22px;
  }
}
</style>
