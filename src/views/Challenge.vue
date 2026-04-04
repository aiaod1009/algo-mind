<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useLevelStore } from '../stores/level'
import { useErrorStore } from '../stores/error'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const levelStore = useLevelStore()
const errorStore = useErrorStore()
const userStore = useUserStore()

const answer = ref('')
const loading = ref(false)

const currentLevelId = computed(() => Number(route.params.id))
const currentLevel = computed(() => levelStore.findLevelById(currentLevelId.value))
const typeTagMap = {
  single: '单选题',
  multi: '多选题',
  judge: '判断题',
  fill: '填空题',
  code: '代码题',
}

const resetAnswer = () => {
  if (!currentLevel.value) {
    answer.value = ''
    return
  }
  if (currentLevel.value.type === 'multi') {
    answer.value = []
    return
  }
  answer.value = ''
}

const loadLevel = async () => {
  if (!levelStore.levels.length) {
    await levelStore.fetchLevels()
  }
  if (!currentLevel.value) {
    ElMessage.error('关卡不存在')
    router.push('/levels')
    return
  }
  if (!currentLevel.value.isUnlocked) {
    ElMessage.warning('该关卡尚未解锁')
    router.push('/levels')
    return
  }
  resetAnswer()
}

const validateAnswer = () => {
  if (!currentLevel.value) return false
  if (currentLevel.value.type === 'multi') {
    return Array.isArray(answer.value) && answer.value.length > 0
  }
  return Boolean(String(answer.value || '').trim())
}

const handleSubmit = async () => {
  if (!validateAnswer()) {
    ElMessage.warning('请先作答')
    return
  }

  loading.value = true
  try {
<<<<<<< Updated upstream
    const result = await levelStore.submitAnswer(currentLevelId.value, answer.value)
    if (!result) {
      ElMessage.error('提交失败，请稍后重试')
      return
    }
=======
    const timeMs = Date.now() - startTimestamp.value
    const result = await levelStore.submitAnswer(currentLevelId.value, answer.value, {
      attempts: attemptsInRun.value,
      timeMs,
      language: language.value,
      stdinInput: stdinInput.value,
    })
>>>>>>> Stashed changes

    if (result.correct) {
      userStore.addPoints(result.pointsEarned)
      await ElMessageBox.alert(
        `回答正确，获得 ${result.pointsEarned} 积分${result.nextLevelUnlocked ? '，下一关已解锁。' : '。'}`,
        '挑战成功',
        { type: 'success' },
      )
      router.push('/levels')
      return
    }

    const addResult = await errorStore.addError({
      levelId: currentLevelId.value,
      question: currentLevel.value.question,
      userAnswer: answer.value,
      description: `关卡：${currentLevel.value.name}。你的答案：${answer.value}`,
<<<<<<< Updated upstream
      createdAt: new Date().toLocaleString(),
=======
>>>>>>> Stashed changes
    })
    if (!addResult.success) {
      ElMessage.error(addResult.message || '添加错题失败')
      return
    }
    await ElMessageBox.alert('回答错误，已加入错题本。', '继续加油', { type: 'warning' })
  } catch (error) {
    const serverMessage = error?.response?.data?.message || error?.message || '提交异常，请稍后重试'
    ElMessage.error(serverMessage)
  } finally {
    loading.value = false
  }
}

onMounted(loadLevel)
</script>

<template>
  <div class="page-container challenge-page" v-if="currentLevel">
    <div class="title-row">
      <h2 class="section-title">{{ currentLevel.name }}</h2>
      <el-tag type="primary" effect="plain">{{ typeTagMap[currentLevel.type] || currentLevel.type }}</el-tag>
    </div>
    <el-card class="surface-card challenge-card" shadow="never">
      <div class="question">{{ currentLevel.question }}</div>

      <el-radio-group v-if="currentLevel.type === 'single'" v-model="answer" class="option-group">
        <el-radio v-for="item in currentLevel.options" :key="item" :value="item">{{ item }}</el-radio>
      </el-radio-group>

      <el-checkbox-group v-else-if="currentLevel.type === 'multi'" v-model="answer" class="option-group">
        <el-checkbox v-for="item in currentLevel.options" :key="item" :value="item">{{ item }}</el-checkbox>
      </el-checkbox-group>

      <el-radio-group v-else-if="currentLevel.type === 'judge'" v-model="answer" class="option-group">
        <el-radio v-for="item in currentLevel.options || ['正确', '错误']" :key="item" :value="item">
          {{ item }}
        </el-radio>
      </el-radio-group>

      <div v-else-if="currentLevel.type === 'fill'" class="input-wrap">
        <el-input v-model="answer" type="textarea" :rows="3" placeholder="请输入你的答案，例如：f(n-1)+f(n-2)" />
      </div>

      <div v-else class="input-wrap">
        <el-input v-model="answer" type="textarea" :rows="8" placeholder="请输入代码或伪代码，建议包含关键逻辑和注释。" />
      </div>

      <div class="action-row">
        <el-button @click="router.push('/levels')">返回关卡</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">提交答案</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.challenge-page {
  padding-bottom: 28px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.challenge-card {
  padding: 24px;
  border: 1px solid var(--line-soft);
}

.question {
  font-size: 19px;
  font-weight: 700;
  color: var(--text-title);
}

.option-group {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.input-wrap {
  margin-top: 20px;
}

.action-row {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
