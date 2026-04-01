import api from './index'

// 用户相关API
export const userApi = {
  // 获取用户做题热力图数据
  getUserProblemHeatmap(year) {
    return api.get(`/user/heatmap?year=${year}`)
  },

  // 获取用户做题统计
  getUserProblemStats() {
    return api.get('/user/stats')
  },

  // 获取用户活动列表
  getUserActivities(params = {}) {
    return api.get('/user/activities', { params })
  },

  // 更新用户资料
  updateUserProfile(data) {
    return api.put('/user/profile', data)
  },

  // 获取用户状态设置
  getUserStatus() {
    return api.get('/user/status')
  },

  // 更新用户状态设置
  updateUserStatus(data) {
    return api.put('/user/status', data)
  },

  // 获取用户错题本
  getUserErrors() {
    return api.get('/user/errors')
  },

  // 添加错题
  addUserError(data) {
    return api.post('/user/errors', data)
  },

  // 删除错题
  deleteUserError(errorId) {
    return api.delete(`/user/errors/${errorId}`)
  },

  // 获取用户排行榜位置
  getUserRanking() {
    return api.get('/user/ranking')
  },

  // 获取用户积分记录
  getUserPointsHistory() {
    return api.get('/user/points-history')
  },

  // 获取用户创建的题目
  getUserCreatedProblems() {
    return api.get('/user/created-problems')
  },

  // 获取用户完成的题目
  getUserSolvedProblems() {
    return api.get('/user/solved-problems')
  },
}

export default userApi
