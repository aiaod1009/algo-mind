<template>
  <div class="knowledge-tree-map">
    <!-- 加载状态 -->
    <div v-if="loading" class="knowledge-tree-loading">
      <div class="knowledge-tree-skeleton">
        <div class="skeleton-header"></div>
        <div class="skeleton-tree">
          <div v-for="i in 6" :key="i" class="skeleton-node"></div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="knowledge-tree-error">
      <div class="error-icon">⚠️</div>
      <p>{{ error }}</p>
      <button type="button" class="retry-btn" @click="$emit('retry')">
        重新加载
      </button>
    </div>

    <!-- 树状图内容 -->
    <div v-else class="knowledge-tree-content">
      <!-- 头部 -->
      <div class="knowledge-tree-header">
        <div class="header-title">
          <h2>探索知识地图</h2>
          <p>通往编程高峰的必经之路</p>
        </div>
        <div class="header-sort">
          <label class="sort-label">
            <span>{{ sortLabel }}</span>
            <select :value="sortValue" @change="$emit('update:sortValue', $event.target.value)">
              <option v-for="option in sortOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 9l6 6 6-6" />
            </svg>
          </label>
        </div>
      </div>

      <!-- 树状图 -->
      <div class="knowledge-tree-container" ref="treeContainer">
        <!-- 连接线 SVG -->
        <svg class="tree-connections" :viewBox="`0 0 ${containerWidth} ${containerHeight}`" preserveAspectRatio="none">
          <defs>
            <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
              <polygon points="0 0, 10 3.5, 0 7" fill="#94a3b8" />
            </marker>
          </defs>
          <path
            v-for="(line, index) in connectionLines"
            :key="index"
            :d="line.path"
            fill="none"
            stroke="#cbd5e1"
            stroke-width="1.5"
            stroke-dasharray="4,4"
            marker-end="url(#arrowhead)"
          />
        </svg>

        <!-- 节点网格 -->
        <div class="tree-nodes-grid">
          <div
            v-for="(node, index) in treeNodes"
            :key="node.id"
            class="tree-node-wrapper"
            :style="getNodePosition(index)"
            :data-level="node.level"
            :data-category="node.category"
          >
            <div
              class="tree-node"
              :class="[
                `is-${node.accent}`,
                { 'is-active': node.id === activeNodeId, 'is-root': node.level === 0 }
              ]"
              @mouseenter="handleNodeHover(node)"
              @mouseleave="handleNodeLeave"
              @click="$emit('nodeClick', node)"
            >
              <span class="node-dot" :style="{ backgroundColor: node.color }"></span>
              <span class="node-label">{{ node.label }}</span>
            </div>

            <!-- 悬停提示 -->
            <transition name="tooltip-fade">
              <div
                v-if="hoveredNode?.id === node.id"
                class="node-tooltip"
                :class="{ 'tooltip-right': shouldShowTooltipRight(index) }"
              >
                <div class="tooltip-header">
                  <span class="tooltip-dot" :style="{ backgroundColor: node.color }"></span>
                  <span class="tooltip-title">{{ node.label }}</span>
                </div>
                <div class="tooltip-content">
                  <p v-if="node.description" class="tooltip-desc">{{ node.description }}</p>
                  <div v-if="node.concepts?.length" class="tooltip-section">
                    <span class="tooltip-section-title">核心概念</span>
                    <div class="tooltip-tags">
                      <span v-for="concept in node.concepts" :key="concept" class="tooltip-tag">{{ concept }}</span>
                    </div>
                  </div>
                  <div v-if="node.algorithms?.length" class="tooltip-section">
                    <span class="tooltip-section-title">关键算法</span>
                    <div class="tooltip-tags">
                      <span v-for="algo in node.algorithms" :key="algo" class="tooltip-tag is-algo">{{ algo }}</span>
                    </div>
                  </div>
                  <div v-if="node.scenarios?.length" class="tooltip-section">
                    <span class="tooltip-section-title">应用场景</span>
                    <p class="tooltip-scenarios">{{ node.scenarios.join('、') }}</p>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'

const props = defineProps({
  stages: {
    type: Array,
    default: () => []
  },
  sortValue: {
    type: String,
    default: 'path'
  },
  sortOptions: {
    type: Array,
    default: () => []
  },
  sortLabel: {
    type: String,
    default: '学习路径'
  },
  activeNodeId: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:sortValue', 'nodeClick', 'retry'])

const treeContainer = ref(null)
const containerWidth = ref(1200)
const containerHeight = ref(600)
const hoveredNode = ref(null)

// 颜色映射
const accentColors = {
  sunrise: '#f59e0b',
  azure: '#3b82f6',
  mint: '#10b981',
  amber: '#f59e0b',
  ocean: '#0ea5e9',
  violet: '#8b5cf6',
  rose: '#f43f5e',
  cyan: '#06b6d4'
}

// 将阶段数据转换为树状节点
const treeNodes = computed(() => {
  const nodes = []
  const categoryMap = new Map()

  props.stages.forEach((stage, stageIndex) => {
    // 添加阶段根节点
    const rootNode = {
      id: `stage-${stage.id}`,
      label: stage.title,
      level: 0,
      category: stage.id,
      accent: stage.accent,
      color: accentColors[stage.accent] || '#64748b',
      description: stage.subtitle,
      concepts: [],
      algorithms: [],
      scenarios: [],
      stageIndex,
      isRoot: true
    }
    nodes.push(rootNode)
    categoryMap.set(stage.id, rootNode)

    // 添加子节点
    stage.nodes?.forEach((node, nodeIndex) => {
      const nodeData = getNodeMetadata(node.label)
      nodes.push({
        id: node.id,
        label: node.label,
        level: 1,
        category: stage.id,
        accent: stage.accent,
        color: accentColors[stage.accent] || '#64748b',
        parentId: rootNode.id,
        description: nodeData.description,
        concepts: nodeData.concepts,
        algorithms: nodeData.algorithms,
        scenarios: nodeData.scenarios,
        stageIndex,
        nodeIndex,
        slug: node.slug,
        keyword: node.keyword
      })
    })
  })

  return nodes
})

// 获取节点元数据
const getNodeMetadata = (label) => {
  const metadataMap = {
    '数组': {
      description: '最基础的数据结构，连续内存存储相同类型元素',
      concepts: ['随机访问', '连续存储', '索引访问'],
      algorithms: ['双指针', '滑动窗口', '前缀和'],
      scenarios: ['数据存储', '线性遍历', '区间查询']
    },
    '排序': {
      description: '将数据按特定顺序重新排列',
      concepts: ['比较排序', '非比较排序', '稳定性'],
      algorithms: ['快速排序', '归并排序', '堆排序'],
      scenarios: ['数据整理', 'Top K', '去重']
    },
    '字符串': {
      description: '字符序列的处理与操作',
      concepts: ['字符编码', '子串', '模式匹配'],
      algorithms: ['KMP', 'Trie树', '后缀数组'],
      scenarios: ['文本处理', '正则匹配', 'DNA序列']
    },
    '回溯': {
      description: '通过试错来搜索问题的解',
      concepts: ['状态空间', '剪枝', '递归'],
      algorithms: ['DFS', '排列组合', 'N皇后'],
      scenarios: ['组合问题', '排列问题', '子集问题']
    },
    '前缀和': {
      description: '预处理技术，快速计算区间和',
      concepts: ['预处理', '差分', '区间查询'],
      algorithms: ['一维前缀和', '二维前缀和'],
      scenarios: ['区间求和', '子矩阵求和', '频率统计']
    },
    '差分数组': {
      description: '区间修改的高效处理方式',
      concepts: ['区间更新', '前缀和逆运算'],
      algorithms: ['一维差分', '二维差分'],
      scenarios: ['区间加减', '频繁区间修改']
    },
    '数组遍历与双指针': {
      description: '利用两个指针高效遍历数组',
      concepts: ['对撞指针', '快慢指针', '滑动窗口'],
      algorithms: ['两数之和', '三数之和', '盛水容器'],
      scenarios: ['有序数组', '去重', '查找']
    },
    '链表快慢指针': {
      description: '利用速度差解决链表问题',
      concepts: ['环检测', '中点查找', '倒数第K个'],
      algorithms: ['Floyd判圈', '寻找入口', '重排链表'],
      scenarios: ['环检测', '链表分割', '链表合并']
    },
    '栈与括号匹配': {
      description: '后进先出结构处理嵌套问题',
      concepts: ['LIFO', '表达式求值', '括号匹配'],
      algorithms: ['单调栈', '后缀表达式', '括号生成'],
      scenarios: ['表达式计算', '括号匹配', '浏览器前进后退']
    },
    '队列与层序遍历': {
      description: '先进先出结构处理层级问题',
      concepts: ['FIFO', 'BFS', '优先级队列'],
      algorithms: ['普通队列', '双端队列', '优先队列'],
      scenarios: ['任务调度', 'BFS遍历', '缓存实现']
    },
    '双端队列与单调队列': {
      description: '两端操作的高效数据结构',
      concepts: ['两端操作', '单调性维护'],
      algorithms: ['滑动窗口最大值', '单调栈'],
      scenarios: ['滑动窗口', '区间最值']
    },
    '图论': {
      description: '研究图结构及其算法',
      concepts: ['顶点', '边', '连通性', '路径'],
      algorithms: ['Dijkstra', 'Floyd', '拓扑排序', '最小生成树'],
      scenarios: ['最短路径', '网络流', '社交网络']
    },
    '堆与优先队列': {
      description: '高效获取最值的数据结构',
      concepts: ['完全二叉树', '堆性质', '优先队列'],
      algorithms: ['堆排序', 'Top K', '合并K个链表'],
      scenarios: ['任务调度', '流式数据', '贪心算法']
    },
    '二叉树遍历算法': {
      description: '按特定顺序访问树节点',
      concepts: ['前序', '中序', '后序', '层序'],
      algorithms: ['递归遍历', '迭代遍历', 'Morris遍历'],
      scenarios: ['表达式树', '文件系统', '决策树']
    },
    '二叉搜索树': {
      description: '有序树结构支持高效查找',
      concepts: ['有序性', '平衡性', '查找效率'],
      algorithms: ['插入', '删除', '查找', 'AVL树', '红黑树'],
      scenarios: ['数据库索引', '集合实现', '范围查询']
    },
    '并查集': {
      description: '处理不相交集合的合并与查询',
      concepts: ['集合合并', '路径压缩', '按秩合并'],
      algorithms: ['Find', 'Union', '带权并查集'],
      scenarios: ['连通分量', '最小生成树', '等价类划分']
    },
    '动态规划': {
      description: '通过子问题求解原问题的算法思想',
      concepts: ['最优子结构', '重叠子问题', '状态转移'],
      algorithms: ['背包问题', '最长子序列', '区间DP'],
      scenarios: ['路径规划', '资源分配', '序列比对']
    },
    '哈希表查找与计数': {
      description: '基于哈希函数实现O(1)查找',
      concepts: ['哈希函数', '冲突处理', '负载因子'],
      algorithms: ['开放寻址', '链地址法', '一致性哈希'],
      scenarios: ['缓存实现', '去重统计', '快速查找']
    }
  }

  return metadataMap[label] || {
    description: '算法知识点',
    concepts: ['基础概念'],
    algorithms: ['基础算法'],
    scenarios: ['典型应用']
  }
}

// 计算节点位置
const getNodePosition = (index) => {
  const node = treeNodes.value[index]
  if (!node) return {}

  // 响应式布局计算
  const isMobile = window.innerWidth <= 767
  const isTablet = window.innerWidth <= 1199

  if (isMobile) {
    // 移动端：垂直布局
    return {
      gridColumn: 1,
      gridRow: index + 1
    }
  }

  // 桌面端：树状布局
  const nodesPerRow = isTablet ? 4 : 6
  const row = Math.floor(index / nodesPerRow)
  const col = index % nodesPerRow

  return {
    gridColumn: col + 1,
    gridRow: row + 1
  }
}

// 计算连接线
const connectionLines = computed(() => {
  const lines = []
  const nodeElements = treeContainer.value?.querySelectorAll('.tree-node-wrapper') || []

  treeNodes.value.forEach((node, index) => {
    if (node.parentId && node.level === 1) {
      const parentIndex = treeNodes.value.findIndex(n => n.id === node.parentId)
      if (parentIndex !== -1 && nodeElements[parentIndex] && nodeElements[index]) {
        const parentRect = nodeElements[parentIndex].getBoundingClientRect()
        const childRect = nodeElements[index].getBoundingClientRect()
        const containerRect = treeContainer.value.getBoundingClientRect()

        const x1 = parentRect.left + parentRect.width / 2 - containerRect.left
        const y1 = parentRect.bottom - containerRect.top
        const x2 = childRect.left + childRect.width / 2 - containerRect.left
        const y2 = childRect.top - containerRect.top

        // 贝塞尔曲线
        const controlY = (y1 + y2) / 2
        const path = `M ${x1} ${y1} C ${x1} ${controlY}, ${x2} ${controlY}, ${x2} ${y2}`

        lines.push({ path })
      }
    }
  })

  return lines
})

// 处理节点悬停
const handleNodeHover = (node) => {
  hoveredNode.value = node
}

const handleNodeLeave = () => {
  hoveredNode.value = null
}

// 判断提示框位置
const shouldShowTooltipRight = (index) => {
  const col = index % 6
  return col >= 3
}

// 更新容器尺寸
const updateContainerSize = () => {
  if (treeContainer.value) {
    containerWidth.value = treeContainer.value.offsetWidth
    containerHeight.value = treeContainer.value.offsetHeight
  }
}

// 监听窗口变化
onMounted(() => {
  nextTick(() => {
    updateContainerSize()
  })
  window.addEventListener('resize', updateContainerSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateContainerSize)
})

// 监听数据变化重新计算
watch(() => props.stages, () => {
  nextTick(() => {
    updateContainerSize()
  })
}, { deep: true })
</script>

<style scoped>
.knowledge-tree-map {
  width: 100%;
}

/* 加载状态 */
.knowledge-tree-loading {
  padding: 40px 20px;
}

.knowledge-tree-skeleton {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  padding: 30px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.skeleton-header {
  height: 40px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 8px;
  margin-bottom: 30px;
  width: 60%;
}

.skeleton-tree {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
}

.skeleton-node {
  height: 50px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  border-radius: 12px;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 错误状态 */
.knowledge-tree-error {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.knowledge-tree-error p {
  font-size: 16px;
  margin-bottom: 20px;
}

.retry-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.3);
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

/* 头部 */
.knowledge-tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-title h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
}

.header-title p {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.sort-label {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sort-label:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.sort-label span {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.sort-label select {
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
  outline: none;
  appearance: none;
  padding-right: 8px;
}

.sort-label svg {
  color: #94a3b8;
}

/* 树状图容器 */
.knowledge-tree-container {
  position: relative;
  min-height: 400px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 24px;
  padding: 40px 30px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.tree-connections {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 节点网格 */
.tree-nodes-grid {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 24px 20px;
}

.tree-node-wrapper {
  position: relative;
}

/* 节点样式 */
.tree-node {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: white;
  border-radius: 14px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.tree-node:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.tree-node.is-active {
  border-color: currentColor;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.tree-node.is-root {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  color: white;
}

.tree-node.is-root .node-label {
  color: white;
  font-weight: 600;
}

.node-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.node-label {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 颜色主题 */
.tree-node.is-sunrise { --accent-color: #f59e0b; }
.tree-node.is-azure { --accent-color: #3b82f6; }
.tree-node.is-mint { --accent-color: #10b981; }
.tree-node.is-amber { --accent-color: #f59e0b; }
.tree-node.is-ocean { --accent-color: #0ea5e9; }
.tree-node.is-violet { --accent-color: #8b5cf6; }

.tree-node:not(.is-root):hover {
  border-color: var(--accent-color, #cbd5e1);
}

/* 提示框 */
.node-tooltip {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 12px;
  width: 320px;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(226, 232, 240, 0.8);
  z-index: 100;
}

.node-tooltip.tooltip-right {
  left: auto;
  right: 0;
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.tooltip-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.tooltip-title {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.tooltip-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 16px;
}

.tooltip-section {
  margin-bottom: 14px;
}

.tooltip-section:last-child {
  margin-bottom: 0;
}

.tooltip-section-title {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 8px;
}

.tooltip-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tooltip-tag {
  padding: 5px 10px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #475569;
}

.tooltip-tag.is-algo {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1d4ed8;
}

.tooltip-scenarios {
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
  margin: 0;
}

/* 动画 */
.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 响应式布局 */
@media (max-width: 1199px) {
  .knowledge-tree-container {
    padding: 30px 24px;
  }

  .tree-nodes-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 20px 16px;
  }

  .node-tooltip {
    width: 280px;
  }
}

@media (max-width: 767px) {
  .knowledge-tree-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-title h2 {
    font-size: 24px;
  }

  .knowledge-tree-container {
    padding: 24px 16px;
    border-radius: 20px;
  }

  .tree-nodes-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .tree-node {
    padding: 16px;
  }

  .node-tooltip {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: calc(100vw - 40px);
    max-width: 360px;
    margin: 0;
  }

  .node-tooltip.tooltip-right {
    left: 50%;
    right: auto;
  }
}
</style>
