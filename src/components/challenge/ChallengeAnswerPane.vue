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
      </div>
    </div>

    <div class="editor-shell">
      <div class="editor-toolbar">
        <div class="toolbar-left">{{ toolbarLeftText }}</div>
        <div class="toolbar-right">{{ toolbarRightText }}</div>
      </div>

      <div class="answer-wrap" v-if="level.type === 'single'">
        <el-radio-group v-model="answerModel" class="option-group">
          <el-radio v-for="item in level.options" :key="item" :value="item">{{ item }}</el-radio>
        </el-radio-group>
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'multi'">
        <el-checkbox-group v-model="answerModel" class="option-group">
          <el-checkbox v-for="item in level.options" :key="item" :value="item">{{ item }}</el-checkbox>
        </el-checkbox-group>
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'judge'">
        <el-radio-group v-model="answerModel" class="option-group">
          <el-radio v-for="item in level.options || ['正确', '错误']" :key="item" :value="item">
            {{ item }}
          </el-radio>
        </el-radio-group>
      </div>

      <div class="answer-wrap" v-else-if="level.type === 'fill'">
        <el-input
          v-model="answerModel"
          type="textarea"
          :rows="6"
          placeholder="请输入你的答案"
        />
      </div>

      <div class="answer-wrap code-answer-wrap" v-else-if="isCodeChallenge">
        <CodeEditor
          v-model="answerModel"
          v-model:language="languageModel"
          class="challenge-code-editor"
          :disabled="loading"
          :prompt="level.question || level.name"
          placeholder="在这里输入你的代码"
          :min-height="420"
          :show-submit-button="true"
          submit-text="运行评测"
          @submit="$emit('quick-run')"
          @reset-template="$emit('reset-template')"
        />

        <div class="editor-help">
          <span>支持语言切换、行号、自动布局和 Ctrl/Cmd + Enter 快捷提交</span>
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
