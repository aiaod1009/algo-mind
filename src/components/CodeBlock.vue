<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  language: {
    type: String,
    default: 'javascript'
  },
  title: {
    type: String,
    default: ''
  },
  code: {
    type: String,
    default: ''
  },
  callouts: {
    type: Array,
    default: () => []
  },
  maxLines: {
    type: Number,
    default: 15
  },
  showLineNumbers: {
    type: Boolean,
    default: true
  }
})

const isExpanded = ref(false)
const isCopied = ref(false)
const codeContainerRef = ref(null)
const actualLineCount = computed(() => props.code.split('\n').length)
const shouldShowExpand = computed(() => actualLineCount.value > props.maxLines)

const highlightedCode = computed(() => {
  // 简单的语法高亮实现
  let code = props.code
    // 字符串
    .replace(/(['"`])(.*?)(\1)/g, '<span class="token-string">$1$2$3</span>')
    // 关键字
    .replace(/\b(def|class|return|if|else|elif|for|while|import|from|try|except|finally|with|as|in|is|not|and|or|None|True|False|function|const|let|var|if|else|for|while|return|class|import|export|default|async|await|new|this|typeof|instanceof)\b/g, '<span class="token-keyword">$1</span>')
    // 注释
    .replace(/(#.*$)/gm, '<span class="token-comment">$1</span>')
    .replace(/(\/\/.*$)/gm, '<span class="token-comment">$1</span>')
    // 数字
    .replace(/\b(\d+)\b/g, '<span class="token-number">$1</span>')
    // 函数名
    .replace(/(\w+)(?=\()/g, '<span class="token-function">$1</span>')
  
  return code
})

const lineNumbers = computed(() => {
  const lines = props.code.split('\n').length
  return Array.from({ length: lines }, (_, i) => i + 1)
})

const displayLineNumbers = computed(() => {
  if (isExpanded.value || !shouldShowExpand.value) {
    return lineNumbers.value
  }
  return lineNumbers.value.slice(0, props.maxLines)
})

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(props.code)
    isCopied.value = true
    ElMessage.success('代码已复制到剪贴板')
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}


</script>

<template>
  <article class="code-block-card">
    <!-- 工具栏 -->
    <header class="code-block-header">
      <div class="code-block-meta">
        <span class="code-lang-badge">
          <span class="lang-name">{{ language.toLowerCase() }}</span>
        </span>
        <h4 v-if="title" class="code-title">{{ title }}</h4>
      </div>
      <div class="code-block-actions">
        <button 
          class="action-btn copy-btn" 
          :class="{ copied: isCopied }"
          @click="copyCode"
          title="复制代码"
        >
          <svg v-if="!isCopied" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
          </svg>
          <svg v-else viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>{{ isCopied ? '已复制' : '复制' }}</span>
        </button>
      </div>
    </header>

    <!-- 代码内容区 -->
    <div class="code-block-body" :class="{ collapsed: !isExpanded && shouldShowExpand }">
      <div class="code-wrapper" ref="codeContainerRef">
        <!-- 行号 -->
        <div v-if="showLineNumbers" class="line-numbers">
          <span 
            v-for="num in displayLineNumbers" 
            :key="num" 
            class="line-number"
          >{{ num }}</span>
        </div>
        
        <!-- 代码内容 -->
        <pre class="code-content"><code v-html="highlightedCode"></code></pre>
      </div>
      
      <!-- 展开遮罩 -->
      <div v-if="!isExpanded && shouldShowExpand" class="expand-overlay">
        <button class="expand-btn" @click="toggleExpand">
          <span>展开</span>
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"></polyline>
          </svg>
        </button>
      </div>
    </div>

    <!-- 底部说明 -->
    <div v-if="callouts.length" class="code-block-footer">
      <div class="code-callouts-list">
        <span 
          v-for="(callout, index) in callouts" 
          :key="index"
          class="callout-tag"
        >
          {{ callout }}
        </span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.code-block-card {
  --code-bg: #1e1e1e;
  --code-header-bg: #252526;
  --code-border: #3e3e42;
  --code-text: #d4d4d4;
  --code-line-number: #858585;
  --code-keyword: #569cd6;
  --code-string: #ce9178;
  --code-comment: #6a9955;
  --code-number: #b5cea8;
  --code-function: #dcdcaa;
  
  border: 1px solid var(--code-border);
  border-radius: 12px;
  overflow: hidden;
  background: var(--code-bg);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transition: box-shadow 0.3s ease;
}

.code-block-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

/* 头部工具栏 */
.code-block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--code-header-bg);
  border-bottom: 1px solid var(--code-border);
}

.code-block-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-lang-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  font-size: 13px;
  color: var(--code-text);
}

.lang-icon {
  font-size: 14px;
}

.lang-name {
  font-weight: 500;
  text-transform: lowercase;
}

.code-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--code-text);
}

.code-block-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--code-border);
  border-radius: 6px;
  background: transparent;
  color: var(--code-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: #569cd6;
}

.action-btn.copied {
  color: #4ec9b0;
  border-color: #4ec9b0;
}

/* 代码内容区 */
.code-block-body {
  position: relative;
  max-height: none;
}

.code-block-body.collapsed {
  max-height: calc(var(--max-lines, 15) * 1.7em + 20px);
}

.code-wrapper {
  display: flex;
  overflow-x: auto;
  font-family: 'Fira Code', 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.7;
}

.line-numbers {
  flex-shrink: 0;
  padding: 16px 12px 16px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-right: 1px solid var(--code-border);
  text-align: right;
  user-select: none;
}

.line-number {
  display: block;
  color: var(--code-line-number);
  font-size: 13px;
  line-height: 1.7;
}

.code-content {
  flex: 1;
  margin: 0;
  padding: 16px 20px;
  background: var(--code-bg);
  color: var(--code-text);
  overflow-x: auto;
  white-space: pre;
  tab-size: 4;
}

.code-content code {
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
}

/* 语法高亮 */
:deep(.token-keyword) {
  color: var(--code-keyword);
  font-weight: 600;
}

:deep(.token-string) {
  color: var(--code-string);
}

:deep(.token-comment) {
  color: var(--code-comment);
  font-style: italic;
}

:deep(.token-number) {
  color: var(--code-number);
}

:deep(.token-function) {
  color: var(--code-function);
}

/* 展开遮罩 */
.expand-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: linear-gradient(transparent, var(--code-bg) 60%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 12px;
}

.expand-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid var(--code-border);
  border-radius: 20px;
  background: var(--code-header-bg);
  color: var(--code-text);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.expand-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #569cd6;
}

.expand-btn svg {
  transition: transform 0.2s ease;
}

/* 底部说明 */
.code-block-footer {
  padding: 12px 16px;
  background: var(--code-header-bg);
  border-top: 1px solid var(--code-border);
}

.code-callouts-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.callout-tag {
  padding: 4px 10px;
  background: rgba(86, 156, 214, 0.15);
  border: 1px solid rgba(86, 156, 214, 0.3);
  border-radius: 4px;
  color: #569cd6;
  font-size: 12px;
  line-height: 1.5;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .code-block-card {
    border-radius: 8px;
  }
  
  .code-block-header {
    padding: 10px 12px;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .code-title {
    font-size: 13px;
    width: 100%;
    margin-top: 4px;
  }
  
  .code-wrapper {
    font-size: 12px;
  }
  
  .line-numbers {
    padding: 12px 8px 12px 12px;
  }
  
  .line-number {
    font-size: 11px;
  }
  
  .code-content {
    padding: 12px 14px;
  }
  
  .action-btn span {
    display: none;
  }
  
  .action-btn {
    padding: 6px;
  }
}

@media (max-width: 480px) {
  .code-lang-badge {
    padding: 3px 8px;
    font-size: 12px;
  }
  
  .callout-tag {
    font-size: 11px;
    padding: 3px 8px;
  }
}
</style>
