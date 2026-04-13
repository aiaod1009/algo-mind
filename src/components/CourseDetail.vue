<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import api from '../api'

const props = defineProps({
  course: {
    type: Object,
    required: true,
  },
})

const userStore = useUserStore()

const activeTab = ref('chapters')
const comments = ref([])
const danmakus = ref([])
const newComment = ref('')
const newDanmaku = ref('')
const showDanmakuInput = ref(false)
const danmakuListRef = ref(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const videoDuration = ref(3600)

const videoProgress = computed(() => (currentTime.value / videoDuration.value) * 100)

const fetchComments = async () => {
  try {
    const courseId = props.course?.id || 1
    const res = await api.get(`/courses/${courseId}/comments`)
    if (res.data?.code === 0 && Array.isArray(res.data.data)) {
      comments.value = res.data.data.map(item => ({
        id: item.id,
        user: { name: item.userName, avatar: item.userAvatar || '' },
        content: item.content,
        time: item.time,
        likes: item.likes,
        replies: (item.replies || []).map(r => ({
          id: r.id,
          user: { name: r.userName },
          content: r.content,
          time: r.time
        }))
      }))
    }
  } catch (e) {
    console.error('获取评论失败', e)
  }
  return comments.value
}

const fetchDanmakus = async () => {
  danmakus.value = [
    { id: 1, content: '这里讲得好清楚', time: 15, color: '#4a6f9d' },
    { id: 2, content: '原来如此！', time: 30, color: '#6672cb' },
    { id: 3, content: '标记一下', time: 45, color: '#f59e0b' },
    { id: 4, content: '这个要记住', time: 60, color: '#10b981' },
    { id: 5, content: '老师太强了', time: 90, color: '#ef4444' },
    { id: 6, content: '学到了', time: 120, color: '#4a6f9d' },
  ]
  return danmakus.value
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  try {
    const courseId = props.course?.id || 1
    const res = await api.post(`/courses/${courseId}/comments`, {
      userId: userStore.userInfo?.id || 1,
      userName: userStore.userInfo?.name || '匿名用户',
      userAvatar: userStore.userInfo?.avatar || '',
      content: newComment.value
    })
    if (res.data?.code === 0) {
      await fetchComments()
      newComment.value = ''
    }
  } catch (e) {
    console.error('发表评论失败', e)
  }
}

const likeComment = async (comment) => {
  try {
    const res = await api.post(`/comments/${comment.id}/like`)
    if (res.data?.code === 0) {
      comment.likes = res.data.data.likes
    }
  } catch (e) {
    console.error('点赞失败', e)
  }
}

const submitDanmaku = async () => {
  if (!newDanmaku.value.trim()) return
  danmakus.value.push({
    id: Date.now(),
    content: newDanmaku.value,
    time: currentTime.value,
    color: ['#4a6f9d', '#6672cb', '#f59e0b', '#10b981', '#ef4444'][Math.floor(Math.random() * 5)],
  })
  newDanmaku.value = ''
  showDanmakuInput.value = false
}

const toggleDanmakuInput = () => {
  showDanmakuInput.value = !showDanmakuInput.value
  if (showDanmakuInput.value) {
    nextTick(() => {
      document.querySelector('.danmaku-input')?.focus()
    })
  }
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const seekTo = (time) => {
  currentTime.value = time
}

defineExpose({
  fetchComments,
  fetchDanmakus,
  updateComments: (data) => { comments.value = data },
  updateDanmakus: (data) => { danmakus.value = data },
})

onMounted(() => {
  fetchComments()
  fetchDanmakus()
})
</script>

<template>
  <div class="course-detail">
    <div class="video-section">
      <div class="video-player">
        <iframe
          class="video-iframe"
          src="https://player.bilibili.com/player.html?bvid=BV1r5411f7Bf&poster=1"
          frameborder="0"
          allowfullscreen
        ></iframe>
      </div>
    </div>

    <div class="course-info-section">
      <div class="course-header">
        <div class="course-title-wrap">
          <span class="course-level" :class="course.level">{{ course.level === 'beginner' ? '入门' : course.level === 'intermediate' ? '进阶' : '高级' }}</span>
          <h2 class="course-title">{{ course.title }}</h2>
        </div>
        <div class="course-stats">
          <span class="stat">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
            {{ course.rating }}
          </span>
          <span class="stat">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
            </svg>
            {{ course.students }} 学员
          </span>
        </div>
      </div>

      <div class="teacher-card">
        <div class="teacher-avatar">
          <span>{{ course.teacher.name.slice(0, 1) }}</span>
        </div>
        <div class="teacher-info">
          <span class="teacher-name">{{ course.teacher.name }}</span>
          <span class="teacher-title">{{ course.teacher.title }}</span>
        </div>
      </div>

      <p class="course-description">{{ course.description }}</p>

      <div class="course-direction-card">
        <span class="direction-icon">📍</span>
        <span>{{ course.direction }}</span>
      </div>

      <div class="course-tags">
        <span v-for="tag in course.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>

    <div class="content-tabs">
      <button :class="['tab-btn', { active: activeTab === 'chapters' }]" @click="activeTab = 'chapters'">
        课程目录
      </button>
      <button :class="['tab-btn', { active: activeTab === 'comments' }]" @click="activeTab = 'comments'">
        讨论 ({{ comments.length }})
      </button>
      <button :class="['tab-btn', { active: activeTab === 'notes' }]" @click="activeTab = 'notes'">
        笔记
      </button>
    </div>

    <div class="tab-content">
      <div v-show="activeTab === 'chapters'" class="chapters-panel">
        <div class="chapter-list">
          <div
            v-for="(chapter, idx) in course.chapters"
            :key="chapter.id"
            :class="['chapter-item', { completed: chapter.completed, active: idx === 0 }]"
          >
            <div class="chapter-status">
              <svg v-if="chapter.completed" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <span v-else class="chapter-num">{{ idx + 1 }}</span>
            </div>
            <div class="chapter-info">
              <span class="chapter-title">{{ chapter.title }}</span>
              <span class="chapter-duration">{{ chapter.duration }}</span>
            </div>
            <button class="play-btn">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <polygon points="5 3 19 12 5 21 5 3"/>
              </svg>
            </button>
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'comments'" class="comments-panel">
        <div class="comment-input-wrap">
          <div class="user-avatar">
            <span>{{ (userStore.userInfo?.name || '我').slice(0, 1) }}</span>
          </div>
          <div class="input-area">
            <textarea v-model="newComment" placeholder="分享你的学习心得..." rows="2"></textarea>
            <button class="submit-btn" :disabled="!newComment.trim()" @click="submitComment">发表评论</button>
          </div>
        </div>

        <div class="comments-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">
              <span>{{ comment.user.name.slice(0, 1) }}</span>
            </div>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-user">{{ comment.user.name }}</span>
                <span class="comment-time">{{ comment.time }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <div class="comment-actions">
                <button class="action-btn" @click="likeComment(comment)">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/>
                  </svg>
                  {{ comment.likes }}
                </button>
                <button class="action-btn">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  回复
                </button>
              </div>
              <div v-if="comment.replies.length" class="replies">
                <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                  <span class="reply-user">{{ reply.user.name }}</span>
                  <span class="reply-content">{{ reply.content }}</span>
                  <span class="reply-time">{{ reply.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-show="activeTab === 'notes'" class="notes-panel">
        <div class="empty-notes">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
          <p>暂无笔记</p>
          <span>观看视频时可随时记录学习笔记</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.course-detail {
  padding: 0;
}

.video-section {
  position: relative;
  background: #1e293b;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
}

.video-player {
  position: relative;
  aspect-ratio: 16 / 9;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}

.video-iframe {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  border-radius: 12px;
}

.danmaku-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.danmaku-item {
  position: absolute;
  white-space: nowrap;
  font-size: 16px;
  font-weight: 500;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  animation: danmakuMove 8s linear forwards;
}

@keyframes danmakuMove {
  from { transform: translateX(100%); right: 0; }
  to { transform: translateX(-100%); right: 100%; }
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 40px 16px 16px;
}

.progress-bar-wrap {
  margin-bottom: 12px;
}

.video-progress {
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  cursor: pointer;
}

.video-progress:hover {
  height: 6px;
}

.video-progress .progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 2px;
  transition: width 0.1s ease;
}

.controls-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.controls-left,
.controls-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.time-display {
  color: white;
  font-size: 13px;
  font-family: 'JetBrains Mono', monospace;
}

.danmaku-toggle {
  background: rgba(255, 255, 255, 0.1);
  padding: 6px 12px;
  border-radius: 16px;
}

.danmaku-input-wrap {
  position: absolute;
  bottom: 80px;
  left: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.8);
  border-radius: 12px;
  backdrop-filter: blur(8px);
}

.danmaku-input {
  flex: 1;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  outline: none;
}

.danmaku-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.send-danmaku {
  padding: 10px 20px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.course-info-section {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.course-title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.course-level {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
}

.course-level.beginner {
  background: #dcfce7;
  color: #15803d;
}

.course-level.intermediate {
  background: #fef3c7;
  color: #b45309;
}

.course-level.advanced {
  background: #fee2e2;
  color: #b91c1c;
}

.course-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.course-stats {
  display: flex;
  gap: 16px;
}

.course-stats .stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #64748b;
}

.course-stats .stat svg {
  color: #fbbf24;
}

.teacher-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 16px;
}

.teacher-avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.teacher-info {
  display: flex;
  flex-direction: column;
}

.teacher-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.teacher-title {
  font-size: 12px;
  color: #64748b;
}

.course-description {
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
  margin: 0 0 16px;
}

.course-direction-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border-radius: 10px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #4a6f9d;
}

.course-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.course-tags .tag {
  padding: 6px 14px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 12px;
  color: #475569;
}

.content-tabs {
  display: flex;
  border-bottom: 1px solid #e2e8f0;
  padding: 0 24px;
}

.tab-btn {
  padding: 16px 24px;
  background: none;
  border: none;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
}

.tab-btn:hover {
  color: #4a6f9d;
}

.tab-btn.active {
  color: #4a6f9d;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 24px;
  right: 24px;
  height: 2px;
  background: linear-gradient(90deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 1px;
}

.tab-content {
  padding: 24px;
}

.chapter-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chapter-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.chapter-item:hover {
  background: #f1f5f9;
}

.chapter-item.active {
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
}

.chapter-item.completed .chapter-status {
  color: #10b981;
}

.chapter-status {
  width: 32px;
  height: 32px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 13px;
  font-weight: 600;
}

.chapter-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chapter-title {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.chapter-duration {
  font-size: 12px;
  color: #94a3b8;
}

.play-btn {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;
}

.play-btn:hover {
  transform: scale(1.1);
}

.comment-input-wrap {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
  margin-bottom: 24px;
}

.user-avatar,
.comment-avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.input-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-area textarea {
  width: 100%;
  padding: 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #1e293b;
  resize: none;
  outline: none;
}

.input-area textarea:focus {
  border-color: #4a6f9d;
}

.submit-btn {
  align-self: flex-end;
  padding: 10px 24px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.3);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 14px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-user {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.comment-time {
  font-size: 12px;
  color: #94a3b8;
}

.comment-content {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin: 0 0 10px;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.comment-actions .action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.comment-actions .action-btn:hover {
  color: #4a6f9d;
}

.replies {
  margin-top: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
}

.reply-item {
  font-size: 13px;
  margin-bottom: 8px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-user {
  color: #4a6f9d;
  font-weight: 500;
}

.reply-content {
  color: #475569;
  margin: 0 8px;
}

.reply-time {
  color: #94a3b8;
  font-size: 11px;
}

.empty-notes {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #94a3b8;
  text-align: center;
}

.empty-notes p {
  font-size: 16px;
  font-weight: 500;
  color: #64748b;
  margin: 16px 0 8px;
}

.empty-notes span {
  font-size: 13px;
}
</style>
