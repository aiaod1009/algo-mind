<template>
  <Transition name="panel">
    <div v-if="node" class="node-detail-overlay" @click.self="$emit('close')">
      <div class="node-detail-panel" :style="{ '--accent': categoryColor }">
        <div class="panel-header">
          <div class="panel-header-top">
            <div class="panel-icon" :style="{ backgroundColor: categoryColor }">
              <span v-if="node.isRoot">★</span>
              <span v-else>{{ node.level }}</span>
            </div>
            <div class="panel-title-area">
              <h3 class="panel-title">{{ node.label }}</h3>
              <span class="panel-category" :style="{ color: categoryColor }">
                {{ categoryName }}
              </span>
            </div>
            <button class="panel-close" @click="$emit('close')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12" />
              </svg>
            </button>
          </div>
          <p class="panel-description">{{ node.description }}</p>
        </div>

        <div class="panel-body">
          <div v-if="node.concepts?.length" class="panel-section">
            <h4 class="section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 16v-4M12 8h.01" />
              </svg>
              核心概念
            </h4>
            <div class="concept-tags">
              <span
                v-for="concept in node.concepts"
                :key="concept"
                class="concept-tag"
              >{{ concept }}</span>
            </div>
          </div>

          <div v-if="node.algorithms?.length" class="panel-section">
            <h4 class="section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <polyline points="16 18 22 12 16 6" />
                <polyline points="8 6 2 12 8 18" />
              </svg>
              关键算法
            </h4>
            <div class="algorithm-list">
              <div
                v-for="algo in node.algorithms"
                :key="algo.name"
                class="algorithm-item"
              >
                <span class="algo-name">{{ algo.name }}</span>
                <span class="algo-complexity">{{ algo.complexity }}</span>
              </div>
            </div>
          </div>

          <div v-if="node.prerequisites?.length" class="panel-section">
            <h4 class="section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
              前置知识
            </h4>
            <div class="prereq-list">
              <span
                v-for="prereq in node.prerequisites"
                :key="prereq"
                class="prereq-item"
                @click="$emit('navigate', prereq)"
              >{{ prereq }}</span>
            </div>
          </div>

          <div v-if="downstreamNodes.length" class="panel-section">
            <h4 class="section-title">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
              后续知识
            </h4>
            <div class="prereq-list">
              <span
                v-for="ds in downstreamNodes"
                :key="ds.id"
                class="prereq-item downstream"
                :style="{ '--accent': getCategoryColor(ds.category) }"
                @click="$emit('navigate', ds.label)"
              >{{ ds.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  node: { type: Object, default: null },
  categories: { type: Array, required: true },
  connections: { type: Array, required: true },
  knowledgeNodes: { type: Array, required: true }
})

defineEmits(['close', 'navigate'])

const categoryColor = computed(() => {
  if (!props.node) return '#64748b'
  const cat = props.categories.find(c => c.id === props.node.category)
  return cat?.color || '#64748b'
})

const categoryName = computed(() => {
  if (!props.node) return ''
  const cat = props.categories.find(c => c.id === props.node.category)
  return cat?.name || ''
})

const downstreamNodes = computed(() => {
  if (!props.node) return []
  const dsIds = props.connections
    .filter(c => c.source === props.node.id)
    .map(c => c.target)
  return props.knowledgeNodes.filter(n => dsIds.includes(n.id))
})

function getCategoryColor(catId) {
  const cat = props.categories.find(c => c.id === catId)
  return cat?.color || '#64748b'
}
</script>

<style scoped>
.node-detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.3);
  backdrop-filter: blur(4px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.node-detail-panel {
  width: 420px;
  max-width: 100%;
  max-height: 80vh;
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 60px -12px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 24px 24px 20px;
  background: linear-gradient(135deg, var(--accent, #64748b) 0%, var(--accent, #64748b) 100%);
  background-size: 100% 4px;
  background-repeat: no-repeat;
  background-position: bottom;
  border-bottom: 4px solid var(--accent, #64748b);
}

.panel-header-top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 12px;
}

.panel-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 800;
  color: white;
  flex-shrink: 0;
}

.panel-title-area {
  flex: 1;
  min-width: 0;
}

.panel-title {
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 4px;
  letter-spacing: -0.01em;
}

.panel-category {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.panel-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
  flex-shrink: 0;
}

.panel-close:hover {
  background: #e2e8f0;
  color: #334155;
}

.panel-close svg {
  width: 16px;
  height: 16px;
}

.panel-description {
  font-size: 14px;
  color: #475569;
  line-height: 1.7;
  margin: 0;
}

.panel-body {
  padding: 20px 24px 24px;
  overflow-y: auto;
}

.panel-section {
  margin-bottom: 20px;
}

.panel-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin: 0 0 10px;
}

.concept-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.concept-tag {
  padding: 5px 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  transition: all 0.2s;
}

.concept-tag:hover {
  background: #e2e8f0;
}

.algorithm-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.algorithm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #f1f5f9;
  transition: all 0.2s;
}

.algorithm-item:hover {
  background: #f1f5f9;
  border-color: #e2e8f0;
}

.algo-name {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.algo-complexity {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
}

.prereq-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.prereq-item {
  padding: 5px 12px;
  background: #fef3c7;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #92400e;
  cursor: pointer;
  transition: all 0.2s;
}

.prereq-item:hover {
  background: #fde68a;
}

.prereq-item.downstream {
  background: #f1f5f9;
  color: #475569;
}

.prereq-item.downstream:hover {
  background: #e2e8f0;
  color: #334155;
}

.panel-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-enter-from {
  opacity: 0;
}

.panel-enter-from .node-detail-panel {
  transform: scale(0.95) translateY(10px);
}

.panel-leave-to {
  opacity: 0;
}

.panel-leave-to .node-detail-panel {
  transform: scale(0.95) translateY(10px);
}

@media (max-width: 768px) {
  .node-detail-overlay {
    padding: 16px;
    align-items: flex-end;
  }

  .node-detail-panel {
    width: 100%;
    max-height: 70vh;
    border-radius: 20px 20px 0 0;
  }

  .panel-header {
    padding: 20px 20px 16px;
  }

  .panel-body {
    padding: 16px 20px 20px;
  }
}
</style>
