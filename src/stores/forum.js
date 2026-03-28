import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const LOCAL_FORUM_KEY = 'forum-posts'

const DEFAULT_POSTS = [
  {
    id: 1,
    author: '匿名牛油',
    authorLevel: 'LV.1',
    avatar: '',
    topic: '27届感觉看不到未来了',
    content:
      '27届双非本计算机专业，感觉前后端挤满了人，于是转 AI 产品经理，学习相关知识，最近准备重新梳理项目和简历。',
    quote: '同济一哥：注意隐私，你这样我要加你微信了',
    tag: '# 实习如何「偷」产出？',
    likes: 520,
    comments: 29,
    liked: false,
    createdAt: Date.now() - 1000 * 60 * 30,
  },
  {
    id: 2,
    author: '金融老炮',
    authorLevel: 'LV.3',
    avatar: '',
    topic: 'AI时代，技术er的三大职业单选题',
    content:
      '最近和几个校招季的学弟学妹聊天，发现一个很有意思的现象：明明选择变多了，焦虑反而更强了。',
    quote: '一只丹尼尔：在大厂的时候，每天写代码、优化算法很踏实',
    tag: '# 招商银行实习',
    likes: 241,
    comments: 11,
    liked: false,
    createdAt: Date.now() - 1000 * 60 * 80,
  },
]

const readLocalPosts = () => {
  const raw = localStorage.getItem(LOCAL_FORUM_KEY)
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : null
  } catch (error) {
    return null
  }
}

const saveLocalPosts = (posts) => {
  localStorage.setItem(LOCAL_FORUM_KEY, JSON.stringify(posts))
}

export const useForumStore = defineStore('forum', () => {
  const posts = ref([])

  const fetchPosts = async () => {
    try {
      const res = await api.get('/forum-posts')
      if (res.data?.code === 0 && Array.isArray(res.data.data)) {
        posts.value = res.data.data
        saveLocalPosts(posts.value)
        return
      }
    } catch (error) {
      console.warn('论坛接口不可用，使用本地论坛数据。', error)
    }

    const local = readLocalPosts()
    posts.value = local || DEFAULT_POSTS
    if (!local) {
      saveLocalPosts(posts.value)
    }
  }

  const addPost = async (postPayload) => {
    const nextPost = {
      id: Date.now(),
      author: postPayload.author || '匿名用户',
      authorLevel: postPayload.authorLevel || 'LV.1',
      avatar: postPayload.avatar || '',
      topic: postPayload.topic || '未命名话题',
      content: postPayload.content || '',
      quote: postPayload.quote || '',
      tag: postPayload.tag || '# 未分类',
      likes: 0,
      comments: 0,
      liked: false,
      createdAt: Date.now(),
    }

    posts.value.unshift(nextPost)
    saveLocalPosts(posts.value)

    try {
      await api.post('/forum-posts', nextPost)
    } catch (error) {
      console.warn('论坛发帖接口不可用，已保存在本地。', error)
    }
  }

  const toggleLike = (postId) => {
    const item = posts.value.find((post) => Number(post.id) === Number(postId))
    if (!item) return
    if (item.liked) {
      item.likes = Math.max(0, Number(item.likes || 0) - 1)
      item.liked = false
    } else {
      item.likes = Number(item.likes || 0) + 1
      item.liked = true
    }
    saveLocalPosts(posts.value)
  }

  const addCommentCount = (postId) => {
    const item = posts.value.find((post) => Number(post.id) === Number(postId))
    if (!item) return
    item.comments = Number(item.comments || 0) + 1
    saveLocalPosts(posts.value)
  }

  return { posts, fetchPosts, addPost, toggleLike, addCommentCount }
})
