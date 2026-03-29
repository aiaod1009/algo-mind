<template>
  <div class="search-bar-container" :class="{ 'is-sticky': isSticky }">
    <div class="search-bar-wrapper">
      <div class="search-main-row">
        <div class="search-input-container">
          <div class="search-input-wrapper">
            <input 
              type="text" 
              class="search-input" 
              v-model="searchKeyword"
              :placeholder="placeholder"
              @input="handleSearch"
              @focus="isFocused = true"
              @blur="isFocused = false"
            />
            <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="14" height="14">
                <path fill="currentColor" d="M764.288 214.592 512 466.88 259.712 214.592a31.936 31.936 0 0 0-45.12 45.12L466.752 512 214.528 764.224a31.936 31.936 0 1 0 45.12 45.184L512 557.184l252.288 252.288a31.936 31.936 0 0 0 45.12-45.12L557.12 512.064l252.288-252.352a31.936 31.936 0 1 0-45.12-45.184z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
      
      <div class="filter-options" :class="{ 'is-expanded': showFilters }">
        <div class="search-dropdown-container">
          <div class="search-dropdown-trigger" @click="toggleDropdown('sort')">
            <div class="trigger-content">
              <span class="trigger-text">{{ currentSort.label }}</span>
            </div>
            <svg class="dropdown-arrow" :class="{ 'is-open': openDropdown === 'sort' }" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="12" height="12">
              <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"/>
            </svg>
          </div>
          <Transition name="dropdown">
            <div v-if="openDropdown === 'sort'" class="dropdown-menu">
              <div 
                v-for="item in sortOptions" 
                :key="item.value" 
                class="dropdown-item"
                :class="{ 'is-active': currentSort.value === item.value }"
                @click="selectSort(item)"
              >
                {{ item.label }}
              </div>
            </div>
          </Transition>
        </div>
        
        <div class="search-dropdown-container">
          <div class="search-dropdown-trigger" @click="toggleDropdown('category')">
            <div class="trigger-content">
              <i class="el-icon trigger-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="14" height="14">
                  <path fill="currentColor" d="M160 160v704h704V160zm-32-64h768a32 32 0 0 1 32 32v768a32 32 0 0 1-32 32H128a32 32 0 0 1-32-32V128a32 32 0 0 1 32-32"/>
                  <path fill="currentColor" d="M384 288q64 0 64 64t-64 64-64-64 64-64M185.408 876.992l-50.816-38.912L350.72 556.032a96 96 0 0 1 134.592-17.856l1.856 1.472 122.88 99.136a32 32 0 0 0 44.992-4.864l216-269.888 49.92 39.936-215.808 269.824-.256.32a96 96 0 0 1-135.04 14.464l-122.88-99.072-.64-.512a32 32 0 0 0-44.8 5.952z"/>
                </svg>
              </i>
              <span class="trigger-text">{{ currentCategory.label }}</span>
            </div>
            <svg class="dropdown-arrow" :class="{ 'is-open': openDropdown === 'category' }" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="12" height="12">
              <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"/>
            </svg>
          </div>
          <Transition name="dropdown">
            <div v-if="openDropdown === 'category'" class="dropdown-menu">
              <div 
                v-for="item in categoryOptions" 
                :key="item.value" 
                class="dropdown-item"
                :class="{ 'is-active': currentCategory.value === item.value }"
                @click="selectCategory(item)"
              >
                {{ item.label }}
              </div>
            </div>
          </Transition>
        </div>
        
        <div class="search-dropdown-container">
          <div class="search-dropdown-trigger" @click="toggleDropdown('time')">
            <div class="trigger-content">
              <i class="el-icon trigger-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="14" height="14">
                  <path fill="currentColor" d="M128 384a512 512 0 1 0 512-512 512 512 0 0 0-512 512m544 0a32 32 0 0 1-32 32H448a32 32 0 0 1-32-32V192a32 32 0 0 1 64 0v160h160a32 32 0 0 1 32 32"/>
                </svg>
              </i>
              <span class="trigger-text">{{ currentTime.label }}</span>
            </div>
            <svg class="dropdown-arrow" :class="{ 'is-open': openDropdown === 'time' }" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="12" height="12">
              <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"/>
            </svg>
          </div>
          <Transition name="dropdown">
            <div v-if="openDropdown === 'time'" class="dropdown-menu">
              <div 
                v-for="item in timeOptions" 
                :key="item.value" 
                class="dropdown-item"
                :class="{ 'is-active': currentTime.value === item.value }"
                @click="selectTime(item)"
              >
                {{ item.label }}
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索帖子、话题...'
  },
  isSticky: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['search', 'filter-change'])

const searchKeyword = ref('')
const isFocused = ref(false)
const showFilters = ref(true)
const openDropdown = ref(null)

const sortOptions = [
  { label: '最新发布', value: 'latest' },
  { label: '昨日热门', value: 'yesterday' },
  { label: '本周热门', value: 'week' },
  { label: '本月热门', value: 'month' },
  { label: '全部热门', value: 'all' }
]

const categoryOptions = [
  { label: '全部类型', value: 'all' },
  { label: '学习交流', value: 'study' },
  { label: '算法刷题', value: 'algorithm' },
  { label: '面试经验', value: 'interview' },
  { label: '职场成长', value: 'career' },
  { label: '技术讨论', value: 'tech' }
]

const timeOptions = [
  { label: '全部时间', value: 'all' },
  { label: '今天', value: 'today' },
  { label: '最近三天', value: '3days' },
  { label: '最近一周', value: 'week' },
  { label: '最近一月', value: 'month' }
]

const currentSort = ref(sortOptions[0])
const currentCategory = ref(categoryOptions[0])
const currentTime = ref(timeOptions[0])

const handleSearch = () => {
  emit('search', {
    keyword: searchKeyword.value,
    sort: currentSort.value.value,
    category: currentCategory.value.value,
    time: currentTime.value.value
  })
}

const clearSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

const toggleDropdown = (name) => {
  openDropdown.value = openDropdown.value === name ? null : name
}

const selectSort = (item) => {
  currentSort.value = item
  openDropdown.value = null
  handleSearch()
}

const selectCategory = (item) => {
  currentCategory.value = item
  openDropdown.value = null
  handleSearch()
}

const selectTime = (item) => {
  currentTime.value = item
  openDropdown.value = null
  handleSearch()
}

const handleClickOutside = (e) => {
  if (!e.target.closest('.search-dropdown-container')) {
    openDropdown.value = null
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.search-bar-container {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 12px;
  padding: 12px 16px;
  border: 1px solid var(--line-soft);
  transition: all 0.3s ease;
  margin: 4px 0;
}

.search-bar-container.is-sticky {
  position: sticky;
  top: 80px;
  z-index: 100;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.search-bar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-main-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input-container {
  flex: 1;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  width: 100%;
  height: 44px;
  padding: 0 40px 0 16px;
  border: 2px solid var(--line-soft);
  border-radius: 12px;
  font-size: 15px;
  color: var(--text-main);
  background: white;
  outline: none;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #4a90d9;
  box-shadow: 0 0 0 4px rgba(74, 144, 217, 0.1);
}

.search-input::placeholder {
  color: var(--text-sub);
}

.clear-btn {
  position: absolute;
  right: 12px;
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub);
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: rgba(0, 0, 0, 0.12);
  color: var(--text-main);
}

.filter-options {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  overflow: hidden;
  transition: all 0.3s ease;
}

.filter-options.is-expanded {
  max-height: 100px;
}

.search-dropdown-container {
  position: relative;
}

.search-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 100px;
}

.search-dropdown-trigger:hover {
  border-color: rgba(74, 144, 217, 0.3);
  background: rgba(74, 144, 217, 0.05);
}

.trigger-content {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.trigger-icon {
  color: #4a90d9;
  display: flex;
  align-items: center;
}

.trigger-text {
  font-size: 13px;
  color: var(--text-main);
  font-weight: 500;
}

.dropdown-arrow {
  color: var(--text-sub);
  transition: transform 0.2s ease;
}

.dropdown-arrow.is-open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  min-width: 140px;
  background: white;
  border: 1px solid var(--line-soft);
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 200;
  overflow: hidden;
}

.dropdown-item {
  padding: 10px 14px;
  font-size: 13px;
  color: var(--text-main);
  cursor: pointer;
  transition: all 0.15s ease;
}

.dropdown-item:hover {
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
}

.dropdown-item.is-active {
  background: rgba(74, 144, 217, 0.1);
  color: #4a90d9;
  font-weight: 600;
}

.dropdown-item.is-active:hover {
  background: linear-gradient(135deg, #4a90d9 0%, #3a7bc8 100%);
  color: white;
}

.dropdown-enter-active {
  animation: dropdown-in 0.2s ease-out;
}

.dropdown-leave-active {
  animation: dropdown-in 0.15s ease-in reverse;
}

@keyframes dropdown-in {
  0% {
    opacity: 0;
    transform: translateY(-8px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .filter-options {
    gap: 8px;
  }
  
  .search-dropdown-trigger {
    padding: 6px 10px;
    min-width: 80px;
  }
  
  .trigger-text {
    font-size: 12px;
  }
}
</style>
