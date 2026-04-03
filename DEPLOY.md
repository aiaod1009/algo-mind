# 部署指南

## 开发环境（本地开发）

### 1. 启动后端
```bash
cd demo
.\mvnw spring-boot:run
# 后端运行在 http://localhost:3000/api
```

### 2. 启动前端
```bash
npm run dev
# 前端运行在 http://localhost:5176
# Vite 代理会自动将 /api 请求转发到后端
```

---

## 生产环境部署

### 方案一：前后端分离部署（推荐）

#### 后端部署
```bash
cd demo
# 打包
.\mvnw clean package -DskipTests

# 运行（指定生产环境配置）
java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# 或者设置环境变量指定前端地址
set CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

#### 前端部署
```bash
# 构建生产包
npm run build

# 将 dist 目录部署到 Nginx 或其他静态服务器
```

#### Nginx 配置示例
```nginx
server {
    listen 80;
    server_name your-frontend-domain.com;

    # 前端静态资源
    location / {
        root /path/to/your/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://backend-server:3000/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # 关键：处理跨域
        add_header Access-Control-Allow-Origin $http_origin always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "*" always;
        add_header Access-Control-Allow-Credentials "true" always;
        
        # 处理 OPTIONS 预检
        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }
}
```

---

### 方案二：前后端合并部署（简单）

将前端构建后的 `dist` 目录复制到后端的 `src/main/resources/static`，让 Spring Boot 同时提供前端静态资源和 API。

```bash
# 1. 前端构建
npm run build

# 2. 复制到后端
xcopy /E /I dist demo\src\main\resources\static

# 3. 后端打包
 cd demo
.\mvnw clean package -DskipTests

# 4. 运行
java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

这样访问 `http://backend-server:3000/` 就能直接看到前端页面。

---

## 关键配置说明

### 后端 CORS 配置

优先级：环境变量 > 配置文件 > 默认值

**方式1：环境变量（推荐生产环境）**
```bash
# Windows
set CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Linux/Mac
export CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

**方式2：配置文件**
```yaml
# application.yaml
cors:
  allowed-origins: https://yourdomain.com,https://www.yourdomain.com
```

**方式3：不配置（开发环境）**
不设置时自动使用默认值（localhost 地址）

---

## 常见问题

### 1. 部署后头像上传报错 "网络错误"

检查：
- 后端是否正确设置了 `CORS_ALLOWED_ORIGINS` 环境变量
- Nginx 是否正确配置了 `/api` 代理
- 浏览器控制台查看具体错误信息

### 2. 头像上传成功但图片不显示

检查：
- 后端 `file.upload-dir` 配置是否正确
- 生产环境需要确保上传目录持久化（不要用临时目录）

### 3. 跨域问题仍然存在

尝试：
1. 重启后端服务（配置变更需要重启）
2. 检查浏览器缓存（Ctrl+F5 强制刷新）
3. 查看后端日志确认 CORS 配置是否加载

---

## 生产环境检查清单

- [ ] 后端配置了正确的 `CORS_ALLOWED_ORIGINS`
- [ ] 数据库从 H2 切换到 MySQL
- [ ] 文件上传目录持久化配置
- [ ] AI API Key 使用环境变量注入
- [ ] 关闭 H2 Console（安全）
- [ ] 配置 HTTPS
- [ ] 设置合适的日志级别
