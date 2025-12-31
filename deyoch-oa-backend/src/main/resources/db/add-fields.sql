-- OA系统管理表SQL语句

-- 1. 确认所有管理表都已包含id列（主键）
-- 所有表都已在初始化脚本中定义了id列，作为自增主键

-- 2. 为任务表添加创建人字段（存储用户名，因为用户名不可修改）
ALTER TABLE `deyoch_task` 
ADD COLUMN `creator` varchar(50) NOT NULL COMMENT '创建人（用户名，不可修改）' AFTER `assignee`;

-- 3. 为流程实例表添加更新人字段（存储用户名）
ALTER TABLE `deyoch_process_instance` 
ADD COLUMN `updater` varchar(50) DEFAULT NULL COMMENT '更新人（用户名）' AFTER `initiator`;

-- 4. 为文档表添加更新人字段（存储用户名）
ALTER TABLE `deyoch_document` 
ADD COLUMN `updater` varchar(50) DEFAULT NULL COMMENT '更新人（用户名）' AFTER `uploader`;

-- 5. 查看所有管理表的结构（验证id列存在）
SHOW COLUMNS FROM `deyoch_user`;          -- 用户表
SHOW COLUMNS FROM `deyoch_role`;          -- 角色表
SHOW COLUMNS FROM `deyoch_permission`;    -- 权限表
SHOW COLUMNS FROM `deyoch_dept`;          -- 部门表
SHOW COLUMNS FROM `deyoch_announcement`;  -- 公告表
SHOW COLUMNS FROM `deyoch_process`;       -- 流程表
SHOW COLUMNS FROM `deyoch_process_instance`; -- 流程实例表
SHOW COLUMNS FROM `deyoch_task`;          -- 任务表
SHOW COLUMNS FROM `deyoch_schedule`;      -- 日程表
SHOW COLUMNS FROM `deyoch_document`;      -- 文档表

-- 6. 查看所有表的主键信息
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    CONSTRAINT_NAME, 
    REFERENCED_TABLE_NAME, 
    REFERENCED_COLUMN_NAME 
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE 
    TABLE_SCHEMA = 'deyoch_oa' 
    AND CONSTRAINT_NAME = 'PRIMARY';
