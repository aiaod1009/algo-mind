<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLevelStore } from '../stores/level'
import { useUserStore } from '../stores/user'

const router = useRouter()
const levelStore = useLevelStore()
const userStore = useUserStore()

const loading = ref(false)
const dialogVisible = ref(false)
const selectedLevel = ref(null)
const activeTrack = ref(userStore.userInfo?.targetTrack || userStore.selectedTrack || 'algo')
const prefersReducedMotion = ref(false)

let motionMediaQuery = null
let motionListener = null

const trackList = computed(() => levelStore.tracks)
const trackLevels = computed(() => levelStore.getLevelsByTrack(activeTrack.value))

const progressLevelId = computed(() => {
  if (!trackLevels.value.length) return null
  const latestUnlocked = [...trackLevels.value].reverse().find((item) => item.isUnlocked)
  return latestUnlocked?.id || trackLevels.value[0]?.id || null
})

const progressLevelIndex = computed(() => {
  if (!trackLevels.value.length || !progressLevelId.value) return -1
  return trackLevels.value.findIndex((item) => Number(item.id) === Number(progressLevelId.value))
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

const mapWorldStyle = computed(() => {
  const count = trackLevels.value.length
  const expectedWidth = count > 0 ? count * 260 + 400 : 2200
  return {
    minWidth: `${Math.max(2200, expectedWidth)}px`,
  }
})

const getNodePoint = (index) => {
  return {
    x: 140 + index * 260,
  }
}

const getNodeStyle = (index) => {
  const point = getNodePoint(index)
  return {
    left: `${point.x}px`,
    bottom: '100px',
  }
}

const getTruckStyle = (index) => {
  const point = getNodePoint(index)
  return {
    left: `${point.x + 56}px`,
    bottom: '104px',
  }
}

const openLevelDialog = (level) => {
  selectedLevel.value = level
  dialogVisible.value = true
}

const nodeStyles = computed(() => {
  return trackLevels.value.map((_, index) => getNodeStyle(index))
})

const progressTruckStyle = computed(() => {
  if (progressLevelIndex.value < 0) return null
  return getTruckStyle(progressLevelIndex.value)
})

const startChallengeFromDialog = () => {
  if (!selectedLevel.value) return
  if (!selectedLevel.value.isUnlocked) {
    ElMessage.warning('该关卡尚未解锁')
    return
  }
  dialogVisible.value = false
  router.push(`/challenge/${selectedLevel.value.id}`)
}

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
    ElMessage.error('关卡加载失败')
  } finally {
    loading.value = false
  }
}

const setupMotionPreference = () => {
  if (typeof window === 'undefined' || !window.matchMedia) return
  motionMediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
  prefersReducedMotion.value = motionMediaQuery.matches
  motionListener = (event) => {
    prefersReducedMotion.value = event.matches
  }
  if (motionMediaQuery.addEventListener) {
    motionMediaQuery.addEventListener('change', motionListener)
  } else {
    motionMediaQuery.addListener(motionListener)
  }
}

const teardownMotionPreference = () => {
  if (!motionMediaQuery || !motionListener) return
  if (motionMediaQuery.removeEventListener) {
    motionMediaQuery.removeEventListener('change', motionListener)
  } else {
    motionMediaQuery.removeListener(motionListener)
  }
  motionMediaQuery = null
  motionListener = null
}

const handleWheelScroll = (event) => {
  const viewport = event.currentTarget
  if (viewport.scrollWidth > viewport.clientWidth) {
    event.preventDefault()
    viewport.scrollLeft += event.deltaY
  }
}

watch(activeTrack, () => {
  dialogVisible.value = false
  selectedLevel.value = null
})

onMounted(() => {
  setupMotionPreference()
  loadData()
})

onUnmounted(() => {
  teardownMotionPreference()
})
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
      <section class="surface-card pixel-stage">
        <div v-if="trackLevels.length" class="stage-viewport" @wheel.prevent="handleWheelScroll">
          <div class="map-world" :style="mapWorldStyle" :class="{ 'reduced-motion': prefersReducedMotion }">
            <div class="sky-pixels" aria-hidden="true"></div>

            <span class="cloud cloud-a" aria-hidden="true"></span>
            <span class="cloud cloud-b" aria-hidden="true"></span>
            <span class="cloud cloud-c" aria-hidden="true"></span>
            <span class="cloud cloud-d" aria-hidden="true"></span>

            <span class="pipe pipe-a" aria-hidden="true"></span>
            <span class="pipe pipe-b" aria-hidden="true"></span>
            <span class="pipe pipe-c" aria-hidden="true"></span>
            <span class="pipe pipe-d" aria-hidden="true"></span>

            <div v-for="(item, index) in trackLevels" :key="item.id" class="level-sign-wrap" :style="nodeStyles[index]">
              <div class="node-stars" :class="{ dimmed: !item.isUnlocked }">
                <span v-for="star in 3" :key="`${item.id}-${star}`" class="star"
                  :class="{ filled: star <= getLevelStars(item) }">
                  ★
                </span>
              </div>

              <button class="level-sign" :class="{
                completed: item.isCompleted,
                locked: !item.isUnlocked,
              }" type="button" @click="openLevelDialog(item)">
                <span class="sign-plate">
                  <span class="piece-index">{{ item.order || index + 1 }}</span>
                  <span v-if="!item.isUnlocked" class="sign-lock" aria-hidden="true">🔒</span>
                </span>
                <span class="sign-pole" aria-hidden="true"></span>
                <span class="sign-base" aria-hidden="true"></span>
              </button>

              <div class="piece-name">{{ item.name }}</div>
            </div>

            <div v-if="progressTruckStyle" class="progress-truck" :style="progressTruckStyle" aria-hidden="true">
              <span class="truck-body"></span>
              <span class="truck-cab"></span>
              <span class="truck-window"></span>
              <span class="truck-wheel wheel-front"></span>
              <span class="truck-wheel wheel-back"></span>
            </div>

            <div class="ground-line" aria-hidden="true"></div>
            <div class="brick-floor" aria-hidden="true"></div>
          </div>
        </div>

        <div v-else class="empty-map">当前赛道暂无关卡</div>
      </section>
    </el-skeleton>

    <el-dialog v-model="dialogVisible" width="460px" :title="selectedLevel?.name || '关卡信息'" class="level-dialog"
      destroy-on-close>
      <div v-if="selectedLevel" class="dialog-content">
        <p class="dialog-desc">{{ selectedLevel.description }}</p>

        <div class="dialog-stars">
          <span>已存星级</span>
          <span class="stars-inline">
            <span v-for="star in 3" :key="`dialog-${star}`" class="star"
              :class="{ filled: star <= getLevelStars(selectedLevel) }">
              ★
            </span>
          </span>
        </div>

        <div class="dialog-meta">
          <el-tag type="warning" effect="plain">奖励 {{ selectedLevel.rewardPoints }} 积分</el-tag>
          <el-tag type="primary" effect="plain">题型 {{ typeTagMap[selectedLevel.type] || selectedLevel.type }}</el-tag>
          <el-tag :type="selectedLevel.isUnlocked ? 'success' : 'info'" effect="plain">
            {{ selectedLevel.isUnlocked ? '已解锁' : '未解锁' }}
          </el-tag>
        </div>

        <p v-if="!selectedLevel.isUnlocked" class="unlock-tip">完成同赛道上一关后可解锁当前关卡</p>
      </div>

      <template #footer>
        <div class="dialog-actions">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" :disabled="!selectedLevel?.isUnlocked" @click="startChallengeFromDialog">
            开始挑战
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.levels-page {
  display: grid;
  gap: 10px;
  padding-bottom: 26px;
}

.track-goal {
  color: var(--text-sub);
}

.pixel-stage {
  border: 1px solid var(--line-soft);
  border-radius: 18px;
  padding: 10px;
  overflow: hidden;
}

.stage-viewport {
  overflow-x: auto;
  overflow-y: hidden;
  border-radius: 14px;
  background: #6cc3ec;
  padding-bottom: 8px;
}

.map-world {
  position: relative;
  height: 580px;
  border-radius: 14px;
  background:
    radial-gradient(circle at 28% 18%, rgba(255, 255, 255, 0.2) 0 78px, transparent 79px),
    linear-gradient(180deg, #64c2ef 0%, #58bce9 52%, #74c8e9 100%);
  overflow: hidden;
}

.sky-pixels {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle, rgba(255, 255, 255, 0.11) 1px, transparent 1px) 0 0 / 7px 7px,
    radial-gradient(circle, rgba(255, 255, 255, 0.08) 1px, transparent 1px) 3px 3px / 9px 9px;
  opacity: 0.4;
}

.cloud {
  position: absolute;
  width: 190px;
  height: 58px;
  border-radius: 40px 48px 36px 42px;
  background: #fff;
  filter: drop-shadow(0 5px 0 rgba(95, 153, 188, 0.34));
  --cloud-scale: 1;
  animation: cloud-float 10s ease-in-out infinite;
}

.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: #fff;
}

.cloud::before {
  width: 86px;
  height: 54px;
  left: 18px;
  top: -24px;
  border-radius: 60% 56% 52% 58%;
}

.cloud::after {
  width: 96px;
  height: 48px;
  right: 18px;
  top: -18px;
  border-radius: 58% 52% 56% 62%;
}

.cloud-a {
  top: 64px;
  left: 180px;
  width: 230px;
  --cloud-scale: 1.08;
}

.cloud-a::before {
  width: 98px;
  height: 64px;
  left: 28px;
  top: -30px;
}

.cloud-a::after {
  width: 112px;
  height: 54px;
  right: 24px;
  top: -20px;
}

.cloud-b {
  top: 132px;
  left: 860px;
  width: 170px;
  --cloud-scale: 0.94;
}

.cloud-b::before {
  width: 74px;
  height: 46px;
  left: 16px;
  top: -20px;
}

.cloud-b::after {
  width: 80px;
  height: 40px;
  right: 16px;
  top: -14px;
}

.cloud-c {
  top: 96px;
  left: 1500px;
  width: 250px;
  --cloud-scale: 1.14;
}

.cloud-c::before {
  width: 108px;
  height: 66px;
  left: 26px;
  top: -34px;
}

.cloud-c::after {
  width: 122px;
  height: 58px;
  right: 22px;
  top: -24px;
}

.cloud-d {
  top: 148px;
  left: 2070px;
  width: 200px;
  --cloud-scale: 1.02;
}

.cloud-d::before {
  width: 82px;
  height: 50px;
  left: 20px;
  top: -22px;
}

.cloud-d::after {
  width: 95px;
  height: 46px;
  right: 18px;
  top: -18px;
}

.pipe {
  position: absolute;
  bottom: 104px;
  width: 96px;
  height: 146px;
  background:
    linear-gradient(90deg, #2f8f2f 0%, #55c12f 32%, #bcff3c 60%, #4baa28 100%);
  border-left: 4px solid rgba(29, 96, 34, 0.55);
  border-right: 4px solid rgba(28, 92, 31, 0.45);
}

.pipe::before {
  content: '';
  position: absolute;
  top: -18px;
  left: -11px;
  width: 118px;
  height: 24px;
  border-radius: 2px;
  background:
    linear-gradient(90deg, #2b8b2b 0%, #5fcd31 28%, #d8ff56 62%, #4faf29 100%);
  border-left: 3px solid rgba(29, 96, 34, 0.5);
  border-right: 3px solid rgba(28, 92, 31, 0.45);
}

.pipe-a {
  left: 260px;
  height: 170px;
}

.pipe-b {
  left: 740px;
  height: 124px;
}

.pipe-c {
  left: 1320px;
  height: 186px;
}

.pipe-d {
  left: 1870px;
  height: 148px;
}

.level-sign-wrap {
  position: absolute;
  transform: translateX(-50%);
  width: 150px;
  height: 164px;
  z-index: 2;
}

.node-stars {
  position: absolute;
  bottom: 108px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  width: 108px;
  justify-content: center;
  gap: 4px;
}

.node-stars.dimmed {
  opacity: 0.56;
}

.star {
  font-size: 24px;
  line-height: 1;
  color: #f3f7f0;
  text-shadow:
    -2px 0 #5ca03f,
    2px 0 #5ca03f,
    0 -2px #5ca03f,
    0 2px #5ca03f;
}

.star.filled {
  color: #ffd84d;
  text-shadow:
    -2px 0 #568f38,
    2px 0 #568f38,
    0 -2px #568f38,
    0 2px #568f38;
}

.level-sign {
  width: 100px;
  height: 96px;
  border: none;
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  background: transparent;
  cursor: pointer;
}

.sign-plate {
  position: absolute;
  top: 0;
  left: 7px;
  width: 86px;
  height: 56px;
  border-radius: 12px;
  border: 3px solid #f3f8e9;
  background: linear-gradient(180deg, #b7dc77 0%, #8eb95a 72%, #77a04d 100%);
  color: #f8fff0;
  text-shadow: 0 2px 0 rgba(86, 113, 50, 0.78);
  box-shadow: 0 5px 0 #8a6133;
  display: grid;
  place-items: center;
}

.sign-plate::before {
  content: '';
  position: absolute;
  left: 8px;
  right: 8px;
  top: 6px;
  height: 14px;
  border-radius: 999px;
  background: rgba(241, 251, 220, 0.66);
}

.sign-pole {
  position: absolute;
  left: 48px;
  top: 53px;
  width: 10px;
  height: 32px;
  border-radius: 4px;
  background: linear-gradient(180deg, #cca26b 0%, #936737 100%);
  box-shadow: inset 2px 0 0 rgba(255, 227, 176, 0.4);
}

.sign-base {
  position: absolute;
  left: 36px;
  top: 82px;
  width: 34px;
  height: 10px;
  border-radius: 999px;
  background: #7b532b;
}

.level-sign.completed .sign-plate {
  border-color: #ffe6a5;
  background: linear-gradient(180deg, #c8e98c 0%, #9cc966 72%, #80ab4f 100%);
}

.level-sign.locked .sign-plate {
  border-color: #dde3d2;
  background: linear-gradient(180deg, #b8c3ad 0%, #9aa491 72%, #818a79 100%);
  box-shadow: 0 5px 0 #6d5942;
}

.sign-lock {
  position: absolute;
  right: 6px;
  bottom: 4px;
  font-size: 13px;
  line-height: 1;
}

.piece-index {
  position: relative;
  z-index: 1;
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
}

.progress-truck {
  position: absolute;
  transform: translateX(-50%);
  width: 102px;
  height: 52px;
  z-index: 4;
  animation: truck-drive 1.1s linear infinite;
}

.truck-body {
  position: absolute;
  left: 0;
  bottom: 10px;
  width: 64px;
  height: 24px;
  border-radius: 5px;
  background: linear-gradient(180deg, #f26a37 0%, #d14f24 100%);
  box-shadow: inset 0 -4px 0 rgba(0, 0, 0, 0.22);
}

.truck-cab {
  position: absolute;
  left: 54px;
  bottom: 10px;
  width: 32px;
  height: 32px;
  border-radius: 5px 7px 4px 4px;
  background: linear-gradient(180deg, #ffd95f 0%, #ecbc3f 100%);
}

.truck-window {
  position: absolute;
  left: 60px;
  bottom: 26px;
  width: 17px;
  height: 10px;
  border-radius: 3px;
  background: #b7efff;
}

.truck-wheel {
  position: absolute;
  bottom: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #2f2f2f;
  border: 3px solid #595959;
  animation: truck-wheel-spin 0.7s linear infinite;
}

.wheel-front {
  left: 14px;
}

.wheel-back {
  left: 66px;
}

@keyframes truck-drive {

  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }

  50% {
    transform: translateX(-50%) translateY(-2px);
  }
}

@keyframes truck-wheel-spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.piece-name {
  position: absolute;
  left: 50%;
  bottom: 136px;
  transform: translateX(-50%);
  max-width: 144px;
  text-align: center;
  color: #f4faeb;
  background: rgba(82, 126, 49, 0.5);
  border: 1px solid rgba(210, 233, 173, 0.42);
  border-radius: 10px;
  padding: 4px 9px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

@keyframes cloud-float {

  0%,
  100% {
    transform: translateX(0) translateY(0) scale(var(--cloud-scale));
  }

  50% {
    transform: translateX(10px) translateY(-5px) scale(calc(var(--cloud-scale) + 0.03));
  }
}

.ground-line {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 94px;
  height: 10px;
  background: linear-gradient(180deg, #6ece2f 0%, #58b727 100%);
  box-shadow: 0 2px 0 #46951f;
}

.brick-floor {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 94px;
  background:
    repeating-linear-gradient(0deg,
      #a65a2e 0,
      #a65a2e 12px,
      #8f4928 12px,
      #8f4928 16px),
    repeating-linear-gradient(90deg,
      rgba(255, 179, 113, 0.28) 0,
      rgba(255, 179, 113, 0.28) 22px,
      rgba(120, 58, 29, 0.24) 22px,
      rgba(120, 58, 29, 0.24) 24px);
  border-top: 3px solid #d27b42;
}

.empty-map {
  min-height: 300px;
  display: grid;
  place-items: center;
  color: var(--text-sub);
}

.dialog-content {
  display: grid;
  gap: 12px;
}

.dialog-desc {
  margin: 0;
  color: var(--text-sub);
}

.dialog-stars {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-title);
  font-weight: 600;
}

.stars-inline {
  display: inline-flex;
  gap: 4px;
}

.dialog-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.unlock-tip {
  margin: 0;
  color: #6a7f99;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 900px) {
  .pixel-stage {
    padding: 8px;
  }

  .map-world {
    height: 520px;
  }

  .level-sign-wrap {
    width: 132px;
  }

  .level-sign {
    width: 90px;
    height: 86px;
  }

  .progress-truck {
    width: 84px;
    height: 44px;
  }

  .piece-index {
    font-size: 30px;
  }

  .star {
    font-size: 21px;
  }
}

@media (prefers-reduced-motion: reduce) {

  .cloud,
  .progress-truck,
  .truck-wheel {
    animation: none !important;
  }
}

.map-world.reduced-motion .cloud,
.map-world.reduced-motion .progress-truck,
.map-world.reduced-motion .truck-wheel {
  animation: none !important;
}
</style>
