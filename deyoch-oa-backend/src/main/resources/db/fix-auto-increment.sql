-- 调整deyoch_process表的AUTO_INCREMENT值
-- 1. 首先查询当前表的最大ID值
SELECT MAX(id) AS max_id FROM deyoch_process;

-- 2. 根据查询结果，调整AUTO_INCREMENT值为max_id + 1
-- 例如，如果max_id是6，则执行：
-- ALTER TABLE deyoch_process AUTO_INCREMENT = 7;

-- 3. 为了自动化执行，可以使用以下存储过程
DELIMITER //
CREATE PROCEDURE fix_auto_increment()
BEGIN
    DECLARE max_id BIGINT;
    
    -- 处理deyoch_process表
    SELECT MAX(id) INTO max_id FROM deyoch_process;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_process AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    -- 处理deyoch_process_instance表
    SELECT MAX(id) INTO max_id FROM deyoch_process_instance;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_process_instance AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    -- 处理其他相关表
    SELECT MAX(id) INTO max_id FROM deyoch_announcement;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_announcement AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    SELECT MAX(id) INTO max_id FROM deyoch_task;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_task AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    SELECT MAX(id) INTO max_id FROM deyoch_schedule;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_schedule AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    SELECT MAX(id) INTO max_id FROM deyoch_user;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_user AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    SELECT MAX(id) INTO max_id FROM deyoch_role;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_role AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    SELECT MAX(id) INTO max_id FROM deyoch_dept;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_dept AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
    
    SELECT MAX(id) INTO max_id FROM deyoch_document;
    IF max_id IS NOT NULL THEN
        SET @sql = CONCAT('ALTER TABLE deyoch_document AUTO_INCREMENT = ', max_id + 1);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 执行存储过程
CALL fix_auto_increment();

-- 删除存储过程
DROP PROCEDURE fix_auto_increment;

-- 验证修复结果
SELECT TABLE_NAME, AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'deyoch_oa' AND TABLE_NAME IN (
    'deyoch_process', 'deyoch_process_instance', 'deyoch_announcement', 
    'deyoch_task', 'deyoch_schedule', 'deyoch_user', 
    'deyoch_role', 'deyoch_dept', 'deyoch_document'
);
