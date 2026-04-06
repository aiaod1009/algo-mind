<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { useLevelStore } from '../stores/level'
import { useErrorStore } from '../stores/error'
import { useUserStore } from '../stores/user'
import ChallengeTaskPanel from '../components/challenge/ChallengeTaskPanel.vue'
import ChallengeAnswerPane from '../components/challenge/ChallengeAnswerPane.vue'
import ChallengeEvaluationPanel from '../components/challenge/ChallengeEvaluationPanel.vue'
import {
  DEFAULT_EDITOR_LANGUAGE,
  LANGUAGE_LABEL_MAP,
  getCodeTemplate as buildCodeTemplate,
  isTemplateLikeCode as matchesTemplate,
} from '../constants/codeEditor'

const route = useRoute()
const router = useRouter()
const levelStore = useLevelStore()
const errorStore = useErrorStore()
const userStore = useUserStore()

const DRAFT_KEY_PREFIX = 'challenge-draft-'
const CODE_PASS_SCORE = 70
const MAX_ATTEMPTS = 5

const answer = ref('')
const stdinInput = ref('')
const language = ref(DEFAULT_EDITOR_LANGUAGE)
const loading = ref(false)
const attemptsInRun = ref(0)
const startTimestamp = ref(Date.now())
const evaluationResult = ref(null)

const typeLabelMap = {
  single: '单选题',
  multi: '多选题',
  judge: '判断题',
  fill: '填空题',
  code: '代码题',
}

const currentLevelId = computed(() => Number(route.params.id))
const currentLevel = computed(() => levelStore.findLevelById(currentLevelId.value))
const maxAttempts = computed(() => MAX_ATTEMPTS)
const isCodeChallenge = computed(() => currentLevel.value?.type === 'code')
const showEvaluationPanel = computed(() => isCodeChallenge.value && Boolean(evaluationResult.value))
const submitButtonText = computed(() => (isCodeChallenge.value ? '运行评测' : '提交答案'))
const practiceRuleText = computed(() => `练习得分以历史最高成绩为准，当前关卡满分 ${currentLevel.value?.rewardPoints || 0} 分`)
const practiceWindowText = computed(() => {
  const now = new Date()
  const end = new Date(now.getTime() + 9 * 24 * 60 * 60 * 1000)
  return `${now.toLocaleDateString()} 00:00 至 ${end.toLocaleDateString()} 23:59`
})

const getDraftKey = () => `${DRAFT_KEY_PREFIX}${currentLevelId.value}`

const getTemplatePrompt = () => {
  return currentLevel.value?.question || currentLevel.value?.name || 'Write your solution here.'
}

const getCodeTemplate = (lang = language.value) => {
  return buildCodeTemplate(lang, getTemplatePrompt())
}

const isTemplateLikeCode = (value) => {
  return matchesTemplate(value, getTemplatePrompt())
}

const createEmptyAnswer = () => (currentLevel.value?.type === 'multi' ? [] : '')

const applyCodeTemplate = (force = false) => {
  if (!isCodeChallenge.value) {
    return
  }

  if (!force && !isTemplateLikeCode(answer.value)) {
    return
  }

  answer.value = getCodeTemplate(language.value)
}

const normalizeEvaluationResult = (result) => {
  if (!result) {
    return null
  }

  const scoreNumber = Number(result.score)
  const starsNumber = Number(result.stars)
  const pointsEarnedNumber = Number(result.pointsEarned)

  return {
    score: Number.isFinite(scoreNumber) ? Math.max(0, Math.min(100, Math.round(scoreNumber))) : null,
    stars: Number.isFinite(starsNumber) ? Math.max(0, Math.min(3, Math.round(starsNumber))) : 0,
    output: String(result.output ?? result.stdout ?? result.runOutput ?? '').trim(),
    analysis: String(result.analysis ?? result.aiAnalysis ?? result.feedback ?? '').trim(),
    correctness: String(result.correctness ?? '').trim(),
    quality: String(result.quality ?? '').trim(),
    efficiency: String(result.efficiency ?? '').trim(),
    suggestions: Array.isArray(result.suggestions) ? result.suggestions : [],
    pointsEarned: Number.isFinite(pointsEarnedNumber) ? Math.max(0, Math.round(pointsEarnedNumber)) : 0,
    updatedAt: new Date().toLocaleString(),
  }
}

const persistDraft = () => {
  localStorage.setItem(
    getDraftKey(),
    JSON.stringify({
      answer: answer.value,
      stdinInput: stdinInput.value,
      language: language.value,
      savedAt: Date.now(),
    }),
  )
}

const removeDraft = () => {
  localStorage.removeItem(getDraftKey())
}

const restoreDraft = () => {
  const raw = localStorage.getItem(getDraftKey())

  if (!raw) {
    if (isCodeChallenge.value) {
      applyCodeTemplate(true)
    } else {
      answer.value = createEmptyAnswer()
    }
    return
  }

  try {
    const draft = JSON.parse(raw)

    if (draft.answer != null) {
      answer.value = currentLevel.value?.type === 'multi'
        ? (Array.isArray(draft.answer) ? draft.answer : [])
        : draft.answer
    } else {
      answer.value = createEmptyAnswer()
    }

    if (typeof draft.stdinInput === 'string') {
      stdinInput.value = draft.stdinInput
    }

    if (typeof draft.language === 'string' && LANGUAGE_LABEL_MAP[draft.language]) {
      language.value = draft.language
    }

    if (isCodeChallenge.value && !String(answer.value || '').trim()) {
      applyCodeTemplate(true)
    }
  } catch (error) {
    console.warn('读取作答草稿失败。', error)
    answer.value = createEmptyAnswer()

    if (isCodeChallenge.value) {
      applyCodeTemplate(true)
    }
  }
}

const initializeChallenge = async () => {
  try {
    if (!levelStore.levels.length) {
      await levelStore.fetchLevels()
    }
  } catch (error) {
    console.error('关卡加载失败。', error)
    ElMessage.error('关卡加载失败，请稍后重试')
    router.push('/levels')
    return
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

  attemptsInRun.value = 0
  startTimestamp.value = Date.now()
  evaluationResult.value = null
  answer.value = createEmptyAnswer()
  stdinInput.value = ''
  language.value = DEFAULT_EDITOR_LANGUAGE
  restoreDraft()
}

const validateCurrentAnswer = () => {
  if (!currentLevel.value) {
    return false
  }

  if (currentLevel.value.type === 'multi') {
    if (!Array.isArray(answer.value) || answer.value.length === 0) {
      ElMessage.warning('请至少选择一个答案')
      return false
    }
    return true
  }

  const textValue = String(answer.value || '').trim()
  if (!textValue) {
    ElMessage.warning(isCodeChallenge.value ? '请先输入代码' : '请先完成作答')
    return false
  }

  if (isCodeChallenge.value && isTemplateLikeCode(textValue)) {
    ElMessage.warning('请先补全你的代码，再进行评测')
    return false
  }

  return true
}

const syncCodeProgress = async () => {
  if (!currentLevel.value?.answer) {
    return null
  }

  const timeMs = Date.now() - startTimestamp.value
  return levelStore.submitAnswer(currentLevelId.value, currentLevel.value.answer, {
    attempts: attemptsInRun.value,
    timeMs,
    language: language.value,
    stdinInput: stdinInput.value,
    code: String(answer.value || ''),
  })
}

const submitCodeChallenge = async () => {
  const response = await api.evaluateCode({
    code: String(answer.value || ''),
    language: LANGUAGE_LABEL_MAP[language.value] || language.value,
    question: currentLevel.value.question,
    description: currentLevel.value.description,
    stdinInput: stdinInput.value,
  })

  if (response.data?.code !== 0 || !response.data?.data) {
    ElMessage.error('代码评测失败，请稍后重试')
    return
  }

  evaluationResult.value = normalizeEvaluationResult(response.data.data)
  evaluationResult.value.pointsEarned = 0

  if ((evaluationResult.value.score || 0) < CODE_PASS_SCORE) {
    ElMessage.warning(`评测已完成，达到 ${CODE_PASS_SCORE} 分即可通关`)
    return
  }

  const progressResult = await syncCodeProgress()
  if (!progressResult?.correct) {
    ElMessage.warning('代码评测已通过，但关卡进度同步失败，请再试一次')
    return
  }

  if (progressResult.pointsEarned > 0) {
    userStore.addPoints(progressResult.pointsEarned)
  }

  evaluationResult.value.pointsEarned = progressResult.pointsEarned || 0
  removeDraft()
  ElMessage.success('代码评测通过，关卡进度已同步')
}

const submitStandardChallenge = async () => {
  const timeMs = Date.now() - startTimestamp.value
  const result = await levelStore.submitAnswer(currentLevelId.value, answer.value, {
    attempts: attemptsInRun.value,
    timeMs,
    language: language.value,
    stdinInput: stdinInput.value,
  })

  if (!result) {
    ElMessage.error('提交失败，请稍后重试')
    return
  }

  if (result.correct) {
    userStore.addPoints(result.pointsEarned)
    removeDraft()

    const starsEarned = result.starsEarned || 0
    const starsText = '★'.repeat(starsEarned) + '☆'.repeat(3 - starsEarned)

    await ElMessageBox.alert(
      `回答正确，获得 ${result.pointsEarned} 积分。\n本次星级：${starsText}（${starsEarned}/3），历史最佳：${result.bestStars || 0}/3${result.nextLevelUnlocked ? '\n下一关已解锁。' : '。'}`,
      '挑战成功',
      { type: 'success' },
    )

    router.push('/levels')
    return
  }

  try {
    await errorStore.fetchErrors({ skipIfLoaded: false })
  } catch (syncError) {
    console.warn('错题本刷新失败。', syncError)
  }

  await ElMessageBox.alert('回答错误，已加入错题本。', '继续加油', { type: 'warning' })
}

const handleSubmit = async () => {
  if (!validateCurrentAnswer()) {
    return
  }

  if (attemptsInRun.value >= maxAttempts.value) {
    ElMessage.warning(`本关最多尝试 ${maxAttempts.value} 次，请稍后再来`)
    return
  }

  loading.value = true
  attemptsInRun.value += 1

  try {
    if (isCodeChallenge.value) {
      await submitCodeChallenge()
    } else {
      await submitStandardChallenge()
    }
  } catch (error) {
    console.error('提交异常。', error)
    ElMessage.error('提交异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSaveDraft = () => {
  persistDraft()
  ElMessage.success('当前作答已保存')
}

const handleQuickRun = () => {
  handleSubmit()
}

const handleResetTemplate = () => {
  applyCodeTemplate(true)
  evaluationResult.value = null
  ElMessage.success('已恢复当前语言的默认模板')
}

const goBack = () => {
  router.push('/levels')
}

onMounted(initializeChallenge)

watch(currentLevelId, initializeChallenge)

watch(language, (nextLanguage, previousLanguage) => {
  if (!isCodeChallenge.value || nextLanguage === previousLanguage) {
    return
  }

  evaluationResult.value = null

  if (isTemplateLikeCode(answer.value)) {
    answer.value = getCodeTemplate(nextLanguage)
  }
})

watch(
  [answer, stdinInput, language],
  () => {
    if (!currentLevel.value) {
      return
    }

    persistDraft()
  },
  { deep: true },
)
</script>

<template>
  <div v-if="currentLevel" class="page-container challenge-page">
    <header class="challenge-top">
      <div class="title-wrap">
        <div class="top-kicker">Challenge</div>
        <div class="top-title">{{ currentLevel.name }}</div>
      </div>

      <div class="top-action-group">
        <el-button plain @click="router.push('/errors')">错题本</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">{{ submitButtonText }}</el-button>
      </div>
    </header>

    <div class="challenge-layout">
      <ChallengeTaskPanel
        :level="currentLevel"
        :practice-rule-text="practiceRuleText"
        :practice-window-text="practiceWindowText"
        :max-attempts="maxAttempts"
        :type-label="typeLabelMap[currentLevel.type]"
      />

      <div class="workspace-layout" :class="{ 'with-evaluation': showEvaluationPanel }">
        <ChallengeAnswerPane
          v-model:answer="answer"
          v-model:language="language"
          v-model:stdin-input="stdinInput"
          :level="currentLevel"
          :is-code-challenge="isCodeChallenge"
          :loading="loading"
          :max-attempts="maxAttempts"
          :attempts-in-run="attemptsInRun"
          :pass-score="CODE_PASS_SCORE"
          :type-label="typeLabelMap[currentLevel.type]"
          @save-draft="handleSaveDraft"
          @quick-run="handleQuickRun"
          @reset-template="handleResetTemplate"
        />

        <ChallengeEvaluationPanel
          v-if="showEvaluationPanel"
          :result="evaluationResult"
          :pass-score="CODE_PASS_SCORE"
        />
      </div>
    </div>

    <div class="action-row">
      <el-button @click="goBack">返回关卡</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">{{ submitButtonText }}</el-button>
    </div>
  </div>
</template>

<style scoped>
.challenge-page {
  padding-bottom: 28px;
  display: grid;
  gap: 16px;
}

.challenge-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.title-wrap {
  display: grid;
  gap: 6px;
}

.top-kicker {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--text-sub);
}

.top-title {
  color: var(--text-title);
  font-size: 30px;
  font-weight: 800;
}

.top-action-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.challenge-layout {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 14px;
}

.workspace-layout {
  display: grid;
  gap: 12px;
}

.workspace-layout.with-evaluation {
  grid-template-columns: minmax(0, 1fr) 320px;
  align-items: start;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1200px) {
  .workspace-layout.with-evaluation {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .challenge-layout {
    grid-template-columns: 1fr;
  }

  .top-title {
    font-size: 24px;
  }
}

@media (max-width: 720px) {
  .challenge-top,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }

  .top-action-group {
    justify-content: stretch;
  }
}
</style>
