<script setup>
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useUserStore } from './stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showTopbar = computed(() => route.path !== '/')
const trackTextMap = {
  algo: '算法思维',
  ds: '数据结构',
  contest: '竞赛冲刺',
}

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
const trackLabel = computed(() => trackTextMap[userStore.userInfo?.targetTrack || userStore.selectedTrack] || '算法思维')

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

    <!-- 移动端菜单 -->
    <div v-if="showTopbar" class="mobile-menu-wrapper hidden-on-desktop">
      <div class="mobile-menu">
        <div
          v-for="item in menuItems"
          :key="item.path"
          :class="['mobile-menu-item', { active: activePath === item.path }]"
          @click="handleMenuSelect(item.path)"
        >
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

/* 默认隐藏移动端菜单 */
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

  /* 在移动端显示菜单 */
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
    margin-left: auto;
  }

  .actions :deep(.el-tag) {
    display: none;
  }

  .actions :deep(.el-tag:last-of-type) {
    display: inline-flex;
    padding: 0 4px;
    font-size: 11px;
  }

  .user-name {
    display: none;
  }

  .app-main.with-topbar {
    padding-top: 112px;
  }
}
</style>
