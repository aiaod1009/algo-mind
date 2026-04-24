<template>
  <div
    v-if="isLoggedIn"
    class="floating-ai-assistant"
    :class="{ minimized: isMinimized, dragging: isDragging, resizing: isResizing }"
    :style="positionStyle"
  >
    <div class="assistant-header" @mousedown.left.prevent="startDrag" @click="handleHeaderClick">
      <div class="header-topbar">
        <button
          v-if="isMinimized"
          class="minimized-toggle assistant-icon"
          type="button"
          title="展开助手"
          @click.stop="handleAssistantIconClick"
        >
          <span class="icon">AI</span>
          <span class="status-indicator" :class="{ connected: isConnected }"></span>
        </button>

        <button
          v-else
          class="chat-pill assistant-icon"
          type="button"
          title="收起助手"
          @click.stop="handleAssistantIconClick"
        >
          <span class="chat-pill-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <path
                d="M5.2 17.8A7.8 7.8 0 0 1 4 13.6C4 8.9 7.9 5 12.7 5h.6C18 5 22 8.9 22 13.6s-4 8.6-8.7 8.6H9.9L5 21l.2-3.2Z"
                fill="currentColor"
              />
            </svg>
          </span>
          <span class="chat-pill-text">聊天</span>
          <span class="status-indicator" :class="{ connected: isConnected }"></span>
        </button>

        <div class="header-actions">
          <button
            class="action-btn action-btn--collapse"
            type="button"
            :title="isMinimized ? '展开助手' : '最小化助手'"
            @click.stop="toggleMinimize"
          >
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M7 10l5 5 5-5" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2.4" />
            </svg>
          </button>
        </div>
      </div>

      <div class="header-profile">
        <div class="profile-avatars">
          <span class="profile-avatar profile-avatar--visitor" aria-hidden="true">
            <svg viewBox="0 0 24 24" focusable="false">
              <circle cx="12" cy="8" r="4.2" fill="currentColor" />
              <path d="M4.8 20c.9-4 4-6 7.2-6s6.3 2 7.2 6" fill="currentColor" />
            </svg>
          </span>
          <span class="profile-avatar profile-avatar--assistant" aria-hidden="true">AI</span>
        </div>

        <div class="profile-copy">
          <p class="profile-title">有疑问吗？联系我们！</p>
          <p class="profile-subtitle">
            <span class="profile-clock" aria-hidden="true">
              <svg viewBox="0 0 24 24" focusable="false">
                <circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="2" />
                <path d="M12 7.8v4.8l3.4 2" fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" />
              </svg>
            </span>
            上次活动 {{ lastActiveLabel }}
          </p>
        </div>
      </div>
    </div>

    <div v-if="!isMinimized" ref="messagesContainer" class="messages-container">
      <div v-for="msg in messages" :key="msg.id" class="message-row" :class="`is-${msg.role}`">
        <div v-if="msg.role !== 'user' && msg.role !== 'system'" class="message-avatar message-avatar--assistant" aria-hidden="true">
          AI
        </div>

        <div class="message" :class="msg.role">
          <div class="message-content">{{ msg.content }}</div>
          <!-- 文件附件展示 -->
          <div v-if="msg.files && msg.files.length > 0" class="message-files">
            <div v-for="(file, fileIndex) in msg.files" :key="fileIndex" class="file-attachment">
              <!-- 图片预览 -->
              <div v-if="file.isImage" class="image-preview">
                <img :src="file.url" :alt="file.name" />
              </div>
              <!-- 文件卡片 -->
              <div v-else class="file-card">
                <span class="file-icon">{{ getFileIcon(file.type) }}</span>
                <div class="file-info">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
        </div>

        <div v-if="msg.role === 'user'" class="message-avatar message-avatar--user" aria-hidden="true">
          我
        </div>
      </div>

      <div v-if="isLoading" class="loading-message">
        <div class="message-avatar message-avatar--assistant" aria-hidden="true">AI</div>
        <div class="loading-bubble">
          <span class="loading-spinner"></span>
          <span>AI 正在思考...</span>
        </div>
      </div>
    </div>

    <div v-if="!isMinimized" class="input-container">
      <div class="composer-shell">
        <textarea
          v-model="inputMessage"
          class="message-input"
          placeholder="输入你的信息..."
          @keyup.enter.exact.prevent="sendMessage"
          @keyup.enter.shift="inputMessage += '\n'"
          :disabled="isLoading"
        ></textarea>

        <!-- 已选文件预览区域 -->
        <div v-if="selectedFiles.length > 0" class="selected-files-area">
          <div class="selected-files-header">
            <span class="selected-files-title">已选择 {{ selectedFiles.length }} 个文件</span>
            <button class="clear-files-btn" type="button" @click="clearAllFiles">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <div class="selected-files-list">
            <div v-for="(file, index) in selectedFiles" :key="index" class="selected-file-item">
              <!-- 图片缩略图 -->
              <div v-if="file.previewUrl" class="selected-file-thumb">
                <img :src="file.previewUrl" :alt="file.name" />
              </div>
              <!-- 文件图标 -->
              <div v-else class="selected-file-icon">{{ file.icon }}</div>
              <div class="selected-file-info">
                <span class="selected-file-name">{{ file.name }}</span>
                <span class="selected-file-size">{{ formatFileSize(file.size) }}</span>
              </div>
              <button class="remove-file-btn" type="button" @click="removeFile(index)">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="composer-toolbar">
          <button class="toolbar-btn" type="button" title="表情功能暂未开放" disabled>
            <svg viewBox="0 0 24 24" focusable="false">
              <circle cx="12" cy="12" r="8.5" fill="none" stroke="currentColor" stroke-width="2" />
              <circle cx="9" cy="10" r="1" fill="currentColor" />
              <circle cx="15" cy="10" r="1" fill="currentColor" />
              <path d="M8.5 14.2c.8 1.2 2.1 1.8 3.5 1.8s2.7-.6 3.5-1.8" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
            </svg>
          </button>

          <!-- 文件上传按钮 -->
          <input
            ref="fileInputRef"
            type="file"
            multiple
            accept="image/*,.txt,.md,.csv,.json,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip"
            style="display: none"
            @change="handleFileSelect"
          />
          <button
            class="toolbar-btn"
            type="button"
            title="上传文件"
            :disabled="isLoading || isUploading || selectedFiles.length >= 5"
            @click="triggerFileInput"
          >
            <svg viewBox="0 0 24 24" focusable="false">
              <path
                d="M9 12.8l5.4-5.4a3 3 0 1 1 4.2 4.2l-7.3 7.3a4.5 4.5 0 0 1-6.4-6.4l7.3-7.3"
                fill="none"
                stroke="currentColor"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
              />
            </svg>
          </button>

          <button class="toolbar-btn" type="button" title="清空消息" @click="clearMessages">
            <svg viewBox="0 0 24 24" focusable="false">
              <path d="M5 14v-4" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
              <path d="M9 17V7" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
              <path d="M13 19V5" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
              <path d="M17 16V8" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
              <path d="M21 13v-2" fill="none" stroke="currentColor" stroke-linecap="round" stroke-width="2" />
            </svg>
          </button>
        </div>

        <button
          class="send-btn"
          type="button"
          @click="sendMessage"
          :disabled="!isConnected || isLoading || isUploading || (!inputMessage.trim() && selectedFiles.length === 0)"
          :class="{ loading: isLoading || isUploading }"
          :title="isUploading ? '上传中' : (isLoading ? '发送中' : '发送消息')"
        >
          <svg viewBox="0 0 24 24" focusable="false">
            <path d="M4 19 21 12 4 5l3.9 7L4 19Z" fill="currentColor" />
          </svg>
        </button>
      </div>

      <div class="assistant-brand">
        <span>Powered by</span>
        <strong>AlgoMind AI</strong>
      </div>
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
import { ElMessage } from 'element-plus'
import api from '../api'

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

    // 文件上传相关
    const fileInputRef = ref(null)
    const selectedFiles = ref([])
    const isUploading = ref(false)
    const MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
    const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
    const ALLOWED_FILE_TYPES = [
      ...ALLOWED_IMAGE_TYPES,
      'text/plain',
      'text/markdown',
      'text/csv',
      'application/json',
      'application/pdf',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-powerpoint',
      'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      'application/zip',
      'application/x-zip-compressed'
    ]
    const viewport = ref({
      width: typeof window === 'undefined' ? 1440 : window.innerWidth,
      height: typeof window === 'undefined' ? 900 : window.innerHeight,
    })
    const position = ref({ x: 24, y: 24 })
    const size = ref({ width: 400, height: 720 })
    const dragOffset = ref({ x: 0, y: 0 })
    const dragStartPoint = ref({ x: 0, y: 0 })
    const dragMoved = ref(false)
    const suppressHeaderClick = ref(false)
    const resizeStartPoint = ref({ x: 0, y: 0 })
    const resizeStartSize = ref({ width: 400, height: 720 })
    const DRAG_THRESHOLD = 3
    const DEFAULT_WIDTH = 400
    const DEFAULT_HEIGHT = 720
    const MIN_WIDTH = 320
    const MIN_HEIGHT = 520
    const HARD_MIN_WIDTH = 280
    const HARD_MIN_HEIGHT = 360
    const MINIMIZED_WIDTH = 60
    const MINIMIZED_HEIGHT = 60
    const VIEWPORT_GAP = 20

    const isLoggedIn = computed(() => !!userStore.userInfo?.token)

    const expandedDimensions = computed(() => {
      const maxWidth = Math.max(HARD_MIN_WIDTH, viewport.value.width - VIEWPORT_GAP)
      const maxHeight = Math.max(HARD_MIN_HEIGHT, viewport.value.height - VIEWPORT_GAP)

      return {
        width: Math.min(size.value.width, maxWidth),
        height: Math.min(size.value.height, maxHeight),
      }
    })

    const positionStyle = computed(() => ({
      transform: `translate3d(${position.value.x}px, ${position.value.y}px, 0)`,
      width: `${isMinimized.value ? MINIMIZED_WIDTH : expandedDimensions.value.width}px`,
      height: `${isMinimized.value ? MINIMIZED_HEIGHT : expandedDimensions.value.height}px`,
      zIndex: 9999,
    }))

    const lastActiveLabel = computed(() => {
      const latestTimestamp = messages.value[messages.value.length - 1]?.timestamp
      const timestamp = latestTimestamp || new Date()
      return new Date(timestamp).toLocaleDateString('zh-CN')
    })

    const getAssistantWidth = () => (isMinimized.value ? MINIMIZED_WIDTH : expandedDimensions.value.width)
    const getAssistantHeight = () => (isMinimized.value ? MINIMIZED_HEIGHT : expandedDimensions.value.height)

    const clampPositionToViewport = () => {
      const assistantWidth = getAssistantWidth()
      const assistantHeight = getAssistantHeight()
      const maxX = Math.max(0, viewport.value.width - assistantWidth)
      const maxY = Math.max(0, viewport.value.height - assistantHeight)

      position.value = {
        x: Math.min(maxX, Math.max(0, position.value.x)),
        y: Math.min(maxY, Math.max(0, position.value.y)),
      }
    }

    const updateViewport = () => {
      if (typeof window === 'undefined') {
        return
      }

      viewport.value = {
        width: window.innerWidth,
        height: window.innerHeight,
      }

      clampPositionToViewport()
    }

    const snapToWindowEdgeIfIntersecting = () => {
      const assistantWidth = getAssistantWidth()
      const assistantHeight = getAssistantHeight()
      const intersectsLeftEdge = position.value.x < 0
      const intersectsRightEdge = position.value.x + assistantWidth > viewport.value.width
      const intersectsTopEdge = position.value.y < 0
      const intersectsBottomEdge = position.value.y + assistantHeight > viewport.value.height

      if (!intersectsLeftEdge && !intersectsRightEdge && !intersectsTopEdge && !intersectsBottomEdge) {
        return
      }

      clampPositionToViewport()
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

    // 构建对话历史，用于上下文理解
    const buildChatHistory = () => {
      // 只保留最近10轮对话（20条消息），避免超出token限制
      const historyMessages = messages.value
        .filter((msg) => msg.role === 'user' || msg.role === 'assistant')
        .slice(-20)
        .map((msg) => ({
          role: msg.role,
          content: msg.content || '',
        }))
      return historyMessages
    }

    const sendMessage = async () => {
      const content = inputMessage.value.trim()
      const hasFiles = selectedFiles.value.length > 0

      // 如果没有文字且没有文件，不发送
      if ((!content && !hasFiles) || !isConnected.value || isLoading.value || ws.value?.readyState !== WebSocket.OPEN) {
        return
      }

      // 先上传文件
      let uploadedFiles = []
      if (hasFiles) {
        uploadedFiles = await uploadFiles()
        if (selectedFiles.value.length > 0 && uploadedFiles.length === 0) {
          // 上传失败，不继续发送消息
          return
        }
      }

      const messageId = `msg-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
      const userMessageContent = content || (hasFiles ? '请分析我上传的文件' : '')

      // 获取历史对话上下文
      const historyMessages = buildChatHistory()

      messages.value.push({
        id: `user-${messageId}`,
        role: 'user',
        content: userMessageContent,
        files: uploadedFiles,
        timestamp: new Date(),
      })
      scrollToBottom()

      inputMessage.value = ''
      clearAllFiles()
      isLoading.value = true
      currentMessageId.value = messageId

      try {
        ws.value.send(
          JSON.stringify({
            type: 'message',
            content: userMessageContent,
            messageId,
            files: uploadedFiles.length > 0 ? uploadedFiles : undefined,
            messages: historyMessages, // 传递历史对话上下文
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
        clampPositionToViewport()
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
      if (isResizing.value || event.target.closest('.action-btn') || event.target.closest('.toolbar-btn')) {
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
        width: expandedDimensions.value.width,
        height: expandedDimensions.value.height,
      }

      document.addEventListener('mousemove', onResize)
      document.addEventListener('mouseup', stopResize)
    }

    const onResize = (event) => {
      if (!isResizing.value) {
        return
      }

      const maxWidth = Math.max(HARD_MIN_WIDTH, viewport.value.width - position.value.x)
      const maxHeight = Math.max(HARD_MIN_HEIGHT, viewport.value.height - position.value.y)
      const nextWidth = resizeStartSize.value.width + (event.clientX - resizeStartPoint.value.x)
      const nextHeight = resizeStartSize.value.height + (event.clientY - resizeStartPoint.value.y)

      size.value = {
        width: Math.min(maxWidth, Math.max(Math.min(MIN_WIDTH, maxWidth), nextWidth)),
        height: Math.min(maxHeight, Math.max(Math.min(MIN_HEIGHT, maxHeight), nextHeight)),
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
        clampPositionToViewport()
      }
    }

    const handleAssistantIconClick = () => {
      if (dragMoved.value || suppressHeaderClick.value || isDragging.value) {
        suppressHeaderClick.value = false
        return
      }
      isMinimized.value = !isMinimized.value
      clampPositionToViewport()
    }

    const toggleMinimize = () => {
      isMinimized.value = !isMinimized.value
      clampPositionToViewport()
    }

    // 文件上传相关函数
    const isImageFile = (file) => ALLOWED_IMAGE_TYPES.includes(file.type)

    const getFileIcon = (fileType) => {
      if (ALLOWED_IMAGE_TYPES.includes(fileType)) return '🖼️'
      if (fileType.includes('pdf')) return '📄'
      if (fileType.includes('word') || fileType.includes('document')) return '📝'
      if (fileType.includes('excel') || fileType.includes('sheet')) return '📊'
      if (fileType.includes('powerpoint') || fileType.includes('presentation')) return '📽️'
      if (fileType.includes('zip') || fileType.includes('compressed')) return '📦'
      if (fileType.includes('json')) return '🔧'
      if (fileType.includes('csv')) return '📈'
      if (fileType.includes('text') || fileType.includes('markdown')) return '📃'
      return '📎'
    }

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const triggerFileInput = () => {
      fileInputRef.value?.click()
    }

    const handleFileSelect = (event) => {
      const files = Array.from(event.target.files || [])
      validateAndAddFiles(files)
      // 重置input，允许重复选择相同文件
      event.target.value = ''
    }

    const validateAndAddFiles = (files) => {
      for (const file of files) {
        // 检查文件大小
        if (file.size > MAX_FILE_SIZE) {
          ElMessage.warning(`文件 "${file.name}" 超过10MB限制`)
          continue
        }

        // 检查文件类型
        if (!ALLOWED_FILE_TYPES.includes(file.type)) {
          ElMessage.warning(`不支持的文件类型: ${file.name}`)
          continue
        }

        // 检查是否已存在
        const exists = selectedFiles.value.some(f => f.name === file.name && f.size === file.size)
        if (exists) {
          ElMessage.warning(`文件 "${file.name}" 已添加`)
          continue
        }

        // 限制最多5个文件
        if (selectedFiles.value.length >= 5) {
          ElMessage.warning('最多只能上传5个文件')
          break
        }

        // 创建预览URL
        const fileWithPreview = {
          file,
          name: file.name,
          size: file.size,
          type: file.type,
          previewUrl: isImageFile(file) ? URL.createObjectURL(file) : null,
          icon: getFileIcon(file.type)
        }

        selectedFiles.value.push(fileWithPreview)
      }
    }

    const removeFile = (index) => {
      const file = selectedFiles.value[index]
      if (file.previewUrl) {
        URL.revokeObjectURL(file.previewUrl)
      }
      selectedFiles.value.splice(index, 1)
    }

    const clearAllFiles = () => {
      selectedFiles.value.forEach(file => {
        if (file.previewUrl) {
          URL.revokeObjectURL(file.previewUrl)
        }
      })
      selectedFiles.value = []
    }

    // 上传文件到服务器
    const uploadFiles = async () => {
      if (selectedFiles.value.length === 0) return []

      isUploading.value = true
      const uploadedFiles = []

      try {
        for (const fileInfo of selectedFiles.value) {
          const formData = new FormData()
          formData.append('file', fileInfo.file)

          // 调用实际上传API
          const response = await api.uploadChatFile(formData)

          if (response.data?.code === 0 && response.data?.data) {
            uploadedFiles.push({
              name: fileInfo.name,
              size: fileInfo.size,
              type: fileInfo.type,
              url: response.data.data.url,
              isImage: isImageFile(fileInfo.file)
            })
          } else {
            throw new Error(response.data?.message || '上传失败')
          }
        }
      } catch (error) {
        ElMessage.error('文件上传失败，请重试')
        console.error('File upload error:', error)
        return []
      } finally {
        isUploading.value = false
      }

      return uploadedFiles
    }

    onMounted(() => {
      updateViewport()
      window.addEventListener('resize', updateViewport)

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
      window.removeEventListener('resize', updateViewport)
      document.removeEventListener('mousemove', onDrag)
      document.removeEventListener('mouseup', stopDrag)
      document.removeEventListener('mousemove', onResize)
      document.removeEventListener('mouseup', stopResize)
    })

    return {
      analyzeSelectedText,
      clearMessages,
      clearAllFiles,
      expandAssistant,
      fileInputRef,
      formatFileSize,
      formatTime,
      getFileIcon,
      handleAssistantIconClick,
      handleFileSelect,
      handleHeaderClick,
      inputMessage,
      isConnected,
      isDragging,
      isLoading,
      isLoggedIn,
      isMinimized,
      isResizing,
      isUploading,
      lastActiveLabel,
      messages,
      messagesContainer,
      positionStyle,
      removeFile,
      selectedFiles,
      sendMessage,
      startDrag,
      startResize,
      toggleMinimize,
      triggerFileInput,
    }
  },
}
</script>

<style scoped>
.floating-ai-assistant {
  --assistant-yellow: #f0b800;
  --assistant-yellow-deep: #c78f00;
  --assistant-yellow-soft: #ffd85c;
  --assistant-ink: #34425c;
  --assistant-muted: #7b879b;
  --assistant-surface: #ffffff;
  --assistant-border: #f2c542;
  --assistant-shadow: 0 18px 42px rgba(122, 88, 0, 0.22);
  position: fixed;
  top: 0;
  left: 0;
  background: var(--assistant-surface);
  border-radius: 24px;
  box-shadow: var(--assistant-shadow);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.18s ease, box-shadow 0.24s ease, width 0.3s ease, height 0.3s ease;
  z-index: 9999;
  will-change: transform;
  min-width: 280px;
  min-height: 360px;
  border: 1px solid rgba(240, 184, 0, 0.28);
}

.floating-ai-assistant:not(.minimized):hover {
  box-shadow: 0 24px 52px rgba(122, 88, 0, 0.28);
}

.floating-ai-assistant.dragging,
.floating-ai-assistant.resizing {
  transition: none;
}

.assistant-header {
  position: relative;
  padding: 8px 14px 12px;
  background:
    linear-gradient(rgba(170, 118, 0, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(170, 118, 0, 0.08) 1px, transparent 1px),
    linear-gradient(180deg, #f2bf05 0%, #e5ad00 100%);
  background-size: 16px 16px, 16px 16px, 100% 100%;
  color: #fff;
  cursor: move;
  user-select: none;
  min-height: 140px;
}

.assistant-header::after {
  content: '';
  position: absolute;
  inset: auto 0 0 0;
  height: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0), rgba(198, 142, 0, 0.12));
}

.header-topbar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.chat-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 2px solid rgba(150, 105, 0, 0.42);
  background: linear-gradient(180deg, #f4c52f 0%, #d6a009 100%);
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.28);
  cursor: pointer;
  transition: transform 0.18s ease, filter 0.18s ease;
}

.chat-pill:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.chat-pill:active {
  transform: translateY(0);
}

.chat-pill-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
}

.chat-pill-icon svg {
  width: 16px;
  height: 16px;
}

.chat-pill-text {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.icon {
  font-size: 14px;
  font-weight: 700;
}

.header-actions {
  position: absolute;
  top: 0;
  right: 0;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.action-btn:active {
  transform: translateY(0);
}

.action-btn svg {
  width: 18px;
  height: 18px;
}

.action-btn--collapse svg {
  transition: transform 0.2s ease;
}

.header-profile {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
  text-align: center;
}

.profile-avatars {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
}

.profile-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 18px rgba(103, 73, 0, 0.14);
}

.profile-avatar--visitor {
  margin-right: -8px;
  background: linear-gradient(180deg, #ffffff 0%, #eef1f6 100%);
  color: #8a90a3;
}

.profile-avatar--visitor svg {
  width: 26px;
  height: 26px;
}

.profile-avatar--assistant {
  margin-left: -8px;
  background: radial-gradient(circle at 35% 30%, #eaf4ff 0%, #c9d8f8 45%, #8898be 100%);
  color: #40506a;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.profile-copy {
  display: grid;
  gap: 4px;
}

.profile-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0.01em;
}

.profile-subtitle {
  margin: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.92);
}

.profile-clock {
  display: inline-flex;
  width: 15px;
  height: 15px;
}

.profile-clock svg {
  width: 15px;
  height: 15px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.14);
  transition: background 0.3s ease;
}

.status-indicator.connected {
  background: #75f08b;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px 16px 12px;
  background: linear-gradient(180deg, #fff 0%, #fffdf6 100%);
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-row.is-user {
  justify-content: flex-end;
}

.message-row.is-system {
  justify-content: center;
}

.message-avatar {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 800;
}

.message-avatar--assistant {
  background: radial-gradient(circle at 35% 30%, #f2f7ff 0%, #cbd7f3 52%, #95a4c6 100%);
  color: #425371;
}

.message-avatar--user {
  background: linear-gradient(180deg, #f6c83e 0%, #daa100 100%);
  color: #fff;
}

.message {
  max-width: calc(100% - 48px);
  padding: 14px 16px 12px;
  border-radius: 16px;
  box-shadow: 0 8px 18px rgba(42, 57, 90, 0.07);
}

.message.user {
  align-self: flex-end;
  background: linear-gradient(180deg, #f4c52f 0%, #e2ad06 100%);
  color: #fff;
  border-bottom-right-radius: 6px;
}

.message.assistant {
  align-self: flex-start;
  background: #eef2f8;
  color: var(--assistant-ink);
  border-bottom-left-radius: 6px;
}

.message.system {
  max-width: 50%;
  background: rgba(240, 184, 0, 0.12);
  color: #7d6513;
  text-align: center;
  border: 1px solid rgba(240, 184, 0, 0.22);
  box-shadow: none;
  padding: 6px 20px 4px;
  border-radius: 999px;
}

.message.system .message-content {
  font-size: 11px;
  line-height: 1.4;
}

.message.system .message-time {
  font-size: 9px;
  margin-top: 2px;
}

.message-content {
  word-break: break-word;
  line-height: 1.6;
  font-size: 14px;
}

.message-time {
  margin-top: 6px;
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
}

.message.system .message-time {
  text-align: center;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 10px;
}

.loading-bubble {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 16px;
  background: #eef2f8;
  color: var(--assistant-muted);
  box-shadow: 0 8px 18px rgba(42, 57, 90, 0.07);
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(240, 184, 0, 0.22);
  border-top-color: var(--assistant-yellow-deep);
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
  flex-direction: column;
  gap: 8px;
  padding: 10px 10px 8px;
  background: #fff;
}

.composer-shell {
  position: relative;
  display: flex;
  flex-direction: column;
  border: 2px solid var(--assistant-border);
  border-radius: 22px;
  background: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 226, 135, 0.28);
}

.composer-shell:focus-within {
  border-color: var(--assistant-yellow-deep);
  box-shadow: 0 0 0 4px rgba(240, 184, 0, 0.12);
}

.message-input {
  width: 100%;
  min-height: 76px;
  max-height: 144px;
  border: none;
  padding: 16px 56px 10px 16px;
  resize: none;
  font-size: 16px;
  line-height: 1.5;
  outline: none;
  background: transparent;
  color: var(--assistant-ink);
}

.message-input::placeholder {
  color: #8e98a9;
}

.message-input:disabled {
  cursor: not-allowed;
  background: transparent;
}

.composer-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px 10px;
  color: #74839a;
}

.toolbar-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: inherit;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.toolbar-btn svg {
  width: 20px;
  height: 20px;
}

.toolbar-btn:hover:not(:disabled) {
  background: rgba(240, 184, 0, 0.12);
  color: #5f6f89;
  transform: translateY(-1px);
}

.toolbar-btn:disabled {
  cursor: not-allowed;
  opacity: 0.74;
}

/* 已选文件区域 */
.selected-files-area {
  background: rgba(255, 248, 230, 0.7);
  border: 1px solid rgba(240, 184, 0, 0.2);
  border-radius: 12px;
  padding: 10px 12px;
  margin: 0 12px 10px;
}

.selected-files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.selected-files-title {
  font-size: 12px;
  font-weight: 600;
  color: #7d6513;
}

.clear-files-btn {
  width: 22px;
  height: 22px;
  background: rgba(239, 68, 68, 0.1);
  border: none;
  border-radius: 6px;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-files-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

.selected-files-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.selected-file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  border: 1px solid rgba(240, 184, 0, 0.15);
}

.selected-file-thumb {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.selected-file-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected-file-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  background: rgba(240, 184, 0, 0.1);
  border-radius: 6px;
  flex-shrink: 0;
}

.selected-file-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.selected-file-name {
  font-size: 12px;
  font-weight: 500;
  color: #4a4a4a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selected-file-size {
  font-size: 10px;
  color: #888;
}

.remove-file-btn {
  width: 20px;
  height: 20px;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.remove-file-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* 消息中的文件展示 */
.message-files {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 8px;
  margin-bottom: 4px;
}

.file-attachment {
  display: flex;
  flex-direction: column;
}

.image-preview {
  max-width: 180px;
  border-radius: 8px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 8px;
}

.file-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 220px;
}

.file-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.file-name {
  font-size: 12px;
  font-weight: 500;
  color: #4a4a4a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 10px;
  color: #888;
}

.send-btn {
  position: absolute;
  right: 10px;
  bottom: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #a4afc0;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.send-btn svg {
  width: 26px;
  height: 26px;
}

.send-btn:hover:not(:disabled) {
  color: var(--assistant-yellow-deep);
  transform: translateX(1px);
}

.send-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.send-btn.loading {
  color: var(--assistant-yellow-deep);
}

.assistant-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding-bottom: 2px;
  font-size: 12px;
  color: #9ba3b1;
}

.assistant-brand strong {
  color: #768297;
  font-weight: 700;
}

.floating-ai-assistant.minimized {
  border-radius: 50%;
  width: 60px !important;
  height: 60px !important;
  min-width: 60px;
  min-height: 60px;
  background: #fff;
  border: none;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.floating-ai-assistant.minimized .assistant-header {
  height: 100%;
  padding: 0;
  min-height: 60px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-ai-assistant.minimized .assistant-header::after,
.floating-ai-assistant.minimized .header-profile,
.floating-ai-assistant.minimized .header-actions,
.floating-ai-assistant.minimized .messages-container,
.floating-ai-assistant.minimized .input-container,
.floating-ai-assistant.minimized .resize-handle {
  display: none;
}

.floating-ai-assistant.minimized .header-topbar {
  width: 100%;
  justify-content: center;
}

.floating-ai-assistant.minimized .minimized-toggle {
  gap: 0;
  padding: 0;
  border: none;
  background: transparent;
  color: #1a1a1a;
  box-shadow: none;
  font-size: 18px;
  font-weight: 700;
}

.floating-ai-assistant.minimized .minimized-toggle:hover {
  transform: none;
  filter: none;
}

.floating-ai-assistant.minimized .status-indicator {
  background: #dc3545;
  box-shadow: none;
}

.floating-ai-assistant.minimized .status-indicator.connected {
  background: #28a745;
}

.resize-handle {
  position: absolute;
  right: 8px;
  bottom: 34px;
  width: 18px;
  height: 18px;
  border: none;
  padding: 0;
  opacity: 0;
  background:
    linear-gradient(135deg, transparent 0 34%, rgba(240, 184, 0, 0.62) 34% 42%, transparent 42% 56%, rgba(240, 184, 0, 0.62) 56% 64%, transparent 64%);
  cursor: nwse-resize;
  transition: opacity 0.2s ease;
}

.floating-ai-assistant:hover .resize-handle,
.floating-ai-assistant.resizing .resize-handle {
  opacity: 1;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: rgba(255, 232, 166, 0.18);
  border-radius: 999px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: rgba(226, 173, 6, 0.36);
  border-radius: 999px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: rgba(199, 143, 0, 0.54);
}

@media (max-width: 768px) {
  .floating-ai-assistant {
    border-radius: 22px;
  }

  .assistant-header {
    min-height: 156px;
    padding: 10px 12px 16px;
  }

  .profile-title {
    font-size: 22px;
  }

  .profile-subtitle {
    font-size: 14px;
  }

  .message-input {
    min-height: 70px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .assistant-header {
    min-height: 146px;
  }

  .chat-pill {
    padding: 7px 14px;
  }

  .chat-pill-text {
    font-size: 15px;
  }

  .profile-avatar {
    width: 44px;
    height: 44px;
  }

  .profile-title {
    font-size: 18px;
  }

  .messages-container {
    padding: 14px 12px 10px;
  }

  .message {
    max-width: calc(100% - 42px);
    padding: 12px 14px 10px;
  }

  .input-container {
    padding: 8px 8px 6px;
  }
}
</style>
