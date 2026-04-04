import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const TRACKS = [
  { code: 'algo', name: '算法思维赛道', goal: '掌握常见算法范式，提升解题速度' },
  { code: 'ds', name: '数据结构赛道', goal: '强化结构设计与复杂度控制能力' },
  { code: 'contest', name: '竞赛冲刺赛道', goal: '在限时条件下稳定完成中高难度题目' },
]

const LOCAL_LEVEL_KEY = 'levels'

const readLocalLevels = () => {
  const raw = localStorage.getItem(LOCAL_LEVEL_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

const saveLocalLevels = (levels) => {
  localStorage.setItem(LOCAL_LEVEL_KEY, JSON.stringify(levels))
}

const normalizeNumber = (value, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const clampStars = (value) => {
  return Math.max(0, Math.min(3, Math.floor(normalizeNumber(value, 0))))
}

export const useLevelStore = defineStore('level', () => {
  const levels = ref([])
  const tracks = ref(TRACKS)

  const normalizeLevels = (rawLevels) => {
    return rawLevels.map((item) => ({
      ...item,
      track: item.track || 'algo',
      order: Number(item.order || 1),
      type: item.type || 'single',
      isUnlocked: Boolean(item.isUnlocked),
      stars: clampStars(item.stars ?? item.bestStars ?? 0),
      bestStars: clampStars(item.bestStars ?? item.stars ?? 0),
      isCompleted: Boolean(item.isCompleted || clampStars(item.bestStars ?? item.stars ?? 0) > 0),
      attemptCount: Math.max(0, Math.floor(normalizeNumber(item.attemptCount, 0))),
      bestTimeMs: item.bestTimeMs == null ? null : Math.max(0, Math.floor(normalizeNumber(item.bestTimeMs, 0))),
      targetTimeMs: Math.max(1000, Math.floor(normalizeNumber(item.targetTimeMs, 90000))),
    }))
  }

  const fetchLevels = async () => {
    const res = await api.get('/levels')
    if (res.data?.code === 0 && Array.isArray(res.data.data)) {
      levels.value = normalizeLevels(res.data.data)
      saveLocalLevels(levels.value)
      return
    }
    throw new Error(res.data?.message || '获取关卡失败')
  }

  const hydrateLevelsFromLocal = () => {
    const local = readLocalLevels()
    if (!Array.isArray(local) || !local.length) return false
    levels.value = normalizeLevels(local)
    return true
  }

  const findLevelById = (id) => levels.value.find((item) => Number(item.id) === Number(id))

  const getLevelsByTrack = (trackCode) => {
    return levels.value
      .filter((item) => item.track === trackCode)
      .sort((a, b) => Number(a.order || 0) - Number(b.order || 0))
  }

  const submitAnswer = async (levelId, answer, meta = {}) => {
    const payload = { levelId, answer, meta }
    if (meta?.language) {
      payload.language = meta.language
    }
    if (meta?.stdinInput != null) {
      payload.stdinInput = meta.stdinInput
    }
    if (typeof answer === 'string') {
      payload.code = answer
    }

    const res = await api.post('/submit', payload)
    if (res.data?.code === 0) {
      return res.data.data
    }
    throw new Error(res.data?.message || '提交失败')
  }

  return {
    levels,
    tracks,
    fetchLevels,
    hydrateLevelsFromLocal,
    submitAnswer,
    findLevelById,
    getLevelsByTrack,
  }
})
