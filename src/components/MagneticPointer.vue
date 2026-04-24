<template>
  <div v-if="isEnabled" ref="pointer" class="pointer" :class="{ 'is-active': isActive }" aria-hidden="true">
    <div></div>
    <div></div>
    <div></div>
    <div></div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const STORAGE_KEY = 'magnetic-pointer-enabled'
const route = useRoute()
const pointer = ref(null)
const currentTarget = ref(null)
const isActive = ref(false)
const isEnabled = ref(false)
const lastPointerPosition = ref({
  x: typeof window === 'undefined' ? 0 : Math.round(window.innerWidth / 2),
  y: typeof window === 'undefined' ? 0 : Math.round(window.innerHeight / 2),
})

const DEFAULT_POINTER_SIZE = '4rem'
const TARGET_SELECTOR = '._target, .el-sub-menu__title'

const loadEnabledState = () => {
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

const handleToggleEvent = (event) => {
  if (event?.detail && typeof event.detail.enabled === 'boolean') {
    isEnabled.value = event.detail.enabled
    if (!isEnabled.value) {
      clearCurrentTarget()
    }
  }
}

const setPointerSize = (width = DEFAULT_POINTER_SIZE, height = DEFAULT_POINTER_SIZE) => {
  if (!pointer.value) {
    return
  }

  pointer.value.style.setProperty('--width', width)
  pointer.value.style.setProperty('--height', height)
}

const updatePointerPosition = (x, y) => {
  if (!pointer.value) {
    return
  }

  pointer.value.style.transform = `translate(${x}px, ${y}px)`
}

const clearCurrentTarget = () => {
  if (currentTarget.value?.classList) {
    currentTarget.value.classList.remove('target-highlight')
  }

  currentTarget.value = null
  isActive.value = false
  setPointerSize()
}

const applyMagneticTarget = (target) => {
  if (!target || currentTarget.value === target) {
    return
  }

  clearCurrentTarget()
  currentTarget.value = target
  isActive.value = true

  const rect = target.getBoundingClientRect()
  if (rect.width > 0 && rect.height > 0) {
    setPointerSize(`${rect.width}px`, `${rect.height}px`)
  }

  target.classList.add('target-highlight')
}

const getClosestMagneticTarget = (node) => {
  if (!node || typeof node.closest !== 'function') {
    return null
  }

  return node.closest(TARGET_SELECTOR)
}

const syncTargetFromPointer = () => {
  if (typeof document === 'undefined') {
    return
  }

  const hoveredElement = document.elementFromPoint(
    lastPointerPosition.value.x,
    lastPointerPosition.value.y,
  )
  const target = getClosestMagneticTarget(hoveredElement)

  if (target) {
    applyMagneticTarget(target)
  } else {
    clearCurrentTarget()
  }

  updatePointerPosition(lastPointerPosition.value.x, lastPointerPosition.value.y)
}

const move = (event) => {
  if (!event || typeof event.clientX !== 'number' || typeof event.clientY !== 'number') {
    return
  }

  lastPointerPosition.value = {
    x: event.clientX,
    y: event.clientY,
  }

  const target = getClosestMagneticTarget(event.target)
  if (target) {
    applyMagneticTarget(target)
  } else if (currentTarget.value && !currentTarget.value.contains(event.target)) {
    clearCurrentTarget()
  }

  let x = event.clientX
  let y = event.clientY

  if (currentTarget.value) {
    if (!currentTarget.value.isConnected) {
      clearCurrentTarget()
    } else {
      const rect = currentTarget.value.getBoundingClientRect()
      if (rect.width > 0 && rect.height > 0) {
        const centerX = rect.left + rect.width / 2
        const centerY = rect.top + rect.height / 2

        x = centerX + (x - centerX) * 0.1
        y = centerY + (y - centerY) * 0.1
      } else {
        clearCurrentTarget()
      }
    }
  }

  updatePointerPosition(x, y)
}

const handleWindowBlur = () => {
  clearCurrentTarget()
}

const handleVisibilityChange = () => {
  if (document.hidden) {
    clearCurrentTarget()
  }
}

watch(
  () => route.fullPath,
  async () => {
    clearCurrentTarget()
    await nextTick()
    requestAnimationFrame(syncTargetFromPointer)
  },
)

onMounted(() => {
  isEnabled.value = loadEnabledState()

  window.addEventListener('magnetic-pointer-toggle', handleToggleEvent)

  if (!isEnabled.value) {
    return
  }

  setPointerSize()
  updatePointerPosition(lastPointerPosition.value.x, lastPointerPosition.value.y)

  window.addEventListener('pointermove', move, { passive: true })
  window.addEventListener('blur', handleWindowBlur)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  clearCurrentTarget()
  window.removeEventListener('pointermove', move)
  window.removeEventListener('blur', handleWindowBlur)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('magnetic-pointer-toggle', handleToggleEvent)
})

watch(isEnabled, async (newValue) => {
  if (newValue) {
    await nextTick()
    setPointerSize()
    updatePointerPosition(lastPointerPosition.value.x, lastPointerPosition.value.y)

    window.addEventListener('pointermove', move, { passive: true })
    window.addEventListener('blur', handleWindowBlur)
    document.addEventListener('visibilitychange', handleVisibilityChange)
  } else {
    clearCurrentTarget()
    window.removeEventListener('pointermove', move)
    window.removeEventListener('blur', handleWindowBlur)
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  }
})
</script>

<style scoped>
.pointer {
  --width: 4rem;
  --height: 4rem;
  position: fixed;
  top: calc(var(--height) / -2);
  left: calc(var(--width) / -2);
  width: var(--width);
  height: var(--height);
  transition:
    transform 0.12s ease-out,
    width 0.18s ease,
    height 0.18s ease;
  pointer-events: none;
  z-index: 10010;
}

.pointer div {
  position: absolute;
  width: 1rem;
  height: 1rem;
  border-width: 0.3rem;
  border-color: #17f700;
  transition: all 0.22s ease;
}

.pointer.is-active div {
  border-color: #ff6b6b !important;
  transform: scale(1) !important;
  box-shadow: 0 0 15px rgba(255, 107, 107, 0.8) !important;
}

.pointer div:nth-child(1) {
  top: 0;
  left: 0;
  border-top-style: solid;
  border-left-style: solid;
}

.pointer div:nth-child(2) {
  top: 0;
  right: 0;
  border-top-style: solid;
  border-right-style: solid;
}

.pointer div:nth-child(3) {
  bottom: 0;
  left: 0;
  border-bottom-style: solid;
  border-left-style: solid;
}

.pointer div:nth-child(4) {
  bottom: 0;
  right: 0;
  border-bottom-style: solid;
  border-right-style: solid;
}

:global(._target),
:global(.el-sub-menu__title) {
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

:global(.target-highlight) {
  transform: scale(1);
  box-shadow: 0 0 20px rgba(255, 107, 107, 0.6);
  border-radius: 4px;
}
</style>
