-- ============================================================
-- FlowSync 协同任务管理系统 - 数据库建表脚本
-- 适用于 Railway MySQL
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role` varchar(30) NOT NULL COMMENT '角色',
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 项目表
CREATE TABLE IF NOT EXISTS `project_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(100) NOT NULL COMMENT '项目名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `priority` varchar(20) NOT NULL COMMENT '优先级',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 项目成员表
CREATE TABLE IF NOT EXISTS `project_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'member',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 任务表
CREATE TABLE IF NOT EXISTS `task_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `project_id` bigint NOT NULL COMMENT '所属项目',
  `parent_id` bigint DEFAULT NULL COMMENT '父任务',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `assignee_id` bigint DEFAULT NULL COMMENT '负责人',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `priority` varchar(20) NOT NULL COMMENT '优先级',
  `due_date` date DEFAULT NULL COMMENT '截止日期',
  `ai_suggestion` text COMMENT 'AI建议',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `completed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否完成',
  `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
  `completion_summary` varchar(500) DEFAULT NULL COMMENT '完成总结',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 任务日志表
CREATE TABLE IF NOT EXISTS `task_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `progress_percent` int NOT NULL COMMENT '进度百分比',
  `content` varchar(1000) NOT NULL COMMENT '内容',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务日志表';

-- 总结表
CREATE TABLE IF NOT EXISTS `task_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '总结ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `task_id` bigint DEFAULT NULL COMMENT '任务ID',
  `summary_type` varchar(30) NOT NULL COMMENT '总结类型',
  `content` varchar(2000) NOT NULL COMMENT '内容',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='总结表';

-- 插入默认管理员账号（密码: admin123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `phone`, `email`)
SELECT 'admin', 'admin123', '管理员', 'leader', '13800000000', 'admin@flowsync.com'
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');