<template>
  <div
    v-if="isLoggedIn"
    class="floating-ai-assistant"
    :class="{ minimized: isMinimized, dragging: isDragging, resizing: isResizing }"
    :style="positionStyle"
  >
    <div class="assistant-header" @mousedown.left.prevent="startDrag" @click="handleHeaderClick">
      <div class="header-content">
        <div class="assistant-icon" @click.stop="handleAssistantIconClick">
          <span class="icon">AI</span>
          <span class="status-indicator" :class="{ connected: isConnected }"></span>
        </div>
        <div class="assistant-title">AI 助手</div>
        <div class="header-actions">
          <button class="action-btn" @click.stop="toggleMinimize" :title="isMinimized ? '展开' : '最小化'">
            {{ isMinimized ? '□' : '_' }}
          </button>
          <button class="action-btn" @click.stop="clearMessages" title="清空消息">
            x
          </button>
        </div>
      </div>
    </div>

    <div v-if="!isMinimized" ref="messagesContainer" class="messages-container">
      <div v-for="msg in messages" :key="msg.id" class="message" :class="msg.role">
        <div class="message-content">{{ msg.content }}</div>
        <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
      </div>
      <div v-if="isLoading" class="loading-message">
        <span class="loading-spinner"></span>
        <span>AI 正在思考...</span>
      </div>
    </div>

    <div v-if="!isMinimized" class="input-container">
      <textarea
        v-model="inputMessage"
        class="message-input"
        placeholder="请输入您的问题..."
        @keyup.enter.exact.prevent="sendMessage"
        @keyup.enter.shift="inputMessage += '\n'"
        :disabled="isLoading"
      ></textarea>
      <button
        class="send-btn"
        @click="sendMessage"
        :disabled="!isConnected || isLoading || !inputMessage.trim()"
        :class="{ loading: isLoading }"
      >
        {{ isLoading ? '发送中...' : '发送' }}
      </button>
    </div>

    <button
      v-if="!isMinimized"
      class="resize-handle"
      type="button"
      title="拖动调整大小"
      @mousedown.left.prevent.stop="startResize"
    ></button>
  </div>
</template>

<script>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useUserStore } from '../stores/user'

export default {
  name: 'FloatingAIAssistant',
  setup() {
    const userStore = useUserStore()

    const isDragging = ref(false)
    const isConnected = ref(false)
    const isLoading = ref(false)
    const isMinimized = ref(true)
    const isResizing = ref(false)
    const inputMessage = ref('')
    const messages = ref([])
    const messagesContainer = ref(null)
    const currentMessageId = ref(null)
    const ws = ref(null)
    const reconnectTimer = ref(null)
    const pingTimer = ref(null)
    const position = ref({ x: 30, y: 30 })
    const size = ref({ width: 350, height: 430 })
    const dragOffset = ref({ x: 0, y: 0 })
    const dragStartPoint = ref({ x: 0, y: 0 })
    const dragMoved = ref(false)
    const suppressHeaderClick = ref(false)
    const resizeStartPoint = ref({ x: 0, y: 0 })
    const resizeStartSize = ref({ width: 350, height: 430 })
    const DRAG_THRESHOLD = 3
    const MIN_WIDTH = 300
    const MIN_HEIGHT = 320
    const DEFAULT_WIDTH = 350
    const DEFAULT_HEIGHT = 430
    const MINIMIZED_WIDTH = 60
    const MINIMIZED_HEIGHT = 60

    const isLoggedIn = computed(() => !!userStore.userInfo?.token)

    const positionStyle = computed(() => ({
      transform: `translate3d(${position.value.x}px, ${position.value.y}px, 0)`,
      width: `${isMinimized.value ? MINIMIZED_WIDTH : size.value.width}px`,
      height: `${isMinimized.value ? MINIMIZED_HEIGHT : size.value.height}px`,
      zIndex: 9999,
    }))

    const getAssistantWidth = () => (isMinimized.value ? MINIMIZED_WIDTH : size.value.width)

    const snapToWindowEdgeIfIntersecting = () => {
      if (typeof window === 'undefined') {
        return
      }

      const assistantWidth = getAssistantWidth()
      const viewportWidth = window.innerWidth
      const intersectsLeftEdge = position.value.x < 0
      const intersectsRightEdge = position.value.x + assistantWidth > viewportWidth

      if (!intersectsLeftEdge && !intersectsRightEdge) {
        return
      }

      const leftSnapX = 0
      const rightSnapX = Math.max(0, viewportWidth - assistantWidth)

      if (intersectsLeftEdge && intersectsRightEdge) {
        const distanceToLeft = Math.abs(position.value.x - leftSnapX)
        const distanceToRight = Math.abs(position.value.x - rightSnapX)
        position.value.x = distanceToLeft <= distanceToRight ? leftSnapX : rightSnapX
        return
      }

      position.value.x = intersectsLeftEdge ? leftSnapX : rightSnapX
    }

    const wsUrl = computed(() => {
      const token = userStore.userInfo?.token
      if (import.meta.env.DEV) {
        return `/api/ws/chat${token ? `?token=${token}` : ''}`
      }
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      return `${protocol}//${window.location.host}/api/ws/chat${token ? `?token=${token}` : ''}`
    })

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const formatTime = (timestamp) =>
      new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
      })

    const appendSystemMessage = (content) => {
      messages.value.push({
        id: `system-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`,
        role: 'system',
        content,
        timestamp: new Date(),
      })
      scrollToBottom()
    }

    const upsertAssistantMessage = (id, content) => {
      const existing = messages.value.find((msg) => msg.id === id)
      if (existing) {
        existing.content = content
        existing.timestamp = new Date()
      } else {
        messages.value.push({
          id,
          role: 'assistant',
          content,
          timestamp: new Date(),
        })
      }
      scrollToBottom()
    }

    const handleMessage = (response) => {
      switch (response.type) {
        case 'system':
          if (response.status === 'connected') {
            appendSystemMessage(response.content || '连接成功')
          } else if (response.status === 'cleared') {
            messages.value = []
          }
          break
        case 'assistant':
          if (response.status === 'thinking') {
            isLoading.value = true
            currentMessageId.value = response.id || currentMessageId.value
          } else if (response.status === 'streaming' || response.status === 'message') {
            const assistantId = response.id || currentMessageId.value || `assistant-${Date.now()}`
            currentMessageId.value = assistantId
            upsertAssistantMessage(assistantId, response.content || '')
          } else if (response.status === 'completed') {
            const assistantId = response.id || currentMessageId.value || `assistant-${Date.now()}`
            if (response.content) {
              upsertAssistantMessage(assistantId, response.content)
            }
            currentMessageId.value = null
            isLoading.value = false
          }
          break
        case 'error':
          currentMessageId.value = null
          isLoading.value = false
          appendSystemMessage(response.content || 'AI 助手出现异常，请稍后重试')
          break
        default:
          break
      }
    }

    const startPing = () => {
      stopPing()
      pingTimer.value = setInterval(() => {
        if (ws.value?.readyState === WebSocket.OPEN) {
          ws.value.send(JSON.stringify({ type: 'ping' }))
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

    const cleanupSocket = () => {
      if (ws.value) {
        ws.value.onopen = null
        ws.value.onmessage = null
        ws.value.onerror = null
        ws.value.onclose = null
        ws.value.close()
        ws.value = null
      }
      stopPing()
      isConnected.value = false
      isLoading.value = false
      currentMessageId.value = null
    }

    const initWebSocket = () => {
      if (!isLoggedIn.value) return
      if (ws.value?.readyState === WebSocket.OPEN || ws.value?.readyState === WebSocket.CONNECTING) {
        return
      }

      try {
        ws.value = new WebSocket(wsUrl.value)

        ws.value.onopen = () => {
          isConnected.value = true
          startPing()
        }

        ws.value.onmessage = (event) => {
          try {
            handleMessage(JSON.parse(event.data))
          } catch (error) {
            console.error('解析 WebSocket 消息失败', error)
          }
        }

        ws.value.onerror = (error) => {
          console.error('WebSocket 错误', error)
          isConnected.value = false
        }

        ws.value.onclose = () => {
          isConnected.value = false
          stopPing()
          if (isLoggedIn.value) {
            scheduleReconnect()
          }
        }
      } catch (error) {
        console.error('创建 WebSocket 失败', error)
        scheduleReconnect()
      }
    }

    const sendMessage = () => {
      const content = inputMessage.value.trim()
      if (!content || !isConnected.value || isLoading.value || ws.value?.readyState !== WebSocket.OPEN) {
        return
      }

      const messageId = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
      messages.value.push({
        id: `user-${messageId}`,
        role: 'user',
        content,
        timestamp: new Date(),
      })
      scrollToBottom()

      inputMessage.value = ''
      isLoading.value = true
      currentMessageId.value = messageId

      try {
        ws.value.send(
          JSON.stringify({
            type: 'message',
            content,
            messageId,
          }),
        )
      } catch (error) {
        isLoading.value = false
        currentMessageId.value = null
        appendSystemMessage('发送失败，请稍后重试')
        console.error('发送消息失败', error)
      }
    }

    const clearMessages = () => {
      if (ws.value?.readyState === WebSocket.OPEN) {
        ws.value.send(JSON.stringify({ type: 'clear' }))
      } else {
        messages.value = []
      }
    }

    const expandAssistant = () => {
      if (isMinimized.value) {
        isMinimized.value = false
      }
    }

    const analyzeSelectedText = (text, type) => {
      expandAssistant()

      if (!text || !isConnected.value || isLoading.value) {
        return
      }

      const prompt =
        type === 'correctness'
          ? `请分析以下代码的正确性：\n\n${text}`
          : `请解释以下代码的含义：\n\n${text}`

      inputMessage.value = prompt
      sendMessage()
    }

    const startDrag = (event) => {
      if (isResizing.value || event.target.closest('.action-btn') || event.target.closest('.resize-handle')) {
        return
      }

      isDragging.value = true
      dragMoved.value = false
      suppressHeaderClick.value = false
      dragStartPoint.value = {
        x: event.clientX,
        y: event.clientY,
      }
      dragOffset.value = {
        x: event.clientX - position.value.x,
        y: event.clientY - position.value.y,
      }

      document.addEventListener('mousemove', onDrag)
      document.addEventListener('mouseup', stopDrag)
    }

    const onDrag = (event) => {
      if (!isDragging.value) {
        return
      }

      const deltaX = event.clientX - dragStartPoint.value.x
      const deltaY = event.clientY - dragStartPoint.value.y
      if (!dragMoved.value && Math.hypot(deltaX, deltaY) >= DRAG_THRESHOLD) {
        dragMoved.value = true
      }

      if (!dragMoved.value) {
        return
      }

      position.value = {
        x: event.clientX - dragOffset.value.x,
        y: event.clientY - dragOffset.value.y,
      }
    }

    const startResize = (event) => {
      if (isMinimized.value) {
        return
      }

      isResizing.value = true
      resizeStartPoint.value = {
        x: event.clientX,
        y: event.clientY,
      }
      resizeStartSize.value = {
        width: size.value.width,
        height: size.value.height,
      }

      document.addEventListener('mousemove', onResize)
      document.addEventListener('mouseup', stopResize)
    }

    const onResize = (event) => {
      if (!isResizing.value) {
        return
      }

      const viewportWidth = typeof window === 'undefined' ? resizeStartSize.value.width : window.innerWidth
      const viewportHeight = typeof window === 'undefined' ? resizeStartSize.value.height : window.innerHeight
      const maxWidth = Math.max(MIN_WIDTH, viewportWidth - position.value.x)
      const maxHeight = Math.max(MIN_HEIGHT, viewportHeight - position.value.y)

      const nextWidth = resizeStartSize.value.width + (event.clientX - resizeStartPoint.value.x)
      const nextHeight = resizeStartSize.value.height + (event.clientY - resizeStartPoint.value.y)

      size.value = {
        width: Math.min(maxWidth, Math.max(MIN_WIDTH, nextWidth)),
        height: Math.min(maxHeight, Math.max(MIN_HEIGHT, nextHeight)),
      }
    }

    const stopResize = () => {
      isResizing.value = false
      document.removeEventListener('mousemove', onResize)
      document.removeEventListener('mouseup', stopResize)
    }

    const stopDrag = () => {
      isDragging.value = false
      document.removeEventListener('mousemove', onDrag)
      document.removeEventListener('mouseup', stopDrag)

      if (dragMoved.value) {
        snapToWindowEdgeIfIntersecting()
        suppressHeaderClick.value = true
      }
      dragMoved.value = false
    }

    const handleHeaderClick = () => {
      if (dragMoved.value || suppressHeaderClick.value) {
        suppressHeaderClick.value = false
        return
      }

      if (isMinimized.value) {
        isMinimized.value = false
      }
    }

    const handleAssistantIconClick = () => {
      if (dragMoved.value || suppressHeaderClick.value || isDragging.value) {
        suppressHeaderClick.value = false
        return
      }
      isMinimized.value = !isMinimized.value
    }

    const toggleMinimize = () => {
      isMinimized.value = !isMinimized.value
    }

    onMounted(() => {
      watch(
        isLoggedIn,
        (loggedIn) => {
          if (loggedIn) {
            initWebSocket()
          } else {
            cleanupSocket()
            messages.value = []
          }
        },
        { immediate: true },
      )
    })

    onUnmounted(() => {
      cleanupSocket()
      if (reconnectTimer.value) {
        clearTimeout(reconnectTimer.value)
      }
      document.removeEventListener('mousemove', onDrag)
      document.removeEventListener('mouseup', stopDrag)
      document.removeEventListener('mousemove', onResize)
      document.removeEventListener('mouseup', stopResize)
    })

    return {
      analyzeSelectedText,
      clearMessages,
      expandAssistant,
      formatTime,
      handleAssistantIconClick,
      handleHeaderClick,
      inputMessage,
      isConnected,
      isDragging,
      isLoading,
      isLoggedIn,
      isMinimized,
      isResizing,
      messages,
      messagesContainer,
      positionStyle,
      sendMessage,
      startDrag,
      startResize,
      toggleMinimize,
    }
  },
}
</script>

<style scoped>
.floating-ai-assistant {
  position: fixed;
  top: 0;
  left: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.18s ease, box-shadow 0.2s ease, width 0.3s ease, height 0.3s ease;
  z-index: 9999;
  will-change: transform;
  min-width: 300px;
  min-height: 320px;
}

.floating-ai-assistant.dragging,
.floating-ai-assistant.resizing {
  transition: none;
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
  gap: 12px;
}

.assistant-icon {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon {
  font-size: 14px;
  font-weight: 700;
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
  flex: 1;
  font-size: 14px;
  font-weight: 600;
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
  padding: 4px 6px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.action-btn:hover {
  background: rgba(0, 0, 0, 0.06);
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 160px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}

.message {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 18px;
}

.message.user {
  align-self: flex-end;
  background: #007bff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message.assistant {
  align-self: flex-start;
  background: #fff;
  color: #333;
  border: 1px solid #e9ecef;
  border-bottom-left-radius: 4px;
}

.message.system {
  align-self: center;
  width: 80px;
  height: 50px;
  background: #e9ecef;
  color: #6c757d;
  text-align: center;
  border-radius: 12px;
  font-size: 12px;
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.message.system .message-content {
  word-break: break-word;
  line-height: 1.4;
  text-align: center;
}

.message.system .message-time {
  margin-top: 4px;
  font-size: 10px;
  opacity: 0.7;
  text-align: center;
}

.message-content {
  word-break: break-word;
  line-height: 1.4;
}

.message-time {
  margin-top: 4px;
  font-size: 10px;
  opacity: 0.7;
  text-align: right;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6c757d;
  font-size: 14px;
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
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.input-container {
  display: flex;
  padding: 12px 16px;
  border-top: 1px solid #e9ecef;
  background: #fff;
  height: 83px;
}

.message-input {
  flex: 1;
  min-height: 40px;
  max-height: 120px;
  border: 1px solid #ced4da;
  border-radius: 20px;
  padding: 10px 16px;
  resize: none;
  font-size: 14px;
  line-height: 1.4;
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
  margin-left: 8px;
  padding: 0 16px;
  border: none;
  border-radius: 20px;
  background: #007bff;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background 0.2s ease;
  height: 55px;
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

.floating-ai-assistant.minimized {
  border-radius: 999px;
  min-width: 60px;
  min-height: 60px;
}

.floating-ai-assistant.minimized .assistant-header {
  height: 100%;
  padding: 0;
  border-bottom: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.floating-ai-assistant.minimized .header-content {
  justify-content: center;
}

.floating-ai-assistant.minimized .assistant-title,
.floating-ai-assistant.minimized .header-actions,
.floating-ai-assistant.minimized .messages-container,
.floating-ai-assistant.minimized .input-container,
.floating-ai-assistant.minimized .resize-handle {
  display: none;
}

.resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 18px;
  height: 18px;
  border: none;
  padding: 0;
  background:
    linear-gradient(135deg, transparent 0 38%, rgba(0, 0, 0, 0.24) 38% 46%, transparent 46% 58%, rgba(0, 0, 0, 0.24) 58% 66%, transparent 66%);
  cursor: nwse-resize;
}

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
