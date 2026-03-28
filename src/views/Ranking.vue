<script setup>
import { computed } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const trackMap = {
  algo: '算法思维',
  ds: '数据结构',
  contest: '竞赛冲刺',
}

const rankingRows = computed(() => userStore.getLeaderboard())

const topThree = computed(() => rankingRows.value.slice(0, 3))

const getTrackLabel = (trackCode) => trackMap[trackCode] || '算法思维'

const rowClassName = ({ row }) => {
  if (!userStore.userInfo) return ''
  return Number(row.id) === Number(userStore.userInfo.id) ? 'self-row' : ''
}
</script>

<template>
  <div class="page-container ranking-page">
    <h2 class="section-title">积分排行</h2>

    <section class="podium-grid">
      <el-card v-for="item in topThree" :key="item.id" class="podium-item surface-card" shadow="never">
        <div class="rank-badge">TOP {{ item.rank }}</div>
        <div class="name">{{ item.name }}</div>
        <div class="score">{{ item.points }} 分</div>
        <div class="track">{{ getTrackLabel(item.targetTrack) }}</div>
      </el-card>
    </section>

    <el-card class="surface-card" shadow="never">
      <el-table :data="rankingRows" stripe :row-class-name="rowClassName">
        <el-table-column prop="rank" label="排名" width="90" />
        <el-table-column prop="name" label="昵称" min-width="180" />
        <el-table-column label="赛道" min-width="160">
          <template #default="scope">
            {{ getTrackLabel(scope.row.targetTrack) }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="140" />
        <el-table-column label="状态" width="140">
          <template #default="scope">
            <el-tag :type="scope.row.rank <= 3 ? 'warning' : 'info'" effect="light">
              {{ scope.row.rank <= 3 ? '冲榜中' : '上升中' }} </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.ranking-page {
  padding-bottom: 28px;
  display: grid;
  gap: 16px;
}

.podium-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.podium-item {
  border: 1px solid var(--line-soft);
}

.rank-badge {
  font-size: 12px;
  color: var(--text-sub);
}

.name {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-title);
}

.score {
  margin-top: 6px;
  font-size: 24px;
  font-weight: 800;
  color: var(--text-title);
}

.track {
  margin-top: 4px;
  color: var(--text-sub);
}

:deep(.el-table .self-row) {
  --el-table-tr-bg-color: #edf3fb;
}

@media (max-width: 900px) {
  .podium-grid {
    grid-template-columns: 1fr;
  }
}
</style>
