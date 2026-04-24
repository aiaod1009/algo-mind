<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: {
    type: Object,
    default: null,
  },
  historySnapshot: {
    type: Object,
    default: null,
  },
  currentScore: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['clear'])

const displayHistoryScore = computed(() => {
  const score = Number(props.historySnapshot?.score ?? props.result?.historyScore)
  return Number.isFinite(score) ? score : null
})

const displayCurrentScore = computed(() => {
  const score = Number(props.result?.currentScore ?? props.currentScore)
  return Number.isFinite(score) ? score : null
})

const scoreDiff = computed(() => {
  if (displayCurrentScore.value == null || displayHistoryScore.value == null) {
    return null
  }
  return displayCurrentScore.value - displayHistoryScore.value
})

const scoreDiffText = computed(() => {
  if (scoreDiff.value == null) return '待评测'
  if (scoreDiff.value > 0) return `+${scoreDiff.value}分`
  if (scoreDiff.value < 0) return `${scoreDiff.value}分`
  return '持平'
})

const scoreDiffClass = computed(() => {
  if (scoreDiff.value == null) return 'unchanged'
  if (scoreDiff.value > 0) return 'improved'
  if (scoreDiff.value < 0) return 'declined'
  return 'unchanged'
})

const formatTime = (value) => {
  if (!value) return ''
  const time = new Date(value).getTime()
  if (!Number.isFinite(time)) return String(value)

  const diffMs = Date.now() - time
  const diffMinutes = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays < 7) return `${diffDays} 天前`

  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<template>
  <div class="comparison-panel">
    <div class="comparison-header">
      <h4>代码对比结果</h4>
      <button class="clear-btn _target" @click="emit('clear')" title="关闭对比">
        <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </button>
    </div>

    <div class="score-comparison">
      <div class="score-item history">
        <span class="label">历史代码</span>
        <span class="score">{{ displayHistoryScore ?? '--' }}分</span>
        <span class="time">{{ formatTime(historySnapshot?.savedAt) }}</span>
      </div>
      <div class="score-arrow">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M5 12h14M12 5l7 7-7 7" />
        </svg>
      </div>
      <div class="score-item current">
        <span class="label">当前代码</span>
        <span class="score">{{ displayCurrentScore ?? '--' }}分</span>
        <span class="diff" :class="scoreDiffClass">{{ scoreDiffText }}</span>
      </div>
    </div>

    <div v-if="result?.summary" class="comparison-section">
      <h5>总结</h5>
      <p class="summary-text">{{ result.summary }}</p>
    </div>

    <div v-if="result?.diffAnalysis?.length" class="comparison-section">
      <h5>差异分析</h5>
      <ul class="diff-list">
        <li v-for="(diff, index) in result.diffAnalysis" :key="index">
          {{ diff }}
        </li>
      </ul>
    </div>

    <div v-if="result?.improvements?.length" class="comparison-section">
      <h5>优劣对照</h5>
      <ul class="improvement-list">
        <li v-for="(item, index) in result.improvements" :key="index" :class="item.type">
          <span class="icon">{{ item.type === 'current' ? '优' : '参' }}</span>
          <span class="desc">{{ item.description }}</span>
        </li>
      </ul>
    </div>

    <div v-if="result?.suggestions?.length" class="comparison-section">
      <h5>优化建议</h5>
      <ul class="suggestion-list">
        <li v-for="(suggestion, index) in result.suggestions" :key="index">
          {{ suggestion }}
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.comparison-panel {
  display: grid;
  gap: 12px;
}

.comparison-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comparison-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-title);
}

.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-sub);
  transition: all 0.2s;
}

.clear-btn:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--text-main);
}

.score-comparison {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
}

.score-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: center;
}

.score-item .label {
  font-size: 12px;
  color: var(--text-sub);
}

.score-item .score {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-title);
}

.score-item .time {
  font-size: 11px;
  color: var(--text-sub);
}

.score-item .diff {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

.score-item .diff.improved {
  background: #dcfce7;
  color: #16a34a;
}

.score-item .diff.declined {
  background: #fee2e2;
  color: #dc2626;
}

.score-item .diff.unchanged {
  background: #f1f5f9;
  color: #64748b;
}

.score-arrow {
  color: var(--text-sub);
}

.comparison-section {
  display: grid;
  gap: 6px;
}

.comparison-section h5 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-title);
}

.summary-text {
  margin: 0;
  padding: 10px;
  background: #fff;
  border: 1px solid var(--line-soft);
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-main);
  line-height: 1.6;
}

.diff-list,
.suggestion-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--text-main);
}

.diff-list li,
.suggestion-list li {
  margin: 4px 0;
  line-height: 1.5;
}

.improvement-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.improvement-list li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid var(--line-soft);
  border-radius: 8px;
  font-size: 13px;
}

.improvement-list li.current {
  background: #f0fdf4;
  border-color: #bbf7d0;
}

.improvement-list li.history {
  background: #fefce8;
  border-color: #fef08a;
}

.improvement-list li .icon {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 10px;
}

.improvement-list li.current .icon {
  background: #22c55e;
  color: white;
}

.improvement-list li.history .icon {
  background: #eab308;
  color: white;
}

.improvement-list li .desc {
  color: var(--text-main);
  line-height: 1.4;
}
</style>
