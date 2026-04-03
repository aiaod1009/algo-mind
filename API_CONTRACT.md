# AlgoMind API 接口契约

## 接口 26：获取当前学习计划（更新版）

```yaml
路径: GET /api/learning-plans/current
功能: 获取当前赛道的学习计划，包含用户学习进度、每日安排、攻强补弱分析、坚持天数等完整数据

请求参数:
  参数名: 类型 | 是否必填 | 说明
  ---
  track: string | 可选 | 目标赛道 (algo/ds/contest)，默认algo

响应数据:
  {
    "code": 200,
    "message": "success",
    "data": {
      // ========== 基础计划信息 ==========
      "id": 1,
      "track": "algo",
      "trackLabel": "算法思维赛道",
      "weeklyGoal": 10,
      "weekGoals": [...],
      "dailyTasks": [...],
      "recommendations": [...],
      
      // ========== 本周计划完成情况 ==========
      "weeklyProgress": {
        "weeklyGoal": 10,              // 周目标题数
        "completed": 7,                // 已完成题数
        "remaining": 3,                // 剩余题数
        "completionRate": 0.7,         // 完成率 (0-1)
        "status": "进行中",             // 状态：已完成/进行中/需加油
        "completedByType": {           // 按题型统计
          "single": 3,
          "multi": 2,
          "code": 2,
          "fill": 0
        },
        "last7Days": [                 // 最近7天完成情况
          {
            "day": "周一",
            "completed": 1,
            "target": 2,
            "isDone": false
          }
        ]
      },
      
      // ========== 每日安排（实际 vs 计划） ==========
      "dailySchedule": [
        {
          "day": "今天",
          "date": "2025-01-15",
          "isToday": true,
          "totalTasks": 3,
          "completedTasks": 1,
          "progress": 33,                // 完成百分比
          "plannedTasks": [
            {
              "title": "薄弱专项突破练习",
              "duration": 40,
              "type": "practice",
              "priority": "high",
              "isCompleted": true          // 实际完成状态
            },
            {
              "title": "错题回顾与总结",
              "duration": 30,
              "type": "review",
              "priority": "high",
              "isCompleted": false
            }
          ]
        },
        {
          "day": "明天",
          "date": "2025-01-16",
          "isToday": false,
          "plannedTasks": [
            {
              "title": "新知识点学习",
              "duration": 40,
              "type": "learn",
              "priority": "medium"
            }
          ]
        }
      ],
      
      // ========== 攻强补弱分析 ==========
      "strengthWeakness": {
        "strongAreas": [               // 擅长领域（正确率>80%）
          {
            "name": "数组",
            "accuracy": 95,            // 正确率
            "solvedCount": 45,         // 解题数
            "level": "优秀"             // 等级：优秀/良好/需加强
          },
          {
            "name": "链表",
            "accuracy": 88,
            "solvedCount": 32,
            "level": "优秀"
          }
        ],
        "weakAreas": [                 // 薄弱环节（正确率<60%）
          {
            "name": "图论",
            "accuracy": 45,
            "solvedCount": 15,
            "level": "需加强"
          },
          {
            "name": "树形DP",
            "accuracy": 52,
            "solvedCount": 12,
            "level": "需加强"
          }
        ],
        "priorityOrder": ["图论", "树形DP"],  // 建议突破顺序
        "suggestions": [               // 攻强补弱建议
          "优先攻克：图论（正确率仅45%）",
          "保持优势：继续巩固数组的解题能力",
          "均衡发展：适当分配时间给薄弱环节"
        ]
      },
      
      // ========== 坚持天数统计 ==========
      "persistenceStats": {
        "currentStreak": 7,            // 当前连续打卡天数
        "longestStreak": 23,           // 最长连续打卡天数
        "streakStatus": "连续打卡达人", // 打卡状态描述
        "monthlyActiveDays": 22,       // 本月活跃天数
        "monthlyAttendanceRate": 73,   // 本月出勤率(%)
        "persistenceIndex": 75,        // 坚持指数 (0-100)
        "persistenceLevel": "良好",     // 坚持等级：优秀/良好/一般/需努力
        "milestones": [                // 里程碑
          {
            "name": "连续3天",
            "achieved": true,
            "status": "已获得"
          },
          {
            "name": "连续7天",
            "achieved": true,
            "status": "已获得"
          },
          {
            "name": "连续30天",
            "achieved": false,
            "status": "待完成"
          }
        ]
      },
      
      // ========== 学习数据概览 ==========
      "learningOverview": {
        "totalSolved": 156,            // 累计解题数
        "totalProblems": 200,          // 总题数
        "overallAccuracy": 78,         // 总体正确率(%)
        "totalPoints": 2500,           // 总积分
        "trackStats": {                // 各赛道统计
          "algo": {
            "solved": 65,
            "total": 80,
            "accuracy": 81
          },
          "ds": {
            "solved": 52,
            "total": 70,
            "accuracy": 74
          },
          "contest": {
            "solved": 39,
            "total": 50,
            "accuracy": 78
          }
        },
        "recentActivities": [          // 最近活动
          {
            "description": "完成了题目「二分查找」",
            "type": "solve",
            "points": 18,
            "time": "10分钟前"
          },
          {
            "description": "错题回顾「动态规划」",
            "type": "review",
            "points": 0,
            "time": "1小时前"
          },
          {
            "description": "连续打卡7天",
            "type": "achievement",
            "points": 50,
            "time": "昨天"
          }
        ]
      },
      
      "generatedAt": "2025-01-01T10:00:00Z",
      "createdAt": "2025-01-01T10:00:00Z"
    }
  }

前端调用代码位置: src/api/user.js:getCurrentLearningPlan()
后端接收代码位置: LearningPlanController.java:getCurrentPlan()
```

---

## 接口 27：生成个性化学习计划（更新版）

```yaml
路径: POST /api/learning-plans/generate
功能: 基于用户画像数据生成个性化学习计划

请求参数:
  参数名: 类型 | 是否必填 | 说明
  ---
  # 基础信息
  track: string | 可选 | 目标赛道 (algo/ds/contest)，默认algo
  trackLabel: string | 可选 | 赛道标签，默认"算法思维赛道"
  weeklyGoal: integer | 可选 | 每周目标题数，默认10
  
  # 用户画像数据
  weeklyCompleted: integer | 可选 | 本周已完成题数，默认0
  totalSolved: integer | 可选 | 累计解题数，默认0
  currentStreak: integer | 可选 | 当前连续打卡天数，默认0
  persistenceIndex: integer | 可选 | 坚持指数 (0-100)，默认50
  
  # 知识点标签
  errorTopics: string[] | 可选 | 历史错题知识点标签列表，如["动态规划","二叉树"]
  strongAreas: string[] | 可选 | 擅长领域列表，如["数组","链表"]
  weakAreas: string[] | 可选 | 薄弱环节列表，如["图论","贪心算法"]

请求示例:
  {
    "track": "algo",
    "trackLabel": "算法思维赛道",
    "weeklyGoal": 10,
    "weeklyCompleted": 5,
    "totalSolved": 156,
    "currentStreak": 7,
    "persistenceIndex": 75,
    "errorTopics": ["动态规划", "二分查找"],
    "strongAreas": ["数组", "链表", "栈队列"],
    "weakAreas": ["图论", "树形DP"]
  }

响应数据:
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "track": "algo",
      "trackLabel": "算法思维赛道",
      "weeklyGoal": 10,
      "isNewGenerated": true,
      "weekGoals": [
        {
          "id": 1,
          "title": "薄弱专项突破：图论",
          "progress": 0,
          "target": 4
        },
        {
          "id": 2,
          "title": "错题回顾与总结",
          "progress": 0,
          "target": 3
        },
        {
          "id": 3,
          "title": "算法思维强化",
          "progress": 0,
          "target": 3
        },
        {
          "id": 4,
          "title": "每日坚持打卡",
          "progress": 5,
          "target": 10
        }
      ],
      "dailyTasks": [
        {
          "day": "今天",
          "tasks": [
            {
              "title": "图论 专项练习",
              "duration": 40,
              "type": "practice",
              "priority": "high"
            },
            {
              "title": "错题回顾：动态规划",
              "duration": 30,
              "type": "review",
              "priority": "high"
            },
            {
              "title": "动态规划思想理解",
              "duration": 30,
              "type": "learn",
              "priority": "medium"
            }
          ]
        },
        {
          "day": "明天",
          "tasks": [
            {
              "title": "algo 进阶知识点",
              "duration": 40,
              "type": "learn",
              "priority": "medium"
            },
            {
              "title": "综合练习题",
              "duration": 35,
              "type": "practice",
              "priority": "medium"
            }
          ]
        },
        {
          "day": "后天",
          "tasks": [
            {
              "title": "连续打卡奖励：挑战题",
              "duration": 45,
              "type": "challenge",
              "priority": "high"
            },
            {
              "title": "本周知识点总结",
              "duration": 30,
              "type": "review",
              "priority": "medium"
            },
            {
              "title": "综合模拟测试",
              "duration": 40,
              "type": "practice",
              "priority": "medium"
            }
          ]
        }
      ],
      "recommendations": [
        {
          "type": "video",
          "title": "图论 专项讲解",
          "source": "针对薄弱点",
          "priority": "high"
        },
        {
          "type": "practice",
          "title": "图论 专项练习20题",
          "source": "强化训练",
          "priority": "high"
        },
        {
          "type": "article",
          "title": "动态规划 常见错误分析",
          "source": "避免再犯",
          "priority": "medium"
        },
        {
          "type": "video",
          "title": "动态规划入门讲解",
          "source": "基础巩固",
          "priority": "medium"
        },
        {
          "type": "practice",
          "title": "DP经典50题",
          "source": "必刷题集",
          "priority": "medium"
        }
      ],
      "profileAnalysis": {
        "learningStatus": "学习状态良好，可适当提升强度",
        "focusAreas": [
          "薄弱环节：图论, 树形DP",
          "错题知识点：动态规划, 二分查找"
        ],
        "suggestions": [
          "针对薄弱环节进行专项突破"
        ]
      },
      "generatedAt": "2025-01-01T10:00:00Z",
      "createdAt": "2025-01-01T10:00:00Z"
    }
  }

个性化逻辑说明:
  1. 坚持指数调整:
     - persistenceIndex >= 80: 目标增加20%（挑战模式）
     - persistenceIndex >= 50: 保持原目标（标准模式）
     - persistenceIndex < 50: 目标降低20%（建立信心模式）
  
  2. 周目标生成:
     - 优先安排薄弱环节专项突破（占35%）
     - 错题回顾（如果有错题，占25%）
     - 赛道核心目标（占25%）
     - 每日坚持打卡（根据完成率调整）
  
  3. 每日任务安排:
     - 今天: 优先攻克薄弱环节 + 错题回顾
     - 明天: 根据完成率调整难度（完成率低则降低难度）
     - 后天: 综合练习 + 连续打卡奖励（streak>=7天）
  
  4. 推荐资源:
     - 针对薄弱环节的专项资源
     - 错题知识点分析文章
     - 坚持指数低时推荐习惯养成文章
     - 赛道基础资源

前端调用代码位置: src/api/user.js:generateLearningPlan()
后端接收代码位置: LearningPlanController.java:generateLearningPlan()
```

## 前端调用示例

### 获取当前学习计划
```javascript
import api from './api'

// 获取当前学习计划（包含完整进度数据）
const response = await api.getCurrentLearningPlan('algo')

// 使用示例
const plan = response.data.data

// 本周进度
console.log(plan.weeklyProgress.completionRate)  // 0.7
console.log(plan.weeklyProgress.status)          // "进行中"

// 攻强补弱
console.log(plan.strengthWeakness.weakAreas)     // [{name: "图论", accuracy: 45}]
console.log(plan.strengthWeakness.suggestions)   // ["优先攻克：图论..."]

// 坚持统计
console.log(plan.persistenceStats.currentStreak) // 7
console.log(plan.persistenceStats.persistenceIndex) // 75
```

### 生成个性化学习计划
```javascript
import api from './api'

// 收集用户画像数据
const userProfileData = {
  // 基础信息
  track: 'algo',
  trackLabel: '算法思维赛道',
  weeklyGoal: 10,
  
  // 用户画像数据（从stores中获取）
  weeklyCompleted: userStore.userInfo?.weeklyCompleted || 0,
  totalSolved: stats.value?.solvedProblems || 0,
  currentStreak: stats.value?.currentStreak || 0,
  persistenceIndex: calculatePersistenceIndex(), // 计算坚持指数
  
  // 错题知识点（从错题本中提取）
  errorTopics: errorStore.errors?.map(e => extractTopic(e.question)) || [],
  
  // 强弱领域（根据正确率统计）
  strongAreas: ['数组', '链表', '栈队列'],
  weakAreas: ['图论', '树形DP']
}

// 调用生成接口
const response = await api.generateLearningPlan(userProfileData)
```

## 坚持指数计算建议

```javascript
function calculatePersistenceIndex() {
  const streak = userStore.userInfo?.currentStreak || 0
  const weeklyCompletion = weeklyCompleted / weeklyGoal
  const totalDays = Math.min(totalSolved / 3, 30) // 按平均每天3题估算
  
  // 综合计算：连续打卡(40%) + 周完成率(40%) + 总坚持天数(20%)
  const streakScore = Math.min(streak / 14, 1) * 40 // 14天满分为40分
  const completionScore = weeklyCompletion * 40 // 完成率40分
  const persistenceScore = Math.min(totalDays / 30, 1) * 20 // 30天满分为20分
  
  return Math.round(streakScore + completionScore + persistenceScore)
}
```
