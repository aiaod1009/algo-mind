import api from './index'

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
    return api.post('/error-analysis', { errorId, description })
  },

  // 学习计划相关接口
  getLearningPlans() {
    return api.get('/learning-plans')
  },

  getCurrentLearningPlan(track = 'algo') {
    return api.get(`/learning-plans/current?track=${track}`)
  },

  generateLearningPlan(data) {
    return api.post('/learning-plans/generate', data)
  },

  saveLearningPlan(data) {
    return api.post('/learning-plans', data)
  },

  deleteLearningPlan(planId) {
    return api.delete(`/learning-plans/${planId}`)
  },

  // 头像相关接口
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    // 不设置 Content-Type，让浏览器自动设置 boundary
    return api.post('/users/me/avatar', formData)
  },

  getAvatar() {
    return api.get('/users/me/avatar')
  },

  deleteAvatar() {
    return api.delete('/users/me/avatar')
  },
}

export default userApi
