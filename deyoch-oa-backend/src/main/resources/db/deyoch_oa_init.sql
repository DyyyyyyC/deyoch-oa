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

 Date: 09/01/2026 18:32:31
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
INSERT INTO `deyoch_announcement` VALUES (1, '关于2026年春节放假安排的通知', '各位同事：\n\n根据国家法定节假日安排，现将2026年春节放假安排通知如下：\n\n放假时间：2026年1月28日（除夕）至2026年2月3日（正月初七），共7天。\n\n请各部门提前做好工作安排，确保节前工作顺利完成。\n\n祝大家新年快乐！\n\n人事部\n2026年1月9日', 1, '2026-01-09 09:00:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_announcement` VALUES (2, '新员工入职培训通知', '各部门新入职员工：\n\n为帮助新员工快速融入公司，人事部将组织新员工入职培训。\n\n培训时间：2026年1月15日 上午9:00-12:00\n培训地点：会议室A\n培训内容：公司介绍、规章制度、企业文化等\n\n请相关人员准时参加。', 16, '2026-01-08 14:30:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_announcement` VALUES (3, '系统升级维护通知', '各位用户：\n\nOA系统将于本周六（1月11日）晚上22:00-24:00进行系统升级维护。\n\n维护期间系统将暂停服务，请大家提前做好相关工作安排。\n\n如有紧急情况，请联系IT部门。\n\n技术部', 2, '2026-01-09 16:00:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_announcement` VALUES (4, '月度绩效考核提醒', '各部门负责人：\n\n请于本月底前完成员工月度绩效考核工作。\n\n考核内容包括：工作完成情况、团队协作、创新能力等。\n\n考核结果将作为年终评优和薪资调整的重要依据。', 16, '2026-01-07 10:00:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_announcement` VALUES (5, '办公用品采购申请流程优化', '各部门：\n\n为提高办公效率，现对办公用品采购申请流程进行优化：\n\n1. 统一使用OA系统提交申请\n2. 部门负责人审批后提交行政部\n3. 行政部统一采购配送\n\n新流程将于下周一正式启用。', 19, '2026-01-06 15:20:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');

-- ----------------------------
-- Table structure for deyoch_chunk_info
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_chunk_info`;
CREATE TABLE `deyoch_chunk_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上传ID',
  `chunk_index` int NOT NULL COMMENT '分片索引',
  `chunk_size` bigint NOT NULL COMMENT '分片大小',
  `chunk_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分片MD5值',
  `chunk_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分片保存路径',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_chunk`(`upload_id`, `chunk_index`) USING BTREE,
  INDEX `idx_upload_id`(`upload_id`) USING BTREE,
  CONSTRAINT `fk_chunk_info_upload` FOREIGN KEY (`upload_id`) REFERENCES `deyoch_chunk_upload` (`upload_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分片信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_chunk_info
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_chunk_upload
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_chunk_upload`;
CREATE TABLE `deyoch_chunk_upload`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '上传ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_size` bigint NOT NULL COMMENT '文件大小',
  `total_chunks` int NOT NULL COMMENT '总分片数',
  `chunk_size` bigint NOT NULL DEFAULT 2097152 COMMENT '分片大小',
  `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `user_id` bigint NOT NULL COMMENT '上传用户ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-上传中，1-已完成，2-已取消',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件保存路径',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_id`(`upload_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  CONSTRAINT `fk_chunk_upload_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分片上传记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_chunk_upload
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_db_version
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_db_version`;
CREATE TABLE `deyoch_db_version`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版本号',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '版本描述',
  `upgrade_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '升级时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_version`(`version`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据库版本记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_db_version
-- ----------------------------
INSERT INTO `deyoch_db_version` VALUES (1, '2.0', '初始化测试数据', '2026-01-09 14:40:55');
INSERT INTO `deyoch_db_version` VALUES (2, '2.1', '新增通讯录和消息中心功能', '2026-01-09 14:40:55');
INSERT INTO `deyoch_db_version` VALUES (3, '2.2', '完善数据库结构和测试数据', '2026-01-09 14:40:55');

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
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_dept
-- ----------------------------
INSERT INTO `deyoch_dept` VALUES (1, '总公司', 0, '001', NULL, '13800138000', 'zhangsan@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2025-12-25 15:00:51');
INSERT INTO `deyoch_dept` VALUES (2, '技术部', 1, '001001', 2, '13800138001', 'lisi@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (3, '市场部', 1, '001002', 9, '13800138002', 'wangwu@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (4, '财务部', 1, '001003', 13, '13800138003', 'zhaoliu@deyoch.com', 0, 1, '2025-12-25 15:00:51', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (5, '人事部', 1, '001004', 16, '13800138004', 'hr@deyoch.com', 5, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (6, '行政部', 1, '001005', 19, '13800138005', 'admin@deyoch.com', 6, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (7, '前端开发组', 2, '001001001', 3, '13800138011', 'frontend@deyoch.com', 1, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (8, '后端开发组', 2, '001001002', 4, '13800138012', 'backend@deyoch.com', 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (9, '测试组', 2, '001001003', 7, '13800138013', 'qa@deyoch.com', 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (10, '销售一部', 3, '001002001', 10, '13800138021', 'sales1@deyoch.com', 1, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_dept` VALUES (11, '销售二部', 3, '001002002', 12, '13800138022', 'sales2@deyoch.com', 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');

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
INSERT INTO `deyoch_document` VALUES (1, '公司组织架构图.pdf', '/documents/2026/01/org_chart.pdf', 2048576, 'pdf', 16, 5, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.0', 1, NULL);
INSERT INTO `deyoch_document` VALUES (2, '员工手册.docx', '/documents/2026/01/employee_handbook.docx', 1536000, 'docx', 16, 5, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '2.1', 1, NULL);
INSERT INTO `deyoch_document` VALUES (3, '技术规范文档.md', '/documents/2026/01/tech_standards.md', 512000, 'md', 2, 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.5', 1, NULL);
INSERT INTO `deyoch_document` VALUES (4, '项目需求文档.docx', '/documents/2026/01/project_requirements.docx', 3072000, 'docx', 3, 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.0', 1, NULL);
INSERT INTO `deyoch_document` VALUES (5, '测试用例模板.xlsx', '/documents/2026/01/test_case_template.xlsx', 256000, 'xlsx', 7, 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.2', 1, NULL);
INSERT INTO `deyoch_document` VALUES (6, '销售培训资料.pptx', '/documents/2026/01/sales_training.pptx', 8192000, 'pptx', 9, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.0', 1, NULL);
INSERT INTO `deyoch_document` VALUES (7, '财务制度文件.pdf', '/documents/2026/01/finance_policy.pdf', 1024000, 'pdf', 13, 4, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '3.0', 1, NULL);
INSERT INTO `deyoch_document` VALUES (8, '招聘流程图.png', '/documents/2026/01/recruitment_flow.png', 128000, 'png', 17, 5, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.0', 1, NULL);
INSERT INTO `deyoch_document` VALUES (9, '办公室管理制度.docx', '/documents/2026/01/office_management.docx', 768000, 'docx', 19, 6, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '2.0', 1, NULL);
INSERT INTO `deyoch_document` VALUES (10, 'API接口文档.json', '/documents/2026/01/api_docs.json', 384000, 'json', 4, 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55', '1.3', 1, NULL);

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
  `is_current` tinyint NOT NULL DEFAULT 0 COMMENT '是否当前版本：0-否，1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_document_id`(`document_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_current`(`is_current`) USING BTREE,
  CONSTRAINT `fk_document_version_document` FOREIGN KEY (`document_id`) REFERENCES `deyoch_document` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_document_version_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档版本表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_document_version
-- ----------------------------

-- ----------------------------
-- Table structure for deyoch_message
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_message`;
CREATE TABLE `deyoch_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1-系统消息，2-审批通知，3-任务通知，4-公告通知',
  `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级：1-普通，2-重要，3-紧急',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联类型：task,process,announcement,document',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_related`(`related_type`, `related_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `deyoch_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_message
-- ----------------------------
INSERT INTO `deyoch_message` VALUES (1, '欢迎使用德约OA系统', '欢迎您使用德约OA系统！\n\n系统为您提供了丰富的办公功能，包括任务管理、日程安排、文档管理、消息通知等。\n\n如有使用问题，请联系技术支持。', 1, 1, NULL, 2, 1, '2026-01-09 09:30:00', NULL, NULL, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (2, '系统升级完成通知', 'OA系统升级已完成，新增功能：\n\n1. 优化了用户界面\n2. 增加了消息中心\n3. 完善了通讯录功能\n\n请体验新功能并反馈使用感受。', 1, 2, NULL, 3, 0, NULL, NULL, NULL, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (3, '请假申请待审批', '您有一个请假申请需要审批：\n\n申请人：李四\n请假类型：年假\n请假时间：2026-01-20 至 2026-01-22\n请假原因：回家探亲\n\n请及时处理。', 2, 2, 3, 2, 0, NULL, 'process', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (4, '报销申请待审批', '您有一个报销申请需要审批：\n\n申请人：王五\n报销类型：差旅费\n报销金额：1500元\n报销事由：客户拜访交通费\n\n请及时处理。', 2, 2, 4, 13, 1, '2026-01-09 11:20:00', 'process', 2, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (5, '新任务分配：前端优化', '您有一个新的任务：\n\n任务标题：OA系统前端优化\n优先级：高\n截止时间：2026-01-20\n\n请及时开始处理，如有问题请联系项目负责人。', 3, 3, 3, 5, 1, '2026-01-08 14:15:00', 'task', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (6, '任务提醒：数据库优化', '任务提醒：\n\n您的任务\"数据库性能优化\"即将到期，请注意时间安排。\n\n截止时间：2026-01-25\n当前进度：进行中\n\n如需延期请及时申请。', 3, 2, NULL, 6, 0, NULL, 'task', 2, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (7, '春节放假通知', '关于2026年春节放假安排的通知已发布，请查看详情。\n\n放假时间：2026年1月28日至2月3日\n\n请提前做好工作安排。', 4, 1, 1, 5, 1, '2026-01-09 10:05:00', 'announcement', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (8, '新员工培训通知', '新员工入职培训将于1月15日举行，请相关人员准时参加。\n\n培训时间：上午9:00-12:00\n培训地点：会议室A', 4, 1, 16, 17, 0, NULL, 'announcement', 2, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (9, '会议通知：技术部周会', '技术部周会将于明天下午2点在会议室A举行，请准时参加。', 3, 1, 2, 3, 0, NULL, NULL, NULL, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (10, '文档审核通知', '您提交的技术规范文档已通过审核，可以正式发布使用。', 1, 1, NULL, 2, 1, '2026-01-09 15:45:00', 'document', 3, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (11, '密码到期提醒', '您的登录密码将于7天后到期，请及时修改密码。', 1, 2, NULL, 11, 0, NULL, NULL, NULL, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (12, '考勤异常提醒', '检测到您昨日考勤异常，请及时补充考勤记录。', 1, 2, NULL, 12, 0, NULL, NULL, NULL, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (13, '项目进度汇报', '请于本周五前提交项目进度汇报，包括完成情况和遇到的问题。', 3, 2, 3, 4, 1, '2026-01-09 16:30:00', 'task', 3, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (14, '培训资料更新', '销售培训资料已更新，请下载最新版本进行学习。', 4, 1, 9, 10, 0, NULL, 'document', 6, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_message` VALUES (15, '财务报表提醒', '月度财务报表截止日期临近，请抓紧时间完成。', 3, 3, 13, 14, 0, NULL, 'task', 5, '2026-01-09 14:40:55', '2026-01-09 14:40:55');

-- ----------------------------
-- Table structure for deyoch_message_template
-- ----------------------------
DROP TABLE IF EXISTS `deyoch_message_template`;
CREATE TABLE `deyoch_message_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型',
  `title_template` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题模板',
  `content_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容模板',
  `variables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '变量列表',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_message_template
-- ----------------------------
INSERT INTO `deyoch_message_template` VALUES (1, '任务分配通知', 'task_assign', '您有新的任务：{taskTitle}', '您好，{receiverName}！\n\n您有一个新的任务需要处理：\n\n任务标题：{taskTitle}\n任务内容：{taskContent}\n优先级：{priority}\n截止时间：{deadline}\n\n请及时处理，如有问题请联系任务创建人。', 'receiverName,taskTitle,taskContent,priority,deadline', 1, '2026-01-09 14:40:55');
INSERT INTO `deyoch_message_template` VALUES (2, '会议通知', 'meeting', '会议通知：{meetingTitle}', '您好，{receiverName}！\n\n您有一个会议需要参加：\n\n会议主题：{meetingTitle}\n会议时间：{meetingTime}\n会议地点：{meetingLocation}\n会议内容：{meetingContent}\n\n请准时参加。', 'receiverName,meetingTitle,meetingTime,meetingLocation,meetingContent', 1, '2026-01-09 14:40:55');
INSERT INTO `deyoch_message_template` VALUES (3, '审批通知', 'approval', '您有新的审批任务', '您好，{receiverName}！\n\n您有一个新的审批任务：\n\n申请类型：{approvalType}\n申请人：{applicantName}\n申请时间：{applyTime}\n申请内容：{applyContent}\n\n请及时处理审批。', 'receiverName,approvalType,applicantName,applyTime,applyContent', 1, '2026-01-09 14:40:55');
INSERT INTO `deyoch_message_template` VALUES (4, '系统通知', 'system', '系统通知：{noticeTitle}', '您好，{receiverName}！\n\n系统通知：\n\n{noticeContent}\n\n如有疑问，请联系系统管理员。', 'receiverName,noticeTitle,noticeContent', 1, '2026-01-09 14:40:55');

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
INSERT INTO `deyoch_process_instance` VALUES (1, 1, '李四年假申请', 3, '2026-01-08 10:30:00', NULL, 0, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_process_instance` VALUES (2, 2, '王五差旅费报销', 4, '2026-01-08 14:20:00', '2026-01-09 11:20:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_process_instance` VALUES (3, 3, '赵六出差申请', 5, '2026-01-07 09:15:00', NULL, 0, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_process_instance` VALUES (4, 4, '办公用品采购申请', 19, '2026-01-06 16:00:00', '2026-01-08 10:00:00', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_process_instance` VALUES (5, 5, '孙七加班申请', 6, '2026-01-09 08:45:00', NULL, 0, '2026-01-09 14:40:55', '2026-01-09 14:40:55');

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_role
-- ----------------------------
INSERT INTO `deyoch_role` VALUES (1, '管理员', 'admin', '系统管理员', '2025-12-25 15:00:51', '2026-01-08 15:55:06');
INSERT INTO `deyoch_role` VALUES (2, '普通用户', 'user', '普通用户', '2025-12-25 15:00:51', '2026-01-08 15:14:07');
INSERT INTO `deyoch_role` VALUES (3, '部门主管', 'supervisor', '部门主管，负责部门日常管理', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_role` VALUES (5, '项目经理', 'pm', '项目经理，负责项目管理', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_role` VALUES (6, '开发工程师', 'developer', '开发工程师', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_role` VALUES (7, '测试工程师', 'tester', '测试工程师', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_role` VALUES (8, '销售代表', 'sales', '销售代表', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_role` VALUES (9, '人事专员', 'hr_specialist', '人事专员', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_role` VALUES (10, '财务专员', 'finance_specialist', '财务专员', '2026-01-09 14:40:55', '2026-01-09 14:40:55');

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
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_role_permission
-- ----------------------------
INSERT INTO `deyoch_role_permission` VALUES (41, 1, 1);
INSERT INTO `deyoch_role_permission` VALUES (42, 1, 2);
INSERT INTO `deyoch_role_permission` VALUES (43, 1, 3);
INSERT INTO `deyoch_role_permission` VALUES (44, 1, 4);
INSERT INTO `deyoch_role_permission` VALUES (45, 1, 5);
INSERT INTO `deyoch_role_permission` VALUES (46, 1, 7);
INSERT INTO `deyoch_role_permission` VALUES (47, 1, 8);
INSERT INTO `deyoch_role_permission` VALUES (48, 1, 9);
INSERT INTO `deyoch_role_permission` VALUES (49, 1, 10);
INSERT INTO `deyoch_role_permission` VALUES (50, 1, 11);
INSERT INTO `deyoch_role_permission` VALUES (51, 1, 12);
INSERT INTO `deyoch_role_permission` VALUES (52, 1, 13);
INSERT INTO `deyoch_role_permission` VALUES (53, 3, 7);
INSERT INTO `deyoch_role_permission` VALUES (54, 3, 9);
INSERT INTO `deyoch_role_permission` VALUES (55, 3, 10);
INSERT INTO `deyoch_role_permission` VALUES (56, 3, 11);
INSERT INTO `deyoch_role_permission` VALUES (57, 3, 12);
INSERT INTO `deyoch_role_permission` VALUES (58, 3, 13);
INSERT INTO `deyoch_role_permission` VALUES (59, 5, 7);
INSERT INTO `deyoch_role_permission` VALUES (60, 5, 9);
INSERT INTO `deyoch_role_permission` VALUES (61, 5, 10);
INSERT INTO `deyoch_role_permission` VALUES (62, 5, 11);
INSERT INTO `deyoch_role_permission` VALUES (63, 5, 12);
INSERT INTO `deyoch_role_permission` VALUES (64, 5, 13);
INSERT INTO `deyoch_role_permission` VALUES (65, 6, 7);
INSERT INTO `deyoch_role_permission` VALUES (66, 6, 9);
INSERT INTO `deyoch_role_permission` VALUES (67, 6, 10);
INSERT INTO `deyoch_role_permission` VALUES (68, 6, 11);
INSERT INTO `deyoch_role_permission` VALUES (69, 6, 12);
INSERT INTO `deyoch_role_permission` VALUES (70, 6, 13);
INSERT INTO `deyoch_role_permission` VALUES (71, 7, 7);
INSERT INTO `deyoch_role_permission` VALUES (72, 7, 9);
INSERT INTO `deyoch_role_permission` VALUES (73, 7, 10);
INSERT INTO `deyoch_role_permission` VALUES (74, 7, 11);
INSERT INTO `deyoch_role_permission` VALUES (75, 7, 12);
INSERT INTO `deyoch_role_permission` VALUES (76, 7, 13);
INSERT INTO `deyoch_role_permission` VALUES (77, 8, 7);
INSERT INTO `deyoch_role_permission` VALUES (78, 8, 10);
INSERT INTO `deyoch_role_permission` VALUES (79, 8, 12);
INSERT INTO `deyoch_role_permission` VALUES (80, 8, 13);
INSERT INTO `deyoch_role_permission` VALUES (81, 9, 7);
INSERT INTO `deyoch_role_permission` VALUES (82, 9, 10);
INSERT INTO `deyoch_role_permission` VALUES (83, 9, 11);
INSERT INTO `deyoch_role_permission` VALUES (84, 9, 12);
INSERT INTO `deyoch_role_permission` VALUES (85, 9, 13);
INSERT INTO `deyoch_role_permission` VALUES (86, 10, 7);
INSERT INTO `deyoch_role_permission` VALUES (87, 10, 10);
INSERT INTO `deyoch_role_permission` VALUES (88, 10, 11);
INSERT INTO `deyoch_role_permission` VALUES (89, 10, 12);
INSERT INTO `deyoch_role_permission` VALUES (90, 10, 13);

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日程表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_schedule
-- ----------------------------
INSERT INTO `deyoch_schedule` VALUES (1, '技术部周会', '讨论本周工作进展和下周计划', 2, '2026-01-13 14:00:00', '2026-01-13 15:30:00', '会议室A', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_schedule` VALUES (2, '项目评审会议', '对新项目进行技术评审', 3, '2026-01-14 10:00:00', '2026-01-14 12:00:00', '会议室B', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_schedule` VALUES (3, '客户拜访', '拜访重要客户，洽谈合作事宜', 11, '2026-01-15 09:00:00', '2026-01-15 17:00:00', '客户公司', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_schedule` VALUES (4, '财务月报会议', '汇报月度财务情况', 13, '2026-01-16 15:00:00', '2026-01-16 16:00:00', '财务部', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_schedule` VALUES (5, '新员工面试', '面试前端开发工程师候选人', 17, '2026-01-17 10:00:00', '2026-01-17 11:00:00', '人事部', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_schedule` VALUES (6, '系统培训', '为新员工进行OA系统使用培训', 5, '2026-01-18 14:00:00', '2026-01-18 16:00:00', '培训室', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_schedule` VALUES (7, '部门聚餐', '技术部团建聚餐活动', 2, '2026-01-19 18:00:00', '2026-01-19 21:00:00', '海底捞火锅', 1, '2026-01-09 14:40:55', '2026-01-09 14:40:55');

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
INSERT INTO `deyoch_task` VALUES (1, 'OA系统前端优化', '优化系统前端界面，提升用户体验：\n1. 优化页面加载速度\n2. 改进UI设计\n3. 增加响应式布局', 5, 3, 3, 1, '2026-01-08 09:00:00', '2026-01-20 18:00:00', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_task` VALUES (2, '数据库性能优化', '对系统数据库进行性能优化：\n1. 分析慢查询\n2. 优化索引\n3. 数据库分表分库方案', 6, 4, 3, 1, '2026-01-10 09:00:00', '2026-01-25 18:00:00', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_task` VALUES (3, '新功能测试', '对新开发的功能模块进行全面测试：\n1. 功能测试\n2. 性能测试\n3. 兼容性测试', 8, 7, 2, 0, '2026-01-15 09:00:00', '2026-01-30 18:00:00', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_task` VALUES (4, '客户需求调研', '对重点客户进行需求调研：\n1. 制定调研计划\n2. 客户访谈\n3. 需求分析报告', 11, 10, 2, 1, '2026-01-12 09:00:00', '2026-01-26 18:00:00', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_task` VALUES (5, '年度财务报表编制', '编制2025年度财务报表：\n1. 收集财务数据\n2. 编制报表\n3. 审核确认', 14, 13, 3, 1, '2026-01-05 09:00:00', '2026-01-31 18:00:00', '2026-01-09 14:40:55', '2026-01-09 14:40:55');
INSERT INTO `deyoch_task` VALUES (6, '招聘计划制定', '制定2026年第一季度招聘计划：\n1. 各部门需求调研\n2. 制定招聘策略\n3. 发布招聘信息', 17, 16, 2, 2, '2026-01-03 09:00:00', '2026-01-15 18:00:00', '2026-01-09 14:40:55', '2026-01-09 14:40:55');

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
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of deyoch_user
-- ----------------------------
INSERT INTO `deyoch_user` VALUES (1, 'admin', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '管理员', '大哥', 'admin@deyoch.com', '13800138000', NULL, NULL, NULL, NULL, '', 1, 1, 1, '2025-12-25 15:00:51', '2026-01-09 18:22:51');
INSERT INTO `deyoch_user` VALUES (2, 'zhangsan', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '张三', '张三', 'zhangsan@deyoch.com', '13800138101', '技术总监', 'A座5楼501', '8501', 'EMP001', '', 2, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (3, 'lisi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '李四', '李四', 'lisi@deyoch.com', '13800138102', '前端架构师', 'A座5楼502', '8502', 'EMP002', '', 7, 5, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (4, 'wangwu', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '王五', '王五', 'wangwu@deyoch.com', '13800138103', '后端架构师', 'A座5楼503', '8503', 'EMP003', '', 8, 5, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (5, 'zhaoliu', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '赵六', '赵六', 'zhaoliu@deyoch.com', '13800138104', '高级前端工程师', 'A座5楼504', '8504', 'EMP004', '', 7, 6, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (6, 'sunqi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '孙七', '孙七', 'sunqi@deyoch.com', '13800138105', '高级后端工程师', 'A座5楼505', '8505', 'EMP005', '', 8, 6, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (7, 'zhouba', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '周八', '周八', 'zhouba@deyoch.com', '13800138106', '测试主管', 'A座5楼506', '8506', 'EMP006', '', 9, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (8, 'wujiu', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '吴九', '吴九', 'wujiu@deyoch.com', '13800138107', '高级测试工程师', 'A座5楼507', '8507', 'EMP007', '', 9, 7, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (9, 'zhengshi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '郑十', '郑十', 'zhengshi@deyoch.com', '13800138201', '市场总监', 'B座3楼301', '8301', 'EMP008', '', 3, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (10, 'chenyi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '陈一', '陈一', 'chenyi@deyoch.com', '13800138202', '销售经理', 'B座3楼302', '8302', 'EMP009', '', 10, 4, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (11, 'liuer', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '刘二', '刘二', 'liuer@deyoch.com', '13800138203', '销售代表', 'B座3楼303', '8303', 'EMP010', '', 10, 8, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (12, 'huangsan', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '黄三', '黄三', 'huangsan@deyoch.com', '13800138204', '销售代表', 'B座3楼304', '8304', 'EMP011', '', 11, 8, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (13, 'xusi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '徐四', '徐四', 'xusi@deyoch.com', '13800138301', '财务总监', 'C座2楼201', '8201', 'EMP012', '', 4, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (14, 'caowu', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '曹五', '曹五', 'caowu@deyoch.com', '13800138302', '会计', 'C座2楼202', '8202', 'EMP013', '', 4, 10, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (15, 'yanliu', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '严六', '严六', 'yanliu@deyoch.com', '13800138303', '出纳', 'C座2楼203', '8203', 'EMP014', '', 4, 10, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (16, 'heqi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '何七', '何七', 'heqi@deyoch.com', '13800138401', '人事总监', 'D座4楼401', '8401', 'EMP015', '', 5, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (17, 'lvba', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '吕八', '吕八', 'lvba@deyoch.com', '13800138402', '招聘专员', 'D座4楼402', '8402', 'EMP016', '', 5, 9, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (18, 'shaojiu', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '邵九', '邵九', 'shaojiu@deyoch.com', '13800138403', '薪酬专员', 'D座4楼403', '8403', 'EMP017', '', 5, 9, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (19, 'qianshi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '钱十', '钱十', 'qianshi@deyoch.com', '13800138501', '行政总监', 'E座1楼101', '8101', 'EMP018', '', 6, 3, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');
INSERT INTO `deyoch_user` VALUES (20, 'sunyi', '$2a$10$DiwWYwCJYjQSOhsoMW4d8uK9Meq8zx3Wwu1UCrFN/M4MxVo5eX6oO', '孙一', '孙一', 'sunyi@deyoch.com', '13800138502', '行政专员', 'E座1楼102', '8102', 'EMP019', '', 6, 2, 1, '2026-01-09 14:40:55', '2026-01-09 14:41:43');

-- ----------------------------
-- View structure for v_contact_directory
-- ----------------------------
DROP VIEW IF EXISTS `v_contact_directory`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_contact_directory` AS select `u`.`id` AS `id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`u`.`phone` AS `phone`,`u`.`position` AS `position`,`u`.`office_location` AS `office_location`,`u`.`extension` AS `extension`,`u`.`employee_id` AS `employee_id`,`u`.`status` AS `status`,`d`.`dept_name` AS `dept_name`,`r`.`role_name` AS `role_name` from ((`deyoch_user` `u` left join `deyoch_dept` `d` on((`u`.`dept_id` = `d`.`id`))) left join `deyoch_role` `r` on((`u`.`role_id` = `r`.`id`))) where (`u`.`status` = 1);

SET FOREIGN_KEY_CHECKS = 1;
