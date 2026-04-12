<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useForumStore } from '../stores/forum'
import { useUserStore } from '../stores/user'
import AuthorLevelBadge from '../components/AuthorLevelBadge.vue'
import api from '../api'
import { getFullFileUrl } from '../utils/file'
import likeIcon from '../assets/icons/like.svg'

const route = useRoute()
const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()
const resolveAvatar = (avatar) => getFullFileUrl(avatar || '')

const postId = computed(() => Number(route.params.id))
const targetCommentId = computed(() => route.query.commentId ? Number(route.query.commentId) : null)

const post = ref(null)
const loading = ref(false)
const error = ref('')
const comments = ref([])
const commentsLoading = ref(false)
const commentContent = ref('')
const submitting = ref(false)
const commentsSectionRef = ref(null)
const highlightedCommentId = ref(null)

// 热门帖子数据（模拟）
const hotPosts = ref([
  { id: 1, title: '2025年春招算法岗面经汇总', hot: 2341 },
  { id: 2, title: '字节跳动后端开发一面经验', hot: 1856 },
  { id: 3, title: 'LeetCode周赛第400场题解', hot: 1523 },
  { id: 4, title: '动态规划入门到精通学习路线', hot: 1289 },
  { id: 5, title: '阿里Java后端实习面经分享', hot: 987 },
  { id: 6, title: '如何高效刷题？我的学习方法', hot: 856 },
  { id: 7, title: '美团算法工程师面试复盘', hot: 743 },
  { id: 8, title: '2025年最值得学习的算法', hot: 621 },
])

// 创作者榜单（模拟）
const creators = ref([
  { name: '算法小王子', level: 'Lv.8', posts: 156 },
  { name: 'CodeMaster', level: 'Lv.7', posts: 128 },
  { name: '面试达人', level: 'Lv.6', posts: 98 },
  { name: '算法小白', level: 'Lv.5', posts: 76 },
])

const formatTime = (timestamp) => {
  if (!timestamp) return '刚刚'
  const date = new Date(timestamp)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const scrollToComments = () => {
  if (route.hash === '#comments' || route.query.scrollTo === 'comments' || targetCommentId.value) {
    nextTick(() => {
      setTimeout(() => {
        const el = document.getElementById('comments')
        if (el) {
          el.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      }, 300)
    })
  }
}

const scrollToComment = () => {
  if (!targetCommentId.value) return
  
  nextTick(() => {
    setTimeout(() => {
      const commentEl = document.getElementById(`comment-${targetCommentId.value}`)
      if (commentEl) {
        commentEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
        highlightedCommentId.value = targetCommentId.value
        setTimeout(() => {
          highlightedCommentId.value = null
        }, 3000)
      }
    }, 500)
  })
}

const fetchPost = async () => {
  if (!postId.value || Number.isNaN(postId.value)) {
    error.value = '帖子ID无效'
    post.value = null
    return
  }

  loading.value = true
  error.value = ''

  try {
    const res = await api.get(`/forum-posts/${postId.value}`)
    if (res.data?.code === 0 && res.data?.data) {
      post.value = res.data.data
    } else {
      post.value = null
      error.value = res.data?.message || '帖子不存在'
    }
  } catch (e) {
    console.error('获取帖子失败:', e)
    post.value = null
    error.value = '获取帖子失败'
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  if (!postId.value || Number.isNaN(postId.value)) return
  
  commentsLoading.value = true
  try {
    const res = await api.get(`/forum-posts/${postId.value}/comments`)
    if (res.data?.code === 0) {
      comments.value = res.data.data?.list || []
    }
  } catch (e) {
    console.error('获取评论失败:', e)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true
  try {
    const res = await api.post(`/forum-posts/${postId.value}/comments`, {
      content: commentContent.value.trim()
    })
    if (res.data?.code === 0) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      await fetchComments()
      if (post.value) {
        post.value.comments = (post.value.comments || 0) + 1
      }
    }
  } catch (e) {
    console.error('发表评论失败:', e)
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const action = post.value?.liked ? 'unlike' : 'like'
    const res = await api.post(`/forum-posts/${postId.value}/like`, { action })
    if (res.data?.code === 0 && res.data?.data) {
      post.value.liked = Boolean(res.data.data.liked)
      post.value.likes = Number(res.data.data.likes || 0)
      ElMessage.success(post.value.liked ? '点赞成功' : '取消点赞')
    }
  } catch (e) {
    console.error('点赞失败:', e)
    ElMessage.error('操作失败')
  }
}

const goBack = () => {
  router.back()
}

const goToPost = (id) => {
  router.push(`/forum/post/${id}`)
}

watch(() => postId.value, () => {
  fetchPost()
  fetchComments()
}, { immediate: true })

watch(() => comments.value, () => {
  scrollToComments()
  scrollToComment()
}, { immediate: true })
</script>

<template>
  <div class="post-detail-page">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <button class="nav-back" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </button>
      <span class="nav-title">帖子详情</span>
      <div class="nav-right"></div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-container">
      <span class="empty-icon">⚠️</span>
      <p class="error-message">{{ error }}</p>
      <el-button type="primary" @click="goBack">返回论坛</el-button>
    </div>

    <template v-else-if="post">
      <!-- 三栏布局容器 -->
      <div class="main-container">
        <!-- 左侧主内容区 -->
        <div class="content-left">
          <!-- 帖子内容卡片 -->
          <div class="post-content-card">
            <!-- 作者信息 -->
            <div class="author-section">
              <el-avatar :size="48" :src="resolveAvatar(post.avatar) || ''" class="author-avatar">
                {{ (post.author || '匿').slice(0, 1) }}
              </el-avatar>
              <div class="author-info">
                <div class="author-name-row">
                  <span class="author-name">{{ post.author }}</span>
                  <AuthorLevelBadge :profile="post.authorLevelProfile" :raw-label="post.authorLevel || 'Lv.1'" size="sm" />
                </div>
                <div class="author-meta">
                  <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                  <span class="post-location">{{ post.location || '北京' }}</span>
                </div>
              </div>
              <button class="follow-btn">关注</button>
            </div>

            <!-- 帖子标题 -->
            <h1 class="post-title">{{ post.topic }}</h1>

            <!-- 帖子正文 -->
            <div class="post-body">
              <p>{{ post.content }}</p>
            </div>

            <!-- 话题标签 -->
            <div class="post-tags" v-if="post.tag">
              <span class="tag-item">#{{ post.tag }}#</span>
            </div>

            <!-- 帖子底部操作栏 -->
            <div class="post-actions">
              <button class="action-btn" :class="{ 'is-liked': post.liked }" @click="handleLike">
                <img class="like-icon" :src="likeIcon" alt="点赞" />
                <span class="btn-count">{{ post.likes || 0 }}</span>
              </button>
              <button class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                <span class="btn-count">{{ post.comments || 0 }}</span>
              </button>
              <button class="action-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
                </svg>
                <span class="btn-count">收藏</span>
              </button>
              <button class="action-btn share-btn">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="18" cy="5" r="3" />
                  <circle cx="6" cy="12" r="3" />
                  <circle cx="18" cy="19" r="3" />
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" />
                  <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
                </svg>
                <span class="btn-count">分享</span>
              </button>
            </div>
          </div>

          <!-- 评论区 -->
          <div id="comments" ref="commentsSectionRef" class="comments-section">
            <div class="comments-header">
              <h3 class="comments-title">
                全部评论 <span class="comments-count">({{ comments.length }}条)</span>
              </h3>
            </div>

            <!-- 评论输入框 -->
            <div class="comment-input-section">
              <el-avatar :size="36" :src="resolveAvatar(userStore.userInfo?.avatar) || ''" class="comment-avatar-input">
                {{ (userStore.userInfo?.username || '匿').slice(0, 1) }}
              </el-avatar>
              <div class="comment-input-wrapper">
                <input 
                  v-model="commentContent" 
                  type="text" 
                  class="comment-input" 
                  placeholder="写下你的评论..."
                  @keyup.enter="submitComment"
                />
                <button class="submit-btn" @click="submitComment" :disabled="submitting">
                  评论
                </button>
              </div>
            </div>

            <div v-if="commentsLoading" class="comments-loading">
              <el-skeleton :rows="3" animated />
            </div>

            <div v-else-if="comments.length === 0" class="no-comments">
              <span class="empty-icon">💭</span>
              <p>暂无评论，快来抢沙发吧~</p>
            </div>

            <div v-else class="comments-list">
              <div v-for="comment in comments" :key="comment.id" :id="`comment-${comment.id}`" class="comment-item" :class="{ 'is-highlighted': highlightedCommentId === comment.id }">
                <div class="comment-left">
                  <el-avatar :size="40" :src="resolveAvatar(comment.avatar) || ''" class="comment-avatar">
                    {{ (comment.author || '匿').slice(0, 1) }}
                  </el-avatar>
                </div>
                <div class="comment-right">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.author }}</span>
                    <AuthorLevelBadge :profile="comment.authorLevelProfile" :raw-label="comment.authorLevel || 'Lv.1'" size="sm" />
                  </div>
                  <div class="comment-company" v-if="comment.company">
                    {{ comment.company }}
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  <div class="comment-meta">
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                    <button class="comment-like">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
                      </svg>
                      点赞
                    </button>
                    <button class="comment-reply">回复</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="content-right">
          <!-- 全站热榜 -->
          <div class="sidebar-card hot-posts-card">
            <div class="card-header">
              <h3 class="card-title">全站热榜</h3>
              <a href="#" class="more-link">更多 &gt;</a>
            </div>
            <div class="hot-posts-list">
              <div v-for="(item, index) in hotPosts" :key="item.id" class="hot-post-item" @click="goToPost(item.id)">
                <span class="rank-num" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
                <span class="post-title-text">{{ item.title }}</span>
                <span class="hot-count">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  {{ item.hot }}
                </span>
              </div>
            </div>
          </div>

          <!-- 创作者周榜 -->
          <div class="sidebar-card creators-card">
            <div class="card-header">
              <h3 class="card-title">创作者周榜</h3>
              <a href="#" class="more-link">更多 &gt;</a>
            </div>
            <div class="creators-list">
              <div v-for="(creator, index) in creators" :key="creator.name" class="creator-item">
                <span class="rank-num" :class="{ 'top-three': index < 3 }">{{ index + 1 }}</span>
                <el-avatar :size="32" class="creator-avatar">
                  {{ creator.name.slice(0, 1) }}
                </el-avatar>
                <div class="creator-info">
                  <span class="creator-name">{{ creator.name }}</span>
                  <span class="creator-level">{{ creator.level }}</span>
                </div>
                <span class="creator-posts">{{ creator.posts }}篇</span>
              </div>
            </div>
          </div>

          <!-- 广告位/推广 -->
          <div class="sidebar-card promo-card">
            <div class="promo-content">
              <h4 class="promo-title">AIgoMind 算法学习平台</h4>
              <p class="promo-desc">系统化学习算法，提升编程能力</p>
              <button class="promo-btn">立即开始</button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="not-found">
      <span class="empty-icon">🔍</span>
      <p>帖子不存在或已被删除</p>
      <el-button type="primary" @click="goBack">返回论坛</el-button>
    </div>
  </div>
</template>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #333;
  border-radius: 50%;
  transition: background 0.2s;
}

.nav-back:hover {
  background: #f5f5f5;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  width: 36px;
}

/* 主容器 - 三栏布局 */
.main-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
}

/* 左侧内容区 */
.content-left {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 帖子内容卡片 */
.post-content-card {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 作者信息 */
.author-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.author-avatar {
  flex-shrink: 0;
}

.author-info {
  flex: 1;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.author-level {
  font-size: 12px;
  color: #00b96b;
  background: rgba(0, 185, 107, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.author-meta {
  font-size: 13px;
  color: #999;
  display: flex;
  gap: 12px;
}

.follow-btn {
  padding: 6px 16px;
  border: 1px solid #00b96b;
  background: transparent;
  color: #00b96b;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.follow-btn:hover {
  background: #00b96b;
  color: #fff;
}

/* 帖子标题 */
.post-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  line-height: 1.5;
  margin-bottom: 16px;
}

/* 帖子正文 */
.post-body {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 20px;
}

.post-body p {
  margin: 0;
  white-space: pre-wrap;
}

/* 话题标签 */
.post-tags {
  margin-bottom: 20px;
}

.tag-item {
  font-size: 14px;
  color: #00b96b;
  background: rgba(0, 185, 107, 0.08);
  padding: 4px 12px;
  border-radius: 16px;
}

/* 帖子底部操作栏 */
.post-actions {
  display: flex;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f5f5f5;
  color: #333;
}

.action-btn.is-liked {
  color: #ff4757;
  background: rgba(255, 71, 87, 0.08);
}

.like-icon {
  width: 20px;
  height: 20px;
  display: block;
  flex-shrink: 0;
}

.action-btn.is-liked .like-icon {
  filter: brightness(0) saturate(100%) invert(24%) sepia(73%) saturate(5592%) hue-rotate(338deg) brightness(101%) contrast(102%);
}

.btn-count {
  font-size: 14px;
  font-weight: 500;
}

/* 评论区 */
.comments-section {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.comments-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.comments-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.comments-count {
  font-size: 14px;
  color: #999;
  font-weight: normal;
}

/* 评论输入框 */
.comment-input-section {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.comment-avatar-input {
  flex-shrink: 0;
}

.comment-input-wrapper {
  flex: 1;
  display: flex;
  gap: 12px;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 24px;
}

.comment-input {
  flex: 1;
  height: 40px;
  padding: 0 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
}

.comment-input::placeholder {
  color: #999;
}

.submit-btn {
  padding: 0 20px;
  height: 40px;
  border: none;
  background: #00b96b;
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover {
  background: #00a85a;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 20px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.is-highlighted {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border-radius: 12px;
  padding: 20px;
  margin: 0 -12px;
  box-shadow: 0 2px 12px rgba(250, 173, 20, 0.2);
  animation: highlight-pulse 0.5s ease;
}

@keyframes highlight-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.01); }
  100% { transform: scale(1); }
}

.comment-left {
  flex-shrink: 0;
}

.comment-avatar {
  border-radius: 50%;
}

.comment-right {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-author {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.comment-level {
  font-size: 11px;
  color: #00b96b;
  background: rgba(0, 185, 107, 0.1);
  padding: 1px 6px;
  border-radius: 8px;
}

.comment-company {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.comment-content {
  font-size: 15px;
  line-height: 1.7;
  color: #333;
  margin-bottom: 12px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.comment-like,
.comment-reply {
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: #999;
  cursor: pointer;
  font-size: 13px;
  transition: color 0.2s;
}

.comment-like:hover,
.comment-reply:hover {
  color: #00b96b;
}

/* 右侧边栏 */
.content-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.more-link {
  font-size: 13px;
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}

.more-link:hover {
  color: #00b96b;
}

/* 热榜列表 */
.hot-posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-post-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0;
  transition: background 0.2s;
}

.hot-post-item:hover {
  background: #f9f9f9;
  margin: 0 -8px;
  padding: 4px 8px;
  border-radius: 8px;
}

.rank-num {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: #999;
  background: #f5f5f5;
  border-radius: 4px;
  flex-shrink: 0;
}

.rank-num.top-three {
  color: #fff;
  background: #ff6b6b;
}

.post-title-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.hot-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #ff6b6b;
  flex-shrink: 0;
}

/* 创作者榜单 */
.creators-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.creator-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 0;
  transition: background 0.2s;
}

.creator-item:hover {
  background: #f9f9f9;
  margin: 0 -8px;
  padding: 4px 8px;
  border-radius: 8px;
}

.creator-avatar {
  flex-shrink: 0;
}

.creator-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.creator-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.creator-level {
  font-size: 11px;
  color: #00b96b;
}

.creator-posts {
  font-size: 12px;
  color: #999;
}

/* 推广卡片 */
.promo-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.promo-content {
  text-align: center;
}

.promo-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.promo-desc {
  font-size: 13px;
  opacity: 0.9;
  margin-bottom: 16px;
}

.promo-btn {
  padding: 8px 24px;
  border: none;
  background: #fff;
  color: #667eea;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

.promo-btn:hover {
  transform: translateY(-2px);
}

/* 加载和空状态 */
.loading-container {
  padding: 40px;
  background: #fff;
  max-width: 1200px;
  margin: 20px auto;
  border-radius: 12px;
}

.error-container,
.not-found {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
  max-width: 1200px;
  margin: 20px auto;
  border-radius: 12px;
}

.error-container .empty-icon,
.not-found .empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.error-message {
  color: #999;
  margin-bottom: 20px;
  font-size: 16px;
}

.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.no-comments .empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.comments-loading {
  padding: 20px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .main-container {
    grid-template-columns: 1fr;
  }

  .content-right {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-container {
    padding: 0 16px;
    margin: 12px auto;
  }

  .top-nav {
    padding: 12px 16px;
  }

  .post-content-card,
  .comments-section {
    padding: 16px;
  }

  .post-title {
    font-size: 18px;
  }

  .post-actions {
    gap: 12px;
  }

  .action-btn {
    padding: 6px 12px;
  }

  .comment-input-section {
    flex-direction: column;
  }

  .comment-input-wrapper {
    width: 100%;
  }
}
</style>
