<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { getTrackLabel } from './constants'
import AppFooter from './components/AppFooter.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showTopbar = computed(() => !['/', '/register'].includes(route.path))

const menuItems = [
  { path: '/home', label: '学习计划' },
  { path: '/courses', label: '网课' },
  { path: '/projects', label: '项目实战' },
  { path: '/forum', label: '论坛' },
  { path: '/ranking', label: '积分排行' },
  { path: '/errors', label: '错题本' },
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

const toggleTheme = () => {
  themeIndex.value = themeIndex.value >= 6 ? 2 : themeIndex.value + 1
  localStorage.setItem('themeIndex', themeIndex.value.toString())
}
</script>

<template>
  <div :class="['app-shell', `theme-${themeIndex}`]">
    <header v-if="showTopbar" class="topbar">
      <div class="page-container topbar-inner">
        <div class="brand" @click="handleMenuSelect('/home')">
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
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/courses">网课</el-menu-item>
          <el-menu-item index="/projects">项目实战</el-menu-item>
          <el-menu-item index="/forum">论坛</el-menu-item>
          <el-menu-item index="/ranking">积分排行</el-menu-item>
          <el-menu-item index="/errors">错题本</el-menu-item>
        </el-menu>
        <div class="nav-search-wrapper" :class="{ 'is-expanded': navSearchExpanded }">
          <div class="nav-search-box">
            <button class="nav-search-btn" @click="toggleNavSearch">
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
                <button v-if="navSearchKeyword" class="nav-clear-btn" @click.stop="clearNavSearch">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5"
                    stroke-linecap="round">
                    <path d="M18 6 6 18M6 6l12 12" />
                  </svg>
                </button>
                <button class="nav-go-btn" @click="handleNavSearch">
                  搜索
                </button>
              </div>
            </Transition>
          </div>
        </div>
        <div class="actions">
          <button class="theme-toggle-btn" @click="toggleTheme" title="切换背景主题">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
              stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
              <circle cx="8.5" cy="8.5" r="1.5" />
              <polyline points="21 15 16 10 5 21" />
            </svg>
          </button>
          <el-tag type="success" effect="light">积分 {{ userStore.points }}</el-tag>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-entry">
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
                <el-dropdown-item command="profile">个人主页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <div v-if="showTopbar" class="mobile-menu-wrapper hidden-on-desktop">
      <div class="mobile-menu">
        <div v-for="item in menuItems" :key="item.path"
          :class="['mobile-menu-item', { active: activePath === item.path }]" @click="handleMenuSelect(item.path)">
          {{ item.label }}
        </div>
      </div>
    </div>

    <main :class="['app-main', { 'with-topbar': showTopbar }]">
      <RouterView />
    </main>

    <!-- 页脚 -->
    <AppFooter />
  </div>
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

@media (max-width: 680px) {
  .topbar {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 20;
  }

  .topbar-inner {
    height: 56px;
    flex-wrap: nowrap;
    padding: 0 12px;
    gap: 8px;
  }

  .brand {
    flex-shrink: 0;
  }

  .brand-text {
    font-size: 18px;
  }

  .main-menu {
    display: none;
  }

  .nav-search-wrapper {
    display: flex;
    margin-left: auto;
  }

  .nav-search-btn {
    width: 32px;
    height: 32px;
    border-radius: 8px;
  }

  .nav-search-input-wrap {
    position: absolute;
    right: 12px;
    top: 50px;
    z-index: 100;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }

  .nav-search-input {
    width: 140px;
  }

  .hidden-on-desktop {
    display: block;
  }

  .mobile-menu-wrapper {
    position: fixed;
    top: 56px;
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
    padding: 8px 12px;
    gap: 4px;
    min-width: min-content;
  }

  .mobile-menu-item {
    flex-shrink: 0;
    padding: 8px 16px;
    font-size: 14px;
    color: var(--text-sub);
    cursor: pointer;
    border-radius: 20px;
    white-space: nowrap;
    transition: all 0.2s;
  }

  .mobile-menu-item:hover {
    background: rgba(64, 158, 255, 0.1);
    color: var(--brand-blue);
  }

  .mobile-menu-item.active {
    background: var(--brand-blue);
    color: #fff;
  }

  .actions {
    gap: 4px;
  }

  .actions :deep(.el-tag) {
    display: none;
  }

  .user-name {
    display: none;
  }

  .app-main.with-topbar {
    padding-top: 112px;
  }
}
</style>
