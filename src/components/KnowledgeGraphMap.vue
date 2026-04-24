<template>
  <div class="knowledge-graph-map">
    <!-- 加载状态 -->
    <div v-if="loading" class="graph-loading">
      <div class="graph-skeleton">
        <div v-for="i in 3" :key="i" class="skeleton-row">
          <div v-for="j in 4" :key="j" class="skeleton-node"></div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="graph-error">
      <div class="error-icon">⚠️</div>
      <p>{{ error }}</p>
      <button type="button" class="retry-btn" @click="$emit('retry')">重新加载</button>
    </div>

    <!-- 图内容 -->
    <div v-else class="graph-content">
      <!-- 头部 -->
      <div class="graph-header">
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

      <!-- 水平分层图 -->
      <div class="graph-container" ref="graphContainer">
        <!-- SVG 连接线 -->
        <svg class="graph-connections" :viewBox="`0 0 ${graphWidth} ${graphHeight}`" preserveAspectRatio="none">
          <defs>
            <marker id="dot" markerWidth="8" markerHeight="8" refX="4" refY="4">
              <circle cx="4" cy="4" r="3" fill="#94a3b8" />
            </marker>
          </defs>
          <path
            v-for="(line, index) in connectionLines"
            :key="index"
            :d="line.path"
            fill="none"
            stroke="#94a3b8"
            stroke-width="1.5"
            stroke-dasharray="4,3"
            marker-start="url(#dot)"
            marker-end="url(#dot)"
          />
        </svg>

        <!-- 节点层 -->
        <div class="graph-layers">
          <div
            v-for="(layer, layerIndex) in graphLayers"
            :key="layerIndex"
            class="graph-layer"
            :class="`layer-${layerIndex}`"
          >
            <div
              v-for="node in layer.nodes"
              :key="node.id"
              class="graph-node"
              :class="{ 'is-active': node.id === activeNodeId }"
              :style="{ left: `${node.x}px`, top: `${node.y}px` }"
              @mouseenter="handleNodeHover(node)"
              @mouseleave="handleNodeLeave"
              @click="$emit('nodeClick', node)"
            >
              <div class="node-card">
                <span class="node-icon" :style="{ backgroundColor: node.color }"></span>
                <span class="node-text">{{ node.label }}</span>
              </div>

              <!-- 悬停提示 -->
              <transition name="tooltip-fade">
                <div
                  v-if="hoveredNode?.id === node.id"
                  class="node-tooltip"
                  :class="{ 'tooltip-top': layerIndex > 1 }"
                >
                  <div class="tooltip-header">
                    <span class="tooltip-icon" :style="{ backgroundColor: node.color }"></span>
                    <span class="tooltip-title">{{ node.label }}</span>
                  </div>
                  <div class="tooltip-body">
                    <p v-if="node.description" class="tooltip-desc">{{ node.description }}</p>
                    <div v-if="node.concepts?.length" class="tooltip-section">
                      <span class="section-label">核心概念</span>
                      <div class="tag-list">
                        <span v-for="c in node.concepts" :key="c" class="tag">{{ c }}</span>
                      </div>
                    </div>
                    <div v-if="node.algorithms?.length" class="tooltip-section">
                      <span class="section-label">关键算法</span>
                      <div class="tag-list">
                        <span v-for="a in node.algorithms" :key="a" class="tag algo">{{ a }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'

const props = defineProps({
  stages: { type: Array, default: () => [] },
  sortValue: { type: String, default: 'path' },
  sortOptions: { type: Array, default: () => [] },
  sortLabel: { type: String, default: '学习路径' },
  activeNodeId: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
})

const emit = defineEmits(['update:sortValue', 'nodeClick', 'retry'])

const graphContainer = ref(null)
const graphWidth = ref(1200)
const graphHeight = ref(500)
const hoveredNode = ref(null)

// 颜色映射
const colors = {
  orange: '#f97316',
  purple: '#8b5cf6',
  blue: '#3b82f6',
  cyan: '#06b6d4',
  green: '#10b981',
  pink: '#ec4899',
  amber: '#f59e0b',
  indigo: '#6366f1'
}

// 构建水平分层图数据
const graphData = computed(() => {
  const nodes = []
  const edges = []

  // 第一层：基础
  const layer1 = [
    { id: 'l1-1', label: '数组和字符串', color: colors.orange, x: 0, y: 0, layer: 0 },
    { id: 'l1-2', label: '哈希表', color: colors.purple, x: 0, y: 1, layer: 0 },
    { id: 'l1-3', label: '算法面试小抄', color: colors.blue, x: 0, y: 2, layer: 0 }
  ]

  // 第二层
  const layer2 = [
    { id: 'l2-1', label: '排序算法', color: colors.purple, x: 1, y: 0.5, layer: 1 },
    { id: 'l2-2', label: '链表', color: colors.purple, x: 1, y: 1.5, layer: 1 },
    { id: 'l2-3', label: '深度优先搜索', color: colors.orange, x: 1, y: 2.5, layer: 1 },
    { id: 'l2-4', label: '广度优先搜索', color: colors.green, x: 1, y: 3.5, layer: 1 }
  ]

  // 第三层
  const layer3 = [
    { id: 'l3-1', label: '二分查找', color: colors.blue, x: 2, y: 0, layer: 2 },
    { id: 'l3-2', label: '普通树', color: colors.green, x: 2, y: 1, layer: 2 },
    { id: 'l3-3', label: '图', color: colors.cyan, x: 2, y: 2, layer: 2 }
  ]

  // 第四层
  const layer4 = [
    { id: 'l4-1', label: '前缀和', color: colors.blue, x: 3, y: -0.5, layer: 3 },
    { id: 'l4-2', label: '滑动窗口和双指针', color: colors.blue, x: 3, y: 0.5, layer: 3 },
    { id: 'l4-3', label: '队列与栈', color: colors.pink, x: 3, y: 1.5, layer: 3 },
    { id: 'l4-4', label: '堆', color: colors.pink, x: 3, y: 2.5, layer: 3 }
  ]

  // 第五层
  const layer5 = [
    { id: 'l5-1', label: '递归和分治', color: colors.purple, x: 4, y: 0, layer: 4 },
    { id: 'l5-2', label: '贪心算法', color: colors.amber, x: 4, y: 1, layer: 4 },
    { id: 'l5-3', label: '动态规划基础模型', color: colors.amber, x: 4, y: 2, layer: 4 }
  ]

  // 第六层
  const layer6 = [
    { id: 'l6-1', label: '位运算和数学', color: colors.blue, x: 5, y: -0.5, layer: 5 },
    { id: 'l6-2', label: '线性/区间问题', color: colors.indigo, x: 5, y: 0.5, layer: 5 },
    { id: 'l6-3', label: '状态、计数/数位问题', color: colors.indigo, x: 5, y: 1.5, layer: 5 },
    { id: 'l6-4', label: '树形、图上、概率/博弈问题', color: colors.green, x: 5, y: 2.5, layer: 5 }
  ]

  // 合并所有节点
  const allNodes = [...layer1, ...layer2, ...layer3, ...layer4, ...layer5, ...layer6]

  // 添加元数据
  allNodes.forEach(node => {
    const meta = getNodeMeta(node.label)
    Object.assign(node, meta)
  })

  // 定义连接关系
  const connections = [
    // 第一层到第二层
    ['l1-1', 'l2-1'], ['l1-1', 'l2-2'], ['l1-2', 'l2-2'],
    ['l1-2', 'l2-3'], ['l1-3', 'l2-3'], ['l1-3', 'l2-4'],
    // 第二层到第三层
    ['l2-1', 'l3-1'], ['l2-1', 'l3-2'], ['l2-2', 'l3-2'],
    ['l2-3', 'l3-3'], ['l2-4', 'l3-3'],
    // 第三层到第四层
    ['l3-1', 'l4-1'], ['l3-1', 'l4-2'], ['l3-2', 'l4-2'],
    ['l3-2', 'l4-3'], ['l3-3', 'l4-3'], ['l3-3', 'l4-4'],
    // 第四层到第五层
    ['l4-2', 'l5-1'], ['l4-2', 'l5-2'], ['l4-3', 'l5-2'],
    ['l4-3', 'l5-3'], ['l4-4', 'l5-3'],
    // 第五层到第六层
    ['l5-1', 'l6-1'], ['l5-1', 'l6-2'], ['l5-2', 'l6-2'],
    ['l5-2', 'l6-3'], ['l5-3', 'l6-3'], ['l5-3', 'l6-4']
  ]

  connections.forEach(([from, to]) => {
    edges.push({ from, to })
  })

  return { nodes: allNodes, edges }
})

// 计算节点位置
const graphLayers = computed(() => {
  const { nodes } = graphData.value
  const layers = []
  const maxLayer = Math.max(...nodes.map(n => n.layer))

  for (let i = 0; i <= maxLayer; i++) {
    const layerNodes = nodes.filter(n => n.layer === i)
    layers.push({ layer: i, nodes: layerNodes })
  }

  return layers
})

// 计算连接线
const connectionLines = computed(() => {
  const lines = []
  const { nodes, edges } = graphData.value
  const nodeMap = new Map(nodes.map(n => [n.id, n]))

  const layerWidth = graphWidth.value / 6
  const nodeHeight = 60
  const startY = 80

  edges.forEach(edge => {
    const from = nodeMap.get(edge.from)
    const to = nodeMap.get(edge.to)
    if (!from || !to) return

    const x1 = from.layer * layerWidth + 100
    const y1 = startY + from.y * nodeHeight + 20
    const x2 = to.layer * layerWidth + 20
    const y2 = startY + to.y * nodeHeight + 20

    // 水平曲线
    const midX = (x1 + x2) / 2
    const path = `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`

    lines.push({ path })
  })

  return lines
})

// 节点元数据
const getNodeMeta = (label) => {
  const metaMap = {
    '数组和字符串': {
      description: '最基础的数据结构，所有算法的起点',
      concepts: ['随机访问', '连续存储', '字符操作'],
      algorithms: ['遍历', '双指针', '滑动窗口']
    },
    '哈希表': {
      description: '键值对存储，O(1)查找效率',
      concepts: ['哈希函数', '冲突处理', '负载因子'],
      algorithms: ['开放寻址', '链地址法']
    },
    '算法面试小抄': {
      description: '常见算法面试题速查手册',
      concepts: ['模板', '套路', '技巧'],
      algorithms: ['快速解题', '代码模板']
    },
    '排序算法': {
      description: '将数据按特定顺序排列',
      concepts: ['比较排序', '稳定性', '时间复杂度'],
      algorithms: ['快排', '归并排序', '堆排序']
    },
    '链表': {
      description: '动态数据结构，灵活插入删除',
      concepts: ['指针', '节点', '头尾'],
      algorithms: ['快慢指针', '反转链表']
    },
    '深度优先搜索': {
      description: '沿着一条路径走到底再回溯',
      concepts: ['递归', '栈', '回溯'],
      algorithms: ['DFS', '全排列', '子集']
    },
    '广度优先搜索': {
      description: '层层推进的搜索策略',
      concepts: ['队列', '层级', '最短路径'],
      algorithms: ['BFS', '层序遍历']
    },
    '二分查找': {
      description: '在有序数组中快速查找',
      concepts: ['有序性', '减半', '边界'],
      algorithms: ['标准二分', '查找边界']
    },
    '普通树': {
      description: '层次化的数据结构',
      concepts: ['根节点', '子节点', '叶子'],
      algorithms: ['前中后序遍历', '层序遍历']
    },
    '图': {
      description: '节点和边的集合',
      concepts: ['顶点', '边', '连通性'],
      algorithms: ['Dijkstra', '拓扑排序']
    },
    '前缀和': {
      description: '预处理技术快速计算区间和',
      concepts: ['预处理', '差分', '区间查询'],
      algorithms: ['一维前缀和', '二维前缀和']
    },
    '滑动窗口和双指针': {
      description: '高效处理子数组问题',
      concepts: ['窗口', '指针移动', '单调性'],
      algorithms: ['滑动窗口', '对撞指针']
    },
    '队列与栈': {
      description: '基础线性数据结构',
      concepts: ['FIFO', 'LIFO', '单调'],
      algorithms: ['单调栈', '单调队列']
    },
    '堆': {
      description: '优先队列的高效实现',
      concepts: ['完全二叉树', '堆性质', '优先'],
      algorithms: ['堆排序', 'Top K']
    },
    '递归和分治': {
      description: '将大问题分解为小问题',
      concepts: ['递归', '分治', '合并'],
      algorithms: ['归并', '快速排序', '二分']
    },
    '贪心算法': {
      description: '每步选择局部最优',
      concepts: ['局部最优', '全局最优', '证明'],
      algorithms: ['区间调度', '哈夫曼编码']
    },
    '动态规划基础模型': {
      description: '通过子问题求解原问题',
      concepts: ['状态', '转移', '最优子结构'],
      algorithms: ['背包', '最长子序列']
    },
    '位运算和数学': {
      description: '底层运算和数学技巧',
      concepts: ['位操作', '数学公式', '数论'],
      algorithms: ['异或', '快速幂', 'GCD']
    },
    '线性/区间问题': {
      description: '一维数组相关问题',
      concepts: ['区间', '线段树', '树状数组'],
      algorithms: ['区间查询', '区间修改']
    },
    '状态、计数/数位问题': {
      description: '复杂状态的动态规划',
      concepts: ['状态压缩', '数位DP', '计数'],
      algorithms: ['状态压缩DP', '数位DP']
    },
    '树形、图上、概率/博弈问题': {
      description: '高级算法专题',
      concepts: ['树形DP', '博弈论', '概率'],
      algorithms: ['树形DP', 'Nim游戏']
    }
  }

  return metaMap[label] || {
    description: '算法知识点',
    concepts: ['基础概念'],
    algorithms: ['基础算法']
  }
}

// 处理悬停
const handleNodeHover = (node) => {
  hoveredNode.value = node
}

const handleNodeLeave = () => {
  hoveredNode.value = null
}

// 更新尺寸
const updateSize = () => {
  if (graphContainer.value) {
    graphWidth.value = graphContainer.value.offsetWidth
    graphHeight.value = graphContainer.value.offsetHeight
  }
}

onMounted(() => {
  nextTick(updateSize)
  window.addEventListener('resize', updateSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateSize)
})

watch(() => props.stages, () => {
  nextTick(updateSize)
}, { deep: true })
</script>

<style scoped>
.knowledge-graph-map {
  width: 100%;
}

/* 加载状态 */
.graph-loading {
  padding: 40px;
}

.graph-skeleton {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.skeleton-row {
  display: flex;
  gap: 20px;
  justify-content: space-around;
}

.skeleton-node {
  width: 120px;
  height: 40px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: skeleton 1.5s infinite;
  border-radius: 8px;
}

@keyframes skeleton {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 错误状态 */
.graph-error {
  text-align: center;
  padding: 60px 20px;
  color: #64748b;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.retry-btn {
  margin-top: 20px;
  padding: 10px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

/* 头部 */
.graph-header {
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
}

.sort-label select {
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
  outline: none;
}

/* 图容器 */
.graph-container {
  position: relative;
  min-height: 450px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 24px;
  padding: 40px 30px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  overflow-x: auto;
}

.graph-connections {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 节点层 */
.graph-layers {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  min-width: 1000px;
  height: 100%;
}

.graph-layer {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 140px;
}

/* 节点 */
.graph-node {
  position: relative;
  cursor: pointer;
}

.node-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.node-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #cbd5e1;
}

.graph-node.is-active .node-card {
  border-color: #3b82f6;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.2);
}

.node-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.node-text {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
  white-space: nowrap;
}

/* 提示框 */
.node-tooltip {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 12px;
  width: 280px;
  background: white;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(226, 232, 240, 0.8);
  z-index: 100;
}

.node-tooltip.tooltip-top {
  top: auto;
  bottom: 100%;
  margin-top: 0;
  margin-bottom: 12px;
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.tooltip-icon {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.tooltip-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.tooltip-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 14px;
}

.tooltip-section {
  margin-bottom: 12px;
}

.tooltip-section:last-child {
  margin-bottom: 0;
}

.section-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 4px 10px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
  color: #475569;
}

.tag.algo {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1d4ed8;
}

/* 动画 */
.tooltip-fade-enter-active,
.tooltip-fade-leave-active {
  transition: all 0.2s ease;
}

.tooltip-fade-enter-from,
.tooltip-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}

/* 响应式 */
@media (max-width: 1199px) {
  .graph-container {
    padding: 30px 20px;
  }

  .graph-layers {
    min-width: 800px;
  }
}

@media (max-width: 767px) {
  .graph-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .graph-container {
    padding: 20px 16px;
    border-radius: 20px;
  }

  .graph-layers {
    min-width: 600px;
  }

  .node-card {
    padding: 8px 12px;
  }

  .node-text {
    font-size: 12px;
  }

  .node-tooltip {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: calc(100vw - 40px);
    max-width: 320px;
    margin: 0;
  }
}
</style>
