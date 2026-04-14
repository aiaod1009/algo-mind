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
const evaluationStreamingText = ref('')
const isEvaluatingStream = ref(false)
const runResult = ref(null)
const isAiDockExpanded = ref(false)
let isDraftSyncPaused = false

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
const hasEvaluationResult = computed(() => isCodeChallenge.value && Boolean(evaluationResult.value))
const showRunPanel = computed(() => isCodeChallenge.value && Boolean(runResult.value))
const submitButtonText = computed(() => (isCodeChallenge.value ? '运行评测' : '提交答案'))
const evaluationSummary = computed(() => {
  if (!evaluationResult.value) {
    return {
      scoreText: '--',
      starsText: '☆☆☆',
      statusText: '未评测',
    }
  }

  const stars = Math.max(0, Math.min(3, Number(evaluationResult.value.stars || 0)))
  const score = Number(evaluationResult.value.score)
  return {
    scoreText: Number.isFinite(score) ? `${score}` : '--',
    starsText: '★'.repeat(stars) + '☆'.repeat(3 - stars),
    statusText: score >= CODE_PASS_SCORE ? '已达标' : '待改进',
  }
})
const practiceRuleText = computed(() => `练习得分以历史最高成绩为准，当前关卡满分 ${currentLevel.value?.rewardPoints || 0} 分`)
const practiceWindowText = computed(() => {
  const now = new Date()
  const end = new Date(now.getTime() + 9 * 24 * 60 * 60 * 1000)
  return `${now.toLocaleDateString()} 00:00 至 ${end.toLocaleDateString()} 23:59`
})
const projectQuery = computed(() => (typeof route.query.project === 'string' ? route.query.project : undefined))

const navigateBackToLevels = () => {
  router.push({
    path: '/levels',
    query: projectQuery.value ? { project: projectQuery.value } : {},
  })
}

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

const hasChoiceOptions = (level) => {
  return Array.isArray(level?.options) && level.options.length > 0
}

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
  const shortComment = String(
    result.shortComment ?? result.analysis ?? result.aiAnalysis ?? result.feedback ?? '',
  ).trim()
  const recommendedCode = String(
    result.recommendedCode ?? result.aiRecommendedCode ?? '',
  ).trim()

  return {
    score: Number.isFinite(scoreNumber) ? Math.max(0, Math.min(100, Math.round(scoreNumber))) : null,
    stars: Number.isFinite(starsNumber) ? Math.max(0, Math.min(3, Math.round(starsNumber))) : 0,
    output: String(result.output ?? result.stdout ?? result.runOutput ?? '').trim(),
    shortComment,
    analysis: shortComment,
    correctness: String(result.correctness ?? '').trim(),
    quality: String(result.quality ?? '').trim(),
    efficiency: String(result.efficiency ?? '').trim(),
    suggestions: Array.isArray(result.suggestions) ? result.suggestions : [],
    recommendedCode,
    pointsEarned: Number.isFinite(pointsEarnedNumber) ? Math.max(0, Math.round(pointsEarnedNumber)) : 0,
    updatedAt: String(result.updatedAt ?? new Date().toLocaleString()),
  }
}

const buildEvaluationPayload = () => ({
  code: String(answer.value || ''),
  language: LANGUAGE_LABEL_MAP[language.value] || language.value,
  question: currentLevel.value?.question,
  description: currentLevel.value?.description,
  stdinInput: stdinInput.value,
})

const parseEvaluationResponseContent = (content) => {
  const rawText = String(content || '').trim()
  if (!rawText) {
    throw new Error('AI 未返回评测内容')
  }

  let jsonText = rawText
  if (jsonText.startsWith('```json')) {
    jsonText = jsonText.replace(/^```json\s*/, '').replace(/\s*```$/, '')
  } else if (jsonText.startsWith('```')) {
    jsonText = jsonText.replace(/^```\s*/, '').replace(/\s*```$/, '')
  }

  return normalizeEvaluationResult(JSON.parse(jsonText.trim()))
}

const JSON_SEPARATOR = '---JSON---'

const extractTextBeforeJson = (text) => {
  const separatorIndex = text.indexOf(JSON_SEPARATOR)
  if (separatorIndex === -1) {
    return text
  }
  return text.substring(0, separatorIndex).trim()
}

const extractJsonAfterSeparator = (text) => {
  const separatorIndex = text.indexOf(JSON_SEPARATOR)
  if (separatorIndex === -1) {
    return text
  }
  return text.substring(separatorIndex + JSON_SEPARATOR.length).trim()
}

const requestStreamEvaluation = (payload) => new Promise((resolve, reject) => {
  let fullText = ''

  api.evaluateCodeStream(
    payload,
    (chunk) => {
      fullText += chunk
      evaluationStreamingText.value = extractTextBeforeJson(fullText)
    },
    async () => {
      try {
        const jsonText = extractJsonAfterSeparator(fullText) || fullText
        const parsed = parseEvaluationResponseContent(jsonText)
        resolve(parsed)
      } catch (streamParseError) {
        try {
          const response = await api.evaluateCode(payload)
          if (response.data?.code !== 0 || !response.data?.data) {
            throw new Error(response.data?.message || '代码评测失败')
          }
          resolve(normalizeEvaluationResult(response.data.data))
        } catch (fallbackError) {
          reject(fallbackError)
        }
      }
    },
    async (streamError) => {
      try {
        const response = await api.evaluateCode(payload)
        if (response.data?.code !== 0 || !response.data?.data) {
          throw new Error(response.data?.message || '代码评测失败')
        }
        resolve(normalizeEvaluationResult(response.data.data))
      } catch (fallbackError) {
        reject(streamError || fallbackError)
      }
    },
  )
})

const normalizeRunResult = (result) => {
  if (!result) {
    return null
  }

  return {
    output: String(result?.output ?? '').trim(),
    error: String(result?.error ?? '').trim(),
    updatedAt: String(result?.updatedAt ?? new Date().toLocaleString()),
  }
}

const persistDraft = () => {
  if (isDraftSyncPaused || !currentLevel.value) {
    return
  }

  localStorage.setItem(
    getDraftKey(),
    JSON.stringify({
      answer: answer.value,
      stdinInput: stdinInput.value,
      language: language.value,
      attemptsInRun: attemptsInRun.value,
      startTimestamp: startTimestamp.value,
      evaluationResult: evaluationResult.value,
      runResult: runResult.value,
      isAiDockExpanded: isAiDockExpanded.value,
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

    if (Number.isFinite(Number(draft.attemptsInRun))) {
      attemptsInRun.value = Math.max(0, Number(draft.attemptsInRun))
    }

    if (Number.isFinite(Number(draft.startTimestamp)) && Number(draft.startTimestamp) > 0) {
      startTimestamp.value = Number(draft.startTimestamp)
    }

    evaluationResult.value = normalizeEvaluationResult(draft.evaluationResult)
    runResult.value = normalizeRunResult(draft.runResult)
    isAiDockExpanded.value = Boolean(draft.isAiDockExpanded && isCodeChallenge.value)

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
    navigateBackToLevels()
    return
  }

  if (!currentLevel.value) {
    ElMessage.error('关卡不存在')
    navigateBackToLevels()
    return
  }

  if (!currentLevel.value.isUnlocked) {
    ElMessage.warning('该关卡尚未解锁')
    navigateBackToLevels()
    return
  }

  isDraftSyncPaused = true

  try {

  attemptsInRun.value = 0
  startTimestamp.value = Date.now()
  evaluationResult.value = null
  evaluationStreamingText.value = ''
  isEvaluatingStream.value = false
  isAiDockExpanded.value = false
  runResult.value = null
  answer.value = createEmptyAnswer()
  stdinInput.value = ''
  language.value = DEFAULT_EDITOR_LANGUAGE

  // 检查是否从 URL 参数恢复历史代码
  const applySnapshotId = route.query.applySnapshot
  const targetLanguage = route.query.language

  if (applySnapshotId) {
    await loadAndApplySnapshot(Number(applySnapshotId), targetLanguage)
  } else {
    restoreDraft()
  }
  } finally {
    isDraftSyncPaused = false
    persistDraft()
  }
}

const validateCurrentAnswer = () => {
  if (!currentLevel.value) {
    return false
  }

  if (currentLevel.value.type === 'multi') {
    if (hasChoiceOptions(currentLevel.value)) {
      if (!Array.isArray(answer.value) || answer.value.length === 0) {
        ElMessage.warning('请至少选择一个答案')
        return false
      }
      return true
    }

    const textValue = Array.isArray(answer.value)
      ? answer.value.join(',').trim()
      : String(answer.value || '').trim()
    if (!textValue) {
      ElMessage.warning('请先填写答案')
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

const refreshErrorBook = async () => {
  try {
    await errorStore.refreshAll()
  } catch (syncError) {
    console.warn('错题本状态刷新失败', syncError)
  }
}

const submitCodeChallenge = async () => {
  const payload = buildEvaluationPayload()

  evaluationResult.value = null
  evaluationStreamingText.value = ''
  isEvaluatingStream.value = true
  isAiDockExpanded.value = true

  try {
    evaluationResult.value = await requestStreamEvaluation(payload)
    persistDraft()
  } catch (error) {
    console.error('AI 代码评测失败。', error)
    ElMessage.error('代码评测失败，请稍后重试')
    return
  } finally {
    isEvaluatingStream.value = false
  }

  if (!evaluationResult.value) {
    ElMessage.error('代码评测失败，请稍后重试')
    return
  }

  evaluationResult.value.pointsEarned = 0
  isAiDockExpanded.value = true

  if ((evaluationResult.value.score || 0) < CODE_PASS_SCORE) {
    try {
      const timeMs = Date.now() - startTimestamp.value
      await levelStore.submitAnswer(currentLevelId.value, String(answer.value || ''), {
        attempts: attemptsInRun.value,
        timeMs,
        language: language.value,
        stdinInput: stdinInput.value,
      })
      persistDraft()
      await refreshErrorBook()
    } catch (syncError) {
      console.warn('编程题错题落库失败。', syncError)
    }
    ElMessage.warning(`评测已完成，达到 ${CODE_PASS_SCORE} 分即可通关`)
    return
  }

  try {
    const progressResult = await syncCodeProgress()
    if (!progressResult?.correct) {
      ElMessage.warning('代码评测已通过，但关卡进度同步失败，请再试一次')
      persistDraft()
      return
    }

    if (progressResult.pointsEarned > 0) {
      userStore.addPoints(progressResult.pointsEarned)
    }

    await refreshErrorBook()
    evaluationResult.value.pointsEarned = progressResult.pointsEarned || 0
    persistDraft()
    ElMessage.success('代码评测通过，关卡进度已同步')
  } catch (error) {
    console.error('代码评测结果同步失败。', error)
    persistDraft()
    ElMessage.error('代码评测已完成，但保存结果失败，请稍后重试')
  }
}

const runCodeOnly = async () => {
  if (!isCodeChallenge.value) {
    return
  }

  if (!validateCurrentAnswer()) {
    return
  }

  loading.value = true

  try {
    const response = await api.runCode({
      code: String(answer.value || ''),
      language: language.value,
      stdinInput: stdinInput.value,
    })

    if (response.data?.code !== 0 || !response.data?.data) {
      ElMessage.error(response.data?.message || '代码运行失败，请稍后重试')
      return
    }

    runResult.value = normalizeRunResult(response.data.data)
    persistDraft()

    if (runResult.value.error) {
      ElMessage.warning('代码已运行，存在错误输出')
    } else {
      ElMessage.success('代码运行成功')
    }
  } catch (error) {
    console.error('代码运行异常。', error)
    ElMessage.error('代码运行异常，请稍后重试')
  } finally {
    loading.value = false
  }
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

    await refreshErrorBook()
    const starsEarned = result.starsEarned || 0
    const starsText = '★'.repeat(starsEarned) + '☆'.repeat(3 - starsEarned)

    await ElMessageBox.alert(
      `回答正确，获得 ${result.pointsEarned} 积分。\n本次星级：${starsText}（${starsEarned}/3），历史最佳：${result.bestStars || 0}/3${result.nextLevelUnlocked ? '\n下一关已解锁。' : '。'}`,
      '挑战成功',
      { type: 'success' },
    )

    navigateBackToLevels()
    return
  }

  try {
    await refreshErrorBook()
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
    // AI测评超时不显示错误提示，因为AI测评可能需要较长时间
    if (error.code === 'ECONNABORTED') {
      console.warn('AI测评请求超时，用户可以继续等待或重新提交')
    } else {
      ElMessage.error('提交异常，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const handleSaveDraft = () => {
  persistDraft()
  ElMessage.success('当前作答已保存')
}

const handleQuickRun = () => {
  runCodeOnly()
}

const handleResetTemplate = () => {
  applyCodeTemplate(true)
  evaluationResult.value = null
  evaluationStreamingText.value = ''
  isEvaluatingStream.value = false
  isAiDockExpanded.value = false
  runResult.value = null
  ElMessage.success('已恢复当前语言的默认模板')
}

const loadAndApplySnapshot = async (snapshotId, targetLanguage) => {
  try {
    const res = await api.getCodeSnapshots({ levelId: currentLevelId.value })
    const list = res.data?.data || []
    const snapshot = list.find(item => item.id === snapshotId)

    if (!snapshot) {
      ElMessage.warning('历史代码不存在')
      restoreDraft()
      return
    }

    // 设置语言
    if (targetLanguage && LANGUAGE_LABEL_MAP[targetLanguage]) {
      language.value = targetLanguage
    } else if (snapshot.language) {
      // 尝试匹配语言
      const langKey = Object.keys(LANGUAGE_LABEL_MAP).find(
        key => LANGUAGE_LABEL_MAP[key] === snapshot.language
      )
      if (langKey) {
        language.value = langKey
      }
    }

    // 设置代码
    answer.value = snapshot.code || ''
    stdinInput.value = snapshot.stdinInput || ''

    // 恢复评测结果（如果有）
    if (snapshot.score > 0 || snapshot.aiAnalysis || snapshot.aiRecommendedCode || snapshot.aiSuggestionsJson) {
      evaluationResult.value = {
        score: snapshot.score,
        stars: snapshot.stars,
        shortComment: snapshot.aiAnalysis,
        analysis: snapshot.aiAnalysis,
        suggestions: snapshot.aiSuggestionsJson ? JSON.parse(snapshot.aiSuggestionsJson) : [],
        recommendedCode: snapshot.aiRecommendedCode || '',
      }
    }

    // 恢复运行结果（如果有）
    if (snapshot.runOutput || snapshot.compilePassed !== undefined) {
      runResult.value = {
        output: snapshot.runOutput,
        error: snapshot.compilePassed ? '' : '编译失败',
      }
    }

    ElMessage.success('已恢复历史代码')

    // 清除 URL 参数
    router.replace({
      path: `/challenge/${currentLevelId.value}`,
      query: projectQuery.value ? { project: projectQuery.value } : {},
    })
  } catch (err) {
    console.error('加载历史代码失败', err)
    ElMessage.error('加载历史代码失败')
    restoreDraft()
  }
}

const handleGitIt = async () => {
  if (!answer.value || !String(answer.value).trim()) {
    ElMessage.warning('请先编写代码再保存')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定将当前代码保存到历史记录？你可以随时在「历史代码」中回顾。',
      '保存代码',
      { confirmButtonText: '保存', cancelButtonText: '取消', type: 'info' },
    )
  } catch {
    return
  }

  loading.value = true
  try {
    const snapshot = {
      levelId: currentLevelId.value,
      levelName: currentLevel.value?.name || currentLevel.value?.title || '',
      language: LANGUAGE_LABEL_MAP[language.value] || language.value,
      code: String(answer.value || ''),
      stdinInput: stdinInput.value || null,
      score: evaluationResult.value?.score ?? 0,
      stars: evaluationResult.value?.stars ?? 0,
      compilePassed: Boolean(runResult.value && !runResult.value.error),
      aiAnalysis: evaluationResult.value?.shortComment || evaluationResult.value?.analysis || null,
      aiCorrectness: null,
      aiQuality: null,
      aiEfficiency: null,
      aiSuggestionsJson: evaluationResult.value?.suggestions?.length
        ? JSON.stringify(evaluationResult.value.suggestions)
        : null,
      aiRecommendedCode: evaluationResult.value?.recommendedCode || null,
      runOutput: runResult.value?.output || null,
    }

    await api.saveCodeSnapshot(snapshot)
    ElMessage.success('代码已保存到历史记录')
  } catch (err) {
    console.error('保存代码快照失败', err)
    ElMessage.error('代码保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const openEvaluationDialog = () => {
  if (isEvaluatingStream.value) {
    isAiDockExpanded.value = true
    return
  }
  if (!hasEvaluationResult.value) {
    ElMessage.info('请先运行评测')
    return
  }
  isAiDockExpanded.value = !isAiDockExpanded.value
}

const goBack = () => {
  persistDraft()
  navigateBackToLevels()
}

const handleOpenCodeHistory = () => {
  persistDraft()
  router.push({ path: '/code-history', query: { levelId: currentLevelId.value } })
}

const handleOpenErrors = () => {
  persistDraft()
  router.push('/errors')
}

onMounted(initializeChallenge)

watch(currentLevelId, initializeChallenge)

watch(language, (nextLanguage, previousLanguage) => {
  if (!isCodeChallenge.value || nextLanguage === previousLanguage) {
    return
  }

  evaluationResult.value = null
  evaluationStreamingText.value = ''
  isEvaluatingStream.value = false
  isAiDockExpanded.value = false
  runResult.value = null

  if (isTemplateLikeCode(answer.value)) {
    answer.value = getCodeTemplate(nextLanguage)
  }
})

watch(
  [answer, stdinInput, language, evaluationResult, runResult, attemptsInRun, isAiDockExpanded],
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
    <!-- 浮动返回键 -->
    <button class="floating-back-btn" @click="goBack" title="返回关卡列表">
      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2"
        stroke-linecap="round" stroke-linejoin="round">
        <path d="M19 12H5" />
        <path d="M12 19l-7-7 7-7" />
      </svg>
      <span class="back-text">返回</span>
    </button>

    <header class="challenge-top">
      <div class="title-wrap">
        <div class="top-kicker">Challenge</div>
        <div class="top-title">{{ currentLevel.name }}</div>
      </div>

      <div class="top-action-group">
        <el-button v-if="isCodeChallenge" plain class="action-pill-btn" @click="handleOpenCodeHistory">历史代码</el-button>
        <el-button plain class="action-pill-btn" @click="handleOpenErrors">错题本</el-button>
      </div>
    </header>

    <div class="challenge-layout" :class="{ 'with-ai-dock': isCodeChallenge }">
      <ChallengeTaskPanel :level="currentLevel" :practice-rule-text="practiceRuleText"
        :practice-window-text="practiceWindowText" :max-attempts="maxAttempts"
        :type-label="typeLabelMap[currentLevel.type]" />

      <div class="workspace-layout">
        <ChallengeAnswerPane v-model:answer="answer" v-model:language="language" v-model:stdin-input="stdinInput"
          :level="currentLevel" :is-code-challenge="isCodeChallenge" :loading="loading" :max-attempts="maxAttempts"
          :attempts-in-run="attemptsInRun" :pass-score="CODE_PASS_SCORE" :type-label="typeLabelMap[currentLevel.type]"
          @save-draft="handleSaveDraft" @quick-run="handleQuickRun" @reset-template="handleResetTemplate" @git-it="handleGitIt" />

        <section v-if="showRunPanel" class="surface-card run-result-panel">
          <div class="run-result-head">
            <h3>运行结果</h3>
            <span class="run-time">{{ runResult.updatedAt }}</span>
          </div>

          <div class="run-block">
            <div class="run-label">标准输出</div>
            <pre class="run-pre">{{ runResult.output || '暂无输出' }}</pre>
          </div>

          <div class="run-block">
            <div class="run-label">错误输出</div>
            <pre class="run-pre run-pre-error">{{ runResult.error || '无错误输出' }}</pre>
          </div>
        </section>
      </div>

      <aside v-if="isCodeChallenge" class="surface-card ai-dock" :class="{ expanded: isAiDockExpanded }">
        <div class="ai-dock-head">
          <h3>AI评估结果</h3>
          <span class="ai-status"
            :class="{ passed: hasEvaluationResult && Number(evaluationSummary.scoreText) >= CODE_PASS_SCORE, loading: isEvaluatingStream }">
            {{ isEvaluatingStream ? '分析中' : evaluationSummary.statusText }}
          </span>
        </div>

        <!-- 收起状态：显示摘要 -->
        <template v-if="!isAiDockExpanded">
          <div class="ai-dock-meta">
            <div class="meta-item">
              <span>总分</span>
              <strong>{{ evaluationSummary.scoreText }} 分</strong>
            </div>
            <div class="meta-item">
              <span>星级</span>
              <strong class="stars">{{ evaluationSummary.starsText }}</strong>
            </div>
          </div>

          <p class="ai-dock-tip">点击下方按钮查看完整代码评审与优化建议。</p>

          <el-button type="primary" plain class="open-eval-btn" :disabled="!hasEvaluationResult && !isEvaluatingStream"
            @click="openEvaluationDialog">
            展开评估详情
          </el-button>
        </template>

        <!-- 展开状态：显示完整评估内容 -->
        <template v-else>
          <div class="ai-dock-full-content">
            <ChallengeEvaluationPanel v-if="hasEvaluationResult || isEvaluatingStream" :result="evaluationResult"
              :pass-score="CODE_PASS_SCORE" :loading="isEvaluatingStream" :streaming-text="evaluationStreamingText" />
          </div>
          <el-button type="primary" plain class="open-eval-btn" @click="openEvaluationDialog">
            收起评估详情
          </el-button>
        </template>
      </aside>
    </div>

    <div class="action-row">
      <el-button class="action-pill-btn" @click="goBack">返回关卡</el-button>
      <el-button v-if="isCodeChallenge" plain class="action-pill-btn" :loading="loading"
        @click="runCodeOnly">仅运行</el-button>
      <el-button type="primary" class="action-pill-btn" :loading="loading" @click="handleSubmit">{{ submitButtonText
      }}</el-button>
    </div>
  </div>
</template>

<style scoped>
.challenge-page {
  width: min(1720px, calc(100vw - 24px));
  max-width: min(1720px, calc(100vw - 24px));
  padding-bottom: 28px;
  display: grid;
  gap: 16px;
  position: relative;
}

/* 浮动返回键样式 - 固定在页面左上角 */
.floating-back-btn {
  position: fixed;
  top: 100px;
  left: 24px;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(74, 111, 157, 0.15);
  border-radius: 10px;
  cursor: pointer;
  color: #4a6f9d;
  font-size: 13px;
  font-weight: 500;
  box-shadow: 0 2px 12px rgba(74, 111, 157, 0.12);
  backdrop-filter: blur(10px);
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.floating-back-btn:hover {
  transform: translateX(-3px);
  background: #4a6f9d;
  color: white;
  box-shadow: 0 4px 20px rgba(74, 111, 157, 0.3);
}

.floating-back-btn:active {
  transform: translateX(-1px) scale(0.97);
}

.back-text {
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
  letter-spacing: 0.3px;
}

@media (max-width: 980px) {
  .floating-back-btn {
    top: 92px;
    left: 16px;
    padding: 6px 10px;
  }

  .back-text {
    display: none;
  }
}

@media (max-width: 720px) {
  .floating-back-btn {
    top: 88px;
    left: 12px;
  }
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
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.challenge-layout.with-ai-dock {
  grid-template-columns: 280px minmax(0, 1fr) minmax(280px, 25%);
}

.workspace-layout {
  display: grid;
  gap: 14px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.action-pill-btn {
  border-radius: 999px;
  min-height: 40px;
  padding: 0 20px;
}

.ai-dock {
  border: 1px solid var(--line-soft);
  padding: 14px;
  display: grid;
  gap: 12px;
  position: sticky;
  top: 12px;
}

.ai-dock-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.ai-dock-head h3 {
  margin: 0;
  color: var(--text-title);
  font-size: 22px;
  font-weight: 800;
}

.ai-status {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #596579;
  background: #edf1f7;
}

.ai-status.passed {
  color: #166534;
  background: #dcfce7;
}

.ai-status.loading {
  color: #1d4ed8;
  background: #dbeafe;
}

.ai-dock-meta {
  display: grid;
  gap: 8px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-sub);
  font-size: 14px;
}

.meta-item strong {
  color: var(--text-title);
  font-size: 20px;
}

.meta-item .stars {
  color: #f59e0b;
  letter-spacing: 2px;
}

.ai-dock-tip {
  margin: 0;
  color: var(--text-sub);
  line-height: 1.6;
}

.open-eval-btn {
  width: 100%;
}

/* AI Dock 展开状态样式 */
.ai-dock {
  transition: all 0.3s ease;
  max-height: 300px;
  overflow: hidden;
  width: 100%;
}

.ai-dock.expanded {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  grid-column: 3;
  width: 100%;
  min-width: 0;
}

.ai-dock-full-content {
  padding: 16px 0;
  border-top: 1px solid var(--line-soft);
  margin-top: 12px;
}



.run-result-panel {
  border: 1px solid var(--line-soft);
  display: grid;
  gap: 10px;
  padding: 16px;
}

.run-result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.run-result-head h3 {
  margin: 0;
  color: var(--text-title);
  font-size: 16px;
  font-weight: 700;
}

.run-time {
  color: var(--text-sub);
  font-size: 12px;
}

.run-block {
  display: grid;
  gap: 6px;
}

.run-label {
  color: var(--text-title);
  font-size: 13px;
  font-weight: 600;
}

.run-pre {
  margin: 0;
  min-height: 72px;
  max-height: 280px;
  overflow: auto;
  border: 1px solid var(--line-soft);
  border-radius: 8px;
  background: #fff;
  padding: 10px;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-main);
  font-size: 12px;
}

.run-pre-error {
  color: #b42318;
}

@media (max-width: 1200px) {
  .challenge-layout.with-ai-dock {
    grid-template-columns: 260px minmax(0, 1fr);
  }

  .ai-dock {
    grid-column: 1 / -1;
    position: static;
    width: 100%;
    max-width: 100%;
  }

  .ai-dock.expanded {
    grid-column: 1 / -1;
    width: 100%;
    max-width: 100%;
  }
}

@media (max-width: 980px) {
  .challenge-layout {
    grid-template-columns: 1fr;
  }

  .challenge-layout.with-ai-dock {
    grid-template-columns: 1fr;
  }

  .top-title {
    font-size: 24px;
  }

  .challenge-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .top-action-group {
    width: 100%;
  }

  .top-action-group .el-button {
    flex: 1;
  }

  .ai-dock.expanded {
    width: 100%;
    max-width: 100%;
    max-height: 80vh;
    grid-column: 1;
  }

  .workspace-layout {
    min-width: 0;
  }
}

@media (max-width: 720px) {
  .challenge-page {
    gap: 12px;
    padding-bottom: 80px;
  }

  .challenge-top {
    padding: 0 8px;
  }

  .top-title {
    font-size: 20px;
  }

  .top-kicker {
    font-size: 11px;
  }

  .challenge-layout,
  .challenge-layout.with-ai-dock {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .task-panel {
    padding: 12px;
  }

  .task-panel h3 {
    font-size: 14px;
    margin-bottom: 8px;
  }

  .task-name {
    font-size: 16px;
  }

  .task-rule {
    font-size: 12px;
  }

  .task-desc {
    font-size: 13px;
    line-height: 1.5;
  }

  .task-tags {
    flex-wrap: wrap;
    gap: 6px;
  }

  .workspace-layout {
    gap: 10px;
  }

  .answer-pane {
    padding: 12px;
  }

  .editor-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    padding: 8px 10px;
  }

  .toolbar-left {
    font-size: 11px;
  }

  .toolbar-right {
    font-size: 10px;
    opacity: 0.7;
  }

  .topbar-right span {
    display: none;
  }

  .topbar-right span:first-child {
    display: inline;
  }

  .stdin-box {
    padding: 10px;
  }

  .stdin-title {
    font-size: 13px;
    margin-bottom: 8px;
  }

  .editor-help {
    font-size: 11px;
    padding: 8px 10px;
    flex-direction: column;
    gap: 4px;
  }

  .editor-footer {
    flex-direction: column;
    gap: 8px;
    padding: 10px;
  }

  .footer-actions {
    width: 100%;
    justify-content: space-between;
  }

  .footer-actions .el-button {
    flex: 1;
    font-size: 12px;
    padding: 6px 12px;
  }

  .action-row {
    flex-direction: column;
    gap: 8px;
    padding: 12px;
    background: rgba(255, 255, 255, 0.95);
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
    z-index: 50;
  }

  .action-row .el-button {
    width: 100%;
    min-height: 44px;
    font-size: 14px;
  }

  .ai-dock {
    padding: 10px;
  }

  .ai-dock-head h3 {
    font-size: 16px;
  }

  .ai-status {
    font-size: 11px;
    padding: 3px 8px;
  }

  .ai-dock.expanded {
    max-height: 60vh;
    width: 100%;
    max-width: 100%;
    grid-column: 1;
  }

  .eval-panel {
    padding: 10px;
    font-size: 13px;
  }

  .eval-title {
    font-size: 14px;
  }

  .eval-score-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .eval-score-row .score {
    font-size: 20px;
  }

  .eval-section-title {
    font-size: 12px;
    margin-top: 10px;
  }

  .eval-pre,
  .eval-analysis {
    font-size: 11px;
    padding: 8px;
    min-height: 60px;
  }

  .eval-detail {
    font-size: 12px;
    line-height: 1.5;
  }

  .eval-suggestions {
    font-size: 12px;
    padding-left: 16px;
  }

  .eval-suggestions li {
    margin: 4px 0;
    line-height: 1.4;
  }

  .open-eval-btn {
    font-size: 12px;
    padding: 8px 12px;
  }
}

@media (max-width: 480px) {
  .floating-back-btn {
    width: 40px;
    height: 40px;
    padding: 0;
    justify-content: center;
    border-radius: 50%;
    top: 80px;
    left: 12px;
  }

  .floating-back-btn svg {
    width: 16px;
    height: 16px;
  }

  .challenge-page {
    width: 100vw;
    padding-left: 12px;
    padding-right: 12px;
  }

  .top-title {
    font-size: 18px;
  }

  .task-name {
    font-size: 15px;
  }

  .task-rule {
    font-size: 11px;
  }

  .editor-stage {
    min-height: 280px !important;
  }

  .editor-stage>div>div {
    height: 280px !important;
  }

  .ai-dock-head {
    flex-wrap: wrap;
    gap: 8px;
  }

  .ai-dock-head h3 {
    width: 100%;
  }

  .stars {
    font-size: 16px;
  }

  .eval-meta {
    font-size: 11px;
  }
}
</style>
