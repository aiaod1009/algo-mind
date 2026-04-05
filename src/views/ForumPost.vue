<script setup>
import { ref, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useForumStore } from '../stores/forum'
import { useUserStore } from '../stores/user'
import api from '../api'

const route = useRoute()
const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()

const postId = computed(() => Number(route.params.id))

const post = ref(null)
const loading = ref(false)
const error = ref('')
const comments = ref([])
const commentsLoading = ref(false)
const commentContent = ref('')
const submitting = ref(false)
const commentsSectionRef = ref(null)

const formatTime = (timestamp) => {
  if (!timestamp) return '刚刚'
  const diff = Date.now() - Number(new Date(timestamp))
  if (diff < 1000 * 60) return '刚刚'
  if (diff < 1000 * 60 * 60) return `${Math.floor(diff / (1000 * 60))} 分钟前`
  if (diff < 1000 * 60 * 60 * 24) return `${Math.floor(diff / (1000 * 60 * 60))} 小时前`
  return new Date(timestamp).toLocaleDateString()
}

const scrollToComments = () => {
  if (route.hash === '#comments' || route.query.scrollTo === 'comments') {
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
    <div class="post-header">
      <button class="back-btn" @click="goBack">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        <span>返回列表</span>
      </button>
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

      <div class="post-card">
        <div class="post-meta">
          <div class="author-info">
            <el-avatar :size="48" :src="post.avatar || ''">
              {{ (post.author || '匿').slice(0, 1) }}
            </el-avatar>
            <div class="author-detail">
              <div class="author-name-row">
                <span class="author-name">{{ post.author }}</span>
                <el-tag type="info" effect="light" size="small">{{ post.authorLevel || 'Lv.1' }}</el-tag>
              </div>
              <div class="post-time">发布于 {{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
        </div>

        <h1 class="post-title">{{ post.topic }}</h1>

        <div class="post-content">
          <p>{{ post.content }}</p>
        </div>

        <div v-if="post.quote" class="post-quote">
          <span class="quote-icon">💭</span>
          {{ post.quote }}
        </div>

        <div class="post-tag">{{ post.tag }}</div>

        <div class="post-actions">
          <button class="action-btn like-btn" :class="{ 'is-liked': post.liked }" @click="handleLike">
            <span class="btn-icon">{{ post.liked ? '❤️' : '🤍' }}</span>
            <span class="btn-count">{{ post.likes || 0 }}</span>
            <span class="btn-label">点赞</span>
          </button>
          <div class="action-btn comment-btn">
            <span class="btn-icon">💬</span>
            <span class="btn-count">{{ post.comments || 0 }}</span>
            <span class="btn-label">评论</span>
          </div>
        </div>
      </div>

      <div id="comments" ref="commentsSectionRef" class="comments-section">
        <h3 class="section-title">
          <span class="title-icon">💬</span>
          评论 ({{ comments.length }})
        </h3>

        <div class="comment-input-card">
          <el-avatar :size="40" :src="userStore.userInfo?.avatar || ''">
            {{ (userStore.userInfo?.name || '我').slice(0, 1) }}
          </el-avatar>
          <div class="input-wrapper">
            <textarea
              v-model="commentContent"
              class="comment-textarea"
              placeholder="写下你的评论..."
              rows="3"
            ></textarea>
            <div class="input-footer">
              <span class="char-count">{{ commentContent.length }}/500</span>
              <el-button
                type="primary"
                :loading="submitting"
                :disabled="!commentContent.trim()"
                @click="submitComment"
              >
                发表评论
              </el-button>
            </div>
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
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <el-avatar :size="36" :src="comment.avatar || ''">
                {{ (comment.author || '匿').slice(0, 1) }}
              </el-avatar>
            </div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author">{{ comment.author }}</span>
                <el-tag type="info" effect="light" size="small">{{ comment.authorLevel || 'Lv.1' }}</el-tag>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div class="comment-footer">
                <span class="comment-likes">👍 {{ comment.likes || 0 }}</span>
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
  </div>
</template>

<style scoped>
.post-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 40px;
}

.error-container {
  text-align: center;
  padding: 60px 20px;
}

.error-container .empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.error-message {
  color: var(--text-sub);
  margin-bottom: 20px;
  font-size: 16px;
}

.post-header {
  margin-bottom: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  background: rgba(74, 144, 217, 0.1);
  border-radius: 20px;
  color: #4a90d9;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: rgba(74, 144, 217, 0.2);
}

.post-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid var(--line-soft);
}

.post-meta {
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-title);
}

.post-time {
  font-size: 13px;
  color: var(--text-sub);
}

.post-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-title);
  line-height: 1.4;
  margin-bottom: 16px;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--text-main);
  margin-bottom: 16px;
}

.post-content p {
  margin: 0;
  white-space: pre-wrap;
}

.post-quote {
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f0f4f8 100%);
  border-left: 4px solid #4a90d9;
  border-radius: 0 8px 8px 0;
  margin-bottom: 16px;
  color: var(--text-sub);
  font-style: italic;
}

.quote-icon {
  margin-right: 8px;
}

.post-tag {
  display: inline-block;
  padding: 6px 14px;
  background: linear-gradient(135deg, rgba(74, 144, 217, 0.1) 0%, rgba(74, 144, 217, 0.05) 100%);
  border-radius: 20px;
  color: #4a90d9;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
}

.post-actions {
  display: flex;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid var(--line-soft);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: var(--text-sub);
}

.action-btn:hover {
  background: rgba(74, 144, 217, 0.1);
  color: #4a90d9;
}

.action-btn.is-liked {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: white;
}

.btn-icon {
  font-size: 18px;
}

.btn-count {
  font-weight: 600;
}

.btn-label {
  font-size: 13px;
}

.comments-section {
  margin-top: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-title);
  margin-bottom: 16px;
}

.title-icon {
  font-size: 20px;
}

.comment-input-card {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--line-soft);
  margin-bottom: 20px;
}

.input-wrapper {
  flex: 1;
}

.comment-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--line-soft);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  min-height: 80px;
  outline: none;
  transition: border-color 0.2s;
}

.comment-textarea:focus {
  border-color: #4a90d9;
}

.comment-textarea::placeholder {
  color: #bfbfbf;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.char-count {
  font-size: 12px;
  color: var(--text-sub);
}

.comments-loading {
  padding: 20px;
}

.no-comments {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-sub);
}

.no-comments .empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--line-soft);
  transition: all 0.2s;
}

.comment-item:hover {
  border-color: rgba(74, 144, 217, 0.3);
  box-shadow: 0 2px 8px rgba(74, 144, 217, 0.08);
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-title);
}

.comment-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-sub);
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main);
  margin-bottom: 8px;
}

.comment-footer {
  display: flex;
  gap: 16px;
}

.comment-likes {
  font-size: 12px;
  color: var(--text-sub);
}

.not-found {
  text-align: center;
  padding: 60px 20px;
}

.not-found .empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.not-found p {
  color: var(--text-sub);
  margin-bottom: 20px;
}

@media (max-width: 600px) {
  .post-detail-page {
    padding: 12px;
  }

  .post-card {
    padding: 16px;
  }

  .post-title {
    font-size: 20px;
  }

  .post-actions {
    flex-wrap: wrap;
  }
}
</style>
