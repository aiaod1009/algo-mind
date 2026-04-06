<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: {
    type: Object,
    required: true,
  },
  passScore: {
    type: Number,
    default: 70,
  },
})

const passed = computed(() => Number(props.result?.score || 0) >= props.passScore)
const starsDisplay = computed(() => {
  const stars = Math.min(3, Math.max(0, Number(props.result?.stars || 0)))
  return '★'.repeat(stars) + '☆'.repeat(3 - stars)
})
</script>

<template>
  <aside class="eval-panel">
    <div class="eval-title-row">
      <div class="eval-title">评测结果</div>
      <span class="eval-status" :class="{ passed }">{{ passed ? '已达标' : '待改进' }}</span>
    </div>

    <div class="eval-score-row">
      <span class="label">评测分数</span>
      <span class="score">{{ result.score ?? '--' }}</span>
    </div>

    <div class="eval-score-row">
      <span class="label">通关标准</span>
      <span class="meta">{{ passScore }} 分</span>
    </div>

    <div class="eval-score-row">
      <span class="label">星级评定</span>
      <span class="stars" :class="'stars-' + (result.stars || 0)">{{ starsDisplay }}</span>
    </div>

    <div class="eval-meta" v-if="result.pointsEarned > 0">本次奖励：{{ result.pointsEarned }} 分</div>
    <div class="eval-meta">评测时间：{{ result.updatedAt }}</div>

    <div class="eval-section-title">程序输出</div>
    <pre class="eval-pre">{{ result.output || '暂无输出' }}</pre>

    <div class="eval-section-title">AI 分析</div>
    <p class="eval-analysis">{{ result.analysis || '暂无分析内容' }}</p>

    <template v-if="result.correctness || result.quality || result.efficiency">
      <div class="eval-section-title">详细评价</div>
      <div class="eval-detail" v-if="result.correctness">
        <span class="detail-label">正确性：</span>{{ result.correctness }}
      </div>
      <div class="eval-detail" v-if="result.quality">
        <span class="detail-label">代码质量：</span>{{ result.quality }}
      </div>
      <div class="eval-detail" v-if="result.efficiency">
        <span class="detail-label">效率：</span>{{ result.efficiency }}
      </div>
    </template>

    <template v-if="result.suggestions?.length">
      <div class="eval-section-title">改进建议</div>
      <ul class="eval-suggestions">
        <li v-for="(suggestion, index) in result.suggestions" :key="index">{{ suggestion }}</li>
      </ul>
    </template>
  </aside>
</template>

<style scoped>
.eval-panel {
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  padding: 12px;
  background: #f8fbff;
  display: grid;
  gap: 8px;
  max-height: 620px;
  overflow-y: auto;
}

.eval-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.eval-title {
  color: var(--text-title);
  font-weight: 700;
  font-size: 16px;
}

.eval-status {
  padding: 4px 10px;
  border-radius: 999px;
  background: #e5e7eb;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
}

.eval-status.passed {
  background: #dcfce7;
  color: #166534;
}

.eval-score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.label,
.meta {
  color: var(--text-sub);
  font-size: 13px;
}

.score {
  color: var(--brand-blue-strong);
  font-size: 24px;
  font-weight: 700;
}

.stars {
  font-size: 20px;
  letter-spacing: 2px;
}

.stars-3,
.stars-2,
.stars-1 {
  color: #f59e0b;
}

.stars-0 {
  color: #d1d5db;
}

.eval-meta {
  color: var(--text-sub);
  font-size: 12px;
}

.eval-section-title {
  margin-top: 6px;
  color: var(--text-title);
  font-size: 13px;
  font-weight: 700;
}

.eval-pre {
  margin: 0;
  border: 1px solid var(--line-soft);
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  min-height: 68px;
  max-height: 160px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-main);
  font-size: 12px;
}

.eval-analysis {
  margin: 0;
  border: 1px solid var(--line-soft);
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  min-height: 96px;
  max-height: 220px;
  overflow-y: auto;
  white-space: pre-wrap;
  color: var(--text-main);
  font-size: 13px;
}

.eval-detail {
  font-size: 13px;
  color: var(--text-main);
  margin: 4px 0;
  line-height: 1.5;
}

.detail-label {
  color: var(--text-sub);
  font-weight: 500;
}

.eval-suggestions {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: var(--text-main);
}

.eval-suggestions li {
  margin: 4px 0;
  line-height: 1.5;
}
</style>
