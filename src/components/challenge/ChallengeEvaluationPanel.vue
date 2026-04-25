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

const CODE_SEPARATOR = '---CODE---'
const JSON_SEPARATOR = '---JSON---'

const passed = computed(() => Number(props.result?.score || 0) >= props.passScore)
const starsDisplay = computed(() => {
  const stars = Math.min(3, Math.max(0, Number(props.result?.stars || 0)))
  return '★'.repeat(stars) + '☆'.repeat(3 - stars)
})

const normalizeSuggestions = (value) => {
  if (!Array.isArray(value)) {
    return []
  }
  return value.map((item) => String(item || '').trim()).filter(Boolean)
}

const extractAnalysisText = (text) => {
  const source = String(text || '')
  const jsonSeparatorIndex = source.indexOf(JSON_SEPARATOR)
  if (jsonSeparatorIndex === -1) {
    return source.trim().startsWith('{') ? '' : source.trim()
  }
  return ''
}

const extractCodeCandidate = (text) => {
  const source = String(text || '')
  const codeSeparatorIndex = source.indexOf(CODE_SEPARATOR)
  if (codeSeparatorIndex === -1) {
    return ''
  }

  const codeStart = codeSeparatorIndex + CODE_SEPARATOR.length
  const jsonAfterCode = source.indexOf(JSON_SEPARATOR, codeStart)
  return jsonAfterCode !== -1
    ? source.slice(codeStart, jsonAfterCode).trim()
    : source.slice(codeStart).trim()
}

const extractJsonCandidate = (text) => {
  const source = String(text || '')
  const separatorIndex = source.indexOf(JSON_SEPARATOR)
  if (separatorIndex !== -1) {
    const afterSeparator = source.slice(separatorIndex + JSON_SEPARATOR.length).trim()
    const codeSeparatorIndex = afterSeparator.indexOf(CODE_SEPARATOR)
    return codeSeparatorIndex !== -1
      ? afterSeparator.slice(0, codeSeparatorIndex).trim()
      : afterSeparator
  }

  const trimmed = source.trim()
  return trimmed.startsWith('{') ? trimmed : ''
}

const stripMarkdownFence = (text) => {
  const value = String(text || '').trim()
  if (!value.startsWith('```')) {
    return value
  }

  return value
    .replace(/^```[\w-]*\s*/, '')
    .replace(/\s*```$/, '')
    .trim()
}

const trimBlankLines = (text) => {
  return String(text || '')
    .replace(/\r\n/g, '\n')
    .replace(/^\s*\n+/, '')
    .replace(/\n+\s*$/, '')
}

const dedentBlock = (text) => {
  const lines = trimBlankLines(text).split('\n')
  const nonEmptyLines = lines.filter((line) => line.trim())
  if (!nonEmptyLines.length) {
    return ''
  }

  const minIndent = Math.min(
    ...nonEmptyLines.map((line) => {
      const match = line.match(/^\s*/)
      return match ? match[0].length : 0
    }),
  )

  return lines.map((line) => line.slice(Math.min(minIndent, line.length))).join('\n')
}

const normalizeCodeBlock = (text) => dedentBlock(stripMarkdownFence(text))

const decodeEscapedCharacter = (char) => {
  switch (char) {
    case 'n':
      return '\n'
    case 'r':
      return '\r'
    case 't':
      return '\t'
    case 'b':
      return '\b'
    case 'f':
      return '\f'
    case '"':
      return '"'
    case '\\':
      return '\\'
    case '/':
      return '/'
    default:
      return char
  }
}

const readJsonString = (text, quoteIndex) => {
  if (text[quoteIndex] !== '"') {
    return { value: '', completed: false, endIndex: quoteIndex }
  }

  let value = ''
  let escape = false

  for (let index = quoteIndex + 1; index < text.length; index += 1) {
    const char = text[index]

    if (escape) {
      if (char === 'u' && /^[\da-fA-F]{4}$/.test(text.slice(index + 1, index + 5))) {
        value += String.fromCharCode(Number.parseInt(text.slice(index + 1, index + 5), 16))
        index += 4
      } else {
        value += decodeEscapedCharacter(char)
      }
      escape = false
      continue
    }

    if (char === '\\') {
      escape = true
      continue
    }

    if (char === '"') {
      return { value, completed: true, endIndex: index }
    }

    value += char
  }

  return { value, completed: false, endIndex: text.length - 1 }
}

const extractJsonStringField = (text, fieldName) => {
  const keyMatch = new RegExp(`"${fieldName}"\\s*:\\s*"`, 's').exec(text)
  if (!keyMatch) {
    return ''
  }

  const quoteIndex = keyMatch.index + keyMatch[0].length - 1
  return readJsonString(text, quoteIndex).value.trim()
}

const extractJsonStringArrayField = (text, fieldName) => {
  const keyMatch = new RegExp(`"${fieldName}"\\s*:\\s*\\[`, 's').exec(text)
  if (!keyMatch) {
    return []
  }

  const items = []
  let cursor = keyMatch.index + keyMatch[0].length

  while (cursor < text.length) {
    while (cursor < text.length && /[\s,]/.test(text[cursor])) {
      cursor += 1
    }

    if (cursor >= text.length || text[cursor] === ']') {
      break
    }

    if (text[cursor] !== '"') {
      break
    }

    const parsed = readJsonString(text, cursor)
    if (parsed.value.trim()) {
      items.push(parsed.value.trim())
    }

    cursor = parsed.endIndex + 1

    if (!parsed.completed) {
      break
    }
  }

  return items
}

const mergeAnalysisText = (analysisBeforeJson, shortComment) => {
  const preface = String(analysisBeforeJson || '').trim()
  const comment = String(shortComment || '').trim()

  if (!preface) {
    return comment
  }
  if (!comment) {
    return preface
  }
  if (preface === comment || preface.includes(comment)) {
    return preface
  }
  if (comment.includes(preface)) {
    return comment
  }
  return `${preface}\n\n${comment}`
}

const parseStreamingPayload = (rawValue) => {
  const raw = String(rawValue || '')
  const analysisBeforeJson = extractAnalysisText(raw)
  const codeCandidate = extractCodeCandidate(raw)
  const jsonCandidate = extractJsonCandidate(raw)

  let shortComment = ''
  let suggestions = []
  let recommendedCode = ''

  if (jsonCandidate) {
    try {
      const parsed = JSON.parse(jsonCandidate)
      shortComment = String(parsed.shortComment ?? parsed.analysis ?? '').trim()
      suggestions = normalizeSuggestions(parsed.suggestions)
      recommendedCode = String(parsed.recommendedCode ?? '').trim()
    } catch {
      shortComment = extractJsonStringField(jsonCandidate, 'shortComment')
        || extractJsonStringField(jsonCandidate, 'analysis')
      suggestions = extractJsonStringArrayField(jsonCandidate, 'suggestions')
      recommendedCode = extractJsonStringField(jsonCandidate, 'recommendedCode')
    }
  }

  if (codeCandidate) {
    recommendedCode = codeCandidate
  }

  return {
    analysisText: mergeAnalysisText(analysisBeforeJson, shortComment),
    suggestions,
    recommendedCode: normalizeCodeBlock(recommendedCode),
  }
}

const streamingPayload = computed(() => parseStreamingPayload(props.streamingText))
const streamingAnalysisText = computed(() => {
  return streamingPayload.value.analysisText || 'AI 正在分析你的代码，请稍候...'
})
const streamingSuggestions = computed(() => streamingPayload.value.suggestions)
const streamingRecommendedCode = computed(() => streamingPayload.value.recommendedCode)

const finalAnalysisText = computed(() => {
  return String(props.result?.shortComment || props.result?.analysis || '').trim() || '暂无分析内容'
})
const finalRecommendedCode = computed(() => normalizeCodeBlock(props.result?.recommendedCode || ''))
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
          <span class="stream-shell-meta">分析、建议和参考代码会随着模型输出持续刷新</span>
        </div>
        <div class="eval-analysis streaming-analysis">{{ streamingAnalysisText }}</div>
      </div>

      <template v-if="streamingSuggestions.length">
        <div class="eval-section-title">改进建议</div>
        <ul class="eval-suggestions">
          <li v-for="(suggestion, index) in streamingSuggestions" :key="index">{{ suggestion }}</li>
        </ul>
      </template>

      <template v-if="streamingRecommendedCode">
        <div class="eval-section-title">推荐代码</div>
        <pre class="eval-pre recommended-code-block">{{ streamingRecommendedCode }}</pre>
      </template>
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

      <div v-if="result.pointsEarned > 0" class="eval-meta">本次奖励：{{ result.pointsEarned }} 积分</div>
      <div class="eval-meta">评测时间：{{ result.updatedAt }}</div>

      <div class="eval-section-title">中文分析</div>
      <div class="eval-analysis">{{ finalAnalysisText }}</div>

      <template v-if="result.suggestions?.length">
        <div class="eval-section-title">改进建议</div>
        <ul class="eval-suggestions">
          <li v-for="(suggestion, index) in result.suggestions" :key="index">{{ suggestion }}</li>
        </ul>
      </template>

      <template v-if="finalRecommendedCode">
        <div class="eval-section-title">推荐代码</div>
        <pre class="eval-pre recommended-code-block">{{ finalRecommendedCode }}</pre>
      </template>
    </template>

    <template v-else>
      <div class="empty-state">运行评测后，这里会展示 AI 评分、中文分析、改进建议和推荐代码。</div>
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
  max-height: 320px;
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
  word-break: break-word;
  line-height: 1.7;
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
  background: #0f172a;
  color: #e2e8f0;
  border-color: rgba(148, 163, 184, 0.3);
  font-family: 'Fira Code', 'JetBrains Mono', monospace;
  line-height: 1.6;
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
</style>
