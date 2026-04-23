<template>
  <div class="user-home-page page-container">
    <div class="user-home-card">
      <div class="header-row">
        <img :src="displayAvatar" alt="avatar" class="user-avatar" />
        <div class="user-meta">
          <h1 class="user-name">{{ profile.name || '用户' }}</h1>
          <p class="user-level">创作者等级：{{ profile.authorLevelCode || 'Lv.1' }}</p>
        </div>
      </div>

      <p class="user-bio">{{ profile.bio || '这个人很神秘，还没有留下简介。' }}</p>

      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-label">积分</span>
          <span class="stat-value">{{ profile.points || 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">创作分</span>
          <span class="stat-value">{{ profile.authorScore || 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">加入时间</span>
          <span class="stat-value">{{ joinedAt }}</span>
        </div>
      </div>

      <div class="links-row" v-if="profile.github || profile.website">
        <a v-if="profile.github" :href="profile.github" target="_blank" rel="noopener noreferrer"
          class="link-item">GitHub</a>
        <a v-if="profile.website" :href="profile.website" target="_blank" rel="noopener noreferrer"
          class="link-item">个人网站</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { ElMessage } from 'element-plus'
import { getFullFileUrl } from '@/utils/file'

const route = useRoute()
const profile = ref({})

const displayAvatar = computed(() => {
  const avatar = getFullFileUrl(profile.value?.avatar || '')
  if (avatar) return avatar
  return `https://api.dicebear.com/7.x/identicon/svg?seed=${route.params.id}`
})

const joinedAt = computed(() => {
  if (!profile.value?.createdAt) return '-'
  return new Date(profile.value.createdAt).toLocaleDateString()
})

const fetchUserProfile = async () => {
  try {
    const response = await api.get(`/users/${route.params.id}/public`)
    if (response.data?.code === 0 || response.data?.code === 200) {
      profile.value = response.data.data || {}
      return
    }
    ElMessage.error(response.data?.message || '获取用户信息失败')
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.user-home-page {
  min-height: calc(100vh - 72px);
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.03), rgba(59, 130, 246, 0.06));
}

.user-home-card {
  width: 100%;
  max-width: 760px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
}

.header-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 16px;
}

.user-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.user-name {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
}

.user-level {
  margin: 8px 0 0;
  color: #475569;
  font-size: 14px;
}

.user-bio {
  margin: 0 0 22px;
  color: #334155;
  line-height: 1.7;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.stat-item {
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.25);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-label {
  color: #64748b;
  font-size: 12px;
}

.stat-value {
  color: #0f172a;
  font-size: 18px;
  font-weight: 700;
}

.links-row {
  margin-top: 18px;
  display: flex;
  gap: 10px;
}

.link-item {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}

.link-item:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .user-home-page {
    padding: 14px;
  }

  .user-home-card {
    padding: 20px;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
