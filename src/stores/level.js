import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api'

const TRACKS = [
  { code: 'algo', name: '算法思维赛道', goal: '掌握常见算法范式，提升解题速度' },
  { code: 'ds', name: '数据结构赛道', goal: '强化结构设计与复杂度控制能力' },
  { code: 'contest', name: '竞赛冲刺赛道', goal: '在限时条件下稳定完成中高难度题目' },
]

const MOCK_LEVELS = [
  {
    id: 1,
    track: 'algo',
    order: 1,
    name: 'A1 时间复杂度基础',
    isUnlocked: true,
    rewardPoints: 18,
    type: 'single',
    question: '以下哪一个是二分查找在有序数组中的时间复杂度？',
    options: ['O(n)', 'O(log n)', 'O(n log n)', 'O(1)'],
    answer: 'O(log n)',
    description: '理解查找算法复杂度',
  },
  {
    id: 2,
    track: 'algo',
    order: 2,
    name: 'A2 双指针技巧',
    isUnlocked: false,
    rewardPoints: 24,
    type: 'single',
    question: '在有序数组中寻找两数之和，最常见的高效解法是？',
    options: ['暴力双循环', '哈希 + 排序', '双指针夹逼', '递归回溯'],
    answer: '双指针夹逼',
    description: '掌握指针移动策略',
  },
  {
    id: 3,
    track: 'algo',
    order: 3,
    name: 'A3 动态规划入门',
    isUnlocked: false,
    rewardPoints: 30,
    type: 'fill',
    question: '斐波那契数列动态规划的状态转移方程：f(n)= ________ 。',
    answer: 'f(n-1)+f(n-2)',
    description: '建立状态与转移关系',
  },
  {
    id: 4,
    track: 'algo',
    order: 4,
    name: 'A4 滑动窗口核心',
    isUnlocked: false,
    rewardPoints: 32,
    type: 'multi',
    question: '关于滑动窗口算法，哪些说法正确？',
    options: ['常用于子数组/子串最值问题', '窗口左右边界都可能移动', '一定要排序数组', '可配合哈希表维护频次'],
    answer: ['常用于子数组/子串最值问题', '窗口左右边界都可能移动', '可配合哈希表维护频次'],
    description: '理解窗口收缩与扩张',
  },
  {
    id: 5,
    track: 'algo',
    order: 5,
    name: 'A5 二分模板编程',
    isUnlocked: false,
    rewardPoints: 38,
    type: 'code',
    question: '实现二分查找模板，包含 left、right、mid 和命中返回下标逻辑。',
    answer: ['left', 'right', 'mid', 'while', 'return'],
    description: '把思路落到可复用代码',
  },
  {
    id: 6,
    track: 'algo',
    order: 6,
    name: 'A6 递归终止条件',
    isUnlocked: false,
    rewardPoints: 42,
    type: 'judge',
    question: '递归函数若没有终止条件，通常会导致栈溢出。',
    options: ['正确', '错误'],
    answer: '正确',
    description: '强化递归边界意识',
  },
  {
    id: 7,
    track: 'ds',
    order: 1,
    name: 'D1 栈与括号匹配',
    isUnlocked: true,
    rewardPoints: 16,
    type: 'judge',
    question: '使用栈可以在线性时间内判断括号字符串是否合法。',
    options: ['正确', '错误'],
    answer: '正确',
    description: '理解栈的后进先出特性',
  },
  {
    id: 8,
    track: 'ds',
    order: 2,
    name: 'D2 哈希冲突策略',
    isUnlocked: false,
    rewardPoints: 22,
    type: 'multi',
    question: '下列哪些方法可用于处理哈希冲突？',
    options: ['链地址法', '开放寻址法', '红黑树旋转', '再哈希法'],
    answer: ['链地址法', '开放寻址法', '再哈希法'],
    description: '掌握常见冲突处理方案',
  },
  {
    id: 9,
    track: 'ds',
    order: 3,
    name: 'D3 并查集代码骨架',
    isUnlocked: false,
    rewardPoints: 30,
    type: 'code',
    question: '写出并查集路径压缩核心逻辑（可用伪代码），至少包含 find、parent、递归压缩。',
    answer: ['find', 'parent', 'return', 'parent[x]'],
    description: '提升结构型题目实现能力',
  },
  {
    id: 10,
    track: 'ds',
    order: 4,
    name: 'D4 队列操作模拟',
    isUnlocked: false,
    rewardPoints: 34,
    type: 'fill',
    question: '循环队列中，判空条件通常是 front == ________。',
    answer: 'rear',
    description: '巩固队列边界状态',
  },
  {
    id: 11,
    track: 'ds',
    order: 5,
    name: 'D5 链表反转编程',
    isUnlocked: false,
    rewardPoints: 40,
    type: 'code',
    question: '编写单链表反转核心逻辑，至少包含 prev、curr、next 三个指针。',
    answer: ['prev', 'curr', 'next', 'while', 'return'],
    description: '训练指针操作稳定性',
  },
  {
    id: 12,
    track: 'ds',
    order: 6,
    name: 'D6 堆结构判断',
    isUnlocked: false,
    rewardPoints: 44,
    type: 'single',
    question: '数组表示的完全二叉树中，父节点 i 的左孩子下标通常是？',
    options: ['2*i', '2*i+1', 'i+1', 'i/2'],
    answer: '2*i+1',
    description: '掌握堆的下标映射',
  },
  {
    id: 13,
    track: 'contest',
    order: 1,
    name: 'C1 贪心选择判断',
    isUnlocked: true,
    rewardPoints: 20,
    type: 'single',
    question: '活动选择问题中，经典贪心策略是优先选择哪类活动？',
    options: ['开始最早', '结束最早', '持续最短', '收益最大'],
    answer: '结束最早',
    description: '练习竞赛常见贪心模型',
  },
  {
    id: 14,
    track: 'contest',
    order: 2,
    name: 'C2 区间调度多选',
    isUnlocked: false,
    rewardPoints: 28,
    type: 'multi',
    question: '关于区间调度，哪些说法是正确的？',
    options: ['按结束时间排序常见且有效', '一定需要动态规划', '可结合双指针处理部分变体', '排序后常可线性扫描求解'],
    answer: ['按结束时间排序常见且有效', '可结合双指针处理部分变体', '排序后常可线性扫描求解'],
    description: '强化多策略组合思维',
  },
  {
    id: 15,
    track: 'contest',
    order: 3,
    name: 'C3 限时模拟复盘',
    isUnlocked: false,
    rewardPoints: 36,
    type: 'fill',
    question: '你在限时赛中的复盘模板第一句应包含：题意重述 + ________。',
    answer: '关键约束',
    description: '建立赛后复盘习惯',
  },
  {
    id: 16,
    track: 'contest',
    order: 4,
    name: 'C4 差分数组应用',
    isUnlocked: false,
    rewardPoints: 42,
    type: 'single',
    question: '处理大量区间加法并最终还原数组，优先使用哪种技巧？',
    options: ['前缀和', '差分数组', '单调栈', '并查集'],
    answer: '差分数组',
    description: '提升数据处理效率',
  },
  {
    id: 17,
    track: 'contest',
    order: 5,
    name: 'C5 快速输入输出编程',
    isUnlocked: false,
    rewardPoints: 48,
    type: 'code',
    question: '写出竞赛常用快速读入模板，至少包含读取循环和输出结果逻辑。',
    answer: ['for', 'input', 'print'],
    description: '减少IO成为瓶颈',
  },
  {
    id: 18,
    track: 'contest',
    order: 6,
    name: 'C6 复杂度压测判断',
    isUnlocked: false,
    rewardPoints: 52,
    type: 'judge',
    question: '当 n=1e5 时，O(n^2) 的算法在竞赛限时内通常不可接受。',
    options: ['正确', '错误'],
    answer: '正确',
    description: '建立复杂度直觉',
  },
]

const LOCAL_LEVEL_KEY = 'levels'
const STAR_RULES = {
  twoStarMaxAttempts: 2,
  threeStarMaxAttempts: 1,
  defaultTargetTimeMs: 90000,
}

const readLocalLevels = () => {
  const raw = localStorage.getItem(LOCAL_LEVEL_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

const saveLocalLevels = (levels) => {
  localStorage.setItem(LOCAL_LEVEL_KEY, JSON.stringify(levels))
}

const mergeLocalWithMockLevels = (localLevels) => {
  if (!Array.isArray(localLevels) || !localLevels.length) {
    return [...MOCK_LEVELS]
  }
  const localMap = new Map(localLevels.map((item) => [Number(item.id), item]))
  const merged = MOCK_LEVELS.map((mockItem) => {
    const localItem = localMap.get(Number(mockItem.id))
    if (!localItem) return { ...mockItem }
    return {
      ...mockItem,
      ...localItem,
      id: mockItem.id,
      track: mockItem.track,
      order: mockItem.order,
      name: mockItem.name,
      question: mockItem.question,
      description: mockItem.description,
      type: mockItem.type,
      options: mockItem.options,
      answer: mockItem.answer,
      rewardPoints: mockItem.rewardPoints,
    }
  })

  const extras = localLevels.filter((item) => !MOCK_LEVELS.some((base) => Number(base.id) === Number(item.id)))
  return [...merged, ...extras]
}

const normalizeNumber = (value, fallback = 0) => {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

const clampStars = (value) => {
  return Math.max(0, Math.min(3, Math.floor(normalizeNumber(value, 0))))
}

const normalizeText = (value) => String(value || '').trim().toLowerCase()

const compareMultiAnswer = (userAnswer, rightAnswer) => {
  const userList = Array.isArray(userAnswer) ? userAnswer.map((item) => normalizeText(item)).sort() : []
  const rightList = Array.isArray(rightAnswer) ? rightAnswer.map((item) => normalizeText(item)).sort() : []
  return JSON.stringify(userList) === JSON.stringify(rightList)
}

const compareCodeAnswer = (userAnswer, rightAnswer) => {
  const text = normalizeText(userAnswer)
  if (!Array.isArray(rightAnswer) || !rightAnswer.length) return false
  return rightAnswer.every((keyword) => text.includes(normalizeText(keyword)))
}

const calculateStars = ({ attempts, timeMs, targetTimeMs }) => {
  const safeAttempts = Math.max(1, Math.floor(normalizeNumber(attempts, 1)))
  const safeTime = Math.max(0, normalizeNumber(timeMs, 0))
  const safeTargetTime = Math.max(1000, normalizeNumber(targetTimeMs, STAR_RULES.defaultTargetTimeMs))

  if (safeAttempts <= STAR_RULES.threeStarMaxAttempts && safeTime <= safeTargetTime) {
    return 3
  }
  if (safeAttempts <= STAR_RULES.twoStarMaxAttempts) {
    return 2
  }
  return 1
}

export const useLevelStore = defineStore('level', () => {
  const levels = ref([])
  const tracks = ref(TRACKS)

  const normalizeLevels = (rawLevels) => {
    return rawLevels.map((item) => ({
      ...item,
      track: item.track || 'algo',
      order: Number(item.order || 1),
      type: item.type || 'single',
      isUnlocked: Boolean(item.isUnlocked),
      stars: clampStars(item.stars ?? item.bestStars ?? 0),
      bestStars: clampStars(item.bestStars ?? item.stars ?? 0),
      isCompleted: Boolean(item.isCompleted || clampStars(item.bestStars ?? item.stars ?? 0) > 0),
      attemptCount: Math.max(0, Math.floor(normalizeNumber(item.attemptCount, 0))),
      bestTimeMs: item.bestTimeMs == null ? null : Math.max(0, Math.floor(normalizeNumber(item.bestTimeMs, 0))),
      targetTimeMs: Math.max(1000, Math.floor(normalizeNumber(item.targetTimeMs, STAR_RULES.defaultTargetTimeMs))),
    }))
  }

  const fetchLevels = async () => {
    try {
      const res = await api.get('/levels')
      if (res.data?.code === 0 && Array.isArray(res.data.data)) {
        levels.value = normalizeLevels(mergeLocalWithMockLevels(res.data.data))
        saveLocalLevels(levels.value)
        return
      }
    } catch (error) {
      console.warn('关卡接口不可用，使用本地关卡数据。', error)
    }

    const local = readLocalLevels()
    const mergedLocal = mergeLocalWithMockLevels(local)
    levels.value = normalizeLevels(mergedLocal)
    if (!local || mergedLocal.length !== local.length) {
      saveLocalLevels(levels.value)
    }
  }

  const findLevelById = (id) => levels.value.find((item) => Number(item.id) === Number(id))

  const getLevelsByTrack = (trackCode) => {
    return levels.value
      .filter((item) => item.track === trackCode)
      .sort((a, b) => Number(a.order || 0) - Number(b.order || 0))
  }

  const unlockNextLevel = (currentId) => {
    const currentLevel = findLevelById(currentId)
    if (!currentLevel) return false
    const currentTrackLevels = getLevelsByTrack(currentLevel.track)
    const currentIndex = currentTrackLevels.findIndex((item) => Number(item.id) === Number(currentId))
    const next = currentTrackLevels[currentIndex + 1]
    if (!next) return false
    if (next.isUnlocked) return false
    const realNext = findLevelById(next.id)
    if (!realNext) return false
    realNext.isUnlocked = true
    return true
  }

  const submitAnswer = async (levelId, answer, meta = {}) => {
    try {
      const payload = { levelId, answer, meta }
      if (meta?.language) {
        payload.language = meta.language
      }
      if (meta?.stdinInput != null) {
        payload.stdinInput = meta.stdinInput
      }
      if (typeof answer === 'string') {
        payload.code = answer
      }

      const res = await api.post('/submit', payload)
      if (res.data?.code === 0) {
        return res.data.data
      }
    } catch (error) {
      console.warn('提交接口不可用，执行本地判题。', error)
    }

    const level = findLevelById(levelId)
    if (!level) return null

    level.attemptCount = Math.max(0, Math.floor(normalizeNumber(level.attemptCount, 0))) + 1

    let correct = false
    if (level.type === 'multi') {
      correct = compareMultiAnswer(answer, level.answer)
    } else if (level.type === 'code') {
      correct = compareCodeAnswer(answer, level.answer)
    } else {
      const normalizedUserAnswer = normalizeText(answer)
      const normalizedRightAnswer = normalizeText(level.answer)
      correct = normalizedUserAnswer === normalizedRightAnswer
    }

    if (correct) {
      const attemptsInRun = Math.max(1, Math.floor(normalizeNumber(meta.attempts, 1)))
      const timeMs = Math.max(0, Math.floor(normalizeNumber(meta.timeMs, 0)))
      const starsEarned = calculateStars({
        attempts: attemptsInRun,
        timeMs,
        targetTimeMs: level.targetTimeMs,
      })
      const currentBestStars = clampStars(level.bestStars)

      level.stars = starsEarned
      level.bestStars = Math.max(currentBestStars, starsEarned)
      level.isCompleted = true
      if (timeMs > 0) {
        level.bestTimeMs = level.bestTimeMs == null ? timeMs : Math.min(level.bestTimeMs, timeMs)
      }

      const nextLevelUnlocked = unlockNextLevel(levelId)
      saveLocalLevels(levels.value)
      return {
        correct: true,
        pointsEarned: level.rewardPoints,
        nextLevelUnlocked,
        starsEarned,
        bestStars: level.bestStars,
        timeMs,
        score: 100,
        output: level.type === 'code' ? '本地评测模式未执行真实代码，以下为模拟输出。' : '',
        analysis:
          level.type === 'code'
            ? '关键逻辑匹配通过。当前为离线本地判题，建议连接后端以获取真实运行日志、边界用例和 AI 详细建议。'
            : '',
      }
    }

    saveLocalLevels(levels.value)

    return {
      correct: false,
      pointsEarned: 0,
      nextLevelUnlocked: false,
      starsEarned: 0,
      bestStars: clampStars(level.bestStars),
      score: level.type === 'code' ? 60 : 0,
      output: level.type === 'code' ? '本地评测模式未执行真实代码，无法返回程序标准输出。' : '',
      analysis:
        level.type === 'code'
          ? '未通过关键字匹配校验。请检查主流程、边界处理和返回值，再次提交可获得新的 AI 评估结果。'
          : '',
    }
  }

  return { levels, tracks, fetchLevels, submitAnswer, findLevelById, getLevelsByTrack }
})
