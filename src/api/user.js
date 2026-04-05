import api from './index'

const AI_TIMEOUT = 60000
const LONG_TIMEOUT = 30000

const createApiCall = (method, url, timeout) => {
  return async (dataOrParams) => {
    const config = { timeout }
    if (method === 'get') {
      config.params = dataOrParams
      return api.get(url, config)
    } else if (method === 'post') {
      return api.post(url, dataOrParams, config)
    } else if (method === 'put') {
      return api.put(url, dataOrParams, config)
    } else if (method === 'delete') {
      return api.delete(url, config)
    }
  }
}

export const userApi = {
  getUserProblemHeatmap(year) {
    return api.get(`/users/me/heatmap?year=${year}`)
  },

  getUserProblemStats() {
    return api.get('/users/me/stats')
  },

  getUserActivities(params = {}) {
    return api.get('/users/me/activities', { params })
  },

  updateUserProfile(data) {
    return api.put('/users/me', data)
  },

  getUserStatus() {
    return api.get('/users/me/status')
  },

  updateUserStatus(data) {
    return api.put('/users/me/status', data)
  },

  getUserErrors() {
    return api.get('/errors')
  },

  addUserError(data) {
    return api.post('/errors', data)
  },

  deleteUserError(errorId) {
    return api.delete(`/errors/${errorId}`)
  },

  getUserRanking() {
    return api.get('/users/me/ranking')
  },

  getUserPointsHistory(params = {}) {
    return api.get('/users/me/points-history', { params })
  },

  getUserCreatedProblems(params = {}) {
    return api.get('/users/me/created-problems', { params })
  },

  getUserSolvedProblems(params = {}) {
    return api.get('/users/me/solved-problems', { params })
  },

  analyzeError(errorId, description) {
    return api.post('/error-analysis', { errorId, description }, { timeout: AI_TIMEOUT })
  },

  getLearningPlans() {
    return api.get('/learning-plans')
  },

  getCurrentLearningPlan(track = 'algo') {
    return api.get(`/learning-plans/current?track=${track}`, { timeout: LONG_TIMEOUT })
  },

  generateLearningPlan(data) {
    return api.post('/learning-plans/generate', data, { timeout: AI_TIMEOUT })
  },

  saveLearningPlan(data) {
    return api.post('/learning-plans', data)
  },

  deleteLearningPlan(planId) {
    return api.delete(`/learning-plans/${planId}`)
  },

  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: LONG_TIMEOUT
    })
  },

  uploadAvatarFromUrl(data) {
    return api.post('/users/me/avatar-from-url', data)
  },

  getAvatar() {
    return api.get('/users/me/avatar')
  },

  deleteAvatar() {
    return api.delete('/users/me/avatar')
  },

  aiChat(data) {
    return api.post('/ai/chat', data, { timeout: AI_TIMEOUT })
  },

  evaluateCode(data) {
    return api.post('/ai/evaluate-code', data, { timeout: AI_TIMEOUT })
  },
}

export default userApi
