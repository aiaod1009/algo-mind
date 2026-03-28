<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useForumStore } from '../stores/forum'
import { useUserStore } from '../stores/user'

const forumStore = useForumStore()
const userStore = useUserStore()
const publishing = ref(false)

const postForm = reactive({
  author: userStore.userInfo?.name || '同学',
  topic: '',
  content: '',
  tag: '# 学习交流',
  quote: '',
})

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

const handleLike = (post) => {
  forumStore.toggleLike(post.id)
}

const handleComment = (post) => {
  forumStore.addCommentCount(post.id)
  ElMessage.success(`已评论：${post.topic}`)
}

const handlePublish = async () => {
  if (!postForm.topic.trim() || !postForm.content.trim()) {
    ElMessage.warning('请填写标题和正文')
    return
  }

  publishing.value = true
  try {
    await forumStore.addPost({
      author: postForm.author.trim() || userStore.userInfo?.name || '同学',
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
    ElMessage.success('发布成功')
  } catch (error) {
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  forumStore.fetchPosts()
})
</script>

<template>
  <div class="page-container forum-page">
    <h2 class="section-title">简版论坛</h2>

    <el-card class="surface-card composer-card" shadow="never">
      <h3 class="composer-title">发布内容</h3>
      <el-form label-width="88px">
        <el-form-item label="发言人">
          <el-input v-model="postForm.author" placeholder="请输入发言人昵称" maxlength="20" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="postForm.topic" placeholder="例如：校招迷茫期如何破局" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="postForm.content" type="textarea" :rows="4" placeholder="输入你想发布的论坛内容" maxlength="400"
            show-word-limit />
        </el-form-item>
        <el-form-item label="话题">
          <el-input v-model="postForm.tag" placeholder="# 学习交流" maxlength="20" />
        </el-form-item>
        <el-form-item label="引用语">
          <el-input v-model="postForm.quote" placeholder="可选，支持一句引用" maxlength="80" />
        </el-form-item>
      </el-form>
      <div class="publish-row">
        <el-button type="primary" :loading="publishing" @click="handlePublish">发布帖子</el-button>
      </div>
    </el-card>

    <section class="feed-list">
      <el-card v-for="item in posts" :key="item.id" class="feed-card" shadow="never">
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
        <p class="content">{{ item.content }}<span class="full">...全文</span></p>

        <div class="quote" v-if="item.quote">{{ item.quote }}</div>
        <div class="hash">{{ item.tag }}</div>

        <div class="action-row">
          <el-button text :type="item.liked ? 'primary' : 'info'" @click="handleLike(item)">
            👍 {{ item.likes }}
          </el-button>
          <el-button text type="info" @click="handleComment(item)">💬 {{ item.comments }}</el-button>
        </div>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.forum-page {
  padding-bottom: 28px;
  display: grid;
  gap: 12px;
}

.composer-card {
  border: 1px solid var(--line-soft);
}

.composer-title {
  margin-bottom: 10px;
  color: var(--text-title);
}

.publish-row {
  display: flex;
  justify-content: flex-end;
}

.feed-list {
  display: grid;
  gap: 12px;
}

.feed-card {
  border-radius: 16px;
  border: 1px solid var(--line-soft);
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
}

.full {
  color: #0ea992;
  font-weight: 700;
}

.quote {
  margin-top: 12px;
  border-left: 4px solid var(--line-soft);
  padding-left: 10px;
  color: var(--text-muted);
}

.hash {
  margin-top: 10px;
  display: inline-block;
  border: 1px solid var(--line-soft);
  border-radius: 999px;
  padding: 4px 10px;
  color: var(--text-sub);
}

.action-row {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 14px;
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
