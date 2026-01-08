# 德优OA办公自动化系统

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-supported-blue.svg)](https://www.docker.com/)

一个基于Spring Boot 3.5.9 + Vue 3 + Element Plus技术栈的现代化企业办公自动化系统，提供完整的用户管理、权限控制、文档管理、消息通知、通讯录管理等功能。

## ✨ 功能特性

### 🔐 用户与权限管理
- 用户管理：用户注册、登录、个人信息管理
- 角色管理：灵活的角色定义和权限分配
- 权限管理：细粒度的功能权限控制
- 部门管理：组织架构管理和部门层级关系

### 📋 办公自动化
- 公告管理：系统公告发布和管理
- 流程审批：自定义审批流程和状态跟踪
- 任务管理：任务分配、进度跟踪和状态管理
- 日程管理：个人和团队日程安排

### 📁 文档管理
- 文件上传下载：支持多种文件格式
- 版本管理：文档版本控制和历史记录
- 分片上传：大文件分片上传和断点续传
- 权限控制：文档访问权限管理

### 📞 通讯录管理
- 员工信息：完整的员工联系信息管理
- 组织架构：可视化的组织架构树
- 搜索筛选：多条件搜索和筛选功能
- 导入导出：Excel批量导入导出

### 💬 消息通知系统
- 实时消息：WebSocket实时消息推送
- 消息分类：系统消息、审批通知、任务通知等
- 消息状态：已读/未读状态管理
- 消息模板：可配置的消息模板

### 🎨 用户体验
- 响应式设计：适配不同屏幕尺寸
- 国际化支持：多语言界面
- 主题切换：明暗主题切换
- 操作友好：直观的用户界面和交互

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.5.9
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + MyBatis Plus
- **缓存**: Redis 7.x
- **WebSocket**: Spring WebSocket
- **文档**: Swagger/OpenAPI 3
- **构建**: Maven 3.9+

### 前端技术栈
- **框架**: Vue 3.x + Composition API
- **UI库**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **语言**: TypeScript/JavaScript

### 部署技术
- **容器化**: Docker + Docker Compose
- **Web服务器**: Nginx
- **监控**: Spring Boot Actuator
- **日志**: Logback + 日志轮转

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选)
- **Docker**: 20.10+ (推荐)

### 方式一：Docker部署（推荐）

1. **克隆项目**
```bash
git clone <repository-url>
cd deyoch-oa
```

2. **配置环境变量**
```bash
# 复制环境变量模板
cp .env.example .env

# 根据需要修改配置
vim .env
```

3. **启动服务**

**Windows:**
```cmd
start.bat
```

**Linux/macOS:**
```bash
chmod +x start.sh
./start.sh
```

**或者使用Docker Compose:**
```bash
docker-compose up -d
```

4. **访问系统**
- 前端地址: http://localhost
- 后端API: http://localhost:8080
- 默认账号: admin / admin123

### 方式二：本地开发部署

#### 后端启动

1. **配置数据库**
```sql
CREATE DATABASE deyoch_oa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **修改配置文件**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/deyoch_oa
    username: your_username
    password: your_password
```

3. **运行数据库脚本**
```bash
# 执行初始化脚本
mysql -u root -p deyoch_oa < deyoch-oa-backend/src/main/resources/db/upgrade_v2.0.sql
```

4. **启动后端服务**
```bash
cd deyoch-oa-backend
mvn spring-boot:run
```

#### 前端启动

1. **安装依赖**
```bash
cd deyoch-oa-frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

3. **构建生产版本**
```bash
npm run build
```

## 📖 使用指南

### 系统管理

1. **用户管理**
   - 登录系统后，管理员可以在"系统管理 > 用户管理"中添加、编辑用户
   - 支持批量导入用户信息
   - 可以设置用户状态和角色权限

2. **角色权限**
   - 在"系统管理 > 角色管理"中创建和管理角色
   - 为角色分配相应的功能权限
   - 支持角色继承和权限组合

3. **部门管理**
   - 在"系统管理 > 部门管理"中管理组织架构
   - 支持多级部门层次结构
   - 可以设置部门负责人和联系信息

### 办公功能

1. **公告管理**
   - 发布系统公告和通知
   - 支持富文本编辑和附件上传
   - 可以设置公告的有效期和可见范围

2. **任务管理**
   - 创建和分配任务
   - 跟踪任务进度和状态
   - 支持任务优先级和截止时间设置

3. **文档管理**
   - 上传和管理各类文档
   - 支持文档版本控制
   - 大文件分片上传功能

### 通讯录功能

1. **查看通讯录**
   - 浏览完整的企业通讯录
   - 按部门、职位等条件筛选
   - 查看员工详细联系信息

2. **组织架构**
   - 可视化的组织架构树
   - 快速定位部门和人员
   - 支持组织架构导出

### 消息通知

1. **接收消息**
   - 实时接收系统消息和通知
   - 消息分类显示和管理
   - 支持消息已读/未读状态

2. **发送消息**
   - 向其他用户发送消息
   - 支持消息模板和批量发送
   - 消息发送状态跟踪

## 🔧 配置说明

### 环境变量配置

```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=deyoch_oa
MYSQL_USER=deyoch
MYSQL_PASSWORD=deyoch123

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400

# 文件上传配置
FILE_UPLOAD_PATH=/app/uploads

# Redis配置（可选）
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=
```

### 应用配置

详细的配置说明请参考：
- [后端配置文档](deyoch-oa-backend/README.md)
- [前端配置文档](deyoch-oa-frontend/README.md)

## 📋 API文档

系统启动后，可以通过以下地址访问API文档：

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 🧪 测试

### 运行测试

```bash
# 后端测试
cd deyoch-oa-backend
mvn test

# 前端测试
cd deyoch-oa-frontend
npm run test
```

### 测试覆盖率

```bash
# 后端测试覆盖率
mvn jacoco:report

# 前端测试覆盖率
npm run test:coverage
```

## 📦 部署

### Docker部署

详细的部署说明请参考 [部署文档](DEPLOYMENT.md)

### 生产环境部署

1. **安全配置**
   - 修改所有默认密码
   - 配置HTTPS证书
   - 设置防火墙规则

2. **性能优化**
   - 调整JVM参数
   - 配置数据库连接池
   - 启用Redis缓存

3. **监控配置**
   - 配置应用监控
   - 设置日志收集
   - 配置告警规则

## 🤝 贡献指南

我们欢迎所有形式的贡献，包括但不限于：

- 🐛 报告Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🔧 提交代码修复
- ⭐ 为项目点星

### 开发流程

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范

- 后端代码遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- 前端代码遵循 [Vue.js Style Guide](https://vuejs.org/style-guide/)
- 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/)

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [Docker](https://www.docker.com/)

## 📞 联系我们

- 项目主页: [GitHub Repository](https://github.com/your-org/deyoch-oa)
- 问题反馈: [GitHub Issues](https://github.com/your-org/deyoch-oa/issues)
- 邮箱: support@deyoch.com

## 📊 项目状态

- ✅ 用户权限管理
- ✅ 办公自动化核心功能
- ✅ 文档管理和版本控制
- ✅ 通讯录管理
- ✅ 消息通知系统
- ✅ 大文件分片上传
- ✅ Docker容器化部署
- 🔄 单元测试完善中
- 📋 性能优化计划中

---

**注意**: 这是一个企业级办公自动化系统，在生产环境使用前请确保进行充分的安全配置和测试。