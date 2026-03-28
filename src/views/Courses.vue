<script setup>
import { computed, ref } from 'vue'

const activeTrack = ref('all')

const courses = ref([
  {
    id: 1,
    title: '算法思维训练营（基础）',
    teacher: '陈老师',
    track: 'algo',
    duration: '16 课时',
    progress: 35,
    tags: ['复杂度', '双指针', '二分'],
  },
  {
    id: 2,
    title: '数据结构高频实战',
    teacher: '王老师',
    track: 'ds',
    duration: '20 课时',
    progress: 62,
    tags: ['链表', '哈希', '并查集'],
  },
  {
    id: 3,
    title: '竞赛冲刺：贪心与图论',
    teacher: '李老师',
    track: 'contest',
    duration: '24 课时',
    progress: 12,
    tags: ['贪心', '最短路', '拓扑排序'],
  },
  {
    id: 4,
    title: 'DP 专题从入门到模板',
    teacher: '赵老师',
    track: 'algo',
    duration: '18 课时',
    progress: 0,
    tags: ['记忆化', '状态转移', '背包'],
  },
])

const trackOptions = [
  { label: '全部', value: 'all' },
  { label: '算法思维', value: 'algo' },
  { label: '数据结构', value: 'ds' },
  { label: '竞赛冲刺', value: 'contest' },
]

const filteredCourses = computed(() => {
  if (activeTrack.value === 'all') return courses.value
  return courses.value.filter((item) => item.track === activeTrack.value)
})

const trackLabelMap = {
  algo: '算法思维',
  ds: '数据结构',
  contest: '竞赛冲刺',
}

const getTrackLabel = (track) => trackLabelMap[track] || '综合'

const actionText = (progress) => {
  if (progress <= 0) return '开始学习'
  if (progress >= 100) return '复习课程'
  return '继续学习'
}
</script>

<template>
  <div class="page-container courses-page">
    <section class="surface-card header-card">
      <div>
        <h2 class="section-title">网课中心</h2>
        <p class="subtitle">按赛道选择课程，结合闯关和错题复盘提升学习效率。</p>
      </div>
      <el-segmented v-model="activeTrack" :options="trackOptions" />
    </section>

    <section class="course-grid">
      <el-card v-for="item in filteredCourses" :key="item.id" class="course-card" shadow="never">
        <div class="card-head">
          <h3>{{ item.title }}</h3>
          <el-tag type="primary" effect="plain">{{ getTrackLabel(item.track) }}</el-tag>
        </div>

        <p class="meta">讲师：{{ item.teacher }} · {{ item.duration }}</p>

        <div class="tag-row">
          <el-tag v-for="tag in item.tags" :key="tag" size="small" effect="light">{{ tag }}</el-tag>
        </div>

        <div class="progress-row">
          <el-progress :percentage="item.progress" :stroke-width="10" />
        </div>

        <div class="footer-row">
          <span class="progress-text">已学习 {{ item.progress }}%</span>
          <el-button type="primary" plain>{{ actionText(item.progress) }}</el-button>
        </div>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.courses-page {
  padding-bottom: 28px;
  display: grid;
  gap: 14px;
}

.header-card {
  padding: 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid var(--line-soft);
}

.subtitle {
  color: var(--text-sub);
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.course-card {
  border: 1px solid var(--line-soft);
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.card-head h3 {
  color: var(--text-title);
  font-size: 19px;
}

.meta {
  margin-top: 10px;
  color: var(--text-sub);
}

.tag-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.progress-row {
  margin-top: 16px;
}

.footer-row {
  margin-top: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-text {
  color: var(--text-sub);
}

@media (max-width: 900px) {
  .header-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .course-grid {
    grid-template-columns: 1fr;
  }
}
</style>
