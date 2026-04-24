import { ref, computed } from 'vue'

const CATEGORIES = [
  { id: 'foundation', name: '基础', color: '#f59e0b', lightColor: '#fef3c7' },
  { id: 'linear', name: '线性结构', color: '#3b82f6', lightColor: '#dbeafe' },
  { id: 'tree', name: '树结构', color: '#10b981', lightColor: '#d1fae5' },
  { id: 'graph', name: '图论', color: '#8b5cf6', lightColor: '#ede9fe' },
  { id: 'dp', name: '动态规划', color: '#ec4899', lightColor: '#fce7f3' },
  { id: 'advanced', name: '高级算法', color: '#06b6d4', lightColor: '#cffafe' }
]

const KNOWLEDGE_NODES = [
  {
    id: 'root',
    label: '基础入口',
    category: 'foundation',
    level: 0,
    isRoot: true,
    symbolSize: 65,
    description: '算法学习的起点，掌握基础数据结构和编程技巧',
    concepts: ['时间复杂度', '空间复杂度', '递归', '迭代'],
    algorithms: [
      { name: '基础遍历', complexity: 'O(n)' },
      { name: '二分查找', complexity: 'O(log n)' }
    ],
    prerequisites: []
  },
  {
    id: 'array',
    label: '数组',
    category: 'foundation',
    level: 1,
    symbolSize: 45,
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
    level: 1,
    symbolSize: 45,
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
    level: 1,
    symbolSize: 45,
    description: '键值对存储，O(1)查找效率',
    concepts: ['哈希函数', '冲突处理', '负载因子'],
    algorithms: [
      { name: '开放寻址', complexity: 'O(1)' },
      { name: '链地址法', complexity: 'O(1)' }
    ],
    prerequisites: ['基础入口']
  },
  {
    id: 'linkedlist',
    label: '链表',
    category: 'linear',
    level: 2,
    symbolSize: 45,
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
    level: 2,
    symbolSize: 45,
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
    level: 2,
    symbolSize: 45,
    description: '先进先出的线性结构',
    concepts: ['FIFO', '队首', '队尾'],
    algorithms: [
      { name: '单调队列', complexity: 'O(n)' },
      { name: 'BFS', complexity: 'O(V+E)' }
    ],
    prerequisites: ['栈']
  },
  {
    id: 'bst',
    label: '二叉搜索树',
    category: 'tree',
    level: 3,
    symbolSize: 45,
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
    level: 3,
    symbolSize: 45,
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
    level: 3,
    symbolSize: 45,
    description: '前缀树，高效处理字符串',
    concepts: ['前缀', '字典树', '多叉树'],
    algorithms: [
      { name: '前缀查找', complexity: 'O(m)' },
      { name: '自动补全', complexity: 'O(m)' }
    ],
    prerequisites: ['字符串', '二叉搜索树']
  },
  {
    id: 'graph-basic',
    label: '图基础',
    category: 'graph',
    level: 4,
    symbolSize: 45,
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
    level: 4,
    symbolSize: 45,
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
    level: 4,
    symbolSize: 45,
    description: '连接所有顶点的最小权重树',
    concepts: ['贪心', '割性质', '环性质'],
    algorithms: [
      { name: 'Prim', complexity: 'O(E log V)' },
      { name: 'Kruskal', complexity: 'O(E log E)' }
    ],
    prerequisites: ['图基础', '并查集']
  },
  {
    id: 'dp-basic',
    label: 'DP基础',
    category: 'dp',
    level: 5,
    symbolSize: 45,
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
    level: 5,
    symbolSize: 45,
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
    level: 5,
    symbolSize: 45,
    description: '序列相关的动态规划',
    concepts: ['子序列', '子数组', '状态设计'],
    algorithms: [
      { name: 'LIS', complexity: 'O(n log n)' },
      { name: 'LCS', complexity: 'O(nm)' },
      { name: '编辑距离', complexity: 'O(nm)' }
    ],
    prerequisites: ['DP基础', '字符串']
  },
  {
    id: 'union-find',
    label: '并查集',
    category: 'advanced',
    level: 3,
    symbolSize: 45,
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
    level: 4,
    symbolSize: 45,
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
    level: 4,
    symbolSize: 45,
    description: '高效处理前缀和问题',
    concepts: ['lowbit', '前缀和', '单点修改'],
    algorithms: [
      { name: '查询', complexity: 'O(log n)' },
      { name: '修改', complexity: 'O(log n)' }
    ],
    prerequisites: ['数组', '二叉搜索树']
  }
]

const CONNECTIONS = [
  { source: 'root', target: 'array' },
  { source: 'root', target: 'hash' },
  { source: 'array', target: 'string' },
  { source: 'array', target: 'linkedlist' },
  { source: 'linkedlist', target: 'stack' },
  { source: 'stack', target: 'queue' },
  { source: 'linkedlist', target: 'bst' },
  { source: 'stack', target: 'bst' },
  { source: 'bst', target: 'heap' },
  { source: 'string', target: 'trie' },
  { source: 'bst', target: 'trie' },
  { source: 'queue', target: 'graph-basic' },
  { source: 'stack', target: 'graph-basic' },
  { source: 'graph-basic', target: 'shortest-path' },
  { source: 'heap', target: 'shortest-path' },
  { source: 'graph-basic', target: 'mst' },
  { source: 'root', target: 'dp-basic' },
  { source: 'dp-basic', target: 'knapsack' },
  { source: 'dp-basic', target: 'sequence-dp' },
  { source: 'string', target: 'sequence-dp' },
  { source: 'array', target: 'union-find' },
  { source: 'bst', target: 'segment-tree' },
  { source: 'bst', target: 'bit' },
  { source: 'array', target: 'bit' }
]

export function useKnowledgeGraph() {
  const searchQuery = ref('')
  const selectedCategory = ref('')
  const selectedNode = ref(null)
  const hoveredNodeId = ref(null)
  const layoutType = ref('force')

  const nodeMap = computed(() => {
    const map = {}
    KNOWLEDGE_NODES.forEach(n => { map[n.id] = n })
    return map
  })

  const categoryMap = computed(() => {
    const map = {}
    CATEGORIES.forEach(c => { map[c.id] = c })
    return map
  })

  const filteredNodeIds = computed(() => {
    let nodes = KNOWLEDGE_NODES
    if (selectedCategory.value) {
      nodes = nodes.filter(n => n.category === selectedCategory.value)
    }
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      nodes = nodes.filter(n =>
        n.label.toLowerCase().includes(q) ||
        n.description.toLowerCase().includes(q) ||
        n.concepts?.some(c => c.toLowerCase().includes(q)) ||
        n.algorithms?.some(a => a.name.toLowerCase().includes(q))
      )
    }
    return new Set(nodes.map(n => n.id))
  })

  const connectedNodeIds = computed(() => {
    if (!hoveredNodeId.value) return new Set()
    const ids = new Set([hoveredNodeId.value])
    CONNECTIONS.forEach(c => {
      if (c.source === hoveredNodeId.value) ids.add(c.target)
      if (c.target === hoveredNodeId.value) ids.add(c.source)
    })
    return ids
  })

  const stats = computed(() => ({
    nodeCount: KNOWLEDGE_NODES.length,
    categoryCount: CATEGORIES.length,
    connectionCount: CONNECTIONS.length
  }))

  function buildEChartsNodes() {
    return KNOWLEDGE_NODES.map(node => {
      const cat = categoryMap.value[node.category]
      const isFiltered = filteredNodeIds.value.has(node.id)
      const isHovered = hoveredNodeId.value === node.id
      const isConnected = connectedNodeIds.value.has(node.id)
      const isDimmed = hoveredNodeId.value && !isHovered && !isConnected
      const isSearchFiltered = !isFiltered && (selectedCategory.value || searchQuery.value)

      let opacity = 1
      let borderWidth = 2
      let borderColor = 'rgba(255,255,255,0.8)'

      if (isSearchFiltered) {
        opacity = 0.1
      } else if (isDimmed) {
        opacity = 0.2
      } else if (isHovered) {
        borderWidth = 4
        borderColor = '#fff'
      } else if (isConnected) {
        borderWidth = 3
        borderColor = '#fff'
      }

      return {
        id: node.id,
        name: node.label,
        symbolSize: node.symbolSize,
        category: CATEGORIES.findIndex(c => c.id === node.category),
        itemStyle: {
          color: cat.color,
          borderColor,
          borderWidth,
          opacity,
          shadowBlur: isHovered ? 24 : (isConnected ? 16 : 8),
          shadowColor: `${cat.color}88`,
          shadowOffsetY: 3
        },
        label: {
          show: true,
          fontSize: node.isRoot ? 14 : 12,
          fontWeight: node.isRoot ? 800 : 600,
          color: '#1e293b',
          position: 'inside',
          opacity: isSearchFiltered || isDimmed ? 0.15 : 1
        },
        _rawData: node
      }
    })
  }

  function buildEChartsLinks() {
    return CONNECTIONS.map(conn => {
      const targetNode = nodeMap.value[conn.target]
      const sourceNode = nodeMap.value[conn.source]
      const cat = categoryMap.value[targetNode?.category]
      const isHighlighted = hoveredNodeId.value &&
        (conn.source === hoveredNodeId.value || conn.target === hoveredNodeId.value)
      const isSearchFiltered = !filteredNodeIds.value.has(conn.source) || !filteredNodeIds.value.has(conn.target)

      let opacity = 0.3
      let width = 1.5

      if (isSearchFiltered && (selectedCategory.value || searchQuery.value)) {
        opacity = 0.04
        width = 0.5
      } else if (isHighlighted) {
        opacity = 0.85
        width = 3
      }

      return {
        source: conn.source,
        target: conn.target,
        lineStyle: {
          color: isHighlighted ? cat?.color : '#94a3b8',
          width,
          opacity,
          curveness: 0.15
        }
      }
    })
  }

  const chartOption = computed(() => {
    const nodes = buildEChartsNodes()
    const links = buildEChartsLinks()

    const baseSeries = {
      type: 'graph',
      data: nodes,
      links,
      categories: CATEGORIES.map(c => ({ name: c.name })),
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'inside',
        fontSize: 12,
        fontWeight: 600
      },
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [4, 10],
      emphasis: {
        focus: 'adjacency',
        itemStyle: {
          borderWidth: 4,
          borderColor: '#fff'
        },
        lineStyle: {
          width: 3,
          opacity: 1
        },
        label: {
          fontSize: 14,
          fontWeight: 700
        }
      },
      itemStyle: {
        shadowBlur: 8,
        shadowColor: 'rgba(0,0,0,0.12)',
        shadowOffsetY: 3
      }
    }

    if (layoutType.value === 'force') {
      return {
        tooltip: { show: false },
        legend: { show: false },
        animationDuration: 800,
        animationEasingUpdate: 'quinticInOut',
        series: [{
          ...baseSeries,
          layout: 'force',
          force: {
            repulsion: 380,
            edgeLength: [130, 260],
            gravity: 0.06,
            friction: 0.6,
            layoutAnimation: true
          }
        }]
      }
    }

    return {
      tooltip: { show: false },
      legend: { show: false },
      animationDuration: 800,
      animationEasingUpdate: 'quinticInOut',
      series: [{
        ...baseSeries,
        layout: 'circular',
        circular: {
          rotateLabel: true
        }
      }]
    }
  })

  function selectCategory(catId) {
    selectedCategory.value = selectedCategory.value === catId ? '' : catId
  }

  function clearSelection() {
    selectedCategory.value = ''
    searchQuery.value = ''
    hoveredNodeId.value = null
    selectedNode.value = null
  }

  function selectNode(nodeId) {
    if (!nodeId) {
      selectedNode.value = null
      return
    }
    selectedNode.value = nodeMap.value[nodeId] || null
  }

  function hoverNode(nodeId) {
    hoveredNodeId.value = nodeId
  }

  function clearHover() {
    hoveredNodeId.value = null
  }

  function toggleLayout() {
    layoutType.value = layoutType.value === 'force' ? 'circular' : 'force'
  }

  return {
    categories: CATEGORIES,
    knowledgeNodes: KNOWLEDGE_NODES,
    connections: CONNECTIONS,
    searchQuery,
    selectedCategory,
    selectedNode,
    hoveredNodeId,
    layoutType,
    nodeMap,
    categoryMap,
    filteredNodeIds,
    connectedNodeIds,
    stats,
    chartOption,
    selectCategory,
    clearSelection,
    selectNode,
    hoverNode,
    clearHover,
    toggleLayout
  }
}
