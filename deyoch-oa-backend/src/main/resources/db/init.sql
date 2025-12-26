-- OA系统数据库初始化脚本
-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `deyoch_oa` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `deyoch_oa`;

-- 1. 用户表
CREATE TABLE `deyoch_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `avatar` varchar(200) DEFAULT NULL COMMENT '头像',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `deyoch_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 3. 权限表
CREATE TABLE `deyoch_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `perm_name` varchar(50) NOT NULL COMMENT '权限名称',
  `perm_code` varchar(50) NOT NULL COMMENT '权限编码',
  `perm_type` varchar(20) NOT NULL COMMENT '权限类型：menu-菜单，button-按钮',
  `parent_id` bigint DEFAULT NULL COMMENT '父级ID',
  `path` varchar(100) DEFAULT NULL COMMENT '路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 4. 角色权限关联表
CREATE TABLE `deyoch_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`,`perm_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_perm_id` (`perm_id`),
  CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`role_id`) REFERENCES `deyoch_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_perm_perm` FOREIGN KEY (`perm_id`) REFERENCES `deyoch_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 5. 部门表
CREATE TABLE `deyoch_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_name` varchar(50) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父级部门ID',
  `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编码',
  `leader` varchar(50) DEFAULT NULL COMMENT '部门负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '部门电话',
  `email` varchar(100) DEFAULT NULL COMMENT '部门邮箱',
  `sort` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 6. 公告表
CREATE TABLE `deyoch_announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `publisher` varchar(50) NOT NULL COMMENT '发布人',
  `publish_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤销',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 7. 流程表
CREATE TABLE `deyoch_process` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_name` varchar(100) NOT NULL COMMENT '流程名称',
  `process_key` varchar(50) NOT NULL COMMENT '流程标识',
  `description` varchar(200) DEFAULT NULL COMMENT '流程描述',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_process_key` (`process_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程表';

-- 8. 流程实例表
CREATE TABLE `deyoch_process_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_id` bigint NOT NULL COMMENT '流程ID',
  `instance_name` varchar(100) NOT NULL COMMENT '实例名称',
  `initiator` varchar(50) NOT NULL COMMENT '发起人',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-运行中，1-已完成，2-已取消',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_process_id` (`process_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_process_instance_process` FOREIGN KEY (`process_id`) REFERENCES `deyoch_process` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程实例表';

-- 9. 任务表
CREATE TABLE `deyoch_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '任务标题',
  `content` text DEFAULT NULL COMMENT '任务内容',
  `assignee` varchar(50) NOT NULL COMMENT '负责人',
  `priority` tinyint DEFAULT 2 COMMENT '优先级：1-低，2-中，3-高',
  `status` tinyint DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已完成，3-已取消',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_assignee` (`assignee`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 10. 日程表
CREATE TABLE `deyoch_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '日程标题',
  `content` text DEFAULT NULL COMMENT '日程内容',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `location` varchar(100) DEFAULT NULL COMMENT '地点',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-取消，1-正常',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  CONSTRAINT `fk_schedule_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';

-- 11. 文档表
CREATE TABLE `deyoch_document` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) NOT NULL COMMENT '文档标题',
  `content` text DEFAULT NULL COMMENT '文档内容',
  `file_path` varchar(200) DEFAULT NULL COMMENT '文件路径',
  `file_name` varchar(100) DEFAULT NULL COMMENT '文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `uploader` varchar(50) NOT NULL COMMENT '上传人',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_uploader` (`uploader`),
  KEY `idx_file_type` (`file_type`),
  CONSTRAINT `fk_document_dept` FOREIGN KEY (`dept_id`) REFERENCES `deyoch_dept` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表';

-- 插入初始数据

-- 插入角色数据
INSERT INTO `deyoch_role` (`role_name`, `role_code`, `description`) VALUES 
('管理员', 'admin', '系统管理员'),
('普通用户', 'user', '普通用户'),
('部门经理', 'manager', '部门经理');

-- 插入权限数据
INSERT INTO `deyoch_permission` (`perm_name`, `perm_code`, `perm_type`, `parent_id`, `path`, `component`, `icon`, `sort`) VALUES 
('系统管理', 'sys:manage', 'menu', 0, '/system', 'Layout', 'Setting', 1),
('用户管理', 'sys:user:manage', 'menu', 1, '/system/user', 'system/user/index', 'User', 1),
('角色管理', 'sys:role:manage', 'menu', 1, '/system/role', 'system/role/index', 'Avatar', 2),
('权限管理', 'sys:perm:manage', 'menu', 1, '/system/perm', 'system/perm/index', 'Lock', 3),
('部门管理', 'sys:dept:manage', 'menu', 1, '/system/dept', 'system/dept/index', 'OfficeBuilding', 4),
('办公管理', 'oa:manage', 'menu', 0, '/oa', 'Layout', 'DocumentCopy', 2),
('公告管理', 'oa:announcement:manage', 'menu', 6, '/oa/announcement', 'oa/announcement/index', 'Bell', 1),
('流程管理', 'oa:process:manage', 'menu', 6, '/oa/process', 'oa/process/index', 'View', 2),
('任务管理', 'oa:task:manage', 'menu', 6, '/oa/task', 'oa/task/index', 'List', 3),
('日程管理', 'oa:schedule:manage', 'menu', 6, '/oa/schedule', 'oa/schedule/index', 'Calendar', 4),
('文档管理', 'oa:document:manage', 'menu', 6, '/oa/document', 'oa/document/index', 'Document', 5);

-- 插入部门数据
INSERT INTO `deyoch_dept` (`dept_name`, `parent_id`, `dept_code`, `leader`, `phone`, `email`) VALUES 
('总公司', 0, '001', '张三', '13800138000', 'zhangsan@deyoch.com'),
('技术部', 1, '001001', '李四', '13800138001', 'lisi@deyoch.com'),
('市场部', 1, '001002', '王五', '13800138002', 'wangwu@deyoch.com'),
('财务部', 1, '001003', '赵六', '13800138003', 'zhaoliu@deyoch.com');

-- 插入初始管理员用户
-- 密码：admin123（已加密，实际使用时需替换为真实加密值）
INSERT INTO `deyoch_user` (`username`, `password`, `nickname`, `email`, `phone`, `avatar`, `dept_id`, `role_id`, `status`) VALUES 
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGO7o199u291q2Gz0p8c8Q0M6Z2O3O', '管理员', 'admin@deyoch.com', '13800138000', '/avatar/admin.jpg', 1, 1, 1);
