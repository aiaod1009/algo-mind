<script setup>
import { computed, ref } from 'vue'

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

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const activeTab = ref('analysis')

const hasStructuredData = computed(() => {
  return props.analysisData && Object.keys(props.analysisData).length > 0
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

const getDifficultyColor = (difficulty) => {
  const colors = {
    easy: '#22c55e',
    medium: '#f59e0b',
    hard: '#ef4444',
  }
  return colors[difficulty] || '#6b7280'
}

const getDifficultyLabel = (difficulty) => {
  const labels = {
    easy: '简单',
    medium: '中等',
    hard: '困难',
  }
  return labels[difficulty] || difficulty
}

const getMasteryLabel = (level) => {
  const labels = {
    beginner: '入门',
    intermediate: '进阶',
    advanced: '高级',
  }
  return labels[level] || level
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="800px"
    class="analysis-dialog"
    :close-on-click-modal="false"
  >
    <div class="analysis-container">
      <el-skeleton :loading="loading" animated :rows="8">
        <template v-if="hasStructuredData">
          <div class="analysis-tabs">
            <button
              :class="['tab-btn', { active: activeTab === 'analysis' }]"
              @click="activeTab = 'analysis'"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              错误分析
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'knowledge' }]"
              @click="activeTab = 'knowledge'"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
                <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
              </svg>
              知识点
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'suggestions' }]"
              @click="activeTab = 'suggestions'"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
              改进建议
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'plan' }]"
              @click="activeTab = 'plan'"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              学习计划
            </button>
          </div>

          <div class="analysis-summary" v-if="analysisData.summary">
            <div class="summary-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <p class="summary-text">{{ analysisData.summary }}</p>
          </div>

          <Transition name="fade" mode="out-in">
            <div :key="activeTab" class="tab-content">
              <template v-if="activeTab === 'analysis' && analysisData.errorAnalysis">
                <div class="section-card error-analysis">
                  <div class="section-header">
                    <div class="section-icon error">
                      <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/>
                        <line x1="15" y1="9" x2="9" y2="15"/>
                        <line x1="9" y1="9" x2="15" y2="15"/>
                      </svg>
                    </div>
                    <h3>根本原因</h3>
                  </div>
                  <p class="root-cause">{{ analysisData.errorAnalysis.rootCause }}</p>
                  <p class="explanation">{{ analysisData.errorAnalysis.detailedExplanation }}</p>
                  
                  <div class="common-mistakes" v-if="analysisData.errorAnalysis.commonMistakes?.length">
                    <h4>常见错误</h4>
                    <ul>
                      <li v-for="(mistake, idx) in analysisData.errorAnalysis.commonMistakes" :key="idx">
                        {{ mistake }}
                      </li>
                    </ul>
                  </div>
                </div>
              </template>

              <template v-else-if="activeTab === 'knowledge' && analysisData.knowledgePoints?.length">
                <div class="knowledge-grid">
                  <div 
                    v-for="(kp, idx) in analysisData.knowledgePoints" 
                    :key="idx"
                    class="knowledge-card"
                  >
                    <div class="kp-header">
                      <span class="kp-name">{{ kp.name }}</span>
                      <span class="kp-level" :class="kp.masteryLevel">
                        {{ getMasteryLabel(kp.masteryLevel) }}
                      </span>
                    </div>
                    <p class="kp-desc">{{ kp.description }}</p>
                    <div class="kp-resources" v-if="kp.relatedResources?.length">
                      <span class="resource-label">推荐资源：</span>
                      <span v-for="(res, i) in kp.relatedResources" :key="i" class="resource-tag">
                        {{ res }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>

              <template v-else-if="activeTab === 'suggestions' && analysisData.suggestions?.length">
                <div class="suggestions-list">
                  <div 
                    v-for="(s, idx) in analysisData.suggestions" 
                    :key="idx"
                    class="suggestion-card"
                  >
                    <div class="suggestion-header">
                      <div class="suggestion-icon">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M9 11l3 3L22 4"/>
                          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
                        </svg>
                      </div>
                      <div class="suggestion-title">
                        <h4>{{ s.title }}</h4>
                        <span 
                          class="priority-badge"
                          :style="{ backgroundColor: getPriorityColor(s.priority) }"
                        >
                          {{ getPriorityLabel(s.priority) }}
                        </span>
                      </div>
                    </div>
                    <p class="suggestion-desc">{{ s.description }}</p>
                    <ul class="action-items" v-if="s.actionItems?.length">
                      <li v-for="(item, i) in s.actionItems" :key="i">{{ item }}</li>
                    </ul>
                  </div>
                </div>

                <div class="recommended-section" v-if="analysisData.recommendedProblems?.length">
                  <h4>推荐练习</h4>
                  <div class="recommended-grid">
                    <div 
                      v-for="(p, idx) in analysisData.recommendedProblems" 
                      :key="idx"
                      class="recommended-card"
                    >
                      <div class="rec-header">
                        <span class="rec-title">{{ p.title }}</span>
                        <span 
                          class="rec-difficulty"
                          :style="{ color: getDifficultyColor(p.difficulty) }"
                        >
                          {{ getDifficultyLabel(p.difficulty) }}
                        </span>
                      </div>
                      <p class="rec-reason">{{ p.reason }}</p>
                      <span class="rec-source">{{ p.source }}</span>
                    </div>
                  </div>
                </div>
              </template>

              <template v-else-if="activeTab === 'plan' && analysisData.studyPlan">
                <div class="study-plan">
                  <div class="plan-timeline">
                    <div class="plan-item short">
                      <div class="plan-dot"></div>
                      <div class="plan-content">
                        <span class="plan-label">本周目标</span>
                        <p>{{ analysisData.studyPlan.shortTerm }}</p>
                      </div>
                    </div>
                    <div class="plan-item mid">
                      <div class="plan-dot"></div>
                      <div class="plan-content">
                        <span class="plan-label">本月目标</span>
                        <p>{{ analysisData.studyPlan.midTerm }}</p>
                      </div>
                    </div>
                    <div class="plan-item long">
                      <div class="plan-dot"></div>
                      <div class="plan-content">
                        <span class="plan-label">本季度目标</span>
                        <p>{{ analysisData.studyPlan.longTerm }}</p>
                      </div>
                    </div>
                  </div>

                  <div class="daily-tasks" v-if="analysisData.studyPlan.dailyTasks?.length">
                    <h4>每日任务</h4>
                    <div class="task-list">
                      <div 
                        v-for="(task, idx) in analysisData.studyPlan.dailyTasks" 
                        :key="idx"
                        class="task-item"
                      >
                        <span class="task-check">☐</span>
                        <span class="task-text">{{ task }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </Transition>
        </template>

        <template v-else>
          <div class="text-analysis">
            <pre>{{ content || '暂无分析结果' }}</pre>
          </div>
        </template>
      </el-skeleton>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button type="primary" v-if="hasStructuredData" @click="activeTab = 'plan'">
          查看学习计划
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.analysis-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.analysis-container {
  max-height: 60vh;
  overflow-y: auto;
  padding: 20px;
}

.analysis-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  background: #f3f4f6;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #6b7280;
  transition: all 0.2s;
}

.tab-btn:hover {
  background: #e5e7eb;
}

.tab-btn.active {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
}

.analysis-summary {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.summary-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 12px;
  color: #f59e0b;
}

.summary-text {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  color: #92400e;
}

.section-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.section-icon.error {
  background: #fef2f2;
  color: #ef4444;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.root-cause {
  font-size: 16px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 12px;
}

.explanation {
  color: #6b7280;
  line-height: 1.7;
  margin-bottom: 16px;
}

.common-mistakes h4 {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.common-mistakes ul {
  margin: 0;
  padding-left: 20px;
}

.common-mistakes li {
  color: #ef4444;
  margin-bottom: 4px;
}

.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.knowledge-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.knowledge-card:hover {
  border-color: #4f46e5;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.1);
}

.kp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.kp-name {
  font-weight: 600;
  color: #1f2937;
}

.kp-level {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}

.kp-level.beginner {
  background: #dcfce7;
  color: #16a34a;
}

.kp-level.intermediate {
  background: #fef3c7;
  color: #d97706;
}

.kp-level.advanced {
  background: #fee2e2;
  color: #dc2626;
}

.kp-desc {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 12px;
}

.kp-resources {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.resource-label {
  font-size: 12px;
  color: #9ca3af;
}

.resource-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #f3f4f6;
  border-radius: 4px;
  color: #4b5563;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.suggestion-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.suggestion-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ecfdf5;
  color: #10b981;
  border-radius: 8px;
}

.suggestion-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.suggestion-title h4 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.priority-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  color: white;
}

.suggestion-desc {
  color: #6b7280;
  margin-bottom: 12px;
}

.action-items {
  margin: 0;
  padding-left: 20px;
}

.action-items li {
  color: #4b5563;
  margin-bottom: 4px;
}

.recommended-section {
  margin-top: 24px;
}

.recommended-section h4 {
  font-size: 16px;
  color: #1f2937;
  margin-bottom: 12px;
}

.recommended-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.recommended-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
}

.rec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.rec-title {
  font-weight: 500;
  color: #1f2937;
}

.rec-difficulty {
  font-size: 12px;
  font-weight: 500;
}

.rec-reason {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.rec-source {
  font-size: 12px;
  color: #9ca3af;
}

.study-plan {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.plan-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.plan-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.plan-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
}

.plan-item.short .plan-dot {
  background: #22c55e;
}

.plan-item.mid .plan-dot {
  background: #f59e0b;
}

.plan-item.long .plan-dot {
  background: #8b5cf6;
}

.plan-content {
  flex: 1;
}

.plan-label {
  font-size: 13px;
  color: #6b7280;
  display: block;
  margin-bottom: 4px;
}

.plan-content p {
  margin: 0;
  font-size: 15px;
  color: #1f2937;
}

.daily-tasks h4 {
  font-size: 16px;
  color: #1f2937;
  margin-bottom: 12px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.task-check {
  font-size: 18px;
  color: #9ca3af;
}

.task-text {
  color: #4b5563;
}

.text-analysis {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.text-analysis pre {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #374151;
  font-family: inherit;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
