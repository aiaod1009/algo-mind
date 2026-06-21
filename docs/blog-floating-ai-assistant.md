# 悬浮窗 AI 助手：WebSocket 实时通信与可拖拽浮窗的完整实现

> 一个类似 Intercom / Drift 的悬浮式 AI 聊天助手，支持 WebSocket 实时对话、流式输出、文件上传、可拖拽可缩放。

---

## 一、功能概述

右下角的悬浮 AI 助手，核心能力：

- **实时对话**：WebSocket 双向通信，AI 流式回复
- **可拖拽**：按住标题栏拖动，松手自动吸附窗口边缘
- **可缩放**：右下角拖拽调整大小，有最小尺寸限制
- **最小化**：收起成圆形图标，带连接状态指示灯
- **文件上传**：支持图片、PDF、Office 等，最多 5 个
- **上下文感知**：自动携带对话历史和用户画像

---

## 二、WebSocket 连接管理

### 建立连接

```javascript
const wsUrl = computed(() => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/api/ws/chat?token=${token}`
})

const initWebSocket = () => {
  ws.value = new WebSocket(wsUrl.value)

  ws.value.onopen = () => {
    isConnected.value = true
    startPing()  // 启动心跳
  }

  ws.value.onmessage = (event) => {
    handleMessage(JSON.parse(event.data))
  }

  ws.value.onclose = () => {
    isConnected.value = false
    stopPing()
    scheduleReconnect()  // 自动重连
  }
}
```

### 心跳保活

每 30 秒发一次 `ping`，防止连接被服务端超时断开：

```javascript
const startPing = () => {
  pingTimer.value = setInterval(() => {
    if (ws.value?.readyState === WebSocket.OPEN) {
      ws.value.send(JSON.stringify({ type: 'ping' }))
    }
  }, 30000)
}
```

### 自动重连

连接断开后 5 秒自动重连，登录状态变化时也会触发：

```javascript
const scheduleReconnect = () => {
  reconnectTimer.value = setTimeout(() => {
    initWebSocket()
  }, 5000)
}

// 监听登录状态
watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    initWebSocket()
  } else {
    cleanupSocket()
    messages.value = []
  }
}, { immediate: true })
```

---

## 三、流式消息处理

### 消息协议

服务端通过 WebSocket 推送 JSON，用 `type` + `status` 区分状态：

```json
// 思考中
{ "type": "assistant", "status": "thinking", "id": "msg-123" }

// 流式输出（每收到一段就推送一次）
{ "type": "assistant", "status": "streaming", "id": "msg-123", "content": "你好，我是..." }

// 完成
{ "type": "assistant", "status": "completed", "id": "msg-123", "content": "完整回复内容" }

// 错误
{ "type": "error", "content": "AI 服务调用失败" }
```

### upsert 模式

流式输出时，同一条消息会收到多次 `streaming` 状态，用 **upsert**（存在则更新，不存在则插入）避免重复：

```javascript
const upsertAssistantMessage = (id, content) => {
  const existing = messages.value.find((msg) => msg.id === id)
  if (existing) {
    existing.content = content       // 更新已有消息
    existing.timestamp = new Date()
  } else {
    messages.value.push({            // 插入新消息
      id,
      role: 'assistant',
      content,
      timestamp: new Date(),
    })
  }
  scrollToBottom()
}
```

### 状态机

```javascript
const handleMessage = (response) => {
  switch (response.type) {
    case 'assistant':
      if (response.status === 'thinking') {
        isLoading.value = true                    // 显示 loading
        currentMessageId.value = response.id
      } else if (response.status === 'streaming') {
        upsertAssistantMessage(response.id, response.content)  // 实时更新
      } else if (response.status === 'completed') {
        upsertAssistantMessage(response.id, response.content)  // 最终内容
        isLoading.value = false                   // 隐藏 loading
        currentMessageId.value = null
      }
      break
    case 'error':
      isLoading.value = false
      appendSystemMessage(response.content)       // 显示错误
      break
  }
}
```

---

## 四、对话上下文

### 携带历史消息

每次发送消息时，自动带上最近 10 轮对话（20 条），让 AI 有上下文：

```javascript
const buildChatHistory = () => {
  return messages.value
    .filter((msg) => msg.role === 'user' || msg.role === 'assistant')
    .slice(-20)                                  // 最近 20 条
    .map((msg) => ({
      role: msg.role,
      content: msg.content || '',
    }))
}

// 发送时带上历史
ws.value.send(JSON.stringify({
  type: 'message',
  content: userInput,
  messages: buildChatHistory(),   // 历史对话
}))
```

### 用户画像注入

后端会把用户的赛道、薄弱点、错题数等信息注入到 System Prompt：

```java
// 后端拼接上下文
StringBuilder builder = new StringBuilder("以下是当前学生画像：\n");
builder.append("- 当前赛道：").append(track).append('\n');
builder.append("- 薄弱点：").append(weakTopics).append('\n');
builder.append("- 累计错题：").append(totalErrors).append('\n');

messages.add(ChatMessage.system(builder.toString()));
```

这样 AI 回答时会结合用户的实际学习情况，而不是泛泛而谈。

---

## 五、可拖拽浮窗

### 拖拽实现

用 `mousedown` + `mousemove` + `mouseup` 三件套：

```javascript
const startDrag = (event) => {
  isDragging.value = true
  dragOffset.value = {
    x: event.clientX - position.value.x,    // 鼠标与窗口左上角的偏移
    y: event.clientY - position.value.y,
  }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (event) => {
  position.value = {
    x: event.clientX - dragOffset.value.x,   // 更新位置
    y: event.clientY - dragOffset.value.y,
  }
}

const stopDrag = () => {
  isDragging.value = false
  snapToWindowEdgeIfIntersecting()            // 松手后吸附边缘
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}
```

### 阈值防误触

拖动距离小于 3px 不算拖拽，避免点击和拖拽冲突：

```javascript
const DRAG_THRESHOLD = 3

const onDrag = (event) => {
  const deltaX = event.clientX - dragStartPoint.value.x
  const deltaY = event.clientY - dragStartPoint.value.y
  if (!dragMoved.value && Math.hypot(deltaX, deltaY) >= DRAG_THRESHOLD) {
    dragMoved.value = true                    // 超过阈值才算拖拽
  }
  if (!dragMoved.value) return                // 未超过阈值不更新位置
  // ...
}
```

### 边界约束

窗口不能拖出屏幕：

```javascript
const clampPositionToViewport = () => {
  const maxX = Math.max(0, viewport.value.width - assistantWidth)
  const maxY = Math.max(0, viewport.value.height - assistantHeight)

  position.value = {
    x: Math.min(maxX, Math.max(0, position.value.x)),
    y: Math.min(maxY, Math.max(0, position.value.y)),
  }
}
```

### 性能优化

用 CSS `transform: translate3d()` 代替 `left/top`，触发 GPU 加速，避免重排：

```javascript
const positionStyle = computed(() => ({
  transform: `translate3d(${position.value.x}px, ${position.value.y}px, 0)`,
  width: `${width}px`,
  height: `${height}px`,
}))
```

拖拽时禁用 CSS transition，松手后恢复：

```css
.floating-ai-assistant {
  transition: transform 0.18s ease, width 0.3s ease, height 0.3s ease;
}

.floating-ai-assistant.dragging,
.floating-ai-assistant.resizing {
  transition: none;  /* 拖拽/缩放时无延迟 */
}
```

---

## 六、可缩放窗口

右下角有个拖拽手柄，拖动时同步更新宽高：

```javascript
const startResize = (event) => {
  isResizing.value = true
  resizeStartPoint.value = { x: event.clientX, y: event.clientY }
  resizeStartSize.value = { width: currentWidth, height: currentHeight }

  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}

const onResize = (event) => {
  const nextWidth = resizeStartSize.value.width + (event.clientX - resizeStartPoint.value.x)
  const nextHeight = resizeStartSize.value.height + (event.clientY - resizeStartPoint.value.y)

  size.value = {
    width: Math.min(maxWidth, Math.max(MIN_WIDTH, nextWidth)),   // 限制范围
    height: Math.min(maxHeight, Math.max(MIN_HEIGHT, nextHeight)),
  }
}
```

尺寸限制：
- 最小 320×520（正常使用）
- 硬限 280×360（极限情况）
- 最大不超过视口尺寸

---

## 七、文件上传

### 选择文件

点击附件按钮触发隐藏的 `<input type="file">`：

```javascript
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  validateAndAddFiles(files)
  event.target.value = ''   // 重置 input，允许重复选同一文件
}
```

### 校验规则

```javascript
const validateAndAddFiles = (files) => {
  for (const file of files) {
    if (file.size > 10 * 1024 * 1024) continue        // 10MB 限制
    if (!ALLOWED_FILE_TYPES.includes(file.type)) continue  // 类型白名单
    if (selectedFiles.value.length >= 5) break         // 最多 5 个

    selectedFiles.value.push({
      file,
      name: file.name,
      size: file.size,
      type: file.type,
      previewUrl: isImageFile(file) ? URL.createObjectURL(file) : null,
    })
  }
}
```

### 图片预览

用 `URL.createObjectURL` 生成本地预览 URL，不上传就能看到缩略图：

```javascript
previewUrl: isImageFile(file) ? URL.createObjectURL(file) : null
```

组件销毁时记得释放：

```javascript
const removeFile = (index) => {
  const file = selectedFiles.value[index]
  if (file.previewUrl) URL.revokeObjectURL(file.previewUrl)  // 防止内存泄漏
  selectedFiles.value.splice(index, 1)
}
```

### 上传流程

先上传文件到服务器，拿到 URL 后再和文字一起通过 WebSocket 发送：

```javascript
const sendMessage = async () => {
  // 1. 先上传文件
  let uploadedFiles = []
  if (selectedFiles.value.length > 0) {
    uploadedFiles = await uploadFiles()
  }

  // 2. 文件 URL + 文字一起发
  ws.value.send(JSON.stringify({
    type: 'message',
    content: inputMessage.value,
    files: uploadedFiles,          // [{ name, url, type, isImage }]
    messages: buildChatHistory(),
  }))
}
```

---

## 八、最小化 / 展开

收起时变成一个 60×60 的圆形图标，带连接状态指示灯：

```css
.floating-ai-assistant.minimized {
  border-radius: 50%;
  width: 60px;
  height: 60px;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #dc3545;           /* 断开 - 红色 */
}

.status-indicator.connected {
  background: #28a745;           /* 已连接 - 绿色 */
}
```

---

## 九、组件结构

```
FloatingAIAssistant.vue
  ├── 标题栏（可拖拽）
  │     ├── 连接状态指示灯
  │     ├── 最小化/展开按钮
  │     └── 用户 + AI 头像
  ├── 消息列表（可滚动）
  │     ├── 用户消息（右侧黄色气泡）
  │     ├── AI 消息（左侧灰色气泡）
  │     ├── 系统消息（居中胶囊）
  │     └── Loading 动画
  ├── 输入区
  │     ├── 文本输入框（Enter 发送，Shift+Enter 换行）
  │     ├── 文件预览区（已选文件缩略图）
  │     ├── 工具栏（附件、清空）
  │     └── 发送按钮
  └── 缩放手柄（右下角）
```

---

## 十、数据流

```
用户输入文字 + 选择文件
        ↓
  上传文件 → 拿到 URL
        ↓
  构建消息 { content, files, messages(历史) }
        ↓
  WebSocket.send()
        ↓
  后端：拼 Prompt（System + 用户画像 + 历史 + 当前消息）
        ↓
  调豆包 AI 流式接口
        ↓
  每收到一段 → WebSocket 推送 { status: "streaming", content: "..." }
        ↓
  前端 upsert 更新消息气泡
        ↓
  流结束 → 推送 { status: "completed" }
        ↓
  前端隐藏 loading，完成
```

---

## 十一、踩坑记录

1. **拖拽和点击冲突**：用 3px 阈值区分，`Math.hypot(deltaX, deltaY) >= 3` 才算拖拽。

2. **transform vs left/top**：用 `translate3d` 触发 GPU 加松，拖拽流畅不卡顿。

3. **内存泄漏**：`URL.createObjectURL` 创建的预览 URL 必须手动 `revokeObjectURL` 释放。

4. **WebSocket 重连**：断开后 5 秒重连，但要先检查是否还在登录状态，避免无意义重连。

5. **消息去重**：流式输出用 upsert 模式，同一条消息只更新内容，不重复插入。

6. **边界约束**：窗口拖拽/缩放后要检查是否超出视口，`resize` 事件也要重新约束。
