<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useForumStore } from '../stores/forum'
import { useUserStore } from '../stores/user'
import FlowerPagination from '../components/FlowerPagination.vue'
import SearchBar from '../components/SearchBar.vue'

const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()

const posts = computed(() => forumStore.posts)

const formatTime = (timestamp) => {
  if (!timestamp) return '刚刚'
  const diff = Date.now() - Number(timestamp)
  if (diff < 1000 * 60) return '刚刚'
  if (diff < 1000 * 60 * 60) return `${Math.floor(diff / (1000 * 60))} 分钟前`
  if (diff < 1000 * 60 * 60 * 24) return `${Math.floor(diff / (1000 * 60 * 60))} 小时前`
  return new Date(timestamp).toLocaleDateString()
}

const resolveUserLevel = () => {
  const score = Number(userStore.points || 0)
  if (score >= 200) return 'LV.5'
  if (score >= 120) return 'LV.4'
  if (score >= 80) return 'LV.3'
  if (score >= 40) return 'LV.2'
  return 'LV.1'
}

const isComposerExpanded = ref(false)
const publishing = ref(false)

const postForm = reactive({
  topic: '',
  content: '',
  tag: '',
  quote: '',
})

const allTags = ['# 学习交流', '# 算法刷题', '# 面试经验', '# 校招求职', '# 实习分享', '# 技术讨论', '# 职场成长', '# 项目经验', '# 系统设计', '# 源码解析']
const tagInput = ref('')
const showTagDropdown = ref(false)
const filteredTags = computed(() => {
  if (!tagInput.value) return allTags
  const input = tagInput.value.toLowerCase().replace('#', '').trim()
  return allTags.filter(t => t.toLowerCase().includes(input))
})

watch(tagInput, (val) => {
  if (val.includes('#')) {
    showTagDropdown.value = true
  }
})

const selectTag = (tag) => {
  postForm.tag = tag
  tagInput.value = tag
  showTagDropdown.value = false
}

const createNewTag = () => {
  if (tagInput.value && !tagInput.value.startsWith('#')) {
    tagInput.value = '# ' + tagInput.value
  }
  postForm.tag = tagInput.value
  showTagDropdown.value = false
}

const handlePublish = async () => {
  if (!postForm.topic.trim() || !postForm.content.trim()) {
    ElMessage.warning('请填写标题和正文')
    return
  }

  publishing.value = true
  try {
    await forumStore.addPost({
      author: userStore.userInfo?.name || '同学',
      authorLevel: resolveUserLevel(),
      avatar: userStore.userInfo?.avatar || '',
      topic: postForm.topic.trim(),
      content: postForm.content.trim(),
      quote: postForm.quote.trim(),
      tag: postForm.tag.trim() || '# 学习交流',
    })
    postForm.topic = ''
    postForm.content = ''
    postForm.quote = ''
    tagInput.value = ''
    isComposerExpanded.value = false
    ElMessage.success('发布成功')
  } catch (error) {
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    publishing.value = false
  }
}

const POSTS_PER_PAGE = 15
const currentPage = ref(1)

const searchFilters = ref({
  keyword: '',
  sort: 'latest',
  category: 'all',
  time: 'all'
})

const filteredPosts = computed(() => {
  let result = posts.value
  if (searchFilters.value.keyword) {
    const keyword = searchFilters.value.keyword.toLowerCase()
    result = result.filter(post => 
      post.topic.toLowerCase().includes(keyword) ||
      post.content.toLowerCase().includes(keyword) ||
      post.tag.toLowerCase().includes(keyword)
    )
  }
  return result
})

const totalPages = computed(() => {
  const source = searchFilters.value.keyword ? filteredPosts.value : posts.value
  return Math.ceil(source.length / POSTS_PER_PAGE)
})

const paginatedPosts = computed(() => {
  const source = searchFilters.value.keyword ? filteredPosts.value : posts.value
  const start = (currentPage.value - 1) * POSTS_PER_PAGE
  return source.slice(start, start + POSTS_PER_PAGE)
})

const handleSearch = (filters) => {
  searchFilters.value = filters
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const searchBarAtBottom = ref(false)

const handleScroll = () => {
  const scrollTop = window.scrollY
  const windowHeight = window.innerHeight
  const docHeight = document.documentElement.scrollHeight
  const scrollBottom = scrollTop + windowHeight
  if (scrollBottom >= docHeight - 100) {
    searchBarAtBottom.value = true
  } else if (scrollTop < 200) {
    searchBarAtBottom.value = false
  }
}

const expandedPosts = ref(new Set())
const toggleExpand = (postId) => {
  if (expandedPosts.value.has(postId)) {
    expandedPosts.value.delete(postId)
  } else {
    expandedPosts.value.add(postId)
  }
}

const activeCommentPost = ref(null)
const quickComment = ref('')

const toggleCommentPanel = (postId) => {
  if (activeCommentPost.value === postId) {
    activeCommentPost.value = null
  } else {
    activeCommentPost.value = postId
    quickComment.value = ''
  }
}

const submitQuickComment = (postId) => {
  if (!quickComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  forumStore.addCommentCount(postId)
  activeCommentPost.value = null
  ElMessage.success('评论成功')
}

const likeEmojis = ['👍', '❤️', '😊', '🎉', '🔥', '👏', '😍', '🚀']
const showEmojiBurst = ref(null)
const burstPosition = ref({ x: 0, y: 0 })

const handleLike = (post, event) => {
  forumStore.toggleLike(post.id)
  if (!post.liked) {
    showEmojiBurst.value = post.id
    const rect = event.target.getBoundingClientRect()
    burstPosition.value = { x: rect.left + rect.width / 2, y: rect.top }
    setTimeout(() => {
      showEmojiBurst.value = null
    }, 1000)
  }
}

const goToPost = (postId) => {
  router.push(`/forum/${postId}`)
}

const highlightedPost = ref(null)

const hotQuestions = ref([
  { id: 1, title: '两数之和', difficulty: '简单', passRate: '52.3%', tags: ['数组', '哈希表'], hot: 9823 },
  { id: 2, title: '反转链表', difficulty: '简单', passRate: '71.2%', tags: ['链表'], hot: 8756 },
  { id: 3, title: '二叉树层序遍历', difficulty: '中等', passRate: '64.8%', tags: ['树', 'BFS'], hot: 7234 },
  { id: 4, title: '最长回文子串', difficulty: '中等', passRate: '35.6%', tags: ['字符串', 'DP'], hot: 6891 },
  { id: 5, title: '合并K个升序链表', difficulty: '困难', passRate: '28.4%', tags: ['链表', '堆'], hot: 5672 },
  { id: 6, title: '接雨水', difficulty: '困难', passRate: '42.1%', tags: ['栈', '双指针'], hot: 5234 },
  { id: 7, title: 'LRU缓存机制', difficulty: '中等', passRate: '38.9%', tags: ['设计', '哈希表'], hot: 4987 },
  { id: 8, title: '三数之和', difficulty: '中等', passRate: '31.2%', tags: ['数组', '双指针'], hot: 4756 },
  { id: 9, title: '最大子数组和', difficulty: '简单', passRate: '58.7%', tags: ['数组', 'DP'], hot: 4523 },
  { id: 10, title: '爬楼梯', difficulty: '简单', passRate: '68.3%', tags: ['DP'], hot: 4312 },
  { id: 11, title: '买卖股票最佳时机', difficulty: '简单', passRate: '62.1%', tags: ['数组', 'DP'], hot: 4098 },
  { id: 12, title: '全排列', difficulty: '中等', passRate: '72.4%', tags: ['回溯'], hot: 3876 },
  { id: 13, title: '二叉树最大深度', difficulty: '简单', passRate: '75.2%', tags: ['树', 'DFS'], hot: 3654 },
  { id: 14, title: '编辑距离', difficulty: '困难', passRate: '25.8%', tags: ['字符串', 'DP'], hot: 3421 },
  { id: 15, title: '正则表达式匹配', difficulty: '困难', passRate: '21.3%', tags: ['字符串', 'DP'], hot: 3198 },
])

const hotCurrentPage = ref(0)
const hotPageSize = 10
const hotTotalPages = computed(() => Math.ceil(hotQuestions.value.length / hotPageSize))
const paginatedHotQuestions = computed(() => {
  const start = hotCurrentPage.value * hotPageSize
  return hotQuestions.value.slice(start, start + hotPageSize)
})

const hotPrevPage = () => {
  if (hotCurrentPage.value > 0) hotCurrentPage.value--
}
const hotNextPage = () => {
  if (hotCurrentPage.value < hotTotalPages.value - 1) hotCurrentPage.value++
}

const hoveredHot = ref(null)
let hoverTimer = null

const handleHotHover = (question) => {
  clearTimeout(hoverTimer)
  hoverTimer = setTimeout(() => {
    hoveredHot.value = question.id
  }, 5000)
}

const handleHotLeave = () => {
  clearTimeout(hoverTimer)
  hoveredHot.value = null
}

const goToChallenge = (questionId) => {
  router.push(`/challenge/${questionId}`)
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  forumStore.fetchPosts()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="page-container forum-page">
    <h2 class="section-title">简版论坛</h2>

    <Transition name="search-slide">
      <SearchBar 
        v-if="!searchBarAtBottom"
        placeholder="搜索帖子、话题..."
        :is-sticky="false"
        @search="handleSearch"
      />
    </Transition>

    <div class="composer-wrapper">
      <div v-if="!isComposerExpanded" class="composer-collapsed" @click="isComposerExpanded = true">
        <div class="collapsed-left">
          <el-avatar :size="36" :src="userStore.userInfo?.avatar || ''">
            {{ (userStore.userInfo?.name || '同学').slice(0, 1) }}
          </el-avatar>
          <span class="collapsed-hint">{{ userStore.userInfo?.name || '同学' }}，有什么想分享的？</span>
        </div>
        <el-button type="primary" round size="small">发布</el-button>
      </div>

      <Transition name="composer-expand">
        <el-card v-if="isComposerExpanded" class="composer-card" shadow="never">
          <div class="composer-header">
            <el-avatar :size="40" :src="userStore.userInfo?.avatar || ''">
              {{ (userStore.userInfo?.name || '同学').slice(0, 1) }}
            </el-avatar>
            <div class="composer-user-info">
              <span class="composer-name">{{ userStore.userInfo?.name || '同学' }}</span>
              <span class="composer-level">{{ resolveUserLevel() }}</span>
            </div>
            <el-button link type="info" @click="isComposerExpanded = false">收起</el-button>
          </div>

          <input 
            v-model="postForm.topic" 
            class="topic-input" 
            placeholder="请输入标题..."
            maxlength="50"
          />

          <div class="editor-toolbar">
            <button class="toolbar-btn" title="加粗"><b>B</b></button>
            <button class="toolbar-btn" title="斜体"><i>I</i></button>
            <button class="toolbar-btn" title="标题">H</button>
            <button class="toolbar-btn" title="链接">🔗</button>
            <button class="toolbar-btn" title="图片">🖼️</button>
            <button class="toolbar-btn" title="代码">{ }</button>
            <button class="toolbar-btn" title="列表">≡</button>
            <button class="toolbar-btn" title="引用">"</button>
          </div>

          <textarea 
            v-model="postForm.content" 
            class="content-textarea" 
            placeholder="输入你想发布的论坛内容，支持 Markdown 格式..."
            maxlength="400"
          />

          <div class="form-row">
            <div class="tag-input-wrapper">
              <input 
                v-model="tagInput"
                class="tag-input" 
                placeholder="输入 # 添加话题标签..."
                @focus="showTagDropdown = true"
                @blur="setTimeout(() => showTagDropdown = false, 200)"
              />
              <Transition name="dropdown">
                <div v-if="showTagDropdown && (tagInput.includes('#') || !tagInput)" class="tag-dropdown">
                  <div 
                    v-for="tag in filteredTags" 
                    :key="tag" 
                    class="tag-option"
                    @mousedown="selectTag(tag)"
                  >
                    {{ tag }}
                  </div>
                  <div v-if="tagInput && !filteredTags.includes(tagInput)" class="tag-option create-new" @mousedown="createNewTag">
                    + 创建新话题 "{{ tagInput }}"
                  </div>
                </div>
              </Transition>
            </div>
            <input 
              v-model="postForm.quote" 
              class="quote-input" 
              placeholder="引用语（可选）"
              maxlength="80"
            />
          </div>

          <div class="publish-row">
            <el-button @click="isComposerExpanded = false">取消</el-button>
            <el-button type="primary" :loading="publishing" @click="handlePublish" round>发布帖子</el-button>
          </div>
        </el-card>
      </Transition>
    </div>

    <div class="forum-layout">
      <section class="feed-list">
        <el-card 
          v-for="item in paginatedPosts" 
          :key="item.id" 
          class="feed-card" 
          :class="{ 'is-highlighted': highlightedPost === item.id }"
          shadow="never"
          @click="goToPost(item.id)"
        >
          <div class="head-row">
            <div class="user-wrap">
              <el-avatar :size="44" :src="item.avatar || ''">{{ item.author.slice(0, 1) }}</el-avatar>
              <div>
                <div class="name-row">
                  <span class="name">{{ item.author }}</span>
                  <el-tag type="info" effect="light" size="small">{{ item.authorLevel || 'LV.1' }}</el-tag>
                </div>
                <div class="meta">算法社区 · {{ formatTime(item.createdAt) }}</div>
              </div>
            </div>
            <el-button link type="info">···</el-button>
          </div>

          <h3 class="topic">{{ item.topic }}</h3>
          
          <p class="content" :class="{ 'is-expanded': expandedPosts.has(item.id) }">
            {{ item.content }}
            <span v-if="!expandedPosts.has(item.id) && item.content.length > 80" class="full" @click.stop="toggleExpand(item.id)">...全文</span>
            <span v-if="expandedPosts.has(item.id)" class="collapse" @click.stop="toggleExpand(item.id)">收起</span>
          </p>

          <div class="quote" v-if="item.quote">{{ item.quote }}</div>
          <div class="hash" @click.stop>{{ item.tag }}</div>

          <div class="action-row">
            <button 
              class="action-btn like-btn" 
              :class="{ 'is-liked': item.liked }"
              @click.stop="handleLike(item, $event)"
            >
              <span class="btn-icon">👍</span>
              <span class="btn-count">{{ item.likes }}</span>
            </button>
            <button class="action-btn comment-btn" @click.stop="toggleCommentPanel(item.id)">
              <span class="btn-icon">💬</span>
              <span class="btn-count">{{ item.comments }}</span>
            </button>
          </div>

          <Transition name="comment-slide">
            <div v-if="activeCommentPost === item.id" class="quick-comment-panel" @click.stop>
              <input 
                v-model="quickComment" 
                class="quick-comment-input" 
                placeholder="写下你的评论..."
                @keyup.enter="submitQuickComment(item.id)"
              />
              <el-button type="primary" size="small" round @click="submitQuickComment(item.id)">发送</el-button>
            </div>
          </Transition>
        </el-card>
        
        <Transition name="search-slide">
          <SearchBar 
            v-if="searchBarAtBottom"
            placeholder="搜索帖子、话题..."
            :is-sticky="false"
            @search="handleSearch"
          />
        </Transition>
        
        <div v-if="totalPages > 1" class="pagination-wrapper">
          <FlowerPagination 
            :total="totalPages" 
            :defaultPage="currentPage"
            @change="handlePageChange"
          />
        </div>
      </section>

      <aside class="hot-sidebar">
        <div class="hot-card">
          <div class="hot-header">
            <h3 class="hot-title">🔥 今日热门题</h3>
            <div class="hot-nav">
              <button class="nav-arrow" :disabled="hotCurrentPage === 0" @click="hotPrevPage">‹</button>
              <button class="nav-arrow" :disabled="hotCurrentPage >= hotTotalPages - 1" @click="hotNextPage">›</button>
            </div>
          </div>
          <div class="hot-list">
            <div 
              v-for="(q, index) in paginatedHotQuestions" 
              :key="q.id" 
              class="hot-item"
              @mouseenter="handleHotHover(q)"
              @mouseleave="handleHotLeave"
              @click="goToChallenge(q.id)"
            >
              <span class="hot-rank">{{ hotCurrentPage * hotPageSize + index + 1 }}</span>
              <span class="hot-item-title">{{ q.title }}</span>
              <span class="hot-badge">HOT</span>
              
              <Transition name="tooltip">
                <div v-if="hoveredHot === q.id" class="hot-tooltip">
                  <div class="tooltip-row">
                    <span class="tooltip-label">难度</span>
                    <span class="tooltip-value" :class="q.difficulty">{{ q.difficulty }}</span>
                  </div>
                  <div class="tooltip-row">
                    <span class="tooltip-label">通过率</span>
                    <span class="tooltip-value">{{ q.passRate }}</span>
                  </div>
                  <div class="tooltip-row">
                    <span class="tooltip-label">标签</span>
                    <span class="tooltip-value tags">{{ q.tags.join(' · ') }}</span>
                  </div>
                </div>
              </Transition>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <Transition name="emoji-burst">
      <div v-if="showEmojiBurst" class="emoji-burst-container" :style="{ left: burstPosition.x + 'px', top: burstPosition.y + 'px' }">
        <span v-for="(emoji, i) in likeEmojis" :key="i" class="burst-emoji" :style="{ animationDelay: i * 0.05 + 's' }">
          {{ emoji }}
        </span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.forum-page {
  padding-bottom: 20px;
  display: grid;
  gap: 8px;
}

.composer-wrapper {
  margin-bottom: 8px;
}

.composer-collapsed {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid var(--line-soft);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.composer-collapsed:hover {
  border-color: rgba(74, 144, 217, 0.3);
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.1);
}

.collapsed-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapsed-hint {
  color: var(--text-sub);
  font-size: 15px;
}

.composer-card {
  border: 2px solid rgba(74, 144, 217, 0.2);
  border-radius: 16px;
}

.composer-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.composer-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.composer-name {
  font-weight: 600;
  color: var(--text-title);
}

.composer-level {
  font-size: 12px;
  color: var(--text-sub);
}

.topic-input {
  width: 100%;
  padding: 12px 0;
  border: none;
  border-bottom: 1px solid var(--line-soft);
  font-size: 20px;
  font-weight: 700;
  color: var(--text-title);
  outline: none;
  background: transparent;
}

.topic-input::placeholder {
  color: var(--text-sub);
  font-weight: 400;
}

.editor-toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 0;
  border-bottom: 1px solid var(--line-soft);
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-sub);
  font-size: 14px;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: rgba(74, 144, 217, 0.1);
  color: #4a90d9;
}

.content-textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px 0;
  border: none;
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-main);
  outline: none;
  resize: vertical;
  background: transparent;
}

.content-textarea::placeholder {
  color: var(--text-sub);
}

.form-row {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.tag-input-wrapper {
  position: relative;
  flex: 1;
}

.tag-input, .quote-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-main);
  outline: none;
  transition: all 0.2s;
}

.tag-input:focus, .quote-input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 3px rgba(74, 144, 217, 0.1);
}

.tag-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
}

.tag-option {
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.15s;
}

.tag-option:hover {
  background: rgba(74, 144, 217, 0.1);
  color: #4a90d9;
}

.tag-option.create-new {
  border-top: 1px solid var(--line-soft);
  color: #4a90d9;
  font-weight: 500;
}

.publish-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.forum-layout {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 16px;
}

.feed-list {
  display: grid;
  gap: 10px;
}

.feed-card {
  border-radius: 16px;
  border: 1px solid var(--line-soft);
  cursor: pointer;
  transition: all 0.3s ease;
}

.feed-card:hover {
  border-color: rgba(74, 144, 217, 0.3);
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.1);
}

.feed-card.is-highlighted {
  border-color: #4a90d9;
  box-shadow: 0 0 0 3px rgba(74, 144, 217, 0.2);
}

.head-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-title);
}

.meta {
  color: var(--text-sub);
  font-size: 13px;
}

.topic {
  margin-top: 12px;
  font-size: 30px;
  line-height: 1.2;
  font-weight: 800;
  color: var(--text-title);
}

.content {
  margin-top: 10px;
  font-size: 20px;
  line-height: 1.5;
  color: var(--text-main);
  max-height: 80px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.content.is-expanded {
  max-height: 500px;
}

.full, .collapse {
  color: #4a90d9;
  font-weight: 600;
  cursor: pointer;
}

.full:hover, .collapse:hover {
  text-decoration: underline;
}

.quote {
  margin-top: 12px;
  border-left: 4px solid var(--line-soft);
  padding-left: 10px;
  color: var(--text-muted);
  font-style: italic;
}

.hash {
  margin-top: 10px;
  display: inline-block;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  padding: 4px 12px;
  color: var(--text-sub);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.hash:hover {
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
  border-color: transparent;
}

.action-row {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-sub);
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(74, 144, 217, 0.1);
  color: #4a90d9;
}

.action-btn.is-liked {
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
}

.btn-icon {
  font-size: 16px;
}

.quick-comment-panel {
  margin-top: 12px;
  padding: 12px;
  background: rgba(74, 144, 217, 0.05);
  border-radius: 12px;
  display: flex;
  gap: 10px;
}

.quick-comment-input {
  flex: 1;
  padding: 8px 14px;
  border: 1px solid var(--line-soft);
  border-radius: 20px;
  font-size: 14px;
  outline: none;
}

.quick-comment-input:focus {
  border-color: #4a90d9;
}

.hot-sidebar {
  position: sticky;
  top: 92px;
  height: fit-content;
}

.hot-card {
  width: 400px;
  height: 420px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid var(--line-soft);
  border-radius: 16px;
  padding: 16px;
  overflow: hidden;
}

.hot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.hot-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-title);
}

.hot-nav {
  display: flex;
  gap: 4px;
}

.nav-arrow {
  width: 24px;
  height: 24px;
  border: 1px solid var(--line-soft);
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-sub);
  transition: all 0.2s;
}

.nav-arrow:hover:not(:disabled) {
  background: #4a90d9;
  color: white;
  border-color: #4a90d9;
}

.nav-arrow:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 380px;
  overflow: hidden;
}

.hot-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.hot-item:hover {
  background: rgba(74, 144, 217, 0.08);
}

.hot-rank {
  width: 20px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-sub);
  text-align: center;
}

.hot-item:nth-child(-n+3) .hot-rank {
  color: #ff6b6b;
}

.hot-item-title {
  flex: 1;
  font-size: 13px;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-badge {
  padding: 2px 6px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 4px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.hot-tooltip {
  position: absolute;
  right: 0;
  top: 100%;
  z-index: 100;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  padding: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  min-width: 160px;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.tooltip-row:last-child {
  margin-bottom: 0;
}

.tooltip-label {
  color: var(--text-sub);
  font-size: 12px;
}

.tooltip-value {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-main);
}

.tooltip-value.简单 { color: #52c41a; }
.tooltip-value.中等 { color: #faad14; }
.tooltip-value.困难 { color: #ff4d4f; }

.tooltip-value.tags {
  color: #4a90d9;
}

.pagination-wrapper {
  margin-top: 8px;
  padding: 12px 0;
  display: flex;
  justify-content: center;
}

.emoji-burst-container {
  position: fixed;
  z-index: 9999;
  pointer-events: none;
  display: flex;
  gap: 4px;
}

.burst-emoji {
  font-size: 24px;
  animation: burst-up 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@keyframes burst-up {
  0% {
    opacity: 1;
    transform: translateY(0) scale(0.5);
  }
  100% {
    opacity: 0;
    transform: translateY(-60px) scale(1.2);
  }
}

.composer-expand-enter-active {
  animation: composer-in 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.composer-expand-leave-active {
  animation: composer-in 0.2s ease-in reverse;
}

@keyframes composer-in {
  0% {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.search-slide-enter-active {
  animation: search-in 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.search-slide-leave-active {
  animation: search-in 0.25s ease-in reverse;
}

@keyframes search-in {
  0% {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.comment-slide-enter-active,
.comment-slide-leave-active {
  transition: all 0.3s ease;
}

.comment-slide-enter-from,
.comment-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.tooltip-enter-active,
.tooltip-leave-active {
  transition: all 0.2s ease;
}

.tooltip-enter-from,
.tooltip-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.emoji-burst-enter-active,
.emoji-burst-leave-active {
  transition: opacity 0.1s;
}

.emoji-burst-enter-from,
.emoji-burst-leave-to {
  opacity: 0;
}

@media (max-width: 1200px) {
  .forum-layout {
    grid-template-columns: 1fr;
  }

  .hot-sidebar {
    display: none;
  }
}

@media (max-width: 900px) {
  .name {
    font-size: 18px;
  }

  .topic {
    font-size: 24px;
  }

  .content {
    font-size: 16px;
  }
}
</style>
