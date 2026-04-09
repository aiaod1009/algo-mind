import api from './index'
import { withNonBlockingAuth } from './requestOptions'

const AI_TIMEOUT = 60000
const LONG_TIMEOUT = 30000

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
    return api.get(
      `/learning-plans/current?track=${track}`,
      withNonBlockingAuth({ timeout: LONG_TIMEOUT }),
    )
  },

  generateLearningPlan(data) {
    return api.post(
      '/learning-plans/generate',
      data,
      withNonBlockingAuth({ timeout: AI_TIMEOUT }),
    )
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
      timeout: LONG_TIMEOUT,
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
    return api.post('/ai/chat', data, withNonBlockingAuth({ timeout: AI_TIMEOUT }))
  },

  evaluateCode(data) {
    return api.post('/ai/evaluate-code', data, { timeout: AI_TIMEOUT })
  },

  runCode(data) {
    return api.post('/run-code', data, { timeout: AI_TIMEOUT })
  },

  getCourseRecommendations() {
    return api.get('/course-recommendations', withNonBlockingAuth())
  },
}

export default userApi
