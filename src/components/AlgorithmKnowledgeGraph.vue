<template>
  <div class="algorithm-knowledge-graph">
    <!-- 工具栏 -->
    <div class="graph-toolbar">
      <div class="toolbar-left">
        <h2 class="graph-title">算法知识图谱</h2>
        <span class="graph-subtitle">探索数据结构与算法的学习路径</span>
      </div>
      <div class="toolbar-right">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索知识点..."
            class="search-input"
          />
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="M21 21l-4.35-4.35"/>
          </svg>
        </div>
        <div class="toolbar-actions">
          <button
            class="toolbar-btn"
            :class="{ active: showGrid }"
            @click="showGrid = !showGrid"
            title="切换网格显示"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/>
              <rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/>
              <rect x="3" y="14" width="7" height="7"/>
            </svg>
          </button>
          <button class="toolbar-btn" @click="resetView" title="重置视图">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
              <path d="M3 3v5h5"/>
            </svg>
          </button>
          <button class="toolbar-btn" @click="zoomIn" title="放大">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
              <path d="M11 8v6M8 11h6"/>
            </svg>
          </button>
          <button class="toolbar-btn" @click="zoomOut" title="缩小">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="M21 21l-4.35-4.35"/>
              <path d="M8 11h6"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <button
        v-for="cat in categories"
        :key="cat.id"
        class="filter-btn"
        :class="{ active: selectedCategory === cat.id }"
        :style="{ '--cat-color': cat.color }"
        @click="selectedCategory = selectedCategory === cat.id ? '' : cat.id"
      >
        <span class="filter-dot" :style="{ backgroundColor: cat.color }"></span>
        {{ cat.name }}
      </button>
    </div>

    <!-- 图谱画布 -->
    <div
      class="graph-canvas-container"
      ref="canvasContainer"
      @mousedown="startPan"
      @mousemove="pan"
      @mouseup="endPan"
      @mouseleave="endPan"
      @wheel="handleWheel"
    >
      <div
        class="graph-canvas"
        :style="canvasTransform"
        ref="graphCanvas"
      >
        <!-- 网格背景 -->
        <div v-if="showGrid" class="grid-background">
          <div
            v-for="i in gridRows"
            :key="`row-${i}`"
            class="grid-row"
          >
            <div
              v-for="j in gridCols"
              :key="`col-${j}`"
              class="grid-cell"
            ></div>
          </div>
        </div>

        <!-- SVG 路径层 -->
        <svg class="paths-layer" :width="canvasWidth" :height="canvasHeight">
          <defs>
            <!-- 箭头标记 -->
            <marker
              v-for="cat in categories"
              :key="`arrow-${cat.id}`"
              :id="`arrow-${cat.id}`"
              markerWidth="10"
              markerHeight="10"
              refX="9"
              refY="3"
              orient="auto"
            >
              <path d="M0,0 L0,6 L9,3 z" :fill="cat.color"/>
            </marker>
            <!-- 渐变定义 -->
            <linearGradient id="pathGradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" stop-color="#94a3b8" stop-opacity="0.3"/>
              <stop offset="50%" stop-color="#64748b" stop-opacity="0.6"/>
              <stop offset="100%" stop-color="#94a3b8" stop-opacity="0.3"/>
            </linearGradient>
          </defs>

          <!-- 连接线 -->
          <g class="connections">
            <path
              v-for="(conn, index) in visibleConnections"
              :key="`conn-${index}`"
              :d="conn.path"
              class="connection-path"
              :class="{ highlighted: isPathHighlighted(conn) }"
              :stroke="conn.color"
              :marker-end="`url(#arrow-${conn.targetCategory})`"
            />
          </g>
        </svg>

        <!-- 节点层 -->
        <div class="nodes-layer">
          <div
            v-for="node in visibleNodes"
            :key="node.id"
            class="knowledge-node"
            :class="[
              `node-${node.category}`,
              { 
                'is-highlighted': isNodeHighlighted(node),
                'is-dimmed': isNodeDimmed(node),
                'is-root': node.isRoot
              }
            ]"
            :style="getNodeStyle(node)"
            @mouseenter="handleNodeEnter(node)"
            @mouseleave="handleNodeLeave"
            @click="handleNodeClick(node)"
          >
            <div class="node-content">
              <div class="node-icon-wrapper">
                <div class="node-icon" :style="{ backgroundColor: node.color }">
                  <span v-if="node.isRoot" class="root-icon">★</span>
                  <span v-else class="node-number">{{ node.level }}</span>
                </div>
              </div>
              <div class="node-text">
                <span class="node-label">{{ node.label }}</span>
                <span v-if="node.isRoot" class="node-badge">起点</span>
              </div>
            </div>

            <!-- 悬停详情面板 -->
            <transition name="panel-slide">
              <div
                v-if="hoveredNode?.id === node.id"
                class="node-detail-panel"
                :class="{ 'panel-left': node.x > canvasWidth / 2 }"
              >
                <div class="panel-header" :style="{ borderLeftColor: node.color }">
                  <h3>{{ node.label }}</h3>
                  <span class="panel-category" :style="{ color: node.color }">
                    {{ getCategoryName(node.category) }}
                  </span>
                </div>
                <div class="panel-body">
                  <p class="panel-description">{{ node.description }}</p>
                  
                  <div v-if="node.concepts?.length" class="panel-section">
                    <h4>核心概念</h4>
                    <div class="concept-tags">
                      <span
                        v-for="concept in node.concepts"
                        :key="concept"
                        class="concept-tag"
                      >{{ concept }}</span>
                    </div>
                  </div>

                  <div v-if="node.algorithms?.length" class="panel-section">
                    <h4>关键算法</h4>
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
                    <h4>前置知识</h4>
                    <div class="prereq-list">
                      <span
                        v-for="prereq in node.prerequisites"
                        :key="prereq"
                        class="prereq-item"
                      >{{ prereq }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </div>

    <!-- 图例 -->
    <div class="graph-legend">
      <div class="legend-title">知识分类</div>
      <div class="legend-items">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="legend-item"
          @mouseenter="highlightCategory(cat.id)"
          @mouseleave="clearHighlight"
        >
          <span class="legend-dot" :style="{ backgroundColor: cat.color }"></span>
          <span class="legend-label">{{ cat.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// 画布配置
const GRID_SIZE = 140
const NODE_WIDTH = 120
const NODE_HEIGHT = 50
const canvasWidth = ref(1400)
const canvasHeight = ref(800)

// 状态
const showGrid = ref(false)
const searchQuery = ref('')
const selectedCategory = ref('')
const hoveredNode = ref(null)
const highlightedCategory = ref('')
const scale = ref(1)
const panX = ref(0)
const panY = ref(0)
const isPanning = ref(false)
const lastMouseX = ref(0)
const lastMouseY = ref(0)

// 分类定义
const categories = [
  { id: 'foundation', name: '基础', color: '#f59e0b' },
  { id: 'linear', name: '线性结构', color: '#3b82f6' },
  { id: 'tree', name: '树结构', color: '#10b981' },
  { id: 'graph', name: '图论', color: '#8b5cf6' },
  { id: 'dp', name: '动态规划', color: '#ec4899' },
  { id: 'advanced', name: '高级算法', color: '#06b6d4' }
]

// 知识节点数据
const knowledgeNodes = [
  // 基础入口 - 中心节点
  {
    id: 'root',
    label: '基础入口',
    category: 'foundation',
    color: '#f59e0b',
    x: 4, y: 4,
    isRoot: true,
    level: 0,
    description: '算法学习的起点，掌握基础数据结构和编程技巧',
    concepts: ['时间复杂度', '空间复杂度', '递归', '迭代'],
    algorithms: [
      { name: '基础遍历', complexity: 'O(n)' },
      { name: '二分查找', complexity: 'O(log n)' }
    ]
  },
  // 基础层
  {
    id: 'array',
    label: '数组',
    category: 'foundation',
    color: '#f59e0b',
    x: 2, y: 2,
    level: 1,
    description: '连续内存存储的线性数据结构',
    concepts: ['随机访问', '连续存储', '索引'],
    algorithms: [
      { name: '线性查找', complexity: 'O(n)' },
      { name: '二分查找', complexity: 'O(log n)' }
    ],
    prerequisites: ['基础入口']
  },
  {
    id: 'string',
    label: '字符串',
    category: 'foundation',
    color: '#f59e0b',
    x: 2, y: 4,
    level: 1,
    description: '字符序列的处理与操作',
    concepts: ['字符编码', '子串', '模式匹配'],
    algorithms: [
      { name: 'KMP算法', complexity: 'O(n+m)' },
      { name: '字符串哈希', complexity: 'O(n)' }
    ],
    prerequisites: ['数组']
  },
  {
    id: 'hash',
    label: '哈希表',
    category: 'foundation',
    color: '#f59e0b',
    x: 2, y: 6,
    level: 1,
    description: '键值对存储，O(1)查找效率',
    concepts: ['哈希函数', '冲突处理', '负载因子'],
    algorithms: [
      { name: '开放寻址', complexity: 'O(1)' },
      { name: '链地址法', complexity: 'O(1)' }
    ],
    prerequisites: ['基础入口']
  },
  // 线性结构层
  {
    id: 'linkedlist',
    label: '链表',
    category: 'linear',
    color: '#3b82f6',
    x: 5, y: 2,
    level: 2,
    description: '动态数据结构，灵活插入删除',
    concepts: ['指针', '节点', '头尾'],
    algorithms: [
      { name: '反转链表', complexity: 'O(n)' },
      { name: '快慢指针', complexity: 'O(n)' }
    ],
    prerequisites: ['数组']
  },
  {
    id: 'stack',
    label: '栈',
    category: 'linear',
    color: '#3b82f6',
    x: 5, y: 4,
    level: 2,
    description: '后进先出的线性结构',
    concepts: ['LIFO', '栈顶', '括号匹配'],
    algorithms: [
      { name: '单调栈', complexity: 'O(n)' },
      { name: '表达式求值', complexity: 'O(n)' }
    ],
    prerequisites: ['链表']
  },
  {
    id: 'queue',
    label: '队列',
    category: 'linear',
    color: '#3b82f6',
    x: 5, y: 6,
    level: 2,
    description: '先进先出的线性结构',
    concepts: ['FIFO', '队首', '队尾'],
    algorithms: [
      { name: '单调队列', complexity: 'O(n)' },
      { name: 'BFS', complexity: 'O(V+E)' }
    ],
    prerequisites: ['栈']
  },
  // 树结构层
  {
    id: 'bst',
    label: '二叉搜索树',
    category: 'tree',
    color: '#10b981',
    x: 7, y: 2,
    level: 3,
    description: '有序树结构支持高效查找',
    concepts: ['有序性', '左右子树', '平衡'],
    algorithms: [
      { name: '插入/删除', complexity: 'O(log n)' },
      { name: '查找', complexity: 'O(log n)' }
    ],
    prerequisites: ['链表', '栈']
  },
  {
    id: 'heap',
    label: '堆',
    category: 'tree',
    color: '#10b981',
    x: 7, y: 4,
    level: 3,
    description: '优先队列的高效实现',
    concepts: ['完全二叉树', '堆性质', '优先队列'],
    algorithms: [
      { name: '堆排序', complexity: 'O(n log n)' },
      { name: 'Top K', complexity: 'O(n log k)' }
    ],
    prerequisites: ['二叉搜索树']
  },
  {
    id: 'trie',
    label: 'Trie树',
    category: 'tree',
    color: '#10b981',
    x: 7, y: 6,
    level: 3,
    description: '前缀树，高效处理字符串',
    concepts: ['前缀', '字典树', '多叉树'],
    algorithms: [
      { name: '前缀查找', complexity: 'O(m)' },
      { name: '自动补全', complexity: 'O(m)' }
    ],
    prerequisites: ['字符串', '二叉搜索树']
  },
  // 图论层
  {
    id: 'graph-basic',
    label: '图基础',
    category: 'graph',
    color: '#8b5cf6',
    x: 9, y: 2,
    level: 4,
    description: '图的基本概念和表示',
    concepts: ['顶点', '边', '邻接表', '邻接矩阵'],
    algorithms: [
      { name: 'DFS', complexity: 'O(V+E)' },
      { name: 'BFS', complexity: 'O(V+E)' }
    ],
    prerequisites: ['队列', '栈']
  },
  {
    id: 'shortest-path',
    label: '最短路径',
    category: 'graph',
    color: '#8b5cf6',
    x: 9, y: 4,
    level: 4,
    description: '图中两点间的最短路径算法',
    concepts: ['松弛操作', '贪心', '动态规划'],
    algorithms: [
      { name: 'Dijkstra', complexity: 'O((V+E)log V)' },
      { name: 'Floyd', complexity: 'O(V³)' },
      { name: 'Bellman-Ford', complexity: 'O(VE)' }
    ],
    prerequisites: ['图基础', '堆']
  },
  {
    id: 'mst',
    label: '最小生成树',
    category: 'graph',
    color: '#8b5cf6',
    x: 9, y: 6,
    level: 4,
    description: '连接所有顶点的最小权重树',
    concepts: ['贪心', '割性质', '环性质'],
    algorithms: [
      { name: 'Prim', complexity: 'O(E log V)' },
      { name: 'Kruskal', complexity: 'O(E log E)' }
    ],
    prerequisites: ['图基础', '并查集']
  },
  // 动态规划层
  {
    id: 'dp-basic',
    label: 'DP基础',
    category: 'dp',
    color: '#ec4899',
    x: 11, y: 2,
    level: 5,
    description: '动态规划的核心思想',
    concepts: ['最优子结构', '重叠子问题', '状态转移'],
    algorithms: [
      { name: '记忆化搜索', complexity: 'O(n)' },
      { name: '递推', complexity: 'O(n)' }
    ],
    prerequisites: ['递归', '基础入口']
  },
  {
    id: 'knapsack',
    label: '背包问题',
    category: 'dp',
    color: '#ec4899',
    x: 11, y: 4,
    level: 5,
    description: '经典的动态规划模型',
    concepts: ['状态定义', '选择', '容量'],
    algorithms: [
      { name: '01背包', complexity: 'O(nW)' },
      { name: '完全背包', complexity: 'O(nW)' }
    ],
    prerequisites: ['DP基础']
  },
  {
    id: 'sequence-dp',
    label: '序列DP',
    category: 'dp',
    color: '#ec4899',
    x: 11, y: 6,
    level: 5,
    description: '序列相关的动态规划',
    concepts: ['子序列', '子数组', '状态设计'],
    algorithms: [
      { name: 'LIS', complexity: 'O(n log n)' },
      { name: 'LCS', complexity: 'O(nm)' },
      { name: '编辑距离', complexity: 'O(nm)' }
    ],
    prerequisites: ['DP基础', '字符串']
  },
  // 高级算法层
  {
    id: 'union-find',
    label: '并查集',
    category: 'advanced',
    color: '#06b6d4',
    x: 6, y: 8,
    level: 3,
    description: '处理不相交集合的合并与查询',
    concepts: ['集合合并', '路径压缩', '按秩合并'],
    algorithms: [
      { name: 'Find', complexity: 'O(α(n))' },
      { name: 'Union', complexity: 'O(α(n))' }
    ],
    prerequisites: ['数组']
  },
  {
    id: 'segment-tree',
    label: '线段树',
    category: 'advanced',
    color: '#06b6d4',
    x: 8, y: 8,
    level: 4,
    description: '区间查询和修改的数据结构',
    concepts: ['区间', '懒惰标记', '分治'],
    algorithms: [
      { name: '区间查询', complexity: 'O(log n)' },
      { name: '区间修改', complexity: 'O(log n)' }
    ],
    prerequisites: ['二叉搜索树', '递归']
  },
  {
    id: 'bit',
    label: '树状数组',
    category: 'advanced',
    color: '#06b6d4',
    x: 10, y: 8,
    level: 4,
    description: '高效处理前缀和问题',
    concepts: ['lowbit', '前缀和', '单点修改'],
    algorithms: [
      { name: '查询', complexity: 'O(log n)' },
      { name: '修改', complexity: 'O(log n)' }
    ],
    prerequisites: ['数组', '二叉搜索树']
  }
]

// 连接关系
const connections = [
  // 基础层连接
  { source: 'root', target: 'array' },
  { source: 'root', target: 'hash' },
  { source: 'array', target: 'string' },
  // 线性结构
  { source: 'array', target: 'linkedlist' },
  { source: 'linkedlist', target: 'stack' },
  { source: 'stack', target: 'queue' },
  // 树结构
  { source: 'linkedlist', target: 'bst' },
  { source: 'stack', target: 'bst' },
  { source: 'bst', target: 'heap' },
  { source: 'string', target: 'trie' },
  { source: 'bst', target: 'trie' },
  // 图论
  { source: 'queue', target: 'graph-basic' },
  { source: 'stack', target: 'graph-basic' },
  { source: 'graph-basic', target: 'shortest-path' },
  { source: 'heap', target: 'shortest-path' },
  { source: 'graph-basic', target: 'mst' },
  // 动态规划
  { source: 'root', target: 'dp-basic' },
  { source: 'dp-basic', target: 'knapsack' },
  { source: 'dp-basic', target: 'sequence-dp' },
  { source: 'string', target: 'sequence-dp' },
  // 高级算法
  { source: 'array', target: 'union-find' },
  { source: 'bst', target: 'segment-tree' },
  { source: 'bst', target: 'bit' },
  { source: 'array', target: 'bit' }
]

// 计算网格尺寸
const gridCols = computed(() => Math.ceil(canvasWidth.value / GRID_SIZE))
const gridRows = computed(() => Math.ceil(canvasHeight.value / GRID_SIZE))

// 筛选节点
const visibleNodes = computed(() => {
  let nodes = knowledgeNodes

  // 分类筛选
  if (selectedCategory.value) {
    nodes = nodes.filter(n => n.category === selectedCategory.value)
  }

  // 搜索筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    nodes = nodes.filter(n =>
      n.label.toLowerCase().includes(query) ||
      n.description.toLowerCase().includes(query) ||
      n.concepts?.some(c => c.toLowerCase().includes(query))
    )
  }

  return nodes
})

// 计算连接线
const visibleConnections = computed(() => {
  const nodeIds = new Set(visibleNodes.value.map(n => n.id))

  return connections
    .filter(conn => nodeIds.has(conn.source) && nodeIds.has(conn.target))
    .map(conn => {
      const source = knowledgeNodes.find(n => n.id === conn.source)
      const target = knowledgeNodes.find(n => n.id === conn.target)

      const x1 = source.x * GRID_SIZE + NODE_WIDTH / 2
      const y1 = source.y * GRID_SIZE + NODE_HEIGHT / 2
      const x2 = target.x * GRID_SIZE + NODE_WIDTH / 2
      const y2 = target.y * GRID_SIZE + NODE_HEIGHT / 2

      // 水平曲线
      const midX = (x1 + x2) / 2
      const path = `M ${x1} ${y1} C ${midX} ${y1}, ${midX} ${y2}, ${x2} ${y2}`

      return {
        ...conn,
        path,
        color: target.color,
        targetCategory: target.category
      }
    })
})

// 画布变换
const canvasTransform = computed(() => ({
  transform: `translate(${panX.value}px, ${panY.value}px) scale(${scale.value})`,
  transformOrigin: '0 0'
}))

// 获取节点样式
const getNodeStyle = (node) => ({
  left: `${node.x * GRID_SIZE}px`,
  top: `${node.y * GRID_SIZE}px`,
  width: `${NODE_WIDTH}px`,
  height: `${NODE_HEIGHT}px`
})

// 节点高亮判断
const isNodeHighlighted = (node) => {
  if (!hoveredNode.value) return false
  if (hoveredNode.value.id === node.id) return true

  // 检查是否是前置或后置节点
  const isConnected = connections.some(conn =>
    (conn.source === hoveredNode.value.id && conn.target === node.id) ||
    (conn.target === hoveredNode.value.id && conn.source === node.id)
  )
  return isConnected
}

// 节点变暗判断
const isNodeDimmed = (node) => {
  if (!hoveredNode.value) return false
  return hoveredNode.value.id !== node.id && !isNodeHighlighted(node)
}

// 路径高亮判断
const isPathHighlighted = (conn) => {
  if (!hoveredNode.value) return false
  return conn.source === hoveredNode.value.id || conn.target === hoveredNode.value.id
}

// 事件处理
const handleNodeEnter = (node) => {
  hoveredNode.value = node
}

const handleNodeLeave = () => {
  hoveredNode.value = null
}

const handleNodeClick = (node) => {
  console.log('Clicked:', node.label)
}

const highlightCategory = (catId) => {
  highlightedCategory.value = catId
}

const clearHighlight = () => {
  highlightedCategory.value = ''
}

// 平移操作
const startPan = (e) => {
  isPanning.value = true
  lastMouseX.value = e.clientX
  lastMouseY.value = e.clientY
}

const pan = (e) => {
  if (!isPanning.value) return
  const dx = e.clientX - lastMouseX.value
  const dy = e.clientY - lastMouseY.value
  panX.value += dx
  panY.value += dy
  lastMouseX.value = e.clientX
  lastMouseY.value = e.clientY
}

const endPan = () => {
  isPanning.value = false
}

// 缩放操作
const handleWheel = (e) => {
  e.preventDefault()
  const delta = e.deltaY > 0 ? 0.9 : 1.1
  scale.value = Math.max(0.5, Math.min(2, scale.value * delta))
}

const zoomIn = () => {
  scale.value = Math.min(2, scale.value * 1.2)
}

const zoomOut = () => {
  scale.value = Math.max(0.5, scale.value / 1.2)
}

const resetView = () => {
  scale.value = 1
  panX.value = 0
  panY.value = 0
}

// 辅助函数
const getCategoryName = (catId) => {
  return categories.find(c => c.id === catId)?.name || catId
}

onMounted(() => {
  // 初始化画布居中
  const container = document.querySelector('.graph-canvas-container')
  if (container) {
    panX.value = (container.offsetWidth - canvasWidth.value) / 2
    panY.value = 50
  }
})
</script>

<style scoped>
.algorithm-knowledge-graph {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fafafa;
  border-radius: 16px;
  overflow: hidden;
}

/* 工具栏 */
.graph-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.graph-title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.graph-subtitle {
  font-size: 13px;
  color: #6b7280;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-box {
  position: relative;
}

.search-input {
  width: 240px;
  padding: 10px 16px 10px 40px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}

.search-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: #9ca3af;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
}

.toolbar-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.toolbar-btn.active {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

.toolbar-btn svg {
  width: 20px;
  height: 20px;
}

/* 分类筛选 */
.category-filter {
  display: flex;
  gap: 10px;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  overflow-x: auto;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f3f4f6;
  border: 1px solid transparent;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.filter-btn:hover {
  background: #e5e7eb;
}

.filter-btn.active {
  background: white;
  border-color: var(--cat-color);
  color: var(--cat-color);
}

.filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* 画布容器 */
.graph-canvas-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  cursor: grab;
  background: linear-gradient(135deg, #fafafa 0%, #f3f4f6 100%);
}

.graph-canvas-container:active {
  cursor: grabbing;
}

.graph-canvas {
  position: relative;
  width: v-bind('canvasWidth + "px"');
  height: v-bind('canvasHeight + "px"');
  transition: transform 0.1s ease-out;
}

/* 网格背景 */
.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  pointer-events: none;
}

.grid-row {
  display: flex;
  flex: 1;
}

.grid-cell {
  flex: 1;
  border-right: 1px dashed #e5e7eb;
  border-bottom: 1px dashed #e5e7eb;
}

/* SVG 路径层 */
.paths-layer {
  position: absolute;
  top: 0;
  left: 0;
  pointer-events: none;
}

.connection-path {
  stroke-width: 2;
  fill: none;
  opacity: 0.4;
  transition: all 0.3s;
}

.connection-path.highlighted {
  stroke-width: 3;
  opacity: 1;
  filter: drop-shadow(0 0 4px currentColor);
}

/* 节点层 */
.nodes-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.knowledge-node {
  position: absolute;
  z-index: 10;
}

.node-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: white;
  border-radius: 12px;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.node-content:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.knowledge-node.is-root .node-content {
  background: linear-gradient(135deg, #1f2937 0%, #374151 100%);
  border-color: #f59e0b;
}

.knowledge-node.is-highlighted .node-content {
  border-color: currentColor;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.knowledge-node.is-dimmed .node-content {
  opacity: 0.3;
}

.node-icon-wrapper {
  position: relative;
}

.node-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
}

.root-icon {
  font-size: 14px;
}

.node-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.node-label {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
}

.knowledge-node.is-root .node-label {
  color: white;
}

.node-badge {
  font-size: 10px;
  font-weight: 700;
  color: #f59e0b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* 详情面板 */
.node-detail-panel {
  position: absolute;
  top: 0;
  left: calc(100% + 16px);
  width: 320px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  border: 1px solid #e5e7eb;
  z-index: 100;
  overflow: hidden;
}

.node-detail-panel.panel-left {
  left: auto;
  right: calc(100% + 16px);
}

.panel-header {
  padding: 16px 20px;
  border-left: 4px solid;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 6px;
}

.panel-category {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.panel-body {
  padding: 16px 20px;
}

.panel-description {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
  margin: 0 0 16px;
}

.panel-section {
  margin-bottom: 16px;
}

.panel-section:last-child {
  margin-bottom: 0;
}

.panel-section h4 {
  font-size: 11px;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 10px;
}

.concept-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.concept-tag {
  padding: 4px 10px;
  background: #f3f4f6;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  color: #4b5563;
}

.algorithm-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.algorithm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.algo-name {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

.algo-complexity {
  font-size: 11px;
  font-weight: 500;
  color: #6b7280;
  font-family: 'JetBrains Mono', monospace;
}

.prereq-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.prereq-item {
  padding: 4px 10px;
  background: #fef3c7;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  color: #92400e;
}

/* 动画 */
.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

.panel-slide-enter-from.panel-left,
.panel-slide-leave-to.panel-left {
  transform: translateX(10px);
}

/* 图例 */
.graph-legend {
  padding: 16px 24px;
  background: white;
  border-top: 1px solid #e5e7eb;
}

.legend-title {
  font-size: 12px;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}

.legend-items {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.legend-item:hover {
  opacity: 0.7;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-label {
  font-size: 13px;
  font-weight: 500;
  color: #4b5563;
}

/* 响应式 */
@media (max-width: 768px) {
  .graph-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-right {
    flex-direction: column;
  }

  .search-input {
    width: 100%;
  }

  .category-filter {
    padding: 12px 16px;
  }

  .node-detail-panel {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) !important;
    right: auto;
    width: calc(100vw - 32px);
    max-width: 360px;
  }
}
</style>
