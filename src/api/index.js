import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
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

api.getUserProblemHeatmap = (year) => {
  return api
    .get('/user/problem-heatmap', {
      params: { year },
    })
    .then((res) => {
      const payload = res?.data || {}
      return {
        success: payload.success ?? payload.code === 0,
        data: payload.data || [],
        message: payload.message || '',
        code: payload.code,
      }
    })
}

api.getUserProblemStats = () => {
  return api.get('/user/problem-stats').then((res) => {
    const payload = res?.data || {}
    return {
      success: payload.success ?? payload.code === 0,
      data: payload.data || null,
      message: payload.message || '',
      code: payload.code,
    }
  })
}

api.getUserActivities = (params = {}) => {
  return api
    .get('/user/activities', {
      params,
    })
    .then((res) => {
      const payload = res?.data || {}
      return {
        success: payload.success ?? payload.code === 0,
        data: payload.data || [],
        message: payload.message || '',
        code: payload.code,
      }
    })
}

api.updateUserProfile = (profile) => {
  return api.put('/user/profile', profile).then((res) => {
    const payload = res?.data || {}
    return {
      success: payload.success ?? payload.code === 0,
      data: payload.data || null,
      message: payload.message || '',
      code: payload.code,
    }
  })
}

export default api
