<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ErrorAnalysisDialog from '../components/ErrorAnalysisDialog.vue'
import { useErrorStore } from '../stores/error'

const router = useRouter()
const errorStore = useErrorStore()
const loading = ref(false)
const analysisLoading = ref(false)
const addDialogVisible = ref(false)
const analysisDialogVisible = ref(false)
const analysisText = ref('')
const sortBy = ref('urgent')
const subjectFilter = ref('all')
const statusFilter = ref('all')
const masteredIds = ref([])

const addForm = reactive({
  question: '',
  userAnswer: '',
  description: '',
})

const inferSubject = (item) => {
  const text = `${item.question || ''} ${item.description || ''}`
  if (/树|链表|栈|队列|图|并查集|堆/.test(text)) return '数据结构'
  if (/动态规划|二分|贪心|双指针|滑动窗口|递归/.test(text)) return '算法'
  if (/复杂度|时间复杂度|空间复杂度/.test(text)) return '复杂度'
  return '数学'
}

const toTimestamp = (value) => {
  const ts = new Date(value || '').getTime()
  return Number.isFinite(ts) ? ts : 0
}

const getUrgency = (item) => {
  if (masteredIds.value.includes(item.id)) return 0
  if (item.analysisStatus !== '已分析') return 3
  const ageDays = Math.max(0, (Date.now() - toTimestamp(item.createdAt)) / (24 * 60 * 60 * 1000))
  return ageDays >= 2 ? 2 : 1
}

const statusLabel = (item) => {
  if (masteredIds.value.includes(item.id)) return '已掌握'
  if (item.analysisStatus === '已分析') return '待巩固'
  return '立即复习'
}

const errorItems = computed(() => {
  return errorStore.errors.map((item) => ({
    ...item,
    subject: inferSubject(item),
    urgency: getUrgency(item),
    statusLabel: statusLabel(item),
  }))
})

const subjectOptions = computed(() => {
  const set = new Set(errorItems.value.map((item) => item.subject))
  return ['all', ...Array.from(set)]
})

const visibleItems = computed(() => {
  let list = [...errorItems.value]

  if (subjectFilter.value !== 'all') {
    list = list.filter((item) => item.subject === subjectFilter.value)
  }

  if (statusFilter.value !== 'all') {
    list = list.filter((item) => {
      if (statusFilter.value === 'urgent') return item.statusLabel === '立即复习'
      if (statusFilter.value === 'reinforce') return item.statusLabel === '待巩固'
      if (statusFilter.value === 'mastered') return item.statusLabel === '已掌握'
      return true
    })
  }

  if (sortBy.value === 'latest') {
    list.sort((a, b) => toTimestamp(b.createdAt) - toTimestamp(a.createdAt))
  } else if (sortBy.value === 'oldest') {
    list.sort((a, b) => toTimestamp(a.createdAt) - toTimestamp(b.createdAt))
  } else {
    list.sort((a, b) => b.urgency - a.urgency || toTimestamp(b.createdAt) - toTimestamp(a.createdAt))
  }

  return list
})

const totalCount = computed(() => errorItems.value.length)
const pendingCount = computed(() => errorItems.value.filter((item) => item.statusLabel !== '已掌握').length)
const masteryRate = computed(() => {
  if (!totalCount.value) return 0
  return Math.round((masteredIds.value.length / totalCount.value) * 100)
})

const toggleMastered = (id) => {
  if (masteredIds.value.includes(id)) {
    masteredIds.value = masteredIds.value.filter((item) => item !== id)
    return
  }
  masteredIds.value = [...masteredIds.value, id]
}

const handleReview = (item) => {
  const levelId = Number(item.levelId)
  if (!levelId) {
    ElMessage.info('该错题未关联关卡，暂时无法直接跳转复习。')
    return
  }
  router.push(`/challenge/${levelId}`)
}

const loadData = async () => {
  loading.value = true
  try {
    await errorStore.fetchErrors()
  } catch (error) {
    ElMessage.error('错题加载失败')
  } finally {
    loading.value = false
  }
}

const handleAnalyze = async (row) => {
  analysisDialogVisible.value = true
  analysisLoading.value = true
  analysisText.value = ''
  try {
    const result = await errorStore.getAnalysis(row.id, row.description)
    analysisText.value = result
    errorStore.markAnalysis(row.id, result)
  } catch (error) {
    analysisText.value = '分析失败，请稍后重试。'
  } finally {
    analysisLoading.value = false
  }
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
      createdAt: new Date().toLocaleString(),
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    addForm.question = ''
    addForm.userAnswer = ''
    addForm.description = ''
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container errors-page">
    <div class="overview-card">
      <div class="stat-item">
        <div class="stat-label">总计</div>
        <div class="stat-value">{{ totalCount }}</div>
      </div>
      <div class="stat-item active">
        <div class="stat-label">待复习</div>
        <div class="stat-value">{{ pendingCount }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">掌握度</div>
        <div class="stat-value mastery">{{ masteryRate }}%</div>
      </div>
    </div>

    <div class="filter-row">
      <el-select v-model="sortBy" class="filter-select wide">
        <el-option label="排序方式：急迫度" value="urgent" />
        <el-option label="排序方式：最新" value="latest" />
        <el-option label="排序方式：最早" value="oldest" />
      </el-select>
      <el-select v-model="subjectFilter" class="filter-select">
        <el-option label="科目：全部" value="all" />
        <el-option v-for="subject in subjectOptions.filter((item) => item !== 'all')" :key="subject"
          :label="`科目：${subject}`" :value="subject" />
      </el-select>
      <el-select v-model="statusFilter" class="filter-select">
        <el-option label="状态：全部" value="all" />
        <el-option label="状态：立即复习" value="urgent" />
        <el-option label="状态：待巩固" value="reinforce" />
        <el-option label="状态：已掌握" value="mastered" />
      </el-select>
      <el-button type="primary" @click="addDialogVisible = true">手动添加错题</el-button>
    </div>

    <div v-loading="loading" class="error-list">
      <article v-for="item in visibleItems" :key="item.id" class="error-card"
        :class="{ done: item.statusLabel === '已掌握' }">
        <div class="card-main">
          <div class="card-tags">
            <span class="subject-chip">{{ item.subject }}</span>
            <span class="time-chip">{{ item.createdAt }}</span>
          </div>
          <h3 class="card-title">{{ item.question }}</h3>
          <p class="card-answer">你的答案：{{ item.userAnswer || '（空）' }}</p>
          <p class="card-desc">{{ item.description }}</p>
          <div class="card-foot">
            <div class="status-group">
              <span class="status-chip"
                :class="item.statusLabel === '立即复习' ? 'urgent' : item.statusLabel === '已掌握' ? 'mastered' : 'normal'">
                {{ item.statusLabel }}
              </span>
              <el-button v-if="item.statusLabel === '立即复习'" size="small" type="primary" plain
                @click="handleReview(item)">
                去复习
              </el-button>
            </div>
            <el-button link type="primary" @click="handleAnalyze(item)">AI分析</el-button>
          </div>
        </div>
        <button class="check-btn" :class="{ checked: masteredIds.includes(item.id) }" @click="toggleMastered(item.id)"
          type="button" :aria-label="masteredIds.includes(item.id) ? '取消掌握' : '标记掌握'">
          <span v-if="masteredIds.includes(item.id)">✓</span>
        </button>
      </article>
      <div v-if="!visibleItems.length && !loading" class="empty-box">当前筛选下暂无错题，继续保持。</div>
    </div>

    <el-dialog v-model="addDialogVisible" title="添加错题" width="560px">
      <el-form label-width="82px">
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
        <el-button type="primary" @click="handleAdd">保存</el-button>
      </template>
    </el-dialog>

    <ErrorAnalysisDialog v-model="analysisDialogVisible" :loading="analysisLoading" :content="analysisText" />
  </div>
</template>

<style scoped>
.errors-page {
  --overview-start: #4b6c99;
  --overview-end: #3f5d85;
  --overview-text: #ecf2fa;
  --overview-muted: rgba(236, 242, 250, 0.86);
  --chip-bg: #eef3f9;
  --chip-text: #4a6486;
  --card-border: #c9d7e8;
  --card-border-active: #9db6d3;
  --status-urgent-bg: #f8edf0;
  --status-urgent-border: #e6c9d2;
  --status-urgent-text: #8f5f6c;
  --status-normal-bg: #f6f1ea;
  --status-normal-border: #e6d6c5;
  --status-normal-text: #8b7053;
  --status-mastered-bg: #edf2f8;
  --status-mastered-border: #d6deea;
  --status-mastered-text: #5f7287;
  padding-bottom: 28px;
  display: grid;
  gap: 16px;
}

.overview-card {
  background: linear-gradient(140deg, var(--overview-start) 0%, var(--overview-end) 100%);
  border-radius: 24px;
  padding: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.stat-item {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.16);
  padding: 16px;
  color: var(--overview-text);
  text-align: center;
}

.stat-item.active {
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.stat-label {
  font-size: 15px;
  color: var(--overview-muted);
}

.stat-value {
  margin-top: 8px;
  font-size: 46px;
  line-height: 1;
  font-weight: 700;
  color: var(--overview-text);
}

.stat-value.mastery {
  color: #8fd8ae;
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-select {
  width: 180px;
}

.filter-select.wide {
  width: 240px;
}

.error-list {
  display: grid;
  gap: 14px;
}

.error-card {
  border: 2px solid var(--card-border-active);
  border-radius: 28px;
  background: var(--card-bg);
  padding: 18px 20px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: start;
}

.error-card.done {
  border-color: var(--card-border);
  background: var(--panel-bg);
}

.card-main {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.card-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.subject-chip {
  border-radius: 10px;
  background: var(--chip-bg);
  color: var(--chip-text);
  font-size: 13px;
  padding: 4px 10px;
  font-weight: 600;
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-answer,
.card-desc {
  margin: 0;
  color: var(--text-sub);
  font-size: 13px;
  line-height: 1.6;
  max-height: 64px;
  overflow-y: auto;
}

.card-foot {
  margin-top: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1;
  font-weight: 700;
  padding: 6px 10px;
  border: 1px solid transparent;
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

.check-btn {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  border: 2px solid var(--line-card);
  background: var(--card-bg);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.check-btn.checked {
  border-color: var(--brand-blue);
  background: var(--brand-blue);
}

.empty-box {
  border-radius: 16px;
  background: var(--card-bg);
  border: 1px dashed var(--line-soft);
  color: var(--text-sub);
  text-align: center;
  padding: 28px 12px;
}

@media (max-width: 960px) {
  .overview-card {
    grid-template-columns: 1fr;
  }

  .filter-select,
  .filter-select.wide {
    width: 100%;
  }

  .error-card {
    grid-template-columns: 1fr;
  }

  .check-btn {
    justify-self: end;
  }
}
</style>
