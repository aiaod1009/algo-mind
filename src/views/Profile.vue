<script setup>
import { computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useLevelStore } from '../stores/level'

const userStore = useUserStore()
const levelStore = useLevelStore()

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

const form = reactive({
  name: userStore.userInfo?.name || '',
  bio: userStore.userInfo?.bio || '',
  gender: userStore.userInfo?.gender || 'unknown',
  avatar: userStore.userInfo?.avatar || avatarPresets[0],
  targetTrack: userStore.userInfo?.targetTrack || userStore.selectedTrack,
  weeklyGoal: Number(userStore.userInfo?.weeklyGoal || 10),
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

if (!levelStore.levels.length) {
  levelStore.fetchLevels()
}
</script>

<template>
  <div class="page-container profile-page">
    <h2 class="section-title">个人主页</h2>

    <section class="summary-grid">
      <el-card class="surface-card stat-card" shadow="never">
        <div class="label">当前积分</div>
        <div class="value">{{ userStore.points }}</div>
      </el-card>
      <el-card class="surface-card stat-card" shadow="never">
        <div class="label">当前称号</div>
        <div class="value">{{ levelTitle }}</div>
      </el-card>
      <el-card class="surface-card stat-card" shadow="never">
        <div class="label">关卡进度</div>
        <div class="value">{{ solvedLevels }} / {{ totalLevels }}</div>
      </el-card>
    </section>

    <el-card class="surface-card form-card" shadow="never">
      <h3 class="card-title">学习档案</h3>
      <el-form label-width="110px">
        <el-form-item label="头像">
          <div class="avatar-area">
            <el-avatar :size="64" :src="form.avatar">{{ form.name.slice(0, 1) || '你' }}</el-avatar>
            <el-upload class="avatar-uploader" :show-file-list="false" :auto-upload="false"
              :on-change="handleAvatarUpload">
              <el-button>上传头像</el-button>
            </el-upload>
            <div class="preset-row">
              <button v-for="item in avatarPresets" :key="item" type="button" class="preset-btn"
                :class="{ active: form.avatar === item }" @click="choosePresetAvatar(item)">
                <el-avatar :size="34" :src="item" />
              </button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.name" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio v-for="item in genderOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="个性签名">
          <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="目标赛道">
          <el-select v-model="form.targetTrack" style="width: 280px">
            <el-option v-for="option in trackOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="每周目标题量">
          <el-input-number v-model="form.weeklyGoal" :min="3" :max="40" />
        </el-form-item>
      </el-form>
      <div class="submit-row">
        <el-button type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.profile-page {
  padding-bottom: 28px;
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.stat-card {
  border: 1px solid var(--line-soft);
}

.label {
  color: var(--text-sub);
}

.value {
  margin-top: 8px;
  color: var(--text-title);
  font-size: 26px;
  font-weight: 700;
}

.form-card {
  padding: 20px;
}

.card-title {
  margin-bottom: 12px;
  color: var(--text-title);
}

.submit-row {
  display: flex;
  justify-content: flex-end;
}

.avatar-area {
  display: grid;
  gap: 10px;
}

.preset-row {
  display: flex;
  gap: 8px;
}

.preset-btn {
  border: 1px solid var(--line-card);
  background: #fff;
  border-radius: 20px;
  padding: 2px;
  cursor: pointer;
}

.preset-btn.active {
  border-color: var(--brand-blue);
  box-shadow: 0 0 0 3px rgba(74, 111, 157, 0.2);
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
