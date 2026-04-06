import axios from 'axios'
import { ElMessage } from 'element-plus'
import { userApi } from './user'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const userRaw = localStorage.getItem('user')
  if (!userRaw) {
    return config
  }

  try {
    const token = JSON.parse(userRaw)?.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  } catch (error) {
    console.warn('用户信息解析失败，忽略 token 注入。', error)
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || error?.message || '请求失败'
    const preserveSessionOn401 = Boolean(error?.config?.preserveSessionOn401)
    const suppressAuthMessage = Boolean(error?.config?.suppressAuthMessage)

    if (status === 401) {
      if (!preserveSessionOn401) {
        localStorage.removeItem('user')
      }
      if (!suppressAuthMessage) {
        ElMessage.error('登录已过期，请重新登录')
      }
    } else if (status === 403) {
      ElMessage.error('无权限访问该资源')
    } else if (status === 500) {
      ElMessage.error('服务器内部错误，请稍后重试')
    } else {
      ElMessage.error(message)
    }

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

api.getLearningPlans = userApi.getLearningPlans
api.getCurrentLearningPlan = userApi.getCurrentLearningPlan
api.generateLearningPlan = userApi.generateLearningPlan
api.saveLearningPlan = userApi.saveLearningPlan
api.deleteLearningPlan = userApi.deleteLearningPlan

api.uploadAvatar = userApi.uploadAvatar
api.uploadAvatarFromUrl = userApi.uploadAvatarFromUrl
api.getAvatar = userApi.getAvatar
api.deleteAvatar = userApi.deleteAvatar
api.aiChat = userApi.aiChat
api.evaluateCode = userApi.evaluateCode
api.getCourseRecommendations = userApi.getCourseRecommendations

export default api
