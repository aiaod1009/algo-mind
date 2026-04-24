import api from './index'
import { withNonBlockingAuth } from './requestOptions'

const AI_TIMEOUT = 120000
const LONG_TIMEOUT = 60000
const VERY_LONG_TIMEOUT = 180000

const getStreamHeaders = () => {
  const headers = {
    'Content-Type': 'application/json',
  }

  const userRaw = localStorage.getItem('user')
  if (!userRaw) {
    return headers
  }

  try {
    const token = JSON.parse(userRaw)?.token
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  } catch (error) {
    console.warn('解析用户 token 失败，流式请求将继续匿名发送。', error)
  }

  return headers
}

const extractSsePayload = (block) => {
  if (!block) {
    return null
  }

  const payload = block
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice(5).replace(/^ /, ''))
    .join('\n')

  if (!payload || payload === '[DONE]') {
    return null
  }

  return payload
}

const streamRequest = async (url, data, onMessage, onComplete, onError) => {
  let hasDeliveredContent = false

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: getStreamHeaders(),
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || `请求失败，状态码：${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('当前环境不支持流式读取')
    }

    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let sseMode = null

    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }

      const chunk = decoder.decode(value, { stream: true }).replace(/\r/g, '')
      buffer += chunk

      if (sseMode === null) {
        sseMode = buffer.startsWith('data:') || buffer.includes('\ndata:') || buffer.includes('event:')
      }

      if (!sseMode) {
        hasDeliveredContent = true
        await onMessage?.(chunk)
        buffer = ''
        continue
      }

      let separatorIndex = buffer.indexOf('\n\n')
      while (separatorIndex !== -1) {
        const eventBlock = buffer.slice(0, separatorIndex)
        buffer = buffer.slice(separatorIndex + 2)
        const payload = extractSsePayload(eventBlock)
        if (payload !== null) {
          hasDeliveredContent = true
          await onMessage?.(payload)
        }
        separatorIndex = buffer.indexOf('\n\n')
      }
    }

    if (buffer.length > 0) {
      if (sseMode) {
        const payload = extractSsePayload(buffer)
        if (payload !== null) {
          hasDeliveredContent = true
          await onMessage?.(payload)
        }
      } else {
        hasDeliveredContent = true
        await onMessage?.(buffer)
      }
    }

    await onComplete?.()
  } catch (error) {
    console.error('AI 流式请求出错：', error)
    if (hasDeliveredContent) {
      await onComplete?.()
      return
    }
    await onError?.(error)
  }
}

// WebSocket 连接管理
class WebSocketManager {
  constructor() {
    this.socket = null
    this.connected = false
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 3
    this.reconnectDelay = 1000
    this.callbacks = new Map()
    this.messageId = 0
  }

  connect() {
    return new Promise((resolve, reject) => {
      if (this.connected && this.socket) {
        resolve()
        return
      }

      // 构建 WebSocket URL
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const wsUrl = `${wsProtocol}//${window.location.host}/ws/ai/chat`

      this.socket = new WebSocket(wsUrl)

      this.socket.onopen = () => {
        console.log('WebSocket 连接成功')
        this.connected = true
        this.reconnectAttempts = 0
        resolve()
      }

      this.socket.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.handleMessage(data)
        } catch (error) {
          console.error('WebSocket 消息解析失败：', error)
        }
      }

      this.socket.onclose = () => {
        console.log('WebSocket 连接关闭')
        this.connected = false
        this.attemptReconnect()
      }

      this.socket.onerror = (error) => {
        console.error('WebSocket 错误：', error)
        reject(error)
      }
    })
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连 WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      setTimeout(() => {
        this.connect().catch(err => console.error('重连失败：', err))
      }, this.reconnectDelay * this.reconnectAttempts)
    }
  }

  handleMessage(data) {
    // 处理 WebSocket 消息
    console.log('收到 WebSocket 消息：', data)
    
    // 这里可以根据需要处理不同类型的消息
    // 目前我们只处理 AI 聊天消息
    if (data.type === 'message') {
      // 调用回调函数处理消息
      this.callbacks.forEach(callback => {
        callback(data)
      })
    }
  }

  send(message) {
    return new Promise((resolve, reject) => {
      const sendMessage = async () => {
        if (!this.connected) {
          await this.connect()
        }

        if (this.socket && this.connected) {
          this.socket.send(JSON.stringify(message))
          resolve()
        } else {
          reject(new Error('WebSocket 未连接'))
        }
      }

      sendMessage().catch(reject)
    })
  }

  registerCallback(callback) {
    const id = this.messageId++
    this.callbacks.set(id, callback)
    return id
  }

  unregisterCallback(id) {
    this.callbacks.delete(id)
  }

  close() {
    if (this.socket) {
      this.socket.close()
      this.connected = false
    }
  }
}

// 创建单例实例
const wsManager = new WebSocketManager()

// WebSocket AI 对话
const wsAiChat = async (data, onMessage, onComplete, onError) => {
  try {
    // 注册回调
    const callbackId = wsManager.registerCallback((message) => {
      if (message.type === 'message') {
        onMessage?.(message.content)
        if (message.finished) {
          onComplete?.()
          wsManager.unregisterCallback(callbackId)
        }
      } else if (message.type === 'error') {
        onError?.(new Error(message.content))
        wsManager.unregisterCallback(callbackId)
      }
    })

    // 发送消息
    await wsManager.send(data)
  } catch (error) {
    console.error('WebSocket AI 对话失败：', error)
    onError?.(error)
  }
}

export const userApi = {
  getUserProblemHeatmap(year) {
    return api.get(`/users/me/heatmap?year=${year}`)
  },

  getUserProblemStats() {
    return api.get('/users/me/stats')
  },

  getUserAuthorLevel() {
    return api.get('/users/me/author-level')
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
    return api.delete(`/errors/${Number(errorId)}`)
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

  analyzeError(payloadOrErrorId, description) {
    const payload = typeof payloadOrErrorId === 'object' && payloadOrErrorId !== null
      ? payloadOrErrorId
      : { errorId: payloadOrErrorId, description }

    return api.post('/error-analysis', payload, { timeout: AI_TIMEOUT })
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
      withNonBlockingAuth({ timeout: VERY_LONG_TIMEOUT }),
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

  getAvatar() {
    return api.get('/users/me/avatar')
  },

  deleteAvatar() {
    return api.delete('/users/me/avatar')
  },

  aiChat(data) {
    return api.post('/ai/chat', data, withNonBlockingAuth({ timeout: AI_TIMEOUT }))
  },

  aiChatStream(data, onMessage, onComplete, onError) {
    return streamRequest('/api/ai/chat/stream', data, onMessage, onComplete, onError)
  },

  // WebSocket AI 对话
  aiChatWebSocket(data, onMessage, onComplete, onError) {
    return wsAiChat(data, onMessage, onComplete, onError)
  },

  evaluateCode(data) {
    return api.post('/ai/evaluate-code', data, { timeout: AI_TIMEOUT })
  },

  evaluateCodeStream(data, onMessage, onComplete, onError) {
    return streamRequest('/api/ai/evaluate-code/stream', data, onMessage, onComplete, onError)
  },

  runCode(data) {
    return api.post('/run-code', data, { timeout: AI_TIMEOUT })
  },

  getCourseRecommendations() {
    return api.get('/course-recommendations', withNonBlockingAuth())
  },

  getCodeSnapshots(params = {}) {
    return api.get('/code-snapshots', { params })
  },

  getBestCodeSnapshot(levelId) {
    return api.get('/code-snapshots/best', { params: { levelId } })
  },

  getCodeSnapshotStats() {
    return api.get('/code-snapshots/stats')
  },

  saveCodeSnapshot(data) {
    return api.post('/code-snapshots', data)
  },

  deleteCodeSnapshot(id) {
    return api.delete(`/code-snapshots/${id}`)
  },

  compareCode(data) {
    return api.post('/code-comparison', data, { timeout: AI_TIMEOUT })
  },

  // 聊天文件上传
  uploadChatFile(formData) {
    return api.post('/ai/chat/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: LONG_TIMEOUT,
    })
  },

  // 多模态 AI 对话（支持图片）
  aiChatMultimodal(data) {
    return api.post('/ai/chat/multimodal', data, {
      timeout: AI_TIMEOUT,
    })
  },

  // 多模态 AI 对话流式（支持图片）
  aiChatMultimodalStream(data, onMessage, onComplete, onError) {
    return streamRequest('/api/ai/chat/multimodal/stream', data, onMessage, onComplete, onError)
  },
}

export default userApi
