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
const turnXPattern = [14, 82, 22, 80, 20, 78, 18, 76]
const NODE_VERTICAL_GAP = 170
const MAP_TOP_PADDING = 14
const MAP_BOTTOM_PADDING = 16

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

const getLevelStars = (level) => {
  if (!level) return 0
  const rawStars = Number(level.bestStars ?? level.stars ?? 0)
  if (!Number.isFinite(rawStars)) return 0
  return Math.max(0, Math.min(3, Math.floor(rawStars)))
}

const findPreferredLevelId = () => {
  if (!trackLevels.value.length) return null
  const firstUnlockedNotFull = trackLevels.value.find((item) => item.isUnlocked && getLevelStars(item) < 3)
  if (firstUnlockedNotFull) return firstUnlockedNotFull.id
  const latestUnlocked = [...trackLevels.value].reverse().find((item) => item.isUnlocked)
  return latestUnlocked?.id || trackLevels.value[0]?.id || null
}

const progressLevelId = computed(() => {
  if (!trackLevels.value.length) return null
  const latestCompleted = [...trackLevels.value].reverse().find((item) => item.isCompleted)
  if (latestCompleted) return latestCompleted.id
  const latestUnlocked = [...trackLevels.value].reverse().find((item) => item.isUnlocked)
  return latestUnlocked?.id || trackLevels.value[0]?.id || null
})

const mapTrackStyle = computed(() => {
  const expectedHeight = (trackLevels.value.length - 1) * NODE_VERTICAL_GAP + MAP_TOP_PADDING + MAP_BOTTOM_PADDING + 90
  return {
    minHeight: `${Math.max(700, expectedHeight)}px`,
  }
})

const nodePoints = computed(() => {
  const count = trackLevels.value.length
  if (!count) return []
  if (count === 1) {
    return [{ x: 24, y: 14 }]
  }

  const top = MAP_TOP_PADDING
  const bottom = 100 - MAP_BOTTOM_PADDING
  const step = (bottom - top) / (count - 1)
  return Array.from({ length: count }, (_, index) => ({
    x: turnXPattern[index % turnXPattern.length],
    y: Number((top + step * index).toFixed(2)),
  }))
})

const buildRoadPath = (points) => {
  if (!points.length) return ''
  if (points.length === 1) {
    const point = points[0]
    return `M ${point.x} ${point.y} L ${point.x + 0.1} ${point.y + 0.1}`
  }

  const tension = 0.26
  let path = `M ${points[0].x} ${points[0].y}`
  for (let index = 0; index < points.length - 1; index += 1) {
    const p0 = points[index - 1] || points[index]
    const p1 = points[index]
    const p2 = points[index + 1]
    const p3 = points[index + 2] || p2

    const cp1x = Number((p1.x + (p2.x - p0.x) * tension).toFixed(2))
    const cp1y = Number((p1.y + (p2.y - p0.y) * tension).toFixed(2))
    const cp2x = Number((p2.x - (p3.x - p1.x) * tension).toFixed(2))
    const cp2y = Number((p2.y - (p3.y - p1.y) * tension).toFixed(2))

    path += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p2.x} ${p2.y}`
  }
  return path
}

const roadPath = computed(() => buildRoadPath(nodePoints.value))

const getNodeStyle = (index) => {
  const point = nodePoints.value[index] || { x: 50, y: 50 }
  return {
    left: `${point.x}%`,
    top: `${point.y}%`,
  }
}

const getNodeStateClass = (level) => {
  if (!level?.isUnlocked) return 'state-locked'
  if (level.isCompleted) return 'state-completed'
  return 'state-unlocked'
}

const getRunnerSideClass = (index) => {
  const point = nodePoints.value[index] || { x: 50 }
  return point.x >= 50 ? 'inner-left' : 'inner-right'
}

const handleStart = (level) => {
  if (!level.isUnlocked) {
    ElMessage.warning('该关卡尚未解锁')
    return
  }
  router.push(`/challenge/${level.id}`)
}

const loadData = async () => {
  loading.value = true
  try {
    await levelStore.fetchLevels()
    selectedLevelId.value = findPreferredLevelId()
  } catch (error) {
    ElMessage.error('关卡加载失败')
  } finally {
    loading.value = false
  }
}

watch(activeTrack, () => {
  selectedLevelId.value = findPreferredLevelId()
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
          <div class="map-track" v-if="trackLevels.length" :style="mapTrackStyle">
            <svg class="road-svg" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
              <path class="road-edge" :d="roadPath" />
              <path class="road-fill" :d="roadPath" />
            </svg>
            <div v-for="(item, index) in trackLevels" :key="item.id" class="level-node-wrap"
              :style="getNodeStyle(index)">
              <div class="node-stars" :class="{ dimmed: !item.isUnlocked }">
                <span v-for="star in 3" :key="`${item.id}-${star}`" class="star"
                  :class="{ filled: star <= getLevelStars(item) }">
                  ★
                </span>
              </div>
              <div v-if="Number(item.id) === Number(progressLevelId)" class="runner-marker"
                :class="getRunnerSideClass(index)" aria-hidden="true">
                <span class="runner-crew">
                  <span class="crew-char crew-purple">
                    <span class="crew-eyes eyes-white">
                      <span class="eye"><i class="pupil"></i></span>
                      <span class="eye"><i class="pupil"></i></span>
                    </span>
                  </span>
                  <span class="crew-char crew-black">
                    <span class="crew-eyes eyes-white small-gap">
                      <span class="eye small"><i class="pupil small"></i></span>
                      <span class="eye small"><i class="pupil small"></i></span>
                    </span>
                  </span>
                  <span class="crew-char crew-orange">
                    <span class="crew-eyes eyes-orange">
                      <i class="pupil-dot"></i>
                      <i class="pupil-dot"></i>
                    </span>
                  </span>
                  <span class="crew-char crew-yellow">
                    <span class="crew-face">
                      <span class="crew-eyes eyes-yellow">
                        <i class="pupil-dot"></i>
                        <i class="pupil-dot"></i>
                      </span>
                      <span class="crew-mouth"></span>
                    </span>
                  </span>
                </span>
              </div>
              <button class="level-node" :class="[getNodeStateClass(item), {
                active: Number(item.id) === Number(selectedLevelId),
              }]" type="button" @click="selectLevel(item)">
                <span class="node-index">{{ index + 1 }}</span>
              </button>
              <div v-if="!item.isUnlocked" class="node-lock-badge" aria-hidden="true">
                <svg viewBox="0 0 24 24">
                  <path
                    d="M12 2a5 5 0 0 0-5 5v2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8a2 2 0 0 0-2-2h-1V7a5 5 0 0 0-5-5m-3 7V7a3 3 0 1 1 6 0v2zm3 4a2 2 0 0 1 1 3.72V18h-2v-1.28A2 2 0 0 1 12 13" />
                </svg>
              </div>
              <div class="level-node-name">{{ item.name }}</div>
            </div>
          </div>
          <div v-else class="empty-map">当前赛道暂无关卡</div>
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
          <div class="meta-row">
            <el-tag type="success" effect="plain">最佳星级 {{ getLevelStars(selectedLevel) }}/3</el-tag>
            <el-tag :type="selectedLevel.isCompleted ? 'success' : 'info'" effect="plain">
              {{ selectedLevel.isCompleted ? '已通关' : '未通关' }}
            </el-tag>
          </div>
          <p v-if="!selectedLevel.isUnlocked" class="unlock-tip">完成同赛道上一关后可解锁当前关卡</p>
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
  min-height: 560px;
  padding: 14px;
  border: 1px solid var(--line-soft);
  border-radius: 18px;
  background:
    radial-gradient(circle at 9% 10%, rgba(123, 176, 83, 0.9) 0 48px, transparent 49px),
    radial-gradient(circle at 85% 20%, rgba(118, 173, 78, 0.84) 0 42px, transparent 43px),
    radial-gradient(circle at 15% 82%, rgba(120, 176, 82, 0.88) 0 56px, transparent 57px),
    radial-gradient(circle at 90% 86%, rgba(119, 173, 79, 0.85) 0 50px, transparent 51px),
    linear-gradient(180deg, #a8cf70 0%, #9fc767 100%);
  overflow: hidden;
}

.map-track {
  position: relative;
  min-height: 530px;
  width: 100%;
}

.road-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.road-edge {
  fill: none;
  stroke: #79ad4d;
  stroke-width: 18;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.road-fill {
  fill: none;
  stroke: #efd49b;
  stroke-width: 12;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.level-node-wrap {
  position: absolute;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 180px;
  transform: translate(-50%, -50%);
}

.runner-marker {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  pointer-events: none;
  animation: crew-float 2.6s ease-in-out infinite;
}

.runner-marker.inner-left {
  left: 18px;
}

.runner-marker.inner-right {
  right: 18px;
}

.runner-crew {
  position: relative;
  display: inline-block;
  width: 62px;
  height: 46px;
  animation: crew-breath 2.6s ease-in-out infinite;
}

.crew-char {
  position: absolute;
  bottom: 0;
  transform-origin: center bottom;
}

.crew-eyes {
  position: absolute;
  display: flex;
  align-items: center;
}

.eye {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #ffffff;
  display: grid;
  place-items: center;
}

.eye.small {
  width: 4px;
  height: 4px;
}

.pupil {
  width: 2px;
  height: 2px;
  border-radius: 50%;
  background: #2d2d2d;
}

.pupil.small {
  width: 1.6px;
  height: 1.6px;
}

.pupil-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #2d2d2d;
}

.crew-purple {
  left: 8px;
  width: 21px;
  height: 46px;
  background: #6c3ff5;
  border-radius: 2px 2px 0 0;
}

.crew-black {
  left: 28px;
  width: 14px;
  height: 36px;
  background: #2d2d2d;
  border-radius: 2px 2px 0 0;
}

.crew-orange {
  left: 0;
  width: 28px;
  height: 23px;
  background: #ff9b6b;
  border-radius: 14px 14px 0 0;
}

.crew-yellow {
  left: 36px;
  width: 16px;
  height: 26px;
  background: #e8d754;
  border-radius: 8px 8px 0 0;
}

.crew-purple .crew-eyes {
  left: 7px;
  top: 5px;
  gap: 4px;
}

.crew-black .crew-eyes {
  left: 4px;
  top: 4px;
  gap: 3px;
}

.crew-orange .crew-eyes {
  left: 9px;
  top: 10px;
  gap: 4px;
}

.crew-face {
  position: absolute;
  left: 5px;
  top: 4px;
}

.crew-yellow .crew-eyes {
  position: relative;
  left: 0;
  top: 0;
  gap: 3px;
}

.crew-mouth {
  margin-top: 4px;
  width: 9px;
  height: 1.6px;
  border-radius: 999px;
  background: #2d2d2d;
}

@keyframes crew-float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-3px);
  }
}

@keyframes crew-breath {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.08);
  }
}

.level-node {
  width: 104px;
  height: 58px;
  border: 3px solid #f4f8eb;
  border-radius: 999px;
  position: relative;
  background: linear-gradient(180deg, #bad68a 0%, #9fbe6f 100%);
  color: #f8fff0;
  font-size: 36px;
  font-weight: 700;
  cursor: pointer;
  text-shadow: 0 2px 0 rgba(112, 139, 78, 0.8);
  box-shadow: 0 6px 0 #bb8d53, 0 12px 0 #8a6133;
}

.level-node::before {
  content: '';
  position: absolute;
  left: 7px;
  right: 7px;
  top: 7px;
  height: 18px;
  border-radius: 999px;
  background: rgba(237, 247, 210, 0.62);
}

.node-index {
  position: relative;
  z-index: 1;
  line-height: 0.95;
}

.level-node.active {
  box-shadow: 0 0 0 4px rgba(244, 248, 233, 0.75), 0 6px 0 #bb8d53, 0 12px 0 #8a6133;
}

.level-node.state-completed {
  border-color: #ffe39b;
  background: linear-gradient(180deg, #c7e48f 0%, #afd374 100%);
  box-shadow: 0 6px 0 #d6a05c, 0 12px 0 #8a6133;
}

.level-node.state-unlocked {
  border-color: #f2f6e8;
  background: linear-gradient(180deg, #afcf7c 0%, #95b963 100%);
  box-shadow: 0 6px 0 #be945d, 0 12px 0 #7f5d3a;
}

.level-node.state-locked {
  border-color: #e4e7e0;
  background: linear-gradient(180deg, #b5bfaa 0%, #9ca794 100%);
  color: #f1f3ec;
  text-shadow: 0 2px 0 rgba(117, 126, 108, 0.7);
  box-shadow: 0 6px 0 #a48e6f, 0 12px 0 #7f6548;
}

.node-lock-badge {
  position: absolute;
  left: 50%;
  bottom: 20px;
  transform: translateX(-50%);
  width: 32px;
  height: 20px;
  display: grid;
  place-items: center;
  color: #f6f9f0;
  background: rgba(77, 92, 65, 0.72);
  border: 1px solid rgba(226, 235, 206, 0.45);
  border-radius: 999px;
  z-index: 2;
}

.node-lock-badge svg {
  width: 14px;
  height: 14px;
  fill: currentColor;
}

.level-node-name {
  max-width: 176px;
  color: #f4faeb;
  background: rgba(88, 132, 56, 0.5);
  border: 1px solid rgba(218, 238, 180, 0.45);
  border-radius: 10px;
  padding: 5px 10px;
  font-size: 13px;
  font-weight: 500;
}

.node-stars {
  position: absolute;
  top: -36px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  justify-content: center;
  align-items: center;
  width: 110px;
  gap: 6px;
  padding: 0;
}

.node-stars.dimmed {
  opacity: 0.6;
}

.star {
  font-size: 25px;
  line-height: 1;
  color: #f4f6ef;
  text-shadow:
    -2px 0 #88ae5e,
    2px 0 #88ae5e,
    0 -2px #88ae5e,
    0 2px #88ae5e,
    0 3px 0 rgba(255, 255, 255, 0.55);
}

.star.filled {
  color: #ffd84a;
  text-shadow:
    -2px 0 #7cae4e,
    2px 0 #7cae4e,
    0 -2px #7cae4e,
    0 2px #7cae4e,
    0 2px 0 #fff2b6;
}

.empty-map {
  height: 100%;
  min-height: 320px;
  display: grid;
  place-items: center;
  color: var(--text-sub);
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
  flex-wrap: wrap;
  gap: 10px;
}

.unlock-tip {
  margin: 0;
  color: #6a7f99;
}

@media (max-width: 900px) {
  .stage-layout {
    grid-template-columns: 1fr;
  }

  .level-map {
    min-height: 430px;
    padding: 10px;
  }

  .map-track {
    min-height: 460px;
  }

  .road-svg {
    inset: 0;
    width: 100%;
    height: 100%;
  }

  .road-edge {
    stroke-width: 20;
  }

  .road-fill {
    stroke-width: 15;
  }

  .level-node-wrap {
    width: 148px;
  }

  .runner-marker {
    top: -42px;
  }

  .runner-text {
    display: none;
  }

  .level-node {
    width: 88px;
    height: 52px;
    font-size: 30px;
  }

  .node-stars {
    top: -30px;
    width: 96px;
    gap: 4px;
  }

  .level-node-name {
    max-width: 180px;
    font-size: 12px;
  }

  .star {
    font-size: 22px;
  }
}
</style>
