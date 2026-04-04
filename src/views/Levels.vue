<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLevelStore } from '../stores/level'
import { useUserStore } from '../stores/user'

const router = useRouter()
const levelStore = useLevelStore()
const userStore = useUserStore()
const loading = ref(false)
const activeTrack = ref(userStore.userInfo?.targetTrack || userStore.selectedTrack || 'algo')
const selectedLevelId = ref(null)

const trackList = computed(() => levelStore.tracks)

const trackLevels = computed(() => levelStore.getLevelsByTrack(activeTrack.value))

const selectedLevel = computed(() => {
  if (!selectedLevelId.value) {
    return trackLevels.value[0] || null
  }
  return trackLevels.value.find((item) => Number(item.id) === Number(selectedLevelId.value)) || null
})

const typeTagMap = {
  single: '单选',
  multi: '多选',
  judge: '判断',
  fill: '填空',
  code: '代码',
}

const handleStart = (level) => {
  if (!level.isUnlocked) {
    ElMessage.warning('该关卡尚未解锁')
    return
  }
  router.push(`/challenge/${level.id}`)
}

const loadData = async () => {
<<<<<<< Updated upstream
=======
  if (levelStore.levels.length) {
    loading.value = false
    return
  }

  const hydrated = levelStore.hydrateLevelsFromLocal()
  if (hydrated) {
    loading.value = false
    try {
      await levelStore.fetchLevels()
    } catch (error) {
      ElMessage.warning('远端关卡刷新失败，当前展示为本地缓存')
    }
    return
  }

>>>>>>> Stashed changes
  loading.value = true
  try {
    await levelStore.fetchLevels()
    selectedLevelId.value = trackLevels.value[0]?.id || null
  } catch (error) {
    ElMessage.error('关卡加载失败，请检查网络后重试')
  } finally {
    loading.value = false
  }
}

watch(activeTrack, () => {
  selectedLevelId.value = trackLevels.value[0]?.id || null
})

const selectLevel = (item) => {
  selectedLevelId.value = item.id
}

onMounted(loadData)
</script>

<template>
  <div class="page-container levels-page">
    <h2 class="section-title">选关地图</h2>

    <el-tabs v-model="activeTrack" class="track-tabs">
      <el-tab-pane v-for="item in trackList" :key="item.code" :label="item.name" :name="item.code">
        <div class="track-goal">目标：{{ item.goal }}</div>
      </el-tab-pane>
    </el-tabs>

    <el-skeleton :loading="loading" animated :rows="6">
      <div class="stage-layout">
        <section class="surface-card level-map">
          <div v-for="(item, index) in trackLevels" :key="item.id" class="level-node-wrap">
            <button class="level-node" :class="{
              locked: !item.isUnlocked,
              active: Number(item.id) === Number(selectedLevelId),
            }" type="button" @click="selectLevel(item)">
              {{ index + 1 }}
            </button>
            <div class="level-node-name">{{ item.name }}</div>
            <div v-if="index < trackLevels.length - 1" class="connector"></div>
          </div>
        </section>

        <section class="surface-card detail-panel" v-if="selectedLevel">
          <div class="detail-head">
            <h3>{{ selectedLevel.name }}</h3>
            <el-tag :type="selectedLevel.isUnlocked ? 'success' : 'info'" effect="light">
              {{ selectedLevel.isUnlocked ? '已解锁' : '未解锁' }}
            </el-tag>
          </div>
          <p class="detail-desc">{{ selectedLevel.description }}</p>
          <div class="meta-row">
            <el-tag type="warning" effect="plain">奖励 {{ selectedLevel.rewardPoints }} 积分</el-tag>
            <el-tag type="primary" effect="plain">题型 {{ typeTagMap[selectedLevel.type] || selectedLevel.type }}</el-tag>
          </div>
          <el-button type="primary" :disabled="!selectedLevel.isUnlocked" @click="handleStart(selectedLevel)">
            开始挑战
          </el-button>
        </section>
      </div>
    </el-skeleton>
  </div>
</template>

<style scoped>
.levels-page {
  padding-bottom: 28px;
  display: grid;
  gap: 10px;
}

.track-goal {
  color: var(--text-sub);
}

.stage-layout {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 14px;
}

.level-map {
  min-height: 180px;
  padding: 22px;
  display: flex;
  align-items: flex-start;
  overflow-x: auto;
}

.level-node-wrap {
  display: flex;
  align-items: center;
  position: relative;
}

.level-node {
  width: 56px;
  height: 56px;
  border: 2px solid var(--brand-blue);
  border-radius: 50%;
  background: #edf3fb;
  color: var(--text-title);
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
}

.level-node.active {
  border-color: var(--brand-warm);
  box-shadow: 0 0 0 4px rgba(216, 137, 102, 0.2);
}

.level-node.locked {
  background: #f1f4f8;
  border-color: #c0cbda;
  color: #8092aa;
}

.level-node-name {
  width: 120px;
  margin: 0 12px;
  color: var(--text-sub);
  font-size: 14px;
}

.connector {
  width: 42px;
  height: 2px;
  background: #b4c3d6;
}

.detail-panel {
  padding: 22px;
  border: 1px solid var(--line-soft);
  display: grid;
  gap: 12px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-head h3 {
  color: var(--text-title);
  font-size: 20px;
}

.detail-desc {
  color: var(--text-sub);
}

.meta-row {
  display: flex;
  gap: 10px;
}

@media (max-width: 900px) {
  .stage-layout {
    grid-template-columns: 1fr;
  }

  .level-map {
    padding: 16px;
  }
}
</style>
