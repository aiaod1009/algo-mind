<template>
  <div class="graph-toolbar">
    <div class="toolbar-left">
      <h2 class="graph-title">算法知识图谱</h2>
      <span class="graph-subtitle">探索数据结构与算法的学习路径</span>
    </div>

    <div class="toolbar-center">
      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          :value="searchQuery"
          @input="$emit('update:searchQuery', $event.target.value)"
          type="text"
          placeholder="搜索知识点、概念或算法..."
          class="search-input"
        />
        <button
          v-if="searchQuery"
          class="search-clear"
          @click="$emit('update:searchQuery', '')"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
            <path d="M18 6L6 18M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <div class="toolbar-right">
      <div class="toolbar-actions">
        <button
          class="toolbar-btn"
          :class="{ active: layoutType === 'circular' }"
          @click="$emit('toggleLayout')"
          :title="layoutType === 'force' ? '切换环形布局' : '切换力导向布局'"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="9" />
            <circle cx="12" cy="3" r="1.5" fill="currentColor" />
            <circle cx="20" cy="15" r="1.5" fill="currentColor" />
            <circle cx="4" cy="15" r="1.5" fill="currentColor" />
          </svg>
        </button>
        <button class="toolbar-btn" @click="$emit('resetView')" title="重置视图">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
            <path d="M3 3v5h5" />
          </svg>
        </button>
        <button class="toolbar-btn" @click="$emit('zoomIn')" title="放大">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
            <path d="M11 8v6M8 11h6" />
          </svg>
        </button>
        <button class="toolbar-btn" @click="$emit('zoomOut')" title="缩小">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <path d="M21 21l-4.35-4.35" />
            <path d="M8 11h6" />
          </svg>
        </button>
      </div>
    </div>
  </div>

  <div class="category-filter">
    <button
      v-for="cat in categories"
      :key="cat.id"
      class="filter-btn"
      :class="{ active: selectedCategory === cat.id }"
      :style="{ '--cat-color': cat.color, '--cat-light': cat.lightColor }"
      @click="$emit('selectCategory', cat.id)"
    >
      <span class="filter-dot" :style="{ backgroundColor: cat.color }"></span>
      {{ cat.name }}
    </button>
    <button
      v-if="selectedCategory || searchQuery"
      class="filter-btn clear-btn"
      @click="$emit('clearSelection')"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
        <path d="M18 6L6 18M6 6l12 12" />
      </svg>
      清除筛选
    </button>
  </div>
</template>

<script setup>
defineProps({
  categories: { type: Array, required: true },
  searchQuery: { type: String, default: '' },
  selectedCategory: { type: String, default: '' },
  layoutType: { type: String, default: 'force' }
})

defineEmits([
  'update:searchQuery',
  'selectCategory',
  'clearSelection',
  'toggleLayout',
  'resetView',
  'zoomIn',
  'zoomOut'
])
</script>

<style scoped>
.graph-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.graph-title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.02em;
}

.graph-subtitle {
  font-size: 13px;
  color: #64748b;
}

.toolbar-center {
  flex: 1;
  max-width: 400px;
  min-width: 200px;
}

.search-box {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
  padding: 10px 36px 10px 40px;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  transition: all 0.25s;
  background: #f8fafc;
}

.search-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.12);
  background: white;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #94a3b8;
}

.search-clear {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #94a3b8;
  padding: 4px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.search-clear:hover {
  color: #64748b;
  background: #f1f5f9;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.toolbar-actions {
  display: flex;
  gap: 6px;
  margin-right: 40px;
}

.toolbar-btn {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
}

.toolbar-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #334155;
}

.toolbar-btn.active {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

.toolbar-btn svg {
  width: 18px;
  height: 18px;
}

.category-filter {
  display: flex;
  gap: 8px;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  overflow-x: auto;
  align-items: center;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: #f8fafc;
  border: 1.5px solid transparent;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.filter-btn:hover {
  background: #f1f5f9;
}

.filter-btn.active {
  background: var(--cat-light);
  border-color: var(--cat-color);
  color: var(--cat-color);
  font-weight: 600;
}

.filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.clear-btn {
  color: #94a3b8;
  gap: 4px;
  padding: 7px 12px;
  font-size: 12px;
}

.clear-btn:hover {
  color: #64748b;
  background: #fef2f2;
}

@media (max-width: 768px) {
  .graph-toolbar {
    flex-direction: column;
    align-items: stretch;
    padding: 12px 16px;
  }

  .toolbar-center {
    max-width: 100%;
  }

  .toolbar-right {
    justify-content: flex-end;
  }

  .category-filter {
    padding: 10px 16px;
    gap: 6px;
  }

  .filter-btn {
    padding: 6px 10px;
    font-size: 12px;
  }
}
</style>
