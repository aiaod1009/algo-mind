<template>
  <div ref="pointer" class="pointer" :class="{ 'is-active': isActive, 'is-hidden': isHidden }">
    <div></div>
    <div></div>
    <div></div>
    <div></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'

const pointer = ref(null)
const currentTarget = ref(null)
const isActive = ref(false)
const isHidden = ref(false)

const move = (e) => {
  if (!e || typeof e.clientX === 'undefined' || typeof e.clientY === 'undefined') {
    return
  }
  let x = e.clientX
  let y = e.clientY
  
  // 检测鼠标是否在需要隐藏指针的区域
  let targetElement = e.target
  while (targetElement) {
    if (targetElement.classList && (
      targetElement.classList.contains('view-lines') ||
      targetElement.classList.contains('messages-container') ||
      targetElement.classList.contains('input-container') ||
      targetElement.classList.contains('helper-content')
    )) {
      isHidden.value = true
      break
    }
    targetElement = targetElement.parentElement
  }
  if (!targetElement) {
    isHidden.value = false
  }
  
  if (currentTarget.value) {
    try {
      const rect = currentTarget.value.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2
      
      x = centerX + (x - centerX) * 0.1
      y = centerY + (y - centerY) * 0.1
    } catch (error) {
      // 忽略错误，继续执行
    }
  }
  
  if (pointer.value) {
    try {
      pointer.value.style.transform = `translate(${x}px, ${y}px)`
    } catch (error) {
      // 忽略错误，继续执行
    }
  }
}

const handleMouseEnter = (e) => {
  // 不阻止事件传递
  if (!e || !e.target || typeof e.target.closest !== 'function') {
    return
  }
  const target = e.target.closest('._target, .el-sub-menu__title')
  if (target) {
    currentTarget.value = target
    isActive.value = true
    const rect = target.getBoundingClientRect()
    if (pointer.value) {
      pointer.value.style.setProperty('--width', rect.width + 'px')
      pointer.value.style.setProperty('--height', rect.height + 'px')
    }
    // 添加目标元素的高亮效果
    target.classList.add('target-highlight')
  }
}

const handleMouseLeave = (e) => {
  // 不阻止事件传递
  if (!e || !e.target || typeof e.target.closest !== 'function') {
    return
  }
  const target = e.target.closest('._target, .el-sub-menu__title')
  if (target && currentTarget.value === target) {
    currentTarget.value = null
    isActive.value = false
    if (pointer.value) {
      pointer.value.style.setProperty('--width', '4rem')
      pointer.value.style.setProperty('--height', '4rem')
    }
    // 移除目标元素的高亮效果
    target.classList.remove('target-highlight')
  }
}

// 监听isActive变化，确保样式更新
watch(isActive, (newValue) => {
  console.log('isActive changed:', newValue)
})

onMounted(() => {
  window.addEventListener('mousemove', move)
  // 使用事件委托，监听整个文档的鼠标事件
  // 使用捕获阶段，但确保不阻止事件传递
  document.addEventListener('mouseenter', handleMouseEnter, true)
  document.addEventListener('mouseleave', handleMouseLeave, true)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', move)
  document.removeEventListener('mouseenter', handleMouseEnter, true)
  document.removeEventListener('mouseleave', handleMouseLeave, true)
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
  transition: all 0.2s ease-out;
  pointer-events: none;
  z-index: 9999;
}

.pointer.is-hidden {
  opacity: 0;
  visibility: hidden;
}

.pointer div {
  position: absolute;
  width: 1rem;
  height: 1rem;
  border-width: 0.3rem;
  border-color: #17f700;
  transition: all 0.3s ease;
}

.pointer.is-active div {
  border-color: #ff6b6b !important;
  transform: scale(1) !important;
  box-shadow: 0 0 15px rgba(255, 107, 107, 0.8) !important;
  transition: all 0.3s ease !important;
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

/* 为目标元素添加样式 */
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
  pointer-events: auto;
}
</style>