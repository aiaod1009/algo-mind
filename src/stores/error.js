import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const toStringList = (value) => {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean)
  }

  if (typeof value === 'string') {
    const text = value.trim()
    if (!text) return []

    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item).trim()).filter(Boolean)
      }
    } catch (error) {
      // Ignore JSON parse error and fallback to delimiter split.
    }

    return text
      .split(/[,，、\n]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }

  if (value == null) return []
  return [String(value).trim()].filter(Boolean)
}

const normalizeList = (items) => (Array.isArray(items) ? items : [])

const dedupeByLevel = (items, nextItem) => {
  if (!nextItem?.levelId) {
    return [nextItem, ...items.filter((item) => Number(item.id) !== Number(nextItem?.id))]
  }

  return [
    nextItem,
    ...items.filter((item) => {
      if (Number(item.id) === Number(nextItem.id)) {
        return false
      }
      return Number(item.levelId) !== Number(nextItem.levelId)
    }),
  ]
}

export const useErrorStore = defineStore('error', () => {
  const errors = ref([])
  const completedErrors = ref([])

  const hydrateErrorsFromLocal = () => {
    errors.value = []
    completedErrors.value = []
    return false
  }

  const fetchErrors = async ({ skipIfLoaded = true } = {}) => {
    if (skipIfLoaded && Array.isArray(errors.value) && errors.value.length > 0) {
      return { fromCache: true }
    }

    const res = await api.get('/errors')
    if (res.data?.code === 0) {
      errors.value = normalizeList(res.data.data)
      return { fromCache: false }
    }
    throw new Error(res.data?.message || '获取错题数据失败')
  }

  const fetchCompletedErrors = async ({ skipIfLoaded = true } = {}) => {
    if (skipIfLoaded && Array.isArray(completedErrors.value) && completedErrors.value.length > 0) {
      return { fromCache: true }
    }

    const res = await api.get('/errors/completed')
    if (res.data?.code === 0) {
      completedErrors.value = normalizeList(res.data.data)
      return { fromCache: false }
    }
    throw new Error(res.data?.message || '获取已完成错题失败')
  }

  const refreshAll = async () => {
    await Promise.all([
      fetchErrors({ skipIfLoaded: false }),
      fetchCompletedErrors({ skipIfLoaded: false }),
    ])
  }

  const addError = async (errorItem) => {
    const userAnswerStr = Array.isArray(errorItem.userAnswer)
      ? errorItem.userAnswer.join(', ')
      : String(errorItem.userAnswer || '')

    const payload = {
      levelId: errorItem.levelId || null,
      question: errorItem.question || '未命名题目',
      userAnswer: userAnswerStr,
      description: errorItem.description || '暂无描述',
      analysisStatus: '未分析',
      analysis: '',
    }

    const res = await api.post('/errors', payload)
    if (res.data?.code === 0 && res.data?.data) {
      errors.value = [res.data.data, ...errors.value.filter((item) => Number(item.id) !== Number(res.data.data.id))]
      if (res.data.data.levelId != null) {
        completedErrors.value = completedErrors.value.filter(
          (item) => Number(item.levelId) !== Number(res.data.data.levelId),
        )
      }
      return res.data.data
    }

    throw new Error(res.data?.message || '保存错题失败')
  }

  const completeError = async (errorId) => {
    const numericId = Number(errorId)
    const res = await api.post(`/errors/${numericId}/complete`)
    if (res.data?.code === 0 && res.data?.data) {
      const completedItem = res.data.data
      errors.value = errors.value.filter((item) => Number(item.id) !== numericId)
      completedErrors.value = dedupeByLevel(completedErrors.value, completedItem)
      return completedItem
    }

    throw new Error(res.data?.message || '归档已完成错题失败')
  }

  const getAnalysis = async (errorItem) => {
    const payload = {
      errorId: errorItem.id,
      question: errorItem.question || '',
      userAnswer: toStringList(errorItem.userAnswer),
      description: errorItem.description || '',
      difficulty: errorItem.difficulty || 'medium',
      track: errorItem.track || 'algo',
      solveRate: errorItem.solveRate || null,
      avgTimeSeconds: errorItem.avgTimeSeconds || null,
      userAttempts: errorItem.userAttempts || null,
      relatedTopics: toStringList(errorItem.relatedTopics),
      userLevel: errorItem.userLevel || 'beginner',
    }

    const res = await api.post('/error-analysis', payload, {
      timeout: 60000,
    })
    if (res.data?.code === 0) {
      return {
        analysis: res.data.data.analysis,
        analysisData: res.data.data.analysisData,
      }
    }
    throw new Error(res.data?.message || 'AI 分析失败')
  }

  const markAnalysis = (errorId, analysis, analysisData = null) => {
    const item = errors.value.find((row) => Number(row.id) === Number(errorId))
    if (!item) return
    item.analysisStatus = '已分析'
    item.analysis = analysis
    if (analysisData) {
      item.analysisData = analysisData
    }
  }

  return {
    errors,
    completedErrors,
    hydrateErrorsFromLocal,
    fetchErrors,
    fetchCompletedErrors,
    refreshAll,
    addError,
    completeError,
    getAnalysis,
    markAnalysis,
  }
})
