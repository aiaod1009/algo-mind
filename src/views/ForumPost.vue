<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useForumStore } from '../stores/forum'
import { useUserStore } from '../stores/user'
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
    const res = await api.get(`/forum-posts/${postId.value}/comments`, {
      params: { pageSize: 50 }
    })
    if (res.data?.code === 0 && res.data?.data) {
      comments.value = res.data.data.list || []
      if (targetCommentId.value) {
        scrollToComment()
      }
    }
  } catch (e) {
    console.error('获取评论失败:', e)
  } finally {
    commentsLoading.value = false
  }
}

const handleLike = async () => {
  if (!post.value) return
  try {
    const action = post.value.liked ? 'unlike' : 'like'
    const res = await api.post(`/forum-posts/${postId.value}/like`, { action })
    if (res.data?.code === 0) {
      post.value.likes = res.data.data.likes
      post.value.liked = res.data.data.liked

      const cachedPost = forumStore.posts.find((item) => Number(item.id) === Number(postId.value))
      if (cachedPost) {
        cachedPost.likes = res.data.data.likes
        cachedPost.liked = res.data.data.liked
      }
    }
  } catch (e) {
    console.error('点赞失败:', e)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  submitting.value = true
  try {
    const res = await api.post(`/forum-posts/${postId.value}/comments`, {
      content: commentContent.value.trim(),
      author: userStore.userInfo?.name || '匿名用户',
      authorLevel: 'Lv.1',
    })
    if (res.data?.code === 0) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      fetchComments()
      if (post.value) {
        post.value.comments = (post.value.comments || 0) + 1
      }
    }
  } catch (e) {
    console.error('评论失败:', e)
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/forum')
}

watch(
  () => route.params.id,
  () => {
    fetchPost()
    fetchComments()
    scrollToComments()
  },
  { immediate: true }
)
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
              <span class="author-level">{{ post.authorLevel || 'Lv.1' }}</span>
            </div>
            <div class="author-meta">{{ post.authorSchool || '算法学习平台' }} {{ post.authorMajor || '' }}</div>
          </div>
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

        <!-- 底部信息 -->
        <div class="post-footer-info">
          <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          <span class="post-location">{{ post.location || '北京' }}</span>
        </div>
      </div>

      <!-- 评论区 -->
      <div id="comments" ref="commentsSectionRef" class="comments-section">
        <div class="comments-header">
          <h3 class="comments-title">
            全部评论 <span class="comments-count">({{ comments.length }}条)</span>
          </h3>
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
                <span class="comment-level">{{ comment.authorLevel || 'Lv.1' }}</span>
              </div>
              <div class="comment-company" v-if="comment.company">
                {{ comment.company }}
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-meta">
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
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

    <!-- 底部评论输入栏 -->
    <div class="bottom-input-bar" v-if="post">
      <div class="input-wrapper">
        <input 
          v-model="commentContent" 
          type="text" 
          class="comment-input" 
          placeholder="畅所欲言吧~"
          @keyup.enter="submitComment"
        />
      </div>
      <div class="action-btns">
        <button class="action-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <span class="btn-count">{{ post.comments || 0 }}</span>
        </button>
        <button class="action-btn" :class="{ 'is-liked': post.liked }" @click="handleLike">
          <img class="like-icon" :src="likeIcon" alt="点赞" />
          <span class="btn-count">{{ post.likes || 0 }}</span>
        </button>
        <button class="action-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
          </svg>
          <span class="btn-count">收藏</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 70px;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
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
}

.nav-back:hover {
  background: #f5f5f5;
  border-radius: 50%;
}

.nav-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
}

.nav-right {
  width: 36px;
}

/* 帖子内容卡片 */
.post-content-card {
  background: #fff;
  padding: 16px;
  margin-bottom: 8px;
}

/* 作者信息 */
.author-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
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
}

/* 帖子标题 */
.post-title {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1.5;
  margin-bottom: 12px;
}

/* 帖子正文 */
.post-body {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 16px;
}

.post-body p {
  margin: 0;
  white-space: pre-wrap;
}

/* 话题标签 */
.post-tags {
  margin-bottom: 16px;
}

.tag-item {
  font-size: 14px;
  color: #00b96b;
  margin-right: 12px;
}

/* 底部信息 */
.post-footer-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #999;
}

/* 评论区 */
.comments-section {
  background: #fff;
  padding: 16px;
}

.comments-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
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

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.is-highlighted {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border-radius: 12px;
  padding: 16px;
  margin: 0 -16px;
  box-shadow: 0 2px 12px rgba(250, 173, 20, 0.2);
  animation: highlight-pulse 0.5s ease;
}

@keyframes highlight-pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.01);
  }
  100% {
    transform: scale(1);
  }
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
  margin-bottom: 8px;
}

.comment-meta {
  font-size: 12px;
  color: #999;
}

/* 底部输入栏 */
.bottom-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 8px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 100;
}

.input-wrapper {
  flex: 1;
}

.comment-input {
  width: 100%;
  height: 40px;
  padding: 0 16px;
  border: none;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

.comment-input::placeholder {
  color: #999;
}

.action-btns {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  padding: 4px;
}

.action-btn:hover {
  color: #333;
}

.action-btn.is-liked {
  color: #ff4757;
}

.like-icon {
  width: 20px;
  height: 20px;
  display: block;
  flex-shrink: 0;
  transition: filter 0.2s ease, transform 0.2s ease;
}

.action-btn:hover .like-icon {
  transform: scale(1.06);
}

.action-btn.is-liked .like-icon {
  filter: brightness(0) saturate(100%) invert(24%) sepia(73%) saturate(5592%) hue-rotate(338deg) brightness(101%) contrast(102%);
}

.btn-count {
  font-size: 13px;
}

/* 加载和空状态 */
.loading-container {
  padding: 40px;
  background: #fff;
}

.error-container,
.not-found {
  text-align: center;
  padding: 60px 20px;
  background: #fff;
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
@media (max-width: 600px) {
  .post-title {
    font-size: 18px;
  }
  
  .post-body {
    font-size: 14px;
  }
  
  .comment-content {
    font-size: 14px;
  }
}
</style>
