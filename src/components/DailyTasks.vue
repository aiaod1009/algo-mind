<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  learningPlan: {
    type: Object,
    default: null
  }
})

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const today = new Date().getDay()
const currentDayIndex = ref(today === 0 ? 6 : today - 1)

// 7种配色方案（原4种 + 新增3种）
const colorSchemes = [
  { color: '#2563eb', bgColor: '#dbeafe' }, // 蓝色系
  { color: '#16a34a', bgColor: '#d1fae5' }, // 绿色系
  { color: '#d97706', bgColor: '#fef3c7' }, // 橙色系
  { color: '#7c3aed', bgColor: '#ede9fe' }, // 紫色系
  { color: '#dc2626', bgColor: '#fee2e2' }, // 红色系（新增）
  { color: '#0891b2', bgColor: '#cffafe' }, // 青色系（新增）
  { color: '#db2777', bgColor: '#fce7f3' }, // 粉色系（新增）
]

const taskTypes = [
  { key: 'array', label: '数组', icon: '🔢' },
  { key: 'string', label: '字符串', icon: '📝' },
  { key: 'dp', label: '动态规划', icon: '📈' },
  { key: 'tree', label: '二叉树', icon: '🌳' },
  { key: 'graph', label: '图论', icon: '🕸️' },
  { key: 'sort', label: '排序', icon: '📊' },
  { key: 'greedy', label: '贪心', icon: '💎' },
  { key: 'review', label: '复盘', icon: '📋' },
]

// 为每个任务随机分配配色
const getRandomColorScheme = () => {
  return colorSchemes[Math.floor(Math.random() * colorSchemes.length)]
}

// 默认任务数据
const defaultDailyTasks = {
  0: [ // 周一 - 基础算法
    { type: 'array', title: '数组基础练习', time: '08:30-09:30', duration: '1h' },
    { type: 'string', title: '字符串处理', time: '09:30-11:00', duration: '1.5h' },
    { type: 'sort', title: '排序算法复习', time: '19:30-21:00', duration: '1.5h' },
    { type: 'review', title: '错题整理', time: '21:00-22:00', duration: '1h' },
  ],
  1: [ // 周二 - 进阶数据结构
    { type: 'tree', title: '二叉树遍历', time: '08:30-10:00', duration: '1.5h' },
    { type: 'tree', title: 'BST与平衡树', time: '14:00-15:30', duration: '1.5h' },
    { type: 'review', title: '算法复盘', time: '20:00-21:00', duration: '1h' },
  ],
  2: [ // 周三 - 动态规划
    { type: 'dp', title: 'DP基础入门', time: '08:30-09:30', duration: '1h' },
    { type: 'dp', title: '背包问题专题', time: '09:30-11:00', duration: '1.5h' },
    { type: 'dp', title: '线性DP练习', time: '19:30-21:00', duration: '1.5h' },
    { type: 'review', title: '代码复盘', time: '21:00-22:00', duration: '1h' },
  ],
  3: [ // 周四 - 图论
    { type: 'graph', title: '图的表示与遍历', time: '09:00-10:30', duration: '1.5h' },
    { type: 'graph', title: '最短路径算法', time: '14:00-15:30', duration: '1.5h' },
    { type: 'greedy', title: '贪心算法入门', time: '19:30-21:00', duration: '1.5h' },
  ],
  4: [ // 周五 - 综合练习
    { type: 'array', title: '双指针专题', time: '09:00-10:30', duration: '1.5h' },
    { type: 'string', title: '字符串匹配', time: '14:00-15:30', duration: '1.5h' },
    { type: 'dp', title: '区间DP练习', time: '19:30-21:00', duration: '1.5h' },
  ],
  5: [ // 周六 - 模拟竞赛
    { type: 'review', title: '周赛模拟', time: '09:00-11:00', duration: '2h' },
    { type: 'tree', title: '树形DP专题', time: '14:00-15:30', duration: '1.5h' },
    { type: 'graph', title: '拓扑排序', time: '19:30-21:00', duration: '1.5h' },
  ],
  6: [ // 周日 - 总结复习
    { type: 'review', title: '本周知识点总结', time: '10:00-12:00', duration: '2h' },
    { type: 'sort', title: '高级排序算法', time: '14:00-15:30', duration: '1.5h' },
    { type: 'greedy', title: '贪心经典问题', time: '19:30-21:00', duration: '1.5h' },
  ],
}

// 类型映射：将中文类型转换为组件使用的key
const typeMapping = {
  '数组': 'array',
  '字符串': 'string',
  '动态规划': 'dp',
  '二叉树': 'tree',
  '图论': 'graph',
  '排序': 'sort',
  '贪心': 'greedy',
  '复盘': 'review',
}

// 转换学习计划中的任务数据
const convertLearningPlanTasks = (dailyTasks) => {
  const converted = {}
  dailyTasks.forEach((dayTask, index) => {
    if (index < 7) {
      converted[index] = dayTask.tasks.map(task => ({
        type: typeMapping[task.type] || 'review',
        title: task.title,
        time: '09:00-10:30', // 默认时间，可以根据需要调整
        duration: task.duration || '1.5h'
      }))
    }
  })
  return converted
}

// 计算当前显示的任务
const currentTasks = computed(() => {
  // 如果有传入的学习计划，使用学习计划的数据
  if (props.learningPlan?.dailyTasks && props.learningPlan.dailyTasks.length > 0) {
    const planTasks = convertLearningPlanTasks(props.learningPlan.dailyTasks)
    return planTasks[currentDayIndex.value] || []
  }
  // 否则使用默认任务
  return defaultDailyTasks[currentDayIndex.value] || []
})

const selectDay = (index) => {
  currentDayIndex.value = index
}

const getTaskTypeInfo = (typeKey) => {
  return taskTypes.find(t => t.key === typeKey) || taskTypes[0]
}

const getTypeColor = (typeKey) => {
  const colorMap = {
    array: '#2563eb',
    string: '#16a34a',
    dp: '#d97706',
    tree: '#7c3aed',
    graph: '#dc2626',
    sort: '#0891b2',
    greedy: '#db2777',
    review: '#6b7280',
  }
  return colorMap[typeKey] || '#6b7280'
}

const goToPrevDay = () => {
  currentDayIndex.value = currentDayIndex.value > 0 ? currentDayIndex.value - 1 : 6
}

const goToNextDay = () => {
  currentDayIndex.value = currentDayIndex.value < 6 ? currentDayIndex.value + 1 : 0
}
</script>

<template>
  <section class="daily-tasks-section">
    <div class="content-wrapper">
      <!-- 头部 -->
      <div class="section-header">
        <div class="header-title">
          <div class="title-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
          </div>
          <div class="title-text">
            <h2>每日任务</h2>
            <p>坚持打卡，稳步提升</p>
          </div>
        </div>
        <div class="nav-arrows">
          <button class="nav-btn _target" @click="goToPrevDay">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
          </button>
          <button class="nav-btn _target" @click="goToNextDay">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>
      </div>

      <!-- 题型分类 -->
      <div class="type-tags-section">
        <div class="section-title-bar">
          <h4 class="section-title">题型分类</h4>
          <span class="section-count">8 种类型</span>
        </div>
        <div class="type-tags">
          <div v-for="type in taskTypes" :key="type.key" class="type-tag"
            :style="{ '--tag-color': getTypeColor(type.key) }">
            <span class="tag-icon">{{ type.icon }}</span>
            <span class="tag-label">{{ type.label }}</span>
          </div>
        </div>
      </div>

      <!-- 星期标签 -->
      <div class="weekday-tabs">
        <button v-for="(day, index) in weekdays" :key="index" class="weekday-btn _target"
          :class="{ active: currentDayIndex === index }" @click="selectDay(index)">
          <span class="day-name">{{ day }}</span>
          <span v-if="currentDayIndex === index" class="active-indicator"></span>
        </button>
      </div>

      <!-- 任务详情标题 -->
      <div class="task-title-row">
        <div class="title-left">
          <h3>{{ weekdays[currentDayIndex] }}任务安排</h3>
          <span class="task-count">{{ currentTasks.length }} 个任务</span>
        </div>
        <span class="status-badge">算法学习日</span>
      </div>

      <!-- 任务网格 -->
      <div class="task-grid">
        <div v-for="(task, index) in currentTasks" :key="index" class="task-card" :style="{
          animationDelay: `${index * 0.08}s`,
          backgroundColor: getRandomColorScheme().bgColor,
          '--task-color': getRandomColorScheme().color
        }">
          <div class="card-glow"></div>
          <div class="card-content">
            <div class="task-header-row">
              <div class="task-icon-wrapper" :style="{ backgroundColor: 'rgba(255,255,255,0.5)' }">
                <span class="task-icon">{{ getTaskTypeInfo(task.type).icon }}</span>
              </div>
              <span class="task-duration-badge">{{ task.duration }}</span>
            </div>
            <h4 class="task-name" :style="{ color: 'var(--task-color)' }">{{ task.title }}</h4>
            <div class="task-time">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span>{{ task.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

.daily-tasks-section {
  position: relative;
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  background: var(--card-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 28px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.04),
    0 1px 3px rgba(0, 0, 0, 0.02);
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 32px;
}

/* 头部 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow:
    0 8px 24px rgba(139, 92, 246, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.title-text h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1e1b4b;
  margin: 0;
  letter-spacing: -0.02em;
}

.title-text p {
  font-size: 13px;
  color: #6b7280;
  margin: 4px 0 0;
}

/* 导航箭头 */
.nav-arrows {
  display: flex;
  gap: 10px;
}

.nav-btn {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(139, 92, 246, 0.15);
  border-radius: 12px;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
}

.nav-btn:hover {
  background: white;
  border-color: rgba(139, 92, 246, 0.3);
  color: #8b5cf6;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(139, 92, 246, 0.15);
}

/* 题型分类 */
.type-tags-section {
  margin-bottom: 24px;
}

.section-title-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e1b4b;
  margin: 0;
}

.section-count {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 500;
}

.type-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 16px;
  border: 2px solid #e2e8f0;
}

.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: var(--tag-color);
  border: 1px solid color-mix(in srgb, var(--tag-color) 20%, transparent);
  box-shadow: 0 2px 8px color-mix(in srgb, var(--tag-color) 8%, transparent);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.type-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px color-mix(in srgb, var(--tag-color) 15%, transparent);
  border-color: color-mix(in srgb, var(--tag-color) 40%, transparent);
}

.tag-icon {
  font-size: 16px;
  line-height: 1;
}

.tag-label {
  font-weight: 600;
  letter-spacing: -0.01em;
}

/* 星期标签 */
.weekday-tabs {
  position: relative;
  display: flex;
  gap: 10px;
  margin-bottom: 28px;
  padding: 8px;
  background: #f8fafc;
  border-radius: 16px;
  border: 2px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.weekday-btn {
  position: relative;
  flex: 1;
  padding: 12px 8px;
  background: transparent;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.weekday-btn:hover {
  color: #4b5563;
  background: rgba(255, 255, 255, 0.5);
}

.weekday-btn.active {
  color: white;
  font-weight: 600;
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
  box-shadow:
    0 4px 16px rgba(139, 92, 246, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.day-name {
  position: relative;
  z-index: 1;
}

.active-indicator {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
}

/* 任务标题行 */
.task-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-left h3 {
  font-size: 18px;
  font-weight: 700;
  color: #1e1b4b;
  margin: 0;
}

.task-count {
  padding: 4px 12px;
  background: rgba(139, 92, 246, 0.1);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #8b5cf6;
}

.status-badge {
  padding: 6px 14px;
  background: rgba(251, 191, 36, 0.15);
  border: 1px solid rgba(251, 191, 36, 0.2);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #d97706;
}

/* 任务网格 */
.task-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.task-card {
  position: relative;
  border-radius: 16px;
  padding: 18px;
  cursor: pointer;
  overflow: hidden;
  animation: slideUp 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
}

/* 7种随机配色方案 */
@keyframes slideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.task-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.card-glow {
  display: none;
}

.card-content {
  position: relative;
  z-index: 1;
}

.task-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.task-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
}

.task-card:hover .task-icon-wrapper {
  transform: scale(1.1);
}

.task-icon {
  font-size: 20px;
}

.task-duration-badge {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
}

.task-name {
  font-size: 15px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: -0.01em;
  /* 颜色通过内联样式动态设置 */
}

.task-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.task-time svg {
  opacity: 0.5;
}

/* 响应式 */
@media (max-width: 640px) {
  .content-wrapper {
    padding: 20px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .title-text h2 {
    font-size: 20px;
  }

  .type-tags {
    padding: 12px;
    gap: 8px;
  }

  .type-tag {
    padding: 6px 10px;
    font-size: 12px;
  }

  .tag-icon {
    font-size: 14px;
  }

  .weekday-tabs {
    gap: 4px;
  }

  .weekday-btn {
    padding: 10px 4px;
    font-size: 12px;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }

  .task-title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .status-badge {
    display: none;
  }
}
</style>
