@echo off
REM OA系统快速启动脚本 (Windows)

echo ================================
echo    OA系统快速启动脚本
echo ================================

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker未安装，请先安装Docker Desktop
    pause
    exit /b 1
)

REM 检查Docker Compose是否可用
docker compose version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker Compose不可用，请确保Docker Desktop正在运行
    pause
    exit /b 1
)

echo [信息] Docker环境检查通过

REM 检查环境配置文件
if not exist ".env" (
    echo [警告] 环境配置文件不存在，从模板创建...
    if exist ".env.example" (
        copy ".env.example" ".env"
        echo [成功] 已创建环境配置文件
        echo [提示] 请根据需要修改 .env 文件中的配置
    ) else (
        echo [错误] 环境配置模板文件不存在
        pause
        exit /b 1
    )
)

REM 创建必要的目录
if not exist "backups" mkdir backups
if not exist "logs" mkdir logs

echo [信息] 正在启动OA系统服务...

REM 启动服务
docker compose up -d

if %errorlevel% equ 0 (
    echo [成功] OA系统启动完成
    echo.
    echo ================================
    echo        访问信息
    echo ================================
    echo 前端地址: http://localhost
    echo 后端API: http://localhost:8080
    echo 默认账号: admin / admin123
    echo ================================
    echo.
    echo 按任意键查看服务状态...
    pause >nul
    docker compose ps
) else (
    echo [错误] 服务启动失败
    echo 查看错误日志:
    docker compose logs
)

echo.
echo 按任意键退出...
pause >nul