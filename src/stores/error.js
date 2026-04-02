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
      return
    }

    // Render local data first so the page does not block on network timeout.
    hydrateErrorsFromLocal()

    try {
      const res = await api.get('/errors')
      if (res.data?.code === 0 && Array.isArray(res.data.data)) {
        errors.value = res.data.data
        saveLocalErrors(errors.value)
        return
      }
    } catch (error) {
      console.warn('错题接口不可用，使用本地错题数据。', error)
    }

    errors.value = readLocalErrors()
  }

  const addError = async (errorItem) => {
    const nextItem = {
      id: errorItem.id || Date.now(),
      levelId: errorItem.levelId || null,
      question: errorItem.question || '未命名题目',
      userAnswer: errorItem.userAnswer || '',
      description: errorItem.description || '暂无描述',
      createdAt: errorItem.createdAt || Date.now(),
      analysisStatus: '未分析',
      analysis: '',
    }

    errors.value.unshift(nextItem)
    saveLocalErrors(errors.value)

    try {
      await api.post('/errors', nextItem)
    } catch (error) {
      console.warn('错题上报失败，已保存在本地。', error)
    }
  }

  const getAnalysis = async (errorId, description) => {
    try {
      const res = await api.post('/error-analysis', { errorId, description })
      if (res.data?.code === 0) {
        return res.data.data.analysis
      }
    } catch (error) {
      console.warn('AI 分析接口不可用，使用本地分析文本。', error)
    }

    return `错误原因可能在于对题目条件理解不完整。建议先写出已知条件，再用样例逐步演算，最后总结可复用模板。\n\n题目描述：${description || '无'}`
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
