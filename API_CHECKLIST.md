# API 接口检查清单

> **最后更新时间：2026-04-05**
> **更新内容：全面更新接口列表，标记所有已实现接口**

---

## 已定义的 API 方法

### 1. 基础 API (api/index.js)
- 使用 axios 创建的基础实例
- 已配置请求拦截器（自动注入 token）
- 已配置响应拦截器（错误处理）

---

## 已实现的 API 接口

### 认证相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 登录 | POST | /login | ✅ 已实现 | LoginController |

### 用户相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 更新用户资料 | PUT | /users/me | ✅ 已实现 | UserController |
| 获取做题热力图 | GET | /users/me/heatmap | ✅ 已实现 | UserController |
| 获取做题统计 | GET | /users/me/stats | ✅ 已实现 | UserController |
| 获取活动列表 | GET | /users/me/activities | ✅ 已实现 | UserController |
| 获取用户状态 | GET | /users/me/status | ✅ 已实现 | UserController |
| 更新用户状态 | PUT | /users/me/status | ✅ 已实现 | UserController |
| 获取排行榜位置 | GET | /users/me/ranking | ✅ 已实现 | UserController |
| 获取积分记录 | GET | /users/me/points-history | ✅ 已实现 | UserController |
| 获取创建的题目 | GET | /users/me/created-problems | ✅ 已实现 | UserController |
| 获取完成的题目 | GET | /users/me/solved-problems | ✅ 已实现 | UserController |

### 头像相关 🆕

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 上传头像 | POST | /users/me/avatar | ✅ 已实现 | AvatarController |
| 删除头像 | DELETE | /users/me/avatar | ✅ 已实现 | AvatarController |
| 获取头像 | GET | /users/me/avatar | ✅ 已实现 | AvatarController |
| 从URL上传头像 | POST | /users/me/avatar-from-url | ✅ 已实现 | AvatarController |

### 关卡相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 获取关卡列表 | GET | /levels | ✅ 已实现 | LevelController |
| 提交答案 | POST | /submit | ✅ 已实现 | LevelController |

### 论坛相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 获取帖子列表 | GET | /forum-posts | ✅ 已实现 | ForumPostController |
| 获取帖子详情 | GET | /forum-posts/{id} | ✅ 已实现 | ForumPostController |
| 发布帖子 | POST | /forum-posts | ✅ 已实现 | ForumPostController |
| 删除帖子 | DELETE | /forum-posts/{id} | ✅ 已实现 | ForumPostController |
| 点赞/取消点赞 | POST | /forum-posts/{id}/like | ✅ 已实现 | ForumPostController |
| 更新评论计数 | POST | /forum-posts/{id}/comment-count | ✅ 已实现 | ForumPostController |
| 获取帖子评论 | GET | /forum-posts/{id}/comments | ✅ 已实现 | ForumPostController |
| 获取热门评论 | GET | /forum-posts/{id}/hot-comments | ✅ 已实现 | ForumPostController |

### 错题相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 获取错题列表 | GET | /errors | ✅ 已实现 | ErrorController |
| 获取错题详情 | GET | /errors/{id} | ✅ 已实现 | ErrorController |
| 添加错题 | POST | /errors | ✅ 已实现 | ErrorController |
| 删除错题 | DELETE | /errors/{id} | ✅ 已实现 | ErrorController |
| 更新错题 | PUT | /errors/{id} | ✅ 已实现 | ErrorController |
| AI错题分析 | POST | /error-analysis | ✅ 已实现 | ErrorController |

### 课程相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 获取课程列表 | GET | /courses | ✅ 已实现 | CourseController |
| 获取课程评论 | GET | /courses/{courseId}/comments | ✅ 已实现 | CourseCommentController |
| 添加课程评论 | POST | /courses/{courseId}/comments | ✅ 已实现 | CourseCommentController |
| 点赞课程评论 | POST | /comments/{id}/like | ✅ 已实现 | CourseCommentController |

### 排行榜相关

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 获取排行榜 | GET | /ranking | ✅ 已实现 | RankingController |

### 学习计划相关 🆕

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 获取学习计划列表 | GET | /learning-plans | ✅ 已实现 | LearningPlanController |
| 获取当前学习计划 | GET | /learning-plans/current | ✅ 已实现 | LearningPlanController |
| 生成个性化学习计划 | POST | /learning-plans/generate | ✅ 已实现 | LearningPlanController |
| 保存学习计划 | POST | /learning-plans | ✅ 已实现 | LearningPlanController |
| 删除学习计划 | DELETE | /learning-plans/{id} | ✅ 已实现 | LearningPlanController |

### AI相关 🆕

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| AI对话 | POST | /ai/chat | ✅ 已实现 | AIController |
| AI代码评估 | POST | /ai/evaluate-code | ✅ 已实现 | AIController |

### 代码运行 🆕

| 方法名 | HTTP | 路径 | 状态 | Controller |
|--------|------|------|------|------------|
| 运行代码 | POST | /run-code | ✅ 已实现 | CodeController |

---

## 接口详细说明

### 🆕 头像相关接口

#### POST /users/me/avatar
上传用户头像（multipart/form-data）

**请求参数：**
```
file: MultipartFile (必填)
```

**响应数据：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "avatarUrl": "/uploads/avatars/1_xxx.png",
    "message": "头像上传成功"
  }
}
```

#### POST /users/me/avatar-from-url
从URL上传头像

**请求参数：**
```json
{
  "url": "https://example.com/avatar.png"
}
```

---

### 🆕 学习计划接口

#### GET /learning-plans/current
获取当前学习计划（包含用户学习进度和画像数据）

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| track | string | 否 | 赛道(algo/ds/contest)，默认algo |

**响应数据：** 详见 `API_CONTRACT.md`

#### POST /learning-plans/generate
生成个性化学习计划（AI驱动）

**请求参数：**
```json
{
  "track": "algo",
  "trackLabel": "算法思维赛道",
  "weeklyGoal": 10,
  "weeklyCompleted": 5,
  "totalSolved": 156,
  "currentStreak": 7,
  "persistenceIndex": 75,
  "errorTopics": ["动态规划", "二分查找"],
  "strongAreas": ["数组", "链表"],
  "weakAreas": ["图论", "树形DP"]
}
```

---

### 🆕 AI相关接口

#### POST /ai/chat
AI对话接口

**请求参数：**
```json
{
  "message": "帮我分析一下我的错题",
  "messages": [],
  "context": {
    "track": "algo",
    "trackLabel": "算法思维赛道",
    "weeklyGoal": 10,
    "weakTopics": ["动态规划"],
    "strongTopics": ["数组"],
    "totalErrors": 5,
    "consistencyScore": 75
  }
}
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "content": "根据你的做题记录...",
    "role": "assistant"
  }
}
```

#### POST /ai/evaluate-code
AI代码评估

**请求参数：**
```json
{
  "code": "#include <iostream>\nint main() { ... }",
  "language": "cpp",
  "question": "实现二分查找",
  "description": "给定有序数组...",
  "stdinInput": "5\n1 2 3 4 5\n3"
}
```

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "score": 85,
    "stars": 2,
    "output": "2",
    "analysis": "代码实现了基本的二分查找...",
    "correctness": "正确",
    "quality": "良好",
    "efficiency": "O(log n)",
    "suggestions": ["可以添加边界检查"]
  }
}
```

---

### 🆕 代码运行接口

#### POST /run-code
运行代码（支持多语言）

**请求参数：**
```json
{
  "language": "cpp",
  "code": "#include <iostream>\nint main() { std::cout << \"Hello\"; return 0; }"
}
```

**支持的语言：**
- `cpp` - C++
- `python` - Python
- `java` - Java
- `js` - JavaScript (Node.js)

**响应数据：**
```json
{
  "code": 200,
  "data": {
    "output": "Hello",
    "error": ""
  }
}
```

---

## 统计信息

| 分类 | 已实现 | 总计 |
|------|--------|------|
| 认证相关 | 1 | 1 |
| 用户相关 | 10 | 10 |
| 头像相关 | 4 | 4 |
| 关卡相关 | 2 | 2 |
| 论坛相关 | 8 | 8 |
| 错题相关 | 6 | 6 |
| 课程相关 | 4 | 4 |
| 排行榜相关 | 1 | 1 |
| 学习计划相关 | 5 | 5 |
| AI相关 | 2 | 2 |
| 代码运行 | 1 | 1 |
| **总计** | **44** | **44** |

---

## 更新日志

### 2026-04-05 更新内容

#### 🆕 新增接口
1. **头像相关** - 新增4个接口
   - `POST /users/me/avatar` - 上传头像
   - `DELETE /users/me/avatar` - 删除头像
   - `GET /users/me/avatar` - 获取头像
   - `POST /users/me/avatar-from-url` - 从URL上传头像

2. **学习计划相关** - 新增5个接口
   - `GET /learning-plans` - 获取学习计划列表
   - `GET /learning-plans/current` - 获取当前学习计划
   - `POST /learning-plans/generate` - 生成个性化学习计划（AI驱动）
   - `POST /learning-plans` - 保存学习计划
   - `DELETE /learning-plans/{id}` - 删除学习计划

3. **AI相关** - 新增2个接口
   - `POST /ai/chat` - AI对话
   - `POST /ai/evaluate-code` - AI代码评估

4. **代码运行** - 新增1个接口
   - `POST /run-code` - 运行代码（支持cpp/python/java/js）

5. **错题相关** - 新增2个接口
   - `GET /errors/{id}` - 获取错题详情
   - `PUT /errors/{id}` - 更新错题

6. **论坛相关** - 新增3个接口
   - `GET /forum-posts/{id}` - 获取帖子详情
   - `GET /forum-posts/{id}/comments` - 获取帖子评论
   - `GET /forum-posts/{id}/hot-comments` - 获取热门评论

7. **课程相关** - 新增3个接口
   - `GET /courses/{courseId}/comments` - 获取课程评论
   - `POST /courses/{courseId}/comments` - 添加课程评论
   - `POST /comments/{id}/like` - 点赞课程评论

#### ✅ 状态更新
- 所有接口均已实现，状态从 "⚠️ 需要后端实现" 更新为 "✅ 已实现"
