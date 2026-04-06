import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api'
import { getAvatarUrl, getFullFileUrl } from '../utils/file'

const LEADERBOARD_KEY = 'leaderboard'
const TRACK_KEY = 'learning-track'
const DEFAULT_NAME = 'Anonymous User'
const DEFAULT_BIO = 'Keep a steady pace.'

const normalizeUserAvatar = (avatar) => getFullFileUrl(avatar || '')

const normalizeUserPayload = (user) => {
  if (!user) return null
  return {
    ...user,
    avatar: normalizeUserAvatar(user.avatar),
  }
}

const readLocalUser = () => {
  const userRaw = localStorage.getItem('user')
  if (!userRaw) return null
  try {
    return normalizeUserPayload(JSON.parse(userRaw))
  } catch (error) {
    return null
  }
}

const readLeaderboard = () => {
  const raw = localStorage.getItem(LEADERBOARD_KEY)
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

const saveLeaderboard = (list) => {
  localStorage.setItem(LEADERBOARD_KEY, JSON.stringify(list))
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(readLocalUser())
  const points = ref(Number(localStorage.getItem('points') || 0))
  const selectedTrack = ref(localStorage.getItem(TRACK_KEY) || 'algo')

  const avatar = computed(() => getAvatarUrl(userInfo.value?.avatar))
  const isLogin = computed(() => !!userInfo.value)

  const syncLeaderboard = (payloadUser, payloadPoints) => {
    if (!payloadUser?.id) return
    const list = readLeaderboard()
    const currentIndex = list.findIndex((item) => Number(item.id) === Number(payloadUser.id))
    const nextItem = {
      id: payloadUser.id,
      name: payloadUser.name || DEFAULT_NAME,
      points: Number(payloadPoints || 0),
      targetTrack: payloadUser.targetTrack || selectedTrack.value,
      updatedAt: Date.now(),
    }
    if (currentIndex >= 0) {
      list[currentIndex] = { ...list[currentIndex], ...nextItem }
    } else {
      list.push(nextItem)
    }
    saveLeaderboard(list)
  }

  const syncUser = (user, score) => {
    const normalizedUser = normalizeUserPayload(user) || {}
    const mergedUser = {
      ...normalizedUser,
      targetTrack: normalizedUser.targetTrack || selectedTrack.value,
      weeklyGoal: Number(normalizedUser.weeklyGoal || 10),
      bio: normalizedUser.bio || DEFAULT_BIO,
      gender: normalizedUser.gender || 'unknown',
      avatar: normalizedUser.avatar || '',
    }
    userInfo.value = mergedUser
    points.value = score
    selectedTrack.value = mergedUser.targetTrack
    localStorage.setItem('user', JSON.stringify(mergedUser))
    localStorage.setItem('points', String(score))
    localStorage.setItem(TRACK_KEY, selectedTrack.value)
    syncLeaderboard(mergedUser, score)
  }

  const login = async (username, password) => {
    const res = await api.post('/login', { username, password })
    if (res.data?.code === 0) {
      const nextUser = {
        ...res.data.data.user,
        token: res.data.data.token,
        targetTrack: res.data.data.user?.targetTrack || selectedTrack.value,
        weeklyGoal: Number(res.data.data.user?.weeklyGoal || 10),
        bio: res.data.data.user?.bio || DEFAULT_BIO,
        gender: res.data.data.user?.gender || 'unknown',
        avatar: normalizeUserAvatar(res.data.data.user?.avatar),
      }
      const nextPoints = Number(res.data.data.points || 0)
      syncUser(nextUser, nextPoints)
      return true
    }
    return false
  }

  const addPoints = (value) => {
    points.value += Number(value || 0)
    localStorage.setItem('points', String(points.value))
    syncLeaderboard(userInfo.value, points.value)
  }

  const setPoints = (value) => {
    points.value = Number(value || 0)
    localStorage.setItem('points', String(points.value))
    syncLeaderboard(userInfo.value, points.value)
  }

  const setTrack = (trackCode) => {
    selectedTrack.value = trackCode
    localStorage.setItem(TRACK_KEY, trackCode)
    if (userInfo.value) {
      userInfo.value.targetTrack = trackCode
      localStorage.setItem('user', JSON.stringify(userInfo.value))
      syncLeaderboard(userInfo.value, points.value)
    }
  }

  const updateProfile = (payload) => {
    if (!userInfo.value) return

    const nextAvatar = payload.avatar === undefined
      ? userInfo.value.avatar
      : normalizeUserAvatar(payload.avatar)

    userInfo.value = {
      ...userInfo.value,
      ...payload,
      avatar: nextAvatar,
      weeklyGoal: Number(payload.weeklyGoal || userInfo.value.weeklyGoal || 10),
    }
    selectedTrack.value = userInfo.value.targetTrack || selectedTrack.value
    localStorage.setItem('user', JSON.stringify(userInfo.value))
    localStorage.setItem(TRACK_KEY, selectedTrack.value)
    syncLeaderboard(userInfo.value, points.value)
  }

  const getLeaderboard = () => {
    return readLeaderboard()
      .slice()
      .sort((a, b) => Number(b.points || 0) - Number(a.points || 0))
      .map((item, index) => ({ ...item, rank: index + 1 }))
  }

  const logout = () => {
    userInfo.value = null
    points.value = 0
    localStorage.removeItem('user')
    localStorage.removeItem('points')
    localStorage.removeItem('levels')
    localStorage.removeItem(TRACK_KEY)
  }

  const updateAvatar = (avatarPath) => {
    if (!userInfo.value) return

    userInfo.value.avatar = normalizeUserAvatar(avatarPath)
    localStorage.setItem('user', JSON.stringify(userInfo.value))
  }

  return {
    userInfo,
    points,
    selectedTrack,
    avatar,
    isLogin,
    login,
    logout,
    addPoints,
    setPoints,
    setTrack,
    updateProfile,
    updateAvatar,
    getLeaderboard,
  }
})
