<script setup>
const props = defineProps({
  level: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['start'])

const handleStart = () => {
  if (!props.level.isUnlocked) return
  emit('start', props.level)
}
</script>

<template>
  <el-card class="level-card" :class="{ locked: !level.isUnlocked }" shadow="hover">
    <div class="header-row">
      <h3>{{ level.name }}</h3>
      <el-tag :type="level.isUnlocked ? 'success' : 'info'" effect="light">
        {{ level.isUnlocked ? '已解锁' : '未解锁' }}
      </el-tag>
    </div>
    <p class="desc">{{ level.description }}</p>
    <div class="footer-row">
      <el-tag type="warning" effect="plain">奖励积分 {{ level.rewardPoints }}</el-tag>
      <el-button type="primary" :disabled="!level.isUnlocked" @click="handleStart">进入挑战</el-button>
    </div>
  </el-card>
</template>

<style scoped>
.level-card {
  border-radius: 14px;
  border: 1px solid var(--line-soft);
}

.level-card.locked {
  opacity: 0.72;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.header-row h3 {
  color: var(--text-title);
  font-size: 18px;
  font-weight: 700;
}

.desc {
  margin-top: 12px;
  min-height: 44px;
  color: var(--text-sub);
}

.footer-row {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
</style>
