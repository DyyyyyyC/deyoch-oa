# OA系统部署指南

## 概述

本文档描述了如何使用Docker和Docker Compose部署OA系统。系统包含前端Vue应用、后端Spring Boot应用、MySQL数据库和Redis缓存。

## 系统要求

### 硬件要求
- CPU: 2核心以上
- 内存: 4GB以上
- 磁盘: 20GB以上可用空间
- 网络: 稳定的网络连接

### 软件要求
- Docker 20.10+
- Docker Compose 2.0+
- Git（用于获取源代码）

## 快速开始

### 1. 获取源代码

```bash
git clone <repository-url>
cd deyoch-oa
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量（可选）
vim .env
```

### 3. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 访问系统

- 前端地址: http://localhost
- 后端API: http://localhost:8080
- 数据库: localhost:3306
- Redis: localhost:6379

默认管理员账号:
- 用户名: admin
- 密码: admin123

## 详细部署步骤

### 1. 环境准备

#### 安装Docker

**Ubuntu/Debian:**
```bash
# 更新包索引
sudo apt-get update

# 安装必要的包
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release

# 添加Docker官方GPG密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加Docker仓库
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装Docker
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 添加用户到docker组
sudo usermod -aG docker $USER
```

**CentOS/RHEL:**
```bash
# 安装必要的包
sudo yum install -y yum-utils

# 添加Docker仓库
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装Docker
sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 添加用户到docker组
sudo usermod -aG docker $USER
```

#### 验证安装

```bash
# 验证Docker版本
docker --version

# 验证Docker Compose版本
docker compose version

# 测试Docker运行
docker run hello-world
```

### 2. 配置文件说明

#### 环境变量配置 (.env)

```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=root123          # MySQL root密码
MYSQL_DATABASE=deyoch_oa            # 数据库名称
MYSQL_USER=deyoch                   # 数据库用户名
MYSQL_PASSWORD=deyoch123            # 数据库密码
MYSQL_PORT=3306                     # MySQL端口

# Redis配置
REDIS_PASSWORD=                     # Redis密码（可选）
REDIS_PORT=6379                     # Redis端口

# 后端配置
SPRING_PROFILES_ACTIVE=docker       # Spring配置文件
JWT_SECRET=your-secret-key          # JWT密钥（生产环境必须修改）
JWT_EXPIRATION=86400                # JWT过期时间（秒）
FILE_UPLOAD_PATH=/app/uploads       # 文件上传路径
BACKEND_PORT=8080                   # 后端端口

# 前端配置
FRONTEND_PORT=80                    # 前端端口

# 时区配置
TZ=Asia/Shanghai                    # 时区设置
```

### 3. 服务启动

#### 完整启动

```bash
# 构建并启动所有服务
docker-compose up -d

# 等待服务启动完成
docker-compose logs -f backend | grep "Started DeyochOaBackendApplication"
```

#### 分步启动

```bash
# 1. 启动数据库和缓存
docker-compose up -d mysql redis

# 2. 等待数据库就绪
docker-compose logs -f mysql | grep "ready for connections"

# 3. 启动后端服务
docker-compose up -d backend

# 4. 等待后端就绪
docker-compose logs -f backend | grep "Started DeyochOaBackendApplication"

# 5. 启动前端服务
docker-compose up -d frontend
```

### 4. 数据库初始化

系统会自动执行数据库初始化脚本，包括：
- 创建基础表结构
- 插入初始数据
- 执行升级脚本

如需手动执行：

```bash
# 进入MySQL容器
docker-compose exec mysql mysql -u root -p

# 执行SQL脚本
mysql> source /docker-entrypoint-initdb.d/upgrade_v2.0.sql;
```

### 5. 服务管理

#### 查看服务状态

```bash
# 查看所有服务状态
docker-compose ps

# 查看特定服务状态
docker-compose ps backend

# 查看服务健康状态
docker-compose exec backend curl -f http://localhost:8080/actuator/health
```

#### 查看日志

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend
docker-compose logs frontend

# 实时跟踪日志
docker-compose logs -f backend

# 查看最近100行日志
docker-compose logs --tail=100 backend
```

#### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend

# 停止服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷
docker-compose down -v
```

### 6. 数据备份与恢复

#### 数据库备份

```bash
# 创建备份目录
mkdir -p backups

# 备份数据库
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} > backups/deyoch_oa_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份文件
gzip backups/deyoch_oa_$(date +%Y%m%d_%H%M%S).sql
```

#### 数据库恢复

```bash
# 解压备份文件
gunzip backups/deyoch_oa_20240101_120000.sql.gz

# 恢复数据库
docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} < backups/deyoch_oa_20240101_120000.sql
```

#### 文件备份

```bash
# 备份上传文件
docker run --rm -v deyoch-oa_backend_uploads:/data -v $(pwd)/backups:/backup alpine tar czf /backup/uploads_$(date +%Y%m%d_%H%M%S).tar.gz -C /data .

# 恢复上传文件
docker run --rm -v deyoch-oa_backend_uploads:/data -v $(pwd)/backups:/backup alpine tar xzf /backup/uploads_20240101_120000.tar.gz -C /data
```

## 生产环境配置

### 1. 安全配置

#### 修改默认密码

```bash
# 修改.env文件中的密码
MYSQL_ROOT_PASSWORD=your-strong-password
MYSQL_PASSWORD=your-db-password
JWT_SECRET=your-very-long-and-random-secret-key
```

#### 网络安全

```bash
# 只暴露必要的端口
# 在docker-compose.yml中注释掉不需要外部访问的端口
# ports:
#   - "3306:3306"  # 注释掉MySQL端口
#   - "6379:6379"  # 注释掉Redis端口
```

#### SSL/TLS配置

```nginx
# 在nginx.conf中添加SSL配置
server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    # SSL配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # 其他配置...
}
```

### 2. 性能优化

#### 资源限制

```yaml
# 在docker-compose.yml中添加资源限制
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.5'
          memory: 512M
```

#### 数据库优化

```bash
# 调整MySQL配置
# 编辑mysql/conf.d/my.cnf
innodb_buffer_pool_size=1G  # 根据可用内存调整
max_connections=500         # 根据并发需求调整
```

### 3. 监控配置

#### 健康检查

```bash
# 创建健康检查脚本
cat > health-check.sh << 'EOF'
#!/bin/bash
# 检查所有服务健康状态
docker-compose ps | grep -q "Up (healthy)" || exit 1
curl -f http://localhost/health || exit 1
curl -f http://localhost:8080/actuator/health || exit 1
EOF

chmod +x health-check.sh
```

#### 日志监控

```bash
# 配置日志轮转
cat > /etc/logrotate.d/docker-compose << 'EOF'
/var/lib/docker/containers/*/*.log {
    rotate 7
    daily
    compress
    size=1M
    missingok
    delaycompress
    copytruncate
}
EOF
```

## 故障排查

### 常见问题

#### 1. 服务启动失败

```bash
# 查看详细错误信息
docker-compose logs backend

# 检查端口占用
netstat -tlnp | grep :8080

# 检查磁盘空间
df -h
```

#### 2. 数据库连接失败

```bash
# 检查MySQL服务状态
docker-compose ps mysql

# 测试数据库连接
docker-compose exec mysql mysql -u root -p

# 检查网络连接
docker-compose exec backend ping mysql
```

#### 3. 文件上传失败

```bash
# 检查上传目录权限
docker-compose exec backend ls -la /app/uploads

# 检查磁盘空间
docker-compose exec backend df -h /app/uploads
```

#### 4. 前端无法访问后端

```bash
# 检查nginx配置
docker-compose exec frontend nginx -t

# 检查代理配置
docker-compose exec frontend curl -f http://backend:8080/actuator/health
```

### 性能问题

#### 1. 响应缓慢

```bash
# 检查系统资源使用
docker stats

# 检查数据库慢查询
docker-compose exec mysql mysql -u root -p -e "SHOW PROCESSLIST;"

# 检查应用日志
docker-compose logs backend | grep -i "slow\|timeout\|error"
```

#### 2. 内存不足

```bash
# 检查内存使用
free -h
docker stats --no-stream

# 调整JVM内存设置
# 在docker-compose.yml中修改backend服务的环境变量
JAVA_OPTS: "-Xmx512m -Xms256m"
```

## 维护操作

### 1. 更新系统

```bash
# 拉取最新代码
git pull origin main

# 重新构建镜像
docker-compose build --no-cache

# 重启服务
docker-compose up -d
```

### 2. 清理资源

```bash
# 清理未使用的镜像
docker image prune -f

# 清理未使用的容器
docker container prune -f

# 清理未使用的网络
docker network prune -f

# 清理未使用的数据卷
docker volume prune -f
```

### 3. 定期维护

```bash
# 创建维护脚本
cat > maintenance.sh << 'EOF'
#!/bin/bash
# 每日维护脚本

# 备份数据库
docker-compose exec mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD} ${MYSQL_DATABASE} | gzip > backups/daily_$(date +%Y%m%d).sql.gz

# 清理旧日志
find /var/lib/docker/containers -name "*.log" -mtime +7 -delete

# 清理旧备份
find backups -name "*.sql.gz" -mtime +30 -delete

# 检查磁盘空间
df -h | awk '$5 > 80 {print "Warning: " $0}'
EOF

chmod +x maintenance.sh

# 添加到crontab
echo "0 2 * * * /path/to/maintenance.sh" | crontab -
```

## 联系支持

如果遇到问题，请：

1. 查看本文档的故障排查部分
2. 检查GitHub Issues
3. 联系技术支持团队

---

**注意**: 在生产环境中部署前，请务必修改所有默认密码和密钥，并进行充分的测试。