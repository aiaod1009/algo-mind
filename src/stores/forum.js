import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const LOCAL_FORUM_KEY = 'forum-posts'

const DEFAULT_POSTS = [
  { id: 1, author: '匿名牛油', authorLevel: 'LV.1', avatar: '', topic: '27届感觉看不到未来了', content: '27届双非本计算机专业，感觉前后端挤满了人，于是转 AI 产品经理，学习相关知识，最近准备重新梳理项目和简历。有没有前辈指点一下方向？', quote: '同济一哥：注意隐私，你这样我要加你微信了', tag: '# 实习分享', likes: 520, comments: 29, liked: false, createdAt: Date.now() - 1000 * 60 * 30 },
  { id: 2, author: '金融老炮', authorLevel: 'LV.3', avatar: '', topic: 'AI时代，技术er的三大职业单选题', content: '最近和几个校招季的学弟学妹聊天，发现一个很有意思的现象：明明选择变多了，焦虑反而更强了。到底是选择深耕技术、转管理还是创业？', quote: '一只丹尼尔：在大厂的时候，每天写代码、优化算法很踏实', tag: '# 职场成长', likes: 241, comments: 11, liked: false, createdAt: Date.now() - 1000 * 60 * 80 },
  { id: 3, author: '架构师老李', authorLevel: 'LV.5', avatar: '', topic: '微服务架构设计的几个坑，踩过的都懂', content: '做微服务架构5年了，总结几个常见的坑：1. 服务拆分过细，一个接口调10个服务 2. 分布式事务处理不当 3. 链路追踪缺失，问题定位困难 4. 服务治理能力跟不上。建议新项目先从单体开始，有需要再拆。', quote: '过早优化是万恶之源', tag: '# 系统设计', likes: 892, comments: 67, liked: false, createdAt: Date.now() - 1000 * 60 * 120 },
  { id: 4, author: '前端小王', authorLevel: 'LV.2', avatar: '', topic: 'Vue3 组合式 API 真香', content: '从 Vue2 迁移到 Vue3 一个月了，组合式 API 写起来太舒服了，逻辑复用变得超级简单。推荐大家试试 useVue 这类工具库，真的能提升开发效率。', quote: '', tag: '# 技术讨论', likes: 156, comments: 23, liked: false, createdAt: Date.now() - 1000 * 60 * 180 },
  { id: 5, author: '算法小白', authorLevel: 'LV.1', avatar: '', topic: '刷题打卡第30天，分享一下心得', content: '坚持刷题一个月了，从最开始的力扣第一题都做不出来，到现在能独立解决中等难度的题目。分享一下我的学习路径：先刷数组、链表、二叉树基础，然后逐步进阶动态规划。', quote: '坚持就是胜利', tag: '# 算法刷题', likes: 423, comments: 45, liked: false, createdAt: Date.now() - 1000 * 60 * 240 },
  { id: 6, author: '产品小姐姐', authorLevel: 'LV.3', avatar: '', topic: '面试产品经理的10个高频问题', content: '最近面试了10家公司，总结一下高频问题：1. 自我介绍 2. 为什么做产品 3. 产品设计思路 4. 数据分析方法 5. 项目难点 6. 竞品分析 7. 用户调研 8. 需求优先级 9. 跨部门协作 10. 职业规划', quote: '', tag: '# 面试经验', likes: 678, comments: 89, liked: false, createdAt: Date.now() - 1000 * 60 * 300 },
  { id: 7, author: '后端老张', authorLevel: 'LV.4', avatar: '', topic: 'MySQL 索引优化实战', content: '最近做了个慢查询优化，把一个 3 秒的查询优化到 50ms，分享一下经验：1. 分析执行计划 2. 合理建立联合索引 3. 避免索引失效 4. 覆盖索引优化。索引不是越多越好，要根据查询模式来设计。', quote: '性能优化永无止境', tag: '# 技术讨论', likes: 345, comments: 34, liked: false, createdAt: Date.now() - 1000 * 60 * 360 },
  { id: 8, author: '校招萌新', authorLevel: 'LV.1', avatar: '', topic: '拿到大厂 offer 了！分享一下面经', content: '经过三个月的准备，终于拿到了心仪大厂的 offer！分享一下我的准备过程：1. 刷题 200 道 2. 准备项目亮点 3. 八股文背诵 4. 模拟面试。最重要的是保持自信，面试官也是人。', quote: '努力终会有回报', tag: '# 校招求职', likes: 856, comments: 112, liked: false, createdAt: Date.now() - 1000 * 60 * 420 },
  { id: 9, author: '测试工程师', authorLevel: 'LV.2', avatar: '', topic: '自动化测试框架选型对比', content: '最近在做自动化测试框架选型，对比了几个主流框架：Selenium、Playwright、Cypress，各有优缺点。Playwright 对现代前端支持最好，Cypress 上手最快，Selenium 生态最成熟。', quote: '', tag: '# 技术讨论', likes: 234, comments: 28, liked: false, createdAt: Date.now() - 1000 * 60 * 480 },
  { id: 10, author: '数据分析师', authorLevel: 'LV.3', avatar: '', topic: 'Python 数据分析入门指南', content: '整理了一份 Python 数据分析入门指南，包含 NumPy、Pandas、Matplotlib 的基础用法和实战案例，适合零基础入门。建议先从 Pandas 学起，数据处理最常用。', quote: '数据驱动决策', tag: '# 学习交流', likes: 567, comments: 67, liked: false, createdAt: Date.now() - 1000 * 60 * 540 },
  { id: 11, author: '运维小哥', authorLevel: 'LV.3', avatar: '', topic: 'Docker 容器化部署最佳实践', content: '分享一些 Docker 容器化部署的经验：1. 镜像瘦身技巧 2. 多阶段构建 3. 安全加固 4. 日志收集方案 5. 监控告警配置。容器化不只是打包，还要考虑运维效率。', quote: '', tag: '# 技术讨论', likes: 445, comments: 56, liked: false, createdAt: Date.now() - 1000 * 60 * 600 },
  { id: 12, author: 'UI设计师', authorLevel: 'LV.2', avatar: '', topic: '设计系统搭建心得', content: '最近参与搭建了公司的设计系统，分享一下心得：1. 颜色体系 2. 字体规范 3. 组件库设计 4. 设计协作流程。好的设计系统能大幅提升团队效率。', quote: '设计即沟通', tag: '# 学习交流', likes: 389, comments: 42, liked: false, createdAt: Date.now() - 1000 * 60 * 660 },
  { id: 13, author: '安全研究员', authorLevel: 'LV.4', avatar: '', topic: 'Web 安全入门：常见漏洞解析', content: '整理了 Web 安全常见漏洞：SQL 注入、XSS、CSRF、文件上传漏洞等，每个漏洞都附有原理分析和防护方案。安全意识要从开发阶段就重视起来。', quote: '安全无小事', tag: '# 技术讨论', likes: 678, comments: 78, liked: false, createdAt: Date.now() - 1000 * 60 * 720 },
  { id: 14, author: '创业老兵', authorLevel: 'LV.5', avatar: '', topic: '创业三年的血泪教训', content: '创业三年，踩了无数坑，分享一些教训：1. 不要过度追求完美 2. 现金流比什么都重要 3. 团队比想法更重要 4. 快速试错迭代。创业是一场马拉松，不是百米冲刺。', quote: '创业维艰', tag: '# 职场成长', likes: 1234, comments: 156, liked: false, createdAt: Date.now() - 1000 * 60 * 780 },
  { id: 15, author: '研究生学长', authorLevel: 'LV.3', avatar: '', topic: '考研上岸经验分享', content: '一战上岸 985，分享一下备考经验：1. 时间规划 2. 各科复习策略 3. 心态调整 4. 复试准备技巧。考研不只是考知识，更是考心态和执行力。', quote: '天道酬勤', tag: '# 学习交流', likes: 567, comments: 89, liked: false, createdAt: Date.now() - 1000 * 60 * 840 },
  { id: 16, author: '转行程序员', authorLevel: 'LV.2', avatar: '', topic: '从土木转行程序员的一年', content: '一年前还是土木工程师，现在已经是一名前端开发了。分享一下转行经历和学习路线，希望能帮助到想转行的朋友。转行不容易，但只要坚持就能成功。', quote: '人生不设限', tag: '# 职场成长', likes: 789, comments: 134, liked: false, createdAt: Date.now() - 1000 * 60 * 900 },
  { id: 17, author: '开源爱好者', authorLevel: 'LV.4', avatar: '', topic: '如何参与开源项目', content: '参与开源项目一年多了，分享一下经验：1. 如何选择项目 2. 从小问题开始 3. 代码规范 4. 沟通技巧 5. 持续贡献。开源是最好的学习方式，也是最好的简历。', quote: '开源改变世界', tag: '# 学习交流', likes: 456, comments: 67, liked: false, createdAt: Date.now() - 1000 * 60 * 960 },
  { id: 18, author: '技术总监', authorLevel: 'LV.5', avatar: '', topic: '技术面试官视角：我看重什么', content: '作为技术面试官，分享我看重的几点：1. 基础扎实程度 2. 项目深度 3. 学习能力 4. 沟通表达 5. 解决问题的思路。技术可以学，态度和潜力更重要。', quote: '面试是双向选择', tag: '# 面试经验', likes: 1567, comments: 234, liked: false, createdAt: Date.now() - 1000 * 60 * 1020 },
  { id: 19, author: '独立开发者', authorLevel: 'LV.3', avatar: '', topic: '独立开发一年的收入和感悟', content: '辞职做独立开发者一年了，分享一下收入和感悟：第一个月收入 0，现在月入 2 万+，中间经历了很多困难和成长。独立开发需要自律和持续学习。', quote: '自由需要代价', tag: '# 职场成长', likes: 2345, comments: 345, liked: false, createdAt: Date.now() - 1000 * 60 * 1080 },
  { id: 20, author: '算法工程师', authorLevel: 'LV.4', avatar: '', topic: '深度学习入门路线图', content: '整理了一份深度学习入门路线图：1. 数学基础 2. Python 编程 3. 机器学习基础 4. 深度学习框架 5. 实战项目。建议边学边做项目，理论结合实践。', quote: 'AI 时代不进则退', tag: '# 算法刷题', likes: 890, comments: 123, liked: false, createdAt: Date.now() - 1000 * 60 * 1140 },
  { id: 21, author: '全栈工程师', authorLevel: 'LV.4', avatar: '', topic: '全栈工程师的成长之路', content: '从前端到后端，再到运维，分享一下全栈工程师的成长之路。全栈不是什么都懂一点，而是能独立完成一个完整项目。建议先精通一个领域，再横向扩展。', quote: 'T型人才最吃香', tag: '# 职场成长', likes: 678, comments: 89, liked: false, createdAt: Date.now() - 1000 * 60 * 1200 },
  { id: 22, author: '产品新人', authorLevel: 'LV.1', avatar: '', topic: '产品经理需要懂技术吗？', content: '作为产品新人，一直在纠结要不要学技术。请教了几个前辈，答案是：不需要精通，但要能和技术有效沟通。建议学点基础概念和数据库知识。', quote: '沟通是第一生产力', tag: '# 学习交流', likes: 345, comments: 67, liked: false, createdAt: Date.now() - 1000 * 60 * 1260 },
  { id: 23, author: 'Java老司机', authorLevel: 'LV.5', avatar: '', topic: 'JVM 调优实战经验', content: '做了几年 Java 后端，分享一些 JVM 调优经验：1. 内存模型理解 2. GC 算法选择 3. 常见问题排查 4. 性能监控工具。调优是最后手段，代码优化更重要。', quote: '过早优化是万恶之源', tag: '# 技术讨论', likes: 789, comments: 98, liked: false, createdAt: Date.now() - 1000 * 60 * 1320 },
  { id: 24, author: '前端架构师', authorLevel: 'LV.5', avatar: '', topic: '前端工程化最佳实践', content: '前端工程化不只是用框架，还包括：1. 项目结构规范 2. 代码风格统一 3. 自动化测试 4. CI/CD 流程 5. 性能监控。好的工程化能提升团队效率。', quote: '工程化是团队协作的基础', tag: '# 技术讨论', likes: 567, comments: 78, liked: false, createdAt: Date.now() - 1000 * 60 * 1380 },
  { id: 25, author: '数据科学家', authorLevel: 'LV.4', avatar: '', topic: '机器学习项目落地经验', content: '从模型训练到生产部署，分享一下机器学习项目落地的经验：1. 数据清洗 2. 特征工程 3. 模型选择 4. 性能评估 5. 在线部署。模型只是项目的一部分。', quote: '数据质量决定模型上限', tag: '# 算法刷题', likes: 456, comments: 56, liked: false, createdAt: Date.now() - 1000 * 60 * 1440 },
  { id: 26, author: '技术写作者', authorLevel: 'LV.3', avatar: '', topic: '技术写作的心得体会', content: '坚持写技术博客两年了，分享一下心得：1. 如何选题 2. 文章结构 3. 代码示例 4. 持续更新。写作是最好的学习方式，也能建立个人品牌。', quote: '输出是最好的输入', tag: '# 学习交流', likes: 234, comments: 34, liked: false, createdAt: Date.now() - 1000 * 60 * 1500 },
  { id: 27, author: '面试官小王', authorLevel: 'LV.4', avatar: '', topic: '面试中常见的算法题总结', content: '作为面试官，总结一下常考的算法题类型：1. 数组/链表 2. 二叉树 3. 动态规划 4. 回溯 5. 贪心。建议刷题时注重思路，不要死记硬背。', quote: '理解比记忆更重要', tag: '# 算法刷题', likes: 890, comments: 145, liked: false, createdAt: Date.now() - 1000 * 60 * 1560 },
  { id: 28, author: '职场新人', authorLevel: 'LV.1', avatar: '', topic: '入职三个月的感受和困惑', content: '入职三个月了，分享一下感受：技术栈不熟悉、需求理解不到位、和同事沟通有障碍。有没有过来人分享一下怎么度过新手期？', quote: '成长需要时间', tag: '# 职场成长', likes: 456, comments: 89, liked: false, createdAt: Date.now() - 1000 * 60 * 1620 },
  { id: 29, author: '技术社区运营', authorLevel: 'LV.3', avatar: '', topic: '如何运营一个技术社区', content: '运营技术社区一年了，分享一下经验：1. 内容为王 2. 用户激励 3. 活动策划 4. 氛围营造。社区的核心是价值输出和用户互动。', quote: '社区是人的连接', tag: '# 学习交流', likes: 345, comments: 56, liked: false, createdAt: Date.now() - 1000 * 60 * 1680 },
  { id: 30, author: 'CTO老陈', authorLevel: 'LV.5', avatar: '', topic: '技术管理者的自我修养', content: '从程序员到 CTO，分享一下技术管理的心得：1. 技术决策 2. 团队建设 3. 项目管理 4. 向上沟通。管理不是管人，而是赋能团队。', quote: '技术是基础，管理是艺术', tag: '# 职场成长', likes: 1567, comments: 234, liked: false, createdAt: Date.now() - 1000 * 60 * 1740 },
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
  const isFromCache = ref(false)

  const hydratePostsFromLocal = () => {
    const local = readLocalPosts()
    if (Array.isArray(local) && local.length > 0) {
      posts.value = local
      return true
    }
    return false
  }

  const fetchPosts = async ({ skipIfLoaded = true } = {}) => {
    if (skipIfLoaded && Array.isArray(posts.value) && posts.value.length > 0) {
      return { fromCache: isFromCache.value }
    }

    const hasHydrated = hydratePostsFromLocal()

    try {
      const res = await api.get('/forum-posts')
      const list = res.data?.data?.list
      if (res.data?.code === 0 && Array.isArray(list)) {
        posts.value = list
        saveLocalPosts(posts.value)
        isFromCache.value = false
        return { fromCache: false }
      }
    } catch (error) {
      console.warn('论坛接口不可用，使用本地缓存数据。', error)
    }

    if (hasHydrated) {
      isFromCache.value = true
      return { fromCache: true }
    }

    posts.value = DEFAULT_POSTS
    saveLocalPosts(posts.value)
    isFromCache.value = true
    return { fromCache: true }
  }

  const addPost = async (postPayload) => {
    const payload = {
      author: postPayload.author || '匿名用户',
      authorLevel: postPayload.authorLevel || 'LV.1',
      avatar: postPayload.avatar || '',
      topic: postPayload.topic || '未命名话题',
      content: postPayload.content || '',
      quote: postPayload.quote || '',
      tag: postPayload.tag || '# 未分类',
    }

    try {
      const res = await api.post('/forum-posts', payload)
      if (res.data?.code === 0) {
        const newPost = res.data.data || {
          ...payload,
          id: Date.now(),
          likes: 0,
          comments: 0,
          liked: false,
          createdAt: new Date().toISOString(),
        }
        posts.value.unshift(newPost)
        saveLocalPosts(posts.value)
        return { success: true, synced: true }
      } else {
        throw new Error(res.data?.message || '发帖失败')
      }
    } catch (error) {
      console.error('发帖失败:', error)
      return { success: false, error: error.message || '网络错误，发帖失败' }
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

  return { posts, isFromCache, hydratePostsFromLocal, fetchPosts, addPost, toggleLike, addCommentCount }
})
