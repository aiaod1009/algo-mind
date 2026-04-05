<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CourseDetail from '../components/CourseDetail.vue'

const router = useRouter()
const activeTrack = ref('all')
const selectedCourse = ref(null)
const showDetail = ref(false)

const courses = ref([])
const aiRecommendations = ref([])
const isLoadingRecommendations = ref(false)

const trackOptions = [
  { label: '全部课程', value: 'all' },
  { label: '算法思维', value: 'algo' },
  { label: '数据结构', value: 'ds' },
  { label: '竞赛冲刺', value: 'contest' },
]

const trackLabelMap = {
  algo: '算法思维',
  ds: '数据结构',
  contest: '竞赛冲刺',
}

const levelMap = {
  beginner: '入门',
  intermediate: '进阶',
  advanced: '高级',
}

const getTrackLabel = (track) => trackLabelMap[track] || '综合'
const getLevelLabel = (level) => levelMap[level] || '入门'

const actionText = (progress) => {
  if (progress <= 0) return '开始学习'
  if (progress >= 100) return '复习课程'
  return '继续学习'
}

const getCover = (course) => {
  if (course.cover) return course.cover
  const defaultCovers = [
    'https://api.dicebear.com/7.x/shapes/svg?seed=algo&backgroundColor=4a6f9d',
    'https://api.dicebear.com/7.x/shapes/svg?seed=ds&backgroundColor=6672cb',
    'https://api.dicebear.com/7.x/shapes/svg?seed=contest&backgroundColor=f59e0b',
    'https://api.dicebear.com/7.x/shapes/svg?seed=dp&backgroundColor=10b981'
  ]
  return defaultCovers[course.id % defaultCovers.length]
}

const fetchCourses = async () => {
  courses.value = [
    {
      id: 1,
      title: '算法思维训练营（基础）',
      description: '从零开始培养算法思维，掌握复杂度分析、双指针技巧、二分查找等核心能力',
      teacher: { name: '陈老师', avatar: '', title: 'ACM金牌教练' },
      track: 'algo',
      level: 'beginner',
      duration: '16课时',
      lessons: 16,
      progress: 35,
      tags: ['复杂度', '双指针', '二分'],
      cover: '',
      bvid: 'BV19e4y1q7JJ',
      rating: 4.8,
      students: 1234,
      direction: '适合算法入门，建立正确的思维方式',
      chapters: [
        { id: 1, title: '复杂度分析基础', duration: '45分钟', completed: true },
        { id: 2, title: '双指针技巧入门', duration: '50分钟', completed: true },
        { id: 3, title: '二分查找原理', duration: '40分钟', completed: false },
      ],
    },
    {
      id: 2,
      title: '数据结构高频实战',
      description: '深入理解常用数据结构，掌握链表、哈希表、并查集等核心数据结构的实现与应用',
      teacher: { name: '王老师', avatar: '', title: '前Google工程师' },
      track: 'ds',
      level: 'intermediate',
      duration: '20课时',
      lessons: 20,
      progress: 62,
      tags: ['链表', '哈希', '并查集'],
      cover: '',
      bvid: 'BV1Zs411g7LG',
      rating: 4.9,
      students: 892,
      direction: '面试高频考点，数据结构实战应用',
      chapters: [
        { id: 1, title: '链表基础与变体', duration: '55分钟', completed: true },
        { id: 2, title: '哈希表原理与实现', duration: '60分钟', completed: true },
        { id: 3, title: '并查集与应用', duration: '45分钟', completed: false },
      ],
    },
    {
      id: 3,
      title: '竞赛冲刺：贪心与图论',
      description: '针对算法竞赛的贪心算法和图论专题，涵盖最短路径、拓扑排序等核心考点',
      teacher: { name: '李老师', avatar: '', title: 'NOI金牌导师' },
      track: 'contest',
      level: 'advanced',
      duration: '24课时',
      lessons: 24,
      progress: 12,
      tags: ['贪心', '最短路', '拓扑排序'],
      cover: '',
      bvid: 'BV1xx411c7mD',
      rating: 4.7,
      students: 456,
      direction: '竞赛必备，冲刺省选/国赛',
      chapters: [
        { id: 1, title: '贪心策略与证明', duration: '60分钟', completed: true },
        { id: 2, title: 'Dijkstra算法详解', duration: '55分钟', completed: false },
        { id: 3, title: '拓扑排序应用', duration: '50分钟', completed: false },
      ],
    },
    {
      id: 4,
      title: 'DP专题从入门到模板',
      description: '系统学习动态规划，从递归到记忆化，从线性DP到背包问题，建立完整的DP知识体系',
      teacher: { name: '赵老师', avatar: '', title: '算法竞赛教练' },
      track: 'algo',
      level: 'intermediate',
      duration: '18课时',
      lessons: 18,
      progress: 0,
      tags: ['记忆化', '状态转移', '背包'],
      cover: '',
      bvid: 'BV1xb411e7ww',
      rating: 4.6,
      students: 678,
      direction: '攻克DP难关，掌握状态设计技巧',
      chapters: [
        { id: 1, title: '动态规划入门', duration: '50分钟', completed: false },
        { id: 2, title: '状态转移方程设计', duration: '55分钟', completed: false },
        { id: 3, title: '背包问题专题', duration: '60分钟', completed: false },
      ],
    },
  ]
  return courses.value
}

const fetchAIRecommendations = async () => {
  isLoadingRecommendations.value = true
  aiRecommendations.value = [
    {
      id: 101,
      title: '动态规划强化训练',
      reason: '根据您的错题分析，DP类题目错误率较高，推荐此课程针对性提升',
      matchScore: 95,
      teacher: { name: '张老师', title: 'DP专家' },
      duration: '12课时',
      tags: ['DP优化', '状态压缩'],
    },
    {
      id: 102,
      title: '图论算法精讲',
      reason: '您的学习计划中包含图论内容，此课程与您的学习目标高度匹配',
      matchScore: 88,
      teacher: { name: '刘老师', title: '图论专家' },
      duration: '15课时',
      tags: ['BFS', 'DFS', '最短路'],
    },
  ]
  isLoadingRecommendations.value = false
  return aiRecommendations.value
}

const filteredCourses = computed(() => {
  if (activeTrack.value === 'all') return courses.value
  return courses.value.filter((item) => item.track === activeTrack.value)
})

const openCourseDetail = (course) => {
  selectedCourse.value = course
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
  selectedCourse.value = null
}

const startCourse = (course) => {
  router.push(`/courses/${course.id}`)
}

const enrollCourse = (course) => {
  console.log('Enrolling course:', course.id)
}

defineExpose({
  fetchCourses,
  fetchAIRecommendations,
  updateCourses: (data) => { courses.value = data },
  updateRecommendations: (data) => { aiRecommendations.value = data },
})

onMounted(async () => {
  await fetchCourses()
  fetchAIRecommendations()
})
</script>

<template>
  <div class="courses-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">网课中心</h1>
        <p class="page-desc">精选优质课程，助力算法进阶之路</p>
      </div>
      <div class="track-filter">
        <button
          v-for="opt in trackOptions"
          :key="opt.value"
          :class="['filter-btn', { active: activeTrack === opt.value }]"
          @click="activeTrack = opt.value"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <section class="ai-recommend-section">
      <div class="section-header">
        <div class="section-title-wrap">
          <h2>智能推荐</h2>
        </div>
        <p class="section-desc">基于您的错题分析和学习计划，为您精选以下课程</p>
      </div>
      <div v-if="isLoadingRecommendations" class="loading-state">
        <span class="loading-spinner"></span>
        <span>正在分析您的学习数据...</span>
      </div>
      <div v-else class="recommend-list">
        <div v-for="rec in aiRecommendations" :key="rec.id" class="recommend-card">
          <div class="recommend-badge">
            <span class="match-score">{{ rec.matchScore }}%</span>
            <span class="match-label">匹配度</span>
          </div>
          <div class="recommend-content">
            <h3 class="recommend-title">{{ rec.title }}</h3>
            <p class="recommend-reason">{{ rec.reason }}</p>
            <div class="recommend-meta">
              <span class="teacher">{{ rec.teacher.name }}</span>
              <span class="divider">·</span>
              <span class="duration">{{ rec.duration }}</span>
            </div>
            <div class="recommend-tags">
              <span v-for="tag in rec.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>
          <button class="enroll-btn" @click="enrollCourse(rec)">立即报名</button>
        </div>
      </div>
    </section>

    <section class="courses-section">
      <div class="section-header">
        <h2>全部课程</h2>
        <span class="course-count">{{ filteredCourses.length }} 门课程</span>
      </div>
      <div class="course-grid">
        <div v-for="course in filteredCourses" :key="course.id" class="course-card" @click="openCourseDetail(course)">
          <div class="course-cover">
            <img v-if="getCover(course)" :src="getCover(course)" class="cover-image" alt="课程封面" />
            <div v-else class="cover-placeholder">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                <line x1="8" y1="21" x2="16" y2="21"/>
                <line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
            </div>
            <div class="course-level" :class="course.level">{{ getLevelLabel(course.level) }}</div>
            <div v-if="course.progress > 0" class="course-progress-overlay">
              <div class="progress-ring" :style="{ '--progress': course.progress }">
                <span>{{ course.progress }}%</span>
              </div>
            </div>
          </div>
          <div class="course-body">
            <div class="course-header">
              <h3 class="course-title">{{ course.title }}</h3>
              <span class="track-tag">{{ getTrackLabel(course.track) }}</span>
            </div>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-direction">
              <span class="direction-icon">📍</span>
              <span>{{ course.direction }}</span>
            </div>
            <div class="course-tags">
              <span v-for="tag in course.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
            <div class="course-meta">
              <div class="teacher-info">
                <div class="teacher-avatar">
                  <span>{{ course.teacher.name.slice(0, 1) }}</span>
                </div>
                <div class="teacher-detail">
                  <span class="teacher-name">{{ course.teacher.name }}</span>
                  <span class="teacher-title">{{ course.teacher.title }}</span>
                </div>
              </div>
              <div class="course-stats">
                <span class="stat">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                  </svg>
                  {{ course.rating }}
                </span>
                <span class="stat">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  {{ course.students }}
                </span>
              </div>
            </div>
            <div class="progress-section">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: course.progress + '%' }"></div>
              </div>
              <span class="progress-text">{{ course.progress }}% 完成</span>
            </div>
            <div class="course-footer">
              <span class="duration">{{ course.duration }}</span>
              <button class="action-btn" @click.stop="startCourse(course)">
                {{ actionText(course.progress) }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </section>

    <Transition name="modal">
      <div v-if="showDetail && selectedCourse" class="course-detail-modal" @click.self="closeDetail">
        <div class="detail-container">
          <button class="close-btn" @click="closeDetail">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          <CourseDetail :course="selectedCourse" />
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

.courses-page {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.page-desc {
  color: #64748b;
  margin: 8px 0 0;
  font-size: 14px;
}

.track-filter {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.filter-btn {
  padding: 10px 20px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.filter-btn.active {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-color: transparent;
  color: white;
  box-shadow: 0 2px 8px rgba(74, 111, 157, 0.25);
}

.ai-recommend-section {
  background: linear-gradient(135deg, #f0f4f8 0%, #e8ecf4 100%);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 32px;
  border: 1px solid rgba(74, 111, 157, 0.1);
}

.section-header {
  margin-bottom: 20px;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.section-desc {
  color: #64748b;
  font-size: 13px;
  margin: 6px 0 0;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  color: #64748b;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e2e8f0;
  border-top-color: #4a6f9d;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.recommend-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  border: 1px solid rgba(74, 111, 157, 0.1);
  transition: all 0.2s ease;
}

.recommend-card:hover {
  box-shadow: 0 4px 16px rgba(74, 111, 157, 0.1);
  transform: translateY(-2px);
}

.recommend-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border-radius: 12px;
  min-width: 60px;
}

.match-score {
  font-size: 20px;
  font-weight: 700;
  color: #4a6f9d;
}

.match-label {
  font-size: 10px;
  color: #64748b;
  margin-top: 2px;
}

.recommend-content {
  flex: 1;
}

.recommend-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 6px;
}

.recommend-reason {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
  margin: 0 0 10px;
}

.recommend-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.recommend-tags {
  display: flex;
  gap: 6px;
}

.recommend-tags .tag {
  padding: 3px 8px;
  background: #f1f5f9;
  border-radius: 4px;
  font-size: 11px;
  color: #475569;
}

.enroll-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.enroll-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.3);
}

.courses-section {
  margin-bottom: 32px;
}

.courses-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.courses-section h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.course-count {
  font-size: 13px;
  color: #64748b;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 16px;
}

.course-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.course-card:hover {
  box-shadow: 0 8px 32px rgba(74, 111, 157, 0.12);
  transform: translateY(-4px);
}

.course-cover {
  position: relative;
  height: 160px;
  background: linear-gradient(135deg, #f0f4f8 0%, #e8ecf4 100%);
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.course-level {
  position: absolute;
  top: 12px;
  left: 12px;
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

.course-progress-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
}

.progress-ring {
  width: 48px;
  height: 48px;
  background: conic-gradient(#4a6f9d calc(var(--progress) * 3.6deg), #e2e8f0 0deg);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-ring::before {
  content: '';
  position: absolute;
  width: 36px;
  height: 36px;
  background: white;
  border-radius: 50%;
}

.progress-ring span {
  position: relative;
  font-size: 11px;
  font-weight: 600;
  color: #4a6f9d;
}

.course-body {
  padding: 20px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.course-title {
  font-size: 17px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  line-height: 1.3;
}

.track-tag {
  padding: 4px 10px;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #4a6f9d;
  white-space: nowrap;
}

.course-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-direction {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #4a6f9d;
  margin-bottom: 12px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.direction-icon {
  font-size: 14px;
}

.course-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.course-tags .tag {
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 11px;
  color: #475569;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-top: 1px solid #f1f5f9;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 16px;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.teacher-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.teacher-detail {
  display: flex;
  flex-direction: column;
}

.teacher-name {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
}

.teacher-title {
  font-size: 11px;
  color: #94a3b8;
}

.course-stats {
  display: flex;
  gap: 12px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.stat svg {
  color: #fbbf24;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.duration {
  font-size: 13px;
  color: #64748b;
}

.action-btn {
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

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.3);
}

.course-detail-modal {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 24px;
}

.detail-container {
  position: relative;
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  overflow-y: auto;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 40px;
  height: 40px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .detail-container,
.modal-leave-to .detail-container {
  transform: scale(0.95);
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .track-filter {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .recommend-list {
    grid-template-columns: 1fr;
  }

  .course-grid {
    grid-template-columns: 1fr;
  }
}
</style>
