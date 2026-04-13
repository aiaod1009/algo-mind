<script setup>
import { computed } from 'vue'
import CodeEditor from '../CodeEditor.vue'

const props = defineProps({
  level: {
    type: Object,
    required: true,
  },
  answer: {
    type: [String, Array],
    default: '',
  },
  language: {
    type: String,
    default: 'java',
  },
  stdinInput: {
    type: String,
    default: '',
  },
  isCodeChallenge: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  maxAttempts: {
    type: Number,
    default: 5,
  },
  attemptsInRun: {
    type: Number,
    default: 0,
  },
  passScore: {
    type: Number,
    default: 70,
  },
  typeLabel: {
    type: String,
    default: '',
  },
})

const emit = defineEmits([
  'update:answer',
  'update:language',
  'update:stdinInput',
  'submit',
  'save-draft',
  'quick-run',
  'reset-template',
])

const answerModel = computed({
  get: () => props.answer,
  set: (value) => emit('update:answer', value),
})

const languageModel = computed({
  get: () => props.language,
  set: (value) => emit('update:language', value),
})

const stdinModel = computed({
  get: () => props.stdinInput,
  set: (value) => emit('update:stdinInput', value),
})

const normalizedOptions = computed(() => (Array.isArray(props.level?.options) ? props.level.options : []))
const hasOptions = computed(() => normalizedOptions.value.length > 0)
const multiTextModel = computed({
  get: () => (Array.isArray(props.answer) ? props.answer.join(',') : String(props.answer || '')),
  set: (value) => {
    const parsed = String(value || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
    emit('update:answer', parsed)
  },
})

const title = computed(() => (props.isCodeChallenge ? '代码作答区' : '作答区域'))
const toolbarLeftText = computed(() => {
  if (props.isCodeChallenge) {
    return 'Monaco 在线编程环境'
  }

  return props.typeLabel || props.level.type
})
const toolbarRightText = computed(() => {
  if (props.isCodeChallenge) {
    return `通过分数 ${props.passScore}`
  }

  return `剩余尝试 ${Math.max(0, props.maxAttempts - props.attemptsInRun)} 次`
})
</script>

<template>
  <section class="surface-card answer-pane">
    <div class="pane-head">
      <h3>{{ title }}</h3>
      <div class="pane-actions">
        <el-button text :disabled="loading" @click="$emit('save-draft')">保存作答</el-button>
        <el-button type="primary" :disabled="loading" @click="$emit('git-it')">
          <svg class="git-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          Git it
        </el-button>
      </div>
    </div>

    <div class="editor-shell">
      <div class="editor-toolbar">
        <div class="toolbar-left">{{ toolbarLeftText }}</div>
        <div class="toolbar-right">{{ toolbarRightText }}</div>
      </div>

      <div class="answer-wrap" v-if="level.type === 'single' && hasOptions">
        <el-radio-group v-model="answerModel" class="option-group">
          <el-radio v-for="item in normalizedOptions" :key="item" :value="item">{{ item }}</el-radio>
        </el-radio-group>
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'single' && !hasOptions">
        <div class="option-missing-tip">该题选项数据缺失，请先手动输入你的答案。</div>
        <el-input v-model="answerModel" type="textarea" :rows="4" placeholder="请输入你的答案" />
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'multi' && hasOptions">
        <el-checkbox-group v-model="answerModel" class="option-group">
          <el-checkbox v-for="item in normalizedOptions" :key="item" :value="item">{{ item }}</el-checkbox>
        </el-checkbox-group>
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'multi' && !hasOptions">
        <div class="option-missing-tip">该题选项数据缺失，请用英文逗号分隔填写多个答案。</div>
        <el-input v-model="multiTextModel" type="textarea" :rows="4" placeholder="示例：A,B" />
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'judge'">
        <el-radio-group v-model="answerModel" class="option-group">
          <el-radio v-for="item in level.options || ['正确', '错误']" :key="item" :value="item">
            {{ item }}
          </el-radio>
        </el-radio-group>
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'fill'">
        <el-input v-model="answerModel" type="textarea" :rows="6" placeholder="请输入你的答案" />
      </div>

      <div class="answer-wrap code-answer-wrap" v-else-if="isCodeChallenge">
        <CodeEditor v-model="answerModel" v-model:language="languageModel" class="challenge-code-editor"
          :disabled="loading" :prompt="level.question || level.name" placeholder="在这里输入你的代码" :min-height="420"
          :show-submit-button="true" submit-text="仅运行" @submit="$emit('quick-run')"
          @reset-template="$emit('reset-template')" />

        <div class="editor-help">
          <span>支持语言切换、行号、自动布局和 Ctrl/Cmd + Enter 快捷运行</span>
          <span>代码评测达到 {{ passScore }} 分即可通关</span>
        </div>
      </div>

      <div class="answer-wrap" v-else>
        <el-input v-model="answerModel" type="textarea" :rows="8" placeholder="请输入你的答案" />
      </div>
    </div>

    <div class="stdin-box" v-if="isCodeChallenge">
      <div class="stdin-title">程序标准输入</div>
      <el-input v-model="stdinModel" type="textarea" :rows="4" placeholder="请输入测试输入" />
    </div>
  </section>
</template>

<style scoped>
.answer-pane {
  padding: 16px;
  border: 1px solid var(--line-soft);
  display: grid;
  gap: 14px;
}

.pane-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.pane-head h3 {
  color: var(--text-title);
  font-size: 28px;
  font-weight: 700;
}

.pane-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pane-actions :deep(.el-button.is-text) {
  border: 1px solid #000;
  border-radius: 6px;
  padding: 8px 16px;
}

.pane-actions :deep(.el-button--primary) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.git-icon {
  width: 16px;
  height: 16px;
}

.editor-shell {
  border: 1px solid var(--line-soft);
  border-radius: 12px;
  background: #f8fbff;
  overflow: hidden;
}

.editor-toolbar {
  min-height: 38px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--line-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #f2f7ff;
}

.toolbar-left,
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

.option-missing-tip {
  margin-bottom: 10px;
  color: #9a6700;
  font-size: 13px;
}

.code-answer-wrap {
  display: grid;
  gap: 10px;
}

.challenge-code-editor {
  overflow: hidden;
}

.editor-help {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--text-sub);
  font-size: 12px;
  flex-wrap: wrap;
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

@media (max-width: 980px) {
  .pane-head h3 {
    font-size: 22px;
  }
}

@media (max-width: 720px) {

  .pane-head,
  .editor-help,
  .editor-toolbar {
    display: grid;
  }
}
</style>
