<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: 'AI 智能分析',
  },
  content: {
    type: String,
    default: '',
  },
  analysisData: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const activeTab = ref('analysis')

// 解析纯文本内容为结构化数据
const parsedData = computed(() => {
  // 如果有结构化数据，直接使用
  if (props.analysisData && Object.keys(props.analysisData).length > 0) {
    return props.analysisData
  }
  
  // 否则解析 content 文本
  if (!props.content) {
    return {
      summary: '暂无分析结果',
      rootCause: '',
      explanation: '',
      knowledgePoints: [],
      relatedTopics: [],
      suggestions: [],
      studyPlan: null
    }
  }
  
  const text = props.content
  const data = {
    summary: '',
    rootCause: '',
    explanation: '',
    knowledgePoints: [],
    relatedTopics: [],
    suggestions: [],
    studyPlan: null
  }
  
  // 提取 Summary
  const summaryMatch = text.match(/Summary:\s*([^\n]+(?:\n(?![A-Z][a-z]+:)[^\n]+)*)/i)
  if (summaryMatch) {
    data.summary = summaryMatch[1].trim()
  }
  
  // 提取 Root cause
  const rootCauseMatch = text.match(/Root cause:\s*([^\n]+(?:\n(?![A-Z][a-z]+:)[^\n]+)*)/i)
  if (rootCauseMatch) {
    data.rootCause = rootCauseMatch[1].trim()
  }
  
  // 提取 Explanation
  const explanationMatch = text.match(/Explanation:\s*([^\n]+(?:\n(?![A-Z][a-z]+:)[^\n]+)*)/i)
  if (explanationMatch) {
    data.explanation = explanationMatch[1].trim()
  }
  
  // 提取 Suggestions
  const suggestionsMatch = text.match(/Suggestions:\s*([\s\S]*?)(?=\n\n|$)/i)
  if (suggestionsMatch) {
    const suggestionsText = suggestionsMatch[1].trim()
    const suggestionLines = suggestionsText.split('\n').filter(line => line.trim().startsWith('-') || line.trim().startsWith('•'))
    data.suggestions = suggestionLines.map((line, index) => ({
      title: `改进建议 ${index + 1}`,
      description: line.replace(/^[-•]\s*/, '').trim(),
      priority: index === 0 ? 'high' : 'medium'
    }))
  }
  
  // 如果没有解析到 summary，使用整个内容作为 summary
  if (!data.summary && !data.rootCause) {
    data.summary = text.slice(0, 200) + (text.length > 200 ? '...' : '')
    data.rootCause = text
  }
  
  return data
})

const hasData = computed(() => {
  return parsedData.value.summary || parsedData.value.rootCause || parsedData.value.suggestions.length > 0
})

const getPriorityColor = (priority) => {
  const colors = {
    high: '#ef4444',
    medium: '#f59e0b',
    low: '#22c55e',
  }
  return colors[priority] || '#6b7280'
}

const getPriorityLabel = (priority) => {
  const labels = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级',
  }
  return labels[priority] || priority
}
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog-backdrop">
      <div v-if="visible" class="ai-analysis-modal" @click.self="visible = false">
        <Transition name="dialog-scale">
          <div v-if="visible" class="ai-analysis-card">
            <!-- 头部 -->
            <div class="ai-header">
              <div class="ai-title-wrapper">
                <div class="ai-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                  </svg>
                </div>
                <div class="ai-title-group">
                  <h2 class="ai-title">{{ title }}</h2>
                  <span class="ai-subtitle">智能错题分析助手</span>
                </div>
              </div>
              <button class="ai-close-btn" @click="visible = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="ai-loading">
              <div class="ai-loading-spinner"></div>
              <p>AI 正在分析中...</p>
            </div>

            <!-- 内容区域 -->
            <div v-else class="ai-content">
              <!-- 标签页 -->
              <div class="ai-tabs">
                <button 
                  :class="['ai-tab', { active: activeTab === 'analysis' }]"
                  @click="activeTab = 'analysis'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 16v-4M12 8h.01"/>
                  </svg>
                  <span>错误分析</span>
                </button>
                <button 
                  :class="['ai-tab', { active: activeTab === 'suggestions' }]"
                  @click="activeTab = 'suggestions'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 20h9M12 20l-4-4m4 4l-4 4M12 4v12M5 12l7-7 7 7"/>
                  </svg>
                  <span>改进建议</span>
                </button>
              </div>

              <!-- 错误分析面板 -->
              <div v-if="activeTab === 'analysis'" class="ai-panel">
                <!-- 总结卡片 -->
                <div class="ai-summary-box">
                  <div class="ai-summary-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                    </svg>
                  </div>
                  <div class="ai-summary-content">
                    <h3>分析总结</h3>
                    <p>{{ parsedData.summary || '暂无分析总结' }}</p>
                  </div>
                </div>

                <!-- 错误原因 -->
                <div class="ai-section" v-if="parsedData.rootCause || parsedData.explanation">
                  <div class="ai-section-header">
                    <div class="ai-section-icon error">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="15" y1="9" x2="9" y2="15"/>
                        <line x1="9" y1="9" x2="15" y2="15"/>
                      </svg>
                    </div>
                    <h4>错误原因</h4>
                  </div>
                  <div class="ai-section-body">
                    <p>{{ parsedData.rootCause || parsedData.explanation }}</p>
                  </div>
                </div>

                <!-- 详细解释 -->
                <div class="ai-section" v-if="parsedData.explanation && parsedData.rootCause">
                  <div class="ai-section-header">
                    <div class="ai-section-icon info">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <path d="M12 16v-4M12 8h.01"/>
                      </svg>
                    </div>
                    <h4>详细解释</h4>
                  </div>
                  <div class="ai-section-body">
                    <p>{{ parsedData.explanation }}</p>
                  </div>
                </div>
              </div>

              <!-- 改进建议面板 -->
              <div v-if="activeTab === 'suggestions'" class="ai-panel">
                <div v-if="parsedData.suggestions.length > 0" class="ai-suggestions-list">
                  <div 
                    v-for="(suggestion, index) in parsedData.suggestions" 
                    :key="index"
                    class="ai-suggestion-item"
                  >
                    <div class="ai-suggestion-header">
                      <div class="ai-suggestion-number">{{ index + 1 }}</div>
                      <h4>{{ suggestion.title }}</h4>
                      <span 
                        class="ai-priority-badge"
                        :style="{ 
                          backgroundColor: getPriorityColor(suggestion.priority) + '20',
                          color: getPriorityColor(suggestion.priority)
                        }"
                      >
                        {{ getPriorityLabel(suggestion.priority) }}
                      </span>
                    </div>
                    <p class="ai-suggestion-text">{{ suggestion.description }}</p>
                  </div>
                </div>
                <div v-else class="ai-empty">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 16v-4M12 8h.01"/>
                  </svg>
                  <p>暂无改进建议</p>
                </div>
              </div>
            </div>

            <!-- 底部 -->
            <div class="ai-footer">
              <button class="ai-btn-secondary" @click="visible = false">关闭</button>
              <button class="ai-btn-refresh" @click="emit('refresh')" :disabled="loading">
                <svg v-if="!loading" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/>
                </svg>
                <span v-else class="ai-spinner-small"></span>
                {{ loading ? '分析中...' : '重新分析' }}
              </button>
              <button class="ai-btn-primary" @click="activeTab = 'suggestions'" v-if="activeTab === 'analysis' && parsedData.suggestions.length > 0">
                查看建议
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              </button>
              <button class="ai-btn-primary" @click="activeTab = 'analysis'" v-else-if="activeTab === 'suggestions'">
                返回分析
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M19 12H5M12 19l-7-7 7-7"/>
                </svg>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ================================
   AI 智能分析对话框 - 全新设计
   玻璃拟态 + 渐变 + 动画
   ================================ */

/* 遮罩层 */
.ai-analysis-modal {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
}

/* 对话框卡片 */
.ai-analysis-card {
  width: 100%;
  max-width: 640px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 24px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  overflow: hidden;
}

/* 头部 */
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
}

.ai-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 16px;
  color: white;
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.3);
}

.ai-icon svg {
  width: 24px;
  height: 24px;
}

.ai-title-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ai-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.ai-subtitle {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.ai-close-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ai-close-btn:hover {
  background: rgba(226, 232, 240, 0.8);
  color: #0f172a;
  transform: rotate(90deg);
}

.ai-close-btn svg {
  width: 20px;
  height: 20px;
}

/* 加载状态 */
.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  gap: 20px;
}

.ai-loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(79, 70, 229, 0.1);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.ai-loading p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
}

/* 内容区域 */
.ai-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.5) transparent;
}

.ai-content::-webkit-scrollbar {
  width: 6px;
}

.ai-content::-webkit-scrollbar-track {
  background: transparent;
}

.ai-content::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.5);
  border-radius: 3px;
}

/* 标签页 */
.ai-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  padding: 6px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.ai-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  background: transparent;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ai-tab svg {
  width: 18px;
  height: 18px;
}

.ai-tab:hover {
  color: #475569;
  background: rgba(255, 255, 255, 0.6);
}

.ai-tab.active {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  font-weight: 600;
}

/* 面板 */
.ai-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 总结卡片 */
.ai-summary-box {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.ai-summary-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 14px;
  color: #f59e0b;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.25);
}

.ai-summary-icon svg {
  width: 22px;
  height: 22px;
}

.ai-summary-content h3 {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: #92400e;
}

.ai-summary-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #78350f;
}

/* 内容区块 */
.ai-section {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.ai-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.ai-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.ai-section-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.ai-section-icon svg {
  width: 18px;
  height: 18px;
}

.ai-section-icon.error {
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  color: #ef4444;
}

.ai-section-icon.info {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  color: #3b82f6;
}

.ai-section-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.ai-section-body p {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #475569;
}

/* 建议列表 */
.ai-suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ai-suggestion-item {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.ai-suggestion-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.ai-suggestion-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.ai-suggestion-number {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
}

.ai-suggestion-header h4 {
  flex: 1;
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
}

.ai-priority-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.ai-suggestion-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
}

/* 空状态 */
.ai-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  color: #94a3b8;
}

.ai-empty svg {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.ai-empty p {
  margin: 0;
  font-size: 14px;
}

/* 底部 */
.ai-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 28px;
  background: linear-gradient(180deg, #fafbfc 0%, #f8fafc 100%);
  border-top: 1px solid rgba(226, 232, 240, 0.8);
}

.ai-btn-secondary {
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ai-btn-secondary:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.ai-btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.ai-btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.4);
}

.ai-btn-primary svg {
  width: 16px;
  height: 16px;
}

/* 重新分析按钮 */
.ai-btn-refresh {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ai-btn-refresh:hover:not(:disabled) {
  border-color: #4f46e5;
  color: #4f46e5;
  background: #f5f3ff;
}

.ai-btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.ai-btn-refresh svg {
  width: 16px;
  height: 16px;
}

.ai-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(79, 70, 229, 0.2);
  border-top-color: #4f46e5;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* 过渡动画 */
.dialog-backdrop-enter-active,
.dialog-backdrop-leave-active {
  transition: opacity 0.3s ease;
}

.dialog-backdrop-enter-from,
.dialog-backdrop-leave-to {
  opacity: 0;
}

.dialog-scale-enter-active,
.dialog-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dialog-scale-enter-from,
.dialog-scale-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

/* 响应式 */
@media (max-width: 640px) {
  .ai-analysis-modal {
    padding: 16px;
  }
  
  .ai-analysis-card {
    max-height: 90vh;
  }
  
  .ai-header {
    padding: 20px;
  }
  
  .ai-icon {
    width: 40px;
    height: 40px;
  }
  
  .ai-title {
    font-size: 18px;
  }
  
  .ai-content {
    padding: 20px;
  }
  
  .ai-tabs {
    flex-wrap: wrap;
  }
  
  .ai-tab {
    flex: 1;
    justify-content: center;
    padding: 10px 16px;
  }
  
  .ai-summary-box {
    flex-direction: column;
    text-align: center;
  }
  
  .ai-footer {
    padding: 16px 20px;
    flex-direction: column;
  }
  
  .ai-btn-secondary,
  .ai-btn-primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
