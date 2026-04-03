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

  /**
   * 生成个性化学习计划
   * @param {Object} data - 用户学习数据
   * @param {string} data.track - 目标赛道 (algo/ds/contest)
   * @param {string} data.trackLabel - 赛道标签
   * @param {number} data.weeklyGoal - 每周目标题数
   * @param {number} data.weeklyCompleted - 本周已完成题数
   * @param {string[]} data.errorTopics - 历史错题知识点标签
   * @param {string[]} data.strongAreas - 擅长领域
   * @param {string[]} data.weakAreas - 薄弱环节
   * @param {number} data.persistenceIndex - 坚持指数 (0-100)
   * @param {number} data.totalSolved - 累计解题数
   * @param {number} data.currentStreak - 当前连续打卡天数
   */
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

  uploadAvatarFromUrl(data) {
    return api.post('/users/me/avatar-from-url', data)
  },

  getAvatar() {
    return api.get('/users/me/avatar')
  },

  deleteAvatar() {
    return api.delete('/users/me/avatar')
  },
}

export default userApi
