<script setup>
import { computed, ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const trackMap = {
  algo: '算法思维',
  ds: '数据结构',
  contest: '竞赛冲刺',
}

const trackColors = {
  algo: { primary: '#4a90d9', secondary: '#3a7bc8', gradient: 'linear-gradient(135deg, #4a90d9 0%, #67b26f 100%)' },
  ds: { primary: '#722ed1', secondary: '#531dab', gradient: 'linear-gradient(135deg, #722ed1 0%, #eb2f96 100%)' },
  contest: { primary: '#fa8c16', secondary: '#d46b08', gradient: 'linear-gradient(135deg, #fa8c16 0%, #f5222d 100%)' },
}

const rankingRows = computed(() => userStore.getLeaderboard())

const topThree = computed(() => rankingRows.value.slice(0, 3))

const restRanking = computed(() => rankingRows.value.slice(3))

const getTrackLabel = (trackCode) => trackMap[trackCode] || '算法思维'

const getTrackColor = (trackCode) => trackColors[trackCode] || trackColors.algo

const rowClassName = ({ row }) => {
  if (!userStore.userInfo) return ''
  return Number(row.id) === Number(userStore.userInfo.id) ? 'self-row' : ''
}

const getRankStyle = (rank) => {
  const styles = {
    1: { medal: '🥇', label: '冠军', bg: 'linear-gradient(135deg, #ffd700 0%, #ffb347 100%)', shadow: '0 8px 32px rgba(255, 215, 0, 0.4)', height: '280px' },
    2: { medal: '🥈', label: '亚军', bg: 'linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%)', shadow: '0 6px 24px rgba(192, 192, 192, 0.4)', height: '240px' },
    3: { medal: '🥉', label: '季军', bg: 'linear-gradient(135deg, #cd7f32 0%, #b8860b 100%)', shadow: '0 4px 16px rgba(205, 127, 50, 0.4)', height: '200px' },
  }
  return styles[rank] || { medal: '', label: '', bg: '#f5f5f5', shadow: 'none', height: '160px' }
}

const animatedScores = ref({})

onMounted(() => {
  rankingRows.value.forEach((row, index) => {
    setTimeout(() => {
      animatedScores.value[row.id] = row.points
    }, index * 100)
  })
})

const statsData = computed(() => [
  { icon: '👥', label: '参与人数', value: rankingRows.value.length, color: '#4a90d9' },
  { icon: '🔥', label: '总积分', value: rankingRows.value.reduce((sum, r) => sum + r.points, 0), color: '#fa8c16' },
  { icon: '⚡', label: '最高分', value: rankingRows.value[0]?.points || 0, color: '#52c41a' },
  { icon: '🎯', label: '活跃赛道', value: Object.keys(trackMap).length, color: '#722ed1' },
])
</script>

<template>
  <div class="ranking-page">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">积分排行榜</h1>
        </div>
        <p class="page-subtitle">挑战自我，攀登巅峰</p>
      </div>
      <div class="header-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <section class="stats-section">
      <div v-for="(stat, index) in statsData" :key="stat.label" class="stat-card" :style="{ '--delay': index * 0.1 + 's', '--color': stat.color }">
        <div class="stat-icon">{{ stat.icon }}</div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </section>

    <section class="podium-section">
      <div class="podium-container">
        <div v-if="topThree[1]" class="podium-item second" :style="{ '--podium-height': getRankStyle(2).height }">
          <div class="podium-card" :style="{ background: getRankStyle(2).bg, boxShadow: getRankStyle(2).shadow }">
            <div class="crown">👑</div>
            <div class="medal">{{ getRankStyle(2).medal }}</div>
            <div class="avatar-wrapper">
              <div class="avatar">{{ topThree[1].name.slice(0, 1) }}</div>
              <div class="rank-badge">2</div>
            </div>
            <h3 class="user-name">{{ topThree[1].name }}</h3>
            <div class="user-score">
              <span class="score-value">{{ animatedScores[topThree[1].id] || 0 }}</span>
              <span class="score-unit">积分</span>
            </div>
            <div class="user-track" :style="{ background: getTrackColor(topThree[1].targetTrack).gradient }">
              {{ getTrackLabel(topThree[1].targetTrack) }}
            </div>
            <div class="rank-label">{{ getRankStyle(2).label }}</div>
          </div>
        </div>

        <div v-if="topThree[0]" class="podium-item first" :style="{ '--podium-height': getRankStyle(1).height }">
          <div class="podium-card" :style="{ background: getRankStyle(1).bg, boxShadow: getRankStyle(1).shadow }">
            <div class="crown champion">👑</div>
            <div class="medal">{{ getRankStyle(1).medal }}</div>
            <div class="sparkles">
              <span v-for="i in 6" :key="i" class="sparkle" :style="{ '--i': i }">✨</span>
            </div>
            <div class="avatar-wrapper champion">
              <div class="avatar">{{ topThree[0].name.slice(0, 1) }}</div>
              <div class="rank-badge gold">1</div>
            </div>
            <h3 class="user-name">{{ topThree[0].name }}</h3>
            <div class="user-score champion">
              <span class="score-value">{{ animatedScores[topThree[0].id] || 0 }}</span>
              <span class="score-unit">积分</span>
            </div>
            <div class="user-track" :style="{ background: getTrackColor(topThree[0].targetTrack).gradient }">
              {{ getTrackLabel(topThree[0].targetTrack) }}
            </div>
            <div class="rank-label champion">{{ getRankStyle(1).label }}</div>
          </div>
        </div>

        <div v-if="topThree[2]" class="podium-item third" :style="{ '--podium-height': getRankStyle(3).height }">
          <div class="podium-card" :style="{ background: getRankStyle(3).bg, boxShadow: getRankStyle(3).shadow }">
            <div class="crown">👑</div>
            <div class="medal">{{ getRankStyle(3).medal }}</div>
            <div class="avatar-wrapper">
              <div class="avatar">{{ topThree[2].name.slice(0, 1) }}</div>
              <div class="rank-badge">3</div>
            </div>
            <h3 class="user-name">{{ topThree[2].name }}</h3>
            <div class="user-score">
              <span class="score-value">{{ animatedScores[topThree[2].id] || 0 }}</span>
              <span class="score-unit">积分</span>
            </div>
            <div class="user-track" :style="{ background: getTrackColor(topThree[2].targetTrack).gradient }">
              {{ getTrackLabel(topThree[2].targetTrack) }}
            </div>
            <div class="rank-label">{{ getRankStyle(3).label }}</div>
          </div>
        </div>
      </div>
    </section>

    <section class="ranking-list-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-deco">📊</span>
          完整排名
        </h2>
        <span class="section-count">共 {{ rankingRows.length }} 人</span>
      </div>

      <div class="ranking-list">
        <div 
          v-for="(item, index) in rankingRows" 
          :key="item.id" 
          class="ranking-row"
          :class="{ 'is-self': userStore.userInfo && Number(item.id) === Number(userStore.userInfo.id), 'is-top-three': item.rank <= 3 }"
          :style="{ '--delay': index * 0.05 + 's' }"
        >
          <div class="row-rank">
            <span v-if="item.rank <= 3" class="rank-medal">{{ ['🥇', '🥈', '🥉'][item.rank - 1] }}</span>
            <span v-else class="rank-number">{{ item.rank }}</span>
          </div>
          <div class="row-user">
            <div class="user-avatar" :style="{ background: getTrackColor(item.targetTrack).gradient }">
              {{ item.name.slice(0, 1) }}
            </div>
            <div class="user-info">
              <span class="user-name">{{ item.name }}</span>
              <span class="user-track-tag" :style="{ background: getTrackColor(item.targetTrack).gradient }">
                {{ getTrackLabel(item.targetTrack) }}
              </span>
            </div>
          </div>
          <div class="row-score">
            <div class="score-bar-wrapper">
              <div 
                class="score-bar" 
                :style="{ 
                  width: `${Math.min(100, (item.points / (rankingRows[0]?.points || 1)) * 100)}%`,
                  background: getTrackColor(item.targetTrack).gradient 
                }"
              ></div>
            </div>
            <span class="score-text">{{ item.points }} 分</span>
          </div>
          <div class="row-status">
            <span class="status-badge" :class="{ 'top': item.rank <= 3, 'rising': item.rank > 3 }">
              <span class="status-icon">{{ item.rank <= 3 ? '🔥' : '📈' }}</span>
              {{ item.rank <= 3 ? '领跑中' : '上升中' }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <div class="floating-elements">
      <div class="float-item f1">⭐</div>
      <div class="float-item f2">🌟</div>
      <div class="float-item f3">💫</div>
      <div class="float-item f4">✨</div>
    </div>
  </div>
</template>

<style scoped>
.ranking-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 50%, #f8fafc 100%);
}

.page-header {
  position: relative;
  padding: 40px 20px;
  text-align: center;
  margin-bottom: 30px;
}

.header-content {
  position: relative;
  z-index: 2;
}

.title-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.page-title {
  font-size: 42px;
  font-weight: 900;
  letter-spacing: 2px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-sub);
  letter-spacing: 4px;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.deco-circle.c1 {
  width: 200px;
  height: 200px;
  background: #4a90d9;
  top: -50px;
  left: 10%;
  animation: float 6s ease-in-out infinite;
}

.deco-circle.c2 {
  width: 150px;
  height: 150px;
  background: #722ed1;
  top: 20px;
  right: 15%;
  animation: float 8s ease-in-out infinite reverse;
}

.deco-circle.c3 {
  width: 100px;
  height: 100px;
  background: #eb2f96;
  bottom: 0;
  left: 30%;
  animation: float 7s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(10deg); }
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  border-color: var(--color);
}

.stat-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, rgba(74, 144, 217, 0.1) 0%, rgba(114, 46, 209, 0.1) 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: var(--text-title);
}

.stat-label {
  font-size: 13px;
  color: var(--text-sub);
}

.podium-section {
  margin-bottom: 40px;
}

.podium-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 20px;
  padding: 20px;
}

.podium-item {
  flex: 0 0 auto;
  width: 200px;
  animation: podiumRise 0.8s ease forwards;
  animation-delay: var(--delay, 0s);
  opacity: 0;
}

.podium-item.first {
  order: 2;
  width: 240px;
}

.podium-item.second {
  order: 1;
}

.podium-item.third {
  order: 3;
}

@keyframes podiumRise {
  from {
    opacity: 0;
    transform: translateY(50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.podium-card {
  border-radius: 24px;
  padding: 24px 20px;
  text-align: center;
  position: relative;
  overflow: hidden;
  min-height: var(--podium-height, 200px);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.podium-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 50%);
  pointer-events: none;
}

.crown {
  font-size: 28px;
  margin-bottom: 4px;
  animation: crownFloat 3s ease-in-out infinite;
}

.crown.champion {
  font-size: 36px;
  animation: crownFloat 2s ease-in-out infinite, glow 2s ease-in-out infinite;
}

@keyframes crownFloat {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-6px) rotate(5deg); }
}

@keyframes glow {
  0%, 100% { filter: drop-shadow(0 0 8px rgba(255, 215, 0, 0.8)); }
  50% { filter: drop-shadow(0 0 16px rgba(255, 215, 0, 1)); }
}

.medal {
  font-size: 32px;
  margin-bottom: 8px;
}

.sparkles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.sparkle {
  position: absolute;
  font-size: 14px;
  animation: sparkleAnim 2s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.3s);
}

.sparkle:nth-child(1) { top: 10%; left: 10%; }
.sparkle:nth-child(2) { top: 20%; right: 15%; }
.sparkle:nth-child(3) { top: 40%; left: 5%; }
.sparkle:nth-child(4) { top: 60%; right: 10%; }
.sparkle:nth-child(5) { bottom: 30%; left: 15%; }
.sparkle:nth-child(6) { bottom: 20%; right: 20%; }

@keyframes sparkleAnim {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 12px;
}

.avatar-wrapper.champion {
  transform: scale(1.1);
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: #333;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 3px solid rgba(255, 255, 255, 0.8);
}

.rank-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: white;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.rank-badge.gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffb347 100%);
  width: 32px;
  height: 32px;
  font-size: 16px;
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  margin-bottom: 6px;
}

.user-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
}

.user-score.champion {
  transform: scale(1.1);
}

.score-value {
  font-size: 28px;
  font-weight: 900;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.score-unit {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.user-track {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: white;
  font-weight: 500;
  margin-bottom: 8px;
}

.rank-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
  letter-spacing: 2px;
}

.rank-label.champion {
  font-size: 16px;
  letter-spacing: 4px;
}

.ranking-list-section {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-title);
}

.title-deco {
  font-size: 24px;
}

.section-count {
  font-size: 14px;
  color: var(--text-sub);
  background: rgba(74, 144, 217, 0.1);
  padding: 6px 14px;
  border-radius: 20px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ranking-row {
  display: grid;
  grid-template-columns: 60px 1fr 200px 100px;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fafbfc 0%, #ffffff 100%);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  animation: slideIn 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.ranking-row:hover {
  transform: translateX(8px);
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.1);
  border-color: rgba(74, 144, 217, 0.2);
}

.ranking-row.is-self {
  background: linear-gradient(135deg, #edf3fb 0%, #f0f5ff 100%);
  border-color: #4a90d9;
}

.ranking-row.is-top-three {
  background: linear-gradient(135deg, #fffbe6 0%, #fff7cc 100%);
}

.row-rank {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-medal {
  font-size: 28px;
}

.rank-number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #f0f2f5 0%, #e8eaed 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-sub);
}

.row-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-info .user-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-title);
  text-shadow: none;
}

.user-track-tag {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  color: white;
  font-weight: 500;
  width: fit-content;
}

.row-score {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-bar-wrapper {
  flex: 1;
  height: 8px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
  overflow: hidden;
}

.score-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 1s ease;
}

.score-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-title);
  min-width: 60px;
  text-align: right;
}

.row-status {
  display: flex;
  justify-content: flex-end;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.top {
  background: linear-gradient(135deg, rgba(250, 140, 22, 0.1) 0%, rgba(245, 34, 45, 0.1) 100%);
  color: #fa8c16;
}

.status-badge.rising {
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.1) 0%, rgba(74, 144, 217, 0.1) 100%);
  color: #52c41a;
}

.status-icon {
  font-size: 14px;
}

.floating-elements {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.float-item {
  position: absolute;
  font-size: 24px;
  opacity: 0.15;
  animation: floatAround 20s linear infinite;
}

.float-item.f1 { top: 10%; left: 5%; animation-delay: 0s; }
.float-item.f2 { top: 30%; right: 8%; animation-delay: -5s; }
.float-item.f3 { bottom: 20%; left: 10%; animation-delay: -10s; }
.float-item.f4 { bottom: 40%; right: 5%; animation-delay: -15s; }

@keyframes floatAround {
  0% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(30px, -30px) rotate(90deg); }
  50% { transform: translate(0, -60px) rotate(180deg); }
  75% { transform: translate(-30px, -30px) rotate(270deg); }
  100% { transform: translate(0, 0) rotate(360deg); }
}

@media (max-width: 900px) {
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .podium-container {
    flex-direction: column;
    align-items: center;
  }

  .podium-item {
    width: 100%;
    max-width: 280px;
  }

  .podium-item.first {
    order: 1;
  }

  .podium-item.second {
    order: 2;
  }

  .podium-item.third {
    order: 3;
  }

  .ranking-row {
    grid-template-columns: 50px 1fr 80px;
  }

  .row-score {
    display: none;
  }
}

@media (max-width: 600px) {
  .page-title {
    font-size: 28px;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .ranking-row {
    grid-template-columns: 40px 1fr 60px;
    padding: 12px 14px;
  }

  .row-status {
    display: none;
  }
}
</style>
