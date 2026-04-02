<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLevelStore } from '../stores/level'
import api from '../api'

const userStore = useUserStore()
const levelStore = useLevelStore()

// 当前选中的题库标签
const activeRepoTab = ref('created') // 'created' | 'solved'

// 我的题目（用户自己创建的题目）
const myProblems = ref([
  {
    id: 1,
    name: '自定义动态规划题',
    description: '我创建的DP练习题 - 爬楼梯变种',
    difficulty: '中等',
    difficultyColor: '#d4a373',
    solvedCount: 56,
    createdAt: '2026-03-15',
    isPublic: true
  },
  {
    id: 2,
    name: '二叉树遍历挑战',
    description: '非递归实现前中后序遍历',
    difficulty: '困难',
    difficultyColor: '#e74c3c',
    solvedCount: 23,
    createdAt: '2026-03-10',
    isPublic: true
  },
  {
    id: 3,
    name: '图论最短路径',
    description: 'Dijkstra算法实现与应用',
    difficulty: '困难',
    difficultyColor: '#e74c3c',
    solvedCount: 18,
    createdAt: '2026-02-28',
    isPublic: false
  },
  {
    id: 4,
    name: '数组去重技巧',
    description: '多种方法实现数组去重',
    difficulty: '简单',
    difficultyColor: '#27ae60',
    solvedCount: 128,
    createdAt: '2026-02-20',
    isPublic: true
  }
])

// 做过的题目（用户完成的别人的题目）
const solvedProblems = ref([
  {
    id: 101,
    name: '两数之和',
    description: 'LeetCode 经典入门题',
    difficulty: '简单',
    difficultyColor: '#27ae60',
    author: '官方题库',
    solvedAt: '2026-04-01',
    attempts: 1
  },
  {
    id: 102,
    name: '三数之和',
    description: '双指针技巧应用',
    difficulty: '中等',
    difficultyColor: '#d4a373',
    author: '官方题库',
    solvedAt: '2026-04-01',
    attempts: 2
  },
  {
    id: 103,
    name: '最长回文子串',
    description: '中心扩展法',
    difficulty: '中等',
    difficultyColor: '#d4a373',
    author: '算法达人',
    solvedAt: '2026-03-30',
    attempts: 3
  },
  {
    id: 104,
    name: '最长递增子序列',
    description: '动态规划 + 二分查找',
    difficulty: '中等',
    difficultyColor: '#d4a373',
    author: '竞赛专区',
    solvedAt: '2026-03-28',
    attempts: 2
  }
])

// 做题统计数据（从后端获取）
const problemStats = ref({
  totalSolved: 0,
  totalCreated: 0,
  streakDays: 0,
  todaySolved: 0,
  weeklySolved: 0,
  monthlySolved: 0
})

// 做题热力图数据
const contributionYear = ref(2026)
const contributionData = ref([])

// 从后端获取做题热力图数据
const fetchProblemHeatmap = async (year) => {
  try {
    const response = await api.getUserProblemHeatmap(year)
    if (response.success) {
      contributionData.value = response.data.map(item => ({
        date: item.date,
        level: item.count === 0 ? 0 : item.count <= 2 ? 1 : item.count <= 5 ? 2 : item.count <= 8 ? 3 : 4,
        count: item.count
      }))
    }
  } catch (error) {
    console.error('获取做题数据失败:', error)
    // 使用模拟数据
    generateMockContributionData()
  }
}

// 生成模拟做题数据 (60周 = 420天)
const generateMockContributionData = () => {
  const data = []
  const startDate = new Date(contributionYear.value, 0, 1)
  const totalDays = 60 * 7 // 60周
  
  for (let i = 0; i < totalDays; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)
    
    const isWeekend = date.getDay() === 0 || date.getDay() === 6
    const baseLevel = isWeekend ? 0.4 : 0.2
    const random = Math.random()
    
    let level = 0
    if (random < baseLevel) level = 1
    else if (random < baseLevel + 0.2) level = 2
    else if (random < baseLevel + 0.35) level = 3
    else if (random < baseLevel + 0.45) level = 4
    
    data.push({
      date: date.toISOString().split('T')[0],
      level: level,
      count: level === 0 ? 0 : Math.floor(Math.random() * 10) + 1
    })
  }
  contributionData.value = data
}

// 从后端获取统计数据
const fetchProblemStats = async () => {
  try {
    const response = await api.getUserProblemStats()
    if (response.success) {
      problemStats.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用本地计算的数据
    problemStats.value = {
      totalSolved: solvedProblems.value.length,
      totalCreated: myProblems.value.length,
      streakDays: 12,
      todaySolved: 2,
      weeklySolved: 15,
      monthlySolved: 58
    }
  }
}

const totalContributions = computed(() => {
  return contributionData.value.reduce((sum, day) => sum + day.count, 0)
})

const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
const weekDaysShort = ['', 'Mon', '', 'Wed', '', 'Fri', '']

// 获取月份标签位置（只显示当前年份的月份）
const monthPositions = computed(() => {
  const positions = []
  const weeksToShow = 60
  const daysPerWeek = 7
  
  const startDate = new Date(contributionYear.value, 0, 1)
  let lastMonth = -1
  
  for (let weekIndex = 0; weekIndex < weeksToShow; weekIndex++) {
    const dayIndex = weekIndex * daysPerWeek
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + dayIndex)
    
    const year = date.getFullYear()
    const month = date.getMonth()
    
    // 只显示当前年份的月份，跳过其他年份
    if (year !== contributionYear.value) continue
    
    // 只在月份变化时添加
    if (month !== lastMonth) {
      lastMonth = month
      positions.push({
        month: months[month],
        weekIndex: weekIndex
      })
    }
  }
  
  return positions
})

const solvedLevels = computed(() => {
  if (!levelStore.levels.length) return 0
  return levelStore.levels.filter((item) => item.isUnlocked).length
})

const totalLevels = computed(() => levelStore.levels.length)

const levelTitle = computed(() => {
  const score = Number(userStore.points || 0)
  if (score >= 220) return '冲榜王者'
  if (score >= 140) return '稳定输出'
  if (score >= 80) return '进阶练习者'
  return '新手起步'
})

// 活动数据（从后端获取）
const recentActivities = ref([])

// 从后端获取活动数据
const fetchActivities = async () => {
  try {
    const response = await api.getUserActivities()
    if (response.success) {
      recentActivities.value = response.data.map(activity => ({
        type: activity.type,
        title: activity.title,
        time: formatTime(activity.createdAt),
        icon: getActivityIcon(activity.type)
      }))
    }
  } catch (error) {
    console.error('获取活动数据失败:', error)
    // 使用模拟数据
    recentActivities.value = [
      { type: 'solve', title: '完成了 两数之和', time: '2小时前', icon: '✅' },
      { type: 'solve', title: '完成了 三数之和', time: '5小时前', icon: '✅' },
      { type: 'create', title: '创建了题目 动态规划进阶', time: '昨天', icon: '📝' },
      { type: 'star', title: '收藏了 动态规划专题', time: '昨天', icon: '⭐' },
      { type: 'solve', title: '完成了 最长回文子串', time: '3天前', icon: '✅' },
    ]
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 获取活动图标
const getActivityIcon = (type) => {
  const icons = {
    solve: '✅',
    create: '📝',
    star: '⭐',
    fork: '📦',
    comment: '💬',
    share: '📤'
  }
  return icons[type] || '📌'
}

// 显示编辑弹窗
const showEditModal = ref(false)

// 显示详细编辑页面
const showDetailEdit = ref(false)

// 显示心情设置弹窗
const showStatusModal = ref(false)

// 用户状态设置
const userStatus = ref({
  emoji: '😊',
  mood: 'happy',
  isBusy: false,
  busyAutoReply: '我现在有点忙，稍后回复你~',
  busyEndTime: null
})

// 可选的心情emoji列表
const moodEmojis = [
  { emoji: '😊', label: '开心', value: 'happy' },
  { emoji: '😎', label: '酷', value: 'cool' },
  { emoji: '🤔', label: '思考', value: 'thinking' },
  { emoji: '😴', label: '困倦', value: 'sleepy' },
  { emoji: '🔥', label: '热血', value: 'fire' },
  { emoji: '💪', label: '加油', value: 'strong' },
  { emoji: '📚', label: '学习', value: 'study' },
  { emoji: '🎯', label: '专注', value: 'focus' },
  { emoji: '☕', label: '休息', value: 'coffee' },
  { emoji: '🎮', label: '游戏', value: 'game' },
  { emoji: '🏃', label: '运动', value: 'run' },
  { emoji: '🌙', label: '晚安', value: 'night' }
]

// 忙碌时长选项
const busyDurationOptions = [
  { label: '30分钟', value: 30 },
  { label: '1小时', value: 60 },
  { label: '2小时', value: 120 },
  { label: '4小时', value: 240 },
  { label: '8小时', value: 480 },
  { label: '自定义', value: 'custom' }
]

const selectedBusyDuration = ref(60)
const customBusyMinutes = ref(30)

// 选择心情
const selectMood = (mood) => {
  userStatus.value.emoji = mood.emoji
  userStatus.value.mood = mood.value
}

// 切换忙碌状态
const toggleBusy = () => {
  userStatus.value.isBusy = !userStatus.value.isBusy
  if (userStatus.value.isBusy) {
    const duration = selectedBusyDuration.value === 'custom' 
      ? customBusyMinutes.value 
      : selectedBusyDuration.value
    const endTime = new Date(Date.now() + duration * 60 * 1000)
    userStatus.value.busyEndTime = endTime
    ElMessage.success(`已开启忙碌模式，将在 ${duration} 分钟后自动关闭`)
  } else {
    userStatus.value.busyEndTime = null
    ElMessage.info('已关闭忙碌模式')
  }
}

// 检查忙碌状态是否过期
const checkBusyStatus = () => {
  if (userStatus.value.isBusy && userStatus.value.busyEndTime) {
    if (new Date() >= new Date(userStatus.value.busyEndTime)) {
      userStatus.value.isBusy = false
      userStatus.value.busyEndTime = null
      ElMessage.info('忙碌模式已自动关闭')
    }
  }
}

// 格式化剩余时间
const formatRemainingTime = computed(() => {
  if (!userStatus.value.isBusy || !userStatus.value.busyEndTime) return ''
  const remaining = new Date(userStatus.value.busyEndTime) - new Date()
  if (remaining <= 0) return ''
  const hours = Math.floor(remaining / (1000 * 60 * 60))
  const minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60))
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
})

// 点击状态emoji
const handleStatusClick = () => {
  showStatusModal.value = true
}

// 保存状态设置
const saveStatusSettings = () => {
  localStorage.setItem('userStatus', JSON.stringify(userStatus.value))
  showStatusModal.value = false
  ElMessage.success('状态设置已保存')
}

const editForm = ref({
  name: userStore.userInfo?.name || '',
  bio: userStore.userInfo?.bio || '先把基础打扎实，再冲更高难度。',
  avatar: userStore.userInfo?.avatar || 'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Byte',
  email: userStore.userInfo?.email || '',
  github: userStore.userInfo?.github || '',
  website: userStore.userInfo?.website || ''
})

const handleSave = async () => {
  try {
    const response = await api.updateUserProfile({
      name: editForm.value.name,
      bio: editForm.value.bio,
      avatar: editForm.value.avatar,
      email: editForm.value.email,
      github: editForm.value.github,
      website: editForm.value.website
    })
    
    // 后端返回格式: { data: { code: 0, data: user } }
    if (response.data && response.data.code === 0) {
      userStore.updateProfile({
        name: editForm.value.name,
        bio: editForm.value.bio,
        avatar: editForm.value.avatar,
        email: editForm.value.email,
        github: editForm.value.github,
        website: editForm.value.website
      })
      showEditModal.value = false
      showDetailEdit.value = false
      ElMessage.success('个人资料已更新')
    } else {
      ElMessage.error(response.data?.message || '更新失败')
    }
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新失败，请重试')
  }
}

const changeYear = (year) => {
  contributionYear.value = year
  fetchProblemHeatmap(year)
}

const handleAvatarClick = () => {
  showDetailEdit.value = true
}

// 头像加载错误处理
const handleAvatarError = (event) => {
  // 当图片加载失败时，使用默认头像
  event.target.src = 'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Byte'
}

// 头像上传相关
const fileInputRef = ref(null)
const isUploading = ref(false)

const handleUploadClick = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WEBP 格式的图片')
    return
  }

  // 验证文件大小 (5MB)
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 5MB')
    return
  }

  isUploading.value = true
  try {
    const response = await api.uploadAvatar(file)
    if (response.data && response.data.code === 0) {
      const avatarUrl = response.data.data.avatarUrl
      editForm.value.avatar = avatarUrl
      // 更新用户 store 中的头像
      userStore.updateProfile({ avatar: avatarUrl })
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(response.data?.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    isUploading.value = false
    // 清空 input 以便可以重复选择同一文件
    event.target.value = ''
  }
}

// 加载更多活动
const loadMoreActivities = async () => {
  try {
    const response = await api.getUserActivities({ page: 2, limit: 10 })
    if (response.success) {
      const newActivities = response.data.map(activity => ({
        type: activity.type,
        title: activity.title,
        time: formatTime(activity.createdAt),
        icon: getActivityIcon(activity.type)
      }))
      recentActivities.value.push(...newActivities)
    }
  } catch (error) {
    console.error('加载更多活动失败:', error)
    ElMessage.info('没有更多活动了')
  }
}

onMounted(() => {
  if (!levelStore.levels.length) {
    levelStore.fetchLevels()
  }
  fetchProblemHeatmap(contributionYear.value)
  fetchProblemStats()
  fetchActivities()
})
</script>

<template>
  <div class="github-profile-page">
    <div class="profile-container">
      <!-- 左侧个人信息 -->
      <aside class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="handleAvatarClick">
            <div class="avatar-container">
              <img 
                :src="userStore.userInfo?.avatar || 'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Byte'" 
                :alt="userStore.userInfo?.name"
                class="profile-avatar"
                @error="handleAvatarError"
              />
              <div v-if="!userStore.userInfo?.avatar" class="avatar-placeholder">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
            </div>
            <div class="status-emoji" @click.stop="handleStatusClick" :class="{ busy: userStatus.isBusy }">
              {{ userStatus.emoji }}
              <span v-if="userStatus.isBusy" class="busy-indicator"></span>
            </div>
            <p class="avatar-hint">更改你的头像</p>
          </div>
        </div>
        
        <div class="profile-info">
          <h1 class="profile-name">{{ userStore.userInfo?.name || '同学' }}</h1>
          <p class="profile-username">{{ userStore.userInfo?.name || 'user' }}</p>
          <p class="profile-bio">{{ userStore.userInfo?.bio || '先把基础打扎实，再冲更高难度。' }}</p>
        </div>
        
        <button class="edit-profile-btn" @click="showEditModal = true">
          编辑个人资料
        </button>
        
        <div class="profile-stats">
          <div class="stat-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
            <span>{{ userStore.points }}</span>
            <span class="stat-label">积分</span>
          </div>
          <div class="stat-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
            <span>{{ solvedLevels }}</span>
            <span class="stat-label">已通关</span>
          </div>
          <div class="stat-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
            <span>{{ problemStats.totalSolved }}</span>
            <span class="stat-label">题目</span>
          </div>
        </div>
        
        <div class="profile-details">
          <div class="detail-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <span>{{ levelTitle }}</span>
          </div>
          <div class="detail-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            <span>中国</span>
          </div>
          <div class="detail-item">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            <span>加入于 {{ new Date().getFullYear() }}年</span>
          </div>
        </div>
      </aside>
      
      <!-- 右侧内容区 -->
      <main class="profile-main">
        <!-- 热门题库 - 分为我的题目和做过的题 -->
        <section class="repositories-section">
          <div class="section-header">
            <div class="repo-tabs">
              <button 
                class="repo-tab" 
                :class="{ active: activeRepoTab === 'created' }"
                @click="activeRepoTab = 'created'"
              >
                我的题目
                <span class="tab-count">{{ myProblems.length }}</span>
              </button>
              <button 
                class="repo-tab" 
                :class="{ active: activeRepoTab === 'solved' }"
                @click="activeRepoTab = 'solved'"
              >
                做过的题
                <span class="tab-count">{{ solvedProblems.length }}</span>
              </button>
            </div>
            <a href="#" class="customize-link">自定义您的徽章。</a>
          </div>
          
          <!-- 我的题目 -->
          <div v-if="activeRepoTab === 'created'" class="repo-grid">
            <div v-for="problem in myProblems" :key="problem.id" class="repo-card">
              <div class="repo-header">
                <a href="#" class="repo-name">{{ problem.name }}</a>
                <span class="repo-badge">{{ problem.isPublic ? '公开' : '私有' }}</span>
              </div>
              <p class="repo-description">{{ problem.description }}</p>
              <div class="repo-footer">
                <span class="repo-difficulty">
                  <span class="difficulty-dot" :style="{ backgroundColor: problem.difficultyColor }"></span>
                  {{ problem.difficulty }}
                </span>
                <span class="repo-meta">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  {{ problem.solvedCount }} 人完成
                </span>
              </div>
            </div>
          </div>
          
          <!-- 做过的题 -->
          <div v-else class="repo-grid">
            <div v-for="problem in solvedProblems" :key="problem.id" class="repo-card">
              <div class="repo-header">
                <a href="#" class="repo-name">{{ problem.name }}</a>
              </div>
              <p class="repo-description">{{ problem.description }}</p>
              <div class="repo-footer">
                <span class="repo-difficulty">
                  <span class="difficulty-dot" :style="{ backgroundColor: problem.difficultyColor }"></span>
                  {{ problem.difficulty }}
                </span>
                <span class="repo-meta">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  尝试 {{ problem.attempts }} 次
                </span>
              </div>
            </div>
          </div>
        </section>
        
        <!-- 做题统计热力图 -->
        <section class="contributions-section">
          <div class="contributions-header">
            <h3>{{ contributionYear }}年 已做 {{ totalContributions }} 题</h3>
            <div class="stats-summary">
              <span class="stat-badge">今日 {{ problemStats.todaySolved }} 题</span>
              <span class="stat-badge">本周 {{ problemStats.weeklySolved }} 题</span>
              <span class="stat-badge">本月 {{ problemStats.monthlySolved }} 题</span>
            </div>
          </div>
          
          <div class="contributions-content">
            <div class="contributions-calendar">
              <!-- 月份标签 -->
              <div class="months-row">
                <div v-for="(pos, index) in monthPositions" :key="index" 
                     class="month-label" 
                     :style="{ left: (pos.weekIndex * 14 + 30) + 'px' }">
                  {{ pos.month }}
                </div>
              </div>
              
              <div class="calendar-body">
                <!-- 星期标签 -->
                <div class="weekdays-col">
                  <div v-for="(day, index) in weekDaysShort" :key="index" class="weekday-label">
                    {{ day }}
                  </div>
                </div>
                
                <!-- 做题格子 (60周) -->
                <div class="contributions-grid">
                  <div v-for="week in 60" :key="week" class="week-column">
                    <div 
                      v-for="day in 7" :key="day"
                      class="contribution-cell"
                      :class="'level-' + (contributionData[(week-1)*7 + (day-1)]?.level || 0)"
                      :title="contributionData[(week-1)*7 + (day-1)]?.date + ': 完成 ' + (contributionData[(week-1)*7 + (day-1)]?.count || 0) + ' 题'"
                    ></div>
                  </div>
                </div>
              </div>
              
              <!-- 图例 -->
              <div class="contributions-legend">
                <span class="legend-label">较少</span>
                <div class="legend-cells">
                  <div class="legend-cell level-0"></div>
                  <div class="legend-cell level-1"></div>
                  <div class="legend-cell level-2"></div>
                  <div class="legend-cell level-3"></div>
                  <div class="legend-cell level-4"></div>
                </div>
                <span class="legend-label">较多</span>
              </div>
            </div>
            
            <!-- 年份选择器 -->
            <div class="year-selector">
              <button 
                v-for="year in [2026, 2025, 2024, 2023, 2022]" 
                :key="year"
                class="year-btn"
                :class="{ active: contributionYear === year }"
                @click="changeYear(year)"
              >
                {{ year }}
              </button>
            </div>
          </div>
        </section>
        
        <!-- 活动 -->
        <section class="activity-section">
          <h3>活动</h3>
          <div class="activity-timeline">
            <div class="activity-date">
              <span>{{ new Date().getFullYear() }}年 {{ new Date().getMonth() + 1 }}月{{ new Date().getDate() }}日</span>
            </div>
            <div class="activity-list">
              <div v-if="recentActivities.length === 0" class="empty-activity">
                <p>{{ userStore.userInfo?.name || '用户' }} 在此期间尚无任何活动。</p>
              </div>
              <div v-else v-for="(activity, index) in recentActivities" :key="index" class="activity-item">
                <span class="activity-icon">{{ activity.icon }}</span>
                <span class="activity-text">{{ activity.title }}</span>
                <span class="activity-time">{{ activity.time }}</span>
              </div>
            </div>
          </div>
          <button class="show-more-btn" @click="loadMoreActivities">显示更多活动</button>
        </section>
      </main>
    </div>
    
    <!-- 快速编辑弹窗 - 玻璃拟态风格 -->
    <Transition name="modal">
      <div v-if="showEditModal" class="edit-modal glass-modal" @click.self="showEditModal = false">
        <div class="edit-container glass-container">
          <div class="edit-header glass-header">
            <div class="header-icon">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <h3>编辑个人资料</h3>
            <button class="close-btn glass-close" @click="showEditModal = false">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          
          <div class="edit-body glass-body">
            <!-- 头像预览区 -->
            <div class="avatar-preview-section">
              <div class="avatar-preview-ring">
                <img :src="editForm.avatar" alt="头像预览" />
                <div class="avatar-edit-overlay" @click="handleUploadClick">
                  <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                    <circle cx="12" cy="13" r="4"/>
                  </svg>
                  <span>更换</span>
                </div>
              </div>
            </div>
            
            <!-- 表单区 -->
            <div class="form-section">
              <div class="form-group glass-form-group">
                <label class="floating-label">
                  <span class="label-icon">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                  </span>
                  昵称
                </label>
                <input 
                  v-model="editForm.name" 
                  type="text" 
                  class="form-input glass-input" 
                  placeholder="输入你的昵称"
                  maxlength="20"
                />
                <span class="char-count">{{ editForm.name?.length || 0 }}/20</span>
              </div>
              
              <div class="form-group glass-form-group">
                <label class="floating-label">
                  <span class="label-icon">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                      <line x1="21" y1="10" x2="3" y2="10"/>
                      <line x1="21" y1="6" x2="3" y2="6"/>
                      <line x1="21" y1="14" x2="3" y2="14"/>
                      <line x1="21" y1="18" x2="3" y2="18"/>
                    </svg>
                  </span>
                  个人简介
                </label>
                <textarea 
                  v-model="editForm.bio" 
                  class="form-textarea glass-textarea" 
                  rows="3" 
                  placeholder="用一句话介绍自己..."
                  maxlength="100"
                ></textarea>
                <span class="char-count">{{ editForm.bio?.length || 0 }}/100</span>
              </div>
              
              <div class="form-group glass-form-group">
                <label class="floating-label">
                  <span class="label-icon">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                    </svg>
                  </span>
                  头像链接
                </label>
                <input 
                  v-model="editForm.avatar" 
                  type="text" 
                  class="form-input glass-input" 
                  placeholder="https://..."
                />
              </div>
            </div>
          </div>
          
          <div class="edit-footer glass-footer">
            <button class="detail-btn glass-secondary" @click="showDetailEdit = true; showEditModal = false">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="3"/>
                <path d="M12 1v6m0 6v6m4.22-10.22l4.24-4.24M6.34 6.34L2.1 2.1m17.8 17.8l-4.24-4.24M6.34 17.66l-4.24 4.24M23 12h-6m-6 0H1m20.24-4.24l-4.24 4.24M6.34 6.34l-4.24-4.24"/>
              </svg>
              更多设置
            </button>
            <button class="save-btn glass-primary" @click="handleSave">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              保存更改
            </button>
          </div>
        </div>
      </div>
    </Transition>
    
    <!-- 详细编辑页面 -->
    <Transition name="slide">
      <div v-if="showDetailEdit" class="detail-edit-page">
        <div class="detail-edit-container">
          <div class="detail-edit-header">
            <h2>编辑个人资料</h2>
            <button class="close-btn" @click="showDetailEdit = false">
              <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          
          <div class="detail-edit-body">
            <!-- 头像编辑 -->
            <div class="avatar-edit-section">
              <div class="avatar-preview-large">
                <img 
                  :src="editForm.avatar || 'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Byte'" 
                  alt="头像预览"
                  @error="handleAvatarError"
                />
                <div v-if="!editForm.avatar" class="avatar-preview-placeholder">
                  <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <div v-if="isUploading" class="upload-overlay">
                  <div class="upload-spinner"></div>
                  <span>上传中...</span>
                </div>
              </div>
              <div class="avatar-actions">
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  style="display: none"
                  @change="handleFileChange"
                />
                <button 
                  class="avatar-btn primary" 
                  @click="handleUploadClick"
                  :disabled="isUploading"
                >
                  {{ isUploading ? '上传中...' : '上传新头像' }}
                </button>
                <button class="avatar-btn secondary" @click="editForm.avatar = 'https://api.dicebear.com/9.x/fun-emoji/svg?seed=' + Math.random()">随机头像</button>
              </div>
            </div>
            
            <!-- 表单 -->
            <div class="detail-form">
              <div class="form-row">
                <div class="form-group half">
                  <label>昵称</label>
                  <input v-model="editForm.name" type="text" class="form-input" placeholder="你的昵称" />
                </div>
                <div class="form-group half">
                  <label>邮箱</label>
                  <input v-model="editForm.email" type="email" class="form-input" placeholder="your@email.com" />
                </div>
              </div>
              
              <div class="form-group">
                <label>个人简介</label>
                <textarea v-model="editForm.bio" class="form-textarea" rows="4" placeholder="介绍一下你自己..."></textarea>
              </div>
              
              <div class="form-row">
                <div class="form-group half">
                  <label>GitHub</label>
                  <input v-model="editForm.github" type="text" class="form-input" placeholder="github.com/username" />
                </div>
                <div class="form-group half">
                  <label>个人网站</label>
                  <input v-model="editForm.website" type="text" class="form-input" placeholder="yourwebsite.com" />
                </div>
              </div>
              
              <div class="form-group">
                <label>头像链接</label>
                <input v-model="editForm.avatar" type="text" class="form-input" placeholder="https://..." />
              </div>
            </div>
          </div>
          
          <div class="detail-edit-footer">
            <button class="cancel-btn" @click="showDetailEdit = false">取消</button>
            <button class="save-btn large" @click="handleSave">保存更改</button>
          </div>
        </div>
      </div>
    </Transition>
    
    <!-- 心情设置弹窗 -->
    <Transition name="modal">
      <div v-if="showStatusModal" class="status-modal" @click.self="showStatusModal = false">
        <div class="status-modal-container">
          <div class="status-modal-header">
            <h3>设置状态</h3>
            <button class="close-btn" @click="showStatusModal = false">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          
          <div class="status-modal-body">
            <!-- 心情选择 -->
            <div class="status-section">
              <h4>选择心情</h4>
              <div class="mood-grid">
                <div 
                  v-for="mood in moodEmojis" 
                  :key="mood.value"
                  class="mood-item"
                  :class="{ active: userStatus.mood === mood.value }"
                  @click="selectMood(mood)"
                >
                  <span class="mood-emoji">{{ mood.emoji }}</span>
                  <span class="mood-label">{{ mood.label }}</span>
                </div>
              </div>
            </div>
            
            <!-- 忙碌模式 -->
            <div class="status-section">
              <div class="busy-header">
                <h4>忙碌模式</h4>
                <label class="busy-switch">
                  <input type="checkbox" v-model="userStatus.isBusy" @change="toggleBusy" />
                  <span class="switch-slider"></span>
                </label>
              </div>
              <p class="busy-desc">开启后将暂时取消所有消息提醒，并自动回复发送消息的用户</p>
              
              <div v-if="userStatus.isBusy" class="busy-settings">
                <div class="busy-duration">
                  <label>忙碌时长</label>
                  <div class="duration-options">
                    <button 
                      v-for="opt in busyDurationOptions" 
                      :key="opt.value"
                      class="duration-btn"
                      :class="{ active: selectedBusyDuration === opt.value }"
                      @click="selectedBusyDuration = opt.value"
                    >
                      {{ opt.label }}
                    </button>
                  </div>
                  <input 
                    v-if="selectedBusyDuration === 'custom'"
                    v-model.number="customBusyMinutes"
                    type="number"
                    class="custom-duration-input"
                    min="1"
                    max="480"
                    placeholder="分钟"
                  />
                </div>
                
                <div class="auto-reply">
                  <label>自动回复语</label>
                  <textarea 
                    v-model="userStatus.busyAutoReply" 
                    class="auto-reply-input"
                    rows="3"
                    placeholder="设置自动回复的内容..."
                  ></textarea>
                  <p class="auto-reply-hint">当其他用户给你发送消息时，将自动回复此内容</p>
                </div>
                
                <div v-if="formatRemainingTime" class="remaining-time">
                  <span class="time-icon">⏱️</span>
                  <span>剩余时间：{{ formatRemainingTime }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="status-modal-footer">
            <button class="cancel-btn" @click="showStatusModal = false">取消</button>
            <button class="save-btn" @click="saveStatusSettings">保存设置</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.github-profile-page {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Noto Sans SC', Helvetica, Arial, sans-serif;
  background: #ffffff;
  min-height: 100vh;
  padding: 24px 32px;
}

.profile-container {
  max-width: 1280px;
  margin: 0 auto;
  display: flex;
  gap: 32px;
}

/* 左侧边栏 */
.profile-sidebar {
  width: 296px;
  flex-shrink: 0;
}

.avatar-section {
  margin-bottom: 16px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  max-width: 100%;
}

.avatar-container {
  width: 296px;
  height: 296px;
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  background: #f0f6fa;
  border: 1px solid #d0d7de;
}

.avatar-wrapper img {
  max-width: 100%;
  height: auto;
}

.profile-avatar {
  width: 296px;
  height: 296px;
  border-radius: 50%;
  border: none;
  object-fit: cover;
  transition: filter 0.3s ease;
  display: block;
}

.avatar-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0e4e8 0%, #f0f6fa 100%);
  color: #8c959f;
  pointer-events: none;
}

.avatar-placeholder svg {
  opacity: 0.5;
}

.profile-avatar {
  width: 296px;
  height: 296px;
  border-radius: 50%;
  border: 1px solid #d0d7de;
  object-fit: cover;
  transition: filter 0.3s ease;
}

.avatar-wrapper:hover .profile-avatar {
  filter: brightness(1);
}

.avatar-hint {
  position: absolute;
  bottom: -24px;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  font-size: 12px;
  color: #656d76;
  margin: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  opacity: 0;
  white-space: nowrap;
}

.avatar-wrapper:hover .avatar-hint {
  opacity: 1;
}

.avatar-hint:hover {
  color: #0969da;
}

.status-emoji:hover {
  transform: scale(1.1);
}

.profile-info {
  margin-bottom: 16px;
}

.profile-name {
  font-size: 26px;
  font-weight: 600;
  color: #1f2328;
  margin: 0 0 4px;
  line-height: 1.25;
}

.profile-username {
  font-size: 20px;
  font-weight: 300;
  color: #656d76;
  margin: 0 0 16px;
  line-height: 1.5;
}

.profile-bio {
  font-size: 16px;
  color: #1f2328;
  margin: 0 0 16px;
  line-height: 1.5;
}

.edit-profile-btn {
  width: 100%;
  padding: 10px 16px;
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #24292f;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 16px;
}

.edit-profile-btn:hover {
  background: #f3f4f6;
  border-color: #d0d7de;
}

.profile-stats {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #1f2328;
}

.stat-item svg {
  color: #656d76;
}

.stat-label {
  color: #656d76;
}

.profile-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1f2328;
}

.detail-item svg {
  color: #656d76;
}

/* 右侧主内容 */
.profile-main {
  flex: 1;
  min-width: 0;
}

/* 题库部分 */
.repositories-section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.repo-tabs {
  display: flex;
  gap: 8px;
}

.repo-tab {
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #656d76;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.repo-tab:hover {
  background: #f6f8fa;
  color: #1f2328;
}

.repo-tab.active {
  background: #0969da;
  color: white;
}

.tab-count {
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 12px;
}

.repo-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.2);
}

.customize-link {
  font-size: 12px;
  color: #0969da;
  text-decoration: none;
}

.customize-link:hover {
  text-decoration: underline;
}

.repo-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.repo-card {
  padding: 16px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  background: #ffffff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.repo-card:hover {
  border-color: #0969da;
  box-shadow: 0 2px 8px rgba(9, 105, 218, 0.1);
}

.repo-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.repo-name {
  font-size: 14px;
  font-weight: 600;
  color: #0969da;
  text-decoration: none;
}

.repo-name:hover {
  text-decoration: underline;
}

.repo-badge {
  padding: 2px 8px;
  border: 1px solid #d0d7de;
  border-radius: 12px;
  font-size: 12px;
  color: #656d76;
  font-weight: 500;
}

.repo-description {
  font-size: 12px;
  color: #656d76;
  margin: 0 0 16px;
  line-height: 1.5;
}

.repo-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.repo-difficulty {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #656d76;
}

.difficulty-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.repo-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #656d76;
}

.repo-meta svg {
  color: #656d76;
}

/* 做题统计部分 */
.contributions-section {
  margin-bottom: 32px;
}

.contributions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.contributions-header h3 {
  font-size: 16px;
  font-weight: 400;
  color: #1f2328;
  margin: 0;
}

.stats-summary {
  display: flex;
  gap: 8px;
}

.stat-badge {
  padding: 4px 10px;
  background: #ddf4ff;
  border-radius: 12px;
  font-size: 12px;
  color: #0969da;
  font-weight: 500;
}

.contributions-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.contributions-calendar {
  position: relative;
  padding: 8px 0;
  overflow-x: auto;
  flex: 1;
}

.months-row {
  position: relative;
  height: 20px;
  margin-bottom: 4px;
}

.month-label {
  position: absolute;
  font-size: 12px;
  color: #656d76;
}

.calendar-body {
  display: flex;
  gap: 4px;
}

.weekdays-col {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding-top: 2px;
}

.weekday-label {
  height: 10px;
  font-size: 10px;
  color: #656d76;
  line-height: 10px;
}

.contributions-grid {
  display: flex;
  gap: 3px;
}

.week-column {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.contribution-cell {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background: #ebedf0;
  cursor: pointer;
  transition: outline 0.1s ease;
}

.contribution-cell:hover {
  outline: 1px solid rgba(0,0,0,0.2);
  outline-offset: 1px;
}

.contribution-cell.level-0 { background: #ebedf0; }
.contribution-cell.level-1 { background: #9be9a8; }
.contribution-cell.level-2 { background: #40c463; }
.contribution-cell.level-3 { background: #30a14e; }
.contribution-cell.level-4 { background: #216e39; }

.contributions-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #656d76;
}

.legend-cells {
  display: flex;
  gap: 3px;
}

.legend-cell {
  width: 10px;
  height: 10px;
  border-radius: 2px;
}

.legend-cell.level-0 { background: #ebedf0; }
.legend-cell.level-1 { background: #9be9a8; }
.legend-cell.level-2 { background: #40c463; }
.legend-cell.level-3 { background: #30a14e; }
.legend-cell.level-4 { background: #216e39; }

.year-selector {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 80px;
}

.year-btn {
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  color: #656d76;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s ease;
}

.year-btn:hover {
  background: #f6f8fa;
}

.year-btn.active {
  background: #0969da;
  color: white;
  font-weight: 600;
}

/* 活动部分 */
.activity-section {
  margin-bottom: 32px;
}

.activity-section h3 {
  font-size: 16px;
  font-weight: 400;
  color: #1f2328;
  margin: 0 0 16px;
}

.activity-timeline {
  border-left: 2px solid #d0d7de;
  margin-left: 8px;
  padding-left: 16px;
}

.activity-date {
  font-size: 14px;
  font-weight: 600;
  color: #1f2328;
  margin-bottom: 12px;
  margin-left: -20px;
}

.activity-date::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #d0d7de;
  border-radius: 50%;
  margin-right: 8px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-activity {
  padding: 16px 0;
  color: #656d76;
  font-size: 14px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1f2328;
}

.activity-icon {
  font-size: 16px;
}

.activity-text {
  flex: 1;
}

.activity-time {
  color: #656d76;
  font-size: 12px;
}

.show-more-btn {
  width: 100%;
  padding: 12px;
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #0969da;
  cursor: pointer;
  margin-top: 16px;
}

.show-more-btn:hover {
  background: #f3f4f6;
}

/* 快速编辑弹窗 */
.edit-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 24px;
}

.edit-container {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #d0d7de;
}

.edit-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2328;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #656d76;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.close-btn:hover {
  background: #f6f8fa;
}

.edit-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: #1f2328;
}

.form-input,
.form-textarea {
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  color: #1f2328;
  outline: none;
}

.form-input:focus,
.form-textarea:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.edit-footer {
  padding: 16px 20px;
  border-top: 1px solid #d0d7de;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.save-btn {
  padding: 8px 16px;
  background: #2da44e;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
}

.save-btn:hover {
  background: #2c974b;
}

.save-btn.large {
  padding: 10px 24px;
  font-size: 16px;
}

.detail-btn {
  padding: 8px 16px;
  background: transparent;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #656d76;
  cursor: pointer;
}

.detail-btn:hover {
  background: #f6f8fa;
}

/* 详细编辑页面 */
.detail-edit-page {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1001;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.detail-edit-container {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 640px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.detail-edit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #d0d7de;
}

.detail-edit-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1f2328;
  margin: 0;
}

.detail-edit-body {
  padding: 24px;
}

.avatar-edit-section {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #d0d7de;
}

.avatar-preview-large {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #d0d7de;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f6fa;
}

.avatar-preview-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-preview-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0e4e8 0%, #f0f6fa 100%);
  color: #8c959f;
}

.avatar-preview-placeholder svg {
  opacity: 0.5;
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
}

.upload-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 8px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.avatar-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.avatar-btn.primary {
  background: #0969da;
  border: none;
  color: white;
}

.avatar-btn.primary:hover {
  background: #0550ae;
}

.avatar-btn.secondary {
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  color: #24292f;
}

.avatar-btn.secondary:hover {
  background: #f3f4f6;
}

.detail-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-group.half {
  flex: 1;
}

.detail-edit-footer {
  padding: 20px 24px;
  border-top: 1px solid #d0d7de;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn {
  padding: 10px 20px;
  background: transparent;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #656d76;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #f6f8fa;
}

/* 状态emoji样式增强 */
.status-emoji {
  position: absolute;
  bottom: 20px;
  right: 20px;
  width: 38px;
  height: 38px;
  background: white;
  border: 1px solid #d0d7de;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.status-emoji:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.status-emoji.busy {
  border-color: #f0883e;
  background: #fff8f0;
}

.busy-indicator {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #f0883e;
  border: 2px solid white;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

/* ===== 玻璃拟态编辑弹窗样式 ===== */
.glass-modal {
  background: rgba(0, 0, 0, 0.6) !important;
  backdrop-filter: blur(8px);
}

.glass-container {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  border-radius: 20px !important;
  overflow: hidden;
}

.glass-header {
  background: linear-gradient(135deg, var(--brand-blue) 0%, var(--brand-accent) 100%);
  color: white;
  padding: 24px !important;
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.glass-header h3 {
  color: white;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  flex: 1;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-icon svg {
  color: white;
}

.glass-close {
  background: rgba(255, 255, 255, 0.2) !important;
  border: none !important;
  color: white !important;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.glass-close:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  transform: rotate(90deg);
}

.glass-body {
  padding: 24px !important;
}

/* 头像预览区 */
.avatar-preview-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.avatar-preview-ring {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  padding: 4px;
  background: linear-gradient(135deg, var(--brand-blue) 0%, var(--brand-accent) 100%);
  cursor: pointer;
  transition: transform 0.3s ease;
}

.avatar-preview-ring:hover {
  transform: scale(1.05);
}

.avatar-preview-ring img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
}

.avatar-edit-overlay {
  position: absolute;
  top: 4px;
  left: 4px;
  right: 4px;
  bottom: 4px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-preview-ring:hover .avatar-edit-overlay {
  opacity: 1;
}

.avatar-edit-overlay svg {
  margin-bottom: 4px;
}

/* 表单样式 */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.glass-form-group {
  position: relative;
}

.floating-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #656d76;
  margin-bottom: 8px;
}

.label-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--brand-blue);
}

.glass-input,
.glass-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e1e4e8;
  border-radius: 12px;
  font-size: 15px;
  background: #fafbfc;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.glass-input:focus,
.glass-textarea:focus {
  outline: none;
  border-color: var(--brand-blue);
  background: white;
  box-shadow: 0 0 0 4px rgba(74, 111, 157, 0.1);
}

.glass-textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: -18px;
  font-size: 11px;
  color: #8c959f;
}

/* 底部按钮 */
.glass-footer {
  padding: 20px 24px !important;
  background: #f6f8fa;
  border-top: 1px solid #e1e4e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.glass-primary {
  background: linear-gradient(135deg, var(--brand-blue) 0%, var(--brand-accent) 100%) !important;
  color: white !important;
  border: none !important;
  padding: 12px 24px !important;
  border-radius: 10px !important;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.3);
}

.glass-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 111, 157, 0.4);
}

.glass-secondary {
  background: transparent !important;
  color: #656d76 !important;
  border: 1px solid #d0d7de !important;
  padding: 12px 20px !important;
  border-radius: 10px !important;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.glass-secondary:hover {
  background: #f3f4f6 !important;
  border-color: var(--brand-blue) !important;
  color: var(--brand-blue) !important;
}

/* 心情设置弹窗 */
.status-modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1002;
  padding: 24px;
}

.status-modal-container {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.status-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #d0d7de;
}

.status-modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1f2328;
  margin: 0;
}

.status-modal-body {
  padding: 24px;
}

.status-section {
  margin-bottom: 24px;
}

.status-section:last-child {
  margin-bottom: 0;
}

.status-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1f2328;
  margin: 0 0 12px;
}

.mood-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.mood-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  border: 1px solid #d0d7de;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mood-item:hover {
  border-color: #0969da;
  background: #f6f8fa;
}

.mood-item.active {
  border-color: #0969da;
  background: #ddf4ff;
}

.mood-emoji {
  font-size: 24px;
  margin-bottom: 4px;
}

.mood-label {
  font-size: 12px;
  color: #656d76;
}

.mood-item.active .mood-label {
  color: #0969da;
  font-weight: 500;
}

.busy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.busy-header h4 {
  margin: 0;
}

.busy-desc {
  font-size: 13px;
  color: #656d76;
  margin: 0 0 16px;
}

.busy-switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 26px;
}

.busy-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: #d0d7de;
  border-radius: 26px;
  transition: 0.3s;
}

.switch-slider::before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
}

.busy-switch input:checked + .switch-slider {
  background: #2da44e;
}

.busy-switch input:checked + .switch-slider::before {
  transform: translateX(22px);
}

.busy-settings {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #d0d7de;
}

.busy-duration {
  margin-bottom: 16px;
}

.busy-duration label,
.auto-reply label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #1f2328;
  margin-bottom: 8px;
}

.duration-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.duration-btn {
  padding: 6px 12px;
  background: #f6f8fa;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 13px;
  color: #656d76;
  cursor: pointer;
  transition: all 0.2s ease;
}

.duration-btn:hover {
  border-color: #0969da;
  color: #0969da;
}

.duration-btn.active {
  background: #0969da;
  border-color: #0969da;
  color: white;
}

.custom-duration-input {
  margin-top: 8px;
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.custom-duration-input:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.auto-reply {
  margin-bottom: 16px;
}

.auto-reply-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d0d7de;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  font-family: inherit;
}

.auto-reply-input:focus {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.auto-reply-hint {
  font-size: 12px;
  color: #656d76;
  margin: 8px 0 0;
}

.remaining-time {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff8f0;
  border: 1px solid #f0883e;
  border-radius: 8px;
  font-size: 14px;
  color: #f0883e;
}

.time-icon {
  font-size: 18px;
}

.status-modal-footer {
  padding: 20px 24px;
  border-top: 1px solid #d0d7de;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.status-modal-footer .save-btn {
  padding: 10px 20px;
  background: #2da44e;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
}

.status-modal-footer .save-btn:hover {
  background: #2c974b;
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-enter-from {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

.slide-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

/* 响应式 */
@media (max-width: 1012px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
  
  .avatar-container {
    width: 120px;
    height: 120px;
  }
  
  .profile-avatar {
    width: 120px;
    height: 120px;
  }
  
  .avatar-placeholder svg {
    width: 32px;
    height: 32px;
  }
  
  .status-emoji {
    width: 32px;
    height: 32px;
    font-size: 16px;
    bottom: 8px;
    right: 8px;
  }
  
  .repo-grid {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .github-profile-page {
    padding: 16px;
  }
  
  .repo-grid {
    grid-template-columns: 1fr;
  }
  
  .contributions-calendar {
    overflow-x: scroll;
  }
  
  .avatar-edit-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
