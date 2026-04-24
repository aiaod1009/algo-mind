<template>
  <button
    class="magnetic-pointer-toggle"
    :class="{ 'is-enabled': isEnabled }"
    :title="isEnabled ? '点击关闭磁吸指针' : '点击开启磁吸指针'"
    @click="toggleMagneticPointer"
    :aria-label="isEnabled ? '磁吸指针已开启，点击关闭' : '磁吸指针已关闭，点击开启'"
    :aria-pressed="isEnabled"
  >
    <div class="toggle-icon">
      <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="3" />
        <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83" />
      </svg>
    </div>
    <div class="toggle-content">
      <span class="toggle-label">磁吸指针</span>
      <span class="toggle-status">{{ isEnabled ? '开启' : '关闭' }}</span>
    </div>
    <div class="toggle-indicator" :class="{ 'is-active': isEnabled }"></div>
  </button>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'

const STORAGE_KEY = 'magnetic-pointer-enabled'
const isEnabled = ref(false)

const loadState = () => {
  if (typeof window === 'undefined') {
    return false
  }

  try {
    const stored = window.localStorage.getItem(STORAGE_KEY)
    return stored === 'true'
  } catch {
    return false
  }
}

const saveState = (value) => {
  if (typeof window === 'undefined') {
    return
  }

  try {
    window.localStorage.setItem(STORAGE_KEY, String(value))
  } catch (error) {
    console.warn('Failed to save magnetic pointer state:', error)
  }
}

const toggleMagneticPointer = () => {
  isEnabled.value = !isEnabled.value
  saveState(isEnabled.value)
  dispatchToggleEvent()
}

const dispatchToggleEvent = () => {
  if (typeof window === 'undefined') {
    return
  }

  const event = new CustomEvent('magnetic-pointer-toggle', {
    detail: { enabled: isEnabled.value },
  })
  window.dispatchEvent(event)
}

watch(isEnabled, (newValue) => {
  if (typeof document !== 'undefined') {
    if (newValue) {
      document.body.classList.add('magnetic-pointer-enabled')
      document.body.classList.remove('magnetic-pointer-disabled')
    } else {
      document.body.classList.add('magnetic-pointer-disabled')
      document.body.classList.remove('magnetic-pointer-enabled')
    }
  }
})

onMounted(() => {
  isEnabled.value = loadState()
  
  if (isEnabled.value) {
    document.body.classList.add('magnetic-pointer-enabled')
  } else {
    document.body.classList.add('magnetic-pointer-disabled')
  }
  
  dispatchToggleEvent()
})
</script>

<style scoped>
.magnetic-pointer-toggle {
  position: fixed;
  bottom: 16px;
  right: 16px;
  z-index: 10000;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid rgba(135, 158, 175, 0.24);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px) saturate(140%);
  box-shadow: 
    0 8px 24px rgba(16, 42, 67, 0.12),
    0 2px 8px rgba(16, 42, 67, 0.08);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.magnetic-pointer-toggle:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 32px rgba(16, 42, 67, 0.16),
    0 4px 12px rgba(16, 42, 67, 0.1);
  border-color: rgba(30, 169, 124, 0.3);
}

.magnetic-pointer-toggle:active {
  transform: translateY(0);
  box-shadow: 
    0 4px 12px rgba(16, 42, 67, 0.12),
    0 1px 4px rgba(16, 42, 67, 0.08);
}

.magnetic-pointer-toggle:focus-visible {
  outline: 2px solid rgba(30, 169, 124, 0.6);
  outline-offset: 2px;
}

.magnetic-pointer-toggle.is-enabled {
  border-color: rgba(30, 169, 124, 0.35);
  background: linear-gradient(135deg, rgba(30, 169, 124, 0.08), rgba(255, 255, 255, 0.95));
}

.toggle-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(18, 48, 71, 0.06);
  color: #5f7286;
  transition: all 0.25s ease;
}

.is-enabled .toggle-icon {
  background: linear-gradient(135deg, rgba(30, 169, 124, 0.15), rgba(31, 148, 168, 0.1));
  color: #1ea97c;
}

.toggle-icon svg {
  transition: transform 0.3s ease;
}

.is-enabled .toggle-icon svg {
  transform: rotate(180deg);
}

.toggle-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  min-width: 60px;
}

.toggle-label {
  font-size: 13px;
  font-weight: 700;
  color: #123047;
  line-height: 1.2;
}

.toggle-status {
  font-size: 11px;
  font-weight: 600;
  color: #5f7286;
  line-height: 1.2;
}

.is-enabled .toggle-status {
  color: #1ea97c;
}

.toggle-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(18, 48, 71, 0.2);
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.toggle-indicator.is-active {
  background: #1ea97c;
  box-shadow: 0 0 8px rgba(30, 169, 124, 0.6);
}

@media (max-width: 768px) {
  .magnetic-pointer-toggle {
    padding: 10px 14px;
    gap: 10px;
    border-radius: 14px;
  }

  .toggle-icon {
    width: 32px;
    height: 32px;
  }

  .toggle-icon svg {
    width: 18px;
    height: 18px;
  }

  .toggle-label {
    font-size: 12px;
  }

  .toggle-status {
    font-size: 10px;
  }
}

@media (hover: none) {
  .magnetic-pointer-toggle {
    min-height: 48px;
    padding: 12px 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .magnetic-pointer-toggle,
  .toggle-icon,
  .toggle-icon svg,
  .toggle-indicator {
    transition: none;
  }
}
</style>
