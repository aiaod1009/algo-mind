<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ErrorAnalysisDialog from '../components/ErrorAnalysisDialog.vue'
import FlowerPagination from '../components/FlowerPagination.vue'
import { useErrorStore } from '../stores/error'

const router = useRouter()
const errorStore = useErrorStore()
const loading = ref(false)
const analysisLoading = ref(false)
const addDialogVisible = ref(false)
const analysisDialogVisible = ref(false)
const analysisText = ref('')
const analysisData = ref(null)
const sortBy = ref('urgent')
const subjectFilter = ref('all')
const statusFilter = ref('all')
const MASTERED_STORAGE_KEY = 'errors-mastered-ids'

const readMasteredIds = () => {
  const raw = localStorage.getItem(MASTERED_STORAGE_KEY)
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed.map(Number).filter((id) => Number.isFinite(id)) : []
  } catch (error) {
    return []
  }
}

const saveMasteredIds = (ids) => {
  localStorage.setItem(MASTERED_STORAGE_KEY, JSON.stringify(ids))
}

const masteredIds = ref(readMasteredIds())
const masteredIdSet = computed(() => new Set(masteredIds.value))
const currentPage = ref(1)
const pageSize = 5

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

const formatTime = (value) => {
  if (!value) return ''
  const ts = Number.isFinite(Number(value)) ? Number(value) : new Date(value).getTime()
  if (!Number.isFinite(ts)) return String(value)
  const date = new Date(ts)
  const now = new Date()
  const diffMs = now.getTime() - ts
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)
  if (diffMins < 1) return '刚刚'
  if (diffMins < 60) return `${diffMins} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays < 7) return `${diffDays} 天前`
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const formatAnswer = (value) => {
  if (!value) return '（空）'
  if (Array.isArray(value)) {
    return value.map(v => String(v).trim()).filter(Boolean).join('、') || '（空）'
  }
  try {
    const parsed = JSON.parse(value)
    if (Array.isArray(parsed)) {
      return parsed.map(v => String(v).trim()).filter(Boolean).join('、') || '（空）'
    }
  } catch (e) { }
  return String(value)
}

const getUrgency = (item) => {
  if (masteredIdSet.value.has(item.id)) return 0
  if (item.analysisStatus !== '已分析') return 3
  const ageDays = Math.max(0, (Date.now() - toTimestamp(item.createdAt)) / (24 * 60 * 60 * 1000))
  return ageDays >= 2 ? 2 : 1
}

const statusLabel = (item) => {
  if (masteredIdSet.value.has(item.id)) return '已掌握'
  if (item.analysisStatus === '已分析') return '待巩固'
  return '立即复习'
}

const errorItems = computed(() => {
  const masteredSet = masteredIdSet.value
  return errorStore.errors.map((item) => ({
    ...item,
    subject: inferSubject(item),
    urgency: masteredSet.has(item.id) ? 0 : getUrgency(item),
    statusLabel: statusLabel(item),
    isMastered: masteredSet.has(item.id),
  }))
})

const subjectOptions = computed(() => {
  const set = new Set(errorItems.value.map((item) => item.subject))
  return ['all', ...Array.from(set)]
})

const filteredItems = computed(() => {
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

const totalPages = computed(() => Math.ceil(filteredItems.value.length / pageSize))

const visibleItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredItems.value.slice(start, end)
})

watch([sortBy, subjectFilter, statusFilter], () => {
  currentPage.value = 1
})

const totalCount = computed(() => errorItems.value.length)
const pendingCount = computed(() => errorItems.value.filter((item) => item.statusLabel !== '已掌握').length)
const masteryRate = computed(() => {
  if (!totalCount.value) return 0
  return Math.round((masteredIds.value.length / totalCount.value) * 100)
})

const toggleMastered = (id) => {
  const numericId = Number(id)
  if (masteredIds.value.includes(numericId)) {
    masteredIds.value = masteredIds.value.filter((item) => item !== numericId)
    return
  }
  masteredIds.value = [...masteredIds.value, numericId]
}

const syncMasteredIdsWithCurrentErrors = () => {
  const currentIds = new Set(errorItems.value.map((item) => Number(item.id)))
  masteredIds.value = masteredIds.value.filter((id) => currentIds.has(Number(id)))
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
    await errorStore.fetchErrors({ skipIfLoaded: false })
  } catch (error) {
    console.error('错题加载失败:', error)
    ElMessage.error(error?.message || '错题加载失败，请检查网络后重试')
  } finally {
    loading.value = false
  }
}

const handleAnalyze = async (row) => {
  analysisDialogVisible.value = true
  analysisLoading.value = true
  analysisText.value = ''
  analysisData.value = null
  try {
    const result = await errorStore.getAnalysis(row)
    analysisText.value = result.analysis
    analysisData.value = result.analysisData
    errorStore.markAnalysis(row.id, result.analysis)
  } catch (error) {
    const serverMessage = error?.response?.data?.message || error?.message || 'AI 分析失败，请稍后重试'
    ElMessage.error(serverMessage)
    analysisText.value = ''
    analysisData.value = null
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
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    addForm.question = ''
    addForm.userAnswer = ''
    addForm.description = ''
  } catch (error) {
    const message = error?.message || '错题保存失败，请稍后重试'
    ElMessage.warning(message)
    addDialogVisible.value = false
    addForm.question = ''
    addForm.userAnswer = ''
    addForm.description = ''
  }
}

onMounted(loadData)

watch(masteredIds, (ids) => {
  saveMasteredIds(ids)
}, { deep: true })

watch(errorItems, () => {
  syncMasteredIdsWithCurrentErrors()
}, { deep: true, immediate: true })
</script>

<template>
  <div class="page-container errors-page">
    <div class="stats-showcase">
      <div class="stat-card total">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
            <line x1="12" y1="6" x2="12" y2="12" />
            <line x1="9" y1="9" x2="15" y2="9" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ totalCount }}</span>
          <span class="stat-label">错题总数</span>
        </div>
        <div class="stat-decoration">
          <span></span><span></span><span></span>
        </div>
      </div>

      <div class="stat-card pending">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-number">{{ pendingCount }}</span>
          <span class="stat-label">待复习</span>
        </div>
        <div class="stat-progress">
          <div class="progress-bar" :style="{ width: totalCount > 0 ? (pendingCount / totalCount * 100) + '%' : '0%' }">
          </div>
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
          <span class="stat-number">{{ masteryRate }}<small>%</small></span>
          <span class="stat-label">掌握度</span>
        </div>
        <div class="mastery-ring">
          <svg viewBox="0 0 36 36">
            <circle cx="18" cy="18" r="16" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
            <circle cx="18" cy="18" r="16" fill="none" stroke="currentColor" stroke-width="3" stroke-dasharray="100 100"
              :stroke-dashoffset="100 - masteryRate" transform="rotate(-90 18 18)" />
          </svg>
        </div>
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
            <span class="time-chip">{{ formatTime(item.createdAt) }}</span>
          </div>
          <h3 class="card-title">{{ item.question }}</h3>
          <p class="card-answer">你的答案：{{ formatAnswer(item.userAnswer) }}</p>
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
        <button class="check-btn" :class="{ checked: item.isMastered }" @click="toggleMastered(item.id)" type="button"
          :aria-label="item.isMastered ? '取消掌握' : '标记掌握'">
          <span v-if="item.isMastered">✓</span>
        </button>
      </article>
      <div v-if="!visibleItems.length && !loading" class="empty-box">
        <div class="empty-icon" v-once>
          <svg t="1774845352955" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"
            p-id="4800" width="48" height="48">
            <path
              d="M136.533333 268.8c0-120.174933 97.425067-217.6 217.6-217.6h413.866667c120.1792 0 217.6 97.425067 217.6 217.6v507.733333c0 120.1792-97.4208 217.6-217.6 217.6H354.133333c-120.174933 0-217.6-97.4208-217.6-217.6V268.8z"
              fill="#8F619F" p-id="4801"></path>
            <path
              d="M746.666667 38.4c120.1792 0 217.6 97.425067 217.6 217.6v490.666667c0 120.1792-97.4208 217.6-217.6 217.6H332.8c-120.174933 0-217.6-97.4208-217.6-217.6v-12.8h-12.8c-44.770133 0-81.066667-36.296533-81.066667-81.066667s36.296533-81.066667 81.066667-81.066667h12.8v-140.8h-12.8c-44.770133 0-81.066667-36.296533-81.066667-81.066666s36.296533-81.066667 81.066667-81.066667h12.8v-12.8C115.2 135.825067 212.625067 38.4 332.8 38.4h413.866667z"
              fill="#B87BCD" p-id="4802"></path>
            <path
              d="M917.6576 407.121067c-19.528533-61.8752-81.442133-47.304533-109.960533-32.285867l27.4176 186.5728c30.301867-6.592 106.948267-76.945067 82.542933-154.286933zM167.893333 419.921067c19.528533-61.8752 81.442133-47.304533 109.960534-32.285867l-27.4176 186.5728c-30.301867-6.592-106.952533-76.945067-82.542934-154.286933z"
              fill="#FFD7C9" p-id="4803"></path>
            <path
              d="M209.066667 567.530667c0 177.9072 161.288533 288 339.2 288 177.9072 0 339.2-110.0928 339.2-288 0-177.911467-151.466667-356.266667-339.2-356.266667-211.2 0-339.2 178.3552-339.2 356.266667z"
              fill="#FFE2D8" p-id="4804"></path>
            <path
              d="M384 458.730667c0 14.135467-24.832 25.6-55.466667 25.6s-55.466667-11.464533-55.466666-25.6c0-14.139733 24.832-25.6 55.466666-25.6s55.466667 11.460267 55.466667 25.6zM776.533333 458.730667c0 14.135467-24.832 25.6-55.466666 25.6s-55.466667-11.464533-55.466667-25.6c0-14.139733 24.832-25.6 55.466667-25.6s55.466667 11.460267 55.466666 25.6z"
              fill="#FF8F8E" opacity=".5" p-id="4805"></path>
            <path
              d="M469.333333 398.997333c0 25.92-21.013333 46.933333-46.933333 46.933334s-46.933333-21.013333-46.933333-46.933334c0-25.924267 21.013333-46.933333 46.933333-46.933333s46.933333 21.009067 46.933333 46.933333zM669.866667 401.066667c0 25.92-21.013333 46.933333-46.933334 46.933333s-46.933333-21.013333-46.933333-46.933333 21.013333-46.933333 46.933333-46.933334 46.933333 21.013333 46.933334 46.933334z"
              fill="#FFFFFF" p-id="4806"></path>
            <path
              d="M588.795733 484.266667c-8.529067-38.4-125.8624-38.4-131.1488 4.266666-5.2864 42.666667-6.301867 86.5408 0 104.533334 12.445867 35.5456 35.409067 23.466667 69.282134 23.466666 36.718933 0 54.715733 10.5984 65.403733-27.733333 5.6192-20.164267 4.996267-66.1376-3.537067-104.533333z"
              fill="#A24435" p-id="4807"></path>
            <path
              d="M563.2 578.461867c0 19.848533-16.238933 35.938133-36.266667 35.938133s-36.266667-16.0896-36.266666-35.938133c0-13.8624 7.9232-25.8944 19.528533-31.8848 5.009067-2.5856 10.7008 7.005867 16.738133 7.005866s11.729067-9.591467 16.738134-7.005866c11.605333 5.9904 19.528533 18.0224 19.528533 31.8848z"
              fill="#80382D" p-id="4808"></path>
            <path
              d="M522.666667 569.6c-26.436267 1.553067-54.5024 17.6512-57.6 34.133333 10.666667 19.2 43.2128 10.666667 61.866666 10.666667 21.333333 0 46.933333 6.4 57.6-10.666667-2.065067-15.5136-35.4304-35.682133-61.866666-34.133333z"
              fill="#F36650" p-id="4809"></path>
            <path
              d="M491.089067 481.5744c-4.654933-1.322667-3.754667-10.717867-2.914134-17.5616l31.650134-4.7104c1.224533 8.2176 3.613867 14.941867 0.3456 17.9456-1.463467 1.344-23.261867 5.981867-29.0816 4.3264zM529.1136 482.304c-4.317867-2.1888-3.584-15.112533-3.293867-21.504l36.228267 1.629867c-0.3712 8.298667 0.682667 18.888533-3.0976 21.213866-1.693867 1.041067-24.439467 1.3952-29.8368-1.339733zM512.3328 591.210667c-4.3776 2.056533-4.040533 14.993067-3.946667 21.393066l36.2624-0.520533c-0.119467-8.3072 1.258667-18.862933-2.453333-21.303467-1.655467-1.088-24.384-2.1376-29.8624 0.430934z"
              fill="#FFFFFF" p-id="4810"></path>
            <path
              d="M541.866667 413.930667c0 5.888-9.553067 10.666667-21.333334 10.666666s-21.333333-4.778667-21.333333-10.666666c0-5.892267 9.553067-10.666667 21.333333-10.666667s21.333333 4.7744 21.333334 10.666667z"
              fill="#FFB2B2" p-id="4811"></path>
            <path
              d="M389.457067 312.951467c-7.598933-6.4 22.5024 10.619733 53.333333 0 9.301333-3.204267-12.8 34.133333-53.333333 0zM648.733867 313.352533c7.598933-6.4-22.5024 10.624-53.333334 0-9.301333-3.2 12.8 34.133333 53.333334 0z"
              fill="#734838" p-id="4812"></path>
            <path
              d="M428.6592 398.993067l13.777067-13.7728a4.4288 4.4288 0 0 0-6.2592-6.263467L422.4 392.7424l-13.777067-13.781333a4.4288 4.4288 0 0 0-6.2592 6.263466l13.777067 13.7728-13.781333 13.781334a4.4288 4.4288 0 0 0 6.263466 6.2592L422.4 405.248l13.777067 13.781333a4.394667 4.394667 0 0 0 4.8256 0.96 4.4288 4.4288 0 0 0 1.4336-7.223466l-13.777067-13.7728zM629.192533 401.066667l13.777067-13.777067a4.4288 4.4288 0 0 0-6.2592-6.2592L622.933333 394.807467l-13.777066-13.781334a4.424533 4.424533 0 1 0-6.2592 6.263467L616.674133 401.066667l-13.777066 13.777066a4.4288 4.4288 0 0 0 6.2592 6.2592L622.933333 407.325867l13.777067 13.781333a4.386133 4.386133 0 0 0 3.127467 1.2928 4.424533 4.424533 0 0 0 3.131733-7.556267l-13.777067-13.781333z"
              fill="#000000" p-id="4813"></path>
            <path
              d="M401.066667 164.266667C452.266667 145.066667 652.8 153.6 708.266667 213.333333c23.466667 0 71.253333 37.973333 117.333333 155.733334a469.597867 469.597867 0 0 0-11.0592-16.4736A478.673067 478.673067 0 0 1 823.466667 371.2c-83.2-117.333333-155.733333-134.4-174.933334-134.4s-68.266667 12.8-104.533333 12.8-57.6-17.066667-110.933333-12.8c-42.666667 3.413333-135.112533 96-174.933334 142.933333 0.2688-1.826133 0.622933-3.729067 1.066667-5.696l-0.3072 0.418134L258.133333 375.466667c0.507733-1.152 1.032533-2.333867 1.578667-3.541334 4.522667-18.141867 16.0256-41.6384 34.816-65.681066C322.235733 261.090133 358.5792 216.618667 392.533333 213.333333c-3.554133-12.087467-4.352-44.2368 8.533334-49.066666z"
              fill="#734838" p-id="4814"></path>
          </svg>
        </div>
        <div class="empty-text">当前筛选下暂无错题，继续保持。</div>
        <el-button type="primary" @click="addDialogVisible = true">
          <span class="btn-icon">✏️</span> 手动添加错题
        </el-button>
      </div>
    </div>

    <FlowerPagination v-if="totalPages > 1" :total="totalPages" :default-page="currentPage"
      @change="currentPage = $event" />

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

    <ErrorAnalysisDialog v-model="analysisDialogVisible" :loading="analysisLoading" :content="analysisText"
      :analysis-data="analysisData" />
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

.stats-showcase {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  padding: 24px;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card.total {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: white;
}

.stat-card.pending {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
}

.stat-card.mastery {
  background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
  color: white;
}

.stat-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  opacity: 0.9;
}

.stat-icon svg {
  width: 100%;
  height: 100%;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 42px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -2px;
}

.stat-number small {
  font-size: 24px;
  font-weight: 600;
  margin-left: 2px;
}

.stat-card .stat-label {
  font-size: 14px;
  font-weight: 500;
  opacity: 0.85;
  letter-spacing: 0.5px;
}

.stat-decoration {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  gap: 6px;
}

.stat-decoration span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
}

.stat-decoration span:nth-child(2) {
  background: rgba(255, 255, 255, 0.5);
}

.stat-decoration span:nth-child(3) {
  background: rgba(255, 255, 255, 0.7);
}

.stat-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
}

.stat-progress .progress-bar {
  height: 100%;
  background: rgba(255, 255, 255, 0.6);
  transition: width 0.6s ease;
}

.mastery-ring {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
}

.mastery-ring svg {
  width: 100%;
  height: 100%;
  color: rgba(255, 255, 255, 0.8);
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
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.error-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px -8px rgba(0, 0, 0, 0.12);
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
  transition: all 0.2s ease;
}

.check-btn:hover {
  transform: scale(1.1);
}

.check-btn.checked {
  border-color: var(--brand-blue);
  background: var(--brand-blue);
}

.empty-box {
  border-radius: 16px;
  background: var(--card-bg);
  border: 2px dashed var(--line-soft);
  color: var(--text-sub);
  text-align: center;
  padding: 48px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.empty-icon {
  font-size: 48px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-8px);
  }
}

.empty-text {
  font-size: 16px;
  color: var(--text-sub);
}

.empty-box .el-button {
  margin-top: 8px;
}

.btn-icon {
  margin-right: 6px;
}

@media (max-width: 960px) {
  .stats-showcase {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-number {
    font-size: 36px;
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
