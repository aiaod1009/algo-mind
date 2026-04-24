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
  minimap: { enabled: true, scale: 1, renderCharacters: false, maxColumn: 120 },
  glyphMargin: true,
  scrollBeyondLastLine: true,
  smoothScrolling: true,
  contextmenu: true,
  wordWrap: 'on',
  wrappingIndent: 'indent',
  fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace",
  fontLigatures: true,
  fontSize: 15,
  lineHeight: 26,
  tabSize: 4,
  insertSpaces: true,
  roundedSelection: true,
  readOnly: props.disabled,
  cursorBlinking: 'smooth',
  cursorSmoothCaretAnimation: 'on',
  cursorStyle: 'line-thin',
  renderLineHighlight: 'all',
  renderWhitespace: 'selection',
  renderIndentGuides: true,
  renderFinalNewline: 'on',
  padding: { top: 20, bottom: 20 },
  overviewRulerLanes: 3,
  lineNumbersMinChars: 4,
  folding: true,
  foldingHighlight: true,
  showFoldingControls: 'always',
  unfoldOnClickAfterEndOfLine: true,
  guides: {
    indentation: true,
    bracketPairs: true,
    bracketPairsHorizontal: true,
    highlightActiveIndentation: true,
    highlightActiveBracketPair: true,
  },
  bracketPairColorization: {
    enabled: true,
    independentColorPoolPerBracketType: true,
  },
  matchBrackets: 'always',
  autoClosingBrackets: 'always',
  autoClosingQuotes: 'always',
  autoIndent: 'full',
  formatOnPaste: true,
  formatOnType: true,
  suggest: {
    showKeywords: true,
    showSnippets: true,
    showFunctions: true,
    showClasses: true,
    showVariables: true,
    showModules: true,
  },
  quickSuggestions: true,
  quickSuggestionsDelay: 100,
  acceptSuggestionOnCommitCharacter: true,
  acceptSuggestionOnEnter: 'on',
  snippetSuggestions: 'inline',
  parameterHints: { enabled: true, cycle: true },
  hover: { enabled: true, delay: 300, sticky: true },
  scrollPredominantAxis: 'auto',
  scrollbar: {
    vertical: 'auto',
    horizontal: 'auto',
    useShadows: true,
    verticalHasArrows: false,
    horizontalHasArrows: false,
    verticalScrollbarSize: 12,
    horizontalScrollbarSize: 12,
    alwaysConsumeMouseWheel: false,
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
      // 注释
      { token: 'comment', foreground: '6B7280', fontStyle: 'italic' },
      { token: 'comment.doc', foreground: '6B7280', fontStyle: 'italic bold' },
      // 关键字
      { token: 'keyword', foreground: 'F472B6', fontStyle: 'bold' },
      { token: 'keyword.control', foreground: 'F472B6', fontStyle: 'bold' },
      { token: 'keyword.operator', foreground: 'F472B6' },
      // 字符串
      { token: 'string', foreground: '34D399' },
      { token: 'string.escape', foreground: '6EE7B7' },
      { token: 'string.template', foreground: '34D399' },
      // 数字
      { token: 'number', foreground: 'F59E0B', fontStyle: 'bold' },
      { token: 'number.hex', foreground: 'FBBF24', fontStyle: 'bold' },
      // 函数
      { token: 'function', foreground: '60A5FA' },
      { token: 'function.call', foreground: '60A5FA' },
      { token: 'function.builtin', foreground: '93C5FD' },
      // 类/类型
      { token: 'type', foreground: 'A78BFA', fontStyle: 'bold' },
      { token: 'class', foreground: 'A78BFA', fontStyle: 'bold' },
      { token: 'interface', foreground: 'A78BFA', fontStyle: 'bold' },
      // 变量
      { token: 'variable', foreground: 'E5E7EB' },
      { token: 'variable.predefined', foreground: 'F87171' },
      { token: 'variable.parameter', foreground: 'FCA5A5' },
      // 常量
      { token: 'constant', foreground: 'F59E0B', fontStyle: 'bold' },
      { token: 'constant.language', foreground: 'F59E0B', fontStyle: 'bold' },
      // 运算符
      { token: 'operator', foreground: 'F472B6' },
      // 标签
      { token: 'tag', foreground: 'F87171' },
      { token: 'tag.attribute', foreground: 'FDBA74' },
      // 属性
      { token: 'property', foreground: '93C5FD' },
      // 正则
      { token: 'regexp', foreground: 'FBBF24' },
      // 装饰器/注解
      { token: 'decorator', foreground: 'C084FC', fontStyle: 'italic' },
      // 宏
      { token: 'macro', foreground: 'C084FC', fontStyle: 'bold' },
      // 命名空间
      { token: 'namespace', foreground: 'A78BFA', fontStyle: 'bold' },
      // 模块
      { token: 'module', foreground: 'A78BFA' },
    ],
    colors: {
      // 编辑器背景
      'editor.background': '#0B1120',
      'editor.foreground': '#E2E8F0',
      
      // 行高亮
      'editor.lineHighlightBackground': '#1E293B80',
      'editor.lineHighlightBorder': '#33415540',
      
      // 光标
      'editorCursor.foreground': '#38BDF8',
      'editorCursor.background': '#0B1120',
      
      // 行号
      'editorLineNumber.foreground': '#475569',
      'editorLineNumber.activeForeground': '#94A3B8',
      
      // 选择
      'editor.selectionBackground': '#3B82F640',
      'editor.selectionForeground': '#E2E8F0',
      'editor.inactiveSelectionBackground': '#3B82F620',
      'editor.selectionHighlightBackground': '#3B82F630',
      
      // 缩进指南
      'editorIndentGuide.background1': '#1E293B',
      'editorIndentGuide.background2': '#1E293B',
      'editorIndentGuide.background3': '#1E293B',
      'editorIndentGuide.background4': '#1E293B',
      'editorIndentGuide.activeBackground1': '#475569',
      'editorIndentGuide.activeBackground2': '#475569',
      'editorIndentGuide.activeBackground3': '#475569',
      'editorIndentGuide.activeBackground4': '#475569',
      
      // 括号匹配
      'editorBracketMatch.background': '#3B82F630',
      'editorBracketMatch.border': '#60A5FA',
      
      // 概览标尺
      'editorOverviewRuler.background': '#0B1120',
      'editorOverviewRuler.border': '#1E293B',
      'editorOverviewRuler.findMatchForeground': '#F59E0B80',
      'editorOverviewRuler.rangeHighlightForeground': '#3B82F880',
      'editorOverviewRuler.selectionHighlightForeground': '#3B82F860',
      'editorOverviewRuler.wordHighlightForeground': '#A78BFA80',
      'editorOverviewRuler.wordHighlightStrongForeground': '#A78BFA80',
      'editorOverviewRuler.modifiedForeground': '#3B82F8',
      'editorOverviewRuler.addedForeground': '#10B981',
      'editorOverviewRuler.deletedForeground': '#EF4444',
      'editorOverviewRuler.errorForeground': '#EF4444',
      'editorOverviewRuler.warningForeground': '#F59E0B',
      'editorOverviewRuler.infoForeground': '#3B82F8',
      
      // 小地图
      'minimap.background': '#0B1120',
      'minimap.selectionHighlight': '#3B82F880',
      'minimap.errorHighlight': '#EF444480',
      'minimap.warningHighlight': '#F59E0B80',
      'minimap.findMatchHighlight': '#F59E0B80',
      
      // 编辑器组件
      'editorWidget.background': '#151F32',
      'editorWidget.border': '#334155',
      'editorWidget.foreground': '#E2E8F0',
      
      // 建议小部件
      'editorSuggestWidget.background': '#151F32',
      'editorSuggestWidget.border': '#334155',
      'editorSuggestWidget.foreground': '#E2E8F0',
      'editorSuggestWidget.highlightForeground': '#38BDF8',
      'editorSuggestWidget.selectedBackground': '#1E293B',
      'editorSuggestWidget.selectedForeground': '#E2E8F0',
      
      // 悬停提示
      'editorHoverWidget.background': '#151F32',
      'editorHoverWidget.border': '#334155',
      'editorHoverWidget.foreground': '#E2E8F0',
      
      // 查找/替换
      'editor.findMatchBackground': '#F59E0B40',
      'editor.findMatchHighlightBackground': '#F59E0B20',
      'editor.findRangeHighlightBackground': '#3B82F820',
      
      // 链接
      'editorLink.activeForeground': '#38BDF8',
      
      // 光标的行号背景
      'editor.lineNumber.activeForeground': '#38BDF8',
      
      // 折叠
      'editor.foldBackground': '#1E293B60',
      
      // 单词高亮
      'editor.wordHighlightBackground': '#A78BFA30',
      'editor.wordHighlightStrongBackground': '#A78BFA50',
      
      // 范围高亮
      'editor.rangeHighlightBackground': '#3B82F820',
      
      // 符号高亮
      'editor.symbolHighlightBackground': '#F59E0B30',
      
      // 空白字符
      'editorWhitespace.foreground': '#33415540',
      
      // 行尾
      'editorLineNumber.dimmedForeground': '#334155',
      
      // 活动行号背景
      'editorGutter.activeBackground': '#1E293B40',
      
      // 修改标记
      'editorGutter.modifiedBackground': '#3B82F8',
      'editorGutter.addedBackground': '#10B981',
      'editorGutter.deletedBackground': '#EF4444',
      
      // 错误/警告
      'editorError.foreground': '#EF4444',
      'editorError.background': '#EF444420',
      'editorWarning.foreground': '#F59E0B',
      'editorWarning.background': '#F59E0B20',
      'editorInfo.foreground': '#3B82F8',
      'editorInfo.background': '#3B82F820',
      'editorHint.foreground': '#10B981',
      'editorHint.background': '#10B98120',
      
      // 内联提示
      'editor.inlineValuesBackground': '#1E293B',
      'editor.inlineValuesForeground': '#94A3B8',
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
  border-radius: 16px;
  border: 1px solid rgba(51, 65, 85, 0.6);
  background: linear-gradient(180deg, #0f172a 0%, #0b1120 100%);
  box-shadow:
    0 0 0 1px rgba(56, 189, 248, 0.1),
    0 25px 50px -12px rgba(0, 0, 0, 0.5),
    0 0 100px rgba(56, 189, 248, 0.05);
  overflow: hidden;
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}

.code-editor-card:hover {
  box-shadow:
    0 0 0 1px rgba(56, 189, 248, 0.2),
    0 25px 50px -12px rgba(0, 0, 0, 0.6),
    0 0 120px rgba(56, 189, 248, 0.08);
  border-color: rgba(71, 85, 105, 0.8);
}

.editor-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
  background: linear-gradient(180deg, #151f32 0%, #0f172a 100%);
  border-bottom: 1px solid rgba(51, 65, 85, 0.4);
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 18px;
  background: linear-gradient(180deg, #0f172a 0%, #0b1120 100%);
  border-top: 1px solid rgba(51, 65, 85, 0.4);
}

.topbar-left,
.topbar-right,
.footer-actions,
.footer-hint {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.editor-badge {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid rgba(56, 189, 248, 0.25);
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.15) 0%, rgba(14, 165, 233, 0.1) 100%);
  color: #7dd3fc;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  box-shadow: 0 2px 8px rgba(56, 189, 248, 0.15);
}

.language-select {
  width: 140px;
}

.language-select :deep(.el-input__wrapper) {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(71, 85, 105, 0.5);
  border-radius: 8px;
  box-shadow: none;
}

.language-select :deep(.el-input__wrapper:hover) {
  border-color: rgba(56, 189, 248, 0.4);
  background: rgba(30, 41, 59, 0.8);
}

.language-select :deep(.el-input__inner) {
  color: #e2e8f0;
  font-size: 13px;
  font-weight: 500;
}

.topbar-right {
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.topbar-right span {
  padding: 4px 10px;
  background: rgba(30, 41, 59, 0.5);
  border-radius: 6px;
  border: 1px solid rgba(51, 65, 85, 0.3);
}

.editor-stage {
  position: relative;
  background: #0b1120;
}

.editor-placeholder {
  position: absolute;
  top: 24px;
  left: 68px;
  z-index: 2;
  pointer-events: none;
  color: #475569;
  font-size: 15px;
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, Monaco, 'Courier New', monospace;
  font-style: italic;
  letter-spacing: 0.02em;
}

.editor-loading,
.editor-failure {
  display: grid;
  place-items: center;
  min-height: 300px;
  color: #94a3b8;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.editor-loading::before {
  content: '';
  width: 40px;
  height: 40px;
  border: 3px solid rgba(56, 189, 248, 0.2);
  border-top-color: #38bdf8;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.footer-hint {
  color: #475569;
  font-size: 12px;
  font-weight: 500;
}

.footer-hint span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.footer-hint span::before {
  content: '•';
  color: #38bdf8;
  font-size: 14px;
}

.footer-hint span:first-child::before {
  content: none;
}

.footer-actions :deep(.el-button) {
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.02em;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

.footer-actions :deep(.el-button--primary) {
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(14, 165, 233, 0.3);
}

.footer-actions :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%);
  box-shadow: 0 6px 20px rgba(14, 165, 233, 0.4);
  transform: translateY(-1px);
}

.footer-actions :deep(.el-button--primary:active) {
  transform: translateY(0);
}

.footer-actions :deep(.el-button.is-text) {
  color: #94a3b8;
  background: transparent;
  border: 1px solid transparent;
}

.footer-actions :deep(.el-button.is-text:hover) {
  color: #e2e8f0;
  background: rgba(30, 41, 59, 0.6);
  border-color: rgba(71, 85, 105, 0.5);
}

.footer-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

/* Monaco Editor 容器样式优化 */
:deep(.monaco-editor) {
  border-radius: 0 !important;
}

:deep(.monaco-editor .overflow-guard) {
  border-radius: 0 !important;
}

:deep(.monaco-editor .margin) {
  background: #0b1120 !important;
}

:deep(.monaco-editor .monaco-scrollable-element) {
  background: #0b1120 !important;
}

@media (max-width: 720px) {
  .editor-topbar,
  .editor-footer {
    display: grid;
    gap: 12px;
    padding: 12px 14px;
  }

  .topbar-left,
  .topbar-right,
  .footer-actions,
  .footer-hint {
    width: 100%;
    justify-content: center;
  }

  .language-select {
    width: 100%;
  }

  .editor-badge {
    display: none;
  }
}
</style>
