#!/bin/bash

# OA系统快速启动脚本 (Linux/macOS)

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}    OA系统快速启动脚本${NC}"
echo -e "${BLUE}================================${NC}"

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[错误]${NC} Docker未安装，请先安装Docker"
    exit 1
fi

# 检查Docker Compose是否可用
if ! docker compose version &> /dev/null; then
    echo -e "${RED}[错误]${NC} Docker Compose不可用，请确保Docker服务正在运行"
    exit 1
fi

echo -e "${GREEN}[信息]${NC} Docker环境检查通过"

# 检查环境配置文件
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}[警告]${NC} 环境配置文件不存在，从模板创建..."
    if [ -f ".env.example" ]; then
        cp ".env.example" ".env"
        echo -e "${GREEN}[成功]${NC} 已创建环境配置文件"
        echo -e "${YELLOW}[提示]${NC} 请根据需要修改 .env 文件中的配置"
    else
        echo -e "${RED}[错误]${NC} 环境配置模板文件不存在"
        exit 1
    fi
fi

# 创建必要的目录
mkdir -p backups logs

echo -e "${BLUE}[信息]${NC} 正在启动OA系统服务..."

# 启动服务
if docker compose up -d; then
    echo -e "${GREEN}[成功]${NC} OA系统启动完成"
    echo ""
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}        访问信息${NC}"
    echo -e "${BLUE}================================${NC}"
    echo "前端地址: http://localhost"
    echo "后端API: http://localhost:8080"
    echo "默认账号: admin / admin123"
    echo -e "${BLUE}================================${NC}"
    echo ""
    
    # 等待服务启动
    echo -e "${BLUE}[信息]${NC} 等待服务完全启动..."
    sleep 30
    
    # 显示服务状态
    echo -e "${BLUE}[信息]${NC} 服务状态:"
    docker compose ps
    
    # 检查健康状态
    echo ""
    echo -e "${BLUE}[信息]${NC} 检查服务健康状态..."
    
    # 检查后端健康状态
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        echo -e "${GREEN}[成功]${NC} 后端服务健康检查通过"
    else
        echo -e "${YELLOW}[警告]${NC} 后端服务可能还在启动中，请稍后再试"
    fi
    
    # 检查前端健康状态
    if curl -f http://localhost/health &> /dev/null; then
        echo -e "${GREEN}[成功]${NC} 前端服务健康检查通过"
    else
        echo -e "${YELLOW}[警告]${NC} 前端服务可能还在启动中，请稍后再试"
    fi
    
else
    echo -e "${RED}[错误]${NC} 服务启动失败"
    echo "查看错误日志:"
    docker compose logs
    exit 1
fi

echo ""
echo -e "${GREEN}OA系统启动完成！${NC}"
echo "使用 'docker compose logs -f' 查看实时日志"
echo "使用 'docker compose down' 停止服务"