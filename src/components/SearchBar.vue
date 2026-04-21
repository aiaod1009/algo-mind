<template>
  <div class="search-bar-container" :class="{ 'is-sticky': isSticky }">
    <div class="search-bar-wrapper">
      <div class="search-main-row">
        <div class="search-input-container">
          <div class="search-input-wrapper">
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              :placeholder="placeholder"
              @keyup.enter="handleSearch"
              @focus="isFocused = true"
              @blur="isFocused = false"
            />
            <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="14" height="14">
                <path
                  fill="currentColor"
                  d="M764.288 214.592 512 466.88 259.712 214.592a31.936 31.936 0 0 0-45.12 45.12L466.752 512 214.528 764.224a31.936 31.936 0 1 0 45.12 45.184L512 557.184l252.288 252.288a31.936 31.936 0 0 0 45.12-45.12L557.12 512.064l252.288-252.352a31.936 31.936 0 1 0-45.12-45.184z"
                />
              </svg>
            </button>
          </div>
        </div>
        <button class="search-btn" @click="handleSearch">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            width="16"
            height="16"
            fill="none"
            stroke="currentColor"
            stroke-width="2.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <span>搜索</span>
        </button>
      </div>

      <div class="filter-options" :class="{ 'is-expanded': showFilters }">
        <div class="search-dropdown-container">
          <div class="search-dropdown-trigger" @click="toggleDropdown('sort')">
            <div class="trigger-content">
              <span class="trigger-text">{{ currentSort.label }}</span>
            </div>
            <svg
              class="dropdown-arrow"
              :class="{ 'is-open': openDropdown === 'sort' }"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 1024 1024"
              width="12"
              height="12"
            >
              <path
                fill="currentColor"
                d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"
              />
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
                  <path
                    fill="currentColor"
                    d="M160 160v704h704V160zm-32-64h768a32 32 0 0 1 32 32v768a32 32 0 0 1-32 32H128a32 32 0 0 1-32-32V128a32 32 0 0 1 32-32"
                  />
                  <path
                    fill="currentColor"
                    d="M384 288q64 0 64 64t-64 64-64-64 64-64M185.408 876.992l-50.816-38.912L350.72 556.032a96 96 0 0 1 134.592-17.856l1.856 1.472 122.88 99.136a32 32 0 0 0 44.992-4.864l216-269.888 49.92 39.936-215.808 269.824-.256.32a96 96 0 0 1-135.04 14.464l-122.88-99.072-.64-.512a32 32 0 0 0-44.8 5.952z"
                  />
                </svg>
              </i>
              <span class="trigger-text">{{ currentCategory.label }}</span>
            </div>
            <svg
              class="dropdown-arrow"
              :class="{ 'is-open': openDropdown === 'category' }"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 1024 1024"
              width="12"
              height="12"
            >
              <path
                fill="currentColor"
                d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"
              />
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
                  <path
                    fill="currentColor"
                    d="M128 384a512 512 0 1 0 512-512 512 512 0 0 0-512 512m544 0a32 32 0 0 1-32 32H448a32 32 0 0 1-32-32V192a32 32 0 0 1 64 0v160h160a32 32 0 0 1 32 32"
                  />
                </svg>
              </i>
              <span class="trigger-text">{{ currentTime.label }}</span>
            </div>
            <svg
              class="dropdown-arrow"
              :class="{ 'is-open': openDropdown === 'time' }"
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 1024 1024"
              width="12"
              height="12"
            >
              <path
                fill="currentColor"
                d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"
              />
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

      <div class="search-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.value"
          class="search-tab"
          :class="{ 'is-active': currentTab === tab.value }"
          @click="selectTab(tab)"
        >
          {{ tab.label }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索帖子、题目或知识点...'
  },
  isSticky: {
    type: Boolean,
    default: false
  },
  keyword: {
    type: String,
    default: ''
  },
  activeTab: {
    type: String,
    default: 'all'
  },
  sortValue: {
    type: String,
    default: 'latest'
  },
  categoryValue: {
    type: String,
    default: 'all'
  },
  timeValue: {
    type: String,
    default: 'all'
  }
})

const emit = defineEmits(['search', 'filter-change', 'tab-change'])

const searchKeyword = ref(props.keyword)
const isFocused = ref(false)
const showFilters = ref(true)
const openDropdown = ref(null)

const tabs = [
  { label: '综合', value: 'all' },
  { label: '题库', value: 'questions' },
  { label: '鱼友', value: 'friends' },
  { label: '知识库', value: 'knowledge' }
]

const currentTab = ref(props.activeTab)

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

const findOption = (options, value) => options.find((item) => item.value === value) || options[0]

const currentSort = ref(findOption(sortOptions, props.sortValue))
const currentCategory = ref(findOption(categoryOptions, props.categoryValue))
const currentTime = ref(findOption(timeOptions, props.timeValue))

watch(() => props.keyword, (value) => {
  searchKeyword.value = value
})

watch(() => props.activeTab, (value) => {
  currentTab.value = value
})

watch(() => props.sortValue, (value) => {
  currentSort.value = findOption(sortOptions, value)
})

watch(() => props.categoryValue, (value) => {
  currentCategory.value = findOption(categoryOptions, value)
})

watch(() => props.timeValue, (value) => {
  currentTime.value = findOption(timeOptions, value)
})

const emitSearch = () => {
  emit('search', {
    keyword: searchKeyword.value.trim(),
    sort: currentSort.value.value,
    category: currentCategory.value.value,
    time: currentTime.value.value,
    tab: currentTab.value
  })
}

const handleSearch = () => {
  emitSearch()
}

const clearSearch = () => {
  searchKeyword.value = ''
  emitSearch()
}

const toggleDropdown = (name) => {
  openDropdown.value = openDropdown.value === name ? null : name
}

const selectSort = (item) => {
  currentSort.value = item
  openDropdown.value = null
  emitSearch()
}

const selectCategory = (item) => {
  currentCategory.value = item
  openDropdown.value = null
  emitSearch()
}

const selectTime = (item) => {
  currentTime.value = item
  openDropdown.value = null
  emitSearch()
}

const selectTab = (tab) => {
  currentTab.value = tab.value
  emit('tab-change', tab.value)
  emitSearch()
}

const handleClickOutside = (event) => {
  if (!event.target.closest('.search-dropdown-container')) {
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
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.62) 0%, rgba(246, 251, 255, 0.52) 100%);
  border-radius: 12px;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.58);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  box-shadow:
    0 8px 24px rgba(31, 58, 97, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.72);
  transition: all 0.3s ease;
  margin: 4px 0;
}

.search-bar-container.is-sticky {
  position: sticky;
  top: 80px;
  z-index: 100;
  box-shadow:
    0 10px 30px rgba(20, 45, 84, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.78);
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
  flex-wrap: wrap;
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
  border: 2px solid rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  font-size: 15px;
  color: var(--text-main);
  background: var(--card-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
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

.search-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 20px;
  height: 44px;
  background: linear-gradient(135deg, #0050b9 0%, #006aa8 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(0, 185, 107, 0.3);
}

.search-btn:hover {
  background: linear-gradient(135deg, #00a85c 0%, #00994d 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 185, 107, 0.4);
}

.search-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 185, 107, 0.3);
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
  background: var(--card-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
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
  background: var(--card-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
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

.search-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 12px;
  padding-left: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  margin-top: 4px;
  flex-wrap: wrap;
}

.search-tab {
  padding: 6px 16px;
  font-size: 16px;
  color: var(--text-sub);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  position: relative;
}

.search-tab:hover {
  color: var(--text-main);
  background: rgba(0, 0, 0, 0.04);
}

.search-tab.is-active {
  color: #00b96b;
  font-weight: 600;
  background: rgba(0, 185, 107, 0.1);
}

.search-tab.is-active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: #00b96b;
  border-radius: 2px;
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
