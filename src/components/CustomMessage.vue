<template>
  <Teleport to="body">
    <TransitionGroup name="message" tag="div" class="custom-message-container">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="custom-message"
        :class="[`type-${msg.type}`, { 'is-closing': msg.closing }]"
        @mouseenter="pauseTimer(msg)"
        @mouseleave="resumeTimer(msg)"
      >
        <div class="message-glow"></div>
        <div class="message-content">
          <div class="message-icon">
            <svg v-if="msg.type === 'info'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 16v-4M12 8h.01"/>
            </svg>
            <svg v-else-if="msg.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
            <svg v-else-if="msg.type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
              <line x1="12" y1="9" x2="12" y2="13"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            <svg v-else-if="msg.type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <span class="message-text">{{ msg.message }}</span>
        </div>
        <div class="message-progress">
          <div class="progress-bar" :style="{ width: msg.progress + '%' }"></div>
        </div>
        <button class="message-close" @click="close(msg.id)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup>
import { ref, reactive } from 'vue'

const messages = reactive([])
let idCounter = 0

const show = (message, type = 'info', duration = 3000) => {
  const id = ++idCounter
  const msg = {
    id,
    message,
    type,
    duration,
    progress: 100,
    closing: false,
    timer: null,
    startTime: Date.now(),
    remaining: duration
  }
  
  messages.push(msg)
  
  // 启动进度动画
  startProgress(msg)
  
  // 自动关闭
  msg.timer = setTimeout(() => {
    close(id)
  }, duration)
  
  return id
}

const startProgress = (msg) => {
  const updateProgress = () => {
    if (msg.closing) return
    const elapsed = Date.now() - msg.startTime
    msg.progress = Math.max(0, 100 - (elapsed / msg.duration * 100))
    if (msg.progress > 0) {
      requestAnimationFrame(updateProgress)
    }
  }
  requestAnimationFrame(updateProgress)
}

const pauseTimer = (msg) => {
  if (msg.timer) {
    clearTimeout(msg.timer)
    msg.timer = null
    msg.remaining = msg.duration - (Date.now() - msg.startTime)
  }
}

const resumeTimer = (msg) => {
  if (!msg.timer && msg.remaining > 0) {
    msg.startTime = Date.now() - (msg.duration - msg.remaining)
    msg.timer = setTimeout(() => {
      close(msg.id)
    }, msg.remaining)
    startProgress(msg)
  }
}

const close = (id) => {
  const msg = messages.find(m => m.id === id)
  if (msg) {
    msg.closing = true
    if (msg.timer) {
      clearTimeout(msg.timer)
      msg.timer = null
    }
    setTimeout(() => {
      const index = messages.findIndex(m => m.id === id)
      if (index > -1) {
        messages.splice(index, 1)
      }
    }, 300)
  }
}

const info = (message, duration) => show(message, 'info', duration)
const success = (message, duration) => show(message, 'success', duration)
const warning = (message, duration) => show(message, 'warning', duration)
const error = (message, duration) => show(message, 'error', duration)

defineExpose({
  show,
  info,
  success,
  warning,
  error,
  close
})
</script>

<style scoped>
.custom-message-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.custom-message {
  position: relative;
  min-width: 280px;
  max-width: 480px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 
    0 10px 40px rgba(0, 0, 0, 0.1),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  pointer-events: auto;
  overflow: hidden;
  animation: messageSlideIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.custom-message.is-closing {
  animation: messageSlideOut 0.3s ease forwards;
}

@keyframes messageSlideOut {
  to {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
}

.message-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0.8;
}

.type-info .message-glow {
  background: linear-gradient(90deg, #3b82f6, #60a5fa, #3b82f6);
  background-size: 200% 100%;
  animation: glowPulse 2s ease infinite;
}

.type-success .message-glow {
  background: linear-gradient(90deg, #22c55e, #4ade80, #22c55e);
  background-size: 200% 100%;
  animation: glowPulse 2s ease infinite;
}

.type-warning .message-glow {
  background: linear-gradient(90deg, #f59e0b, #fbbf24, #f59e0b);
  background-size: 200% 100%;
  animation: glowPulse 2s ease infinite;
}

.type-error .message-glow {
  background: linear-gradient(90deg, #ef4444, #f87171, #ef4444);
  background-size: 200% 100%;
  animation: glowPulse 2s ease infinite;
}

@keyframes glowPulse {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.message-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.message-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-icon svg {
  width: 20px;
  height: 20px;
}

.type-info .message-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.type-success .message-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.type-warning .message-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.type-error .message-icon {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.message-text {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
  line-height: 1.5;
}

.message-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: rgba(0, 0, 0, 0.05);
}

.progress-bar {
  height: 100%;
  transition: width 0.1s linear;
}

.type-info .progress-bar {
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}

.type-success .progress-bar {
  background: linear-gradient(90deg, #22c55e, #4ade80);
}

.type-warning .progress-bar {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}

.type-error .progress-bar {
  background: linear-gradient(90deg, #ef4444, #f87171);
}

.message-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  opacity: 0;
  transition: all 0.2s;
}

.custom-message:hover .message-close {
  opacity: 1;
}

.message-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #64748b;
}

.message-close svg {
  width: 14px;
  height: 14px;
}

/* Transition Group Animations */
.message-enter-active,
.message-leave-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.message-enter-from {
  opacity: 0;
  transform: translateY(-30px) scale(0.9);
}

.message-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.message-move {
  transition: transform 0.3s ease;
}
</style>
