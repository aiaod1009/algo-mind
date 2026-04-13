<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useForumStore } from '../stores/forum'
import { useUserStore } from '../stores/user'
import FlowerPagination from '../components/FlowerPagination.vue'
import SearchBar from '../components/SearchBar.vue'
import AuthorLevelBadge from '../components/AuthorLevelBadge.vue'
import api from '../api'
import { getFullFileUrl } from '../utils/file'
import likeIcon from '../assets/icons/like.svg'
import { normalizeAuthorLevelProfile } from '../constants/authorLevelThemes'

const router = useRouter()
const forumStore = useForumStore()
const userStore = useUserStore()

const posts = computed(() => forumStore.posts)
const currentAuthorLevelProfile = computed(() =>
  normalizeAuthorLevelProfile(
    userStore.userInfo?.authorLevelProfile,
    userStore.userInfo?.authorLevelCode || userStore.userInfo?.authorLevel || 'Lv.1',
  ),
)
const resolveAvatar = (avatar) => getFullFileUrl(avatar || '')

const isCurrentUsersPost = (post) => {
  const currentUserId = userStore.userInfo?.id
  const currentName = (userStore.userInfo?.name || '').trim()
  const currentUserAvatar = resolveAvatar(userStore.userInfo?.avatar)
  const postAvatar = resolveAvatar(post?.avatar)

  const sameUserById = currentUserId && post?.userId && Number(post.userId) === Number(currentUserId)
  const sameUserByName = currentName && post?.author === currentName
  const sameUserByAvatar = currentUserAvatar && postAvatar && currentUserAvatar === postAvatar

  return Boolean(sameUserById || sameUserByName || sameUserByAvatar)
}

const resolvePostAvatar = (post) => {
  const currentUserAvatar = resolveAvatar(userStore.userInfo?.avatar)

  // 对当前用户自己发的帖子，优先使用当前头像，避免旧帖子里的坏链接影响展示
  if (isCurrentUsersPost(post) && currentUserAvatar) {
    return currentUserAvatar
  }

  const postAvatar = resolveAvatar(post?.avatar)
  if (postAvatar) return postAvatar

  return ''
}

const resolvePostAuthor = (post) => {
  const currentName = (userStore.userInfo?.name || '').trim()
  if (!currentName) return post?.author || '同学'

  if (isCurrentUsersPost(post)) {
    return currentName
  }

  return post?.author || '同学'
}

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
const contentTextarea = ref(null)
const showEmojiPicker = ref(false)

const postForm = reactive({
  topic: '',
  content: '',
  tag: '',
  quote: '',
})

const allTags = [
  { name: '# 学习交流', icon: '📚', color: '#4a90d9' },
  { name: '# 算法刷题', icon: '💻', color: '#52c41a' },
  { name: '# 面试经验', icon: '🎯', color: '#faad14' },
  { name: '# 校招求职', icon: '🏢', color: '#722ed1' },
  { name: '# 实习分享', icon: '📝', color: '#13c2c2' },
  { name: '# 技术讨论', icon: '⚙️', color: '#eb2f96' },
  { name: '# 职场成长', icon: '📈', color: '#fa8c16' },
  { name: '# 项目经验', icon: '🚀', color: '#2f54eb' },
  { name: '# 系统设计', icon: '🏗️', color: '#1890ff' },
  { name: '# 源码解析', icon: '🔍', color: '#f5222d' },
]
const tagInput = ref('')
const showTagDropdown = ref(false)
const filteredTags = computed(() => {
  if (!tagInput.value) return allTags
  const input = tagInput.value.toLowerCase().replace('#', '').trim()
  return allTags.filter(t => t.name.toLowerCase().includes(input))
})

// 删除菜单相关状态
const activePostMenu = ref(null)
const showPostMenu = (postId) => {
  activePostMenu.value = activePostMenu.value === postId ? null : postId
}

const hidePostMenu = () => {
  activePostMenu.value = null
}

// 点击外部关闭菜单
const handleClickOutside = (e) => {
  const menuWrappers = document.querySelectorAll('.post-menu-wrapper')
  let clickedInside = false
  menuWrappers.forEach(wrapper => {
    if (wrapper.contains(e.target)) {
      clickedInside = true
    }
  })
  if (!clickedInside) {
    hidePostMenu()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

const handleDeletePost = async (post) => {
  if (!isCurrentUsersPost(post)) {
    ElMessage.error('只能删除自己的帖子')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除这条帖子吗？删除后无法恢复。', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const result = await forumStore.deletePost(post.id)
    if (result.success) {
      ElMessage.success('帖子已删除')
      hidePostMenu()
    } else {
      ElMessage.error(result.error || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const emojiList = ['😀', '😂', '🥰', '😎', '🤔', '👍', '👏', '🎉', '🔥', '❤️', '💯', '✨', '🚀', '💪', '🙏', '😊']
const recentEmojis = ref([])

const insertEmoji = (emoji) => {
  postForm.content += emoji
  if (!recentEmojis.value.includes(emoji)) {
    recentEmojis.value.unshift(emoji)
    if (recentEmojis.value.length > 8) recentEmojis.value.pop()
  }
  showEmojiPicker.value = false
}

const toolbarActions = {
  bold: { prefix: '**', suffix: '**', placeholder: '加粗文本' },
  italic: { prefix: '*', suffix: '*', placeholder: '斜体文本' },
  heading: { prefix: '## ', suffix: '', placeholder: '标题' },
  link: { prefix: '[', suffix: '](url)', placeholder: '链接文字' },
  image: { prefix: '![', suffix: '](图片URL)', placeholder: '图片描述' },
  code: { prefix: '```\n', suffix: '\n```', placeholder: '代码' },
  list: { prefix: '- ', suffix: '', placeholder: '列表项' },
  quote: { prefix: '> ', suffix: '', placeholder: '引用文字' },
}

const insertFormat = (type) => {
  const action = toolbarActions[type]
  if (!action || !contentTextarea.value) return

  const textarea = contentTextarea.value
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = postForm.content.substring(start, end) || action.placeholder

  const before = postForm.content.substring(0, start)
  const after = postForm.content.substring(end)
  const newText = action.prefix + selectedText + action.suffix

  postForm.content = before + newText + after

  nextTick(() => {
    textarea.focus()
    const newPos = start + action.prefix.length
    textarea.setSelectionRange(newPos, newPos + selectedText.length)
  })
}

const topicCharCount = computed(() => postForm.topic.length)
const contentCharCount = computed(() => postForm.content.length)

const closeTagDropdown = () => {
  setTimeout(() => {
    showTagDropdown.value = false
  }, 150)
}

const selectTag = (tag) => {
  postForm.tag = tag.name
  tagInput.value = tag.name
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
    const result = await forumStore.addPost({
      author: userStore.userInfo?.name || '同学',
      authorLevel: currentAuthorLevelProfile.value.shortLabel,
      avatar: userStore.userInfo?.avatar || '',
      topic: postForm.topic.trim(),
      content: postForm.content.trim(),
      quote: postForm.quote.trim(),
      tag: postForm.tag.trim() || '# 学习交流',
    })

    if (result.success) {
      postForm.topic = ''
      postForm.content = ''
      postForm.quote = ''
      tagInput.value = ''
      isComposerExpanded.value = false
      if (result.synced) {
        ElMessage.success('发布成功')
      } else {
        ElMessage.warning('已保存到本地，服务器同步失败')
      }
    } else {
      ElMessage.error(result.error || '发布失败，请稍后重试')
    }
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

const normalizedKeyword = computed(() => searchFilters.value.keyword.trim().toLowerCase())

const filteredSource = computed(() => {
  let result = posts.value
  if (normalizedKeyword.value) {
    const keyword = normalizedKeyword.value
    result = result.filter(post =>
      post.topic.toLowerCase().includes(keyword) ||
      post.content.toLowerCase().includes(keyword) ||
      post.tag.toLowerCase().includes(keyword)
    )
  }
  return result
})

const totalPages = computed(() => {
  return Math.ceil(filteredSource.value.length / POSTS_PER_PAGE)
})

const paginatedPosts = computed(() => {
  const start = (currentPage.value - 1) * POSTS_PER_PAGE
  return filteredSource.value.slice(start, start + POSTS_PER_PAGE)
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
let scrollRafId = 0

const updateSearchBarPosition = () => {
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

const handleScroll = () => {
  if (scrollRafId) return
  scrollRafId = window.requestAnimationFrame(() => {
    updateSearchBarPosition()
    scrollRafId = 0
  })
}

const expandedPosts = ref(new Set())
const toggleExpand = (postId) => {
  if (expandedPosts.value.has(postId)) {
    expandedPosts.value.delete(postId)
  } else {
    expandedPosts.value.add(postId)
  }
}

const postHotComments = ref({})
const hotCommentIndex = ref({})
const hotCommentTimers = ref({})

const fetchHotComments = async (postId) => {
  try {
    const res = await api.get(`/forum-posts/${postId}/hot-comments`)
    if (res.data?.code === 0 && res.data?.data) {
      postHotComments.value[postId] = res.data.data
      if (res.data.data.length > 1) {
        hotCommentIndex.value[postId] = 0
        startHotCommentTimer(postId)
      }
    }
  } catch (e) {
    console.error('获取热门评论失败:', e)
  }
}

const startHotCommentTimer = (postId) => {
  if (hotCommentTimers.value[postId]) {
    clearInterval(hotCommentTimers.value[postId])
  }
  hotCommentTimers.value[postId] = setInterval(() => {
    const comments = postHotComments.value[postId]
    if (comments && comments.length > 1) {
      const currentIndex = hotCommentIndex.value[postId] || 0
      hotCommentIndex.value[postId] = (currentIndex + 1) % comments.length
    }
  }, 10000)
}

const stopHotCommentTimer = (postId) => {
  if (hotCommentTimers.value[postId]) {
    clearInterval(hotCommentTimers.value[postId])
    delete hotCommentTimers.value[postId]
  }
}

const likeEmojis = ['👍', '❤️', '😊', '🎉', '🔥', '👏', '😍', '🚀']
const showEmojiBurst = ref(null)
const burstPosition = ref({ x: 0, y: 0 })

const handleLike = async (post, event) => {
  const wasLiked = post.liked
  const rect = event?.currentTarget?.getBoundingClientRect?.()
  const result = await forumStore.toggleLike(post.id)
  if (!result?.success) {
    if (!result?.notified) {
      ElMessage.error(result?.error || '点赞失败，请稍后再试')
    }
    return
  }

  if (!wasLiked && result.liked) {
    showEmojiBurst.value = post.id
    if (rect) {
      burstPosition.value = { x: rect.left + rect.width / 2, y: rect.top + rect.height / 2 }
    }
    setTimeout(() => {
      showEmojiBurst.value = null
    }, 1000)
  }
}

const goToPost = (postId) => {
  router.push({ name: 'forum-post', params: { id: postId } })
}

const goToPostComments = (postId) => {
  router.push({ name: 'forum-post', params: { id: postId }, hash: '#comments' })
}

const goToPostWithComment = (postId, commentId) => {
  router.push({
    name: 'forum-post',
    params: { id: postId },
    query: { commentId: commentId },
    hash: '#comments'
  })
}

const highlightedPost = ref(null)

const hotQuestions = ref([])
const hotLoading = ref(false)

const fetchHotQuestions = async () => {
  hotLoading.value = true
  try {
    const res = await api.get('/hot-questions')
    if (res.data?.code === 0 && Array.isArray(res.data?.data)) {
      hotQuestions.value = res.data.data
    }
  } catch (e) {
    console.error('获取热门题目失败:', e)
  } finally {
    hotLoading.value = false
  }
}

const DEFAULT_HOT_QUESTIONS = [
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
]

const displayHotQuestions = computed(() => {
  return hotQuestions.value.length > 0 ? hotQuestions.value : DEFAULT_HOT_QUESTIONS
})

const hotCurrentPage = ref(0)
const hotPageSize = 10
const hotTotalPages = computed(() => Math.ceil(displayHotQuestions.value.length / hotPageSize))
const paginatedHotQuestions = computed(() => {
  const start = hotCurrentPage.value * hotPageSize
  return displayHotQuestions.value.slice(start, start + hotPageSize)
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

onMounted(async () => {
  fetchHotQuestions()
  window.addEventListener('scroll', handleScroll, { passive: true })
  forumStore.hydratePostsFromLocal()
  updateSearchBarPosition()
  await forumStore.fetchPosts({ skipIfLoaded: false })
  paginatedPosts.value.forEach(post => {
    if (post.comments > 0) {
      fetchHotComments(post.id)
    }
  })
})

watch(currentPage, async () => {
  await nextTick()
  paginatedPosts.value.forEach(post => {
    if (post.comments > 0 && !postHotComments.value[post.id]) {
      fetchHotComments(post.id)
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (scrollRafId) {
    window.cancelAnimationFrame(scrollRafId)
    scrollRafId = 0
  }
  clearTimeout(hoverTimer)
  Object.keys(hotCommentTimers.value).forEach(postId => {
    stopHotCommentTimer(postId)
  })
})
</script>

<template>
  <div class="page-container forum-page">
    <h2 class="section-title">简版论坛</h2>

    <Transition name="search-slide">
      <SearchBar v-if="!searchBarAtBottom" placeholder="搜索帖子、话题..." :is-sticky="false" @search="handleSearch" />
    </Transition>

    <div class="composer-wrapper">
      <div v-if="!isComposerExpanded" class="composer-collapsed" @click="isComposerExpanded = true">
        <div class="collapsed-left">
          <el-avatar :size="40" :src="userStore.avatar || ''" class="collapsed-avatar">
            {{ (userStore.userInfo?.name || '同学').slice(0, 1) }}
          </el-avatar>
          <div class="collapsed-content">
            <span class="collapsed-name">{{ userStore.userInfo?.name || '同学' }}</span>
            <span class="collapsed-hint">分享你的想法、经验或问题...</span>
          </div>
        </div>
        <div class="collapsed-actions">
          <el-button type="primary" round size="default" class="publish-btn-collapsed">
            <span class="btn-icon">📝</span> 发布帖子
          </el-button>
        </div>
      </div>

      <Transition name="composer-expand">
        <div v-if="isComposerExpanded" class="composer-card">
          <div class="composer-header">
            <div class="header-left">
              <el-avatar :size="48" :src="userStore.avatar || ''" class="composer-avatar">
                {{ (userStore.userInfo?.name || '同学').slice(0, 1) }}
              </el-avatar>
              <div class="composer-user-info">
                <span class="composer-name">{{ userStore.userInfo?.name || '同学' }}</span>
                <div class="composer-badges">
                  <AuthorLevelBadge :profile="currentAuthorLevelProfile" size="sm" />
                  <span class="composer-role">社区成员</span>
                </div>
              </div>
            </div>
            <button class="collapse-btn" @click="isComposerExpanded = false" title="收起">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div class="topic-input-wrapper">
            <input v-model="postForm.topic" class="topic-input" placeholder="输入一个吸引人的标题..." maxlength="50" />
            <span class="char-counter" :class="{ 'is-warning': topicCharCount > 40 }">
              {{ topicCharCount }}/50
            </span>
          </div>

          <div class="editor-toolbar">
            <div class="toolbar-group">
              <button class="toolbar-btn" title="加粗 (Ctrl+B)" @click="insertFormat('bold')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M15.6 10.79c.97-.67 1.65-1.77 1.65-2.79 0-2.26-1.75-4-4-4H7v14h7.04c2.09 0 3.71-1.7 3.71-3.79 0-1.52-.86-2.82-2.15-3.42zM10 6.5h3c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5h-3v-3zm3.5 9H10v-3h3.5c.83 0 1.5.67 1.5 1.5s-.67 1.5-1.5 1.5z" />
                </svg>
              </button>
              <button class="toolbar-btn" title="斜体 (Ctrl+I)" @click="insertFormat('italic')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M10 4v3h2.21l-3.42 8H6v3h8v-3h-2.21l3.42-8H18V4z" />
                </svg>
              </button>
              <button class="toolbar-btn" title="标题" @click="insertFormat('heading')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M5 4v3h5.5v12h3V7H19V4z" />
                </svg>
              </button>
            </div>
            <div class="toolbar-divider"></div>
            <div class="toolbar-group">
              <button class="toolbar-btn" title="插入链接" @click="insertFormat('link')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h4v-1.9H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z" />
                </svg>
              </button>
              <button class="toolbar-btn" title="插入图片" @click="insertFormat('image')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z" />
                </svg>
              </button>
              <button class="toolbar-btn" title="代码块" @click="insertFormat('code')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z" />
                </svg>
              </button>
            </div>
            <div class="toolbar-divider"></div>
            <div class="toolbar-group">
              <button class="toolbar-btn" title="列表" @click="insertFormat('list')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M4 10.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5zm0-6c-.83 0-1.5.67-1.5 1.5S3.17 7.5 4 7.5 5.5 6.83 5.5 6 4.83 4.5 4 4.5zm0 12c-.83 0-1.5.68-1.5 1.5s.68 1.5 1.5 1.5 1.5-.68 1.5-1.5-.67-1.5-1.5-1.5zM7 19h14v-2H7v2zm0-6h14v-2H7v2zm0-8v2h14V5H7z" />
                </svg>
              </button>
              <button class="toolbar-btn" title="引用" @click="insertFormat('quote')">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z" />
                </svg>
              </button>
            </div>
            <div class="toolbar-spacer"></div>
            <div class="toolbar-group emoji-group">
              <button class="toolbar-btn emoji-btn" title="插入表情" @click="showEmojiPicker = !showEmojiPicker">
                <span>😊</span>
              </button>
              <Transition name="emoji-dropdown">
                <div v-if="showEmojiPicker" class="emoji-picker">
                  <div class="emoji-header">
                    <span class="emoji-title">常用表情</span>
                  </div>
                  <div class="emoji-grid">
                    <button v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">
                      {{ emoji }}
                    </button>
                  </div>
                  <div v-if="recentEmojis.length > 0" class="emoji-recent">
                    <span class="recent-label">最近使用</span>
                    <div class="recent-grid">
                      <button v-for="emoji in recentEmojis" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">
                        {{ emoji }}
                      </button>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>
          </div>

          <div class="content-wrapper">
            <textarea ref="contentTextarea" v-model="postForm.content" class="content-textarea"
              placeholder="分享你的想法、经验或问题...&#10;&#10;支持 Markdown 格式，让内容更丰富！" maxlength="2000" />
            <div class="content-footer">
              <span class="content-hint">支持 Markdown 格式</span>
              <span class="char-counter"
                :class="{ 'is-warning': contentCharCount > 1800, 'is-danger': contentCharCount > 1950 }">
                {{ contentCharCount }}/2000
              </span>
            </div>
          </div>

          <div class="form-row">
            <div class="tag-input-wrapper">
              <div class="tag-input-icon">#</div>
              <input v-model="tagInput" class="tag-input" placeholder="选择或创建话题标签..." @focus="showTagDropdown = true"
                @click="showTagDropdown = true" @blur="closeTagDropdown" />
              <Transition name="dropdown">
                <div v-if="showTagDropdown" class="tag-dropdown" @mousedown.stop>
                  <div class="dropdown-header">
                    <span class="dropdown-title">热门话题</span>
                    <button class="dropdown-close" @mousedown.prevent="showTagDropdown = false">×</button>
                  </div>
                  <div class="tag-list">
                    <div v-for="tag in filteredTags" :key="tag.name" class="tag-option"
                      :style="{ '--tag-color': tag.color }" @mousedown.prevent="selectTag(tag)">
                      <span class="tag-icon">{{ tag.icon }}</span>
                      <span class="tag-name">{{ tag.name }}</span>
                    </div>
                  </div>
                  <div v-if="tagInput && !filteredTags.find(t => t.name === tagInput)" class="create-new-tag"
                    @mousedown.prevent="createNewTag">
                    <span class="create-icon">✨</span>
                    <span>创建新话题 "{{ tagInput }}"</span>
                  </div>
                </div>
              </Transition>
            </div>
            <div class="quote-input-wrapper">
              <span class="quote-icon">💭</span>
              <input v-model="postForm.quote" class="quote-input" placeholder="添加引用语（可选）" maxlength="80" />
            </div>
          </div>

          <div class="publish-row">
            <div class="publish-hints">
              <span class="hint-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" />
                </svg>
                发布后可编辑
              </span>
              <span class="hint-item">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z" />
                </svg>
                内容安全审核
              </span>
            </div>
            <div class="publish-actions">
              <button class="cancel-btn" @click="isComposerExpanded = false">取消</button>
              <button class="publish-btn" :class="{ 'is-disabled': !postForm.topic.trim() || !postForm.content.trim() }"
                :disabled="!postForm.topic.trim() || !postForm.content.trim()" @click="handlePublish">
                <span v-if="publishing" class="loading-spinner"></span>
                <span v-else class="btn-icon">🚀</span>
                {{ publishing ? '发布中...' : '发布帖子' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </div>

    <div class="forum-layout">
      <section class="feed-list">
        <div v-if="forumStore.isFromCache" class="cache-warning">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          <span>当前展示的是本地缓存数据，服务器连接失败</span>
        </div>
        <el-card v-for="item in paginatedPosts" :key="item.id" class="feed-card"
          :class="{ 'is-highlighted': highlightedPost === item.id }" shadow="never">
          <div class="head-row">
            <div class="user-wrap">
              <el-avatar :size="44" :src="resolvePostAvatar(item)">{{ resolvePostAuthor(item).slice(0, 1) }}</el-avatar>
              <div>
                <div class="name-row">
                  <span class="name">{{ resolvePostAuthor(item) }}</span>
                  <AuthorLevelBadge :profile="item.authorLevelProfile" :raw-label="item.authorLevel || 'Lv.1'" size="sm" />
                </div>
                <div class="meta">算法社区 · {{ formatTime(item.createdAt) }}</div>
              </div>
            </div>
            <div class="post-menu-wrapper" v-if="isCurrentUsersPost(item)">
              <el-button link type="info" @click.stop="showPostMenu(item.id)">···</el-button>
              <Transition name="menu-dropdown">
                <div v-if="activePostMenu === item.id" class="post-menu-dropdown">
                  <div class="menu-item delete" @click.stop="handleDeletePost(item)">
                    <span class="menu-icon">🗑️</span>
                    <span class="menu-text">删除帖子</span>
                  </div>
                </div>
              </Transition>
            </div>
          </div>

          <h3 class="topic topic-link" @click="goToPost(item.id)">{{ item.topic }}</h3>

          <p class="content" :class="{ 'is-expanded': expandedPosts.has(item.id) }">
            {{ item.content }}
            <span v-if="!expandedPosts.has(item.id) && item.content.length > 80" class="full"
              @click.stop="toggleExpand(item.id)">...全文</span>
            <span v-if="expandedPosts.has(item.id)" class="collapse" @click.stop="toggleExpand(item.id)">收起</span>
          </p>

          <div class="quote" v-if="item.quote">{{ item.quote }}</div>
          <div class="hash" @click.stop>{{ item.tag }}</div>

          <div v-if="item.comments > 0" class="hot-comments-section">
            <div v-if="postHotComments[item.id]?.length > 0" class="hot-comments-carousel">
              <div class="hot-comment-wrapper">
                <Transition name="hot-comment-slide" mode="out-in">
                  <div :key="hotCommentIndex[item.id] || 0" class="hot-comment-item"
                    @click.stop="goToPostWithComment(item.id, postHotComments[item.id][hotCommentIndex[item.id] || 0]?.id)">
                    <div class="hot-comment-header">
                      <el-avatar :size="24"
                        :src="resolveAvatar(postHotComments[item.id][hotCommentIndex[item.id] || 0]?.avatar) || ''">
                        {{ (postHotComments[item.id][hotCommentIndex[item.id] || 0]?.author || '匿').slice(0, 1) }}
                      </el-avatar>
                      <span class="hot-comment-author">{{ postHotComments[item.id][hotCommentIndex[item.id] ||
                        0]?.author }}</span>
                      <span class="hot-comment-likes">👍 {{ postHotComments[item.id][hotCommentIndex[item.id] ||
                        0]?.likes || 0 }}</span>
                    </div>
                    <div class="hot-comment-content">{{ postHotComments[item.id][hotCommentIndex[item.id] || 0]?.content
                    }}</div>
                  </div>
                </Transition>
              </div>
              <div v-if="postHotComments[item.id].length > 1" class="hot-comment-dots">
                <span v-for="(_, idx) in postHotComments[item.id]" :key="idx" class="dot"
                  :class="{ active: idx === (hotCommentIndex[item.id] || 0) }"
                  @click.stop="hotCommentIndex[item.id] = idx"></span>
              </div>
            </div>
          </div>

          <div class="action-row">
            <button class="action-btn like-btn" :class="{ 'is-liked': item.liked }"
              @click.stop="handleLike(item, $event)">
              <img class="btn-icon like-icon" :src="likeIcon" alt="点赞" />
              <span class="btn-count">{{ item.likes }}</span>
            </button>
            <button class="action-btn comment-btn" @click.stop="goToPostComments(item.id)">
              <span class="btn-icon">💬</span>
              <span class="btn-count">{{ item.comments }}</span>
            </button>
          </div>
        </el-card>

        <Transition name="search-slide">
          <SearchBar v-if="searchBarAtBottom" placeholder="搜索帖子、话题..." :is-sticky="false" @search="handleSearch" />
        </Transition>

        <div v-if="totalPages > 1" class="pagination-wrapper">
          <FlowerPagination :total="totalPages" :defaultPage="currentPage" @change="handlePageChange" />
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
            <div v-for="(q, index) in paginatedHotQuestions" :key="q.id" class="hot-item"
              @mouseenter="handleHotHover(q)" @mouseleave="handleHotLeave" @click="goToChallenge(q.id)">
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
      <div v-if="showEmojiBurst" class="like-celebration"
        :style="{ left: burstPosition.x + 'px', top: burstPosition.y + 'px' }">
        <div class="celebration-ring"></div>
        <div class="celebration-glow"></div>
        <div class="celebration-particles">
          <span v-for="i in 12" :key="'p' + i" class="particle" :style="{ '--i': i }"></span>
        </div>
        <div class="celebration-emojis">
          <span v-for="(emoji, i) in likeEmojis" :key="i" class="burst-emoji" :style="{ '--i': i }">
            {{ emoji }}
          </span>
        </div>
        <div class="celebration-sparks">
          <span v-for="i in 6" :key="'s' + i" class="spark" :style="{ '--i': i }"></span>
        </div>
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

.cache-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 8px;
  color: #d46b08;
  font-size: 13px;
  margin-bottom: 12px;
}

.composer-wrapper {
  margin-bottom: 8px;
}

.composer-collapsed {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  border: 1px solid var(--line-soft);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.composer-collapsed::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #4a90d9, #67b26f, #4a90d9);
  background-size: 200% 100%;
  animation: gradient-flow 3s ease infinite;
  opacity: 0;
  transition: opacity 0.3s;
}

.composer-collapsed:hover::before {
  opacity: 1;
}

.composer-collapsed:hover {
  border-color: rgba(74, 144, 217, 0.4);
  box-shadow: 0 6px 24px rgba(74, 144, 217, 0.12);
  transform: translateY(-1px);
}

@keyframes gradient-flow {
  0% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0% 50%;
  }
}

.collapsed-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapsed-avatar {
  border: 2px solid rgba(74, 144, 217, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.collapsed-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.collapsed-name {
  font-weight: 600;
  color: var(--text-title);
  font-size: 15px;
}

.collapsed-hint {
  color: var(--text-sub);
  font-size: 13px;
}

.collapsed-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapsed-icon {
  font-size: 20px;
  opacity: 0.6;
}

.publish-btn-collapsed {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  font-weight: 500;
}

.publish-btn-collapsed .btn-icon {
  font-size: 14px;
}

.composer-card {
  background: #ffffff;
  border: 1px solid var(--line-soft);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.composer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.composer-avatar {
  border: 3px solid rgba(74, 144, 217, 0.15);
  box-shadow: 0 4px 12px rgba(74, 144, 217, 0.15);
}

.composer-user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.composer-name {
  font-weight: 700;
  color: var(--text-title);
  font-size: 17px;
}

.composer-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

.composer-level {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
}

.composer-role {
  font-size: 12px;
  color: var(--text-sub);
}

.collapse-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: rgba(0, 0, 0, 0.08);
  color: var(--text-main);
  transform: rotate(90deg);
}

.topic-input-wrapper {
  position: relative;
  margin-bottom: 12px;
}

.topic-input {
  width: 100%;
  padding: 14px 60px 14px 0;
  border: none;
  border-bottom: 2px solid rgba(0, 0, 0, 0.06);
  font-size: 22px;
  font-weight: 700;
  color: var(--text-title);
  outline: none;
  background: transparent;
  transition: border-color 0.2s;
}

.topic-input:focus {
  border-color: #4a90d9;
}

.topic-input::placeholder {
  color: #bfbfbf;
  font-weight: 400;
}

.char-counter {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: var(--text-sub);
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 6px;
  transition: all 0.2s;
}

.char-counter.is-warning {
  color: #faad14;
  background: rgba(250, 173, 20, 0.1);
}

.char-counter.is-danger {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.1);
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #f0f2f5 100%);
  border-radius: 12px;
  margin-bottom: 12px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(0, 0, 0, 0.08);
  margin: 0 8px;
}

.toolbar-spacer {
  flex: 1;
}

.toolbar-btn {
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.toolbar-btn:hover {
  background: white;
  color: #4a90d9;
  box-shadow: 0 2px 8px rgba(74, 144, 217, 0.15);
  transform: translateY(-1px);
}

.toolbar-btn:active {
  transform: translateY(0);
}

.emoji-group {
  position: relative;
}

.emoji-btn span {
  font-size: 18px;
}

.emoji-picker {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 280px;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  padding: 12px;
  z-index: 100;
}

.emoji-header {
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 8px;
}

.emoji-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-sub);
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

.emoji-item {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.emoji-item:hover {
  background: rgba(74, 144, 217, 0.1);
  transform: scale(1.15);
}

.emoji-recent {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.recent-label {
  font-size: 11px;
  color: var(--text-sub);
  display: block;
  margin-bottom: 6px;
}

.recent-grid {
  display: flex;
  gap: 4px;
}

.content-wrapper {
  position: relative;
  margin-bottom: 12px;
}

.content-textarea {
  width: 100%;
  min-height: 140px;
  padding: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-main);
  outline: none;
  resize: vertical;
  background: linear-gradient(135deg, #fafbfc 0%, #ffffff 100%);
  transition: all 0.2s;
}

.content-textarea:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 4px rgba(74, 144, 217, 0.08);
  background: white;
}

.content-textarea::placeholder {
  color: #bfbfbf;
}

.content-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding: 0 4px;
}

.content-hint {
  font-size: 12px;
  color: var(--text-sub);
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.tag-input-wrapper {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
}

.tag-input-icon {
  position: absolute;
  left: 14px;
  font-size: 18px;
  font-weight: 700;
  color: #4a90d9;
  z-index: 1;
}

.tag-input {
  width: 100%;
  padding: 12px 14px 12px 36px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  font-size: 14px;
  color: var(--text-main);
  outline: none;
  transition: all 0.2s;
  background: #fafbfc;
}

.tag-input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 4px rgba(74, 144, 217, 0.08);
  background: white;
}

.tag-input::placeholder {
  color: #bfbfbf;
}

.tag-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 14px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  z-index: 100;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: linear-gradient(135deg, #f8fafc 0%, #f0f2f5 100%);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.dropdown-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-title);
}

.dropdown-close {
  width: 22px;
  height: 22px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  line-height: 1;
}

.dropdown-close:hover {
  background: rgba(0, 0, 0, 0.1);
  color: var(--text-main);
}

.tag-list {
  max-height: 200px;
  overflow-y: auto;
}

.tag-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}

.tag-option:hover {
  background: rgba(74, 144, 217, 0.06);
  border-left-color: var(--tag-color, #4a90d9);
}

.tag-icon {
  font-size: 16px;
}

.tag-name {
  font-size: 14px;
  color: var(--text-main);
}

.create-new-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
  cursor: pointer;
  color: #4a90d9;
  font-size: 13px;
  transition: all 0.15s;
}

.create-new-tag:hover {
  background: rgba(74, 144, 217, 0.06);
}

.create-icon {
  font-size: 14px;
}

.quote-input-wrapper {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
}

.quote-icon {
  position: absolute;
  left: 14px;
  font-size: 16px;
  z-index: 1;
}

.quote-input {
  width: 100%;
  padding: 12px 14px 12px 38px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  font-size: 14px;
  color: var(--text-main);
  outline: none;
  transition: all 0.2s;
  background: #fafbfc;
}

.quote-input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 4px rgba(74, 144, 217, 0.08);
  background: white;
}

.quote-input::placeholder {
  color: #bfbfbf;
}

.publish-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.publish-hints {
  display: flex;
  gap: 16px;
}

.hint-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-sub);
}

.hint-item svg {
  color: #52c41a;
}

.publish-actions {
  display: flex;
  gap: 10px;
}

.cancel-btn {
  padding: 10px 20px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: white;
  border-radius: 10px;
  font-size: 14px;
  color: var(--text-sub);
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  border-color: rgba(0, 0, 0, 0.2);
  color: var(--text-main);
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border: none;
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(74, 144, 217, 0.3);
}

.publish-btn:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 144, 217, 0.4);
}

.publish-btn:active:not(.is-disabled) {
  transform: translateY(0);
}

.publish-btn.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.publish-btn .btn-icon {
  font-size: 16px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.emoji-dropdown-enter-active,
.emoji-dropdown-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.emoji-dropdown-enter-from,
.emoji-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
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

.topic-link {
  cursor: pointer;
  transition: color 0.2s;
}

.topic-link:hover {
  color: #4a90d9;
}

.content {
  margin-top: 10px;
  font-size: 16px;
  line-height: 1.5;
  color: var(--text-main);
  max-height: 80px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}

.content.is-expanded {
  max-height: 500px;
}

.full,
.collapse {
  color: #4a90d9;
  font-weight: 600;
  cursor: pointer;
}

.full:hover,
.collapse:hover {
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

.hot-comments-section {
  margin-top: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #f0f4f8 100%);
  border-radius: 12px;
  border: 1px solid rgba(74, 144, 217, 0.1);
}

.hot-comments-carousel {
  position: relative;
}

.hot-comment-wrapper {
  min-height: 60px;
}

.hot-comment-item {
  padding: 8px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-comment-item:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 2px 8px rgba(74, 144, 217, 0.15);
}

.hot-comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.hot-comment-author {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-title);
}

.hot-comment-likes {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-sub);
}

.hot-comment-content {
  font-size: 14px;
  color: var(--text-main);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hot-comment-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 8px;
}

.hot-comment-dots .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(74, 144, 217, 0.3);
  transition: all 0.3s;
}

.hot-comment-dots .dot.active {
  width: 18px;
  border-radius: 3px;
  background: #4a90d9;
}

.hot-comment-slide-enter-active,
.hot-comment-slide-leave-active {
  transition: all 0.4s ease;
}

.hot-comment-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.hot-comment-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
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

.like-icon {
  width: 16px;
  height: 16px;
  display: block;
  flex-shrink: 0;
  transition: filter 0.2s ease, transform 0.2s ease;
}

.like-btn:hover .like-icon {
  transform: scale(1.06);
}

.like-btn.is-liked .like-icon {
  filter: brightness(0) invert(1);
}

.quick-comment-panel {
  margin-top: 12px;
  padding: 12px;
  background: rgba(74, 144, 217, 0.05);
  border-radius: 12px;
}

.comment-list {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 12px;
}

.comment-item {
  padding: 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  margin-bottom: 8px;
}

.comment-item:last-child {
  margin-bottom: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.comment-level {
  font-size: 11px;
  color: #4a90d9;
  background: rgba(74, 144, 217, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.comment-content {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  margin-bottom: 6px;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.comment-loading,
.no-comments {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

.comment-input-row {
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

  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
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

.tooltip-value.简单 {
  color: #52c41a;
}

.tooltip-value.中等 {
  color: #faad14;
}

.tooltip-value.困难 {
  color: #ff4d4f;
}

.tooltip-value.tags {
  color: #4a90d9;
}

.pagination-wrapper {
  margin-top: 8px;
  padding: 12px 0;
  display: flex;
  justify-content: center;
}

.like-celebration {
  position: fixed;
  z-index: 9999;
  pointer-events: none;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
}

.celebration-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 3px solid rgba(255, 107, 107, 0.8);
  transform: translate(-50%, -50%);
  animation: ring-expand 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
}

.celebration-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.6) 0%, rgba(255, 107, 107, 0.3) 40%, transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: glow-pulse 0.5s ease-out forwards;
}

.celebration-particles {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
}

.particle {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: particle-burst 0.7s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
  animation-delay: calc(var(--i) * 0.02s);
}

.particle:nth-child(1) {
  background: #ff6b6b;
  animation-name: particle-1;
}

.particle:nth-child(2) {
  background: #ffd93d;
  animation-name: particle-2;
}

.particle:nth-child(3) {
  background: #6bcb77;
  animation-name: particle-3;
}

.particle:nth-child(4) {
  background: #4d96ff;
  animation-name: particle-4;
}

.particle:nth-child(5) {
  background: #ff6b9d;
  animation-name: particle-5;
}

.particle:nth-child(6) {
  background: #c44dff;
  animation-name: particle-6;
}

.particle:nth-child(7) {
  background: #ff9f43;
  animation-name: particle-7;
}

.particle:nth-child(8) {
  background: #00d2d3;
  animation-name: particle-8;
}

.particle:nth-child(9) {
  background: #ff6b6b;
  animation-name: particle-9;
}

.particle:nth-child(10) {
  background: #ffd93d;
  animation-name: particle-10;
}

.particle:nth-child(11) {
  background: #6bcb77;
  animation-name: particle-11;
}

.particle:nth-child(12) {
  background: #4d96ff;
  animation-name: particle-12;
}

.celebration-emojis {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
}

.burst-emoji {
  position: absolute;
  font-size: 28px;
  animation: emoji-burst 0.9s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
  animation-delay: calc(var(--i) * 0.04s);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.burst-emoji:nth-child(1) {
  animation-name: emoji-1;
}

.burst-emoji:nth-child(2) {
  animation-name: emoji-2;
}

.burst-emoji:nth-child(3) {
  animation-name: emoji-3;
}

.burst-emoji:nth-child(4) {
  animation-name: emoji-4;
}

.burst-emoji:nth-child(5) {
  animation-name: emoji-5;
}

.burst-emoji:nth-child(6) {
  animation-name: emoji-6;
}

.burst-emoji:nth-child(7) {
  animation-name: emoji-7;
}

.burst-emoji:nth-child(8) {
  animation-name: emoji-8;
}

.celebration-sparks {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
}

.spark {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 6px 2px rgba(255, 255, 255, 0.8);
  animation: spark-fly 0.5s ease-out forwards;
  animation-delay: calc(var(--i) * 0.03s);
}

.spark:nth-child(1) {
  animation-name: spark-1;
}

.spark:nth-child(2) {
  animation-name: spark-2;
}

.spark:nth-child(3) {
  animation-name: spark-3;
}

.spark:nth-child(4) {
  animation-name: spark-4;
}

.spark:nth-child(5) {
  animation-name: spark-5;
}

.spark:nth-child(6) {
  animation-name: spark-6;
}

@keyframes ring-expand {
  0% {
    width: 20px;
    height: 20px;
    opacity: 1;
    border-width: 3px;
  }

  100% {
    width: 120px;
    height: 120px;
    opacity: 0;
    border-width: 1px;
  }
}

@keyframes glow-pulse {
  0% {
    transform: translate(-50%, -50%) scale(0.5);
    opacity: 1;
  }

  50% {
    transform: translate(-50%, -50%) scale(1.5);
    opacity: 0.8;
  }

  100% {
    transform: translate(-50%, -50%) scale(2);
    opacity: 0;
  }
}

@keyframes particle-1 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(20px, -50px) scale(0.3);
  }
}

@keyframes particle-2 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(45px, -35px) scale(0.3);
  }
}

@keyframes particle-3 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(55px, -5px) scale(0.3);
  }
}

@keyframes particle-4 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(50px, 25px) scale(0.3);
  }
}

@keyframes particle-5 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(30px, 50px) scale(0.3);
  }
}

@keyframes particle-6 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-5px, 60px) scale(0.3);
  }
}

@keyframes particle-7 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-40px, 50px) scale(0.3);
  }
}

@keyframes particle-8 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-60px, 20px) scale(0.3);
  }
}

@keyframes particle-9 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-55px, -15px) scale(0.3);
  }
}

@keyframes particle-10 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-40px, -45px) scale(0.3);
  }
}

@keyframes particle-11 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-10px, -55px) scale(0.3);
  }
}

@keyframes particle-12 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(10px, -60px) scale(0.3);
  }
}

@keyframes emoji-1 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(-25px, -75px) scale(0.8) rotate(20deg);
  }
}

@keyframes emoji-2 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(15px, -80px) scale(0.8) rotate(-15deg);
  }
}

@keyframes emoji-3 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(45px, -60px) scale(0.8) rotate(25deg);
  }
}

@keyframes emoji-4 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(60px, -25px) scale(0.8) rotate(-20deg);
  }
}

@keyframes emoji-5 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(50px, 20px) scale(0.8) rotate(15deg);
  }
}

@keyframes emoji-6 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(20px, 55px) scale(0.8) rotate(-25deg);
  }
}

@keyframes emoji-7 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(-35px, 50px) scale(0.8) rotate(20deg);
  }
}

@keyframes emoji-8 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0) rotate(0deg);
  }

  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.3) rotate(-10deg);
  }

  100% {
    opacity: 0;
    transform: translate(-55px, 15px) scale(0.8) rotate(-15deg);
  }
}

@keyframes spark-1 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-15px, -40px) scale(0);
  }
}

@keyframes spark-2 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(20px, -35px) scale(0);
  }
}

@keyframes spark-3 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(35px, -10px) scale(0);
  }
}

@keyframes spark-4 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(30px, 25px) scale(0);
  }
}

@keyframes spark-5 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-25px, 35px) scale(0);
  }
}

@keyframes spark-6 {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }

  100% {
    opacity: 0;
    transform: translate(-40px, 5px) scale(0);
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

/* 帖子菜单样式 */
.post-menu-wrapper {
  position: relative;
}

.post-menu-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 4px;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
  min-width: 120px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 13px;
  color: var(--text-main);
}

.menu-item:hover {
  background: #f5f5f5;
}

.menu-item.delete {
  color: #ff4d4f;
}

.menu-item.delete:hover {
  background: #fff2f0;
}

.menu-icon {
  font-size: 14px;
}

.menu-text {
  white-space: nowrap;
}

/* 菜单下拉动画 */
.menu-dropdown-enter-active,
.menu-dropdown-leave-active {
  transition: all 0.2s ease;
}

.menu-dropdown-enter-from,
.menu-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
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
