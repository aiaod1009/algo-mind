<script setup>
import { computed, nextTick, onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import * as monaco from 'monaco-editor'
import { loader, VueMonacoEditor } from '@guolao/vue-monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'
import {
  DEFAULT_EDITOR_LANGUAGE,
  LANGUAGE_OPTIONS,
  getCodeTemplate,
  isTemplateLikeCode,
} from '../constants/codeEditor'

const MONACO_THEME = 'algo-mind-dark'
const globalRuntime = typeof self !== 'undefined' ? self : globalThis

if (!globalRuntime.__ALGO_MIND_MONACO_READY__) {
  globalRuntime.MonacoEnvironment = {
    getWorker(_, label) {
      if (label === 'json') {
        return new jsonWorker()
      }
      if (label === 'css' || label === 'scss' || label === 'less') {
        return new cssWorker()
      }
      if (label === 'html' || label === 'handlebars' || label === 'razor') {
        return new htmlWorker()
      }
      if (label === 'typescript' || label === 'javascript') {
        return new tsWorker()
      }
      return new editorWorker()
    },
  }

  loader.config({ monaco })
  globalRuntime.__ALGO_MIND_MONACO_READY__ = true
}

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  language: {
    type: String,
    default: DEFAULT_EDITOR_LANGUAGE,
  },
  prompt: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: 'Write your code here...',
  },
  minHeight: {
    type: Number,
    default: 360,
  },
  maxHeight: {
    type: Number,
    default: 760,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  showSubmitButton: {
    type: Boolean,
    default: false,
  },
  submitText: {
    type: String,
    default: 'Submit Code',
  },
})

const emit = defineEmits([
  'update:modelValue',
  'update:language',
  'submit',
  'ready',
  'reset-template',
])

const editorInstance = shallowRef(null)
const monacoInstance = shallowRef(null)
const editorDisposables = []
const isFocused = ref(false)
const hasBootstrappedTemplate = ref(false)

const resolvedLanguage = computed(() => props.language || DEFAULT_EDITOR_LANGUAGE)
const monacoLanguage = computed(() => (resolvedLanguage.value === 'js' ? 'javascript' : resolvedLanguage.value))
const languageOptions = LANGUAGE_OPTIONS

const codeModel = computed({
  get: () => props.modelValue ?? '',
  set: (value) => emit('update:modelValue', value ?? ''),
})

const editorPath = computed(() => {
  const extensionMap = {
    java: 'java',
    cpp: 'cpp',
    python: 'py',
    js: 'js',
  }

  return `file:///challenge/main.${extensionMap[resolvedLanguage.value] || 'txt'}`
})

const currentLineCount = computed(() => {
  const content = String(codeModel.value || '')
  if (!content) {
    return 8
  }

  return content.split(/\r?\n/).length
})

const editorHeight = computed(() => {
  const estimated = currentLineCount.value * 24 + 40
  return Math.max(props.minHeight, Math.min(props.maxHeight, estimated))
})

const showPlaceholder = computed(() => {
  return !codeModel.value && !isFocused.value
})

const editorOptions = computed(() => ({
  automaticLayout: true,
  minimap: { enabled: false },
  glyphMargin: false,
  scrollBeyondLastLine: false,
  smoothScrolling: true,
  contextmenu: true,
  wordWrap: 'on',
  wrappingIndent: 'indent',
  fontFamily: "'JetBrains Mono', 'Consolas', 'Microsoft YaHei Mono', monospace",
  fontLigatures: true,
  fontSize: 14,
  lineHeight: 24,
  tabSize: 2,
  insertSpaces: true,
  roundedSelection: true,
  readOnly: props.disabled,
  cursorBlinking: 'smooth',
  cursorSmoothCaretAnimation: 'on',
  renderLineHighlight: 'all',
  padding: { top: 16, bottom: 16 },
  overviewRulerLanes: 0,
  lineNumbersMinChars: 3,
  folding: true,
  guides: {
    indentation: true,
  },
}))

const resolveTemplate = (language = resolvedLanguage.value) => {
  const prompt = props.prompt || 'Write your solution here.'
  return getCodeTemplate(language, prompt)
}

const resetTemplate = () => {
  emit('update:modelValue', resolveTemplate())
  emit('reset-template')
  nextTick(() => editorInstance.value?.focus())
}

const shouldReplaceWithTemplate = (value) => {
  return isTemplateLikeCode(value, props.prompt || 'Write your solution here.')
}

const handleLanguageChange = (nextLanguage) => {
  emit('update:language', nextLanguage)

  if (shouldReplaceWithTemplate(codeModel.value)) {
    emit('update:modelValue', resolveTemplate(nextLanguage))
  }

  nextTick(() => editorInstance.value?.focus())
}

const syncEditorHeight = () => {
  editorInstance.value?.layout()
}

const handleSubmit = () => {
  emit('submit')
}

const formatCode = async () => {
  await editorInstance.value?.getAction('editor.action.formatDocument')?.run()
}

const focus = () => {
  editorInstance.value?.focus()
}

const ensureTheme = (instance) => {
  if (globalRuntime.__ALGO_MIND_MONACO_THEME_READY__) {
    return
  }

  instance.editor.defineTheme(MONACO_THEME, {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'comment', foreground: '7f8ea3' },
      { token: 'keyword', foreground: 'f472b6' },
      { token: 'number', foreground: 'f59e0b' },
      { token: 'string', foreground: '34d399' },
    ],
    colors: {
      'editor.background': '#0f172a',
      'editor.foreground': '#e5eefc',
      'editor.lineHighlightBackground': '#13233d',
      'editorCursor.foreground': '#7dd3fc',
      'editorLineNumber.foreground': '#5f7593',
      'editorLineNumber.activeForeground': '#dbeafe',
      'editor.selectionBackground': '#1d4ed84d',
      'editor.inactiveSelectionBackground': '#1d4ed833',
      'editorIndentGuide.background1': '#22314b',
      'editorIndentGuide.activeBackground1': '#36527d',
      'editorWidget.background': '#111c31',
      'editorWidget.border': '#233451',
    },
  })

  globalRuntime.__ALGO_MIND_MONACO_THEME_READY__ = true
}

const handleMount = (editor, instance) => {
  editorInstance.value = editor
  monacoInstance.value = instance

  ensureTheme(instance)
  instance.editor.setTheme(MONACO_THEME)

  editorDisposables.push(
    editor.onDidFocusEditorText(() => {
      isFocused.value = true
    }),
  )

  editorDisposables.push(
    editor.onDidBlurEditorText(() => {
      isFocused.value = false
    }),
  )

  editor.addCommand(instance.KeyMod.CtrlCmd | instance.KeyCode.Enter, () => {
    emit('submit')
  })

  emit('ready', { editor, monaco: instance })
  syncEditorHeight()
}

watch(
  () => props.modelValue,
  () => {
    syncEditorHeight()
  },
)

watch(
  () => props.prompt,
  (nextPrompt, previousPrompt) => {
    if (!hasBootstrappedTemplate.value) {
      return
    }

    if (nextPrompt !== previousPrompt && shouldReplaceWithTemplate(codeModel.value)) {
      emit('update:modelValue', resolveTemplate())
    }
  },
)

watch(
  () => [props.language, props.modelValue, props.prompt],
  ([nextLanguage, nextValue]) => {
    if (hasBootstrappedTemplate.value) {
      return
    }

    if (String(nextValue || '').trim()) {
      hasBootstrappedTemplate.value = true
      return
    }

    emit('update:modelValue', resolveTemplate(nextLanguage || DEFAULT_EDITOR_LANGUAGE))
    hasBootstrappedTemplate.value = true
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  editorDisposables.forEach((disposable) => disposable?.dispose?.())
})

defineExpose({
  focus,
  formatCode,
  resetTemplate,
})
</script>

<template>
  <div class="code-editor-card">
    <div class="editor-topbar">
      <div class="topbar-left">
        <div class="editor-badge">Monaco Editor</div>
        <el-select
          :model-value="resolvedLanguage"
          size="small"
          class="language-select"
          :disabled="disabled"
          @update:modelValue="handleLanguageChange"
        >
          <el-option
            v-for="item in languageOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>

      <div class="topbar-right">
        <span>{{ currentLineCount }} lines</span>
        <span>Ctrl/Cmd + Enter</span>
      </div>
    </div>

    <div class="editor-stage" :style="{ minHeight: `${minHeight}px` }">
      <div v-if="showPlaceholder" class="editor-placeholder">
        {{ placeholder }}
      </div>

      <VueMonacoEditor
        v-model:value="codeModel"
        :path="editorPath"
        :language="monacoLanguage"
        :theme="MONACO_THEME"
        :options="editorOptions"
        :height="`${editorHeight}px`"
        width="100%"
        @mount="handleMount"
      >
        <template #default>
          <div class="editor-loading">Loading Monaco Editor...</div>
        </template>

        <template #failure>
          <div class="editor-failure">Monaco Editor failed to load.</div>
        </template>
      </VueMonacoEditor>
    </div>

    <div class="editor-footer">
      <div class="footer-hint">
        <span>Auto layout enabled</span>
        <span>Templates follow the selected language</span>
      </div>

      <div class="footer-actions">
        <el-button text :disabled="disabled" @click="formatCode">Format</el-button>
        <el-button text :disabled="disabled" @click="resetTemplate">Reset Template</el-button>
        <el-button
          v-if="showSubmitButton"
          type="primary"
          :disabled="disabled"
          @click="handleSubmit"
        >
          {{ submitText }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.code-editor-card {
  border-radius: 14px;
  border: 1px solid #1f324f;
  background:
    radial-gradient(circle at top left, rgba(125, 211, 252, 0.08), transparent 32%),
    linear-gradient(180deg, #0d1527 0%, #0f172a 100%);
  box-shadow: 0 22px 40px rgba(15, 23, 42, 0.28);
  overflow: hidden;
}

.editor-topbar,
.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  background: rgba(8, 13, 25, 0.82);
}

.topbar-left,
.topbar-right,
.footer-actions,
.footer-hint {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.editor-badge {
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgba(125, 211, 252, 0.2);
  background: rgba(14, 116, 144, 0.14);
  color: #b6e5f7;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.language-select {
  width: 136px;
}

.topbar-right,
.footer-hint {
  color: #8da2bf;
  font-size: 12px;
}

.editor-stage {
  position: relative;
}

.editor-placeholder {
  position: absolute;
  top: 18px;
  left: 64px;
  z-index: 2;
  pointer-events: none;
  color: #5c6f89;
  font-size: 14px;
}

.editor-loading,
.editor-failure {
  display: grid;
  place-items: center;
  min-height: 260px;
  color: #dbeafe;
  font-size: 14px;
}

.footer-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

@media (max-width: 720px) {
  .editor-topbar,
  .editor-footer {
    display: grid;
  }

  .language-select {
    width: 100%;
  }
}
</style>
