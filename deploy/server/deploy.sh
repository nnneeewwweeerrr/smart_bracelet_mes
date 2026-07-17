#!/bin/bash
# ============================================================
# 智能手环MES系统 - 一键部署脚本
# 使用方法：chmod +x deploy.sh && sudo ./deploy.sh
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_ok()   { echo -e "${GREEN}[✓]${NC} $1"; }
print_info() { echo -e "${YELLOW}[!]${NC} $1"; }
print_err()  { echo -e "${RED}[✗]${NC} $1"; }

# 部署目录
DEPLOY_DIR="/opt/mes"
SQL_DIR="${DEPLOY_DIR}/sql"
BACKEND_DIR="${DEPLOY_DIR}/backend"
WEB_DIST_DIR="${DEPLOY_DIR}/web-dist"
LOG_DIR="/var/log/mes"

echo ""
echo "============================================"
echo "  智能手环MES系统 - 一键部署"
echo "============================================"
echo ""

# ===== 检查root权限 =====
if [ "$EUID" -ne 0 ]; then
  print_err "请使用root用户运行：sudo ./deploy.sh"
  exit 1
fi

# ===== 检测系统类型 =====
if [ -f /etc/os-release ]; then
  . /etc/os-release
  OS=$ID
else
  print_err "无法检测操作系统类型"
  exit 1
fi

print_info "检测到操作系统: $OS"

# ===== 安装函数 =====
install_pkg() {
  if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "alinux" ]; then
    yum install -y "$1" >/dev/null 2>&1
  elif [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    apt-get install -y "$1" >/dev/null 2>&1
  fi
}

# ===== Step 1: 安装JDK 17 =====
echo ""
print_info "Step 1: 安装 JDK 17..."
if command -v java &>/dev/null; then
  JAVA_VER=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}')
  print_ok "JDK已安装: $JAVA_VER"
else
  if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "alinux" ]; then
    yum install -y java-17-openjdk java-17-openjdk-devel >/dev/null 2>&1
  else
    apt-get update >/dev/null 2>&1
    apt-get install -y openjdk-17-jdk >/dev/null 2>&1
  fi
  if command -v java &>/dev/null; then
    print_ok "JDK 17 安装成功"
  else
    print_err "JDK安装失败，请手动安装"
    exit 1
  fi
fi

# ===== Step 2: 安装MySQL 8.0 =====
echo ""
print_info "Step 2: 安装 MySQL 8.0..."
if command -v mysql &>/dev/null; then
  print_ok "MySQL已安装"
else
  if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "alinux" ]; then
    yum install -y https://dev.mysql.com/get/mysql80-community-release-el7-5.noarch.rpm >/dev/null 2>&1
    yum install -y mysql-community-server >/dev/null 2>&1
  else
    apt-get update >/dev/null 2>&1
    apt-get install -y mysql-server >/dev/null 2>&1
  fi

  systemctl start mysqld 2>/dev/null || systemctl start mysql 2>/dev/null
  systemctl enable mysqld 2>/dev/null || systemctl enable mysql 2>/dev/null

  if command -v mysql &>/dev/null; then
    print_ok "MySQL安装成功"
  else
    print_err "MySQL安装失败，请手动安装"
    exit 1
  fi
fi

# 设置MySQL密码
print_info "配置MySQL密码..."
mysqladmin -u root password "ABC123" 2>/dev/null || true
print_ok "MySQL密码已设置"

# 创建数据库
print_info "创建数据库..."
mysql -u root -pABC123 -e "CREATE DATABASE IF NOT EXISTS smart_bracelet_mes DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" 2>/dev/null
mysql -u root -pABC123 -e "CREATE DATABASE IF NOT EXISTS mes_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" 2>/dev/null
print_ok "数据库创建完成"

# ===== Step 3: 安装Maven =====
echo ""
print_info "Step 3: 安装 Maven..."
if command -v mvn &>/dev/null; then
  print_ok "Maven已安装"
else
  if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "alinux" ]; then
    yum install -y maven >/dev/null 2>&1
  else
    apt-get install -y maven >/dev/null 2>&1
  fi
  if command -v mvn &>/dev/null; then
    print_ok "Maven安装成功"
  else
    print_err "Maven安装失败，请手动安装"
    exit 1
  fi
fi

# ===== Step 4: 安装Nginx =====
echo ""
print_info "Step 4: 安装 Nginx..."
if command -v nginx &>/dev/null; then
  print_ok "Nginx已安装"
else
  if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "alinux" ]; then
    yum install -y nginx >/dev/null 2>&1
  else
    apt-get install -y nginx >/dev/null 2>&1
  fi
  systemctl start nginx
  systemctl enable nginx
  if command -v nginx &>/dev/null; then
    print_ok "Nginx安装成功"
  else
    print_err "Nginx安装失败，请手动安装"
    exit 1
  fi
fi

# ===== Step 5: 准备部署目录 =====
echo ""
print_info "Step 5: 准备部署目录..."
mkdir -p ${DEPLOY_DIR} ${LOG_DIR}
print_ok "部署目录: ${DEPLOY_DIR}"

# ===== Step 6: 导入数据库 =====
echo ""
print_info "Step 6: 导入数据库..."
if [ -d "${SQL_DIR}" ]; then
  # 主数据库
  for sql_file in schema.sql init_data.sql sample_data.sql supplement_data.sql; do
    if [ -f "${SQL_DIR}/${sql_file}" ]; then
      print_info "导入 ${sql_file}..."
      mysql -u root -pABC123 smart_bracelet_mes < "${SQL_DIR}/${sql_file}" 2>/dev/null || true
    fi
  done

  # 演示数据库
  for sql_file in demo_schema.sql demo_data.sql; do
    if [ -f "${SQL_DIR}/${sql_file}" ]; then
      print_info "导入 ${sql_file}..."
      mysql -u root -pABC123 mes_demo < "${SQL_DIR}/${sql_file}" 2>/dev/null || true
    fi
  done

  print_ok "数据库导入完成"
else
  print_err "SQL目录不存在: ${SQL_DIR}"
  print_info "请确保SQL文件已正确放置"
fi

# ===== Step 7: 编译后端 =====
echo ""
print_info "Step 7: 编译后端..."
if [ -d "${BACKEND_DIR}" ] && [ -f "${BACKEND_DIR}/pom.xml" ]; then
  cd ${BACKEND_DIR}
  print_info "正在编译（可能需要几分钟下载依赖）..."
  mvn clean package -DskipTests -q 2>&1 | tail -5
  if [ -f "${BACKEND_DIR}/target/smart-bracelet-mes-1.0.0.jar" ]; then
    print_ok "后端编译成功"
  else
    print_err "后端编译失败"
    exit 1
  fi
else
  print_err "后端源码目录不存在: ${BACKEND_DIR}"
  exit 1
fi

# ===== Step 8: 创建系统服务 =====
echo ""
print_info "Step 8: 创建后端系统服务..."
cat > /etc/systemd/system/mes-backend.service << 'EOF'
[Unit]
Description=Smart Bracelet MES Backend
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/mes/backend
ExecStart=/usr/bin/java -jar /opt/mes/backend/target/smart-bracelet-mes-1.0.0.jar
Restart=on-failure
RestartSec=10
StandardOutput=file:/var/log/mes/backend.log
StandardError=file:/var/log/mes/backend-error.log

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl restart mes-backend
systemctl enable mes-backend
sleep 3

if systemctl is-active --quiet mes-backend; then
  print_ok "后端服务已启动"
else
  print_err "后端服务启动失败，查看日志: tail -f /var/log/mes/backend-error.log"
  exit 1
fi

# ===== Step 9: 配置Nginx =====
echo ""
print_info "Step 9: 配置Nginx..."
cat > /etc/nginx/conf.d/mes.conf << 'EOF'
server {
    listen       80;
    server_name  _;

    client_max_body_size 50m;

    # Web前端静态文件
    location / {
        root   /opt/mes/web-dist;
        index  index.html;
        try_files $uri $uri/ /index.html;
    }

    # API反向代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
    }
}
EOF

# 删除默认配置（如果冲突）
if [ -f /etc/nginx/sites-enabled/default ]; then
  rm -f /etc/nginx/sites-enabled/default
fi

nginx -t 2>/dev/null
if [ $? -eq 0 ]; then
  systemctl reload nginx
  print_ok "Nginx配置完成"
else
  print_err "Nginx配置语法错误"
  nginx -t
  exit 1
fi

# ===== Step 10: 验证 =====
echo ""
print_info "Step 10: 验证部署..."
sleep 5

# 检查后端API
API_RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/demo/stock-in/list 2>/dev/null || echo "000")
if [ "$API_RESULT" = "200" ]; then
  print_ok "后端API正常 (HTTP 200)"
else
  print_info "后端API返回: $API_RESULT (可能还在启动中，请稍后检查)"
fi

# 检查Web前端
WEB_RESULT=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/ 2>/dev/null || echo "000")
if [ "$WEB_RESULT" = "200" ]; then
  print_ok "Web前端正常 (HTTP 200)"
else
  print_info "Web前端返回: $WEB_RESULT"
fi

# ===== 完成 =====
echo ""
echo "============================================"
echo -e "${GREEN}  部署完成！${NC}"
echo "============================================"
echo ""
echo "  Web管理后台:  http://47.105.66.12"
echo "  后端API:      http://47.105.66.12/api"
echo "  WebSocket:    ws://47.105.66.12/ws/mes"
echo ""
echo "  默认登录:     admin / admin123"
echo ""
echo "  常用命令:"
echo "    查看后端日志:   tail -f /var/log/mes/backend.log"
echo "    重启后端:       systemctl restart mes-backend"
echo "    重启Nginx:      systemctl reload nginx"
echo "    重启MySQL:      systemctl restart mysqld"
echo ""
echo "  小程序: 已配置API地址为 http://47.105.66.12/api"
echo "  微信开发者工具中勾选「不校验合法域名」即可调试"
echo ""
echo "============================================"
