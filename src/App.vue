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
  width: 180px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-title);
}

.main-menu {
  flex: 1;
  border-bottom: none;
  background: transparent;
}

.actions {
  min-width: 320px;
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

@media (max-width: 900px) {
  .topbar-inner {
    height: auto;
    flex-wrap: wrap;
    padding: 10px 0;
  }

  .brand,
  .actions {
    width: auto;
  }

  .main-menu {
    width: 100%;
    order: 3;
  }

  .app-main.with-topbar {
    padding-top: 128px;
  }
}
</style>
