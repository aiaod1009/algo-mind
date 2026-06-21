# 在线代码编辑器与 AI 评测：从前端到后端的完整实现

> 一个基于 Monaco Editor 的在线编程环境，支持多语言切换、代码运行、AI 流式评测和历史代码对比。

---

## 一、整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    Challenge.vue                         │
│  ┌──────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │ 题目面板  │  │   作答区          │  │  AI 评估面板   │ │
│  │ TaskPanel │  │  AnswerPane      │  │  EvalPanel    │ │
│  │           │  │  ┌────────────┐  │  │               │ │
│  │           │  │  │ CodeEditor │  │  │  流式展示      │ │
│  │           │  │  │ (Monaco)   │  │  │  评分/评语     │ │
│  │           │  │  └────────────┘  │  │  推荐代码      │ │
│  │           │  │  stdin 输入框     │  │  代码对比      │ │
│  └──────────┘  └──────────────────┘  └───────────────┘ │
└─────────────────────────────────────────────────────────┘
         │              │                      │
         ▼              ▼                      ▼
    GET /levels    POST /run-code      POST /ai/evaluate-code/stream
                   POST /submit        POST /ai/evaluate-code
```

---

## 二、Monaco Editor 集成

### 为什么选 Monaco

Monaco Editor 是 VS Code 的编辑器内核，开箱即用提供：
- 语法高亮（支持几十种语言）
- 智能补全（关键字、函数、变量）
- 错误提示
- 代码折叠
- 小地图
- 快捷键绑定

### 安装

```bash
npm install monaco-editor @guolao/vue-monaco-editor
```

### Worker 配置

Monaco 需要 Web Worker 来处理语法分析，不同语言用不同的 Worker：

```javascript
import * as monaco from 'monaco-editor'
import { loader, VueMonacoEditor } from '@guolao/vue-monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

// 配置 Worker 工厂
MonacoEnvironment = {
  getWorker(_, label) {
    if (label === 'json') return new jsonWorker()
    if (label === 'css' || label === 'scss' || label === 'less') return new cssWorker()
    if (label === 'html' || label === 'handlebars' || label === 'razor') return new htmlWorker()
    if (label === 'typescript' || label === 'javascript') return new tsWorker()
    return new editorWorker()
  },
}

loader.config({ monaco })
```

### 组件封装

用 `@guolao/vue-monaco-editor` 的 `VueMonacoEditor` 组件，封装成自己的 `CodeEditor.vue`：

```html
<VueMonacoEditor
  v-model:value="codeModel"
  :path="editorPath"
  :language="monacoLanguage"
  :theme="MONACO_THEME"
  :options="editorOptions"
  :height="`${editorHeight}px`"
  width="100%"
  @mount="handleMount"
/>
```

关键 props：
- `v-model:value` — 双向绑定代码内容
- `language` — 语法高亮语言（java / python / cpp / javascript）
- `theme` — 编辑器主题
- `options` — 编辑器行为配置
- `@mount` — 编辑器挂载后的回调，用于注册快捷键等

### 编辑器配置

```javascript
const editorOptions = {
  automaticLayout: true,        // 自动适应容器尺寸
  minimap: { enabled: true },   // 小地图
  wordWrap: 'on',               // 自动换行
  fontSize: 15,
  lineHeight: 26,
  tabSize: 4,
  fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', monospace",
  fontLigatures: true,          // 连字
  bracketPairColorization: { enabled: true },  // 括号配对着色
  autoClosingBrackets: 'always',
  autoClosingQuotes: 'always',
  formatOnPaste: true,
  formatOnType: true,
  quickSuggestions: true,       // 实时补全
  parameterHints: { enabled: true },
  hover: { enabled: true },
  // ... 更多配置
}
```

### 自定义暗色主题

定义了一套 `algo-mind-dark` 主题，背景色 `#0B1120`：

```javascript
editor.defineTheme('algo-mind-dark', {
  base: 'vs-dark',
  inherit: true,
  rules: [
    { token: 'keyword', foreground: 'F472B6', fontStyle: 'bold' },  // 粉色关键字
    { token: 'string', foreground: '34D399' },                      // 绿色字符串
    { token: 'number', foreground: 'F59E0B', fontStyle: 'bold' },   // 黄色数字
    { token: 'function', foreground: '60A5FA' },                    // 蓝色函数
    { token: 'type', foreground: 'A78BFA', fontStyle: 'bold' },     // 紫色类型
    { token: 'comment', foreground: '6B7280', fontStyle: 'italic' },// 灰色注释
    // ...
  ],
  colors: {
    'editor.background': '#0B1120',
    'editor.foreground': '#E2E8F0',
    'editorCursor.foreground': '#38BDF8',
    // ...
  },
})
```

### 高度自适应

根据代码行数动态计算编辑器高度，限制在合理范围：

```javascript
const editorHeight = computed(() => {
  const lineCount = (codeModel.value || '').split(/\r?\n/).length
  const estimated = lineCount * 24 + 40
  return Math.max(420, Math.min(760, estimated))
})
```

### 快捷键绑定

挂载后注册 `Ctrl/Cmd + Enter` 为运行快捷键：

```javascript
const handleMount = (editor, instance) => {
  editor.addCommand(instance.KeyMod.CtrlCmd | instance.KeyCode.Enter, () => {
    emit('submit')
  })
}
```

---

## 三、多语言切换

### 语言选项

```javascript
const LANGUAGE_OPTIONS = [
  { value: 'java', label: 'Java' },
  { value: 'python', label: 'Python' },
  { value: 'cpp', label: 'C++' },
  { value: 'js', label: 'JavaScript' },
]
```

### 切换逻辑

切换语言时，如果当前代码还是模板代码，就自动替换成新语言的模板：

```javascript
const handleLanguageChange = (nextLanguage) => {
  emit('update:language', nextLanguage)

  // 如果代码还是模板，替换为新语言的模板
  if (isTemplateLikeCode(codeModel.value)) {
    emit('update:modelValue', getCodeTemplate(nextLanguage))
  }
}
```

### 代码模板

每种语言有默认模板，包含题目描述和基础结构：

```javascript
// Java 模板示例
public class Main {
    /**
     * 题目描述...
     */
    public static void main(String[] args) {
        // 在这里写你的代码
    }
}
```

---

## 四、代码运行

### 仅运行（看输出）

调用 `POST /api/run-code`，后端执行代码并返回 stdout/stderr：

```javascript
const runCodeOnly = async () => {
  const response = await api.runCode({
    code: answer.value,
    language: language.value,
    stdinInput: stdinInput.value,  // 用户自定义输入
  })

  runResult.value = {
    output: response.data.data.output,   // 标准输出
    error: response.data.data.error,     // 错误输出
  }
}
```

### 运行结果展示

```html
<section class="run-result-panel">
  <div class="run-block">
    <div class="run-label">标准输出</div>
    <pre class="run-pre">{{ runResult.output || '暂无输出' }}</pre>
  </div>
  <div class="run-block">
    <div class="run-label">错误输出</div>
    <pre class="run-pre run-pre-error">{{ runResult.error || '无错误输出' }}</pre>
  </div>
</section>
```

---

## 五、AI 流式评测

这是最核心的功能。用户提交代码后，AI 会给出评分、评语和优化建议。

### 流式请求

用 SSE（Server-Sent Events）实现流式返回，用户能实时看到 AI 的分析过程：

```javascript
const requestStreamEvaluation = (payload) => new Promise((resolve, reject) => {
  let fullText = ''

  api.evaluateCodeStream(
    payload,
    // onChunk — 每收到一段文字就更新 UI
    (chunk) => {
      fullText += chunk
      evaluationStreamingText.value = fullText  // 实时显示
    },
    // onComplete — 流结束，解析最终结果
    async () => {
      const parsed = parseEvaluationResponse(fullText)
      resolve(parsed)
    },
    // onError — 失败降级到非流式接口
    async (streamError) => {
      const response = await api.evaluateCode(payload)
      resolve(normalizeEvaluationResult(response.data.data))
    },
  )
})
```

### API 层实现

```javascript
// 流式请求 — 用 fetch + ReadableStream
const streamRequest = async (url, data, onMessage, onComplete, onError) => {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify(data),
  })

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')

  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    const chunk = decoder.decode(value, { stream: true })
    onMessage(chunk)  // 每个 chunk 回调
  }

  onComplete()
}
```

### AI 返回格式

后端要求 AI 按固定格式返回：

```
---JSON---
{"score":85,"stars":2,"analysis":"代码逻辑基本正确...","suggestions":["建议1","建议2"]}
---CODE---
完整推荐代码
```

前端解析逻辑：

```javascript
const parseEvaluationResponse = (content) => {
  // 1. 用 ---JSON--- 分隔符提取 JSON
  const jsonStart = content.indexOf('---JSON---')
  const codeStart = content.indexOf('---CODE---')

  // 2. 提取 JSON 部分
  let jsonText = content.slice(jsonStart + 10, codeStart).trim()

  // 3. 去掉可能的 markdown 代码块标记
  jsonText = jsonText.replace(/^```json\s*/, '').replace(/\s*```$/, '')

  // 4. 提取推荐代码部分
  const recommendedCode = content.slice(codeStart + 9).trim()

  // 5. 解析 JSON
  const parsed = JSON.parse(jsonText)
  parsed.recommendedCode = recommendedCode

  return {
    score: Math.max(0, Math.min(100, parsed.score)),
    stars: Math.max(0, Math.min(3, parsed.stars)),
    shortComment: parsed.analysis,
    suggestions: parsed.suggestions,
    recommendedCode: parsed.recommendedCode,
  }
}
```

### 评测结果展示

收起状态显示摘要：

```html
<div class="ai-dock-meta">
  <div class="meta-item">
    <span>总分</span>
    <strong>{{ score }} 分</strong>
  </div>
  <div class="meta-item">
    <span>星级</span>
    <strong class="stars">★★☆</strong>
  </div>
</div>
```

展开状态显示完整评语、建议和推荐代码。

### 评分标准

```javascript
const CODE_PASS_SCORE = 70  // 70 分通关

// AI 评分区间：
// 90-100 → 3 星
// 70-89  → 2 星
// 50-69  → 1 星
// 0-49   → 0 星
```

---

## 六、草稿持久化

用户的代码、语言选择、评测结果都自动保存到 `localStorage`，刷新页面不丢失：

```javascript
const DRAFT_KEY_PREFIX = 'challenge-draft-'

const persistDraft = () => {
  localStorage.setItem(
    `${DRAFT_KEY_PREFIX}${levelId}`,
    JSON.stringify({
      answer: answer.value,
      stdinInput: stdinInput.value,
      language: language.value,
      evaluationResult: evaluationResult.value,
      runResult: runResult.value,
      attemptsInRun: attemptsInRun.value,
      savedAt: Date.now(),
    }),
  )
}

// 每次代码变化自动保存
watch([answer, stdinInput, language, evaluationResult], persistDraft, { deep: true })
```

恢复时读取 localStorage，如果有就恢复状态，没有就用模板代码：

```javascript
const restoreDraft = () => {
  const raw = localStorage.getItem(getDraftKey())
  if (raw) {
    const draft = JSON.parse(raw)
    answer.value = draft.answer
    language.value = draft.language
    // ... 恢复其他状态
  } else {
    answer.value = getCodeTemplate(language.value)  // 用模板
  }
}
```

---

## 七、代码历史与对比

### 保存代码快照

用户可以随时把当前代码保存到历史记录：

```javascript
const handleGitIt = async () => {
  await api.saveCodeSnapshot({
    levelId: currentLevelId,
    language: language.value,
    code: answer.value,
    score: evaluationResult.value?.score,
    stars: evaluationResult.value?.stars,
    aiAnalysis: evaluationResult.value?.shortComment,
    // ...
  })
}
```

### 与历史代码对比

可以选一段历史代码，AI 会对比两版代码的差异：

```javascript
const performCodeComparison = async (snapshotId) => {
  const res = await api.compareCode({
    currentCode: answer.value,
    currentLanguage: language.value,
    historySnapshotId: snapshotId,
    levelId: currentLevelId,
  })
  comparisonResult.value = res.data.data
}
```

---

## 八、组件结构总结

```
Challenge.vue                    — 页面主控，管理状态和 API 调用
  ├── ChallengeTaskPanel.vue     — 左侧题目描述面板
  ├── ChallengeAnswerPane.vue    — 作答区容器
  │     └── CodeEditor.vue       — Monaco Editor 封装
  ├── Run Result Panel           — 运行结果（stdout / stderr）
  └── AI Dock                    — 右侧 AI 评估面板
        ├── ChallengeEvaluationPanel.vue  — 评分、评语、建议
        └── CodeComparisonPanel.vue       — 代码对比结果
```

数据流：

```
用户写代码 → v-model 绑定到 answer
  ↓
点「开始测评」→ 流式调 AI → 实时显示分析文字 → 解析最终结果
  ↓
点「提交代码」→ AI 打分 ≥ 70 → 同步关卡进度 → 加积分
                < 70 → 提示「待改进」，可继续修改
  ↓
点「仅运行」→ 调 runCode 接口 → 显示 stdout / stderr
```

---

## 九、后端简述

后端（Spring Boot）做的事情很简单：

1. **拼 Prompt**：把题目、代码、语言、stdin 拼成结构化的 Prompt
2. **调大模型**：发给豆包 AI（字节跳动），流式接收返回
3. **解析结果**：从 AI 返回的文本中提取 JSON 和推荐代码
4. **降级兜底**：如果 AI 不可用，用本地规则粗略打分（代码长度 + 关键字匹配）

```java
// System Prompt 约束 AI 输出格式
"---JSON---
 {"score":0-100,"stars":0-3,"analysis":"分析","suggestions":["建议1","建议2"]}
---CODE---
 完整推荐代码"
```

降级评分逻辑（AI 不可用时）：

```java
if (codeLength > 300) score = 75;
if (code.contains("for") || code.contains("while")) score += 5;
if (code.contains("return")) score += 5;
```

---

## 十、踩坑记录

1. **Monaco Worker 路径**：Vite 环境下 Worker 需要用 `?worker` 后缀导入，否则会报路径错误。

2. **编辑器高度**：Monaco 不会自动撑高，需要手动计算行数 × 行高 + padding，然后调 `editor.layout()`。

3. **流式解析容错**：AI 可能不严格按格式返回，需要兼容 `---JSON---` 缺失、markdown 代码块包裹、JSON 截断等情况。

4. **主题注册时机**：`defineTheme` 只需调一次，用全局 flag 防止重复注册。

5. **语言切换时模板替换**：只有当前代码还是模板时才替换，避免覆盖用户已写的代码。
