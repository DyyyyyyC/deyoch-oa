-- OA系统数据库升级脚本 v2.0
-- 添加通讯录管理、消息通知、文档版本管理、分片上传等功能

USE `deyoch_oa`;

-- ========================================
-- 1. 扩展用户表，添加通讯录相关字段
-- ========================================

-- 检查并添加通讯录相关字段
-- 添加 real_name 字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE table_name = 'deyoch_user' 
     AND table_schema = 'deyoch_oa' 
     AND column_name = 'real_name') > 0,
    'SELECT "real_name column already exists"',
    'ALTER TABLE `deyoch_user` ADD COLUMN `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT "真实姓名" AFTER `nickname`'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 position 字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE table_name = 'deyoch_user' 
     AND table_schema = 'deyoch_oa' 
     AND column_name = 'position') > 0,
    'SELECT "position column already exists"',
    'ALTER TABLE `deyoch_user` ADD COLUMN `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT "职位" AFTER `phone`'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 office_location 字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE table_name = 'deyoch_user' 
     AND table_schema = 'deyoch_oa' 
     AND column_name = 'office_location') > 0,
    'SELECT "office_location column already exists"',
    'ALTER TABLE `deyoch_user` ADD COLUMN `office_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT "办公地点" AFTER `position`'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 extension 字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE table_name = 'deyoch_user' 
     AND table_schema = 'deyoch_oa' 
     AND column_name = 'extension') > 0,
    'SELECT "extension column already exists"',
    'ALTER TABLE `deyoch_user` ADD COLUMN `extension` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT "分机号" AFTER `office_location`'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 employee_id 字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE table_name = 'deyoch_user' 
     AND table_schema = 'deyoch_oa' 
     AND column_name = 'employee_id') > 0,
    'SELECT "employee_id column already exists"',
    'ALTER TABLE `deyoch_user` ADD COLUMN `employee_id` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT "工号" AFTER `extension`'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_real_name` ON `deyoch_user` (`real_name`);
CREATE INDEX IF NOT EXISTS `idx_position` ON `deyoch_user` (`position`);
CREATE INDEX IF NOT EXISTS `idx_employee_id` ON `deyoch_user` (`employee_id`);

-- ========================================
-- 2. 创建消息通知相关表
-- ========================================

-- 消息表
DROP TABLE IF EXISTS `deyoch_message`;
CREATE TABLE `deyoch_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
    `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '消息内容',
    `type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1-系统消息，2-审批通知，3-任务通知，4-公告通知',
    `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级：1-普通，2-重要，3-紧急',
    `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者ID',
    `receiver_id` bigint NOT NULL COMMENT '接收者ID',
    `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
    `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联类型：task,process,announcement,document',
    `related_id` bigint NULL DEFAULT NULL COMMENT '关联ID',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_receiver_id` (`receiver_id`) USING BTREE,
    INDEX `idx_sender_id` (`sender_id`) USING BTREE,
    INDEX `idx_type` (`type`) USING BTREE,
    INDEX `idx_is_read` (`is_read`) USING BTREE,
    INDEX `idx_created_at` (`created_at`) USING BTREE,
    INDEX `idx_receiver_read` (`receiver_id`, `is_read`) USING BTREE,
    CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `deyoch_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- 消息模板表
DROP TABLE IF EXISTS `deyoch_message_template`;
CREATE TABLE `deyoch_message_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
    `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板类型',
    `title_template` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题模板',
    `content_template` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容模板',
    `variables` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '模板变量说明（JSON格式）',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_type` (`type`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息模板表' ROW_FORMAT = Dynamic;

-- ========================================
-- 3. 扩展文档表，添加版本管理字段
-- ========================================

-- 扩展文档表
ALTER TABLE `deyoch_document` 
ADD COLUMN IF NOT EXISTS `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1.0' COMMENT '版本号' AFTER `file_type`,
ADD COLUMN IF NOT EXISTS `is_current` tinyint NOT NULL DEFAULT 1 COMMENT '是否当前版本：0-否，1-是' AFTER `version`,
ADD COLUMN IF NOT EXISTS `parent_id` bigint NULL DEFAULT NULL COMMENT '父文档ID（用于版本关联）' AFTER `is_current`,
ADD COLUMN IF NOT EXISTS `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件MD5哈希值' AFTER `parent_id`;

-- 添加索引
CREATE INDEX IF NOT EXISTS `idx_version` ON `deyoch_document` (`version`);
CREATE INDEX IF NOT EXISTS `idx_is_current` ON `deyoch_document` (`is_current`);
CREATE INDEX IF NOT EXISTS `idx_parent_id` ON `deyoch_document` (`parent_id`);
CREATE INDEX IF NOT EXISTS `idx_md5_hash` ON `deyoch_document` (`md5_hash`);

-- 文档版本历史表
DROP TABLE IF EXISTS `deyoch_document_version`;
CREATE TABLE `deyoch_document_version` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` bigint NOT NULL COMMENT '文档ID',
    `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '版本号',
    `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
    `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件路径',
    `file_size` bigint NOT NULL COMMENT '文件大小',
    `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件MD5哈希值',
    `change_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更日志',
    `created_by` bigint NOT NULL COMMENT '创建者',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_document_id` (`document_id`) USING BTREE,
    INDEX `idx_version` (`document_id`, `version`) USING BTREE,
    INDEX `idx_created_by` (`created_by`) USING BTREE,
    INDEX `idx_created_at` (`created_at`) USING BTREE,
    CONSTRAINT `fk_doc_version_document` FOREIGN KEY (`document_id`) REFERENCES `deyoch_document` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_doc_version_creator` FOREIGN KEY (`created_by`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文档版本历史表' ROW_FORMAT = Dynamic;

-- ========================================
-- 4. 创建分片上传相关表
-- ========================================

-- 分片上传信息表
DROP TABLE IF EXISTS `deyoch_chunk_upload`;
CREATE TABLE `deyoch_chunk_upload` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上传ID（UUID）',
    `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
    `file_size` bigint NOT NULL COMMENT '文件总大小',
    `chunk_size` int NOT NULL COMMENT '分片大小',
    `total_chunks` int NOT NULL COMMENT '总分片数',
    `uploaded_chunks` int NOT NULL DEFAULT 0 COMMENT '已上传分片数',
    `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件MD5哈希值',
    `user_id` bigint NOT NULL COMMENT '上传用户ID',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-上传中，1-已完成，2-已取消',
    `final_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最终文件路径',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `expires_at` datetime NOT NULL COMMENT '过期时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_upload_id` (`upload_id`) USING BTREE,
    INDEX `idx_user_id` (`user_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_expires_at` (`expires_at`) USING BTREE,
    CONSTRAINT `fk_chunk_upload_user` FOREIGN KEY (`user_id`) REFERENCES `deyoch_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分片上传信息表' ROW_FORMAT = Dynamic;

-- 分片详情表
DROP TABLE IF EXISTS `deyoch_chunk_info`;
CREATE TABLE `deyoch_chunk_info` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上传ID',
    `chunk_index` int NOT NULL COMMENT '分片索引',
    `chunk_size` int NOT NULL COMMENT '分片大小',
    `chunk_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分片文件路径',
    `md5_hash` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分片MD5哈希值',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_upload_chunk` (`upload_id`, `chunk_index`) USING BTREE,
    INDEX `idx_upload_id` (`upload_id`) USING BTREE,
    CONSTRAINT `fk_chunk_info_upload` FOREIGN KEY (`upload_id`) REFERENCES `deyoch_chunk_upload` (`upload_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分片详情表' ROW_FORMAT = Dynamic;

-- ========================================
-- 5. 创建通讯录视图
-- ========================================

-- 通讯录视图
DROP VIEW IF EXISTS `v_contact_directory`;
CREATE VIEW `v_contact_directory` AS
SELECT 
    u.id,
    u.username,
    u.real_name,
    u.nickname,
    u.phone,
    u.email,
    u.position,
    u.office_location,
    u.extension,
    u.employee_id,
    u.avatar,
    u.status,
    d.dept_name,
    d.id as dept_id,
    d.parent_id as dept_parent_id,
    r.role_name,
    r.id as role_id,
    u.created_at,
    u.updated_at
FROM deyoch_user u
LEFT JOIN deyoch_dept d ON u.dept_id = d.id
LEFT JOIN deyoch_role r ON u.role_id = r.id
WHERE u.status = 1;

-- ========================================
-- 6. 插入初始数据
-- ========================================

-- 插入消息模板数据
INSERT INTO `deyoch_message_template` (`name`, `type`, `title_template`, `content_template`, `variables`) VALUES
('任务分配通知', 'task_assign', '您有新的任务：{taskTitle}', '您好，{receiverName}！\n\n您有一个新的任务需要处理：\n任务标题：{taskTitle}\n任务内容：{taskContent}\n截止时间：{deadline}\n\n请及时处理。', '{"taskTitle":"任务标题","taskContent":"任务内容","deadline":"截止时间","receiverName":"接收人姓名"}'),
('审批通知', 'process_approve', '您有新的审批：{processName}', '您好，{receiverName}！\n\n您有一个新的审批流程需要处理：\n流程名称：{processName}\n申请人：{applicantName}\n申请时间：{applyTime}\n\n请及时审批。', '{"processName":"流程名称","applicantName":"申请人","applyTime":"申请时间","receiverName":"接收人姓名"}'),
('公告发布通知', 'announcement_publish', '新公告：{announcementTitle}', '您好，{receiverName}！\n\n有新的公告发布：\n标题：{announcementTitle}\n发布时间：{publishTime}\n\n请及时查看。', '{"announcementTitle":"公告标题","publishTime":"发布时间","receiverName":"接收人姓名"}'),
('系统通知', 'system_notice', '系统通知：{noticeTitle}', '您好，{receiverName}！\n\n系统通知：{noticeContent}\n\n如有疑问，请联系系统管理员。', '{"noticeTitle":"通知标题","noticeContent":"通知内容","receiverName":"接收人姓名"}');

-- 更新现有用户数据，添加通讯录信息
UPDATE `deyoch_user` SET 
    `real_name` = '系统管理员',
    `position` = '系统管理员',
    `office_location` = '总部大楼A座1001',
    `extension` = '1001',
    `employee_id` = 'EMP001'
WHERE `username` = 'admin';

UPDATE `deyoch_user` SET 
    `real_name` = '张三',
    `position` = '高级开发工程师',
    `office_location` = '总部大楼A座2001',
    `extension` = '2001',
    `employee_id` = 'EMP002'
WHERE `username` = 'zhangsan';

UPDATE `deyoch_user` SET 
    `real_name` = '李四',
    `position` = '市场经理',
    `office_location` = '总部大楼B座3001',
    `extension` = '3001',
    `employee_id` = 'EMP003'
WHERE `username` = 'lisi';

UPDATE `deyoch_user` SET 
    `real_name` = '李四',
    `position` = '产品经理',
    `office_location` = '总部大楼B座3002',
    `extension` = '3002',
    `employee_id` = 'EMP004'
WHERE `username` = 'lisi3';

-- 为现有文档添加版本信息
UPDATE `deyoch_document` SET 
    `version` = '1.0',
    `is_current` = 1,
    `parent_id` = NULL
WHERE `version` IS NULL OR `version` = '';

-- ========================================
-- 7. 创建清理过期分片的存储过程
-- ========================================

DELIMITER $$

DROP PROCEDURE IF EXISTS `CleanExpiredChunks`$$

CREATE PROCEDURE `CleanExpiredChunks`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE upload_id_var VARCHAR(64);
    DECLARE chunk_cursor CURSOR FOR 
        SELECT upload_id FROM deyoch_chunk_upload 
        WHERE expires_at < NOW() AND status = 0;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN chunk_cursor;
    
    read_loop: LOOP
        FETCH chunk_cursor INTO upload_id_var;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 删除分片详情
        DELETE FROM deyoch_chunk_info WHERE upload_id = upload_id_var;
        
        -- 删除上传记录
        DELETE FROM deyoch_chunk_upload WHERE upload_id = upload_id_var;
        
    END LOOP;
    
    CLOSE chunk_cursor;
END$$

DELIMITER ;

-- ========================================
-- 8. 创建事件调度器（可选）
-- ========================================

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 创建定时清理过期分片的事件
DROP EVENT IF EXISTS `event_clean_expired_chunks`;

CREATE EVENT `event_clean_expired_chunks`
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
  CALL CleanExpiredChunks();

-- ========================================
-- 升级完成标记
-- ========================================

-- 创建版本记录表（如果不存在）
CREATE TABLE IF NOT EXISTS `deyoch_db_version` (
    `id` int NOT NULL AUTO_INCREMENT,
    `version` varchar(20) NOT NULL,
    `description` varchar(200) NOT NULL,
    `upgrade_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据库版本记录表';

-- 记录升级版本
INSERT INTO `deyoch_db_version` (`version`, `description`) VALUES 
('2.0.0', 'OA系统功能完善：添加通讯录管理、消息通知、文档版本管理、分片上传等功能');

-- 输出升级完成信息
SELECT 'Database upgrade to v2.0.0 completed successfully!' as message;