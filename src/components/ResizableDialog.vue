<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '对话框'
  },
  minWidth: {
    type: Number,
    default: 400
  },
  minHeight: {
    type: Number,
    default: 300
  },
  maxWidth: {
    type: Number,
    default: 1200
  },
  maxHeight: {
    type: Number,
    default: 900
  },
  defaultWidth: {
    type: Number,
    default: 640
  },
  defaultHeight: {
    type: Number,
    default: 480
  },
  storageKey: {
    type: String,
    default: 'resizable-dialog-size'
  },
  showSizeInput: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'resize', 'close'])

// 尺寸状态
const dialogWidth = ref(props.defaultWidth)
const dialogHeight = ref(props.defaultHeight)
const isResizing = ref(false)
const resizeDirection = ref('')
const showSizeEditor = ref(false)

// 输入框值
const widthInput = ref(props.defaultWidth)
const heightInput = ref(props.defaultHeight)

// 拖拽开始位置
const startX = ref(0)
const startY = ref(0)
const startWidth = ref(0)
const startHeight = ref(0)

// 对话框引用
const dialogRef = ref(null)
const resizeHandles = ref([])

// 可见性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 从 localStorage 加载保存的尺寸
const loadSavedSize = () => {
  if (!props.storageKey) return
  try {
    const saved = localStorage.getItem(props.storageKey)
    if (saved) {
      const { width, height } = JSON.parse(saved)
      dialogWidth.value = Math.max(props.minWidth, Math.min(props.maxWidth, width))
      dialogHeight.value = Math.max(props.minHeight, Math.min(props.maxHeight, height))
      widthInput.value = dialogWidth.value
      heightInput.value = dialogHeight.value
    }
  } catch (e) {
    console.warn('Failed to load dialog size:', e)
  }
}

// 保存尺寸到 localStorage
const saveSize = () => {
  if (!props.storageKey) return
  try {
    localStorage.setItem(props.storageKey, JSON.stringify({
      width: dialogWidth.value,
      height: dialogHeight.value
    }))
  } catch (e) {
    console.warn('Failed to save dialog size:', e)
  }
}

// 开始拖拽调整大小
const startResize = (e, direction) => {
  e.preventDefault()
  e.stopPropagation()
  
  isResizing.value = true
  resizeDirection.value = direction
  startX.value = e.clientX
  startY.value = e.clientY
  startWidth.value = dialogWidth.value
  startHeight.value = dialogHeight.value
  
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.body.style.cursor = getResizeCursor(direction)
  document.body.style.userSelect = 'none'
}

// 处理拖拽调整大小
const handleResize = (e) => {
  if (!isResizing.value) return
  
  const deltaX = e.clientX - startX.value
  const deltaY = e.clientY - startY.value
  
  let newWidth = startWidth.value
  let newHeight = startHeight.value
  
  // 根据拖拽方向调整尺寸
  if (resizeDirection.value.includes('e')) {
    newWidth = startWidth.value + deltaX
  }
  if (resizeDirection.value.includes('w')) {
    newWidth = startWidth.value - deltaX
  }
  if (resizeDirection.value.includes('s')) {
    newHeight = startHeight.value + deltaY
  }
  if (resizeDirection.value.includes('n')) {
    newHeight = startHeight.value - deltaY
  }
  
  // 应用尺寸限制
  dialogWidth.value = Math.max(props.minWidth, Math.min(props.maxWidth, newWidth))
  dialogHeight.value = Math.max(props.minHeight, Math.min(props.maxHeight, newHeight))
  
  // 更新输入框值
  widthInput.value = Math.round(dialogWidth.value)
  heightInput.value = Math.round(dialogHeight.value)
  
  emit('resize', { width: dialogWidth.value, height: dialogHeight.value })
}

// 停止拖拽调整大小
const stopResize = () => {
  isResizing.value = false
  resizeDirection.value = ''
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  saveSize()
}

// 获取拖拽光标样式
const getResizeCursor = (direction) => {
  const cursors = {
    'n': 'ns-resize',
    's': 'ns-resize',
    'e': 'ew-resize',
    'w': 'ew-resize',
    'ne': 'nesw-resize',
    'nw': 'nwse-resize',
    'se': 'nwse-resize',
    'sw': 'nesw-resize'
  }
  return cursors[direction] || 'default'
}

// 应用精确尺寸输入
const applySizeInput = () => {
  const newWidth = parseInt(widthInput.value) || props.defaultWidth
  const newHeight = parseInt(heightInput.value) || props.defaultHeight
  
  dialogWidth.value = Math.max(props.minWidth, Math.min(props.maxWidth, newWidth))
  dialogHeight.value = Math.max(props.minHeight, Math.min(props.maxHeight, newHeight))
  
  widthInput.value = dialogWidth.value
  heightInput.value = dialogHeight.value
  
  showSizeEditor.value = false
  saveSize()
  emit('resize', { width: dialogWidth.value, height: dialogHeight.value })
}

// 重置为默认尺寸
const resetSize = () => {
  dialogWidth.value = props.defaultWidth
  dialogHeight.value = props.defaultHeight
  widthInput.value = props.defaultWidth
  heightInput.value = props.defaultHeight
  saveSize()
  emit('resize', { width: dialogWidth.value, height: dialogHeight.value })
}

// 关闭对话框
const closeDialog = () => {
  visible.value = false
  emit('close')
}

// 监听可见性变化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadSavedSize()
    nextTick(() => {
      widthInput.value = dialogWidth.value
      heightInput.value = dialogHeight.value
    })
  }
})

onMounted(() => {
  loadSavedSize()
})

onUnmounted(() => {
  if (isResizing.value) {
    stopResize()
  }
})
</script>

<template>
  <Teleport to="body">
    <Transition name="dialog-fade">
      <div v-if="visible" class="resizable-dialog-overlay" @click.self="closeDialog">
        <Transition name="dialog-scale">
          <div
            v-if="visible"
            ref="dialogRef"
            class="resizable-dialog"
            :class="{ 'is-resizing': isResizing }"
            :style="{
              width: dialogWidth + 'px',
              height: dialogHeight + 'px'
            }"
          >
            <!-- 头部 -->
            <header class="dialog-header">
              <div class="dialog-title-wrapper">
                <h2 class="dialog-title">{{ title }}</h2>
                <div v-if="showSizeEditor" class="size-editor">
                  <input
                    v-model.number="widthInput"
                    type="number"
                    class="size-input"
                    :min="minWidth"
                    :max="maxWidth"
                    @keyup.enter="applySizeInput"
                  />
                  <span class="size-separator">×</span>
                  <input
                    v-model.number="heightInput"
                    type="number"
                    class="size-input"
                    :min="minHeight"
                    :max="maxHeight"
                    @keyup.enter="applySizeInput"
                  />
                  <button class="size-apply-btn" @click="applySizeInput">应用</button>
                  <button class="size-reset-btn" @click="resetSize">重置</button>
                </div>
                <button
                  v-else-if="showSizeInput"
                  class="size-toggle-btn"
                  @click="showSizeEditor = true"
                  title="调整尺寸"
                >
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <line x1="9" y1="3" x2="9" y2="21"/>
                    <line x1="15" y1="3" x2="15" y2="21"/>
                    <line x1="3" y1="9" x2="21" y2="9"/>
                    <line x1="3" y1="15" x2="21" y2="15"/>
                  </svg>
                  {{ Math.round(dialogWidth) }} × {{ Math.round(dialogHeight) }}
                </button>
              </div>
              <button class="dialog-close-btn" @click="closeDialog">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </header>

            <!-- 内容区 -->
            <div class="dialog-content">
              <slot />
            </div>

            <!-- 底部 -->
            <footer v-if="$slots.footer" class="dialog-footer">
              <slot name="footer" />
            </footer>

            <!-- 调整大小手柄 -->
            <div
              v-for="direction in ['n', 's', 'e', 'w', 'ne', 'nw', 'se', 'sw']"
              :key="direction"
              :ref="(el) => { if (el) resizeHandles[direction] = el }"
              :class="['resize-handle', `resize-${direction}`]"
              @mousedown="(e) => startResize(e, direction)"
            />
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 遮罩层 */
.resizable-dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8px);
}

/* 对话框主体 */
.resizable-dialog {
  position: relative;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.35),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
  min-width: v-bind(minWidth + 'px');
  min-height: v-bind(minHeight + 'px');
  max-width: v-bind(maxWidth + 'px');
  max-height: v-bind(maxHeight + 'px');
}

.resizable-dialog.is-resizing {
  box-shadow: 
    0 35px 60px -15px rgba(0, 0, 0, 0.4),
    0 0 0 2px rgba(79, 70, 229, 0.3) inset;
  user-select: none;
}

/* 头部 */
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  flex-shrink: 0;
}

.dialog-title-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dialog-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

/* 尺寸编辑器 */
.size-editor {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.size-input {
  width: 60px;
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  text-align: center;
  background: white;
  transition: all 0.2s ease;
}

.size-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.size-separator {
  color: #94a3b8;
  font-weight: 500;
}

.size-apply-btn,
.size-reset-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.size-apply-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
}

.size-apply-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.size-reset-btn {
  background: rgba(226, 232, 240, 0.8);
  color: #64748b;
}

.size-reset-btn:hover {
  background: rgba(203, 213, 225, 0.8);
  color: #475569;
}

/* 尺寸显示按钮 */
.size-toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  background: rgba(241, 245, 249, 0.6);
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.size-toggle-btn:hover {
  background: rgba(226, 232, 240, 0.8);
  color: #475569;
  border-color: #cbd5e1;
}

.size-toggle-btn svg {
  opacity: 0.7;
}

/* 关闭按钮 */
.dialog-close-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(241, 245, 249, 0.8);
  border-radius: 12px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dialog-close-btn:hover {
  background: rgba(226, 232, 240, 0.8);
  color: #0f172a;
  transform: rotate(90deg);
}

/* 内容区 */
.dialog-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.5) transparent;
}

.dialog-content::-webkit-scrollbar {
  width: 6px;
}

.dialog-content::-webkit-scrollbar-track {
  background: transparent;
}

.dialog-content::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.5);
  border-radius: 3px;
}

/* 底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background: linear-gradient(180deg, #fafbfc 0%, #f8fafc 100%);
  border-top: 1px solid rgba(226, 232, 240, 0.8);
  flex-shrink: 0;
}

/* 调整大小手柄 */
.resize-handle {
  position: absolute;
  z-index: 10;
}

/* 四边手柄 */
.resize-n {
  top: 0;
  left: 10px;
  right: 10px;
  height: 6px;
  cursor: ns-resize;
}

.resize-s {
  bottom: 0;
  left: 10px;
  right: 10px;
  height: 6px;
  cursor: ns-resize;
}

.resize-e {
  right: 0;
  top: 10px;
  bottom: 10px;
  width: 6px;
  cursor: ew-resize;
}

.resize-w {
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 6px;
  cursor: ew-resize;
}

/* 四角手柄 */
.resize-ne {
  top: 0;
  right: 0;
  width: 14px;
  height: 14px;
  cursor: nesw-resize;
}

.resize-nw {
  top: 0;
  left: 0;
  width: 14px;
  height: 14px;
  cursor: nwse-resize;
}

.resize-se {
  bottom: 0;
  right: 0;
  width: 14px;
  height: 14px;
  cursor: nwse-resize;
}

.resize-sw {
  bottom: 0;
  left: 0;
  width: 14px;
  height: 14px;
  cursor: nesw-resize;
}

/* 手柄悬停效果 */
.resize-handle::after {
  content: '';
  position: absolute;
  background: transparent;
  transition: background 0.2s ease;
}

.resize-n::after,
.resize-s::after {
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 4px;
  border-radius: 2px;
}

.resize-e::after,
.resize-w::after {
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 40px;
  border-radius: 2px;
}

.resize-ne::after,
.resize-nw::after,
.resize-se::after,
.resize-sw::after {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.resize-handle:hover::after {
  background: rgba(79, 70, 229, 0.5);
}

.resizable-dialog.is-resizing .resize-handle::after {
  background: rgba(79, 70, 229, 0.8);
}

/* 过渡动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.3s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

.dialog-scale-enter-active,
.dialog-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.dialog-scale-enter-from,
.dialog-scale-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(20px);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .resizable-dialog-overlay {
    padding: 10px;
  }
  
  .resizable-dialog {
    width: 100% !important;
    height: 90vh !important;
    max-width: 100% !important;
    max-height: 90vh !important;
  }
  
  .dialog-header {
    padding: 16px 20px;
  }
  
  .dialog-title {
    font-size: 16px;
  }
  
  .dialog-content {
    padding: 16px;
  }
  
  .size-editor {
    padding: 4px 8px;
    gap: 6px;
  }
  
  .size-input {
    width: 50px;
    padding: 3px 6px;
    font-size: 12px;
  }
  
  .size-apply-btn,
  .size-reset-btn {
    padding: 3px 8px;
    font-size: 11px;
  }
  
  /* 移动端隐藏调整大小手柄 */
  .resize-handle {
    display: none;
  }
}

@media (max-width: 480px) {
  .dialog-title-wrapper {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .size-toggle-btn {
    font-size: 11px;
    padding: 4px 8px;
  }
}
</style>
