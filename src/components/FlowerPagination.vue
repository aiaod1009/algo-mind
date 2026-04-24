<template>
  <div class="flower-pagination-wrapper" ref="wrapperRef" @click="closeFlower">
    
    <div class="basic-pagination" :class="{ 'is-hidden': isExpanded }">
      <div class="box btn-side" @click.stop="changePage(-1)">上一页</div>
      <div class="box btn-center" @click.stop="openFlower">
        <span>{{ currentPage }}</span>
      </div>
      <div class="box btn-side" @click.stop="changePage(1)">下一页</div>
    </div>

    <div 
      class="flower-container" 
      :class="{ 'is-active': isExpanded }"
      @wheel.prevent="handleWheel"
    >
      <div class="flower-wheel">
        <div 
          v-for="(page, index) in totalPages" 
          :key="page"
          class="petal"
          :class="{ 'is-active': activeWheelPage === page }"
          :style="getPetalStyle(index)"
        >
          <div class="petal-content" @click.stop="selectPetal(page)">
            {{ page }}
          </div>
        </div>
      </div>

      <transition-group 
        name="fall" 
        tag="div" 
        class="falling-container"
        @after-enter="removeFallingPetal"
      >
        <div 
          v-for="petal in fallingPetals" 
          :key="petal.id" 
          class="falling-petal"
          :data-id="petal.id"
        >
          {{ petal.page }}
        </div>
      </transition-group>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  total: { type: Number, default: 20 },
  defaultPage: { type: Number, default: 1 }
})

const emit = defineEmits(['change'])

const wrapperRef = ref(null)
const isExpanded = ref(false)
const currentPage = ref(props.defaultPage)

const totalPages = computed(() => props.total)

const wheelRotation = ref(0)
const degreesPerPetal = 30
const activeWheelPage = ref(props.defaultPage)
const fallingPetals = ref([])

const ELLIPSE_RX = 85
const ELLIPSE_RY = 38

let suppressFalling = false

const changePage = (step) => {
  let next = currentPage.value + step
  if (next < 1) next = 1
  if (next > totalPages.value) next = totalPages.value
  currentPage.value = next
  emit('change', next)
}

const syncPageState = (page) => {
  const safePage = Math.min(Math.max(page || 1, 1), totalPages.value || 1)
  currentPage.value = safePage
  activeWheelPage.value = safePage
  wheelRotation.value = -(safePage - 1) * degreesPerPetal
}

const openFlower = () => {
  suppressFalling = true
  activeWheelPage.value = currentPage.value
  wheelRotation.value = -(currentPage.value - 1) * degreesPerPetal
  isExpanded.value = true
  nextTick(() => {
    suppressFalling = false
  })
}

const closeFlower = () => {
  if (!isExpanded.value) return
  isExpanded.value = false
  currentPage.value = activeWheelPage.value
}

const selectPetal = (page) => {
  activeWheelPage.value = page
  wheelRotation.value = -(page - 1) * degreesPerPetal
  setTimeout(() => {
    currentPage.value = page
    emit('change', page)
    isExpanded.value = false
  }, 150)
}

const getPetalStyle = (index) => {
  const baseAngle = index * degreesPerPetal
  const displayAngle = baseAngle + wheelRotation.value
  const displayRad = displayAngle * Math.PI / 180

  const x = ELLIPSE_RX * Math.sin(displayRad)
  const y = -ELLIPSE_RY * Math.cos(displayRad)

  const isVisible = Math.abs(displayAngle) <= 110

  return {
    transform: `translate(${x - 22}px, ${y}px)`,
    opacity: isVisible ? 1 : 0,
    visibility: isVisible ? 'visible' : 'hidden',
    pointerEvents: isVisible ? 'auto' : 'none',
    zIndex: isVisible ? Math.round(-y * 10) : -1
  }
}

let isScrolling = false
const handleWheel = (e) => {
  if (!isExpanded.value || isScrolling) return
  isScrolling = true
  setTimeout(() => { isScrolling = false }, 100)

  const delta = e.deltaY > 0 ? -degreesPerPetal : degreesPerPetal
  let newRotation = wheelRotation.value + delta

  const maxRotation = 0
  const minRotation = -(totalPages.value - 1) * degreesPerPetal
  
  if (newRotation > maxRotation) newRotation = maxRotation
  if (newRotation < minRotation) newRotation = minRotation

  wheelRotation.value = newRotation

  const currentIndex = Math.round(Math.abs(wheelRotation.value) / degreesPerPetal)
  activeWheelPage.value = currentIndex + 1
}

watch(activeWheelPage, (newVal, oldVal) => {
  if (newVal !== oldVal && isExpanded.value && !suppressFalling) {
    spawnFallingPetal(oldVal)
  }
})

watch(
  () => props.defaultPage,
  (page) => {
    syncPageState(page)
  },
)

watch(
  () => props.total,
  () => {
    syncPageState(currentPage.value)
  },
)

let petalIdCounter = 0

const spawnFallingPetal = (pageNum) => {
  petalIdCounter++
  const id = `fp-${petalIdCounter}`
  fallingPetals.value.push({ id, page: pageNum })
  setTimeout(() => {
    fallingPetals.value = fallingPetals.value.filter(p => p.id !== id)
  }, 1000)
}

const removeFallingPetal = (el) => {
  const id = el.getAttribute('data-id')
  fallingPetals.value = fallingPetals.value.filter(p => p.id !== id)
}

const handleClickOutside = (e) => {
  if (wrapperRef.value && !wrapperRef.value.contains(e.target)) {
    closeFlower()
  }
}

onMounted(() => {
  syncPageState(props.defaultPage)
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<style scoped>
.flower-pagination-wrapper {
  position: relative;
  min-width: 260px;
  height: 100px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 20px;
}

.basic-pagination {
  display: flex;
  gap: 15px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 2;
}

.box {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #333;
  transition: all 0.3s;
  user-select: none;
}

.box:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.15);
}

.btn-side {
  width: 80px;
  height: 40px;
  font-size: 14px;
}

.btn-center {
  width: 50px;
  height: 40px;
  background: #4A90E2;
  color: white;
}

.basic-pagination.is-hidden {
  pointer-events: none; 
}

.basic-pagination:not(.is-hidden) .box {
  pointer-events: auto;
}

.basic-pagination.is-hidden .btn-side:first-child {
  transform: translateX(65px) scale(0.5);
  opacity: 0;
  pointer-events: none;
}
.basic-pagination.is-hidden .btn-side:last-child {
  transform: translateX(-65px) scale(0.5);
  opacity: 0;
  pointer-events: none;
}
.basic-pagination.is-hidden .btn-center {
  opacity: 0;
  transform: scale(0);
  pointer-events: none;
}

.flower-container {
  position: absolute;
  bottom: 20px;
  left: 50%;
  margin-left: -110px;
  width: 220px;
  height: 80px;
  opacity: 0;
  transform: translateY(20px) scale(0.8);
  pointer-events: none;
  transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  z-index: 1;
}

.flower-container.is-active {
  opacity: 1;
  transform: translateY(0) scale(1);
  pointer-events: auto;
}

.flower-wheel {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 0;
}

.petal {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 44px;
  height: 26px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.15s ease-out, opacity 0.2s;
}

.petal-content {
  width: 100%;
  height: 100%;
  background: #fff;
  border-radius: 22px 22px 6px 6px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-weight: bold;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.petal.is-active .petal-content {
  background: #4A90E2;
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.4);
}

.falling-container {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 0;
}

.falling-petal {
  position: absolute;
  bottom: 30px;
  left: -22px;
  width: 44px;
  height: 26px;
  background: #ddd;
  color: #666;
  border-radius: 22px 22px 6px 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  pointer-events: none;
}

.fall-enter-active {
  animation: petal-fall 0.8s cubic-bezier(0.55, 0.085, 0.68, 0.53) forwards;
}

@keyframes petal-fall {
  0% {
    transform: translateY(0) scale(1) rotate(5deg);
    opacity: 1;
  }
  20% {
    transform: translateY(8px) scale(0.6) rotate(-10deg);
    opacity: 0.8;
  }
  100% {
    transform: translateY(45px) scale(0.2) rotate(45deg);
    opacity: 0;
  }
}
</style>
