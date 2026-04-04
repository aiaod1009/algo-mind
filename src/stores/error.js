import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const LOCAL_ERROR_KEY = 'errors'

const readLocalErrors = () => {
  const raw = localStorage.getItem(LOCAL_ERROR_KEY)
  if (!raw) return []
  try {
    return JSON.parse(raw)
  } catch (error) {
    return []
  }
}

const saveLocalErrors = (errors) => {
  localStorage.setItem(LOCAL_ERROR_KEY, JSON.stringify(errors))
}

export const useErrorStore = defineStore('error', () => {
  const errors = ref(readLocalErrors())

  const hydrateErrorsFromLocal = () => {
    const local = readLocalErrors()
    if (Array.isArray(local) && local.length > 0) {
      errors.value = local
      return true
    }
    errors.value = []
    return false
  }

  const fetchErrors = async ({ skipIfLoaded = true } = {}) => {
    if (skipIfLoaded && Array.isArray(errors.value) && errors.value.length > 0) {
      return { fromCache: true }
    }

    hydrateErrorsFromLocal()

    try {
      const res = await api.get('/errors')
      if (res.data?.code === 0 && Array.isArray(res.data.data)) {
        errors.value = res.data.data
        saveLocalErrors(errors.value)
        return { fromCache: false }
      }
      throw new Error(res.data?.message || '获取错题数据失败')
    } catch (error) {
      const localData = readLocalErrors()
      if (Array.isArray(localData) && localData.length > 0) {
        errors.value = localData
        return { fromCache: true, isOffline: true }
      }
      throw error
    }
  }

  const addError = async (errorItem) => {
    const userAnswerStr = Array.isArray(errorItem.userAnswer)
      ? errorItem.userAnswer.join(', ')
      : String(errorItem.userAnswer || '')

    const nextItem = {
      id: errorItem.id || Date.now(),
      levelId: errorItem.levelId || null,
      question: errorItem.question || '未命名题目',
      userAnswer: userAnswerStr,
      description: errorItem.description || '暂无描述',
      createdAt: errorItem.createdAt || Date.now(),
      analysisStatus: '未分析',
      analysis: '',
    }

    errors.value.unshift(nextItem)
    saveLocalErrors(errors.value)

    try {
      const { id, levelId, question, userAnswer, description, analysisStatus, analysis } = nextItem
      await api.post('/errors', { levelId, question, userAnswer, description, analysisStatus, analysis })
    } catch (error) {
      const syncError = new Error('错题已保存到本地，但同步服务器失败')
      syncError.originalError = error
      throw syncError
    }
  }

  const getAnalysis = async (errorId, description) => {
    try {
      const res = await api.post('/error-analysis', { errorId, description })
      if (res.data?.code === 0) {
        return res.data.data.analysis
      }
      throw new Error(res.data?.message || 'AI 分析失败')
    } catch (error) {
      console.error('AI 分析接口调用失败:', error)
      throw error
    }
  }

  const markAnalysis = (errorId, analysis) => {
    const item = errors.value.find((row) => Number(row.id) === Number(errorId))
    if (!item) return
    item.analysisStatus = '已分析'
    item.analysis = analysis
    saveLocalErrors(errors.value)
  }

  return { errors, hydrateErrorsFromLocal, fetchErrors, addError, getAnalysis, markAnalysis }
})
