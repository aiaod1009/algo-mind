<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLevelStore } from '../stores/level'

const router = useRouter()
const levelStore = useLevelStore()

const loading = ref(false)

const PROJECT_LIBRARY = [
  {
    id: 'path-visualizer',
    name: '最短路径可视化器',
    summary: '输入地图节点后动态展示 BFS/Dijkstra 搜索过程，适合作为图算法实战入门。',
    icon: '🗺️',
    trackCode: 'algo',
    tag: '图算法',
  },
  {
    id: 'schedule-planner',
    name: '课程排期小助手',
    summary: '基于贪心和区间策略自动安排课程/任务时间，输出冲突检测结果。',
    icon: '📅',
    trackCode: 'algo',
    tag: '贪心策略',
  },
  {
    id: 'route-recommend',
    name: '通勤路线推荐器',
    summary: '综合权重计算多条候选路径，给出时间优先或换乘最少方案。',
    icon: '🚇',
    trackCode: 'algo',
    tag: '最优化',
  },
  {
    id: 'todo-priority',
    name: '任务优先级引擎',
    summary: '使用堆和排序策略实时调整任务优先级，支持延期与插队。',
    icon: '✅',
    trackCode: 'ds',
    tag: '堆与排序',
  },
  {
    id: 'snippet-manager',
    name: '代码片段管理器',
    summary: '借助哈希表与前缀检索快速查找代码片段，支持标签过滤。',
    icon: '📦',
    trackCode: 'ds',
    tag: '哈希与检索',
  },
  {
    id: 'lru-cache-lab',
    name: 'LRU 缓存模拟器',
    summary: '实现并可视化缓存淘汰机制，观察链表+哈希的性能收益。',
    icon: '💾',
    trackCode: 'ds',
    tag: '链表设计',
  },
  {
    id: 'judge-rank',
    name: '迷你 OJ 排名系统',
    summary: '模拟比赛提交和滚榜规则，练习高频数据更新与统计。',
    icon: '🏆',
    trackCode: 'contest',
    tag: '竞赛模拟',
  },
  {
    id: 'live-scoreboard',
    name: '实时积分看板',
    summary: '在高频更新场景下维护实时榜单，考验复杂度控制能力。',
    icon: '📊',
    trackCode: 'contest',
    tag: '实时统计',
  },
  {
    id: 'matchmaking',
    name: '题目匹配推荐器',
    summary: '根据难度与标签进行快速匹配，输出个性化刷题建议。',
    icon: '🎯',
    trackCode: 'contest',
    tag: '匹配策略',
  },
]

const activeTrack = ref('all')

const trackOptions = [
  { label: '全部项目', value: 'all' },
  { label: '算法思维', value: 'algo' },
  { label: '数据结构', value: 'ds' },
  { label: '竞赛冲刺', value: 'contest' },
]

const filteredProjects = computed(() => {
  let list = PROJECT_LIBRARY
  if (activeTrack.value !== 'all') {
    list = list.filter((item) => item.trackCode === activeTrack.value)
  }
  return list.map((project) => {
    const track = levelStore.tracks.find((item) => item.code === project.trackCode)
    const levels = levelStore.getLevelsByTrack(project.trackCode)
    const completed = levels.filter((item) => item.isCompleted).length
    const total = levels.length
    const unlocked = levels.filter((item) => item.isUnlocked).length

    return {
      ...project,
      trackName: track?.name || '算法项目赛道',
      completed,
      total,
      unlocked,
    }
  })
})

const loadData = async () => {
  if (levelStore.levels.length) {
    loading.value = false
    return
  }

  const hydrated = levelStore.hydrateLevelsFromLocal()
  if (hydrated) {
    loading.value = false
    levelStore.fetchLevels().catch(() => {
      ElMessage.warning('远端关卡刷新失败，已显示本地缓存')
    })
    return
  }

  loading.value = true
  try {
    await levelStore.fetchLevels()
  } catch (error) {
    ElMessage.error('项目数据加载失败')
  } finally {
    loading.value = false
  }
}

const openProject = (project) => {
  router.push({
    path: '/levels',
    query: {
      project: project.trackCode,
      projectName: project.name,
    },
  })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container projects-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">项目实战</h1>
        <p class="page-desc">基于真实场景的算法项目，在实战中淬炼工程能力，让代码不再停留在控制台</p>
      </div>
      <div class="track-filter">
        <button v-for="opt in trackOptions" :key="opt.value"
          :class="['filter-btn', { active: activeTrack === opt.value }]" @click="activeTrack = opt.value">
          {{ opt.label }}
        </button>
      </div>
    </div>

    <el-skeleton :loading="loading" animated :rows="5">
      <section class="project-grid">
        <button v-for="item in filteredProjects" :key="item.id" class="surface-card project-card" type="button"
          @click="openProject(item)">
          <div class="card-top">
            <span class="project-icon" aria-hidden="true">{{ item.icon }}</span>
            <span class="project-name">{{ item.name }}</span>
          </div>

          <p class="project-goal">{{ item.summary }}</p>

          <div class="project-track-row">
            <span class="project-track">训练赛道：{{ item.trackName }}</span>
            <span class="project-tag">{{ item.tag }}</span>
          </div>

          <div class="project-meta">
            <span>已完成 {{ item.completed }}/{{ item.total }}</span>
            <span>已解锁 {{ item.unlocked }} 关</span>
          </div>

          <div class="project-cta">进入项目关卡</div>
        </button>
      </section>
    </el-skeleton>
  </div>
</template>

<style scoped>
.projects-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: var(--card-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  padding: 24px 32px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 24px;
  background: #3b82f6;
  border-radius: 2px;
}

.page-desc {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.track-filter {
  display: flex;
  gap: 8px;
  background: #f8fafc;
  padding: 4px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.filter-btn {
  padding: 8px 16px;
  border: none;
  background: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-btn:hover {
  color: #334155;
  background: #f1f5f9;
}

.filter-btn.active {
  background: #3b82f6;
  color: white;
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.2);
}

.project-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.project-card {
  border: 1px solid rgba(120, 146, 172, 0.2);
  border-radius: 16px;
  padding: 20px;
  text-align: left;
  cursor: pointer;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(248, 252, 255, 0.98) 100%);
  box-shadow: 0 7px 20px rgba(53, 86, 121, 0.08);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.project-card:hover {
  transform: translateY(-4px);
  border-color: rgba(16, 185, 129, 0.35);
  box-shadow: 0 14px 28px rgba(34, 73, 117, 0.16);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.project-icon {
  display: inline-grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: rgba(89, 125, 166, 0.12);
  font-size: 20px;
  line-height: 1;
}

.project-name {
  font-size: 20px;
  font-weight: 700;
  color: #1f3351;
}

.project-goal {
  margin: 14px 0;
  color: #4f6781;
  line-height: 1.6;
  min-height: 76px;
}

.project-track-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.project-track {
  font-size: 12px;
  color: #597394;
}

.project-tag {
  font-size: 12px;
  color: #0f9f72;
  background: rgba(15, 159, 114, 0.12);
  border: 1px solid rgba(15, 159, 114, 0.25);
  border-radius: 999px;
  padding: 1px 8px;
}

.project-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 6px;
  font-size: 13px;
  color: #4a688a;
}

.project-cta {
  margin-top: 15px;
  width: fit-content;
  color: #0d8f68;
  font-weight: 700;
  border-bottom: 2px solid rgba(13, 143, 104, 0.25);
}

@media (max-width: 1200px) {
  .project-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .projects-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
    gap: 16px;
    margin-top: 10px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-desc {
    font-size: 14px;
    line-height: 1.5;
  }

  .track-filter {
    width: 100%;
    overflow-x: auto;
    white-space: nowrap;
    justify-content: flex-start;
    padding-bottom: 2px;
  }

  .filter-btn {
    flex-shrink: 0;
  }

  .project-grid {
    grid-template-columns: 1fr;
  }

  .project-card {
    padding: 16px;
  }

  .project-name {
    font-size: 18px;
  }

  .project-goal {
    min-height: 0;
  }
}
</style>
