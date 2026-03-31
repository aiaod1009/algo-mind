<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLevelStore } from '../stores/level'
import AvatarPendant from '../components/AvatarPendant.vue'

const userStore = useUserStore()
const levelStore = useLevelStore()

const showAccessoryShop = ref(false)
const showBackgroundShop = ref(false)
const activeAccessoryTab = ref('pendant')

const trackOptions = [
  { label: '算法思维赛道', value: 'algo' },
  { label: '数据结构赛道', value: 'ds' },
  { label: '竞赛冲刺赛道', value: 'contest' },
]

const genderOptions = [
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '不便透露', value: 'unknown' },
]

const avatarPresets = [
  'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Byte',
  'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Graph',
  'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Queue',
  'https://api.dicebear.com/9.x/fun-emoji/svg?seed=Tree',
]

const accessories = ref({
  pendants: [
    { id: 'star', name: '闪烁星星', price: 50, owned: true, icon: '⭐', animation: 'twinkle' },
    { id: 'crown', name: '皇冠', price: 100, owned: false, icon: '👑', animation: 'float' },
    { id: 'flower', name: '樱花飘落', price: 80, owned: false, icon: '🌸', animation: 'petal' },
    { id: 'lightning', name: '闪电', price: 60, owned: true, icon: '⚡', animation: 'flash' },
    { id: 'fire', name: '火焰', price: 70, owned: false, icon: '🔥', animation: 'burn' },
    { id: 'diamond', name: '钻石', price: 150, owned: false, icon: '💎', animation: 'sparkle' },
    { id: 'music', name: '音符', price: 40, owned: true, icon: '🎵', animation: 'bounce' },
    { id: 'heart', name: '爱心', price: 30, owned: true, icon: '❤️', animation: 'pulse' },
    { id: 'comet', name: '彗星', price: 180, owned: false, icon: '☄️', animation: 'orbit' },
    { id: 'halo', name: '光环', price: 200, owned: false, icon: '😇', animation: 'rotate' },
  ],
  frames: [
    { id: 'gold', name: '金色边框', price: 120, owned: false, color: '#fbbf24', style: 'solid' },
    { id: 'rainbow', name: '彩虹边框', price: 200, owned: false, color: 'rainbow', style: 'gradient' },
    { id: 'neon', name: '霓虹边框', price: 180, owned: true, color: '#4a6f9d', style: 'glow' },
    { id: 'pixel', name: '像素边框', price: 90, owned: false, color: '#6672cb', style: 'pixel' },
    { id: 'crystal', name: '水晶边框', price: 250, owned: false, color: '#67e8f9', style: 'crystal' },
    { id: 'flame', name: '火焰边框', price: 280, owned: false, color: '#f97316', style: 'flame' },
  ],
  bubbles: [
    { id: 'ring', name: '光环环绕', price: 100, owned: true, type: 'ring' },
    { id: 'stars', name: '星星环绕', price: 120, owned: false, type: 'stars' },
    { id: 'particles', name: '粒子环绕', price: 150, owned: false, type: 'particles' },
    { id: 'orbit', name: '轨道环绕', price: 180, owned: false, type: 'orbit' },
    { id: 'petals', name: '花瓣环绕', price: 200, owned: false, type: 'petals' },
  ],
})

const backgrounds = ref({
  page: [
    { id: 'page1', name: '纯净白', price: 0, owned: true, type: 'static', value: '#f8fafc' },
    { id: 'page2', name: '星空', price: 200, owned: false, type: 'animated', value: 'starry' },
    { id: 'page3', name: '流动渐变', price: 180, owned: false, type: 'animated', value: 'flow' },
    { id: 'page4', name: '几何图案', price: 150, owned: false, type: 'pattern', value: 'geometric' },
    { id: 'page5', name: '气泡浮动', price: 220, owned: false, type: 'animated', value: 'bubbles' },
  ],
})

const form = reactive({
  name: userStore.userInfo?.name || '',
  bio: userStore.userInfo?.bio || '',
  gender: userStore.userInfo?.gender || 'unknown',
  avatar: userStore.userInfo?.avatar || avatarPresets[0],
  targetTrack: userStore.userInfo?.targetTrack || userStore.selectedTrack,
  weeklyGoal: Number(userStore.userInfo?.weeklyGoal || 10),
  equippedPendant: userStore.userInfo?.equippedPendant || 'star',
  equippedFrame: userStore.userInfo?.equippedFrame || 'neon',
  equippedBubble: userStore.userInfo?.equippedBubble || 'ring',
  pageBg: userStore.userInfo?.pageBg || 'page1',
})

const solvedLevels = computed(() => {
  if (!levelStore.levels.length) return 0
  return levelStore.levels.filter((item) => item.isUnlocked).length
})

const totalLevels = computed(() => levelStore.levels.length)

const levelTitle = computed(() => {
  const score = Number(userStore.points || 0)
  if (score >= 220) return '冲榜王者'
  if (score >= 140) return '稳定输出'
  if (score >= 80) return '进阶练习者'
  return '新手起步'
})

const currentPendant = computed(() => {
  return accessories.value.pendants.find(p => p.id === form.equippedPendant)
})

const currentFrame = computed(() => {
  return accessories.value.frames.find(f => f.id === form.equippedFrame)
})

const currentPageBg = computed(() => {
  return backgrounds.value.page.find(b => b.id === form.pageBg)
})

const buyAccessory = async (item, type) => {
  if (item.owned) {
    if (type === 'pendant') form.equippedPendant = item.id
    else if (type === 'frame') form.equippedFrame = item.id
    else if (type === 'bubble') form.equippedBubble = item.id
    ElMessage.success(`已装备 ${item.name}`)
    return
  }
  
  if (userStore.points < item.price) {
    ElMessage.warning('积分不足，继续加油哦！')
    return
  }
  
  item.owned = true
  userStore.points -= item.price
  
  if (type === 'pendant') form.equippedPendant = item.id
  else if (type === 'frame') form.equippedFrame = item.id
  else if (type === 'bubble') form.equippedBubble = item.id
  
  ElMessage.success(`购买成功！已装备 ${item.name}`)
}

const buyBackground = async (item) => {
  if (item.owned) {
    form.pageBg = item.id
    ElMessage.success(`已应用 ${item.name}`)
    return
  }
  
  if (userStore.points < item.price) {
    ElMessage.warning('积分不足，继续加油哦！')
    return
  }
  
  item.owned = true
  userStore.points -= item.price
  form.pageBg = item.id
  
  ElMessage.success(`购买成功！已应用 ${item.name}`)
}

const handleSave = () => {
  if (!form.name.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  userStore.updateProfile({
    name: form.name.trim(),
    bio: form.bio.trim(),
    gender: form.gender,
    avatar: form.avatar,
    targetTrack: form.targetTrack,
    weeklyGoal: Number(form.weeklyGoal || 10),
    equippedPendant: form.equippedPendant,
    equippedFrame: form.equippedFrame,
    equippedBubble: form.equippedBubble,
    pageBg: form.pageBg,
  })
  userStore.setTrack(form.targetTrack)
  ElMessage.success('个人信息已更新')
}

const choosePresetAvatar = (url) => {
  form.avatar = url
}

const handleAvatarUpload = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return false
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片文件')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 2MB')
    return false
  }

  const reader = new FileReader()
  reader.onload = () => {
    form.avatar = String(reader.result || '')
  }
  reader.readAsDataURL(file)
  return false
}

const fetchAccessories = async () => {
  return accessories.value
}

const fetchBackgrounds = async () => {
  return backgrounds.value
}

const updateAccessories = (data) => {
  accessories.value = { ...accessories.value, ...data }
}

const updateBackgrounds = (data) => {
  backgrounds.value = { ...backgrounds.value, ...data }
}

defineExpose({
  fetchAccessories,
  fetchBackgrounds,
  updateAccessories,
  updateBackgrounds,
})

if (!levelStore.levels.length) {
  levelStore.fetchLevels()
}
</script>

<template>
  <div class="profile-page" :class="`page-bg-${form.pageBg}`">
    <div v-if="form.pageBg === 'page2'" class="starry-bg"></div>
    <div v-if="form.pageBg === 'page3'" class="flow-bg"></div>
    <div v-if="form.pageBg === 'page5'" class="bubbles-bg">
      <span v-for="i in 15" :key="i" class="bubble" :style="{ '--delay': i * 0.3 + 's', '--size': 10 + Math.random() * 30 + 'px' }"></span>
    </div>

    <div class="page-header">
      <h2 class="section-title">个人主页</h2>
      <div class="header-actions">
        <button class="shop-btn" @click="showAccessoryShop = true">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <line x1="7" y1="7" x2="7.01" y2="7"/>
          </svg>
          挂件商城
        </button>
        <button class="shop-btn" @click="showBackgroundShop = true">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
          背景商城
        </button>
      </div>
    </div>

    <section class="summary-grid">
      <div class="stat-card">
        <div class="stat-icon points">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ userStore.points }}</div>
          <div class="stat-label">当前积分</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon title">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="8" r="7"/>
            <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ levelTitle }}</div>
          <div class="stat-label">当前称号</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon progress">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ solvedLevels }} / {{ totalLevels }}</div>
          <div class="stat-label">关卡进度</div>
        </div>
      </div>
    </section>

    <section class="profile-card">
      <div class="card-header">
        <h3>学习档案</h3>
      </div>

      <div class="avatar-section">
        <div class="avatar-preview">
          <AvatarPendant
            :avatar="form.avatar"
            :name="form.name"
            :pendant="form.equippedPendant"
            :frame="form.equippedFrame"
            :bubble="form.equippedBubble"
            :background="form.avatarBg"
            :size="100"
          />
        </div>

        <div class="avatar-controls">
          <div class="control-group">
            <span class="control-label">头像</span>
            <el-upload class="avatar-uploader" :show-file-list="false" :auto-upload="false" :on-change="handleAvatarUpload">
              <button class="upload-btn">上传头像</button>
            </el-upload>
          </div>
          <div class="control-group">
            <span class="control-label">预设头像</span>
            <div class="preset-row">
              <button v-for="item in avatarPresets" :key="item" type="button" class="preset-btn" :class="{ active: form.avatar === item }" @click="choosePresetAvatar(item)">
                <el-avatar :size="32" :src="item" />
              </button>
            </div>
          </div>
          <div class="control-group">
            <span class="control-label">当前挂件</span>
            <div class="equipped-items">
              <span class="equipped-tag">{{ currentPendant?.name || '无' }}</span>
              <span class="equipped-tag">{{ currentFrame?.name || '无' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="form-row">
          <label class="form-label">昵称</label>
          <input v-model="form.name" type="text" class="form-input" maxlength="20" :placeholder="'请输入昵称'" />
        </div>
        <div class="form-row">
          <label class="form-label">性别</label>
          <div class="radio-group">
            <label v-for="item in genderOptions" :key="item.value" class="radio-item" :class="{ active: form.gender === item.value }">
              <input v-model="form.gender" type="radio" :value="item.value" />
              <span>{{ item.label }}</span>
            </label>
          </div>
        </div>
        <div class="form-row">
          <label class="form-label">个性签名</label>
          <textarea v-model="form.bio" class="form-textarea" maxlength="60" :placeholder="'写点什么介绍自己吧'" ></textarea>
        </div>
        <div class="form-row">
          <label class="form-label">目标赛道</label>
          <select v-model="form.targetTrack" class="form-select">
            <option v-for="option in trackOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
        <div class="form-row">
          <label class="form-label">每周目标题量</label>
          <div class="number-input">
            <button class="num-btn" @click="form.weeklyGoal = Math.max(3, form.weeklyGoal - 1)">-</button>
            <input v-model.number="form.weeklyGoal" type="number" class="num-value" min="3" max="40" />
            <button class="num-btn" @click="form.weeklyGoal = Math.min(40, form.weeklyGoal + 1)">+</button>
          </div>
        </div>
      </div>

      <div class="submit-row">
        <button class="submit-btn" @click="handleSave">保存设置</button>
      </div>
    </section>

    <Transition name="modal">
      <div v-if="showAccessoryShop" class="shop-modal" @click.self="showAccessoryShop = false">
        <div class="shop-container">
          <button class="close-btn" @click="showAccessoryShop = false">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          <div class="shop-header">
            <h3>挂件商城</h3>
            <span class="points-display">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              {{ userStore.points }} 积分
            </span>
          </div>
          <div class="shop-tabs">
            <button :class="['tab', { active: activeAccessoryTab === 'pendant' }]" @click="activeAccessoryTab = 'pendant'">挂件</button>
            <button :class="['tab', { active: activeAccessoryTab === 'frame' }]" @click="activeAccessoryTab = 'frame'">边框</button>
            <button :class="['tab', { active: activeAccessoryTab === 'bubble' }]" @click="activeAccessoryTab = 'bubble'">环绕</button>
          </div>
          <div class="shop-content">
            <div v-if="activeAccessoryTab === 'pendant'" class="items-grid">
              <div v-for="item in accessories.pendants" :key="item.id" class="shop-item" :class="{ owned: item.owned, equipped: form.equippedPendant === item.id }">
                <div class="item-preview">
                  <span class="item-icon" :class="item.animation">{{ item.icon }}</span>
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-price">
                    <template v-if="item.owned">
                      <span v-if="form.equippedPendant === item.id" class="equipped-label">使用中</span>
                      <span v-else class="owned-label">已拥有</span>
                    </template>
                    <template v-else>
                      <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                      {{ item.price }}
                    </template>
                  </span>
                </div>
                <button class="buy-btn" :class="{ owned: item.owned }" @click="buyAccessory(item, 'pendant')">
                  {{ item.owned ? (form.equippedPendant === item.id ? '使用中' : '装备') : '购买' }}
                </button>
              </div>
            </div>
            <div v-if="activeAccessoryTab === 'frame'" class="items-grid">
              <div v-for="item in accessories.frames" :key="item.id" class="shop-item" :class="{ owned: item.owned, equipped: form.equippedFrame === item.id }">
                <div class="item-preview">
                  <div class="frame-preview" :class="item.style" :style="{ borderColor: item.color }"></div>
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-price">
                    <template v-if="item.owned">
                      <span v-if="form.equippedFrame === item.id" class="equipped-label">使用中</span>
                      <span v-else class="owned-label">已拥有</span>
                    </template>
                    <template v-else>
                      <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                      {{ item.price }}
                    </template>
                  </span>
                </div>
                <button class="buy-btn" :class="{ owned: item.owned }" @click="buyAccessory(item, 'frame')">
                  {{ item.owned ? (form.equippedFrame === item.id ? '使用中' : '装备') : '购买' }}
                </button>
              </div>
            </div>
            <div v-if="activeAccessoryTab === 'bubble'" class="items-grid">
              <div v-for="item in accessories.bubbles" :key="item.id" class="shop-item" :class="{ owned: item.owned, equipped: form.equippedBubble === item.id }">
                <div class="item-preview">
                  <div class="bubble-preview" :class="item.type"></div>
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-price">
                    <template v-if="item.owned">
                      <span v-if="form.equippedBubble === item.id" class="equipped-label">使用中</span>
                      <span v-else class="owned-label">已拥有</span>
                    </template>
                    <template v-else>
                      <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                      {{ item.price }}
                    </template>
                  </span>
                </div>
                <button class="buy-btn" :class="{ owned: item.owned }" @click="buyAccessory(item, 'bubble')">
                  {{ item.owned ? (form.equippedBubble === item.id ? '使用中' : '装备') : '购买' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="modal">
      <div v-if="showBackgroundShop" class="shop-modal" @click.self="showBackgroundShop = false">
        <div class="shop-container">
          <button class="close-btn" @click="showBackgroundShop = false">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          <div class="shop-header">
            <h3>背景商城</h3>
            <span class="points-display">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              {{ userStore.points }} 积分
            </span>
          </div>
          <div class="shop-content">
            <div class="items-grid bg-grid">
              <div v-for="item in backgrounds.page" :key="item.id" class="shop-item bg-item" :class="{ owned: item.owned, equipped: form.pageBg === item.id }">
                <div class="bg-preview" :style="{ background: item.type === 'static' ? item.value : '#f8fafc' }">
                  <div v-if="item.type === 'animated'" class="animated-preview" :class="item.value"></div>
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-price">
                    <template v-if="item.owned">
                      <span v-if="form.pageBg === item.id" class="equipped-label">使用中</span>
                      <span v-else class="owned-label">已拥有</span>
                    </template>
                    <template v-else>
                      <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                      </svg>
                      {{ item.price }}
                    </template>
                  </span>
                </div>
                <button class="buy-btn" :class="{ owned: item.owned }" @click="buyBackground(item)">
                  {{ item.owned ? (form.pageBg === item.id ? '使用中' : '应用') : '购买' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;600;700&display=swap');

.profile-page {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  padding: 24px;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.starry-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  z-index: -1;
}

.starry-bg::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: 
    radial-gradient(2px 2px at 20px 30px, white, transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.8), transparent),
    radial-gradient(1px 1px at 90px 40px, white, transparent),
    radial-gradient(2px 2px at 130px 80px, rgba(255,255,255,0.6), transparent),
    radial-gradient(1px 1px at 160px 120px, white, transparent);
  background-repeat: repeat;
  background-size: 200px 150px;
  animation: twinkleBg 4s ease-in-out infinite;
}

@keyframes twinkleBg {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.flow-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientFlow 15s ease infinite;
  z-index: -1;
}

@keyframes gradientFlow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.bubbles-bg {
  position: fixed;
  inset: 0;
  overflow: hidden;
  z-index: -1;
}

.bubbles-bg .bubble {
  position: absolute;
  bottom: -50px;
  left: calc(var(--delay) * 10);
  width: var(--size);
  height: var(--size);
  background: rgba(74, 111, 157, 0.15);
  border-radius: 50%;
  animation: bubbleRise 8s ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes bubbleRise {
  0% { transform: translateY(0) scale(1); opacity: 0.5; }
  50% { opacity: 0.8; }
  100% { transform: translateY(-100vh) scale(0.5); opacity: 0; }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.shop-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #4a6f9d;
  cursor: pointer;
  transition: all 0.2s ease;
}

.shop-btn:hover {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-color: transparent;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 111, 157, 0.3);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(74, 111, 157, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.points {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #f59e0b;
}

.stat-icon.title {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #4a6f9d;
}

.stat-icon.progress {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #10b981;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
}

.profile-card {
  background: white;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  position: relative;
  z-index: 1;
}

.card-header {
  margin-bottom: 24px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.avatar-section {
  display: flex;
  gap: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 24px;
}

.avatar-preview {
  flex-shrink: 0;
}

.avatar-container {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
}

.ring-bubble {
  position: absolute;
  inset: -10px;
  border: 3px solid rgba(74, 111, 157, 0.3);
  border-radius: 50%;
  animation: ringPulse 2s ease-in-out infinite;
}

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.6; }
}

.avatar-frame {
  padding: 4px;
  border-radius: 50%;
}

.avatar-frame.solid {
  border: 3px solid;
}

.avatar-frame.gradient {
  border: 3px solid transparent;
  background: linear-gradient(white, white) padding-box, linear-gradient(45deg, #f59e0b, #ef4444, #8b5cf6, #3b82f6) border-box;
  border-radius: 50%;
}

.avatar-frame.glow {
  border: 2px solid #4a6f9d;
  box-shadow: 0 0 20px rgba(74, 111, 157, 0.5), inset 0 0 20px rgba(74, 111, 157, 0.1);
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { box-shadow: 0 0 20px rgba(74, 111, 157, 0.5), inset 0 0 20px rgba(74, 111, 157, 0.1); }
  50% { box-shadow: 0 0 30px rgba(74, 111, 157, 0.7), inset 0 0 25px rgba(74, 111, 157, 0.15); }
}

.avatar-frame.pixel {
  border: 4px solid;
  border-image: repeating-linear-gradient(90deg, #6672cb 0, #6672cb 4px, transparent 4px, transparent 8px) 4;
  border-radius: 0;
}

.pendant {
  position: absolute;
  top: -5px;
  right: -5px;
  font-size: 28px;
  z-index: 10;
}

.pendant.twinkle {
  animation: twinkle 1.5s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

.pendant.float {
  animation: float 2s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.pendant.petal {
  animation: petalFall 3s ease-in-out infinite;
}

@keyframes petalFall {
  0% { transform: rotate(0deg) translateY(0); }
  50% { transform: rotate(180deg) translateY(5px); }
  100% { transform: rotate(360deg) translateY(0); }
}

.pendant.flash {
  animation: flash 0.8s ease-in-out infinite;
}

@keyframes flash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.pendant.burn {
  animation: burn 0.5s ease-in-out infinite alternate;
}

@keyframes burn {
  0% { transform: scale(1) rotate(-5deg); }
  100% { transform: scale(1.1) rotate(5deg); }
}

.pendant.sparkle {
  animation: sparkle 1s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.5); }
}

.pendant.bounce {
  animation: bounce 0.6s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.pendant.pulse {
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

.avatar-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.control-label {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.upload-btn {
  padding: 10px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
  width: fit-content;
}

.upload-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.preset-row {
  display: flex;
  gap: 8px;
}

.preset-btn {
  width: 40px;
  height: 40px;
  border: 2px solid #e2e8f0;
  background: white;
  border-radius: 50%;
  padding: 2px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.preset-btn:hover {
  border-color: #4a6f9d;
}

.preset-btn.active {
  border-color: #4a6f9d;
  box-shadow: 0 0 0 3px rgba(74, 111, 157, 0.2);
}

.equipped-items {
  display: flex;
  gap: 8px;
}

.equipped-tag {
  padding: 6px 12px;
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border-radius: 8px;
  font-size: 12px;
  color: #4a6f9d;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
}

.form-input {
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #1e293b;
  outline: none;
  transition: all 0.2s ease;
}

.form-input:focus {
  border-color: #4a6f9d;
  background: white;
  box-shadow: 0 0 0 3px rgba(74, 111, 157, 0.1);
}

.form-textarea {
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #1e293b;
  outline: none;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  transition: all 0.2s ease;
}

.form-textarea:focus {
  border-color: #4a6f9d;
  background: white;
  box-shadow: 0 0 0 3px rgba(74, 111, 157, 0.1);
}

.form-select {
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  color: #1e293b;
  outline: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-select:focus {
  border-color: #4a6f9d;
  background: white;
}

.radio-group {
  display: flex;
  gap: 12px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.radio-item input {
  display: none;
}

.radio-item:hover {
  background: #f1f5f9;
}

.radio-item.active {
  background: linear-gradient(135deg, #dbeafe 0%, #ede9fe 100%);
  border-color: #4a6f9d;
}

.number-input {
  display: flex;
  align-items: center;
  gap: 12px;
}

.num-btn {
  width: 36px;
  height: 36px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 18px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.num-btn:hover {
  background: #4a6f9d;
  border-color: #4a6f9d;
  color: white;
}

.num-value {
  width: 60px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  background: transparent;
  border: none;
}

.submit-row {
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 111, 157, 0.4);
}

.shop-modal {
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

.shop-container {
  position: relative;
  background: white;
  border-radius: 24px;
  width: 100%;
  max-width: 700px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: #f1f5f9;
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.shop-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.points-display {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #b45309;
}

.points-display svg {
  color: #f59e0b;
}

.shop-tabs {
  display: flex;
  gap: 8px;
  padding: 16px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.tab {
  padding: 10px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab:hover {
  background: #f1f5f9;
}

.tab.active {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-color: transparent;
  color: white;
}

.shop-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.bg-grid {
  grid-template-columns: repeat(3, 1fr);
}

.shop-item {
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  transition: all 0.2s ease;
}

.shop-item:hover {
  border-color: #cbd5e1;
  transform: translateY(-2px);
}

.shop-item.owned {
  border-color: #4a6f9d;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.shop-item.equipped {
  border-color: #6672cb;
  box-shadow: 0 0 0 3px rgba(102, 114, 203, 0.2);
}

.item-preview {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.item-icon {
  font-size: 36px;
}

.item-icon.twinkle { animation: twinkle 1.5s ease-in-out infinite; }
.item-icon.float { animation: float 2s ease-in-out infinite; }
.item-icon.flash { animation: flash 0.8s ease-in-out infinite; }
.item-icon.burn { animation: burn 0.5s ease-in-out infinite alternate; }
.item-icon.bounce { animation: bounce 0.6s ease-in-out infinite; }
.item-icon.pulse { animation: pulse 1s ease-in-out infinite; }

.frame-preview {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 4px solid;
}

.frame-preview.solid {
  border-color: #fbbf24;
}

.frame-preview.gradient {
  border: 4px solid transparent;
  background: linear-gradient(white, white) padding-box, linear-gradient(45deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box;
  border-radius: 50%;
  animation: gradientShift 3s linear infinite;
}

@keyframes gradientShift {
  0% { background: linear-gradient(white, white) padding-box, linear-gradient(0deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  25% { background: linear-gradient(white, white) padding-box, linear-gradient(90deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  50% { background: linear-gradient(white, white) padding-box, linear-gradient(180deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  75% { background: linear-gradient(white, white) padding-box, linear-gradient(270deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  100% { background: linear-gradient(white, white) padding-box, linear-gradient(360deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
}

.frame-preview.glow {
  border-color: #4a6f9d;
  box-shadow: 0 0 15px rgba(74, 111, 157, 0.5);
  animation: glowPulse 2s ease-in-out infinite;
}

.frame-preview.pixel {
  border-radius: 0;
  border-color: #6672cb;
}

.frame-preview.crystal {
  border-color: rgba(103, 232, 249, 0.5);
  background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(103, 232, 249, 0.1) 50%, rgba(255,255,255,0.3) 100%);
  box-shadow: 0 0 15px rgba(103, 232, 249, 0.3);
  animation: crystalShimmer 3s ease-in-out infinite;
}

@keyframes crystalShimmer {
  0%, 100% { box-shadow: 0 0 15px rgba(103, 232, 249, 0.3); }
  50% { box-shadow: 0 0 25px rgba(103, 232, 249, 0.5); }
}

.frame-preview.flame {
  border: 4px solid transparent;
  background: linear-gradient(white, white) padding-box, linear-gradient(180deg, #fbbf24 0%, #f97316 50%, #ef4444 100%) border-box;
  box-shadow: 0 0 15px rgba(249, 115, 22, 0.4);
  animation: flameFlicker 0.5s ease-in-out infinite alternate;
}

@keyframes flameFlicker {
  0% { box-shadow: 0 0 15px rgba(249, 115, 22, 0.4), 0 -5px 15px rgba(251, 191, 36, 0.3); }
  100% { box-shadow: 0 0 20px rgba(249, 115, 22, 0.6), 0 -8px 20px rgba(251, 191, 36, 0.4); }
}

.bubble-preview {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 3px solid rgba(74, 111, 157, 0.3);
  position: relative;
}

.bubble-preview.ring {
  animation: ringPulse 2s ease-in-out infinite;
}

.bubble-preview.stars {
  border: none;
  background: transparent;
}

.bubble-preview.stars::before {
  content: '✦✦✦';
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #fbbf24;
  animation: starSpin 4s linear infinite;
}

@keyframes starSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.bubble-preview.particles {
  border: none;
  background: radial-gradient(circle, #8b5cf6 2px, transparent 2px);
  background-size: 8px 8px;
  animation: particleGlow 1.5s ease-in-out infinite;
}

@keyframes particleGlow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.bubble-preview.orbit {
  border: 2px dashed rgba(74, 111, 157, 0.4);
  animation: orbitRotate 4s linear infinite;
}

.bubble-preview.orbit::before {
  content: '';
  position: absolute;
  width: 8px;
  height: 8px;
  background: #4a6f9d;
  border-radius: 50%;
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
}

.bubble-preview.petals {
  border: none;
  background: transparent;
}

.bubble-preview.petals::before {
  content: '❀';
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #f472b6;
  animation: petalRotate 5s linear infinite;
}

@keyframes petalRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.bg-item .item-preview {
  height: 50px;
}

.bg-preview {
  width: 100%;
  height: 50px;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.animated-preview.particles {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}

.animated-preview.particles::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(2px 2px at 10px 10px, white, transparent);
  background-size: 20px 20px;
  animation: twinkleBg 2s ease-in-out infinite;
}

.animated-preview.aurora {
  background: linear-gradient(-45deg, #12c2e9, #c471ed, #f64f59);
  background-size: 200% 200%;
  animation: gradientFlow 5s ease infinite;
}

.animated-preview.starry {
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 100%);
}

.animated-preview.starry::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: radial-gradient(1px 1px at 5px 5px, white, transparent);
  background-size: 15px 15px;
}

.animated-preview.flow {
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 200% 200%;
  animation: gradientFlow 3s ease infinite;
}

.animated-preview.bubbles {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.item-price {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.item-price svg {
  color: #f59e0b;
}

.owned-label {
  color: #10b981;
}

.equipped-label {
  color: #6672cb;
  font-weight: 500;
}

.buy-btn {
  padding: 10px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.buy-btn:hover {
  background: #4a6f9d;
  border-color: #4a6f9d;
  color: white;
}

.buy-btn.owned {
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-color: transparent;
  color: white;
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .shop-container,
.modal-leave-to .shop-container {
  transform: scale(0.95);
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .avatar-section {
    flex-direction: column;
    align-items: center;
  }

  .avatar-controls {
    width: 100%;
  }

  .items-grid {
    grid-template-columns: 1fr;
  }

  .bg-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
