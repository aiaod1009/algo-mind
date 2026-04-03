import axios from 'axios'
import { userApi } from './user'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const userRaw = localStorage.getItem('user')
  if (userRaw) {
    try {
      const token = JSON.parse(userRaw)?.token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    } catch (error) {
      console.warn('用户信息解析失败，忽略 token 注入。', error)
    }
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  },
)

api.getUserProblemHeatmap = userApi.getUserProblemHeatmap
api.getUserProblemStats = userApi.getUserProblemStats
api.getUserActivities = userApi.getUserActivities
api.updateUserProfile = userApi.updateUserProfile
api.getUserStatus = userApi.getUserStatus
api.updateUserStatus = userApi.updateUserStatus
api.getUserErrors = userApi.getUserErrors
api.addUserError = userApi.addUserError
api.deleteUserError = userApi.deleteUserError
api.getUserRanking = userApi.getUserRanking
api.getUserPointsHistory = userApi.getUserPointsHistory
api.getUserCreatedProblems = userApi.getUserCreatedProblems
api.getUserSolvedProblems = userApi.getUserSolvedProblems
api.analyzeError = userApi.analyzeError

// 学习计划相关
api.getLearningPlans = userApi.getLearningPlans
api.getCurrentLearningPlan = userApi.getCurrentLearningPlan
api.generateLearningPlan = userApi.generateLearningPlan
api.saveLearningPlan = userApi.saveLearningPlan
api.deleteLearningPlan = userApi.deleteLearningPlan

// 头像相关
api.uploadAvatar = userApi.uploadAvatar
api.uploadAvatarFromUrl = userApi.uploadAvatarFromUrl
api.getAvatar = userApi.getAvatar
api.deleteAvatar = userApi.deleteAvatar

export default api
