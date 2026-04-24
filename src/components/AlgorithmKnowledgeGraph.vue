<template>
  <div class="algorithm-knowledge-graph" ref="graphContainer">
    <GraphToolbar
      :categories="categories"
      :search-query="searchQuery"
      :selected-category="selectedCategory"
      :layout-type="layoutType"
      @update:search-query="val => searchQuery = val"
      @select-category="selectCategory"
      @clear-selection="clearSelection"
      @toggle-layout="toggleLayout"
      @reset-view="resetView"
      @zoom-in="zoomIn"
      @zoom-out="zoomOut"
    />

    <div class="graph-chart-container">
      <v-chart
        ref="chartRef"
        :option="chartOption"
        :autoresize="true"
        :loading="isLoading"
        @click="handleChartClick"
        @mouseover="handleChartMouseOver"
        @mouseout="handleChartMouseOut"
        class="graph-chart"
      />

      <div class="graph-legend">
        <div class="legend-title">知识分类</div>
        <div class="legend-items">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="legend-item"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectCategory(cat.id)"
          >
            <span class="legend-dot" :style="{ backgroundColor: cat.color }"></span>
            <span class="legend-label">{{ cat.name }}</span>
          </div>
        </div>
      </div>

      <div class="graph-stats">
        <span class="stat-chip">
          <strong>{{ stats.nodeCount }}</strong> 节点
        </span>
        <span class="stat-divider">·</span>
        <span class="stat-chip">
          <strong>{{ stats.connectionCount }}</strong> 路径
        </span>
        <span class="stat-divider">·</span>
        <span class="stat-chip">
          <strong>{{ stats.categoryCount }}</strong> 分类
        </span>
      </div>
    </div>

    <NodeDetailPanel
      :node="selectedNode"
      :categories="categories"
      :connections="connections"
      :knowledge-nodes="knowledgeNodes"
      @close="selectNode(null)"
      @navigate="handleNavigate"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { GraphChart } from 'echarts/charts'
import {
  TooltipComponent,
  LegendComponent,
  TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { useKnowledgeGraph } from '../composables/useKnowledgeGraph'
import GraphToolbar from './knowledge-graph/GraphToolbar.vue'
import NodeDetailPanel from './knowledge-graph/NodeDetailPanel.vue'

use([GraphChart, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const {
  categories,
  knowledgeNodes,
  connections,
  searchQuery,
  selectedCategory,
  selectedNode,
  hoveredNodeId,
  layoutType,
  stats,
  chartOption,
  selectCategory,
  clearSelection,
  selectNode,
  hoverNode,
  clearHover,
  toggleLayout,
  nodeMap
} = useKnowledgeGraph()

const chartRef = ref(null)
const graphContainer = ref(null)
const isLoading = ref(true)

function getChartInstance() {
  if (!chartRef.value) return null
  return chartRef.value
}

onMounted(async () => {
  await nextTick()
  isLoading.value = false
})

onUnmounted(() => {
  if (chartRef.value) {
    chartRef.value.dispose?.()
  }
})

function handleChartClick(params) {
  if (params.dataType === 'node') {
    const nodeId = params.data.id
    const node = nodeMap.value[nodeId]
    if (node) {
      selectNode(nodeId)
    }
  }
}

function handleChartMouseOver(params) {
  if (params.dataType === 'node') {
    hoverNode(params.data.id)
  }
}

function handleChartMouseOut() {
  clearHover()
}

function resetView() {
  const chart = getChartInstance()
  if (chart) {
    chart.dispatchAction({ type: 'restore' })
  }
}

function zoomIn() {
  const chart = getChartInstance()
  if (chart) {
    chart.dispatchAction({
      type: 'graphRoam',
      seriesIndex: 0,
      zoom: 1.3
    })
  }
}

function zoomOut() {
  const chart = getChartInstance()
  if (chart) {
    chart.dispatchAction({
      type: 'graphRoam',
      seriesIndex: 0,
      zoom: 0.7
    })
  }
}

function handleNavigate(label) {
  const node = knowledgeNodes.find(n => n.label === label)
  if (node) {
    selectNode(node.id)
    const chart = getChartInstance()
    if (chart) {
      chart.dispatchAction({
        type: 'highlight',
        seriesIndex: 0,
        name: node.label
      })
    }
  }
}
</script>

<style scoped>
.algorithm-knowledge-graph {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.graph-chart-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 20% 30%, rgba(59, 130, 246, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.03) 0%, transparent 50%),
    linear-gradient(135deg, #fafbfc 0%, #f5f7fa 100%);
}

.graph-chart {
  width: 100%;
  height: 100%;
}

.graph-legend {
  position: absolute;
  bottom: 16px;
  left: 16px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.legend-title {
  font-size: 10px;
  font-weight: 700;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 8px;
}

.legend-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s;
  padding: 2px 4px;
  border-radius: 4px;
}

.legend-item:hover {
  background: #f1f5f9;
}

.legend-item.active {
  background: #f1f5f9;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.legend-label {
  font-size: 12px;
  font-weight: 500;
  color: #475569;
}

.graph-stats {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-radius: 10px;
  padding: 8px 14px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.stat-chip {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.stat-chip strong {
  color: #334155;
  font-weight: 700;
}

.stat-divider {
  color: #cbd5e1;
  font-size: 12px;
}

@media (max-width: 768px) {
  .graph-legend {
    bottom: 12px;
    left: 12px;
    right: 12px;
    padding: 10px 14px;
  }

  .legend-items {
    gap: 8px;
  }

  .graph-stats {
    bottom: auto;
    top: 12px;
    right: 12px;
    padding: 6px 10px;
  }

  .stat-chip {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .graph-legend {
    position: relative;
    bottom: auto;
    left: auto;
    right: auto;
    border-radius: 0;
    box-shadow: none;
    border-top: 1px solid #e2e8f0;
  }
}
</style>
