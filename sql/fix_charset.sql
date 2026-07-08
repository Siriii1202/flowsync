-- 修复 task_info 表中的中文乱码
-- 清空现有任务数据
DELETE FROM task_info;

-- 重新插入含中文的任务数据
INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '需求调研与分析', '调研校园内二手交易的现状和需求，整理用户需求文档。', 2, 1, 'in_progress', 'high', '2026-07-15', '建议重点调研学生交易的痛点和安全需求');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '数据库设计与搭建', '设计用户表、商品表、订单表等核心数据表结构，完成数据库搭建。', 2, 1, 'todo', 'high', '2026-07-25', '注意设计商品分类和搜索索引');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '用户注册登录模块开发', '实现用户注册、登录、密码找回等功能，支持手机号验证。', 3, 1, 'todo', 'high', '2026-08-10', '建议使用JWT进行身份认证');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '商品发布与管理模块开发', '实现商品的发布、编辑、下架、搜索等功能。', 3, 1, 'todo', 'medium', '2026-08-25', '注意图片上传和压缩处理');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(2, NULL, '技术选型与架构设计', '确定知识库系统的技术栈和系统架构方案。', 1, 1, 'todo', 'high', '2026-08-10', '推荐使用Markdown作为文档格式');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(2, NULL, '文档编辑与存储功能开发', '实现在线文档的创建、编辑、保存、版本管理等功能。', 2, 1, 'todo', 'high', '2026-09-15', '考虑引入富文本编辑器');
