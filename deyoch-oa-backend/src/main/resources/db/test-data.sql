-- OA系统测试数据生成脚本
-- 使用数据库
USE `deyoch_oa`;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 生成用户数据
INSERT INTO `deyoch_user` (`username`, `password`, `nickname`, `email`, `phone`, `avatar`, `dept_id`, `role_id`, `status`) VALUES 
('user1', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '用户1', 'user1@deyoch.com', '13800138001', '/avatar/user1.jpg', 2, 2, 1),
('user2', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '用户2', 'user2@deyoch.com', '13800138002', '/avatar/user2.jpg', 3, 2, 1),
('user3', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '用户3', 'user3@deyoch.com', '13800138003', '/avatar/user3.jpg', 4, 2, 1),
('manager1', '$2a$10$XGHfIHerZjc0J1tZkzmIoOGdHm9eoHzF2evltZKiXulnjdkuy1pau', '部门经理1', 'manager1@deyoch.com', '13800138004', '/avatar/manager1.jpg', 2, 3, 1);

-- 2. 生成流程数据
-- 明确指定id值，确保外键关联正确
INSERT INTO `deyoch_process` (`id`, `process_name`, `process_key`, `description`, `status`) VALUES 
(1, '请假流程', 'leave', '员工请假审批流程', 1),
(2, '报销流程', 'reimbursement', '费用报销审批流程', 1),
(3, '出差流程', 'business_trip', '员工出差审批流程', 1),
(4, '采购流程', 'purchase', '物资采购审批流程', 1),
(5, '加班流程', 'overtime', '员工加班审批流程', 1);

-- 3. 生成流程实例数据
-- 确保在插入前再次禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;
INSERT INTO `deyoch_process_instance` (`process_id`, `instance_name`, `initiator`, `start_time`, `end_time`, `status`) VALUES 
(1, '张三请假申请', '张三', '2025-12-25 09:00:00', '2025-12-26 10:00:00', 1),
(2, '李四报销申请', '李四', '2025-12-27 14:00:00', NULL, 0),
(3, '王五出差申请', '王五', '2025-12-28 10:00:00', NULL, 0),
(1, '赵六请假申请', '赵六', '2025-12-29 08:30:00', NULL, 0),
(4, '钱七采购申请', '钱七', '2025-12-30 11:00:00', '2025-12-30 15:00:00', 1);
SET FOREIGN_KEY_CHECKS = 1;

-- 4. 生成任务数据
INSERT INTO `deyoch_task` (`title`, `content`, `assignee`, `priority`, `status`, `start_time`, `end_time`) VALUES 
('完成项目设计', '完成OA系统的项目设计文档', 'user1', 3, 1, '2025-12-25 09:00:00', '2025-12-31 18:00:00'),
('修复系统bug', '修复用户管理模块的bug', 'user2', 2, 1, '2025-12-26 10:00:00', '2025-12-28 17:00:00'),
('编写测试用例', '编写系统测试用例', 'user3', 2, 0, '2025-12-29 09:00:00', '2025-12-31 18:00:00'),
('部署生产环境', '部署OA系统到生产环境', 'manager1', 3, 2, '2025-12-20 08:00:00', '2025-12-22 17:00:00'),
('需求分析', '分析新需求', 'user1', 2, 0, '2025-12-30 09:00:00', '2025-01-03 18:00:00');

-- 5. 生成日程数据
INSERT INTO `deyoch_schedule` (`title`, `content`, `user_id`, `start_time`, `end_time`, `location`, `status`) VALUES 
('项目评审会议', 'OA系统项目评审', 1, '2025-12-31 10:00:00', '2025-12-31 12:00:00', '会议室A', 1),
('产品培训', '新员工产品培训', 2, '2025-12-30 14:00:00', '2025-12-30 16:00:00', '培训室', 1),
('部门周会', '技术部周会', 3, '2025-12-31 15:00:00', '2025-12-31 16:30:00', '会议室B', 1),
('客户拜访', '拜访重要客户', 4, '2025-12-30 09:30:00', '2025-12-30 12:00:00', '客户公司', 1),
('年度总结', '2025年度工作总结', 1, '2025-12-30 13:30:00', '2025-12-30 15:00:00', '办公室', 1);

-- 6. 生成公告数据
INSERT INTO `deyoch_announcement` (`title`, `content`, `publisher`, `publish_time`, `status`) VALUES 
('元旦放假通知', '根据国家法定节假日安排，2026年元旦放假时间为2026年1月1日至1月3日，共3天。', '张三', '2025-12-25 10:00:00', 1),
('系统升级通知', 'OA系统将于2025年12月31日22:00-24:00进行系统升级，期间系统将暂停服务，请提前做好准备。', '李四', '2025-12-28 14:30:00', 1),
('年度优秀员工评选', '2025年度优秀员工评选活动正式启动，请各部门推荐候选人。', '王五', '2025-12-20 09:00:00', 1),
('新员工入职培训', '本周四下午14:00在培训室举行新员工入职培训，请新入职员工准时参加。', '赵六', '2025-12-29 08:30:00', 1),
('公司年会通知', '2026年公司年会将于2026年1月15日举行，请各位员工安排好时间。', '钱七', '2025-12-26 16:00:00', 1);

-- 7. 生成文档数据
INSERT INTO `deyoch_document` (`title`, `content`, `file_path`, `file_name`, `file_size`, `file_type`, `uploader`, `dept_id`, `status`) VALUES 
('OA系统需求文档', 'OA系统的详细需求分析文档', '/upload/documents/req.docx', 'req.docx', 1024000, 'docx', '张三', 2, 1),
('数据库设计文档', 'OA系统数据库设计文档', '/upload/documents/db.docx', 'db.docx', 512000, 'docx', '李四', 2, 1),
('用户手册', 'OA系统用户操作手册', '/upload/documents/user_manual.pdf', 'user_manual.pdf', 2048000, 'pdf', '王五', 1, 1),
('开发规范', 'OA系统开发规范文档', '/upload/documents/dev_spec.docx', 'dev_spec.docx', 768000, 'docx', '赵六', 2, 1),
('测试报告', 'OA系统测试报告', '/upload/documents/test_report.pdf', 'test_report.pdf', 1536000, 'pdf', '钱七', 2, 1);

-- 8. 更新角色权限关联数据（添加新的权限路径）
UPDATE `deyoch_permission` SET `path` = '/process' WHERE `perm_code` = 'oa:process:manage';
UPDATE `deyoch_permission` SET `path` = '/process/definition' WHERE `perm_code` = 'oa:process:manage';
UPDATE `deyoch_permission` SET `path` = '/announcement' WHERE `perm_code` = 'oa:announcement:manage';
UPDATE `deyoch_permission` SET `path` = '/task' WHERE `perm_code` = 'oa:task:manage';
UPDATE `deyoch_permission` SET `path` = '/schedule' WHERE `perm_code` = 'oa:schedule:manage';
UPDATE `deyoch_permission` SET `path` = '/document' WHERE `perm_code` = 'oa:document:manage';

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
