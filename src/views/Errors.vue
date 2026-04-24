<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import ErrorAnalysisDialog from '../components/ErrorAnalysisDialog.vue'
import FlowerPagination from '../components/FlowerPagination.vue'
import { useErrorStore } from '../stores/error'
import { useUserStore } from '../stores/user'

const router = useRouter()
const errorStore = useErrorStore()
const userStore = useUserStore()

const loading = ref(false)
const analysisLoading = ref(false)
const completingId = ref(null)
const addDialogVisible = ref(false)
const analysisDialogVisible = ref(false)
const analysisText = ref('')
const analysisData = ref(null)
const currentPage = ref(1)
const pageSize = 5

const viewMode = ref('active')
const sortBy = ref('urgent')
const subjectFilter = ref('all')

const addForm = reactive({
  question: '',
  userAnswer: '',
  description: '',
})

const currentAnalysisItem = ref(null)

const ANALYSIS_NOTICE_PREFIX = 'ai-analysis-limit-notice'

const getAnalysisUserKey = () => {
  const userId = userStore.userInfo?.id
  return userId != null ? String(userId) : 'guest'
}

const getAnalysisNoticeKey = () => `${ANALYSIS_NOTICE_PREFIX}:${getAnalysisUserKey()}`

const getAnalysisQuotaStatus = () => ({
  currentSlot: 'server',
  usedSlots: [],
  usedCount: 0,
  remainingCount: 2,
  canUseCurrentSlot: true,
  isExhausted: false,
})

const consumeAnalysisSlot = () => []

const ensureAnalysisNotice = async () => {
  if (localStorage.getItem(getAnalysisNoticeKey()) === '1') return

  await ElMessageBox.alert(
    'AI 分析每天最多可使用 2 次：每天 00:00 和 12:00 各刷新 1 次，你可以在对应时间段内提前使用。次数用完后，系统会默认保留并展示最后一次生成的数据，请合理分析。',
    '使用提示',
    {
      confirmButtonText: '我知道了',
      type: 'warning',
    },
  )

  localStorage.setItem(getAnalysisNoticeKey(), '1')
}

const inferSubject = (item) => {
  const text = `${item.title || ''} ${item.question || ''} ${item.description || ''}`
  if (/树|链表|队列|栈|哈希|图|并查集/i.test(text)) return '数据结构'
  if (/动态规划|二分|贪心|双指针|滑动窗口|递归|回溯/i.test(text)) return '算法'
  if (/复杂度|时间复杂度|空间复杂度/i.test(text)) return '复杂度'
  return '综合'
}

const toTimestamp = (value) => {
  const time = new Date(value || '').getTime()
  return Number.isFinite(time) ? time : 0
}

const formatTime = (value) => {
  if (!value) return '时间未知'

  const time = toTimestamp(value)
  if (!time) return String(value)

  const diffMs = Date.now() - time
  const diffMinutes = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays < 7) return `${diffDays} 天前`

  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatAnswer = (value, maxLength = 50) => {
  if (!value) return '暂无记录'

  let result = ''
  if (Array.isArray(value)) {
    result = value.map((item) => String(item).trim()).filter(Boolean).join('、') || '暂无记录'
  } else {
    try {
      const parsed = JSON.parse(value)
      if (Array.isArray(parsed)) {
        result = parsed.map((item) => String(item).trim()).filter(Boolean).join('、') || '暂无记录'
      } else {
        result = String(value)
      }
    } catch (error) {
      result = String(value)
    }
  }

  // 截断长文本
  if (result.length > maxLength) {
    return result.substring(0, maxLength) + '...'
  }
  return result
}

const getUrgency = (item) => {
  if (item.analysis) return 1
  const ageDays = Math.max(0, (Date.now() - toTimestamp(item.updatedAt || item.createdAt)) / (24 * 60 * 60 * 1000))
  return ageDays >= 2 ? 3 : 2
}

const decorateActiveItem = (item) => ({
  ...item,
  subject: inferSubject(item),
  timeLabel: '加入错题本',
  timeText: formatTime(item.updatedAt || item.createdAt),
  statusLabel: item.analysis ? '待巩固' : '立即复习',
  statusType: item.analysis ? 'normal' : 'urgent',
  urgency: getUrgency(item),
  isCompleted: false,
})

const decorateCompletedItem = (item) => ({
  ...item,
  subject: inferSubject(item),
  timeLabel: '完成时间',
  timeText: formatTime(item.completedAt || item.lastErrorAt || item.createdAt),
  statusLabel: '已完成',
  statusType: 'mastered',
  urgency: 0,
  isCompleted: true,
})

const activeItems = computed(() => errorStore.errors.map(decorateActiveItem))
const completedItems = computed(() => errorStore.completedErrors.map(decorateCompletedItem))

const subjectOptions = computed(() => {
  const source = viewMode.value === 'completed' ? completedItems.value : activeItems.value
  const set = new Set(source.map((item) => item.subject))
  return ['all', ...Array.from(set)]
})

const visibleSourceItems = computed(() => (
  viewMode.value === 'completed' ? completedItems.value : activeItems.value
))

const filteredItems = computed(() => {
  let list = [...visibleSourceItems.value]

  if (subjectFilter.value !== 'all') {
    list = list.filter((item) => item.subject === subjectFilter.value)
  }

  if (sortBy.value === 'oldest') {
    list.sort((a, b) => toTimestamp(a.completedAt || a.updatedAt || a.createdAt) - toTimestamp(b.completedAt || b.updatedAt || b.createdAt))
    return list
  }

  if (sortBy.value === 'latest' || viewMode.value === 'completed') {
    list.sort((a, b) => toTimestamp(b.completedAt || b.updatedAt || b.createdAt) - toTimestamp(a.completedAt || a.updatedAt || a.createdAt))
    return list
  }

  list.sort((a, b) => b.urgency - a.urgency || toTimestamp(b.updatedAt || b.createdAt) - toTimestamp(a.updatedAt || a.createdAt))
  return list
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredItems.value.length / pageSize)))

const visibleItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredItems.value.slice(start, start + pageSize)
})

const activeCount = computed(() => activeItems.value.length)
const completedCount = computed(() => completedItems.value.length)
const totalCount = computed(() => activeCount.value + completedCount.value)
const completionRate = computed(() => {
  if (!totalCount.value) return 0
  return Math.round((completedCount.value / totalCount.value) * 100)
})

watch([viewMode, sortBy, subjectFilter], () => {
  currentPage.value = 1
})

watch(viewMode, (nextMode) => {
  subjectFilter.value = 'all'
  if (nextMode === 'completed' && sortBy.value === 'urgent') {
    sortBy.value = 'latest'
  }
})

watch(filteredItems, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
}, { immediate: true })

const loadData = async () => {
  loading.value = true
  try {
    await errorStore.refreshAll()
  } catch (error) {
    console.error('错题数据加载失败:', error)
    ElMessage.error(error?.message || '错题数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const openAnalysisDialog = (content, data = null) => {
  analysisText.value = content || ''
  analysisData.value = data
  analysisDialogVisible.value = true
}

const resolveCachedAnalysis = (item) => {
  if (!item?.id) return null

  const cached = errorStore.getCachedAnalysisResult(item.id)
  const content = item.analysis || cached?.analysis || ''
  const data = item.analysisData || cached?.analysisData || null

  if (!content && !data) {
    return null
  }

  return {
    analysis: content,
    analysisData: data,
  }
}

const openCachedAnalysis = (item, message = '') => {
  const cached = resolveCachedAnalysis(item)
  if (!cached) return false

  currentAnalysisItem.value = item
  analysisLoading.value = false
  openAnalysisDialog(cached.analysis, cached.analysisData)

  if (message) {
    ElMessage.info(message)
  }

  return true
}

const handleAnalyze = async (item, forceRefresh = false) => {
  if (analysisLoading.value) return

  currentAnalysisItem.value = item

  // 如果有已有分析且不是强制刷新，直接显示
  if (!forceRefresh && openCachedAnalysis(item)) {
    return
  }

  await ensureAnalysisNotice()

  const quotaStatus = getAnalysisQuotaStatus()
  if (!quotaStatus.canUseCurrentSlot) {
    const fallbackMessage = quotaStatus.isExhausted
      ? '今日 AI 分析次数已用完，已为你展示最后一次生成的分析结果。'
      : '当前时间段的 AI 分析机会已使用，已为你展示最后一次生成的分析结果。'

    if (openCachedAnalysis(item, fallbackMessage)) {
      return
    }

    ElMessage.warning(
      quotaStatus.isExhausted
        ? '今日 AI 分析次数已用完，请在中午 12:00 或次日 00:00 后再试。'
        : '当前时间段的 AI 分析机会已使用，请等待下一个刷新时间后再试。',
    )
    return
  }

  analysisLoading.value = true
  analysisDialogVisible.value = true
  analysisText.value = ''
  analysisData.value = null

  try {
    const result = await errorStore.getAnalysis(item)
    analysisText.value = result.analysis
    analysisData.value = result.analysisData
    errorStore.markAnalysis(item.id, result.analysis, result.analysisData)
    consumeAnalysisSlot()
    if (result.message) {
      ElMessage.info(result.message)
    }
  } catch (error) {
    analysisDialogVisible.value = false
    ElMessage.error(error?.response?.data?.message || error?.message || 'AI 分析失败，请稍后再试')
  } finally {
    analysisLoading.value = false
  }
}

const handleReview = (item) => {
  const levelId = Number(item.levelId)
  if (!levelId) {
    ElMessage.info('这道错题没有关联关卡，暂时无法直接跳转复习。')
    return
  }
  router.push(`/challenge/${levelId}`)
}

const handleRefreshAnalysis = async () => {
  if (!currentAnalysisItem.value) return
  await handleAnalyze(currentAnalysisItem.value, true)
}

const handleMarkCompleted = async (item) => {
  completingId.value = item.id
  try {
    await errorStore.completeError(item.id)
    ElMessage.success('已移动到“已完成错题”')
    if (viewMode.value === 'active' && !filteredItems.value.length && currentPage.value > 1) {
      currentPage.value -= 1
    }
  } catch (error) {
    ElMessage.error(error?.message || '错题归档失败，请稍后重试')
  } finally {
    completingId.value = null
  }
}

const resetAddForm = () => {
  addForm.question = ''
  addForm.userAnswer = ''
  addForm.description = ''
}

const handleAdd = async () => {
  if (!addForm.question || !addForm.userAnswer || !addForm.description) {
    ElMessage.warning('请完整填写错题信息')
    return
  }

  try {
    await errorStore.addError({
      question: addForm.question,
      userAnswer: addForm.userAnswer,
      description: addForm.description,
    })
    ElMessage.success('错题已加入错题本')
    addDialogVisible.value = false
    resetAddForm()
    viewMode.value = 'active'
  } catch (error) {
    ElMessage.error(error?.message || '错题保存失败，请稍后重试')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container errors-page">
    <div class="stats-showcase">
      <div class="stat-card total">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ activeCount }}</span>
          <span class="stat-label">当前错题</span>
        </div>
      </div>

      <div class="stat-card completed">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 6 9 17l-5-5" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ completedCount }}</span>
          <span class="stat-label">已完成错题</span>
        </div>
      </div>

      <div class="stat-card mastery">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ completionRate }}<small>%</small></span>
          <span class="stat-label">完成率</span>
        </div>
      </div>
    </div>

    <div class="toolbar-row">
      <div class="board-switch">
        <button class="switch-btn" :class="{ active: viewMode === 'active' }" type="button"
          @click="viewMode = 'active'">
          当前错题
        </button>
        <button class="switch-btn" :class="{ active: viewMode === 'completed' }" type="button"
          @click="viewMode = 'completed'">
          已完成
        </button>
      </div>

      <div class="filter-row">
        <el-select v-model="sortBy" class="filter-select wide">
          <el-option label="排序：优先待复习" value="urgent" />
          <el-option label="排序：最新" value="latest" />
          <el-option label="排序：最早" value="oldest" />
        </el-select>
        <el-select v-model="subjectFilter" class="filter-select">
          <el-option label="科目：全部" value="all" />
          <el-option v-for="subject in subjectOptions.filter((item) => item !== 'all')" :key="subject"
            :label="`科目：${subject}`" :value="subject" />
        </el-select>
        <el-button type="primary" class="add-error-btn" @click="addDialogVisible = true">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"
            style="margin-right: 6px;">
            <path d="M12 5v14M5 12h14" />
          </svg>
          手动添加错题
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="error-list">
      <article v-for="item in visibleItems" :key="`${viewMode}-${item.id}`" class="error-card"
        :class="{ done: item.isCompleted }">
        <div class="card-main">
          <div class="card-tags">
            <span class="subject-chip">{{ item.subject }}</span>
            <span class="time-chip">{{ item.timeLabel }} · {{ item.timeText }}</span>
          </div>

          <h3 class="card-title">{{ item.title || item.question }}</h3>
          <p v-if="item.title && item.question" class="card-question">{{ item.question }}</p>
          <p class="card-answer">你的答案：{{ formatAnswer(item.userAnswer) }}</p>
          <p v-if="item.correctAnswer" class="card-answer secondary">正确答案：{{ formatAnswer(item.correctAnswer) }}</p>
          <p v-if="item.description" class="card-desc">{{ item.description }}</p>

          <div class="card-foot">
            <div class="status-group">
              <span class="status-chip" :class="item.statusType">{{ item.statusLabel }}</span>
              <el-button v-if="item.levelId" size="small" type="primary" plain @click="handleReview(item)">
                {{ item.isCompleted ? '再练一次' : '去复习' }}
              </el-button>
              <el-button v-if="!item.isCompleted" size="small" type="success" plain :loading="completingId === item.id"
                @click="handleMarkCompleted(item)">
                标记完成
              </el-button>
            </div>

            <div class="action-links">
              <el-button v-if="item.analysis || !item.isCompleted" link type="primary" @click="handleAnalyze(item)">
                {{ item.analysis ? '查看分析' : 'AI 分析' }}
              </el-button>
            </div>
          </div>
        </div>
      </article>

      <div v-if="!visibleItems.length && !loading" class="empty-box">
        <div class="empty-icon">✓</div>
        <div class="empty-text">
          {{ viewMode === 'active' ? '当前没有待处理的错题，继续保持。' : '还没有已完成的错题，完成复习后会自动归档到这里。' }}
        </div>
        <el-button v-if="viewMode === 'active'" type="primary" class="add-error-btn" @click="addDialogVisible = true">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5"
            style="margin-right: 6px;">
            <path d="M12 5v14M5 12h14" />
          </svg>
          手动添加错题
        </el-button>
      </div>
    </div>

    <FlowerPagination v-if="totalPages > 1" :total="totalPages" :default-page="currentPage"
      @change="currentPage = $event" />

    <el-dialog v-model="addDialogVisible" title="添加错题" width="560px">
      <el-form label-width="88px">
        <el-form-item label="题目">
          <el-input v-model="addForm.question" />
        </el-form-item>
        <el-form-item label="错误答案">
          <el-input v-model="addForm.userAnswer" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">
          <img src="../assets/mini_icons/保存.png" alt="save" style="width: 16px; height: 16px; margin-right: 4px; border-radius: 3px; object-fit: cover; vertical-align: middle;" />
          保存
        </el-button>
      </template>
    </el-dialog>

    <ErrorAnalysisDialog v-model="analysisDialogVisible" :loading="analysisLoading" :content="analysisText"
      :analysis-data="analysisData" @refresh="handleRefreshAnalysis" />
  </div>
</template>

<style scoped>
.errors-page {
  --chip-bg: #eef3f9;
  --chip-text: #4a6486;
  --status-urgent-bg: #f8edf0;
  --status-urgent-border: transparent;
  --status-urgent-text: #8f5f6c;
  --status-normal-bg: #f6f1ea;
  --status-normal-border: transparent;
  --status-normal-text: #8b7053;
  --status-mastered-bg: #edf2f8;
  --status-mastered-border: transparent;
  --status-mastered-text: #5f7287;
  display: grid;
  gap: 16px;
  padding-bottom: 28px;
}

.stats-showcase {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.stat-card {
  padding: 24px;
  border-radius: 24px;
  color: #fff;
  box-shadow: 0 20px 50px -30px rgba(30, 41, 59, 0.5);
}

.stat-card.total {
  background: linear-gradient(135deg, #547aa5 0%, #3f5d85 100%);
}

.stat-card.completed {
  background: linear-gradient(135deg, #5d9270 0%, #3d6f52 100%);
}

.stat-card.mastery {
  background: linear-gradient(135deg, #9a7bb2 0%, #755190 100%);
}

.stat-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
}

.stat-icon svg {
  width: 100%;
  height: 100%;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-number {
  font-size: 42px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -1px;
}

.stat-number small {
  font-size: 24px;
  margin-left: 4px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

.toolbar-row {
  display: grid;
  gap: 14px;
}

.board-switch {
  display: inline-flex;
  width: fit-content;
  padding: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  border: 1px solid rgba(141, 161, 185, 0.24);
  backdrop-filter: blur(10px);
}

.switch-btn {
  border: none;
  background: transparent;
  color: var(--text-sub);
  padding: 10px 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.switch-btn.active {
  background: linear-gradient(135deg, #4f6a8d 0%, #6e88aa 100%);
  color: #fff;
  box-shadow: 0 12px 24px -18px rgba(63, 93, 133, 0.8);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.filter-select {
  width: 180px;
}

.filter-select.wide {
  width: 220px;
}

/* el-select 输入框圆角 */
:deep(.filter-select .el-input__wrapper) {
  border-radius: 20px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  padding: 4px 16px;
  border: none;
}

/* el-select 下拉菜单圆角 */
:deep(.el-select-dropdown) {
  border-radius: 18px !important;
  overflow: hidden;
}

:deep(.filter-select .el-input__wrapper.is-focus),
:deep(.filter-select .el-input__wrapper:hover) {
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.15) !important;
}

:deep(.add-error-btn) {
  border-radius: 20px;
  padding: 8px 24px;
  height: 40px;
  border: none;
  background: linear-gradient(135deg, #4a90d9, #6672cb);
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.3);
  font-weight: 600;
  transition: all 0.3s;
  display: inline-flex;
  align-items: center;
}

:deep(.add-error-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 144, 217, 0.4);
  background: linear-gradient(135deg, #5b9ce0, #737ed3);
}

.error-list {
  display: grid;
  gap: 16px;
}

.error-card {
  display: grid;
  grid-template-columns: 1fr;
  border: none;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.error-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.95);
}

.error-card.done {
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
}

.card-main {
  display: grid;
  gap: 10px;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.subject-chip {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--chip-bg);
  color: var(--chip-text);
  font-size: 12px;
  font-weight: 700;
}

.time-chip {
  color: var(--text-sub);
  font-size: 12px;
}

.card-title {
  margin: 0;
  color: var(--text-title);
  font-size: 18px;
  font-weight: 700;
}

.card-question,
.card-answer,
.card-desc {
  margin: 0;
  color: var(--text-sub);
  font-size: 13px;
  line-height: 1.7;
}

.card-answer.secondary {
  color: #5d7a62;
}

.card-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-group,
.action-links {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  line-height: 1;
  font-weight: 700;
}

.status-chip.urgent {
  color: var(--status-urgent-text);
  background: var(--status-urgent-bg);
  border-color: var(--status-urgent-border);
}

.status-chip.normal {
  color: var(--status-normal-text);
  background: var(--status-normal-bg);
  border-color: var(--status-normal-border);
}

.status-chip.mastered {
  color: var(--status-mastered-text);
  background: var(--status-mastered-bg);
  border-color: var(--status-mastered-border);
}

.empty-box {
  display: grid;
  gap: 14px;
  place-items: center;
  padding: 42px 20px;
  border-radius: 20px;
  border: 2px dashed rgba(74, 144, 217, 0.3);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 30px;
  font-weight: 800;
  color: #4f6a8d;
  background: linear-gradient(135deg, rgba(79, 106, 141, 0.12), rgba(111, 136, 170, 0.28));
}

.empty-text {
  color: var(--text-sub);
  font-size: 14px;
  line-height: 1.8;
}

@media (max-width: 900px) {
  .stats-showcase {
    grid-template-columns: 1fr;
  }

  .toolbar-row {
    gap: 12px;
  }

  .filter-select,
  .filter-select.wide {
    width: 100%;
  }
}
</style>
