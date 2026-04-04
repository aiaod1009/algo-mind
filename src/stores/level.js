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
    id: 5,
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
    id: 6,
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
    id: 7,
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
    id: 8,
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
    id: 9,
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
]

const LOCAL_LEVEL_KEY = 'levels'

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
    }))
  }

  const fetchLevels = async () => {
    try {
      const res = await api.get('/levels')
      if (res.data?.code === 0 && Array.isArray(res.data.data)) {
        levels.value = normalizeLevels(res.data.data)
        saveLocalLevels(levels.value)
        return
      }
      throw new Error(res.data?.message || '获取关卡数据失败')
    } catch (error) {
<<<<<<< Updated upstream
      console.warn('关卡接口不可用，使用本地关卡数据。', error)
    }

    const local = readLocalLevels()
    levels.value = normalizeLevels(local || MOCK_LEVELS)
    if (!local) {
      saveLocalLevels(levels.value)
=======
      throw error
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
  const submitAnswer = async (levelId, answer) => {
    try {
      const res = await api.post('/submit', { levelId, answer })
=======
  const submitAnswer = async (levelId, answer, meta = {}) => {
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

    try {
      const res = await api.post('/submit', payload)
>>>>>>> Stashed changes
      if (res.data?.code === 0) {
        return res.data.data
      }
      throw new Error(res.data?.message || '提交失败')
    } catch (error) {
<<<<<<< Updated upstream
      console.warn('提交接口不可用，执行本地判题。', error)
    }

    const level = findLevelById(levelId)
    if (!level) return null

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
      const nextLevelUnlocked = unlockNextLevel(levelId)
      saveLocalLevels(levels.value)
      return {
        correct: true,
        pointsEarned: level.rewardPoints,
        nextLevelUnlocked,
      }
    }

    return {
      correct: false,
      pointsEarned: 0,
      nextLevelUnlocked: false,
=======
      throw error
>>>>>>> Stashed changes
    }
  }

  return { levels, tracks, fetchLevels, submitAnswer, findLevelById, getLevelsByTrack }
})
