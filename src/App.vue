<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { getTrackLabel } from './constants'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showTopbar = computed(() => route.path !== '/')

const menuItems = [
  { path: '/home', label: '学习计划' },
  { path: '/courses', label: '网课' },
  { path: '/levels', label: '关卡练习' },
  { path: '/forum', label: '论坛' },
  { path: '/ranking', label: '积分排行' },
  { path: '/errors', label: '错题本' },
]

const activePath = computed(() => {
  if (route.path.startsWith('/challenge/')) {
    return '/levels'
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
</script>

<template>
  <div class="app-shell">
    <header v-if="showTopbar" class="topbar">
      <div class="page-container topbar-inner">
        <div class="brand">算法闯关助手</div>
        <el-menu class="main-menu" :default-active="activePath" mode="horizontal" @select="handleMenuSelect">
          <el-menu-item index="/home">学习计划</el-menu-item>
          <el-menu-item index="/courses">网课</el-menu-item>
          <el-menu-item index="/levels">关卡练习</el-menu-item>
          <el-menu-item index="/forum">论坛</el-menu-item>
          <el-menu-item index="/ranking">积分排行</el-menu-item>
          <el-menu-item index="/errors">错题本</el-menu-item>
        </el-menu>
        <div class="nav-search-wrapper" :class="{ 'is-expanded': navSearchExpanded }">
          <div class="nav-search-box">
            <button class="nav-search-btn" @click="toggleNavSearch">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/>
                <path d="m21 21-4.35-4.35"/>
              </svg>
            </button>
            <Transition name="search-expand">
              <div v-if="navSearchExpanded" class="nav-search-input-wrap">
                <input 
                  v-model="navSearchKeyword"
                  type="text" 
                  class="nav-search-input"
                  placeholder="搜索帖子..."
                  @keyup.enter="handleNavSearch"
                  @keyup.esc="collapseNavSearch"
                />
                <button v-if="navSearchKeyword" class="nav-clear-btn" @click.stop="clearNavSearch">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                    <path d="M18 6 6 18M6 6l12 12"/>
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
          <el-tag type="info" effect="plain">赛道 {{ trackLabel }}</el-tag>
          <el-tag type="success" effect="light">积分 {{ userStore.points }}</el-tag>
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-entry">
              <el-avatar :size="34" :src="userStore.userInfo?.avatar || ''">
                {{ (userStore.userInfo?.name || '同学').slice(0, 1) }}
              </el-avatar>
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
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.topbar {
  position: fixed;
  top: 0;
  width: 100%;
  z-index: 20;
  background: rgba(248, 250, 252, 0.94);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
}

.topbar-inner {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 72px;
}

.brand {
  flex-shrink: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-title);
  white-space: nowrap;
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
  width: 36px;
  height: 36px;
  border: none;
  background: linear-gradient(135deg, #f0f4f8 0%, #e8ecf0 100%);
  border-radius: 10px;
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
  border: 2px solid rgba(74, 144, 217, 0.3);
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.15);
}

.nav-search-input {
  width: 160px;
  padding: 6px 10px;
  border: none;
  outline: none;
  font-size: 14px;
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

  .brand {
    font-size: 18px;
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
    font-size: 16px;
    flex-shrink: 0;
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
    background: rgba(248, 250, 252, 0.94);
    border-bottom: 1px solid var(--line);
    backdrop-filter: blur(10px);
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
