# AlgoMind 开发文档

> 版本: 1.0.0  
> 生成日期: 2026-04-02  
> 适用项目: AlgoMind 算法学习平台

---

## 第1章：项目概述

### 1.1 项目简介

AlgoMind 是一个面向算法学习者的在线平台，提供关卡挑战、代码运行、错题分析、学习计划和社区论坛等功能。

### 1.2 技术栈

#### 前端技术栈
| 技术 | 版本 | 用途 |
|-----|------|------|
| Vue | 3.5.30 | 前端框架 |
| Vue Router | 5.0.3 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Element Plus | 2.13.6 | UI 组件库 |
| Axios | 1.14.0 | HTTP 客户端 |
| Vite | 7.3.1 | 构建工具 |
| GSAP | 3.14.2 | 动画库 |
| Sass | 1.98.0 | CSS 预处理器 |

#### 后端技术栈
| 技术 | 版本 | 用途 |
|-----|------|------|
| Spring Boot | 3.5.13 | 后端框架 |
| JDK | 21 | Java 运行时 |
| Spring Data JPA | 3.5.13 | ORM 框架 |
| MySQL | 8.x | 主数据库 |
| H2 | 2.x | 开发/测试数据库 |
| Lombok | 1.18.x | 代码简化 |
| SpringDoc | 2.8.16 | API 文档 |

### 1.3 项目结构目录树

```
algo-mind/
├── .vscode/                          # VS Code 配置
│   └── extensions.json
│
├── demo/                             # Spring Boot 后端
│   ├── .mvn/                         # Maven Wrapper
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/demo/
│   │   │   │   ├── config/           # 配置类
│   │   │   │   │   ├── FileStorageConfig.java
│   │   │   │   │   ├── RestTemplateConfig.java
│   │   │   │   │   ├── WebConfig.java          # 跨域配置
│   │   │   │   │   └── WebMvcConfig.java       # 静态资源映射
│   │   │   │   ├── controller/       # 控制器层
│   │   │   │   │   ├── AvatarController.java   # 头像管理
│   │   │   │   │   ├── CodeController.java     # 代码运行
│   │   │   │   │   ├── CourseController.java   # 课程管理
│   │   │   │   │   ├── ErrorController.java    # 错题管理
│   │   │   │   │   ├── ForumPostController.java # 论坛帖子
│   │   │   │   │   ├── GlobalExceptionHandler.java # 全局异常
│   │   │   │   │   ├── LearningPlanController.java # 学习计划
│   │   │   │   │   ├── LevelController.java    # 关卡管理
│   │   │   │   │   ├── LoginController.java    # 登录认证
│   │   │   │   │   ├── RankingController.java  # 排行榜
│   │   │   │   │   └── UserController.java     # 用户管理
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   │   ├── Course.java
│   │   │   │   │   ├── ErrorItem.java
│   │   │   │   │   ├── ForumPost.java
│   │   │   │   │   ├── LearningPlan.java
│   │   │   │   │   ├── Level.java
│   │   │   │   │   └── User.java
│   │   │   │   ├── repository/       # 数据访问层
│   │   │   │   │   ├── CourseRepository.java
│   │   │   │   │   ├── ErrorItemRepository.java
│   │   │   │   │   ├── ForumPostRepository.java
│   │   │   │   │   ├── LearningPlanRepository.java
│   │   │   │   │   ├── LevelRepository.java
│   │   │   │   │   └── UserRepository.java
│   │   │   │   ├── service/          # 服务层
│   │   │   │   │   ├── FileStorageService.java
│   │   │   │   │   └── UserService.java
│   │   │   │   ├── DemoApplication.java
│   │   │   │   └── Result.java       # 统一响应封装
│   │   │   └── resources/
│   │   │       └── application.yaml
│   │   └── test/
│   ├── temp_code/                    # 代码运行临时目录
│   ├── uploads/avatars/              # 头像上传目录
│   ├── pom.xml
│   └── mvnw / mvnw.cmd
│
├── src/                              # Vue3 前端
│   ├── api/                          # API 接口层
│   │   ├── index.js                  # axios 配置
│   │   └── user.js                   # 用户相关 API
│   ├── assets/                       # 静态资源
│   │   ├── base.css
│   │   ├── logo.svg
│   │   └── main.css
│   ├── components/                   # 公共组件
│   │   ├── icons/
│   │   ├── AIAssistant.vue
│   │   ├── AvatarPendant.vue
│   │   ├── CourseDetail.vue
│   │   ├── ErrorAnalysisDialog.vue
│   │   ├── FlowerPagination.vue
│   │   ├── HelloWorld.vue
│   │   ├── LevelCard.vue
│   │   ├── SearchBar.vue
│   │   ├── TheWelcome.vue
│   │   └── WelcomeItem.vue
│   ├── router/                       # 路由配置
│   │   └── index.js
│   ├── stores/                       # Pinia 状态管理
│   │   ├── counter.js
│   │   ├── course.js
│   │   ├── error.js
│   │   ├── forum.js
│   │   ├── level.js
│   │   └── user.js
│   ├── views/                        # 页面视图
│   │   ├── AboutView.vue
│   │   ├── Challenge.vue             # 答题挑战
│   │   ├── Courses.vue               # 课程列表
│   │   ├── Errors.vue                # 错题本
│   │   ├── Forum.vue                 # 论坛社区
│   │   ├── Home.vue                  # 首页
│   │   ├── HomeView.vue
│   │   ├── Levels.vue                # 关卡列表
│   │   ├── Login.vue                 # 登录页
│   │   ├── Profile.vue               # 个人中心
│   │   └── Ranking.vue               # 排行榜
│   ├── App.vue
│   ├── constants.js                  # 常量定义
│   └── main.js                       # 入口文件
│
├── public/                           # 公共资源
├── index.html
├── package.json
├── vite.config.js
└── 开发文档.md
```

---

## 第2章：环境配置与启动

### 2.1 前端启动

#### 环境要求
- Node.js: ^20.19.0 || >=22.12.0
- npm 或 yarn

#### 安装依赖
```bash
cd algo-mind
npm install
```

#### 启动开发服务器
```bash
npm run dev
```
默认访问地址: http://localhost:5173

#### 构建生产环境
```bash
npm run build
```

#### 环境变量配置
创建 `.env` 文件（可选）:
```env
# API 基础地址
VITE_API_BASE_URL=http://localhost:8080/api

# 其他配置
VITE_APP_TITLE=AlgoMind
```

### 2.2 后端启动

#### 环境要求
- JDK 21
- Maven 3.8+
- MySQL 8.0+ (可选，默认使用 H2)

#### 方式一：使用 Maven Wrapper
```bash
cd demo
./mvnw spring-boot:run
```

#### 方式二：使用本地 Maven
```bash
cd demo
mvn spring-boot:run
```

默认访问地址: http://localhost:8080

#### 后端配置文件 (application.yaml)
```yaml
spring:
  application:
    name: algo-mind
  
  # 数据库配置 (H2 - 开发环境)
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  
  # H2 控制台 (开发环境)
  h2:
    console:
      enabled: true
      path: /h2-console
  
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 5MB
      max-request-size: 10MB

# 文件存储配置
file:
  storage:
    upload-dir: ./uploads/avatars
    max-file-size: 5242880  # 5MB
    allowed-types: image/jpeg,image/png,image/gif,image/webp

# 服务器配置
server:
  port: 8080
```

#### MySQL 生产环境配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/algo_mind?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境建议用 validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

### 2.3 数据库建表脚本

#### H2 自动建表
使用 `spring.jpa.hibernate.ddl-auto=update` 自动创建表结构。

#### MySQL 手动建表脚本
```sql
-- 用户表
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(100),
    password VARCHAR(100),
    points INT DEFAULT 0,
    bio VARCHAR(500),
    gender VARCHAR(20),
    avatar VARCHAR(255),
    target_track VARCHAR(20),
    weekly_goal INT DEFAULT 10,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    status_emoji VARCHAR(10),
    status_mood VARCHAR(20),
    is_busy BOOLEAN DEFAULT FALSE,
    busy_auto_reply VARCHAR(200),
    busy_end_time TIMESTAMP,
    github VARCHAR(100),
    website VARCHAR(100)
);

-- 关卡表
CREATE TABLE t_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_locked BOOLEAN DEFAULT TRUE,
    order_num INT DEFAULT 0,
    problem_count INT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 错题表
CREATE TABLE t_error_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    difficulty VARCHAR(20),
    type VARCHAR(50),
    error_type VARCHAR(50),
    error_message TEXT,
    error_code TEXT,
    solution TEXT,
    is_resolved BOOLEAN DEFAULT FALSE,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES t_user(id)
);

-- 论坛帖子表
CREATE TABLE t_forum_post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT,
    author_name VARCHAR(50),
    author_avatar VARCHAR(255),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) DEFAULT 'discussion',
    likes INT DEFAULT 0,
    comments INT DEFAULT 0,
    is_liked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES t_user(id)
);

-- 课程表
CREATE TABLE t_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    difficulty VARCHAR(20),
    duration VARCHAR(50),
    students INT DEFAULT 0,
    rating DECIMAL(2,1) DEFAULT 5.0,
    tags VARCHAR(500),
    image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 学习计划表
CREATE TABLE t_learning_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration VARCHAR(50),
    daily_hours INT DEFAULT 2,
    topics VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES t_user(id)
);
```

---

## 第3章：API 接口文档

### 3.1 认证模块

#### 3.1.1 用户登录
- **路径**: `/api/login`
- **方法**: POST
- **描述**: 用户登录认证

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| username | string | 是 | 用户名/账号 |
| password | string | 是 | 密码 |

**请求示例**:
```json
{
  "username": "admin",
  "password": "admin"
}
```

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "name": "系统管理员",
      "email": "admin@example.com",
      "avatar": "/uploads/avatars/xxx.jpg",
      "bio": "先把基础打扎实，再冲更高难度。",
      "gender": "unknown",
      "targetTrack": "algo",
      "weeklyGoal": 10,
      "github": "",
      "website": ""
    },
    "points": 999
  },
  "requestId": "uuid",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

---

### 3.2 用户模块

#### 3.2.1 更新用户信息
- **路径**: `/api/users/me`
- **方法**: PUT
- **描述**: 更新当前登录用户信息

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| name | string | 否 | 用户名 |
| bio | string | 否 | 个人简介 |
| avatar | string | 否 | 头像URL |
| email | string | 否 | 邮箱 |
| github | string | 否 | GitHub链接 |
| website | string | 否 | 个人网站 |

**请求示例**:
```json
{
  "name": "张三",
  "bio": "热爱算法，持续学习",
  "avatar": "/uploads/avatars/xxx.jpg",
  "email": "zhangsan@example.com",
  "github": "https://github.com/zhangsan",
  "website": "https://zhangsan.dev"
}
```

#### 3.2.2 获取用户刷题热力图
- **路径**: `/api/users/me/heatmap`
- **方法**: GET
- **描述**: 获取用户刷题热力图数据

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| year | integer | 否 | 年份，默认当前年 |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "date": "2026-01-15",
      "count": 3
    },
    {
      "date": "2026-01-16",
      "count": 5
    }
  ]
}
```

#### 3.2.3 获取用户统计数据
- **路径**: `/api/users/me/stats`
- **方法**: GET
- **描述**: 获取用户刷题统计数据

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalSolved": 150,
    "totalCreated": 10,
    "streakDays": 12,
    "todaySolved": 2,
    "weeklySolved": 15,
    "monthlySolved": 58
  }
}
```

#### 3.2.4 获取用户活动记录
- **路径**: `/api/users/me/activities`
- **方法**: GET
- **描述**: 分页获取用户活动记录

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认20 |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "type": "solve",
        "description": "完成了 两数之和",
        "createdAt": "2026-01-15T10:30:00Z"
      }
    ],
    "page": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

#### 3.2.5 获取用户状态
- **路径**: `/api/users/me/status`
- **方法**: GET

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "emoji": "🚀",
    "mood": "专注模式",
    "isBusy": false,
    "busyAutoReply": "",
    "busyEndTime": null
  }
}
```

#### 3.2.6 更新用户状态
- **路径**: `/api/users/me/status`
- **方法**: PUT

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| emoji | string | 否 | 状态表情 |
| mood | string | 否 | 状态描述 |
| isBusy | boolean | 否 | 是否忙碌 |
| busyAutoReply | string | 否 | 自动回复内容 |
| busyEndTime | string | 否 | 忙碌结束时间(ISO8601) |

#### 3.2.7 获取用户排名
- **路径**: `/api/users/me/ranking`
- **方法**: GET

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| track | string | 否 | 赛道(all/algo/ai)，默认all |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "rank": 15,
    "total": 1000,
    "points": 999,
    "track": "algo"
  }
}
```

#### 3.2.8 获取积分历史
- **路径**: `/api/users/me/points-history`
- **方法**: GET

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认20 |

#### 3.2.9 获取创建的题目
- **路径**: `/api/users/me/created-problems`
- **方法**: GET

**请求参数**: 同积分历史

#### 3.2.10 获取解决的题目
- **路径**: `/api/users/me/solved-problems`
- **方法**: GET

**请求参数**: 同积分历史

---

### 3.3 头像模块

#### 3.3.1 上传头像
- **路径**: `/api/users/me/avatar`
- **方法**: POST
- **Content-Type**: multipart/form-data

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | File | 是 | 图片文件(jpg/png/gif/webp) |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "avatarUrl": "/uploads/avatars/avatar_1_abc123.jpg",
    "message": "头像上传成功"
  }
}
```

#### 3.3.2 删除头像
- **路径**: `/api/users/me/avatar`
- **方法**: DELETE

#### 3.3.3 获取头像
- **路径**: `/api/users/me/avatar`
- **方法**: GET

#### 3.3.4 从URL上传头像
- **路径**: `/api/users/me/avatar-from-url`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| url | string | 是 | 图片URL |

---

### 3.4 关卡模块

#### 3.4.1 获取关卡列表
- **路径**: `/api/levels`
- **方法**: GET

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "数组基础",
      "difficulty": "简单",
      "type": "数组",
      "isLocked": false,
      "problemCount": 10
    }
  ]
}
```

#### 3.4.2 提交答案
- **路径**: `/api/submit`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| levelId | integer | 是 | 关卡ID |
| problemId | integer | 是 | 题目ID |
| answer | string | 是 | 用户答案 |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "correct": true,
    "message": "回答正确！",
    "points": 10
  }
}
```

---

### 3.5 代码运行模块

#### 3.5.1 运行代码
- **路径**: `/api/run-code`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| code | string | 是 | 代码内容 |
| language | string | 是 | 编程语言(java/python/cpp) |
| input | string | 否 | 标准输入 |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "output": "Hello World",
    "executionTime": 120,
    "memoryUsage": 10240,
    "status": "success"
  }
}
```

---

### 3.6 错题模块

#### 3.6.1 获取错题列表
- **路径**: `/api/errors`
- **方法**: GET

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "两数之和",
      "difficulty": "简单",
      "type": "数组",
      "errorType": "边界条件",
      "isResolved": false,
      "createdAt": "2026-01-15T10:30:00Z"
    }
  ]
}
```

#### 3.6.2 添加错题
- **路径**: `/api/errors`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| title | string | 是 | 题目名称 |
| difficulty | string | 否 | 难度 |
| type | string | 否 | 类型 |
| errorType | string | 否 | 错误类型 |
| errorMessage | string | 否 | 错误信息 |
| errorCode | string | 否 | 错误代码 |
| solution | string | 否 | 解决方案 |
| note | string | 否 | 备注 |

#### 3.6.3 删除错题
- **路径**: `/api/errors/{id}`
- **方法**: DELETE

#### 3.6.4 更新错题
- **路径**: `/api/errors/{id}`
- **方法**: PUT

#### 3.6.5 AI分析错题
- **路径**: `/api/error-analysis`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| errorId | integer | 是 | 错题ID |
| code | string | 否 | 错误代码 |
| message | string | 否 | 错误信息 |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "analysis": "错误原因分析...",
    "suggestions": ["建议1", "建议2"],
    "relatedConcepts": ["数组", "边界条件"]
  }
}
```

---

### 3.7 排行榜模块

#### 3.7.1 获取排行榜
- **路径**: `/api/ranking`
- **方法**: GET

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| track | string | 否 | 赛道(all/algo/ai)，默认all |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "rank": 1,
      "userId": 1,
      "userName": "张三",
      "avatar": "/uploads/avatars/xxx.jpg",
      "points": 9999,
      "solvedCount": 500
    }
  ]
}
```

---

### 3.8 课程模块

#### 3.8.1 获取课程列表
- **路径**: `/api/courses`
- **方法**: GET

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认10 |
| category | string | 否 | 分类筛选 |

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "title": "算法基础入门",
        "description": "从零开始学习算法",
        "category": "基础",
        "difficulty": "入门",
        "duration": "20小时",
        "students": 1000,
        "rating": 4.8,
        "tags": ["数组", "链表", "栈", "队列"],
        "image": "/images/course1.jpg"
      }
    ],
    "page": 1,
    "pageSize": 10,
    "total": 50
  }
}
```

---

### 3.9 论坛模块

#### 3.9.1 获取帖子列表
- **路径**: `/api/forum-posts`
- **方法**: GET

**响应示例**:
```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "authorName": "张三",
      "authorAvatar": "/uploads/avatars/xxx.jpg",
      "title": "如何高效学习动态规划？",
      "content": "分享一些学习心得...",
      "type": "discussion",
      "likes": 100,
      "comments": 20,
      "isLiked": false,
      "createdAt": "2026-01-15T10:30:00Z"
    }
  ]
}
```

#### 3.9.2 发布帖子
- **路径**: `/api/forum-posts`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| title | string | 是 | 标题 |
| content | string | 是 | 内容 |
| type | string | 否 | 类型(discussion/question/share) |

#### 3.9.3 点赞帖子
- **路径**: `/api/forum-posts/{id}/like`
- **方法**: POST

#### 3.9.4 更新评论数
- **路径**: `/api/forum-posts/{id}/comment-count`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| count | integer | 是 | 评论数量 |

---

### 3.10 学习计划模块

#### 3.10.1 获取学习计划列表
- **路径**: `/api/learning-plans`
- **方法**: GET

#### 3.10.2 获取当前学习计划
- **路径**: `/api/learning-plans/current`
- **方法**: GET

#### 3.10.3 生成学习计划
- **路径**: `/api/learning-plans/generate`
- **方法**: POST

**请求参数**:
| 字段名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| duration | string | 是 | 计划时长(1month/3months/6months) |
| dailyHours | integer | 是 | 每日学习时长 |
| targetLevel | string | 是 | 目标水平(beginner/intermediate/advanced) |
| weakTopics | array | 否 | 薄弱知识点 |

#### 3.10.4 创建学习计划
- **路径**: `/api/learning-plans`
- **方法**: POST

#### 3.10.5 删除学习计划
- **路径**: `/api/learning-plans/{id}`
- **方法**: DELETE

---

## 第4章：数据模型

### 4.1 实体关系图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    User     │       │  ErrorItem  │       │  ForumPost  │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄──────┤ user_id(FK) │       │ author_id   │◄──┐
│ name        │       │ title       │       │ author_name │   │
│ email       │       │ difficulty  │       │ title       │   │
│ password    │       │ type        │       │ content     │   │
│ points      │       │ error_type  │       │ likes       │   │
│ ...         │       │ ...         │       │ ...         │   │
└─────────────┘       └─────────────┘       └─────────────┘   │
       │                                                      │
       │              ┌─────────────┐       ┌─────────────┐   │
       │              │  Level      │       │   Course    │   │
       │              ├─────────────┤       ├─────────────┤   │
       │              │ id (PK)     │       │ id (PK)     │   │
       │              │ title       │       │ title       │   │
       │              │ difficulty  │       │ category    │   │
       │              │ type        │       │ difficulty  │   │
       │              │ ...         │       │ ...         │   │
       │              └─────────────┘       └─────────────┘   │
       │                                                      │
       └──────────────►┌─────────────┐                        │
                       │LearningPlan │◄───────────────────────┘
                       ├─────────────┤
                       │ id (PK)     │
                       │ user_id(FK) │
                       │ title       │
                       │ ...         │
                       └─────────────┘
```

### 4.2 前后端字段映射表

#### User 实体
| 后端字段 | 前端字段 | 类型 | 说明 |
|---------|---------|------|------|
| id | id | Long | 用户ID |
| name | name | String | 用户名 |
| email | email | String | 邮箱 |
| password | - | String | 密码(前端不传输) |
| points | points | Integer | 积分 |
| bio | bio | String | 个人简介 |
| gender | gender | String | 性别 |
| avatar | avatar | String | 头像URL |
| targetTrack | targetTrack | String | 目标赛道 |
| weeklyGoal | weeklyGoal | Integer | 周目标 |
| statusEmoji | status.emoji | String | 状态表情 |
| statusMood | status.mood | String | 状态描述 |
| isBusy | status.isBusy | Boolean | 是否忙碌 |
| busyAutoReply | status.busyAutoReply | String | 自动回复 |
| busyEndTime | status.busyEndTime | String | 忙碌结束时间 |
| github | github | String | GitHub链接 |
| website | website | String | 个人网站 |

#### ErrorItem 实体
| 后端字段 | 前端字段 | 类型 | 说明 |
|---------|---------|------|------|
| id | id | Long | 错题ID |
| title | title | String | 题目名称 |
| difficulty | difficulty | String | 难度 |
| type | type | String | 类型 |
| errorType | errorType | String | 错误类型 |
| errorMessage | errorMessage | String | 错误信息 |
| errorCode | errorCode | String | 错误代码 |
| solution | solution | String | 解决方案 |
| isResolved | isResolved | Boolean | 是否已解决 |
| note | note | String | 备注 |
| createdAt | createdAt | String | 创建时间 |

#### ForumPost 实体
| 后端字段 | 前端字段 | 类型 | 说明 |
|---------|---------|------|------|
| id | id | Long | 帖子ID |
| authorName | author.name | String | 作者名 |
| authorAvatar | author.avatar | String | 作者头像 |
| title | title | String | 标题 |
| content | content | String | 内容 |
| type | type | String | 类型 |
| likes | likes | Integer | 点赞数 |
| comments | comments | Integer | 评论数 |
| isLiked | isLiked | Boolean | 是否已点赞 |
| createdAt | createdAt | String | 创建时间 |

---

## 第5章：修复记录

### 5.1 后端修复记录

| 序号 | 文件 | 问题 | 修复方案 | 轮次 |
|-----|------|------|---------|------|
| 1 | UserController.java | 未使用的 import (JsonProcessingException, ObjectMapper) | 删除未使用的 import | 第1轮 |
| 2 | AvatarController.java | 未使用的 import (InputStream, URL) | 删除未使用的 import | 第1轮 |
| 3 | User.java | 实体字段缺少约束注解 | 添加 @Column(length=...) 注解 | 第1轮 |
| 4 | UserService.java | 密码明文存储 | 添加 BCryptPasswordEncoder 加密 | 第1轮 |
| 5 | AvatarController.java | 用户创建逻辑重复 | 抽取到 UserService.getOrCreateUser() | 第1轮 |
| 6 | FileStorageService.java | URL下载文件缺少类型验证 | 添加 contentType 验证 | 第1轮 |
| 7 | Result.java | 缺少无参构造方法 | 添加 @NoArgsConstructor 注解 | 第1轮 |

### 5.2 前端修复记录

| 序号 | 文件 | 问题 | 修复方案 | 轮次 |
|-----|------|------|---------|------|
| 1 | Login.vue | 表单字段名与后端不一致 (email → username) | 修改 form.email 为 form.username | 第1轮 |
| 2 | Profile.vue | 响应判断逻辑错误 | response.success → response.data.code === 0 | 第1轮 |
| 3 | Profile.vue | loadMoreActivities 参数名错误 | limit → pageSize | 第1轮 |
| 4 | Profile.vue | 活动字段映射错误 | title → description | 第1轮 |
| 5 | api/user.js | 缺少 login/logout 接口定义 | 添加 login/logout 方法 | 第1轮 |
| 6 | stores/user.js | 登录错误处理不完善 | 添加 tryLocalLogin 备用逻辑 | 第1轮 |
| 7 | api/index.js | 响应拦截器未统一处理错误 | 添加状态码判断和错误提示 | 第1轮 |

### 5.3 API 标准化修复记录

| 序号 | 问题 | 修复方案 | 轮次 |
|-----|------|---------|------|
| 1 | 硬编码用户ID (currentUserId = 1L) | [待修复] 需要实现 JWT 认证解析 | 第2轮 |
| 2 | 错误码体系不完整 | [待修复] 补充 401xx, 409xx, 429xx 错误码 | 第2轮 |
| 3 | RESTful 命名不规范 (/submit, /run-code) | [建议] 改为 /answers, /code-execution | 第2轮 |

---

## 第6章：已知风险与待办

### 6.1 待确认项目

| 序号 | 项目 | 风险等级 | 说明 |
|-----|------|---------|------|
| 1 | JWT 认证实现 | 🔴 高 | 当前使用硬编码用户ID，需要实现完整的 JWT 认证流程 |
| 2 | 代码执行安全 | 🔴 高 | 代码运行接口可能存在安全风险，需要沙箱隔离 |
| 3 | 文件上传限制 | 🟡 中 | 需要验证文件类型和大小，防止恶意文件上传 |
| 4 | SQL 注入防护 | 🟢 低 | 使用 JPA 参数绑定，风险较低 |
| 5 | XSS 防护 | 🟡 中 | 需要验证用户输入，防止跨站脚本攻击 |

### 6.2 性能优化建议

| 序号 | 优化项 | 优先级 | 预期效果 |
|-----|-------|-------|---------|
| 1 | 添加 Redis 缓存 | 高 | 减少数据库查询，提升响应速度 |
| 2 | 数据库索引优化 | 高 | 加速查询，特别是排行榜和热力图 |
| 3 | 图片 CDN 加速 | 中 | 头像和课程图片使用 CDN |
| 4 | 接口分页优化 | 中 | 大数据量接口添加游标分页 |
| 5 | 前端懒加载 | 中 | 图片和组件按需加载 |

### 6.3 安全加固建议

| 序号 | 加固项 | 优先级 | 说明 |
|-----|-------|-------|------|
| 1 | HTTPS 强制 | 高 | 生产环境强制使用 HTTPS |
| 2 | 请求限流 | 高 | 防止暴力破解和 DDoS 攻击 |
| 3 | 敏感信息加密 | 高 | 数据库中敏感字段加密存储 |
| 4 | 操作日志审计 | 中 | 记录关键操作日志 |
| 5 | 定期安全扫描 | 中 | 使用工具定期扫描漏洞 |

### 6.4 功能待办

| 序号 | 功能 | 优先级 | 状态 |
|-----|------|-------|------|
| 1 | 错题更新接口前端对接 | 高 | 未开始 |
| 2 | 评论功能完整实现 | 中 | 部分完成 |
| 3 | 消息通知系统 | 中 | 未开始 |
| 4 | 用户关注/粉丝 | 低 | 未开始 |
| 5 | 积分兑换商城 | 低 | 未开始 |

### 6.5 技术债务

| 序号 | 债务项 | 影响 | 还款计划 |
|-----|-------|------|---------|
| 1 | 硬编码用户ID | 无法多用户测试 | 第3轮迭代完成 JWT |
| 2 | 前端模拟数据过多 | 影响真实体验 | 逐步替换为真实接口 |
| 3 | 缺少单元测试 | 代码质量风险 | 补充核心模块测试 |
| 4 | 缺少 API 文档自动化 | 维护成本高 | 接入 Swagger UI |

---

## 附录

### A. 错误码速查表

| 错误码 | 含义 | HTTP状态 |
|-------|------|---------|
| 0 | 成功 | 200 |
| 40001 | 参数错误 | 400 |
| 40002 | 编译失败 | 400 |
| 40101 | 未登录/Token无效 | 401 |
| 40301 | 权限不足 | 403 |
| 40401 | 资源不存在 | 404 |
| 40901 | 资源冲突 | 409 |
| 42901 | 请求过于频繁 | 429 |
| 50001 | 服务器内部错误 | 500 |

### B. 开发团队联系方式

- 项目负责人: [待填写]
- 后端开发: [待填写]
- 前端开发: [待填写]
- 测试人员: [待填写]

### C. 相关链接

- 项目仓库: [待填写]
- 测试环境: http://localhost:5173
- API文档: http://localhost:8080/swagger-ui.html
- H2控制台: http://localhost:8080/h2-console

---

> 本文档由 AI 辅助生成，最后更新时间: 2026-04-02
