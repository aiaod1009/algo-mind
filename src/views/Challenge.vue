<script setup>
import { computed, onMounted, ref, watch } from 'vue'
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
const stdinInput = ref('')
const language = ref('cpp')
const loading = ref(false)
const attemptsInRun = ref(0)
const startTimestamp = ref(Date.now())
const evaluationResult = ref(null)
const DRAFT_KEY_PREFIX = 'challenge-draft-'

const currentLevelId = computed(() => Number(route.params.id))
const currentLevel = computed(() => levelStore.findLevelById(currentLevelId.value))
const typeTagMap = {
  single: '单选题',
  multi: '多选题',
  judge: '判断题',
  fill: '填空题',
  code: '代码题',
}

const practiceRuleText = computed(() => `学生多次练习最高分达到 ${currentLevel.value?.rewardPoints || 0} 分`)
const practiceWindowText = computed(() => {
  const now = new Date()
  const end = new Date(now.getTime() + 9 * 24 * 60 * 60 * 1000)
  return `${now.toLocaleDateString()} 00:00 至 ${end.toLocaleDateString()} 23:59`
})
const maxAttempts = computed(() => 5)
const isCodeChallenge = computed(() => currentLevel.value?.type === 'code')
const showEvaluationPanel = computed(() => isCodeChallenge.value && Boolean(evaluationResult.value))
const submitButtonText = computed(() => (isCodeChallenge.value ? '提交' : '开始评估'))

const normalizeEvaluationResult = (result) => {
  if (!result) return null
  const scoreNumber = Number(result.score)
  return {
    score: Number.isFinite(scoreNumber) ? Math.max(0, Math.min(100, Math.round(scoreNumber))) : null,
    output: String(result.output ?? result.stdout ?? result.runOutput ?? '').trim(),
    analysis: String(result.analysis ?? result.aiAnalysis ?? result.feedback ?? '').trim(),
    pointsEarned: Number(result.pointsEarned || 0),
    updatedAt: Date.now(),
  }
}

const getDraftKey = () => `${DRAFT_KEY_PREFIX}${currentLevelId.value}`

const loadDraft = () => {
  const raw = localStorage.getItem(getDraftKey())
  if (!raw) {
    if (currentLevel.value?.type === 'multi' && !Array.isArray(answer.value)) {
      answer.value = []
    }
    return
  }
  try {
    const draft = JSON.parse(raw)
    if (draft.answer != null) {
      if (currentLevel.value?.type === 'multi') {
        answer.value = Array.isArray(draft.answer) ? draft.answer : []
      } else {
        answer.value = draft.answer
      }
    } else {
      if (currentLevel.value?.type === 'multi') {
        answer.value = []
      }
    }
    if (typeof draft.stdinInput === 'string') {
      stdinInput.value = draft.stdinInput
    }
    if (typeof draft.language === 'string') {
      language.value = draft.language
    }
  } catch (error) {
    console.warn('草稿读取失败。', error)
    if (currentLevel.value?.type === 'multi') {
      answer.value = []
    }
  }
}

const saveDraft = () => {
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
  attemptsInRun.value = 0
  startTimestamp.value = Date.now()
  evaluationResult.value = null
  resetAnswer()
  stdinInput.value = ''
  loadDraft()
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
  attemptsInRun.value += 1
  try {
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

    if (isCodeChallenge.value) {
      evaluationResult.value = normalizeEvaluationResult(result)
      if (result.pointsEarned > 0) {
        userStore.addPoints(result.pointsEarned)
      }
      ElMessage.success('提交成功，已生成评估结果')
      return
    }

    if (result.correct) {
      userStore.addPoints(result.pointsEarned)
      localStorage.removeItem(getDraftKey())
      const starsText = '★'.repeat(result.starsEarned || 0) + '☆'.repeat(3 - (result.starsEarned || 0))
      await ElMessageBox.alert(
        `回答正确，获得 ${result.pointsEarned} 积分。\n本次星级：${starsText}（${result.starsEarned || 0}/3），历史最佳：${result.bestStars || 0}/3${result.nextLevelUnlocked ? '。\n下一关已解锁。' : '。'}`,
        '挑战成功',
        { type: 'success' },
      )
      router.push('/levels')
      return
    }

    await errorStore.addError({
      levelId: currentLevelId.value,
      question: currentLevel.value.question,
      userAnswer: answer.value,
      description: `关卡：${currentLevel.value.name}。你的答案：${answer.value}`,
      createdAt: Date.now(),
    })
    await ElMessageBox.alert('回答错误，已加入错题本。', '继续加油', { type: 'warning' })
  } catch (error) {
    ElMessage.error('提交异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSaveDraft = () => {
  saveDraft()
  ElMessage.success('已保存当前作答')
}

const handleAutoRun = () => {
  ElMessage.success('已执行快速检查（演示模式）')
}

onMounted(loadLevel)
watch(currentLevelId, loadLevel)
</script>

<template>
  <div class="page-container challenge-page" v-if="currentLevel">
    <div class="challenge-top">
      <div class="top-title">AI 实践</div>
      <div class="top-action-group">
        <el-button plain @click="router.push('/errors')">作答记录</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">{{ submitButtonText }}</el-button>
      </div>
    </div>
    <div class="challenge-layout">
      <aside class="surface-card task-panel">
        <h3>任务要求</h3>
        <div class="task-name">{{ currentLevel.name }}</div>
        <p class="task-rule">达标标准：{{ practiceRuleText }}</p>
        <p class="task-rule">练习时间：{{ practiceWindowText }}</p>
        <p class="task-rule">可作答次数：{{ maxAttempts }} 次</p>
        <div class="task-section-title">任务说明</div>
        <p class="task-desc">{{ currentLevel.description }}</p>
        <p class="task-desc">{{ currentLevel.question }}</p>
        <div class="task-tags">
          <el-tag type="primary" effect="plain">{{ typeTagMap[currentLevel.type] || currentLevel.type }}</el-tag>
          <el-tag type="warning" effect="plain">奖励 {{ currentLevel.rewardPoints }} 分</el-tag>
        </div>
      </aside>

      <section class="surface-card editor-panel">
        <div class="editor-head">
          <h3>代码编辑</h3>
          <div class="editor-head-actions">
            <el-button text @click="handleAutoRun">自动运行</el-button>
            <el-button text @click="handleSaveDraft">保存作答</el-button>
          </div>
        </div>

        <div class="code-eval-layout" :class="{ 'is-code': showEvaluationPanel }">
          <div class="editor-shell">
            <div class="editor-toolbar">
              <el-select v-model="language" size="small" class="lang-select">
                <el-option label="C++" value="cpp" />
                <el-option label="Java" value="java" />
                <el-option label="Python" value="python" />
                <el-option label="JavaScript" value="js" />
              </el-select>
              <div class="toolbar-right">评测模式</div>
            </div>

            <div class="answer-wrap" v-if="currentLevel.type === 'single'">
              <el-radio-group v-model="answer" class="option-group">
                <el-radio v-for="item in currentLevel.options" :key="item" :value="item">{{ item }}</el-radio>
              </el-radio-group>
            </div>

            <div class="answer-wrap" v-else-if="currentLevel.type === 'multi'">
              <el-checkbox-group v-model="answer" class="option-group">
                <el-checkbox v-for="item in currentLevel.options" :key="item" :value="item">{{ item }}</el-checkbox>
              </el-checkbox-group>
            </div>

            <div class="answer-wrap" v-else-if="currentLevel.type === 'judge'">
              <el-radio-group v-model="answer" class="option-group">
                <el-radio v-for="item in currentLevel.options || ['正确', '错误']" :key="item" :value="item">
                  {{ item }}
                </el-radio>
              </el-radio-group>
            </div>

            <div class="answer-wrap" v-else-if="currentLevel.type === 'fill'">
              <el-input v-model="answer" type="textarea" :rows="5" placeholder="请输入你的答案，例如：f(n-1)+f(n-2)" />
            </div>

            <div class="answer-wrap" v-else>
              <el-input v-model="answer" type="textarea" :rows="16" class="code-input"
                placeholder="请输入代码或伪代码，建议包含关键逻辑和注释。" />
            </div>
          </div>

          <Transition name="eval-slide">
            <aside v-if="showEvaluationPanel" class="eval-panel">
              <div class="eval-title">评估结果</div>
              <div class="eval-score-row">
                <span class="label">评估分数</span>
                <span class="score">{{ evaluationResult.score ?? '--' }}</span>
              </div>
              <div class="eval-meta" v-if="evaluationResult.pointsEarned > 0">本次奖励：{{ evaluationResult.pointsEarned }} 分
              </div>
              <div class="eval-meta">更新时间：{{ evaluationResult.updatedAt }}</div>

              <div class="eval-section-title">程序输出</div>
              <pre class="eval-pre">{{ evaluationResult.output || '暂无输出' }}</pre>

              <div class="eval-section-title">AI分析</div>
              <p class="eval-analysis">{{ evaluationResult.analysis || '暂无分析内容' }}</p>
            </aside>
          </Transition>
        </div>

        <div class="stdin-box" v-if="isCodeChallenge">
          <div class="stdin-title">程序键盘读取输入</div>
          <el-input v-model="stdinInput" type="textarea" :rows="4" placeholder="请输入测试输入" />
        </div>

        <div class="action-row">
          <el-button @click="router.push('/levels')">返回关卡</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">{{ submitButtonText }}</el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.challenge-page {
  padding-bottom: 28px;
  display: grid;
  gap: 14px;
}

.challenge-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-title {
  color: var(--text-title);
  font-size: 28px;
  font-weight: 700;
}

.top-action-group {
  display: flex;
  gap: 10px;
}

.challenge-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 14px;
}

.task-panel {
  padding: 16px;
  border: 1px solid var(--line-soft);
  display: grid;
  gap: 8px;
}

.task-panel h3 {
  color: var(--text-title);
  font-size: 28px;
  font-weight: 700;
}

.task-name {
  color: var(--brand-blue-strong);
  font-size: 20px;
  font-weight: 700;
  margin-top: 4px;
}

.task-rule,
.task-desc {
  color: var(--text-sub);
  margin: 0;
}

.task-section-title {
  margin-top: 10px;
  color: var(--text-title);
  font-size: 18px;
  font-weight: 700;
}

.task-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.editor-panel {
  padding: 16px;
  border: 1px solid var(--line-soft);
  display: grid;
  gap: 14px;
}

.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-head h3 {
  color: var(--text-title);
  font-size: 28px;
  font-weight: 700;
}

.editor-head-actions {
  display: flex;
  gap: 10px;
}

.code-eval-layout {
  display: block;
}

.code-eval-layout.is-code {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 12px;
}

.eval-slide-enter-active,
.eval-slide-leave-active {
  transition: all 0.24s ease;
}

.eval-slide-enter-from,
.eval-slide-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

.editor-shell {
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  background: #f8fbff;
  overflow: hidden;
}

.editor-toolbar {
  height: 38px;
  padding: 0 10px;
  border-bottom: 1px solid var(--line-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f2f7ff;
}

.lang-select {
  width: 120px;
}

.toolbar-right {
  color: var(--text-sub);
  font-size: 13px;
}

.answer-wrap {
  min-height: 280px;
  padding: 16px;
}

.answer-wrap :deep(textarea),
.stdin-box :deep(textarea) {
  overflow-y: auto;
}

.option-group {
  display: grid;
  gap: 12px;
}

.code-input :deep(textarea) {
  font-family: 'Consolas', 'JetBrains Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.eval-panel {
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  padding: 12px;
  background: #f8fbff;
  display: grid;
  gap: 8px;
  max-height: 420px;
  overflow-y: auto;
}

.eval-title {
  color: var(--text-title);
  font-weight: 700;
  font-size: 16px;
}

.eval-score-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.eval-score-row .label {
  color: var(--text-sub);
  font-size: 13px;
}

.eval-score-row .score {
  color: var(--brand-blue-strong);
  font-size: 24px;
  font-weight: 700;
}

.eval-meta {
  color: var(--text-sub);
  font-size: 12px;
}

.eval-section-title {
  margin-top: 6px;
  color: var(--text-title);
  font-size: 13px;
  font-weight: 700;
}

.eval-pre {
  margin: 0;
  border: 1px solid var(--line-soft);
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  min-height: 68px;
  max-height: 160px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--text-main);
  font-size: 12px;
}

.eval-analysis {
  margin: 0;
  border: 1px solid var(--line-soft);
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  min-height: 96px;
  max-height: 220px;
  overflow-y: auto;
  white-space: pre-wrap;
  color: var(--text-main);
  font-size: 13px;
}

.eval-empty {
  margin: 0;
  color: var(--text-sub);
  font-size: 13px;
}

.stdin-box {
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  padding: 12px;
  background: #f8fbff;
  display: grid;
  gap: 8px;
}

.stdin-title {
  color: var(--text-title);
  font-weight: 700;
}

.action-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1200px) {
  .code-eval-layout.is-code {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .challenge-layout {
    grid-template-columns: 1fr;
  }

  .task-panel h3,
  .editor-head h3 {
    font-size: 22px;
  }
}
</style>
