#!/bin/bash

# OA系统部署脚本
# 使用方法: ./deploy.sh [环境] [操作]
# 环境: dev|test|prod
# 操作: start|stop|restart|update|backup|logs

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.yml"
ENV_FILE="$PROJECT_DIR/.env"

# 默认值
ENVIRONMENT=${1:-dev}
ACTION=${2:-start}

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    log_success "依赖检查通过"
}

# 检查环境文件
check_env_file() {
    log_info "检查环境配置文件..."
    
    if [ ! -f "$ENV_FILE" ]; then
        log_warning "环境配置文件不存在，从模板创建..."
        if [ -f "$PROJECT_DIR/.env.example" ]; then
            cp "$PROJECT_DIR/.env.example" "$ENV_FILE"
            log_success "已创建环境配置文件: $ENV_FILE"
            log_warning "请根据实际情况修改配置文件"
        else
            log_error "环境配置模板文件不存在"
            exit 1
        fi
    fi
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    
    mkdir -p "$PROJECT_DIR/backups"
    mkdir -p "$PROJECT_DIR/logs"
    
    log_success "目录创建完成"
}

# 启动服务
start_services() {
    log_info "启动 OA 系统服务..."
    
    cd "$PROJECT_DIR"
    
    # 拉取最新镜像
    docker-compose pull
    
    # 启动服务
    docker-compose up -d
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30
    
    # 检查服务状态
    check_services_health
    
    log_success "OA 系统启动完成"
    show_access_info
}

# 停止服务
stop_services() {
    log_info "停止 OA 系统服务..."
    
    cd "$PROJECT_DIR"
    docker-compose down
    
    log_success "OA 系统已停止"
}

# 重启服务
restart_services() {
    log_info "重启 OA 系统服务..."
    
    stop_services
    sleep 5
    start_services
}

# 更新系统
update_system() {
    log_info "更新 OA 系统..."
    
    cd "$PROJECT_DIR"
    
    # 备份数据
    backup_data
    
    # 拉取最新代码
    if [ -d ".git" ]; then
        log_info "拉取最新代码..."
        git pull origin main
    fi
    
    # 重新构建镜像
    log_info "重新构建镜像..."
    docker-compose build --no-cache
    
    # 重启服务
    restart_services
    
    log_success "系统更新完成"
}

# 备份数据
backup_data() {
    log_info "备份系统数据..."
    
    cd "$PROJECT_DIR"
    
    # 创建备份目录
    BACKUP_DIR="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    
    # 备份数据库
    log_info "备份数据库..."
    if docker-compose ps mysql | grep -q "Up"; then
        docker-compose exec -T mysql mysqldump -u root -p"${MYSQL_ROOT_PASSWORD:-root123}" "${MYSQL_DATABASE:-deyoch_oa}" > "$BACKUP_DIR/database.sql"
        gzip "$BACKUP_DIR/database.sql"
        log_success "数据库备份完成: $BACKUP_DIR/database.sql.gz"
    else
        log_warning "MySQL 服务未运行，跳过数据库备份"
    fi
    
    # 备份上传文件
    log_info "备份上传文件..."
    if docker volume ls | grep -q "backend_uploads"; then
        docker run --rm -v deyoch-oa_backend_uploads:/data -v "$PROJECT_DIR/$BACKUP_DIR":/backup alpine tar czf /backup/uploads.tar.gz -C /data .
        log_success "上传文件备份完成: $BACKUP_DIR/uploads.tar.gz"
    else
        log_warning "上传文件卷不存在，跳过文件备份"
    fi
    
    # 备份配置文件
    log_info "备份配置文件..."
    cp "$ENV_FILE" "$BACKUP_DIR/env_backup"
    
    log_success "数据备份完成: $BACKUP_DIR"
}

# 查看日志
show_logs() {
    log_info "显示系统日志..."
    
    cd "$PROJECT_DIR"
    
    if [ $# -gt 2 ]; then
        SERVICE=$3
        docker-compose logs -f "$SERVICE"
    else
        docker-compose logs -f
    fi
}

# 检查服务健康状态
check_services_health() {
    log_info "检查服务健康状态..."
    
    cd "$PROJECT_DIR"
    
    # 检查容器状态
    SERVICES=("mysql" "redis" "backend" "frontend")
    
    for service in "${SERVICES[@]}"; do
        if docker-compose ps "$service" | grep -q "Up"; then
            log_success "$service 服务运行正常"
        else
            log_error "$service 服务运行异常"
            docker-compose logs --tail=20 "$service"
        fi
    done
    
    # 检查健康检查
    sleep 10
    
    # 检查后端健康状态
    if docker-compose exec backend curl -f http://localhost:8080/actuator/health &> /dev/null; then
        log_success "后端服务健康检查通过"
    else
        log_warning "后端服务健康检查失败"
    fi
    
    # 检查前端健康状态
    if docker-compose exec frontend curl -f http://localhost/health &> /dev/null; then
        log_success "前端服务健康检查通过"
    else
        log_warning "前端服务健康检查失败"
    fi
}

# 显示访问信息
show_access_info() {
    log_info "系统访问信息:"
    echo "=================================="
    echo "前端地址: http://localhost"
    echo "后端API: http://localhost:8080"
    echo "默认账号: admin / admin123"
    echo "=================================="
}

# 清理系统
cleanup_system() {
    log_info "清理系统资源..."
    
    cd "$PROJECT_DIR"
    
    # 停止服务
    docker-compose down
    
    # 清理镜像
    docker image prune -f
    
    # 清理容器
    docker container prune -f
    
    # 清理网络
    docker network prune -f
    
    log_success "系统清理完成"
}

# 显示帮助信息
show_help() {
    echo "OA系统部署脚本"
    echo ""
    echo "使用方法:"
    echo "  $0 [环境] [操作] [参数]"
    echo ""
    echo "环境:"
    echo "  dev     开发环境 (默认)"
    echo "  test    测试环境"
    echo "  prod    生产环境"
    echo ""
    echo "操作:"
    echo "  start     启动服务 (默认)"
    echo "  stop      停止服务"
    echo "  restart   重启服务"
    echo "  update    更新系统"
    echo "  backup    备份数据"
    echo "  logs      查看日志"
    echo "  health    检查健康状态"
    echo "  cleanup   清理系统"
    echo "  help      显示帮助"
    echo ""
    echo "示例:"
    echo "  $0 dev start          # 启动开发环境"
    echo "  $0 prod restart       # 重启生产环境"
    echo "  $0 test logs backend  # 查看测试环境后端日志"
    echo ""
}

# 主函数
main() {
    log_info "OA系统部署脚本 - 环境: $ENVIRONMENT, 操作: $ACTION"
    
    case $ACTION in
        start)
            check_dependencies
            check_env_file
            create_directories
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        update)
            check_dependencies
            update_system
            ;;
        backup)
            backup_data
            ;;
        logs)
            show_logs "$@"
            ;;
        health)
            check_services_health
            ;;
        cleanup)
            cleanup_system
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知操作: $ACTION"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"