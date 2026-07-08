-- ============================================================
-- FlowSync 数据库初始化脚本
-- 功能：创建数据库、建表、插入测试数据
-- 使用方式：在 MySQL 中执行本文件即可完成初始化
-- 注意：所有 SQL 关键字使用大写，表名和字段名使用小写加下划线格式
-- ============================================================

-- 创建数据库（如果已存在则跳过）
CREATE DATABASE IF NOT EXISTS flowsync_simple
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 切换到该数据库
USE flowsync_simple;

-- ============================================================
-- 1. 用户表（sys_user）
--    存储系统用户信息，用于登录和身份识别
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '用户唯一ID（主键，自增）',
    username    VARCHAR(50)     NOT NULL UNIQUE              COMMENT '用户名（登录使用）',
    password    VARCHAR(100)    NOT NULL                     COMMENT '密码（明文存储，教学演示用）',
    real_name   VARCHAR(50)     NOT NULL                     COMMENT '真实姓名',
    role        VARCHAR(30)     NOT NULL                     COMMENT '用户角色（leader=负责人, member=成员）',
    phone       VARCHAR(20)                                 COMMENT '手机号码',
    email       VARCHAR(100)                                COMMENT '电子邮箱',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间'
) COMMENT '系统用户表';

-- ============================================================
-- 2. 项目表（project_info）
--    存储项目管理信息
-- ============================================================
CREATE TABLE IF NOT EXISTS project_info (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '项目唯一ID（主键，自增）',
    name        VARCHAR(100)    NOT NULL                     COMMENT '项目名称',
    description VARCHAR(500)                                COMMENT '项目详细描述',
    status      VARCHAR(20)     NOT NULL                     COMMENT '项目状态（planning=规划中, in_progress=进行中, completed=已完成）',
    priority    VARCHAR(20)     NOT NULL                     COMMENT '项目优先级（high=高, medium=中, low=低）',
    owner_id    BIGINT                                      COMMENT '项目负责人ID（关联sys_user表的id）',
    start_date  DATE                                        COMMENT '项目开始日期',
    end_date    DATE                                        COMMENT '项目结束日期（截止日期）',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP    COMMENT '项目创建时间'
) COMMENT '项目管理表';

-- ============================================================
-- 3. 任务表（task_info）
--    存储任务管理信息，支持父子任务层级
-- ============================================================
CREATE TABLE IF NOT EXISTS task_info (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '任务唯一ID（主键，自增）',
    project_id      BIGINT          NOT NULL                     COMMENT '所属项目ID（关联project_info表的id）',
    parent_id       BIGINT                                      COMMENT '父任务ID（为空表示顶层任务）',
    title           VARCHAR(100)    NOT NULL                     COMMENT '任务标题',
    description     VARCHAR(1000)                               COMMENT '任务详细描述',
    assignee_id     BIGINT                                      COMMENT '负责人ID（关联sys_user表的id）',
    creator_id      BIGINT                                      COMMENT '创建人ID（关联sys_user表的id）',
    status          VARCHAR(20)     NOT NULL                     COMMENT '任务状态（todo=待办, in_progress=进行中, done=已完成）',
    priority        VARCHAR(20)     NOT NULL                     COMMENT '任务优先级（high=高, medium=中, low=低）',
    due_date        DATE                                        COMMENT '任务截止日期',
    ai_suggestion   TEXT                                        COMMENT 'AI生成的智能建议',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP    COMMENT '任务创建时间'
) COMMENT '任务信息表';

-- ============================================================
-- 4. 任务日志表（task_log）
--    记录任务的进度更新和操作记录
-- ============================================================
CREATE TABLE IF NOT EXISTS task_log (
    id               BIGINT      AUTO_INCREMENT PRIMARY KEY  COMMENT '日志唯一ID（主键，自增）',
    task_id          BIGINT      NOT NULL                     COMMENT '关联的任务ID（关联task_info表的id）',
    progress_percent INT         NOT NULL                     COMMENT '进度百分比（0-100）',
    content          VARCHAR(1000) NOT NULL                   COMMENT '日志内容',
    operator_id      BIGINT                                  COMMENT '操作人ID（关联sys_user表的id）',
    create_time      DATETIME    DEFAULT CURRENT_TIMESTAMP    COMMENT '日志创建时间'
) COMMENT '任务日志表';

-- ============================================================
-- 5. 任务总结表（task_summary）
--    存储对任务或项目的阶段性总结
-- ============================================================
CREATE TABLE IF NOT EXISTS task_summary (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY  COMMENT '总结唯一ID（主键，自增）',
    project_id      BIGINT          NOT NULL                     COMMENT '关联的项目ID（关联project_info表的id）',
    task_id         BIGINT                                      COMMENT '关联的任务ID（关联task_info表的id）',
    summary_type    VARCHAR(30)     NOT NULL                     COMMENT '总结类型（task=任务总结, project=项目总结）',
    content         VARCHAR(2000)   NOT NULL                     COMMENT '总结内容',
    created_by      BIGINT                                      COMMENT '创建人ID（关联sys_user表的id）',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP    COMMENT '总结创建时间'
) COMMENT '任务总结表';

-- ============================================================
-- 插入测试数据
-- ============================================================

-- -------- 3个测试用户 --------
-- 用户1：项目负责人（leader）
INSERT INTO sys_user (username, password, real_name, role, phone, email) VALUES
('leader', '123456', '项目负责人', 'leader', '13800138001', 'leader@flowsync.com');

-- 用户2：成员张三（member）
INSERT INTO sys_user (username, password, real_name, role, phone, email) VALUES
('member1', '123456', '张三', 'member', '13800138002', 'zhangsan@flowsync.com');

-- 用户3：成员李四（member）
INSERT INTO sys_user (username, password, real_name, role, phone, email) VALUES
('member2', '123456', '李四', 'member', '13800138003', 'lisi@flowsync.com');

-- -------- 2个示例项目 --------
-- 项目1：校园二手交易平台（进行中）
INSERT INTO project_info (name, description, status, priority, owner_id, start_date, end_date) VALUES
('校园二手交易平台', '为校内师生提供一个安全、便捷的二手物品交易平台，支持发布商品、在线沟通、线下交易等功能。', 'in_progress', 'high', 1, '2026-07-01', '2026-09-30');

-- 项目2：公司内部知识库系统（规划中）
INSERT INTO project_info (name, description, status, priority, owner_id, start_date, end_date) VALUES
('公司内部知识库系统', '搭建公司内部的知识管理平台，支持文档编写、分类整理、全文检索等功能，方便员工共享和查阅知识。', 'planning', 'medium', 1, '2026-08-01', '2026-11-30');

-- -------- 若干示例任务 --------
-- 项目1的任务
INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '需求调研与分析', '调研校园内二手交易的现状和需求，整理用户需求文档。', 2, 1, 'in_progress', 'high', '2026-07-15', '建议重点调研学生交易的痛点和安全需求');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '数据库设计与搭建', '设计用户表、商品表、订单表等核心数据表结构，完成数据库搭建。', 2, 1, 'todo', 'high', '2026-07-25', '注意设计商品分类和搜索索引');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '用户注册登录模块开发', '实现用户注册、登录、密码找回等功能，支持手机号验证。', 3, 1, 'todo', 'high', '2026-08-10', '建议使用JWT进行身份认证');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(1, NULL, '商品发布与管理模块开发', '实现商品的发布、编辑、下架、搜索等功能。', 3, 1, 'todo', 'medium', '2026-08-25', '注意图片上传和压缩处理');

-- 项目2的任务
INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(2, NULL, '技术选型与架构设计', '确定知识库系统的技术栈和系统架构方案。', 1, 1, 'todo', 'high', '2026-08-10', '推荐使用Markdown作为文档格式');

INSERT INTO task_info (project_id, parent_id, title, description, assignee_id, creator_id, status, priority, due_date, ai_suggestion) VALUES
(2, NULL, '文档编辑与存储功能开发', '实现在线文档的创建、编辑、保存、版本管理等功能。', 2, 1, 'todo', 'high', '2026-09-15', '考虑引入富文本编辑器');
