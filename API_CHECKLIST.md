# API 接口检查清单

## 已定义的 API 方法

### 1. 基础 API (api/index.js)
- 使用 axios 创建的基础实例
- 已配置请求拦截器（自动注入 token）
- 已配置响应拦截器（错误处理）

### 2. 用户相关 API (api/user.js)

| 方法名 | 用途 | HTTP | 路径 | 状态 |
|--------|------|------|------|------|
| getUserProblemHeatmap | 获取做题热力图 | GET | /user/heatmap?year={year} | ✅ 已定义 |
| getUserProblemStats | 获取做题统计 | GET | /user/stats | ✅ 已定义 |
| getUserActivities | 获取活动列表 | GET | /user/activities | ✅ 已定义 |
| updateUserProfile | 更新用户资料 | PUT | /user/profile | ✅ 已定义 |
| getUserStatus | 获取用户状态 | GET | /user/status | ✅ 已定义 |
| updateUserStatus | 更新用户状态 | PUT | /user/status | ✅ 已定义 |
| getUserErrors | 获取错题本 | GET | /user/errors | ✅ 已定义 |
| addUserError | 添加错题 | POST | /user/errors | ✅ 已定义 |
| deleteUserError | 删除错题 | DELETE | /user/errors/{id} | ✅ 已定义 |
| getUserRanking | 获取排行榜位置 | GET | /user/ranking | ✅ 已定义 |
| getUserPointsHistory | 获取积分记录 | GET | /user/points-history | ✅ 已定义 |
| getUserCreatedProblems | 获取创建的题目 | GET | /user/created-problems | ✅ 已定义 |
| getUserSolvedProblems | 获取完成的题目 | GET | /user/solved-problems | ✅ 已定义 |

### 3. Store 中直接调用的 API

#### userStore (stores/user.js)
| 调用 | HTTP | 路径 | 状态 |
|------|------|------|------|
| api.post('/login') | POST | /login | ⚠️ 需要后端实现 |

#### levelStore (stores/level.js)
| 调用 | HTTP | 路径 | 状态 |
|------|------|------|------|
| api.get('/levels') | GET | /levels | ⚠️ 需要后端实现 |
| api.post('/submit') | POST | /submit | ⚠️ 需要后端实现 |

#### forumStore (stores/forum.js)
| 调用 | HTTP | 路径 | 状态 |
|------|------|------|------|
| api.get('/forum-posts') | GET | /forum-posts | ⚠️ 需要后端实现 |
| api.post('/forum-posts') | POST | /forum-posts | ⚠️ 需要后端实现 |

#### errorStore (stores/error.js)
| 调用 | HTTP | 路径 | 状态 |
|------|------|------|------|
| api.get('/errors') | GET | /errors | ⚠️ 需要后端实现 |
| api.post('/errors') | POST | /errors | ⚠️ 需要后端实现 |
| api.post('/error-analysis') | POST | /error-analysis | ⚠️ 需要后端实现 |

## 需要后端实现的接口

### 认证相关
- [ ] POST /login - 用户登录
- [ ] POST /register - 用户注册
- [ ] POST /logout - 用户登出

### 关卡相关
- [ ] GET /levels - 获取关卡列表
- [ ] GET /levels/:id - 获取关卡详情
- [ ] POST /submit - 提交代码

### 论坛相关
- [ ] GET /forum-posts - 获取帖子列表
- [ ] POST /forum-posts - 发布帖子
- [ ] DELETE /forum-posts/:id - 删除帖子
- [ ] POST /forum-posts/:id/comments - 添加评论
- [ ] POST /forum-posts/:id/like - 点赞帖子

### 用户相关（已在 api/user.js 定义，需要后端对应实现）
- [ ] GET /user/heatmap?year={year} - 做题热力图
- [ ] GET /user/stats - 做题统计
- [ ] GET /user/activities - 活动列表
- [ ] PUT /user/profile - 更新资料
- [ ] GET /user/status - 获取状态
- [ ] PUT /user/status - 更新状态
- [ ] GET /user/errors - 错题本
- [ ] POST /user/errors - 添加错题
- [ ] DELETE /user/errors/:id - 删除错题
- [ ] GET /user/ranking - 排行榜位置
- [ ] GET /user/points-history - 积分记录
- [ ] GET /user/created-problems - 创建的题目
- [ ] GET /user/solved-problems - 完成的题目

### 错题相关
- [ ] GET /errors - 获取错题列表
- [ ] POST /errors - 添加错题
- [ ] POST /error-analysis - 错题分析

### 排行榜相关
- [ ] GET /ranking - 获取排行榜

### 课程相关（如果有）
- [ ] GET /courses - 获取课程列表
- [ ] GET /courses/:id - 获取课程详情

## 建议的后端数据结构

### 做题热力图数据
```json
{
  "success": true,
  "data": [
    {
      "date": "2026-01-01",
      "count": 5
    }
  ]
}
```

### 做题统计数据
```json
{
  "success": true,
  "data": {
    "totalSolved": 128,
    "totalCreated": 12,
    "streakDays": 15,
    "todaySolved": 3,
    "weeklySolved": 21,
    "monthlySolved": 89
  }
}
```

### 活动列表数据
```json
{
  "success": true,
  "data": [
    {
      "type": "solve",
      "title": "完成了 两数之和",
      "createdAt": "2026-04-01T10:30:00Z"
    }
  ]
}
```

### 用户状态数据
```json
{
  "success": true,
  "data": {
    "emoji": "😊",
    "mood": "happy",
    "isBusy": false,
    "busyAutoReply": "我现在有点忙，稍后回复你~",
    "busyEndTime": null
  }
}
```
