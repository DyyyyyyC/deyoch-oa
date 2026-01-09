/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80043
 Source Host           : 192.168.28.197:3306
 Source Schema         : deyoch_oa

 Target Server Type    : MySQL
 Target Server Version : 80043
 File Encoding         : 65001

 Date: 09/01/2026 10:25:14
*/

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `deyoch_oa` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `deyoch_oa`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for deyoch_announcement
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_announcement`;
CREATE TABLE `deyoch_announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `user_id` bigint NOT NULL COMMENT '发布人ID',
  `publish_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤销',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_publish_time`(`publish_time`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_announcement_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_announcement
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_db_version
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_db_version`;
CREATE TABLE `deyoch_db_version`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `upgrade_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据库版本记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_db_version
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_dept
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_dept`;
CREATE TABLE `deyoch_dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级部门ID',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门编码',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '部门负责人ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '部门邮箱',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dept_code`(`dept_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id`) USING BTREE,
  CONSTRAINT `fk_dept_leader` FOREIGN KEY (`leader_id`) REFERENCES `deyoch_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_dept
-- ----------------------------
INSERT INTO `deyoch_dept` VALUES (1, '总公司', 0, '001', NULL, '13800138000', 'zhangsan@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (2, '技术部', 1, '001001', NULL, '13800138001', 'lisi@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (3, '市场部', 1, '001002', NULL, '13800138002', 'wangwu@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (4, '财务部', 1, '001003', NULL, '13800138003', 'zhaoliu@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');

-- ----------------------------
-- Table structure for deyoch_document
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_document`;
CREATE TABLE `deyoch_document`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件保存路径',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型',
  `user_id` bigint NOT NULL COMMENT '上传人ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属部门ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1.0' COMMENT '版本号',
  `is_current` tinyint NULL DEFAULT 1 COMMENT '是否当前版本',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父文档ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_file_type`(`file_type`) USING BTREE,
  CONSTRAINT `fk_document_dept` FOREIGN KEY (`dept_id`) REFERENCES `deyoch_dept` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_document_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_document
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_message
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_message`;
CREATE TABLE `deyoch_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `type` tinyint NOT NULL DEFAULT 1,
  `priority` tinyint NOT NULL DEFAULT 1,
  `sender_id` bigint NULL DEFAULT NULL,
  `receiver_id` bigint NOT NULL,
  `is_read` tinyint NOT NULL DEFAULT 0,
  `read_time` datetime NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_message
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_message_template
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_message_template`;
CREATE TABLE `deyoch_message_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title_template` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `variables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_message_template
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_chunk_upload
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_chunk_upload`;
CREATE TABLE `deyoch_chunk_upload`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` bigint NOT NULL,
  `total_chunks` int NOT NULL,
  `chunk_size` bigint NOT NULL DEFAULT 2097152,
  `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_id`(`upload_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分片上传记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_chunk_upload
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_chunk_info
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_chunk_info`;
CREATE TABLE `deyoch_chunk_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `chunk_index` int NOT NULL,
  `chunk_size` bigint NOT NULL,
  `chunk_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `chunk_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_chunk`(`upload_id`, `chunk_index`) USING BTREE,
  INDEX `idx_upload_id`(`upload_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分片信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_chunk_info
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_document_version
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_document_version`;
CREATE TABLE `deyoch_document_version`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_id` bigint NOT NULL,
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `change_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `is_current` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_document_id`(`document_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_current`(`is_current`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_document_version
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_permission
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_permission`;
CREATE TABLE `deyoch_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `perm_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `perm_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `perm_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型：menu-菜单，button-按钮',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级ID',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_permission
-- ----------------------------
INSERT INTO `deyoch_permission` VALUES (1, '系统管理', 'sys:manage', 'menu', 0, '/system', 'Layout', 'Setting', 1, 1, '2025-12-25 15:00:51', '2026-01-04 14:29:16');
INSERT INTO `deyoch_permission` VALUES (2, '用户管理', 'sys:user:manage', 'menu', 1, '/system/user', 'system/user/index', 'User', 1, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (3, '角色管理', 'sys:role:manage', 'menu', 1, '/system/role', 'system/role/index', 'Avatar', 2, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (4, '权限管理', 'sys:perm:manage', 'menu', 1, '/system/permission', 'system/permission/index', 'Lock', 3, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (5, '部门管理', 'sys:dept:manage', 'menu', 1, '/system/dept', 'system/dept/index', 'OfficeBuilding', 4, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (7, '公告管理', 'oa:announcement:manage', 'menu', 0, '/announcement', 'announcement/index', 'Bell', 2, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (8, '流程管理', 'oa:process:manage', 'menu', 0, '/process', 'Layout', 'View', 3, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (9, '任务管理', 'oa:task:manage', 'menu', 0, '/task', 'task/index', 'List', 4, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (10, '日程管理', 'oa:schedule:manage', 'menu', 0, '/schedule', 'schedule/index', 'Calendar', 5, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (11, '文档管理', 'oa:document:manage', 'menu', 0, '/document', 'document/index', 'Document', 6, 1, '2025-12-25 15:00:51', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (12, '通讯录', 'oa:contact:view', 'menu', 0, '/contact', 'contact/index', 'AddressBook', 7, 1, '2026-01-09 10:25:14', '2026-01-09 10:25:14');
INSERT INTO `deyoch_permission` VALUES (13, '消息中心', 'oa:message:view', 'menu', 0, '/message', 'message/index', 'ChatDotRound', 8, 1, '2026-01-09 10:25:14', '2026-01-09 10:25:14');

-- ----------------------------
-- Table structure for deyoch_process
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_process`;
CREATE TABLE `deyoch_process`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程名称',
  `process_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程标识',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '流程描述',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_process_key`(`process_key`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_process_creator` FOREIGN KEY (`creator_id`) REFERENCES `deyoch_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_process
-- ----------------------------
INSERT INTO `deyoch_process` VALUES (1, '请假流程', 'leave', '员工请假审批流程', 1, 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process` VALUES (2, '报销流程', 'reimbursement', '费用报销审批流程', 1, 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process` VALUES (3, '出差流程', 'business_trip', '员工出差审批流程', 1, 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process` VALUES (4, '采购流程', 'purchase', '物资采购审批流程', 1, 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process` VALUES (5, '加班流程', 'overtime', '员工加班审批流程', 1, 1, '2025-12-31 10:02:42', '2025-12-31 10:18:10');

-- ----------------------------
-- Table structure for deyoch_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_process_instance`;
CREATE TABLE `deyoch_process_instance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `process_id` bigint NOT NULL COMMENT '流程ID',
  `instance_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实例名称',
  `user_id` bigint NOT NULL COMMENT '发起人ID',
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-运行中，1-已完成，2-已取消',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_process_id`(`process_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_process_instance_process` FOREIGN KEY (`process_id`) REFERENCES `deyoch_process` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_process_instance_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程实例表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_role
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_role`;
CREATE TABLE `deyoch_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_role
-- ----------------------------
INSERT INTO `deyoch_role` VALUES (1, '管理员', 'admin', '系统管理员', '2025-12-25 15:00:51', '2026-01-08 15:55:06');
INSERT INTO `deyoch_role` VALUES (2, '普通用户', 'user', '普通用户', '2025-12-25 15:00:51', '2026-01-08 15:14:07');
INSERT INTO `deyoch_role` VALUES (4, '部门经理', 'manager', '部门经理', '2025-12-25 15:00:51', '2026-01-08 15:14:07');

-- ----------------------------
-- Table structure for deyoch_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_role_permission`;
CREATE TABLE `deyoch_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id`, `perm_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_perm_id`(`perm_id`) USING BTREE,
  CONSTRAINT `fk_role_perm_perm` FOREIGN KEY (`perm_id`) REFERENCES `deyoch_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`role_id`) REFERENCES `deyoch_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_role_permission
-- ----------------------------
INSERT INTO `deyoch_role_permission` VALUES (15, 1, 1);
INSERT INTO `deyoch_role_permission` VALUES (20, 1, 2);
INSERT INTO `deyoch_role_permission` VALUES (21, 1, 3);
INSERT INTO `deyoch_role_permission` VALUES (22, 1, 4);
INSERT INTO `deyoch_role_permission` VALUES (23, 1, 5);
INSERT INTO `deyoch_role_permission` VALUES (16, 1, 7);
INSERT INTO `deyoch_role_permission` VALUES (17, 1, 8);
INSERT INTO `deyoch_role_permission` VALUES (18, 1, 9);
INSERT INTO `deyoch_role_permission` VALUES (19, 1, 10);
INSERT INTO `deyoch_role_permission` VALUES (26, 1, 11);
INSERT INTO `deyoch_role_permission` VALUES (27, 1, 12);
INSERT INTO `deyoch_role_permission` VALUES (28, 1, 13);

-- ----------------------------
-- Table structure for deyoch_schedule
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_schedule`;
CREATE TABLE `deyoch_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日程标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '日程内容',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-取消，1-正常',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_start_time`(`start_time`) USING BTREE,
  INDEX `idx_end_time`(`end_time`) USING BTREE,
  CONSTRAINT `fk_schedule_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_schedule
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_task
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_task`;
CREATE TABLE `deyoch_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '任务内容',
  `assignee_id` bigint NOT NULL COMMENT '负责人ID',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `priority` tinyint NULL DEFAULT 2 COMMENT '优先级：1-低，2-中，3-高',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已完成，3-已取消',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_assignee_id`(`assignee_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_priority`(`priority`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_task_creator` FOREIGN KEY (`creator_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_task
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_user
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_user`;
CREATE TABLE `deyoch_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职位',
  `office_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '办公地点',
  `extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分机号',
  `employee_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工号',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_real_name`(`real_name`) USING BTREE,
  INDEX `idx_position`(`position`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_user
-- ----------------------------
INSERT INTO `deyoch_user` VALUES (1, 'admin', '$2a$10$sz.gBi9FvBLCshGN2M5roePKCuWcmUFIl19rR.0QZL2DOl.aqFMQy', '管理员', NULL, 'admin@deyoch.com', '13800138000', NULL, NULL, NULL, NULL, '', 1, 1, 1, '2025-12-25 15:00:51', '2025-12-31 16:53:25');

-- ----------------------------
-- View structure for v_contact_directory
-- ----------------------------
DROP VIEW IF EXISTS `v_contact_directory`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_contact_directory` AS select `u`.`id` AS `id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`u`.`phone` AS `phone`,`u`.`position` AS `position`,`u`.`office_location` AS `office_location`,`u`.`extension` AS `extension`,`u`.`employee_id` AS `employee_id`,`u`.`status` AS `status`,`d`.`dept_name` AS `dept_name`,`r`.`role_name` AS `role_name` from ((`deyoch_user` `u` left join `deyoch_dept` `d` on((`u`.`dept_id` = `d`.`id`))) left join `deyoch_role` `r` on((`u`.`role_id` = `r`.`id`))) where (`u`.`status` = 1);

SET FOREIGN_KEY_CHECKS = 1;
