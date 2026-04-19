<template>
  <div v-if="isLoggedIn" class="floating-ai-assistant" :class="{ 'expanded': isExpanded, 'minimized': isMinimized }" :style="positionStyle">
    <!-- 头部 -->
    <div class="assistant-header" @mousedown="startDrag">
      <div class="header-content">
        <div class="assistant-icon">
          <span class="icon">🤖</span>
          <span class="status-indicator" :class="{ 'connected': isConnected }"></span>
        </div>
        <div class="assistant-title">AI 助手</div>
        <div class="header-actions">
          <button class="action-btn" @click="toggleMinimize" title="最小化">
            {{ isMinimized ? '📋' : '📌' }}
          </button>
          <button class="action-btn" @click="clearMessages" title="清空消息">
            🗑️
          </button>
        </div>
      </div>
    </div>

    <!-- 消息区域 -->
    <div v-if="!isMinimized" class="messages-container" ref="messagesContainer">
      <div class="message" v-for="msg in messages" :key="msg.id" :class="msg.role">
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
      </div>
      <div v-if="isLoading" class="loading-message">
        <span class="loading-spinner"></span>
        <span>AI 正在思考...</span>
      </div>
    </div>

    <!-- 输入区域 -->
    <div v-if="!isMinimized" class="input-container">
      <textarea
        v-model="inputMessage"
        class="message-input"
        placeholder="请输入您的问题..."
        @keyup.enter.exact="sendMessage"
        @keyup.enter.shift="inputMessage += '\n'"
        :disabled="isLoading"
      ></textarea>
      <button 
        class="send-btn" 
        @click="sendMessage"
        :disabled="!isConnected || isLoading || !inputMessage.trim()"
        :class="{ 'loading': isLoading }"
      >
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'

export default {
  name: 'FloatingAIAssistant',
  setup() {
    const userStore = useUserStore()
    
    // 状态管理
    const isVisible = ref(false)
    const isExpanded = ref(true)
    const isDragging = ref(false)
    const messages = ref([])
    const inputMessage = ref('')
    const isConnected = ref(false)
    const isLoading = ref(false)
    const isCancelling = ref(false)
    const currentMessageId = ref(null)
    const messagesContainer = ref(null)
    const isMinimized = ref(false)

    // 位置管理
    const position = ref({ x: 30, y: 30 })
    const dragOffset = ref({ x: 0, y: 0 })

    // WebSocket管理
    const ws = ref(null)
    const reconnectTimer = ref(null)
    const pingTimer = ref(null)

    // 计算属性
    const isLoggedIn = computed(() => !!userStore.userInfo?.token)

    const positionStyle = computed(() => {
      return {
        left: `${position.value.x}px`,
        top: `${position.value.y}px`,
        zIndex: 9999
      }
    })

    const wsUrl = computed(() => {
      const token = userStore.userInfo?.token
      const isDev = import.meta.env.DEV
      if (isDev) {
        return `/api/ws/chat${token ? `?token=${token}` : ''}`
      }
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      return `${protocol}//${host}/api/ws/chat${token ? `?token=${token}` : ''}`
    })

    // 方法
    const initWebSocket = () => {
      if (!isLoggedIn.value) return
      
      if (ws.value && ws.value.readyState === WebSocket.OPEN) {
        return
      }

      try {
        console.log('正在建立WebSocket连接:', wsUrl.value)
        ws.value = new WebSocket(wsUrl.value)

        ws.value.onopen = () => {
          console.log('WebSocket连接成功')
          isConnected.value = true
          isVisible.value = true
          startPing()
        }

        ws.value.onmessage = (event) => {
          try {
            const response = JSON.parse(event.data)
            handleMessage(response)
          } catch (e) {
            console.error('解析WebSocket消息失败', e)
          }
        }

        ws.value.onerror = (error) => {
          console.error('WebSocket错误', error)
          isConnected.value = false
        }

        ws.value.onclose = () => {
          console.log('WebSocket连接关闭')
          isConnected.value = false
          stopPing()
          scheduleReconnect()
        }
      } catch (e) {
        console.error('创建WebSocket失败', e)
        scheduleReconnect()
      }
    }

    const handleMessage = (response) => {
      switch (response.type) {
        case 'system':
          if (response.status === 'connected') {
            messages.value.push({
              id: Date.now(),
              role: 'system',
              content: '👋 ' + response.content,
              timestamp: new Date()
            })
          } else if (response.status === 'cleared') {
            messages.value = []
          }
          break
        case 'user':
          messages.value.push({
            id: Date.now(),
            role: 'user',
            content: response.content,
            timestamp: new Date()
          })
          scrollToBottom()
          break
        case 'assistant':
          if (response.status === 'thinking') {
            isLoading.value = true
            currentMessageId.value = response.id
          } else if (response.status === 'streaming') {
            const existingMessage = messages.value.find(msg => msg.id === response.id)
            if (existingMessage) {
              existingMessage.content = response.content
            } else {
              messages.value.push({
                id: response.id,
                role: 'assistant',
                content: response.content,
                timestamp: new Date()
              })
            }
            scrollToBottom()
          } else if (response.status === 'completed') {
            isLoading.value = false
            currentMessageId.value = null
            scrollToBottom()
          }
          break
      }
    }

    const sendMessage = () => {
      if (!isConnected.value || isLoading.value || !inputMessage.value.trim()) {
        return
      }

      const message = inputMessage.value.trim()
      inputMessage.value = ''

      try {
        if (ws.value && ws.value.readyState === WebSocket.OPEN) {
          ws.value.send(JSON.stringify({
            type: 'message',
            content: message
          }))
        }
      } catch (e) {
        console.error('发送消息失败', e)
      }
    }

    const clearMessages = () => {
      try {
        if (ws.value && ws.value.readyState === WebSocket.OPEN) {
          ws.value.send(JSON.stringify({
            type: 'clear'
          }))
        }
      } catch (e) {
        console.error('清空消息失败', e)
      }
    }

    const analyzeSelectedText = (text, type) => {
      if (!isConnected.value || isLoading.value) {
        return
      }

      let prompt = ''
      if (type === 'correctness') {
        prompt = `请分析以下代码的正确性：\n\n${text}`
      } else if (type === 'meaning') {
        prompt = `请解释以下代码的含义：\n\n${text}`
      }

      try {
        if (ws.value && ws.value.readyState === WebSocket.OPEN) {
          ws.value.send(JSON.stringify({
            type: 'message',
            content: prompt
          }))
        }
      } catch (e) {
        console.error('发送分析请求失败', e)
      }
    }

    const startPing = () => {
      stopPing()
      pingTimer.value = setInterval(() => {
        if (ws.value && ws.value.readyState === WebSocket.OPEN) {
          try {
            ws.value.send(JSON.stringify({ type: 'ping' }))
          } catch (e) {
            console.error('发送ping失败', e)
          }
        }
      }, 30000)
    }

    const stopPing = () => {
      if (pingTimer.value) {
        clearInterval(pingTimer.value)
        pingTimer.value = null
      }
    }

    const scheduleReconnect = () => {
      if (reconnectTimer.value) {
        clearTimeout(reconnectTimer.value)
      }
      reconnectTimer.value = setTimeout(() => {
        initWebSocket()
      }, 5000)
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // 拖拽功能
    const startDrag = (e) => {
      isDragging.value = true
      dragOffset.value = {
        x: e.clientX - position.value.x,
        y: e.clientY - position.value.y
      }
      document.addEventListener('mousemove', onDrag)
      document.addEventListener('mouseup', stopDrag)
    }

    const onDrag = (e) => {
      if (isDragging.value) {
        position.value = {
          x: e.clientX - dragOffset.value.x,
          y: e.clientY - dragOffset.value.y
        }
      }
    }

    const stopDrag = () => {
      isDragging.value = false
      document.removeEventListener('mousemove', onDrag)
      document.removeEventListener('mouseup', stopDrag)
    }

    const toggleMinimize = () => {
      isMinimized.value = !isMinimized.value
    }

    // 生命周期
    onMounted(() => {
      watch(
        () => isLoggedIn.value,
        (newValue) => {
          if (newValue) {
            initWebSocket()
          } else {
            if (ws.value) {
              ws.value.close()
              ws.value = null
            }
            isConnected.value = false
            messages.value = []
          }
        },
        { immediate: true }
      )
    })

    onUnmounted(() => {
      if (ws.value) {
        ws.value.close()
      }
      stopPing()
      if (reconnectTimer.value) {
        clearTimeout(reconnectTimer.value)
      }
    })

    return {
      isLoggedIn,
      isConnected,
      isExpanded,
      isMinimized,
      messages,
      inputMessage,
      isLoading,
      positionStyle,
      messagesContainer,
      startDrag,
      sendMessage,
      clearMessages,
      toggleMinimize,
      analyzeSelectedText
    }
  }
}
</script>

<style scoped>
.floating-ai-assistant {
  position: fixed;
  width: 350px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transition: all 0.3s ease;
  z-index: 9999;
}

.assistant-header {
  background: #f8f9fa;
  padding: 12px 16px;
  border-bottom: 1px solid #e9ecef;
  cursor: move;
  user-select: none;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.assistant-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon {
  font-size: 18px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dc3545;
  transition: background 0.3s ease;
}

.status-indicator.connected {
  background: #28a745;
}

.assistant-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.messages-container {
  height: 300px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}

.message {
  margin-bottom: 12px;
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 18px;
  position: relative;
}

.message.user {
  align-self: flex-end;
  background: #007bff;
  color: white;
  margin-left: auto;
  border-bottom-right-radius: 4px;
}

.message.assistant {
  align-self: flex-start;
  background: white;
  color: #333;
  border: 1px solid #e9ecef;
  border-bottom-left-radius: 4px;
}

.message.system {
  align-self: center;
  background: #e9ecef;
  color: #6c757d;
  font-size: 12px;
  max-width: 90%;
  text-align: center;
  border-radius: 12px;
}

.message-content {
  word-wrap: break-word;
  line-height: 1.4;
}

.message-time {
  font-size: 10px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  font-size: 14px;
  margin-top: 8px;
}

.loading-spinner {
  width: 12px;
  height: 12px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.input-container {
  display: flex;
  padding: 12px 16px;
  border-top: 1px solid #e9ecef;
  background: white;
}

.message-input {
  flex: 1;
  border: 1px solid #ced4da;
  border-radius: 20px;
  padding: 10px 16px;
  resize: none;
  font-size: 14px;
  line-height: 1.4;
  min-height: 40px;
  max-height: 120px;
  outline: none;
  transition: border-color 0.2s ease;
}

.message-input:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.message-input:disabled {
  background: #e9ecef;
  cursor: not-allowed;
}

.send-btn {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 16px;
  margin-left: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  background: #0069d9;
}

.send-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.send-btn.loading {
  background: #17a2b8;
}

/* 最小化状态 */
.floating-ai-assistant.minimized {
  width: 60px;
  height: 60px;
}

.floating-ai-assistant.minimized .header-content {
  justify-content: center;
}

.floating-ai-assistant.minimized .assistant-title,
.floating-ai-assistant.minimized .header-actions,
.floating-ai-assistant.minimized .messages-container,
.floating-ai-assistant.minimized .input-container {
  display: none;
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>