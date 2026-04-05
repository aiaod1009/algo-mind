-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `algomind_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `algomind_db`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '昵称',
  `email` varchar(100) UNIQUE NOT NULL COMMENT '邮箱',
  `password` varchar(255) NOT NULL COMMENT '密码(加密)',
  `points` int DEFAULT 0 COMMENT '积分',
  `target_track` varchar(20) DEFAULT 'algo' COMMENT '目标赛道',
  `weekly_goal` int DEFAULT 10 COMMENT '每周目标题数',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `bio` text COMMENT '个人简介',
  `gender` varchar(10) DEFAULT 'unknown' COMMENT '性别',
  `github` varchar(500) DEFAULT NULL,
  `website` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户表';

-- 2. 关卡表
CREATE TABLE IF NOT EXISTS `level` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `track` varchar(20) NOT NULL,
  `order_num` int DEFAULT 0,
  `reward_points` int DEFAULT 10,
  `question` text NOT NULL,
  `answer` varchar(500) NOT NULL,
  `type` varchar(20) DEFAULT 'single',
  `options` json DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP
) COMMENT='关卡表';

-- 3. 用户关卡进度表
CREATE TABLE IF NOT EXISTS `user_level` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `level_id` bigint NOT NULL,
  `is_unlocked` tinyint DEFAULT 0,
  `is_completed` tinyint DEFAULT 0,
  `completed_at` datetime DEFAULT NULL,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`level_id`) REFERENCES `level`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_user_level` (`user_id`, `level_id`)
) COMMENT='用户关卡进度表';

-- 4. 错题本表
CREATE TABLE IF NOT EXISTS `error_log` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `level_id` bigint DEFAULT NULL,
  `question` text NOT NULL,
  `user_answer` text NOT NULL,
  `description` text,
  `analysis_status` varchar(20) DEFAULT 'pending',
  `analysis` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`level_id`) REFERENCES `level`(`id`) ON DELETE SET NULL
) COMMENT='错题本表';

-- 5. 学习计划表
CREATE TABLE IF NOT EXISTS `learning_plan` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `track` varchar(20) DEFAULT 'algo',
  `track_label` varchar(50) DEFAULT NULL,
  `weekly_goal` int DEFAULT 10,
  `week_goals_json` json DEFAULT NULL,
  `daily_tasks_json` json DEFAULT NULL,
  `recommendations_json` json DEFAULT NULL,
  `is_ai_generated` tinyint DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) COMMENT='学习计划表';

-- 可选：用户活动记录表
CREATE TABLE IF NOT EXISTS `user_activity` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `activity_date` date NOT NULL,
  `solve_count` int DEFAULT 0,
  `points_earned` int DEFAULT 0,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_user_date` (`user_id`, `activity_date`)
) COMMENT='用户活动记录表';

-- 可选：积分历史表
CREATE TABLE IF NOT EXISTS `points_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `points` int NOT NULL,
  `source` varchar(100) DEFAULT NULL,
  `level_id` bigint DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) COMMENT='积分历史表';

-- 插入测试数据
INSERT INTO `user` (`name`, `email`, `password`, `points`, `target_track`, `weekly_goal`) 
VALUES ('测试用户', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr/.wFzX6RrY.qIxZqRrZqRrZqRrZq', 100, 'algo', 10);

INSERT INTO `level` (`name`, `track`, `order_num`, `reward_points`, `question`, `answer`, `type`, `options`) 
VALUES 
('二分查找基础', 'algo', 1, 20, '给定一个有序数组 [-1,0,3,5,9,12]，目标值 9，使用二分查找返回其下标。', '4', 'single', '["-1", "0", "3", "4", "5", "9", "12"]'),
('冒泡排序', 'algo', 2, 25, '以下哪个是冒泡排序的时间复杂度？', 'O(n²)', 'single', '["O(n)", "O(n²)", "O(log n)", "O(n log n)"]');

SELECT '数据库初始化完成！' AS message;