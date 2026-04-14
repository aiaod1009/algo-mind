<script setup>
import { computed } from 'vue'

const props = defineProps({
  result: {
    type: Object,
    default: null,
  },
  passScore: {
    type: Number,
    default: 70,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  streamingText: {
    type: String,
    default: '',
  },
})

const passed = computed(() => Number(props.result?.score || 0) >= props.passScore)
const starsDisplay = computed(() => {
  const stars = Math.min(3, Math.max(0, Number(props.result?.stars || 0)))
  return '★'.repeat(stars) + '☆'.repeat(3 - stars)
})

const simpleMarkdownToHtml = (text) => {
  if (!text) return ''
  return text
    // 代码块
    .replace(/```([\s\S]*?)```/g, '<pre class="code-block"><code>$1</code></pre>')
    // 行内代码
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    // 粗体
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    // 斜体
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    // 标题
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // 列表项
    .replace(/^- (.*$)/gim, '<li>$1</li>')
    // 换行
    .replace(/\n/g, '<br>')
}

const streamingPreview = computed(() => {
  const value = String(props.streamingText || '').trim()
  const html = simpleMarkdownToHtml(value)
  return html || 'AI 正在分析你的代码，请稍候...'
})
</script>

<template>
  <aside class="eval-panel">
    <div class="eval-title-row">
      <div class="eval-title">评测结果</div>
      <span class="eval-status" :class="{ passed, loading }">
        {{ loading ? '分析中' : passed ? '已达标' : '待改进' }}
      </span>
    </div>

    <template v-if="loading">
      <div class="streaming-banner">
        <span class="stream-dot"></span>
        <span>正在接收 AI 流式评测结果</span>
      </div>

      <div class="eval-section-title">实时分析</div>
      <div class="stream-shell">
        <div class="stream-shell-head">
          <span class="stream-shell-title">AI 分析进行中</span>
          <span class="stream-shell-meta">内容会在完成后自动汇总到正式评测结果</span>
        </div>
        <div class="eval-analysis streaming-analysis markdown-body" v-html="streamingPreview"></div>
      </div>
    </template>

    <template v-else-if="result">
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

      <div class="eval-meta" v-if="result.pointsEarned > 0">本次奖励：{{ result.pointsEarned }} 积分</div>
      <div class="eval-meta">评测时间：{{ result.updatedAt }}</div>

      <div class="eval-section-title">中文分析</div>
      <p class="eval-analysis">{{ result.shortComment || result.analysis || '暂无分析内容' }}</p>

      <template v-if="result.suggestions?.length">
        <div class="eval-section-title">改进建议</div>
        <ul class="eval-suggestions">
          <li v-for="(suggestion, index) in result.suggestions" :key="index">{{ suggestion }}</li>
        </ul>
      </template>

      <template v-if="result.recommendedCode">
        <div class="eval-section-title">推荐代码</div>
        <pre class="eval-pre recommended-code-block">{{ result.recommendedCode }}</pre>
      </template>
    </template>

    <template v-else>
      <div class="empty-state">运行评测后，这里会展示 AI 评分和优化建议。</div>
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

.eval-status.loading {
  background: #dbeafe;
  color: #1d4ed8;
}

.streaming-banner {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 10px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 13px;
}

.stream-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: currentColor;
  animation: pulse 1.2s ease-in-out infinite;
}

.stream-shell {
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  background:
    linear-gradient(180deg, rgba(59, 130, 246, 0.08), rgba(59, 130, 246, 0.02)),
    #fff;
  overflow: hidden;
}

.stream-shell-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 12px 10px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(248, 250, 252, 0.85);
}

.stream-shell-title {
  color: var(--text-title);
  font-size: 13px;
  font-weight: 700;
}

.stream-shell-meta {
  color: var(--text-sub);
  font-size: 12px;
  line-height: 1.5;
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
  color: #dc2626;
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
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.3) transparent;
}

.eval-pre::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.eval-pre::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 4px;
}

.eval-pre::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.eval-pre::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.5);
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

.streaming-analysis {
  min-height: 220px;
  max-height: 380px;
  border: none;
  border-radius: 0;
  background:
    radial-gradient(circle at top right, rgba(96, 165, 250, 0.12), transparent 40%),
    #fff;
  padding: 14px 12px 16px;
}

.recommended-code-block {
  min-height: 180px;
  max-height: 320px;
  background: #0f172a;
  color: #e2e8f0;
  border-color: rgba(148, 163, 184, 0.3);
  font-family: 'Fira Code', 'JetBrains Mono', monospace;
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

.empty-state {
  padding: 18px 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px dashed var(--line-soft);
  color: var(--text-sub);
  text-align: center;
  line-height: 1.6;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.4;
    transform: scale(0.85);
  }

  50% {
    opacity: 1;
    transform: scale(1);
  }
}

/* Markdown 样式 */
.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin: 8px 0 4px;
  color: var(--text-title);
  font-weight: 600;
}

.markdown-body h1 {
  font-size: 16px;
}

.markdown-body h2 {
  font-size: 14px;
}

.markdown-body h3 {
  font-size: 13px;
}

.markdown-body strong {
  color: #2563eb;
  font-weight: 600;
}

.markdown-body em {
  color: #7c3aed;
  font-style: italic;
}

.markdown-body code {
  font-family: 'Fira Code', monospace;
  font-size: 12px;
}

.markdown-body .inline-code {
  padding: 2px 6px;
  background: rgba(37, 99, 235, 0.08);
  border-radius: 4px;
  color: #1d4ed8;
}

.markdown-body .code-block {
  margin: 8px 0;
  padding: 12px;
  background: #0f172a;
  border-radius: 8px;
  overflow-x: auto;
  color: #e2e8f0;
  line-height: 1.5;
}

.markdown-body .code-block code {
  color: #e2e8f0;
  background: transparent;
  padding: 0;
}

.markdown-body li {
  margin: 6px 0 6px 18px;
  color: var(--text-main);
}

.markdown-body li {
  margin: 4px 0;
  color: #cbd5e1;
}

.markdown-body li::marker {
  color: #60a5fa;
}

.markdown-body br {
  display: block;
  content: '';
  margin: 4px 0;
}
</style>
