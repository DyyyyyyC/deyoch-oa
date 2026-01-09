-- 数据库升级脚本 v2.1
-- 添加通讯录和消息中心权限，以及分片上传相关表结构

USE `deyoch_oa`;

-- 添加通讯录权限
INSERT IGNORE INTO `deyoch_permission` VALUES (12, '通讯录', 'oa:contact:view', 'menu', 0, '/contact', 'contact/index', 'AddressBook', 7, 1, '2026-01-09 10:25:14', '2026-01-09 10:25:14');

-- 添加消息中心权限
INSERT IGNORE INTO `deyoch_permission` VALUES (13, '消息中心', 'oa:message:view', 'menu', 0, '/message', 'message/index', 'ChatDotRound', 8, 1, '2026-01-09 10:25:14', '2026-01-09 10:25:14');

-- 为管理员角色添加通讯录权限
INSERT IGNORE INTO `deyoch_role_permission` VALUES (27, 1, 12);

-- 为管理员角色添加消息中心权限
INSERT IGNORE INTO `deyoch_role_permission` VALUES (28, 1, 13);

-- 创建分片上传记录表
CREATE TABLE IF NOT EXISTS `deyoch_chunk_upload`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `file_size` bigint NOT NULL,
  `total_chunks` int NOT NULL,
  `chunk_size` bigint NOT NULL DEFAULT 2097152,
  `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-上传中，1-已完成，2-已取消',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_id`(`upload_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分片上传记录表' ROW_FORMAT = Dynamic;

-- 创建分片信息表
CREATE TABLE IF NOT EXISTS `deyoch_chunk_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `chunk_index` int NOT NULL,
  `chunk_size` bigint NOT NULL,
  `chunk_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `chunk_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_upload_chunk`(`upload_id`, `chunk_index`) USING BTREE,
  INDEX `idx_upload_id`(`upload_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分片信息表' ROW_FORMAT = Dynamic;

-- 创建文档版本表
CREATE TABLE IF NOT EXISTS `deyoch_document_version`  (
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文档版本表' ROW_FORMAT = Dynamic;

-- 记录升级版本
INSERT IGNORE INTO `deyoch_db_version` (`version`, `description`) VALUES ('2.1', '添加通讯录和消息中心权限，分片上传功能');