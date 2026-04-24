<script setup>
import { computed, ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { getTrackLabel } from './constants'
import AppFooter from './components/AppFooter.vue'
import AIAssistant from './components/AIAssistant.vue'
import FloatingAIAssistant from './components/FloatingAIAssistant.vue'
import MagneticPointer from './components/MagneticPointer.vue'
import MagneticPointerToggle from './components/MagneticPointerToggle.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAuthPage = computed(() => ['/', '/register'].includes(route.path))
const showTopbar = computed(() => !isAuthPage.value)
const showFloatingAssistant = computed(() => !!userStore.userInfo?.token && !isAuthPage.value)
const canUseTextAnalysis = computed(() => !!userStore.userInfo?.token && !isAuthPage.value)
const showMagneticPointerToggle = computed(() => route.path.startsWith('/knowledge-base'))

const menuItems = [
  { path: '/home', label: '学习计划' },
  { path: '/courses', label: '网课' },
  { path: '/projects', label: '项目实战' },
  { path: '/forum', label: '论坛' },
  { path: '/private-message', label: '消息' },
  { path: '/ranking', label: '积分排行' },
  { path: '/errors', label: '错题本' },
  { path: '/code-history', label: '历史提交' },
]

const activePath = computed(() => {
  if (route.path.startsWith('/challenge/') || route.path.startsWith('/levels')) {
    return '/projects'
  }
  return route.path
})

const trackLabel = computed(() => getTrackLabel(userStore.userInfo?.targetTrack || userStore.selectedTrack))

const handleMenuSelect = (path) => {
  if (path !== route.path) {
    router.push(path)
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}

const navSearchKeyword = ref('')
const navSearchExpanded = ref(false)

const toggleNavSearch = () => {
  if (navSearchExpanded.value) {
    navSearchExpanded.value = false
  } else {
    navSearchExpanded.value = true
    setTimeout(() => {
      const input = document.querySelector('.nav-search-input')
      if (input) input.focus()
    }, 100)
  }
}

const collapseNavSearch = () => {
  if (!navSearchKeyword.value) {
    navSearchExpanded.value = false
  }
}

const handleNavSearch = () => {
  if (navSearchKeyword.value.trim()) {
    router.push({
      path: '/forum',
      query: { search: navSearchKeyword.value.trim() }
    })
  }
}

const clearNavSearch = () => {
  navSearchKeyword.value = ''
  navSearchExpanded.value = false
}

const themeIndex = ref(parseInt(localStorage.getItem('themeIndex') || '2', 10))
if (themeIndex.value < 2 || themeIndex.value > 6) {
  themeIndex.value = 2
}

const selectedText = ref('')
const showTextAnalysisMenu = ref(false)
const textAnalysisMenuPosition = ref({ x: 0, y: 0 })
const textAnalysisMenuRef = ref(null)
const textAnalysisMenuAnchorRect = ref(null)

const clearBrowserSelection = () => {
  const selection = window.getSelection()
  if (selection && selection.rangeCount > 0) {
    selection.removeAllRanges()
  }
}

const hideTextAnalysisMenu = (clearSelection = false) => {
  showTextAnalysisMenu.value = false
  selectedText.value = ''
  textAnalysisMenuAnchorRect.value = null
  if (clearSelection) {
    clearBrowserSelection()
  }
}

const positionTextAnalysisMenu = async (rect) => {
  if (!rect) {
    return
  }

  const gap = 14
  const margin = 16
  const fallbackWidth = 320
  const fallbackHeight = 164

  await nextTick()

  const menuElement = textAnalysisMenuRef.value
  const menuWidth = menuElement?.offsetWidth || fallbackWidth
  const menuHeight = menuElement?.offsetHeight || fallbackHeight
  const viewportWidth = window.innerWidth
  const viewportHeight = window.innerHeight
  const safeMenuWidth = Math.min(menuWidth, Math.max(0, viewportWidth - margin * 2))
  const safeMenuHeight = Math.min(menuHeight, Math.max(0, viewportHeight - margin * 2))

  const centeredX = rect.left + rect.width / 2
  const minX = margin + safeMenuWidth / 2
  const maxX = viewportWidth - margin - safeMenuWidth / 2
  const nextX = Math.min(maxX, Math.max(minX, centeredX))

  const spaceBelow = viewportHeight - rect.bottom - gap - margin
  const spaceAbove = rect.top - gap - margin
  const canFitBelow = spaceBelow >= safeMenuHeight
  const canFitAbove = spaceAbove >= safeMenuHeight

  let nextY = rect.bottom + gap
  if (!canFitBelow && canFitAbove) {
    nextY = rect.top - safeMenuHeight - gap
  } else if (!canFitBelow && !canFitAbove) {
    nextY = spaceBelow >= spaceAbove
      ? viewportHeight - safeMenuHeight - margin
      : margin
  }

  textAnalysisMenuPosition.value = {
    x: nextX,
    y: Math.min(viewportHeight - safeMenuHeight - margin, Math.max(margin, nextY)),
  }
}

// 文本选择监听器
const handleSelectionChange = () => {
  if (!canUseTextAnalysis.value) {
    hideTextAnalysisMenu()
    return
  }

  const selection = window.getSelection()
  const selectedTextValue = selection?.toString().trim() || ''

  if (selectedTextValue && selectedTextValue.length > 0) {
    selectedText.value = selectedTextValue
    const range = selection.getRangeAt(0)
    const rect = range.getBoundingClientRect()
    textAnalysisMenuAnchorRect.value = rect
    showTextAnalysisMenu.value = true
    positionTextAnalysisMenu(rect)
    return
  }

  const activeElement = document.activeElement
  const menuContainsFocus = Boolean(
    textAnalysisMenuRef.value
    && activeElement
    && textAnalysisMenuRef.value.contains(activeElement),
  )

  if (!menuContainsFocus) {
    hideTextAnalysisMenu()
  }
}

const aiAssistantRef = ref(null)

// 分析选中文本
const analyzeSelectedText = (type) => {
  if (!canUseTextAnalysis.value || !selectedText.value || !aiAssistantRef.value) return

  aiAssistantRef.value.analyzeSelectedText(selectedText.value, type)

  hideTextAnalysisMenu(true)
}

const handleTextAnalysisMenuPointerDown = (event) => {
  event.preventDefault()
}

const handleDocumentPointerDown = (event) => {
  if (!canUseTextAnalysis.value || !showTextAnalysisMenu.value) {
    return
  }

  const menuElement = textAnalysisMenuRef.value
  if (menuElement && menuElement.contains(event.target)) {
    return
  }

  hideTextAnalysisMenu(true)
}

const handleWindowResize = () => {
  if (!canUseTextAnalysis.value || !showTextAnalysisMenu.value || !textAnalysisMenuAnchorRect.value) {
    return
  }
  positionTextAnalysisMenu(textAnalysisMenuAnchorRect.value)
}

// 快捷键处理
const handleKeyDown = (e) => {
  if (!canUseTextAnalysis.value) {
    return
  }

  if (e.ctrlKey && showTextAnalysisMenu.value) {
    if (e.key === '1') {
      e.preventDefault()
      analyzeSelectedText('correctness')
    } else if (e.key === '2') {
      e.preventDefault()
      analyzeSelectedText('meaning')
    }
  }
}

// 监听文本选择
onMounted(() => {
  document.addEventListener('selectionchange', handleSelectionChange)
  document.addEventListener('pointerdown', handleDocumentPointerDown, true)
  document.addEventListener('keydown', handleKeyDown)
  window.addEventListener('resize', handleWindowResize)
})

onUnmounted(() => {
  document.removeEventListener('selectionchange', handleSelectionChange)
  document.removeEventListener('pointerdown', handleDocumentPointerDown, true)
  document.removeEventListener('keydown', handleKeyDown)
  window.removeEventListener('resize', handleWindowResize)
})

watch(canUseTextAnalysis, (enabled) => {
  if (!enabled) {
    hideTextAnalysisMenu(true)
  }
})

const toggleTheme = () => {
  themeIndex.value = themeIndex.value >= 6 ? 2 : themeIndex.value + 1
  localStorage.setItem('themeIndex', themeIndex.value.toString())
}
</script>

<template>
  <div :class="['app-shell', `theme-${themeIndex}`]">
    <header v-if="showTopbar" class="topbar">
      <div class="page-container topbar-inner">
        <div class="brand _target" @click="handleMenuSelect('/home')">
          <div class="brand-icon">
            <svg viewBox="0 0 32 32" width="32" height="32" fill="none">
              <defs>
                <linearGradient id="algoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4a6f9d" />
                  <stop offset="100%" stop-color="#6672cb" />
                </linearGradient>
              </defs>
              <path d="M16 2L28 9V23L16 30L4 23V9L16 2Z" stroke="url(#algoGrad)" stroke-width="2" fill="none" />
              <path d="M16 10L22 13.5V20.5L16 24L10 20.5V13.5L16 10Z" fill="url(#algoGrad)" opacity="0.3" />
              <circle cx="16" cy="17" r="3" fill="url(#algoGrad)" />
            </svg>
          </div>
          <span class="brand-text"><span class="brand-algo">Algo</span><span class="brand-mind">Mind</span></span>
        </div>
        <el-menu class="main-menu" :default-active="activePath" mode="horizontal" @select="handleMenuSelect">
          <el-menu-item class="_target" index="/home">首页</el-menu-item>
          <el-sub-menu class="_target" index="courses-group">
            <template #title>知识专区</template>
            <el-menu-item class="_target" index="/courses">网课</el-menu-item>
            <el-menu-item class="_target" index="/knowledge-base">知识库</el-menu-item>
          </el-sub-menu>
          <el-menu-item class="_target" index="/projects">项目实战</el-menu-item>
          <el-menu-item class="_target" index="/forum">论坛</el-menu-item>
          <el-menu-item class="_target" index="/private-message">消息</el-menu-item>
          <el-menu-item class="_target" index="/ranking">积分排行</el-menu-item>
          <el-menu-item class="_target" index="/errors">错题本</el-menu-item>
          <el-menu-item class="_target" index="/code-history">历史提交</el-menu-item>
        </el-menu>
        <div class="nav-search-wrapper" :class="{ 'is-expanded': navSearchExpanded }">
          <div class="nav-search-box">
            <button class="nav-search-btn _target" @click="toggleNavSearch">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5"
                stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
            </button>
            <Transition name="search-expand">
              <div v-if="navSearchExpanded" class="nav-search-input-wrap">
                <input v-model="navSearchKeyword" type="text" class="nav-search-input" placeholder="搜索帖子..."
                  @keyup.enter="handleNavSearch" @keyup.esc="collapseNavSearch" />
                <button v-if="navSearchKeyword" class="nav-clear-btn _target" @click.stop="clearNavSearch">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"
                    stroke-linecap="round">
                    <path d="M18 6 6 18M6 6l12 12" />
                  </svg>
                </button>
                <button class="nav-go-btn _target" @click="handleNavSearch">
                  搜索
                </button>
              </div>
            </Transition>
          </div>
        </div>
        <div class="actions">
          <button class="theme-toggle-btn _target" @click="toggleTheme" title="切换背景主题">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <circle cx="8.5" cy="8.5" r="1.5" />
              <polyline points="21 15 16 10 5 21" />
            </svg>
          </button>
          <el-tag type="success" effect="light">积分 {{ userStore.points }}</el-tag>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-entry _target">
              <div class="avatar-with-pendant">
                <el-avatar :size="34" :src="userStore.avatar" :key="userStore.avatar" class="animated-avatar">
                  {{ (userStore.userInfo?.name || '同学').slice(0, 1) }}
                </el-avatar>
                <div class="avatar-pendant">
                  <svg viewBox="0 0 24 24" class="pendant-icon">
                    <polygon
                      points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                      fill="currentColor" />
                  </svg>
                </div>
                <div class="avatar-ring"></div>
                <div class="avatar-sparkles">
                  <span class="sparkle s1">✦</span>
                  <span class="sparkle s2">✦</span>
                  <span class="sparkle s3">✦</span>
                </div>
              </div>
              <span class="user-name">{{ userStore.userInfo?.name || '同学' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item class="_target" command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item class="_target" command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <div v-if="showTopbar" class="mobile-menu-wrapper hidden-on-desktop">
      <div class="mobile-menu">
        <div v-for="item in menuItems" :key="item.path"
          :class="['mobile-menu-item', '_target', { active: activePath === item.path }]" @click="handleMenuSelect(item.path)">
          {{ item.label }}
        </div>
      </div>
    </div>

    <main :class="['app-main', { 'with-topbar': showTopbar }]">
      <RouterView />
    </main>

    <!-- 页脚 -->
    <AppFooter />

    <!-- 文本分析浮动菜单 -->
    <transition name="menu-fade">
      <div v-if="canUseTextAnalysis && showTextAnalysisMenu && selectedText" ref="textAnalysisMenuRef" class="text-analysis-menu"
        :style="{ left: textAnalysisMenuPosition.x + 'px', top: textAnalysisMenuPosition.y + 'px' }"
        @pointerdown="handleTextAnalysisMenuPointerDown">
        <div class="selected-text-preview">
          <span class="preview-label">已选中：</span>
          <span class="preview-text">{{ selectedText.length > 50 ? selectedText.substring(0, 50) + '...' : selectedText
          }}</span>
        </div>
        <div class="menu-divider"></div>
        <button @click="analyzeSelectedText('correctness')" class="analysis-btn _target">
          <span class="btn-icon">🤔</span>
          <span class="btn-label">分析对错</span>
          <span class="btn-shortcut">Ctrl+1</span>
        </button>
        <button @click="analyzeSelectedText('meaning')" class="analysis-btn _target">
          <span class="btn-icon">💡</span>
          <span class="btn-label">解释含义</span>
          <span class="btn-shortcut">Ctrl+2</span>
        </button>
      </div>
    </transition>

    <!-- WebSocket AI悬浮窗助手 - 仅登录后显示 -->
    <FloatingAIAssistant v-if="showFloatingAssistant" ref="aiAssistantRef" />
  </div>
  <MagneticPointer />
  <MagneticPointerToggle v-if="showMagneticPointerToggle" />
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-size: cover;
  background-position: center top;
  background-attachment: fixed;
  background-repeat: no-repeat;
  transition: background-image 0.5s ease;
}

.theme-2 {
  background-image: linear-gradient(rgba(180, 180, 180, 0.4), rgba(180, 180, 180, 0.4)), url('@/assets/2.jpg');
}

.theme-3 {
  background-image: linear-gradient(rgba(180, 180, 180, 0.4), rgba(180, 180, 180, 0.4)), url('@/assets/3.jpg');
}

.theme-4 {
  background-image: linear-gradient(rgba(180, 180, 180, 0.4), rgba(180, 180, 180, 0.4)), url('@/assets/4.jpg');
}

.theme-5 {
  background-image: linear-gradient(rgba(180, 180, 180, 0.4), rgba(180, 180, 180, 0.4)), url('@/assets/5.jpg');
}

.theme-6 {
  background-image: linear-gradient(rgba(180, 180, 180, 0.4), rgba(180, 180, 180, 0.4)), url('@/assets/6.jpg');
}

.theme-toggle-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  transition: all 0.3s ease;
  margin-right: 8px;
}

.theme-toggle-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #3b82f6;
  transform: scale(1.1);
}

.app-main {
  flex: 1;
}

.topbar {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 20;
  background: var(--card-bg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.topbar-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 72px;
}

.brand {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.brand:hover {
  transform: scale(1.02);
}

.brand-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.brand:hover .brand-icon {
  transform: rotate(15deg);
}

.brand-text {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
  line-height: 1;
  display: flex;
  align-items: baseline;
}

.brand-algo {
  font-family: 'JetBrains Mono', 'SF Mono', 'Fira Code', monospace;
  color: var(--brand-blue);
  position: relative;
}

.brand-algo::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, var(--brand-blue), var(--brand-accent));
  transform: scaleX(0);
  transform-origin: right;
  transition: transform 0.3s ease;
}

.brand:hover .brand-algo::after {
  transform: scaleX(1);
  transform-origin: left;
}

.brand-mind {
  background: linear-gradient(135deg, var(--brand-accent) 0%, #8b5cf6 50%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
}

.main-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
  --el-menu-horizontal-height: 72px;
}

.main-menu :deep(.el-menu-item),
.main-menu :deep(.el-sub-menu__title) {
  height: 72px !important;
  line-height: 72px !important;
  box-sizing: border-box;
}

.main-menu :deep(.el-sub-menu__title) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  text-align: center !important;
  vertical-align: middle;
}

.main-menu :deep(.el-sub-menu__icon-arrow) {
  display: none !important;
  visibility: hidden !important;
  width: 0 !important;
  height: 0 !important;
  opacity: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  pointer-events: none !important;
}



.nav-search-wrapper {
  position: relative;
  flex-shrink: 0;
  margin-left: 8px;
}

.nav-search-box {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-search-btn {
  width: 30px;
  height: 30px;
  border: none;
  background: linear-gradient(135deg, #f0f4f8 0%, #e8ecf0 100%);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5a6a7a;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-search-btn:hover {
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(74, 144, 217, 0.35);
}

.nav-search-wrapper.is-expanded .nav-search-btn {
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
}

.nav-search-input-wrap {
  display: flex;
  align-items: center;
  background: white;
  border: 1.5px solid rgba(74, 144, 217, 0.3);
  border-radius: 8px;
  padding: 2px;
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.15);
}

.nav-search-input {
  width: 120px;
  padding: 4px 6px;
  border: none;
  outline: none;
  font-size: 12px;
  color: var(--text-main);
  background: transparent;
}

.nav-search-input::placeholder {
  color: var(--text-sub);
}

.nav-clear-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  transition: all 0.2s ease;
  margin-right: 4px;
}

.nav-clear-btn:hover {
  background: rgba(0, 0, 0, 0.12);
  color: var(--text-main);
}

.nav-go-btn {
  padding: 6px 14px;
  border: none;
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-go-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 144, 217, 0.4);
}

.search-expand-enter-active {
  animation: search-expand-in 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.search-expand-leave-active {
  animation: search-expand-in 0.2s ease-in reverse;
}

@keyframes search-expand-in {
  0% {
    opacity: 0;
    width: 0;
    transform: translateX(-10px);
  }

  100% {
    opacity: 1;
    width: 220px;
    transform: translateX(0);
  }
}

.actions {
  flex-shrink: 0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 6px;
  border-radius: 18px;
  cursor: pointer;
}

.user-entry:hover {
  background: #edf3fb;
}

.user-name {
  max-width: 96px;
  color: var(--text-title);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 头像动态挂件效果 */
.avatar-with-pendant {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.animated-avatar {
  transition: transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

.user-entry:hover .animated-avatar {
  transform: scale(1.1);
}

/* 星星挂件 */
.avatar-pendant {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  color: #fbbf24;
  z-index: 10;
  animation: pendantTwinkle 1.5s ease-in-out infinite;
  filter: drop-shadow(0 2px 4px rgba(251, 191, 36, 0.4));
}

.pendant-icon {
  width: 100%;
  height: 100%;
}

@keyframes pendantTwinkle {

  0%,
  100% {
    transform: scale(1) rotate(0deg);
  }

  50% {
    transform: scale(1.2) rotate(15deg);
  }
}

/* 光环效果 */
.avatar-ring {
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-ring::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 50%;
  padding: 2px;
  background: conic-gradient(from 0deg, #4a6f9d, #6672cb, #8b5cf6, #4a6f9d);
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: ringRotate 3s linear infinite;
}

.user-entry:hover .avatar-ring {
  opacity: 1;
}

@keyframes ringRotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* 闪烁星星 */
.avatar-sparkles {
  position: absolute;
  inset: -10px;
  pointer-events: none;
}

.sparkle {
  position: absolute;
  font-size: 10px;
  color: #fbbf24;
  opacity: 0;
  animation: sparkleFloat 2s ease-in-out infinite;
}

.sparkle.s1 {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  animation-delay: 0s;
}

.sparkle.s2 {
  bottom: 0;
  left: 0;
  animation-delay: 0.6s;
}

.sparkle.s3 {
  bottom: 0;
  right: 0;
  animation-delay: 1.2s;
}

.user-entry:hover .sparkle {
  opacity: 1;
}

@keyframes sparkleFloat {

  0%,
  100% {
    transform: translateY(0) scale(0.8);
    opacity: 0;
  }

  50% {
    transform: translateY(-8px) scale(1.2);
    opacity: 1;
  }
}

:deep(.main-menu.el-menu--horizontal > .el-menu-item) {
  color: var(--text-sub);
}

:deep(.main-menu.el-menu--horizontal > .el-menu-item.is-active) {
  color: var(--brand-blue);
}

.app-main.with-topbar {
  padding-top: 92px;
}

/* 文本分析菜单 */
.text-analysis-menu {
  position: fixed;
  z-index: 10020;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.34), rgba(255, 255, 255, 0.18)),
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.42), transparent 46%);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 18px;
  box-shadow:
    0 20px 48px rgba(15, 23, 42, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.38),
    inset 0 -1px 0 rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  min-width: min(280px, calc(100vw - 32px));
  width: min(360px, calc(100vw - 32px));
  max-width: calc(100vw - 32px);
  max-height: calc(100vh - 32px);
  transform: translate(-50%, 0);
  pointer-events: auto;
  overflow-x: hidden;
  overflow-y: auto;
  isolation: isolate;
}

.selected-text-preview {
  padding: 14px 16px 12px;
  background: linear-gradient(135deg, rgba(90, 167, 255, 0.14), rgba(139, 92, 246, 0.12));
  border-bottom: 1px solid rgba(255, 255, 255, 0.18);
}

.preview-label {
  display: block;
  font-size: 11px;
  color: rgba(71, 85, 105, 0.85);
  margin-bottom: 6px;
  font-weight: 600;
  letter-spacing: 0.06em;
}

.preview-text {
  display: -webkit-box;
  font-size: 13px;
  color: #0f172a;
  line-height: 1.5;
  word-break: break-word;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.menu-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.38), transparent);
  margin: 0;
}

.analysis-btn {
  padding: 13px 16px;
  border: none;
  background: transparent;
  color: #1e293b;
  font-size: 14px;
  cursor: pointer;
  text-align: left;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
}

.analysis-btn:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.24), rgba(148, 163, 184, 0.12));
  color: #1d4ed8;
  transform: translateX(2px);
}

.analysis-btn:active {
  background: linear-gradient(135deg, rgba(148, 163, 184, 0.18), rgba(59, 130, 246, 0.14));
}

.btn-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.btn-label {
  flex: 1;
  font-weight: 500;
}

.btn-shortcut {
  font-size: 11px;
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 999px;
  color: rgba(71, 85, 105, 0.82);
  font-family: monospace;
}

.analysis-btn:hover .btn-shortcut {
  background: rgba(255, 255, 255, 0.34);
  color: #1d4ed8;
}

/* 菜单淡入动画 */
.menu-fade-enter-active,
.menu-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -6px) scale(0.98);
}

.menu-fade-enter-to,
.menu-fade-leave-from {
  opacity: 1;
  transform: translate(-50%, 0) scale(1);
}

.assistant-fade-enter-active,
.assistant-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.assistant-fade-enter-from,
.assistant-fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.assistant-fade-enter-to,
.assistant-fade-leave-from {
  opacity: 1;
  transform: translateY(0);
}

.hidden-on-desktop {
  display: none;
}

@media (max-width: 925px) {
  .topbar-inner {
    flex-wrap: nowrap;
    gap: 12px;
  }

  .brand-text {
    font-size: 20px;
  }

  .main-menu {
    flex: 1;
  }

  :deep(.main-menu.el-menu--horizontal > .el-menu-item) {
    font-size: 13px;
    padding: 0 10px;
  }

  .nav-search-wrapper {
    display: flex;
  }

  .nav-search-btn {
    width: 32px;
    height: 32px;
    border-radius: 8px;
  }

  .nav-search-input-wrap {
    position: absolute;
    right: 0;
    top: 50px;
    z-index: 100;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  .nav-search-input {
    width: 150px;
  }

  .actions {
    gap: 6px;
  }

  .actions :deep(.el-tag) {
    padding: 0 6px;
    font-size: 12px;
  }
}

@media (max-width: 679px) {
  .topbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 20;
  }

  .topbar-inner {
    height: 52px;
    flex-wrap: nowrap;
    padding: 0 10px;
    gap: 6px;
  }

  .brand {
    flex-shrink: 0;
  }

  .brand-icon svg {
    width: 28px;
    height: 28px;
  }

  .brand-text {
    font-size: 16px;
  }

  .main-menu {
    display: none;
  }

  .nav-search-wrapper {
    display: flex;
    margin-left: auto;
  }

  .nav-search-btn {
    width: 30px;
    height: 30px;
    border-radius: 6px;
  }

  .nav-search-btn svg {
    width: 16px;
    height: 16px;
  }

  .nav-search-input-wrap {
    position: absolute;
    right: 10px;
    top: 46px;
    z-index: 100;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    max-width: calc(100vw - 20px);
  }

  .nav-search-input {
    width: 120px;
    min-width: 100px;
    max-width: calc(100vw - 120px);
  }

  .nav-go-btn {
    padding: 5px 10px;
    font-size: 12px;
  }

  .hidden-on-desktop {
    display: block;
  }

  .mobile-menu-wrapper {
    position: fixed;
    top: 52px;
    left: 0;
    right: 0;
    z-index: 19;
    background: var(--card-bg);
    border-bottom: 1px solid rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .mobile-menu-wrapper::-webkit-scrollbar {
    display: none;
  }

  .mobile-menu {
    display: flex;
    flex-wrap: nowrap;
    padding: 6px 10px;
    gap: 3px;
    min-width: min-content;
  }

  .mobile-menu-item {
    flex-shrink: 0;
    padding: 6px 12px;
    font-size: 13px;
    color: var(--text-sub);
    cursor: pointer;
    border-radius: 16px;
    white-space: nowrap;
    transition: all 0.2s ease;
    line-height: 1.4;
  }

  .mobile-menu-item:hover {
    background: rgba(64, 158, 255, 0.1);
    color: var(--brand-blue);
  }

  .mobile-menu-item.active {
    background: var(--brand-blue);
    color: #fff;
    box-shadow: 0 2px 8px rgba(74, 111, 157, 0.3);
  }

  .actions {
    gap: 3px;
  }

  .actions :deep(.el-tag) {
    display: none;
  }

  .theme-toggle-btn {
    width: 32px;
    height: 32px;
    margin-right: 4px;
  }

  .theme-toggle-btn svg {
    width: 18px;
    height: 18px;
  }

  .user-entry {
    padding: 3px;
    gap: 0;
  }

  .user-name {
    display: none;
  }

  .avatar-with-pendant .el-avatar {
    --el-avatar-size: 30px !important;
  }

  .avatar-pendant {
    width: 14px;
    height: 14px;
    top: -4px;
    right: -4px;
  }

  .app-main.with-topbar {
    padding-top: 104px;
  }
}

/* 超小屏幕适配 (320px - 480px) */
@media (max-width: 480px) {
  .topbar-inner {
    padding: 0 8px;
    gap: 4px;
  }

  .brand-text {
    font-size: 14px;
  }

  .brand-icon svg {
    width: 24px;
    height: 24px;
  }

  .mobile-menu {
    padding: 5px 8px;
    gap: 2px;
  }

  .mobile-menu-item {
    padding: 5px 10px;
    font-size: 12px;
    border-radius: 14px;
  }

  .nav-search-input {
    width: 100px;
    min-width: 80px;
  }

  .nav-go-btn {
    padding: 4px 8px;
    font-size: 11px;
  }

  .avatar-with-pendant .el-avatar {
    --el-avatar-size: 28px !important;
  }

  .avatar-pendant {
    width: 12px;
    height: 12px;
  }

  .app-main.with-topbar {
    padding-top: 98px;
  }
}

/* 极小屏幕适配 (320px以下) */
@media (max-width: 360px) {
  .topbar-inner {
    padding: 0 6px;
  }

  .brand-text {
    font-size: 13px;
  }

  .mobile-menu-item {
    padding: 4px 8px;
    font-size: 11px;
  }

  .nav-search-btn {
    width: 28px;
    height: 28px;
  }

  .nav-search-input {
    width: 80px;
    min-width: 60px;
  }
}
</style>