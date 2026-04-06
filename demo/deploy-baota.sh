#!/bin/bash
# 宝塔面板部署脚本

# ===== 配置区（根据你的实际情况修改）=====
PROJECT_NAME="algo-mind"
DOMAIN="yourdomain.com"           # 你的域名
BACKEND_PORT="8080"               # 后端端口（不要和 Nginx 冲突）
JAR_NAME="demo-0.0.1-SNAPSHOT.jar"

# 路径配置（宝塔面板默认路径）
WEB_ROOT="/www/wwwroot/${PROJECT_NAME}"
UPLOAD_DIR="${WEB_ROOT}/uploads/avatars"
LOG_DIR="${WEB_ROOT}/logs"
JAR_PATH="${WEB_ROOT}/${JAR_NAME}"

# ===== 部署步骤 =====

echo "====== 开始部署 ${PROJECT_NAME} ======"

# 1. 创建目录
echo "创建目录..."
mkdir -p ${UPLOAD_DIR}
mkdir -p ${LOG_DIR}

# 2. 停止旧服务
echo "停止旧服务..."
pid=$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}')
if [ -n "$pid" ]; then
    kill -9 $pid
    echo "已停止进程: $pid"
fi

# 3. 备份旧 jar（可选）
if [ -f "${JAR_PATH}" ]; then
    mv ${JAR_PATH} ${JAR_PATH}.bak
    echo "已备份旧版本"
fi

# 4. 复制新 jar
echo "部署新版本..."
cp target/${JAR_NAME} ${JAR_PATH}

# 5. 设置权限
chmod 755 ${JAR_PATH}
chmod -R 755 ${UPLOAD_DIR}
chmod -R 755 ${LOG_DIR}

# 6. 启动服务
echo "启动服务..."
cd ${WEB_ROOT}

# 方式1：直接启动（测试用）
# nohup java -jar ${JAR_NAME} --spring.profiles.active=prod > ${LOG_DIR}/app.log 2>&1 &

# 方式2：带环境变量启动（推荐）
export CORS_ALLOWED_ORIGINS="https://www.${DOMAIN},https://${DOMAIN}"
export AI_API_KEY="你的AI_API密钥"
nohup java -jar ${JAR_NAME} --spring.profiles.active=prod > ${LOG_DIR}/app.log 2>&1 &

echo "服务已启动，端口: ${BACKEND_PORT}"
echo "日志文件: ${LOG_DIR}/app.log"
echo "====== 部署完成 ======"

# 7. 查看启动日志
sleep 3
tail -n 50 ${LOG_DIR}/app.log
