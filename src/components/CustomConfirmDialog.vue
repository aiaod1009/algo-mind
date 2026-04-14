<template>
  <Teleport to="body">
    <Transition name="dialog-fade">
      <div v-if="visible" class="custom-dialog-overlay" @click="handleOverlayClick">
        <div class="custom-dialog" :class="`type-${type}`" @click.stop>
          <div class="dialog-header">
            <div class="dialog-icon">
              <svg v-if="type === 'info'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 16v-4M12 8h.01"/>
              </svg>
              <svg v-else-if="type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              <svg v-else-if="type === 'warning'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <svg v-else-if="type === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
            </div>
            <h3 class="dialog-title">{{ title }}</h3>
            <button class="dialog-close" @click="handleCancel">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <div class="dialog-body">
            <p class="dialog-message">{{ message }}</p>
          </div>
          <div class="dialog-footer">
            <button class="btn btn-cancel" @click="handleCancel">{{ cancelText }}</button>
            <button class="btn btn-confirm" :class="`btn-${type}`" @click="handleConfirm">{{ confirmText }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, defineExpose } from 'vue'

const visible = ref(false)
const title = ref('')
const message = ref('')
const type = ref('info')
const confirmText = ref('确认')
const cancelText = ref('取消')
let resolvePromise = null
let rejectPromise = null

const show = (options) => {
  title.value = options.title || '提示'
  message.value = options.message || ''
  type.value = options.type || 'info'
  confirmText.value = options.confirmText || '确认'
  cancelText.value = options.cancelText || '取消'
  visible.value = true
  
  return new Promise((resolve, reject) => {
    resolvePromise = resolve
    rejectPromise = reject
  })
}

const handleConfirm = () => {
  visible.value = false
  resolvePromise?.('confirm')
}

const handleCancel = () => {
  visible.value = false
  rejectPromise?.('cancel')
}

const handleOverlayClick = () => {
  handleCancel()
}

defineExpose({
  show
})
</script>

<style scoped>
.custom-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.custom-dialog {
  background: white;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  width: 100%;
  max-width: 420px;
  overflow: hidden;
  animation: dialogSlideIn 0.3s ease;
}

@keyframes dialogSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 20px 0;
  position: relative;
}

.dialog-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-icon svg {
  width: 22px;
  height: 22px;
}

.type-info .dialog-icon {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #2563eb;
}

.type-success .dialog-icon {
  background: linear-gradient(135deg, #dcfce7 0%, #bbf7d0 100%);
  color: #16a34a;
}

.type-warning .dialog-icon {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #d97706;
}

.type-error .dialog-icon {
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  color: #dc2626;
}

.dialog-title {
  flex: 1;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.dialog-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  transition: all 0.2s;
}

.dialog-close:hover {
  background: #f1f5f9;
  color: #64748b;
}

.dialog-close svg {
  width: 18px;
  height: 18px;
}

.dialog-body {
  padding: 16px 20px 20px 72px;
}

.dialog-message {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  padding: 0 20px 20px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-cancel {
  background: #f1f5f9;
  color: #64748b;
}

.btn-cancel:hover {
  background: #e2e8f0;
  color: #475569;
}

.btn-confirm {
  color: white;
}

.btn-info {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.btn-info:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.btn-success {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
}

.btn-success:hover {
  background: linear-gradient(135deg, #16a34a 0%, #15803d 100%);
  box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3);
}

.btn-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.btn-warning:hover {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
}

.btn-error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.btn-error:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

/* Transition animations */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.3s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

.dialog-fade-enter-active .custom-dialog,
.dialog-fade-leave-active .custom-dialog {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.dialog-fade-enter-from .custom-dialog,
.dialog-fade-leave-to .custom-dialog {
  transform: translateY(-20px) scale(0.95);
  opacity: 0;
}
</style>
