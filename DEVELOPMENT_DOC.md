# AlgoMind 开发文档

> 最后更新：2026-04-06  
> 适用范围：`algo-mind` 前端 + `demo` Spring Boot 后端  
> 当前重点：已接入 JWT 鉴权、统一当前用户解析、头像与论坛链路修复

---

## 1. 项目简介

AlgoMind 是一个算法学习平台，包含以下核心模块：

- 关卡练习
- 代码运行与 AI 辅助
- 错题本
- 学习计划
- 论坛社区
- 课程评论
- 用户资料与头像
- 排行榜

前端使用 Vue 3 + Vite，后端使用 Spring Boot + Spring Data JPA。

---

## 2. 技术栈

### 2.1 前端

- Vue 3
- Vue Router
- Pinia
- Element Plus
- Axios
- Vite
- GSAP
- Sass

### 2.2 后端

- Spring Boot 3.5.x
- Spring Data JPA
- Spring Security
- JJWT
- H2
- MySQL 8
- Lombok
- SpringDoc
- ip2region

---

## 3. 目录结构

下面是当前项目的推荐理解方式。不是把所有文件都列出来，而是把“最常改、最关键”的目录讲清楚。

```text
algo-mind/
├─ src/                                   # Vue 前端源码
│  ├─ api/
│  │  ├─ index.js                         # axios 实例、请求拦截、统一注入 Bearer token
│  │  └─ user.js                          # 用户、学习计划、头像等 API 封装
│  ├─ assets/                             # 全局样式和静态资源
│  ├─ components/                         # 可复用组件
│  │  ├─ AvatarUpload.vue                 # 头像上传组件
│  │  ├─ AIAssistant.vue                  # AI 助手组件
│  │  ├─ CourseDetail.vue                 # 课程详情组件
│  │  └─ ...
│  ├─ router/
│  │  └─ index.js                         # 前端路由配置
│  ├─ stores/
│  │  ├─ user.js                          # 用户状态、登录态、本地缓存
│  │  ├─ level.js                         # 关卡状态
│  │  ├─ forum.js                         # 论坛状态
│  │  ├─ error.js                         # 错题状态
│  │  └─ course.js                        # 课程状态
│  ├─ utils/
│  │  └─ file.js                          # 文件 URL / 头像 URL 归一化
│  ├─ views/                              # 页面级组件
│  │  ├─ Login.vue                        # 登录页
│  │  ├─ Home.vue                         # 首页
│  │  ├─ Levels.vue                       # 关卡页
│  │  ├─ Challenge.vue                    # 挑战页
│  │  ├─ Courses.vue                      # 课程页
│  │  ├─ Forum.vue                        # 论坛列表
│  │  ├─ ForumPost.vue                    # 帖子详情
│  │  ├─ Errors.vue                       # 错题本
│  │  ├─ Ranking.vue                      # 排行榜
│  │  └─ Profile.vue                      # 个人中心
│  ├─ App.vue
│  ├─ constants.js
│  └─ main.js
├─ public/                                # 前端公开静态资源
├─ demo/                                  # Spring Boot 后端
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/com/example/demo/
│  │  │  │  ├─ auth/                      # JWT、当前用户、认证异常处理
│  │  │  │  │  ├─ AuthProperties.java
│  │  │  │  │  ├─ AuthenticatedUser.java
│  │  │  │  │  ├─ CurrentUserService.java
│  │  │  │  │  ├─ JwtAuthenticationFilter.java
│  │  │  │  │  ├─ JwtService.java
│  │  │  │  │  ├─ RestAccessDeniedHandler.java
│  │  │  │  │  ├─ RestAuthenticationEntryPoint.java
│  │  │  │  │  └─ UnauthorizedException.java
│  │  │  │  ├─ config/                    # 安全、跨域、上传、初始化配置
│  │  │  │  │  ├─ CorsConfig.java
│  │  │  │  │  ├─ DataInitializer.java
│  │  │  │  │  ├─ PasswordConfig.java
│  │  │  │  │  ├─ SecurityConfig.java
│  │  │  │  │  ├─ WebMvcConfig.java
│  │  │  │  │  └─ IpLocationProperties.java
│  │  │  │  ├─ controller/                # REST 接口入口
│  │  │  │  │  ├─ LoginController.java
│  │  │  │  │  ├─ UserController.java
│  │  │  │  │  ├─ AvatarController.java
│  │  │  │  │  ├─ ForumPostController.java
│  │  │  │  │  ├─ CourseCommentController.java
│  │  │  │  │  ├─ LearningPlanController.java
│  │  │  │  │  ├─ ErrorController.java
│  │  │  │  │  ├─ LevelController.java
│  │  │  │  │  ├─ RankingController.java
│  │  │  │  │  ├─ AIController.java
│  │  │  │  │  └─ GlobalExceptionHandler.java
│  │  │  │  ├─ dto/                       # DTO 对象
│  │  │  │  ├─ entity/                    # JPA 实体
│  │  │  │  │  ├─ User.java
│  │  │  │  │  ├─ ForumPost.java
│  │  │  │  │  ├─ ForumComment.java
│  │  │  │  │  ├─ CourseComment.java
│  │  │  │  │  ├─ LearningPlan.java
│  │  │  │  │  └─ ...
│  │  │  │  ├─ repository/                # 数据访问层
│  │  │  │  ├─ service/                   # 业务服务
│  │  │  │  │  ├─ FileStorageService.java
│  │  │  │  │  ├─ UserService.java
│  │  │  │  │  ├─ AIService.java
│  │  │  │  │  └─ ...
│  │  │  │  ├─ util/                      # IP / 文件等工具类
│  │  │  │  │  ├─ ClientIpUtil.java
│  │  │  │  │  ├─ Ip2RegionService.java
│  │  │  │  │  ├─ IpLocation.java
│  │  │  │  │  └─ IpLocationUtil.java
│  │  │  │  ├─ DemoApplication.java
│  │  │  │  └─ Result.java                # 统一响应对象
│  │  │  └─ resources/
│  │  │     ├─ application.yaml           # dev / test / prod 配置
│  │  │     └─ ipdb/
│  │  │        └─ README.txt              # ip2region 数据文件说明
│  │  └─ test/
│  │     ├─ java/com/example/demo/
│  │     │  ├─ DemoApplicationTests.java
│  │     │  └─ AuthFlowIntegrationTest.java
│  │     └─ resources/
│  │        └─ application.yaml           # 测试专用 H2 + JWT 配置
│  ├─ uploads/                            # 本地上传目录
│  ├─ temp_code/                          # 代码运行临时目录
│  ├─ pom.xml
│  ├─ mvnw
│  └─ mvnw.cmd
├─ API_CHECKLIST.md                       # 接口盘点清单
├─ README.md
├─ DEVELOPMENT_DOC.md                     # 当前这份文档
├─ 开发文档.md                             # 旧版中文文档
├─ package.json
└─ vite.config.js
```

---

## 4. 关键模块说明

### 4.1 前端登录态

前端登录态主要在这几处：

- [src/api/index.js](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/src/api/index.js)
- [src/stores/user.js](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/src/stores/user.js)
- [src/views/Login.vue](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/src/views/Login.vue)

当前逻辑：

1. 登录页调用 `/api/login`
2. 后端返回 `token + user`
3. 前端把用户信息和 token 存入 `localStorage`
4. axios 请求拦截器自动把 token 放进：
   `Authorization: Bearer xxx`

### 4.2 后端鉴权

后端鉴权链路主要在：

- [SecurityConfig.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/config/SecurityConfig.java)
- [JwtAuthenticationFilter.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/auth/JwtAuthenticationFilter.java)
- [JwtService.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/auth/JwtService.java)
- [CurrentUserService.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/auth/CurrentUserService.java)

职责分工：

- `SecurityConfig`
  - 定义哪些接口公开
  - 定义哪些接口必须登录
  - 挂载 JWT 过滤器

- `JwtAuthenticationFilter`
  - 每次请求进来先读请求头
  - 如果有 Bearer token，就尝试解析
  - 解析成功后把用户信息写入 SecurityContext

- `JwtService`
  - 登录成功时生成 token
  - 请求进来时解析 token

- `CurrentUserService`
  - 给业务层统一提供：
    - `getCurrentUser()`
    - `requireCurrentUserId()`
    - `requireCurrentUserEntity()`

### 4.3 当前用户已接入的接口

目前已切到“真实当前用户”的模块包括：

- 头像接口
- 用户资料接口
- 学习计划接口
- 论坛发帖 / 评论 / 点赞 / 删帖
- 课程评论

也就是说，现在这些接口已经不再写死 `1L`。

---

## 5. 登录到识别当前用户的完整流程

```text
用户输入账号密码
  ↓
前端 POST /api/login
  ↓
LoginController 校验账号密码
  ↓
JwtService 生成 token
  ↓
前端保存 token 到 localStorage
  ↓
之后前端请求自动带上 Authorization: Bearer token
  ↓
JwtAuthenticationFilter 拦截请求
  ↓
JwtService 解析 token
  ↓
CurrentUser 写入 SecurityContext
  ↓
Controller 中调用 CurrentUserService
  ↓
业务代码得到当前真实用户
```

如果 token 不存在、过期或无效：

- 需要登录的接口返回 `40101`
- 权限不足时返回 `40301`

---

## 6. 安全配置说明

### 6.1 公开接口

当前默认公开的接口主要有：

- `/login`
- `/uploads/**`
- `/h2-console/**`
- `/swagger-ui/**`
- `GET /levels`
- `GET /ranking`
- `GET /hot-questions`
- `GET /bilibili/**`
- `GET /courses/**`
- `GET /forum-posts/**`

### 6.2 需要登录的接口

除上面这些公开接口外，其它接口默认都需要登录。

例如：

- `PUT /users/me`
- `POST /forum-posts`
- `POST /forum-posts/{id}/like`
- `POST /courses/{courseId}/comments`
- `GET /learning-plans`

---

## 7. 重要业务变更记录

### 7.1 头像模块

之前的问题：

- 后端写死当前用户为 `1L`
- 找不到用户时会偷偷创建 admin
- URL 上传头像存在明显风险
- 前后端头像 URL 格式不统一

现在的状态：

- 头像接口走真实当前用户
- 不再自动创建账号
- 统一返回可访问的头像路径
- 前端统一做头像 URL 归一化

### 7.2 论坛模块

之前的问题：

- 发帖、点赞、评论都写死用户 1
- 删除帖子没有真正的归属校验

现在的状态：

- 发帖会记录当前登录用户
- 点赞按当前用户计算
- 评论作者来自当前登录用户
- 删除帖子会检查帖子归属

### 7.3 课程评论

之前的问题：

- 直接信任前端传来的 `userId`、`userName`、`userAvatar`

现在的状态：

- 评论身份全部来自后端当前用户

### 7.4 学习计划与用户资料

之前的问题：

- 统一写死 `1L`

现在的状态：

- 已全部改成 `CurrentUserService`

---

## 8. 本地开发启动

### 8.1 前端启动

在项目根目录执行：

```bash
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

前端构建：

```bash
npm run build
```

### 8.2 后端启动

在 `demo` 目录执行：

```bash
.\mvnw.cmd spring-boot:run
```

或：

```bash
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

### 8.3 常用验证命令

后端编译：

```bash
cd demo
.\mvnw.cmd -q -DskipTests compile
```

后端测试：

```bash
cd demo
.\mvnw.cmd -q test
```

前端构建验证：

```bash
npm run build
```

---

## 9. 配置文件说明

### 9.1 后端主配置

文件：

- [application.yaml](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/resources/application.yaml)

当前主要配置项包括：

- 数据源配置
- H2 / MySQL 切换
- 文件上传限制
- JWT 配置
- IP 归属地配置

### 9.2 JWT 配置

位于 `application.yaml` 的 `auth.jwt` 下：

```yaml
auth:
  jwt:
    issuer: algo-mind
    secret: your-secret
    expiration-minutes: 10080
```

说明：

- `issuer`：签发者标识
- `secret`：JWT 密钥，生产环境必须替换
- `expiration-minutes`：过期时间，单位分钟

### 9.3 测试配置

文件：

- [demo/src/test/resources/application.yaml](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/test/resources/application.yaml)

测试环境默认使用：

- H2 内存数据库
- 测试用 JWT 密钥

---

## 10. 默认账号与开发数据

开发环境会通过 [DataInitializer.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/config/DataInitializer.java) 初始化演示数据。

默认演示账号：

```text
admin@example.com / 123456
```

这份初始化数据会生成：

- 用户
- 关卡
- 论坛帖子
- 评论

---

## 11. 测试说明

目前已补充一条关键鉴权测试：

- [AuthFlowIntegrationTest.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/test/java/com/example/demo/AuthFlowIntegrationTest.java)

覆盖点：

1. 未登录访问受保护接口，应该返回 401
2. 登录成功后拿到 token
3. 带 token 再访问受保护接口，应该成功

---

## 12. 当前已知限制

### 12.1 已解决的问题

- 写死当前用户 `1L`
- 假登录
- 评论信任前端传用户身份
- 头像接口自动造 admin

### 12.2 仍可继续优化的地方

- 目前只有“登录用户”概念，还没有细分管理员角色
- 论坛历史老数据中，部分帖子可能没有完整 `userId`
- 前端 bundle 仍偏大，`npm run build` 会有 chunk size 警告
- 生产环境还需要替换正式 JWT 密钥和正式数据库配置

---

## 13. 推荐维护顺序

后续如果继续开发，建议按这个顺序理解和改动：

1. 先看 [src/api/index.js](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/src/api/index.js) 和 [src/stores/user.js](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/src/stores/user.js)
2. 再看 [SecurityConfig.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/config/SecurityConfig.java) 和 [CurrentUserService.java](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/demo/src/main/java/com/example/demo/auth/CurrentUserService.java)
3. 然后再进具体业务 Controller
4. 最后再看实体和仓库层

这样不会一上来就陷进业务细节里。

---

## 14. 相关文档

- [API_CHECKLIST.md](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/API_CHECKLIST.md)
- [README.md](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/README.md)
- [开发文档.md](/C:/Users/Administrator/Desktop/Caixin_work/algo-mind/开发文档.md)

如果后面继续维护，建议优先以这份 `DEVELOPMENT_DOC.md` 为主，再逐步合并旧文档内容，避免多份文档长期不一致。
