<template>
  <div class="message-page page-container">
    <div class="message-container glass-panel">
      <!-- 侧边栏：分类与联系人 -->
      <div class="message-sidebar">
        <div class="sidebar-header">
          <h2 class="header-title">消息中心</h2>
        </div>

        <div class="message-categories">
          <div class="category-item" :class="{ active: activeItem === 'sys-notice' }"
            @click="activeItem = 'sys-notice'">
            <div class="icon-box sys-notice">
              <div v-if="hasUnreadSystemNotice" class="red-dot"></div>
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path
                  d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" />
              </svg>
            </div>
            <span class="category-name">系统通知</span>
          </div>
          <div class="category-item" :class="{ active: activeItem === 'likes' }" @click="activeItem = 'likes'">
            <div class="icon-box likes">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path
                  d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z" />
              </svg>
            </div>
            <span class="category-name">赞和收藏</span>
          </div>
          <div class="category-item" :class="{ active: activeItem === 'comments' }" @click="activeItem = 'comments'">
            <div class="icon-box comments">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18z" />
              </svg>
            </div>
            <span class="category-name">评论和 @</span>
          </div>
          <div class="category-item" :class="{ active: activeItem === 'follows' }" @click="activeItem = 'follows'">
            <div class="icon-box follows">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
              </svg>
            </div>
            <span class="category-name">新增关注</span>
          </div>
        </div>

        <div class="sidebar-divider">
          <span>近期私信</span>
        </div>

        <div class="contact-list">
          <div class="contact-item" v-for="contact in chatContacts" :key="contact.sender_id"
            :class="{ active: activeItem === contact.sender_id }" @click="activeItem = contact.sender_id">
            <div class="avatar-wrap">
              <img class="avatar"
                :src="contact.sender_avatar || 'https://api.dicebear.com/7.x/identicon/svg?seed=' + contact.sender_id"
                @click.stop="goToUserHome(contact.sender_id)" alt="avatar" />
              <span v-if="getContactUnreadCount(contact) > 0" class="unread-badge">{{ getUnreadText(contact) }}</span>
            </div>
            <div class="contact-info">
              <div class="contact-top">
                <span class="contact-name">{{ contact.sender_name }}</span>
                <span class="contact-time">{{ formatTime(contact.last_msg_time) }}</span>
              </div>
              <p class="contact-desc">{{ contact.last_msg_content }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：消息内容列表 -->
      <div class="message-content-area">
        <div class="content-header">
          <h2 class="content-title">{{ getTitle() }}</h2>
          <div class="header-actions">
            <button class="action-btn" title="标记已读">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </button>
            <button class="action-btn" title="消息设置" @click="showSettingsModal = true">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3"></circle>
                <path
                  d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z">
                </path>
              </svg>
            </button>
          </div>
        </div>

        <div v-if="activeItem === 'sys-notice'" class="system-msg-list">
          <div class="sys-msg-item" v-for="msg in sysNotices" :key="msg.id">
            <div class="sys-msg-time">{{ formatTime(msg.created_at) }}</div>
            <div class="sys-msg-card" :class="{ 'is-read': msg.is_read }">
              <h3 class="sys-msg-title">{{ msg.title }}</h3>
              <p class="sys-msg-detail">{{ msg.content }}</p>
            </div>
          </div>

          <div v-if="sysNotices.length === 0" class="empty-state">
            <div class="empty-icon">🔔</div>
            <p>暂无系统通知</p>
          </div>
        </div>

        <div v-else-if="activeItem === 'likes'" class="empty-state">
          <div class="empty-icon">👍</div>
          <p>暂无收到的赞和收藏</p>
        </div>

        <div v-else-if="activeItem === 'comments'" class="empty-state">
          <div class="empty-icon">💬</div>
          <p>暂无评论或 @ 你的消息</p>
        </div>

        <div v-else-if="activeItem === 'follows'" class="empty-state">
          <div class="empty-icon">👥</div>
          <p>暂无新增关注</p>
        </div>

        <div v-else class="chat-area">
          <div class="chat-message" :class="getMessageClass(msg)" v-for="msg in currentChatMessages" :key="msg.id">
            <div class="chat-avatar"><img
                :src="msg.sender_avatar || 'https://api.dicebear.com/7.x/identicon/svg?seed=' + msg.sender_id"
                @click.stop="goToUserHome(msg.sender_id)" alt="avatar" /></div>
            <div class="chat-bubble">{{ msg.content }}</div>
          </div>

          <div v-if="currentChatMessages.length === 0" class="empty-state">
            <div class="empty-icon">💬</div>
            <p>点击左侧联系人开始聊天</p>
          </div>

          <div class="chat-input-area" v-if="activeItem && typeof activeItem === 'number'">
            <input type="text" v-model.trim="chatInput" :placeholder="'回复 ' + getTitle() + '...'" class="chat-input"
              maxlength="1000" @keyup.enter="sendMessage" />
            <button class="send-btn" :disabled="isSending || !chatInput" @click="sendMessage">{{ isSending ? '发送中...' :
              '发送' }}</button>
          </div>
        </div>

      </div>
    </div>

    <!-- 消息设置模态窗口 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showSettingsModal" class="modal-overlay" @click.self="closeSettingsModal">
          <div class="modal-container">
            <div class="modal-header">
              <h3 class="modal-title">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="3"></circle>
                  <path
                    d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z">
                  </path>
                </svg>
                消息设置
              </h3>
              <button class="modal-close" @click="closeSettingsModal">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            <div class="modal-body">
              <!-- 通知类型设置 -->
              <div class="settings-section">
                <h4 class="section-title">通知类型</h4>
                <div class="setting-items">
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-label">系统通知</span>
                      <span class="setting-desc">接收平台重要公告和更新</span>
                    </div>
                    <label class="toggle-switch">
                      <input type="checkbox" v-model="settings.systemNotice.enabled">
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                  <div class="setting-item" v-if="settings.systemNotice.enabled">
                    <div class="setting-info">
                      <span class="setting-label">通知频率</span>
                    </div>
                    <select v-model="settings.systemNotice.frequency" class="setting-select">
                      <option value="immediate">即时通知</option>
                      <option value="hourly">每小时汇总</option>
                      <option value="daily">每日汇总</option>
                    </select>
                  </div>

                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-label">邮件提醒</span>
                      <span class="setting-desc">通过邮件接收消息通知</span>
                    </div>
                    <label class="toggle-switch">
                      <input type="checkbox" v-model="settings.email.enabled">
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                  <div class="setting-item" v-if="settings.email.enabled">
                    <div class="setting-info">
                      <span class="setting-label">邮件频率</span>
                    </div>
                    <select v-model="settings.email.frequency" class="setting-select">
                      <option value="immediate">即时发送</option>
                      <option value="hourly">每小时汇总</option>
                      <option value="daily">每日摘要</option>
                      <option value="weekly">每周摘要</option>
                    </select>
                  </div>

                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-label">应用内消息</span>
                      <span class="setting-desc">在应用内显示消息提示</span>
                    </div>
                    <label class="toggle-switch">
                      <input type="checkbox" v-model="settings.inApp.enabled">
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                  <div class="setting-item sub-item" v-if="settings.inApp.enabled">
                    <div class="setting-info">
                      <span class="setting-label">显示消息预览</span>
                    </div>
                    <label class="toggle-switch">
                      <input type="checkbox" v-model="settings.inApp.showPreview">
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                </div>
              </div>

              <!-- 优先级设置 -->
              <div class="settings-section">
                <h4 class="section-title">优先级设置</h4>
                <div class="priority-options">
                  <label class="priority-option" :class="{ active: settings.priority === 'all' }">
                    <input type="radio" v-model="settings.priority" value="all">
                    <span class="priority-badge all">全部</span>
                    <span class="priority-desc">接收所有消息</span>
                  </label>
                  <label class="priority-option" :class="{ active: settings.priority === 'important' }">
                    <input type="radio" v-model="settings.priority" value="important">
                    <span class="priority-badge important">重要</span>
                    <span class="priority-desc">仅重要消息</span>
                  </label>
                  <label class="priority-option" :class="{ active: settings.priority === 'urgent' }">
                    <input type="radio" v-model="settings.priority" value="urgent">
                    <span class="priority-badge urgent">紧急</span>
                    <span class="priority-desc">仅紧急消息</span>
                  </label>
                </div>
              </div>

              <!-- 免打扰设置 -->
              <div class="settings-section">
                <h4 class="section-title">免打扰模式</h4>
                <div class="setting-items">
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-label">开启免打扰</span>
                      <span class="setting-desc">在指定时间段内静音通知</span>
                    </div>
                    <label class="toggle-switch">
                      <input type="checkbox" v-model="settings.dnd.enabled">
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                  <div class="dnd-time-settings" v-if="settings.dnd.enabled">
                    <div class="time-input-group">
                      <label>开始时间</label>
                      <input type="time" v-model="settings.dnd.startTime" class="time-input">
                    </div>
                    <div class="time-input-group">
                      <label>结束时间</label>
                      <input type="time" v-model="settings.dnd.endTime" class="time-input">
                    </div>
                  </div>
                </div>
              </div>

              <!-- 声音设置 -->
              <div class="settings-section">
                <h4 class="section-title">声音设置</h4>
                <div class="setting-items">
                  <div class="setting-item">
                    <div class="setting-info">
                      <span class="setting-label">消息提示音</span>
                      <span class="setting-desc">收到新消息时播放声音</span>
                    </div>
                    <label class="toggle-switch">
                      <input type="checkbox" v-model="settings.sound.enabled">
                      <span class="toggle-slider"></span>
                    </label>
                  </div>
                  <div class="setting-item" v-if="settings.sound.enabled">
                    <div class="setting-info">
                      <span class="setting-label">音量</span>
                    </div>
                    <input type="range" v-model="settings.sound.volume" min="0" max="100" class="volume-slider">
                    <span class="volume-value">{{ settings.sound.volume }}%</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn btn-secondary" @click="closeSettingsModal">取消</button>
              <button class="btn btn-primary" @click="saveSettings" :disabled="isSaving">
                <span v-if="isSaving" class="loading-spinner"></span>
                {{ isSaving ? '保存中...' : '保存设置' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { messageApi } from '@/api/message'
import { ElMessage } from 'element-plus'

const router = useRouter()

const activeItem = ref('sys-notice')
const sysNotices = ref([])
const chatContacts = ref([])
const currentChatMessages = ref([])
const chatInput = ref('')
const isSending = ref(false)

const currentUserId = ref(null)

const hasUnreadSystemNotice = computed(() => {
  return (sysNotices.value || []).some(item => Number(item?.is_read) === 0)
})

// 消息设置模态窗口
const showSettingsModal = ref(false)
const isSaving = ref(false)

// 默认设置
const defaultSettings = {
  systemNotice: {
    enabled: true,
    frequency: 'immediate'
  },
  email: {
    enabled: false,
    frequency: 'daily'
  },
  inApp: {
    enabled: true,
    showPreview: true
  },
  priority: 'all',
  dnd: {
    enabled: false,
    startTime: '22:00',
    endTime: '08:00'
  },
  sound: {
    enabled: true,
    volume: 70
  }
}

// 当前设置（从本地存储加载或使用默认）
const settings = ref({ ...defaultSettings })

// 加载设置
const loadSettings = () => {
  const saved = localStorage.getItem('messageSettings')
  if (saved) {
    try {
      settings.value = { ...defaultSettings, ...JSON.parse(saved) }
    } catch (e) {
      console.error('加载设置失败', e)
    }
  }
}

// 关闭设置模态窗口
const closeSettingsModal = () => {
  showSettingsModal.value = false
  // 重置为已保存的设置
  loadSettings()
}

// 保存设置
const saveSettings = async () => {
  isSaving.value = true
  try {
    // 保存到本地存储
    localStorage.setItem('messageSettings', JSON.stringify(settings.value))

    // 这里可以添加API调用保存到服务器
    // await messageApi.updateSettings(settings.value)

    ElMessage.success('设置已保存')
    showSettingsModal.value = false
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  const userRaw = localStorage.getItem('user')
  if (userRaw) {
    try {
      const user = JSON.parse(userRaw)
      currentUserId.value = Number(user?.id) || null
    } catch (error) {
      currentUserId.value = null
    }
  }

  fetchMessages()
  loadSettings()
})

watch(activeItem, async (newVal) => {
  if (typeof newVal === 'number') {
    await fetchConversation(newVal)
  }
})

const fetchConversation = async (contactId) => {
  try {
    const res = await messageApi.getConversation(contactId)
    if (res.data?.code === 0 || res.data?.code === 200) {
      currentChatMessages.value = res.data.data || []
      await markConversationRead(contactId)
    }
  } catch (error) {
    console.error('获取会话失败', error)
  }
}

const markConversationRead = async (contactId) => {
  const unreadIncoming = currentChatMessages.value.filter(msg => {
    const isUnread = Number(msg?.is_read) === 0
    const isIncoming = Number(msg?.sender_id) === Number(contactId)
    const belongsToCurrentUser = Number(msg?.user_id) === Number(currentUserId.value)
    return isUnread && isIncoming && belongsToCurrentUser
  })

  if (unreadIncoming.length === 0) {
    return
  }

  try {
    await Promise.all(unreadIncoming.map(msg => messageApi.markAsRead(msg.id)))
    currentChatMessages.value = currentChatMessages.value.map(msg => {
      const matched = unreadIncoming.find(item => Number(item.id) === Number(msg.id))
      if (!matched) return msg
      return {
        ...msg,
        is_read: 1,
      }
    })
    await fetchMessages()
  } catch (error) {
    console.error('会话已读更新失败', error)
  }
}

const sendMessage = async () => {
  if (typeof activeItem.value !== 'number') return
  if (!chatInput.value) return

  isSending.value = true
  const content = chatInput.value

  try {
    const res = await messageApi.sendChatMessage(activeItem.value, content)
    if (res.data?.code === 0 || res.data?.code === 200) {
      chatInput.value = ''
      await fetchConversation(activeItem.value)
      await fetchMessages()
      ElMessage.success('发送成功')
      return
    }
    ElMessage.error(res.data?.message || '发送失败')
  } catch (error) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    isSending.value = false
  }
}

const fetchMessages = async () => {
  try {
    // 获取通知列表
    const noticeRes = await messageApi.getMessages('sys-notice')
    if (noticeRes.data?.code === 0 || noticeRes.data?.code === 200) {
      sysNotices.value = noticeRes.data.data
    }

    // 获取联系人列表
    const contactsRes = await messageApi.getContacts()
    if (contactsRes.data?.code === 0 || contactsRes.data?.code === 200) {
      chatContacts.value = contactsRes.data.data
    }

    // 如果当前展开的是联系人，立刻更新会话
    if (typeof activeItem.value === 'number') {
      await fetchConversation(activeItem.value)
    }
  } catch (err) {
    console.error('获取消息失败', err)
  }
}

// 简单的日期格式化函数
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const getTitle = () => {
  const titles = {
    'sys-notice': '系统通知',
    'likes': '赞和收藏',
    'comments': '评论和 @',
    'follows': '新增关注'
  }

  if (titles[activeItem.value]) {
    return titles[activeItem.value]
  }

  // 判断是否是私信联系人
  const contact = chatContacts.value.find(c => c.sender_id === activeItem.value)
  if (contact) {
    return contact.sender_name
  }

  return '消息'
}

const getMessageClass = (msg) => {
  if (currentUserId.value && Number(msg.sender_id) === Number(currentUserId.value)) {
    return 'sent'
  }
  return 'received'
}

const getContactUnreadCount = (contact) => {
  return Number(contact?.unread_count || 0)
}

const getUnreadText = (contact) => {
  const count = getContactUnreadCount(contact)
  return count > 99 ? '99+' : String(count)
}

const goToUserHome = (userId) => {
  if (!userId) return
  router.push(`/profile?userId=${userId}`)
}
</script>

<style scoped>
.message-page {
  padding: 24px;
  height: calc(100vh - 72px);
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

.glass-panel {
  background: var(--card-bg, rgba(255, 255, 255, 0.6));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

.message-container {
  width: 100%;
  max-width: 1000px;
  height: 100%;
  display: flex;
  overflow: hidden;
}

/* 侧边栏 */
.message-sidebar {
  width: 280px;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--text-title, #1e293b);
  letter-spacing: 0.5px;
}

.message-categories {
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-item:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateX(4px);
}

.category-item.active {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-right: 16px;
}

.sys-notice {
  background: linear-gradient(135deg, #ff9a9e, #fecfef);
}

.likes {
  background: linear-gradient(135deg, #a18cd1, #fbc2eb);
}

.comments {
  background: linear-gradient(135deg, #84fab0, #8fd3f4);
}

.follows {
  background: linear-gradient(135deg, #fccb90, #d57eeb);
}

.red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid #fff;
}

.category-name {
  font-size: 15px;
  color: var(--text-main, #334155);
  font-weight: 500;
}

.sidebar-divider {
  padding: 16px 24px 8px;
  font-size: 13px;
  color: var(--text-sub, #94a3b8);
  font-weight: 600;
  letter-spacing: 1px;
}

.contact-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-item {
  display: flex;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.contact-item:hover {
  background: rgba(255, 255, 255, 0.5);
}

.avatar-wrap {
  position: relative;
  margin-right: 14px;
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  background: #f1f5f9;
  border: 2px solid #fff;
  cursor: pointer;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ff4d4f;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  border: 2px solid #fff;
  box-sizing: border-box;
}

.badge {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: #ff8c00;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  border: 1px solid #fff;
  white-space: nowrap;
}

.contact-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.contact-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.contact-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-title, #1e293b);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-time {
  font-size: 12px;
  color: var(--text-sub, #94a3b8);
}

.contact-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-main, #64748b);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.emoji {
  color: #ff4d4f;
  font-weight: bold;
}

/* 右侧内容区 */
.message-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.4);
}

.content-header {
  padding: 24px 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
}

.content-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text-title, #1e293b);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub, #64748b);
  transition: all 0.2s;
}

.action-btn:hover {
  background: #fff;
  color: var(--brand-blue, #3b82f6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.system-msg-list {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.sys-msg-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.sys-msg-time {
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.04);
  color: var(--text-sub, #64748b);
  font-size: 12px;
  font-weight: 500;
}

.sys-msg-card {
  width: 100%;
  max-width: 600px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: transform 0.2s;
}

.sys-msg-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.sys-msg-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-title, #1e293b);
}

.sys-msg-detail {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main, #475569);
}

.sys-msg-link {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--brand-blue, #3b82f6);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.sys-msg-link:hover {
  opacity: 0.8;
}

/* 新增的空状态与聊天样式 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--text-sub, #94a3b8);
  font-size: 15px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 24px;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 24px;
}

.chat-message.sent {
  justify-content: flex-end;
}

.chat-message.sent .chat-avatar {
  order: 2;
  margin-right: 0;
  margin-left: 12px;
}

.chat-message.sent .chat-bubble {
  order: 1;
}

.chat-message.received .chat-bubble {
  background: rgba(255, 255, 255, 0.8);
  color: var(--text-title, #1e293b);
  border-radius: 2px 16px 16px 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.chat-message.sent .chat-bubble {
  background: var(--brand-blue, #3b82f6);
  color: #fff;
  border-radius: 16px 2px 16px 16px;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
  overflow: hidden;
  background: #f1f5f9;
  flex-shrink: 0;
  border: 2px solid #fff;
}

.chat-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
}

.chat-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  max-width: 70%;
}

.chat-input-area {
  margin-top: auto;
  display: flex;
  gap: 12px;
  background: rgba(255, 255, 255, 0.6);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
}

.chat-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 14px;
  color: var(--text-title, #1e293b);
}

.send-btn {
  border: none;
  background: var(--brand-blue, #3b82f6);
  color: #fff;
  padding: 8px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.send-btn:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 模态窗口样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  width: 100%;
  max-width: 560px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.8);
}

.modal-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-title, #1e293b);
}

.modal-title svg {
  color: var(--brand-blue, #3b82f6);
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub, #64748b);
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.08);
  color: var(--text-title, #1e293b);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.8);
}

/* 设置区块 */
.settings-section {
  margin-bottom: 28px;
}

.settings-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-title, #1e293b);
  margin: 0 0 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

/* 设置项 */
.setting-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.setting-item.sub-item {
  margin-left: 24px;
  background: rgba(248, 250, 252, 0.8);
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.setting-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-title, #1e293b);
}

.setting-desc {
  font-size: 12px;
  color: var(--text-sub, #94a3b8);
}

/* 开关样式 */
.toggle-switch {
  position: relative;
  width: 48px;
  height: 26px;
  cursor: pointer;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #e2e8f0;
  border-radius: 26px;
  transition: 0.3s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-switch input:checked+.toggle-slider {
  background: var(--brand-blue, #3b82f6);
}

.toggle-switch input:checked+.toggle-slider::before {
  transform: translateX(22px);
}

/* 下拉选择 */
.setting-select {
  padding: 8px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background: white;
  font-size: 13px;
  color: var(--text-title, #1e293b);
  cursor: pointer;
  outline: none;
  min-width: 120px;
}

.setting-select:focus {
  border-color: var(--brand-blue, #3b82f6);
}

/* 优先级选项 */
.priority-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.priority-option {
  flex: 1;
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.priority-option:hover {
  background: rgba(255, 255, 255, 0.9);
}

.priority-option.active {
  border-color: var(--brand-blue, #3b82f6);
  background: rgba(59, 130, 246, 0.05);
}

.priority-option input {
  display: none;
}

.priority-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.priority-badge.all {
  background: linear-gradient(135deg, #84fab0, #8fd3f4);
  color: #0f766e;
}

.priority-badge.important {
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
  color: #9a3412;
}

.priority-badge.urgent {
  background: linear-gradient(135deg, #ff9a9e, #fecfef);
  color: #9f1239;
}

.priority-desc {
  font-size: 12px;
  color: var(--text-sub, #64748b);
}

/* 免打扰时间设置 */
.dnd-time-settings {
  display: flex;
  gap: 16px;
  margin-left: 24px;
  padding: 12px 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
}

.time-input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-input-group label {
  font-size: 12px;
  color: var(--text-sub, #64748b);
}

.time-input {
  padding: 8px 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-title, #1e293b);
  outline: none;
}

.time-input:focus {
  border-color: var(--brand-blue, #3b82f6);
}

/* 音量滑块 */
.volume-slider {
  flex: 1;
  max-width: 150px;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #e2e8f0;
  border-radius: 3px;
  outline: none;
}

.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  background: var(--brand-blue, #3b82f6);
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.volume-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  background: var(--brand-blue, #3b82f6);
  border-radius: 50%;
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.volume-value {
  font-size: 13px;
  color: var(--text-sub, #64748b);
  min-width: 40px;
  text-align: right;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: var(--brand-blue, #3b82f6);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: rgba(0, 0, 0, 0.04);
  color: var(--text-title, #1e293b);
}

.btn-secondary:hover {
  background: rgba(0, 0, 0, 0.08);
}

/* 加载动画 */
.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 模态窗口动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-container,
.modal-fade-leave-active .modal-container {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-fade-enter-from .modal-container,
.modal-fade-leave-to .modal-container {
  transform: scale(0.95);
  opacity: 0;
}

/* 适配移动端 */
@media (max-width: 768px) {
  .message-page {
    padding: 16px;
  }

  .message-sidebar {
    width: 80px;
  }

  .category-name,
  .contact-info,
  .sidebar-divider,
  .header-title {
    display: none;
  }

  .icon-box,
  .avatar-wrap {
    margin-right: 0;
  }

  .category-item,
  .contact-item {
    justify-content: center;
  }

  .sys-msg-card {
    padding: 16px;
  }

  .modal-container {
    max-height: 90vh;
    margin: 10px;
  }

  .modal-body {
    padding: 16px;
  }

  .priority-options {
    flex-direction: column;
  }

  .priority-option {
    flex-direction: row;
    justify-content: flex-start;
    padding: 12px;
  }

  .dnd-time-settings {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
