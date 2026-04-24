import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'
import { normalizeUserAnswerPayload, toStringList } from '../utils/errorAnalysisPayload.js'

const ANALYSIS_CACHE_PREFIX = 'error-analysis-cache'

const normalizeList = (items) => (Array.isArray(items) ? items : [])

const parseAnalysisData = (value) => {
  if (!value) return null
  if (typeof value === 'object') return value

  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch (error) {
      return null
    }
  }

  return null
}

const readLocalUserId = () => {
  try {
    const raw = localStorage.getItem('user')
    if (!raw) return 'guest'
    const parsed = JSON.parse(raw)
    return parsed?.id != null ? String(parsed.id) : 'guest'
  } catch (error) {
    return 'guest'
  }
}

const getAnalysisCacheKey = () => `${ANALYSIS_CACHE_PREFIX}:${readLocalUserId()}`

const readAnalysisCache = () => {
  try {
    const raw = localStorage.getItem(getAnalysisCacheKey())
    if (!raw) return {}
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (error) {
    return {}
  }
}

const writeAnalysisCache = (cache) => {
  localStorage.setItem(getAnalysisCacheKey(), JSON.stringify(cache))
}

const normalizeErrorItem = (item, cached = null) => {
  const serverAnalysisData = parseAnalysisData(item?.analysisData) || parseAnalysisData(item?.analysisDataJson)
  const cachedAnalysisData = parseAnalysisData(cached?.analysisData)

  return {
    ...item,
    analysis: item?.analysis || cached?.analysis || '',
    analysisData: serverAnalysisData || cachedAnalysisData || null,
  }
}

const attachCachedAnalysis = (items) => {
  const cache = readAnalysisCache()
  return normalizeList(items).map((item) => normalizeErrorItem(item, cache[String(item?.id)]))
}

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
      errors.value = attachCachedAnalysis(res.data.data)
      return { fromCache: false }
    }

    throw new Error(res.data?.message || '????????')
  }

  const fetchCompletedErrors = async ({ skipIfLoaded = true } = {}) => {
    if (skipIfLoaded && Array.isArray(completedErrors.value) && completedErrors.value.length > 0) {
      return { fromCache: true }
    }

    const res = await api.get('/errors/completed')
    if (res.data?.code === 0) {
      completedErrors.value = attachCachedAnalysis(res.data.data)
      return { fromCache: false }
    }

    throw new Error(res.data?.message || '?????????')
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
      question: errorItem.question || '?????',
      userAnswer: userAnswerStr,
      description: errorItem.description || '????',
      analysisStatus: '???',
      analysis: '',
    }

    const res = await api.post('/errors', payload)
    if (res.data?.code === 0 && res.data?.data) {
      errors.value = [
        normalizeErrorItem(res.data.data),
        ...errors.value.filter((item) => Number(item.id) !== Number(res.data.data.id)),
      ]

      if (res.data.data.levelId != null) {
        completedErrors.value = completedErrors.value.filter(
          (item) => Number(item.levelId) !== Number(res.data.data.levelId),
        )
      }

      return res.data.data
    }

    throw new Error(res.data?.message || '??????')
  }

  const completeError = async (errorId) => {
    const numericId = Number(errorId)
    const res = await api.post(`/errors/${numericId}/complete`)
    if (res.data?.code === 0 && res.data?.data) {
      const completedItem = normalizeErrorItem(res.data.data)
      errors.value = errors.value.filter((item) => Number(item.id) !== numericId)
      completedErrors.value = dedupeByLevel(completedErrors.value, completedItem)
      return completedItem
    }

    throw new Error(res.data?.message || '?????????')
  }

  const getAnalysis = async (errorItem) => {
    const answerPayload = normalizeUserAnswerPayload(errorItem)
    const payload = {
      errorId: errorItem.id,
      question: errorItem.question || '',
      questionType: answerPayload.questionType,
      userAnswer: answerPayload.userAnswer,
      userAnswers: answerPayload.userAnswers,
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
        analysisData: parseAnalysisData(res.data.data.analysisData),
        analyzedAt: res.data.data.analyzedAt || null,
        reusedLastAnalysis: Boolean(res.data.data.reusedLastAnalysis),
        limitReached: Boolean(res.data.data.limitReached),
        message: res.data.data.message || '',
        quota: res.data.data.quota || null,
      }
    }

    const error = new Error(res.data?.message || 'AI ????')
    error.code = res.data?.code
    throw error
  }

  const cacheAnalysisResult = (errorId, analysis, analysisData = null) => {
    if (errorId == null) return

    const cache = readAnalysisCache()
    cache[String(errorId)] = {
      analysis: analysis || '',
      analysisData: analysisData || null,
      updatedAt: Date.now(),
    }
    writeAnalysisCache(cache)
  }

  const getCachedAnalysisResult = (errorId) => {
    if (errorId == null) return null

    const cached = readAnalysisCache()[String(errorId)]
    if (!cached) return null

    return {
      analysis: cached.analysis || '',
      analysisData: parseAnalysisData(cached.analysisData),
      updatedAt: cached.updatedAt || null,
    }
  }

  const applyAnalysisToCollection = (collection, errorId, analysis, analysisData = null) => {
    const item = collection.value.find((row) => Number(row.id) === Number(errorId))
    if (!item) return

    item.analysisStatus = '???'
    item.analysis = analysis
    item.analysisData = analysisData || null
  }

  const markAnalysis = (errorId, analysis, analysisData = null) => {
    cacheAnalysisResult(errorId, analysis, analysisData)
    applyAnalysisToCollection(errors, errorId, analysis, analysisData)
    applyAnalysisToCollection(completedErrors, errorId, analysis, analysisData)
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
    cacheAnalysisResult,
    getCachedAnalysisResult,
    markAnalysis,
  }
})