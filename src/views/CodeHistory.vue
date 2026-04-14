<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const snapshots = ref([])
const codeDialogVisible = ref(false)
const currentSnapshot = ref(null)
const levelInfo = ref({ id: null, name: '' })
const activeLanguage = ref('all')

const levelId = computed(() => route.query.levelId || route.params.levelId)

const groupedSnapshots = computed(() => {
  const groups = {}
  snapshots.value.forEach((item) => {
    const lang = item.language || '未知'
    if (!groups[lang]) {
      groups[lang] = []
    }
    groups[lang].push(item)
  })
  return groups
})

const languageList = computed(() => {
  return Object.keys(groupedSnapshots.value).sort()
})

const filteredSnapshots = computed(() => {
  if (activeLanguage.value === 'all') {
    return snapshots.value
  }
  return groupedSnapshots.value[activeLanguage.value] || []
})

const pageTitle = computed(() => {
  if (levelInfo.value.name) {
    return `${levelInfo.value.name} - 历史代码`
  }
  return '历史代码'
})

const formatTime = (value) => {
  if (!value) return '时间未知'
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
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const parseSuggestions = (value) => {
  if (!value) return []

  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean)
  }

  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed)
        ? parsed.map((item) => String(item).trim()).filter(Boolean)
        : []
    } catch {
      return []
    }
  }

  return []
}

const getScoreColor = (score) => {
  if (score >= 90) return '#22c55e'
  if (score >= 70) return '#3b82f6'
  if (score >= 50) return '#f59e0b'
  return '#ef4444'
}

const loadData = async () => {
  if (!levelId.value) {
    ElMessage.warning('缺少关卡参数')
    router.push('/levels')
    return
  }

  loading.value = true
  try {
    const snapshotsRes = await api.getCodeSnapshots({ levelId: levelId.value })
    snapshots.value = snapshotsRes.data?.data || []

    if (snapshots.value.length > 0 && snapshots.value[0].levelName) {
      levelInfo.value = {
        id: snapshots.value[0].levelId,
        name: snapshots.value[0].levelName,
      }
    } else {
      levelInfo.value = { id: levelId.value, name: `关卡 ${levelId.value}` }
    }
  } catch (err) {
    console.error('加载历史代码失败', err)
    ElMessage.error('加载历史代码失败')
  } finally {
    loading.value = false
  }
}

const openCodeDialog = (snapshot) => {
  currentSnapshot.value = snapshot
  codeDialogVisible.value = true
}

const copyCode = async (code) => {
  try {
    await navigator.clipboard.writeText(code)
    ElMessage.success('代码已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const applyCode = (snapshot) => {
  router.push({
    path: `/challenge/${levelId.value}`,
    query: {
      applySnapshot: snapshot.id,
      language: snapshot.language,
    },
  })
}

const handleDelete = async (snapshot) => {
  try {
    await ElMessageBox.confirm('确定删除这条历史代码？删除后无法恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }

  try {
    await api.deleteCodeSnapshot(snapshot.id)
    snapshots.value = snapshots.value.filter((item) => item.id !== snapshot.id)
    ElMessage.success('已删除')
    if (currentSnapshot.value?.id === snapshot.id) {
      codeDialogVisible.value = false
    }
  } catch (err) {
    console.error('删除失败', err)
    ElMessage.error('删除失败')
  }
}

const goBack = () => {
  if (levelId.value) {
    router.push(`/challenge/${levelId.value}`)
  } else {
    router.push('/levels')
  }
}

watch(levelId, () => {
  activeLanguage.value = 'all'
  loadData()
})

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container code-history-page">
    <button class="floating-back-btn" @click="goBack" title="返回题目">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M19 12H5" />
        <path d="M12 19l-7-7 7-7" />
      </svg>
      <span class="back-text">返回</span>
    </button>

    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <p class="page-subtitle">共 {{ snapshots.length }} 条历史记录</p>
    </div>

    <div v-if="languageList.length > 0" class="language-tabs">
      <button
        class="lang-tab"
        :class="{ active: activeLanguage === 'all' }"
        @click="activeLanguage = 'all'"
      >
        全部
        <span class="count">{{ snapshots.length }}</span>
      </button>
      <button
        v-for="lang in languageList"
        :key="lang"
        class="lang-tab"
        :class="{ active: activeLanguage === lang }"
        @click="activeLanguage = lang"
      >
        {{ lang }}
        <span class="count">{{ groupedSnapshots[lang].length }}</span>
      </button>
    </div>

    <div v-loading="loading" class="snapshot-list">
      <template v-if="filteredSnapshots.length > 0">
        <article
          v-for="item in filteredSnapshots"
          :key="item.id"
          class="snapshot-card"
          @click="openCodeDialog(item)"
        >
          <div class="card-main">
            <div class="card-tags">
              <span class="lang-chip">{{ item.language }}</span>
              <span v-if="item.isBest" class="best-chip">最佳版本</span>
              <span v-if="item.aiEvalPassed" class="passed-chip">已通过</span>
              <span class="time-chip">{{ formatTime(item.savedAt) }}</span>
            </div>

            <div class="card-meta">
              <div class="score-bar">
                <div
                  class="score-fill"
                  :style="{ width: `${item.score || 0}%`, backgroundColor: getScoreColor(item.score || 0) }"
                />
              </div>
              <span class="score-text" :style="{ color: getScoreColor(item.score || 0) }">
                {{ item.score || 0 }} 分
              </span>
              <span v-if="item.stars" class="stars-text">{{ '★'.repeat(item.stars) }}</span>
            </div>

            <pre class="code-preview">{{ (item.code || '').substring(0, 150) }}{{ (item.code || '').length > 150 ? '...' : '' }}</pre>
          </div>

          <div class="card-actions">
            <el-button size="small" type="primary" plain @click.stop="applyCode(item)">应用此代码</el-button>
            <el-button size="small" type="danger" plain @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </article>
      </template>

      <div v-else-if="!loading" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="64" height="64">
          <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="12" y1="18" x2="12" y2="12" />
          <line x1="9" y1="15" x2="15" y2="15" />
        </svg>
        <p>暂无历史代码</p>
        <p class="empty-hint">在练习页面点击「提交代码」保存你的代码</p>
      </div>
    </div>

    <el-dialog v-model="codeDialogVisible" title="代码详情" width="700px" class="code-dialog">
      <template v-if="currentSnapshot">
        <div class="dialog-header">
          <div class="dialog-tags">
            <span class="lang-chip">{{ currentSnapshot.language }}</span>
            <span v-if="currentSnapshot.isBest" class="best-chip">最佳版本</span>
            <span class="time-chip">{{ formatTime(currentSnapshot.savedAt) }}</span>
          </div>
        </div>

        <div class="dialog-score">
          <span class="score-label">评测分数</span>
          <span class="score-value" :style="{ color: getScoreColor(currentSnapshot.score || 0) }">{{ currentSnapshot.score || 0 }}</span>
          <span v-if="currentSnapshot.stars" class="stars">{{ '★'.repeat(currentSnapshot.stars) }}</span>
        </div>

        <div v-if="currentSnapshot.aiAnalysis" class="dialog-section">
          <h4>AI 中文分析</h4>
          <p>{{ currentSnapshot.aiAnalysis }}</p>
        </div>

        <div v-if="parseSuggestions(currentSnapshot.aiSuggestionsJson).length" class="dialog-section">
          <h4>改进建议</h4>
          <ul class="suggestion-list">
            <li v-for="(suggestion, index) in parseSuggestions(currentSnapshot.aiSuggestionsJson)" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>

        <div v-if="currentSnapshot.aiRecommendedCode" class="dialog-section">
          <div class="code-header">
            <h4>推荐代码</h4>
            <el-button size="small" @click="copyCode(currentSnapshot.aiRecommendedCode)">复制推荐代码</el-button>
          </div>
          <pre class="code-block recommended-code">{{ currentSnapshot.aiRecommendedCode }}</pre>
        </div>

        <div class="dialog-section code-section">
          <div class="code-header">
            <h4>源代码</h4>
            <el-button size="small" @click="copyCode(currentSnapshot.code)">复制代码</el-button>
          </div>
          <pre class="code-block">{{ currentSnapshot.code }}</pre>
        </div>

        <div v-if="currentSnapshot.stdinInput" class="dialog-section">
          <h4>标准输入</h4>
          <pre class="stdin-block">{{ currentSnapshot.stdinInput }}</pre>
        </div>

        <div v-if="currentSnapshot.runOutput" class="dialog-section">
          <h4>运行输出</h4>
          <pre class="output-block">{{ currentSnapshot.runOutput }}</pre>
        </div>
      </template>

      <template #footer>
        <el-button @click="codeDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="applyCode(currentSnapshot); codeDialogVisible = false">应用此代码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.code-history-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.floating-back-btn {
  position: fixed;
  top: 100px;
  left: 24px;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 10px;
  cursor: pointer;
  color: #4a6f9d;
  font-size: 13px;
  font-weight: 500;
  box-shadow: 0 2px 12px rgba(74, 111, 157, 0.12);
  backdrop-filter: blur(10px);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.floating-back-btn:hover {
  transform: translateX(-3px);
  background: #4a6f9d;
  color: white;
  box-shadow: 0 4px 20px rgba(74, 111, 157, 0.3);
}

.floating-back-btn:active {
  transform: translateX(-1px) scale(0.97);
}

.back-text {
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
  letter-spacing: 0.3px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 4px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.language-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.lang-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #475569;
  transition: all 0.2s;
}

.lang-tab:hover {
  border-color: #4a6f9d;
  color: #4a6f9d;
}

.lang-tab.active {
  background: #4a6f9d;
  border-color: #4a6f9d;
  color: white;
}

.lang-tab .count {
  padding: 2px 6px;
  background: rgba(0,0,0,0.06);
  border-radius: 4px;
  font-size: 12px;
}

.lang-tab.active .count {
  background: rgba(255,255,255,0.2);
}

.snapshot-list {
  display: grid;
  gap: 16px;
}

.snapshot-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.snapshot-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.lang-chip {
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 12px;
  color: #475569;
}

.best-chip {
  padding: 4px 10px;
  background: #fef3c7;
  border-radius: 6px;
  font-size: 12px;
  color: #d97706;
  font-weight: 600;
}

.passed-chip {
  padding: 4px 10px;
  background: #dcfce7;
  border-radius: 6px;
  font-size: 12px;
  color: #16a34a;
}

.time-chip {
  padding: 4px 10px;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 12px;
  color: #94a3b8;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.score-bar {
  flex: 1;
  max-width: 200px;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.score-text {
  font-size: 16px;
  font-weight: 600;
}

.stars-text {
  color: #f59e0b;
  font-size: 14px;
}

.code-preview {
  margin: 0;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: #475569;
  overflow: hidden;
  white-space: pre-wrap;
  word-break: break-all;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}

.empty-hint {
  margin-top: 8px !important;
  font-size: 14px !important;
  color: #334155;
}

.code-dialog .dialog-header {
  margin-bottom: 16px;
}

.code-dialog .dialog-tags {
  display: flex;
  gap: 8px;
}

.code-dialog .dialog-score {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 16px;
}

.code-dialog .score-label {
  color: #64748b;
  font-size: 14px;
}

.code-dialog .score-value {
  font-size: 28px;
  font-weight: 700;
}

.code-dialog .stars {
  color: #f59e0b;
  font-size: 18px;
}

.code-dialog .dialog-section {
  margin-bottom: 16px;
}

.code-dialog .dialog-section h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #64748b;
  font-weight: 600;
}

.code-dialog .dialog-section p {
  margin: 0;
  color: #334155;
  line-height: 1.6;
}

.code-dialog .suggestion-list {
  margin: 0;
  padding-left: 18px;
  color: #334155;
}

.code-dialog .suggestion-list li {
  margin: 6px 0;
  line-height: 1.6;
}

.code-dialog .code-section .code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.code-dialog .code-section .code-header h4 {
  margin: 0;
}

.code-dialog .code-block {
  margin: 0;
  padding: 16px;
  background: #1e293b;
  border-radius: 12px;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: #e2e8f0;
  overflow-x: auto;
  white-space: pre;
  line-height: 1.6;
}

.code-dialog .recommended-code {
  background: #0f172a;
}

.code-dialog .stdin-block,
.code-dialog .output-block {
  margin: 0;
  padding: 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  color: #475569;
  white-space: pre-wrap;
}

</style>
