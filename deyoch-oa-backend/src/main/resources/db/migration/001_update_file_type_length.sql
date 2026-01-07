-- 更新文档表file_type字段长度
-- 从 varchar(50) 扩展到 varchar(100) 以支持长MIME类型

ALTER TABLE `deyoch_document` 
MODIFY COLUMN `file_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型';