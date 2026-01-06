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

 Date: 04/01/2026 16:36:07
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_announcement
-- ----------------------------
INSERT INTO `deyoch_announcement` VALUES (1, '元旦放假通知', '根据国家法定节假日安排，2026年元旦放假时间为2026年1月1日至1月3日，共3天。', 2, '2025-12-25 10:00:00', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_announcement` VALUES (2, '系统升级通知', 'OA系统将于2025年12月31日22:00-24:00进行系统升级，期间系统将暂停服务，请提前做好准备。', 3, '2025-12-28 14:30:00', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_announcement` VALUES (3, '年度优秀员工评选', '2025年度优秀员工评选活动正式启动，请各部门推荐候选人。', 4, '2025-12-20 09:00:00', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_announcement` VALUES (5, '公司年会通知', '2026年公司年会将于2026年1月15日举行，请各位员工安排好时间。', 2, '2025-12-26 16:00:00', 1, '2025-12-31 10:02:42', '2025-12-31 16:59:44');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_dept
-- ----------------------------
INSERT INTO `deyoch_dept` VALUES (1, '总公司', 0, '001', 2, '13800138000', 'zhangsan@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (2, '技术部', 1, '001001', 3, '13800138001', 'lisi@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (3, '市场部', 1, '001002', 4, '13800138002', 'wangwu@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (4, '财务部', 1, '001003', 2, '13800138003', 'zhaoliu@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');

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
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_file_type`(`file_type`) USING BTREE,
  CONSTRAINT `fk_document_dept` FOREIGN KEY (`dept_id`) REFERENCES `deyoch_dept` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_document_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='文档表';

-- ----------------------------
-- Records of deyoch_document
-- ----------------------------
INSERT INTO `deyoch_document` VALUES (1, 'OA系统需求文档.docx', '/upload/documents/req.docx', 1024000, 'docx', 2, 2, 1, '2025-12-31 10:02:42', '2025-12-31 14:37:15');
INSERT INTO `deyoch_document` VALUES (2, '数据库设计文档.docx', '/upload/documents/db.docx', 512000, 'docx', 3, 2, 1, '2025-12-31 10:02:42', '2025-12-31 14:37:19');
INSERT INTO `deyoch_document` VALUES (3, '用户手册.pdf', '/upload/documents/user_manual.pdf', 2048000, 'pdf', 4, 1, 1, '2025-12-31 10:02:42', '2025-12-31 14:37:25');
INSERT INTO `deyoch_document` VALUES (4, '开发规范.docx', '/upload/documents/dev_spec.docx', 768000, 'docx', 2, 2, 1, '2025-12-31 10:02:42', '2025-12-31 14:37:28');
INSERT INTO `deyoch_document` VALUES (5, '测试报告.pdf', '/upload/documents/test_report.pdf', 1536000, 'pdf', 2, 2, 1, '2025-12-31 10:02:42', '2025-12-31 14:37:30');

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
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_permission_creator` FOREIGN KEY (`creator_id`) REFERENCES `deyoch_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

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
INSERT INTO `deyoch_permission` VALUES (12, '流程定义', 'oa:process:definition', 'menu', 8, '/process/definition', 'process/definition/index', 'FileText', 1, 1, 1, '2026-01-04 16:34:39', '2026-01-04 16:34:39');
INSERT INTO `deyoch_permission` VALUES (13, '流程实例', 'oa:process:instance', 'menu', 8, '/process/instance', 'process/instance/index', 'PlayCircle', 2, 1, 1, '2026-01-04 16:34:39', '2026-01-04 16:34:39');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_process_instance
-- ----------------------------
INSERT INTO `deyoch_process_instance` VALUES (1, 1, '张三请假申请', 2, '2025-12-25 09:00:00', '2025-12-26 10:00:00', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process_instance` VALUES (2, 2, '李四报销申请', 3, '2025-12-27 14:00:00', NULL, 0, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process_instance` VALUES (3, 3, '王五出差申请', 4, '2025-12-28 10:00:00', NULL, 0, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process_instance` VALUES (4, 1, '赵六请假申请', 2, '2025-12-29 08:30:00', NULL, 0, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_process_instance` VALUES (5, 4, '钱七采购申请', 2, '2025-12-30 11:00:00', '2025-12-30 15:00:00', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');

-- ----------------------------
-- Table structure for deyoch_role
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_role`;
CREATE TABLE `deyoch_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色描述',
  `creator_id` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE,
  INDEX `idx_creator_id`(`creator_id`) USING BTREE,
  CONSTRAINT `fk_role_creator` FOREIGN KEY (`creator_id`) REFERENCES `deyoch_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_role
-- ----------------------------
INSERT INTO `deyoch_role` VALUES (1, '管理员', 'admin', '系统管理员', 1, '2025-12-25 15:00:51', '2026-01-04 15:36:56');
INSERT INTO `deyoch_role` VALUES (2, '普通用户', 'user', '普通用户', 1, '2025-12-25 15:00:51', '2026-01-04 15:36:56');
INSERT INTO `deyoch_role` VALUES (3, '部门经理', 'manager', '部门经理', 1, '2025-12-25 15:00:51', '2026-01-04 15:36:56');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

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
INSERT INTO `deyoch_role_permission` VALUES (24, 1, 12);
INSERT INTO `deyoch_role_permission` VALUES (25, 1, 13);

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_schedule
-- ----------------------------
INSERT INTO `deyoch_schedule` VALUES (1, '项目评审会议', 'OA系统项目评审', 2, '2025-12-31 10:00:00', '2025-12-31 12:00:00', '会议室A', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_schedule` VALUES (3, '部门周会', '技术部周会', 3, '2025-12-31 15:00:00', '2025-12-31 16:30:00', '会议室B', 1, '2025-12-31 10:02:42', '2025-12-31 10:02:42');
INSERT INTO `deyoch_schedule` VALUES (4, '112', '', 2, '2026-01-04 00:00:00', '2026-01-05 00:00:00', '', 0, '2026-01-04 15:09:06', '2026-01-04 15:09:16');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_task
-- ----------------------------
INSERT INTO `deyoch_task` VALUES (1, '完成项目设计', '完成OA系统的项目设计文档', 2, 1, 3, 1, '2025-12-25 09:00:00', '2025-12-31 18:00:00', '2025-12-31 10:02:42', '2025-12-31 17:03:24');
INSERT INTO `deyoch_task` VALUES (2, '修复系统bug', '修复用户管理模块的bug', 2, 1, 2, 1, '2025-12-26 10:00:00', '2025-12-28 17:00:00', '2025-12-31 10:02:42', '2025-12-31 17:03:27');
INSERT INTO `deyoch_task` VALUES (3, '编写测试用例', '编写系统测试用例', 2, 1, 2, 0, '2025-12-29 09:00:00', '2025-12-31 18:00:00', '2025-12-31 10:02:42', '2025-12-31 17:03:27');
INSERT INTO `deyoch_task` VALUES (4, '部署生产环境', '部署OA系统到生产环境', 2, 1, 3, 2, '2025-12-20 08:00:00', '2025-12-22 17:00:00', '2025-12-31 10:02:42', '2025-12-31 17:03:27');
INSERT INTO `deyoch_task` VALUES (5, '需求分析', '分析新需求', 2, 1, 2, 0, '2025-12-30 09:00:00', '2025-01-03 18:00:00', '2025-12-31 10:02:42', '2025-12-31 17:03:27');

-- ----------------------------
-- Table structure for deyoch_user
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_user`;
CREATE TABLE `deyoch_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of deyoch_user
-- ----------------------------
INSERT INTO `deyoch_user` VALUES (1, 'admin', '$2a$10$sz.gBi9FvBLCshGN2M5roePKCuWcmUFIl19rR.0QZL2DOl.aqFMQy', '管理员', 'admin@deyoch.com', '13800138000', '', 1, 1, 1, '2025-12-25 15:00:51', '2025-12-31 16:53:25');
INSERT INTO `deyoch_user` VALUES (2, 'zhangsan', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '张三', 'user1@deyoch.com', '13800138001', '', 2, 2, 1, '2025-12-31 10:02:42', '2025-12-31 16:54:20');
INSERT INTO `deyoch_user` VALUES (3, 'lisi', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '李四', 'user2@deyoch.com', '13800138002', '', 3, 3, 0, '2025-12-31 10:02:44', '2026-01-04 14:34:56');
INSERT INTO `deyoch_user` VALUES (4, 'lisi3', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '李四', 'user2@deyoch.com', '13800138002', '', 3, 2, 1, '2025-12-31 10:02:44', '2025-12-31 16:54:20');

SET FOREIGN_KEY_CHECKS = 1;
