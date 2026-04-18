````
# AlgoMind

AlgoMind 是一个面向算法学习与训练的前后端分离项目，包含关卡练习、代码运行、AI 辅助评测、错题分析、学习计划、论坛社区等核心模块。

前端基于 `Vue 3 + Vite + Pinia + Vue Router + Element Plus`，后端基于 `Spring Boot 3 + Spring Security + Spring Data JPA`，默认通过 Vite 代理把前端 `/api` 请求转发到本地后端。

## 项目结构

- `src/`: Vue 3 前端应用
- `demo/`: Spring Boot 后端服务
- `docker-compose.yml`: 容器化部署编排入口
- `init.sql` / `algomind_db.sql`: 数据初始化相关文件

## 环境要求

- Node.js `^20.19.0 || >=22.12.0`
- Java `21`
- Maven Wrapper（仓库已提供 `demo/mvnw` / `demo/mvnw.cmd`）
- MySQL（如需连接真实数据库）

## 快速启动

### 1. 启动后端

在仓库根目录执行：

```powershell
cd demo
.\mvnw.cmd spring-boot:run
```

后端默认运行在 `http://localhost:8000/api`。

### 2. 启动前端

另开一个终端，在仓库根目录执行：

```powershell
npm install
npm run dev
```

前端开发服务器默认由 Vite 启动，`/api` 请求会代理到 `http://localhost:8000`。

## 常用命令

```powershell
npm run build
```

构建前端生产包。

```powershell
cd demo
.\mvnw.cmd test
```

运行后端测试。

## 文档入口

- [开发文档](./开发文档.md)
- [架构说明书](./架构说明书.md)

如果你需要更完整的接口、协作和架构背景，建议先从上面两份文档继续阅读。

````

