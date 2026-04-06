# 宝塔面板部署指南

## 一、准备工作

### 1. 服务器要求
- Linux 系统（CentOS 7+ / Ubuntu 18+）
- 安装宝塔面板
- 安装 Java 17+（宝塔软件商店搜索安装）
- 安装 Nginx（宝塔软件商店安装）

### 2. 本地打包

```bash
# 1. 进入后端目录
cd demo

# 2. 打包（跳过测试）
.\mvnw clean package -DskipTests

# 3. 前端构建
cd ..
npm run build
```

打包后会生成：
- `demo/target/demo-0.0.1-SNAPSHOT.jar` - 后端 jar 包
- `dist/` - 前端静态文件

---

## 二、宝塔面板配置

### 1. 创建站点

1. 登录宝塔面板
2. 点击「网站」→「添加站点」
3. 填写域名：`yourdomain.com`
4. 选择 PHP 版本：纯静态
5. 点击提交

### 2. 上传文件

```bash
# 通过宝塔面板文件管理器或 FTP 上传

# 创建项目目录
mkdir -p /www/wwwroot/algo-mind

# 上传文件到：
# - /www/wwwroot/algo-mind/demo-0.0.1-SNAPSHOT.jar  (后端 jar)
# - /www/wwwroot/algo-mind/dist/                     (前端构建文件)
# - /www/wwwroot/algo-mind/uploads/avatars/          (头像上传目录，需创建)
# - /www/wwwroot/algo-mind/logs/                     (日志目录，需创建)
```

### 3. 配置 Nginx

1. 宝塔面板 → 网站 → 找到你的站点 → 设置
2. 点击「配置文件」
3. 将 `nginx-baota.conf` 的内容复制进去（修改域名和路径）
4. 保存

### 4. 配置 Java 项目（Supervisor 管理）

**方式一：使用 Supervisor（推荐）**

1. 宝塔面板 → 软件商店 → 安装 Supervisor
2. 点击 Supervisor → 添加守护进程
3. 填写：
   - 名称：`algo-mind-backend`
   - 启动目录：`/www/wwwroot/algo-mind`
   - 启动命令：`java -jar demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
   - 环境变量：
     ```
     CORS_ALLOWED_ORIGINS=https://www.yourdomain.com,https://yourdomain.com
     AI_API_KEY=你的AI_API密钥
     ```
4. 点击确定

**方式二：使用部署脚本**

```bash
# 1. 上传 deploy-baota.sh 到服务器
# 2. 修改脚本中的配置（DOMAIN 等）
# 3. 执行部署
chmod +x deploy-baota.sh
./deploy-baota.sh
```

---

## 三、配置文件修改

### 1. 修改 application-prod.yml

```yaml
# /www/wwwroot/algo-mind/application-prod.yml

server:
  port: 8080  # 确保和 Nginx 代理的端口一致

cors:
  # ★ 改成你的真实域名
  allowed-origins: https://www.yourdomain.com,https://yourdomain.com

file:
  # ★ 改成你的实际路径
  upload-dir: /www/wwwroot/algo-mind/uploads/avatars
```

### 2. 修改 Nginx 配置

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;  # ★ 改成你的域名
    
    location / {
        root /www/wwwroot/algo-mind/dist;  # ★ 前端路径
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;  # ★ 后端端口
        # ... 其他配置
    }
}
```

---

## 四、启动服务

### 1. 启动后端

如果使用 Supervisor：
- 宝塔面板 → Supervisor → 启动 algo-mind-backend

如果使用脚本：
```bash
cd /www/wwwroot/algo-mind
./deploy-baota.sh
```

### 2. 检查日志

```bash
# 查看启动日志
tail -f /www/wwwroot/algo-mind/logs/app.log

# 应该看到：
# ====== CORS 配置已加载 [prod] ======
# 允许的源: [https://www.yourdomain.com, https://yourdomain.com]
```

### 3. 重启 Nginx

宝塔面板 → 网站 → 找到站点 → 设置 → 重启 Nginx

---

## 五、验证部署

### 1. 检查后端是否运行

```bash
# 查看进程
ps -ef | grep demo

# 查看端口
netstat -tlnp | grep 8080
```

### 2. 测试 API

```bash
# 测试健康检查
curl https://yourdomain.com/api/health

# 测试跨域
curl -H "Origin: https://yourdomain.com" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS \
     https://yourdomain.com/api/users/me/avatar \
     -v
```

### 3. 浏览器测试

1. 访问 `https://yourdomain.com`
2. 打开 F12 → Network
3. 尝试上传头像
4. 检查 OPTIONS 请求是否返回 200

---

## 六、常见问题

### 1. 上传头像提示 "网络错误"

**检查：**
```bash
# 1. 后端是否运行
ps -ef | grep java

# 2. CORS 配置是否正确
tail /www/wwwroot/algo-mind/logs/app.log | grep "CORS"

# 3. Nginx 配置是否正确
nginx -t
```

### 2. 头像上传成功但图片不显示

**检查：**
```bash
# 1. 上传目录权限
ls -la /www/wwwroot/algo-mind/uploads/

# 2. Nginx 是否有访问权限
chmod -R 755 /www/wwwroot/algo-mind/uploads/
```

### 3. 后端启动失败：端口被占用

```bash
# 查看端口占用
netstat -tlnp | grep 8080

# 杀死占用进程
kill -9 <PID>

# 或修改 application-prod.yml 使用其他端口
```

### 4. SSL/HTTPS 配置

宝塔面板 → 网站 → 找到站点 → SSL → 申请 Let's Encrypt 证书

---

## 七、更新部署

```bash
# 1. 本地重新打包
.\mvnw clean package -DskipTests
npm run build

# 2. 上传新 jar 和 dist

# 3. 如果使用 Supervisor：
#    宝塔面板 → Supervisor → 重启 algo-mind-backend

# 4. 如果使用脚本：
./deploy-baota.sh
```

---

## 八、安全建议

1. **修改默认端口**：不要用 8080，改成随机端口
2. **配置防火墙**：只开放 80/443 端口
3. **定期备份**：数据库和上传文件
4. **启用 HTTPS**：宝塔面板申请 SSL 证书
5. **设置强密码**：数据库和后台管理
