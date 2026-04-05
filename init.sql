-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `algomind_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE `algomind_db`;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `t_user` (
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
  `github` varchar(500) DEFAULT NULL COMMENT 'GitHub链接',
  `website` varchar(500) DEFAULT NULL COMMENT '个人网站',
  `status_emoji` varchar(50) DEFAULT NULL COMMENT '状态表情',
  `status_mood` varchar(100) DEFAULT NULL COMMENT '心情',
  `is_busy` tinyint DEFAULT 0 COMMENT '是否忙碌',
  `busy_auto_reply` varchar(500) DEFAULT NULL COMMENT '忙碌自动回复',
  `busy_end_time` datetime DEFAULT NULL COMMENT '忙碌结束时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='用户表';

-- 2. 关卡表
CREATE TABLE IF NOT EXISTS `t_level` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `track` varchar(20) NOT NULL COMMENT '赛道：algo|ds|contest',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `name` varchar(100) NOT NULL COMMENT '关卡名称',
  `is_unlocked` tinyint DEFAULT 0 COMMENT '是否解锁',
  `reward_points` int DEFAULT 10 COMMENT '通关奖励积分',
  `type` varchar(20) DEFAULT 'single' COMMENT '题目类型：single|multi|judge|fill|code',
  `question` varchar(1000) NOT NULL COMMENT '题目题干',
  `answer` varchar(500) NOT NULL COMMENT '正确答案',
  `description` varchar(500) DEFAULT NULL COMMENT '关卡描述'
) COMMENT='关卡表';

-- 2.1 关卡选项表（用于单选/多选题）
CREATE TABLE IF NOT EXISTS `t_level_options` (
  `level_id` bigint NOT NULL,
  `options` varchar(500) NOT NULL,
  FOREIGN KEY (`level_id`) REFERENCES `t_level`(`id`) ON DELETE CASCADE,
  INDEX `idx_level_id` (`level_id`)
) COMMENT='关卡选项表';

-- 3. 错题本表
CREATE TABLE IF NOT EXISTS `t_error_item` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `level_id` bigint DEFAULT NULL COMMENT '关卡ID',
  `question` varchar(1000) NOT NULL COMMENT '题目',
  `user_answer` varchar(500) NOT NULL COMMENT '用户答案',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `analysis_status` varchar(20) DEFAULT '未分析' COMMENT '分析状态',
  `analysis` varchar(2000) DEFAULT NULL COMMENT 'AI分析结果'
) COMMENT='错题本表';

-- 4. 学习计划表
CREATE TABLE IF NOT EXISTS `t_learning_plan` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `track` varchar(20) DEFAULT 'algo' COMMENT '赛道',
  `track_label` varchar(50) DEFAULT NULL COMMENT '赛道标签',
  `weekly_goal` int DEFAULT 10 COMMENT '每周目标',
  `week_goals_json` text DEFAULT NULL COMMENT '周目标JSON',
  `daily_tasks_json` text DEFAULT NULL COMMENT '每日任务JSON',
  `recommendations_json` text DEFAULT NULL COMMENT '推荐JSON',
  `is_ai_generated` tinyint DEFAULT 0 COMMENT '是否AI生成',
  `ai_analysis` varchar(4000) DEFAULT NULL COMMENT 'AI分析内容',
  `generated_at` datetime DEFAULT NULL COMMENT '生成时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `t_user`(`id`) ON DELETE CASCADE
) COMMENT='学习计划表';

-- 5. 论坛帖子表
CREATE TABLE IF NOT EXISTS `t_forum_post` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `author` varchar(50) NOT NULL COMMENT '作者名称',
  `author_level` varchar(20) DEFAULT NULL COMMENT '作者等级',
  `avatar` varchar(500) DEFAULT NULL COMMENT '作者头像',
  `topic` varchar(200) NOT NULL COMMENT '帖子标题',
  `content` varchar(2000) NOT NULL COMMENT '帖子内容',
  `quote` varchar(1000) DEFAULT NULL COMMENT '引用内容',
  `tag` varchar(50) DEFAULT NULL COMMENT '帖子标签',
  `likes` int DEFAULT 0 COMMENT '点赞数',
  `comments` int DEFAULT 0 COMMENT '评论数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP
) COMMENT='论坛帖子表';

-- 6. 论坛评论表
CREATE TABLE IF NOT EXISTS `t_forum_comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `author` varchar(50) NOT NULL COMMENT '作者名称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '作者头像',
  `author_level` varchar(20) DEFAULT NULL COMMENT '作者等级',
  `content` varchar(2000) NOT NULL COMMENT '评论内容',
  `likes` int DEFAULT 0 COMMENT '点赞数',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`post_id`) REFERENCES `t_forum_post`(`id`) ON DELETE CASCADE
) COMMENT='论坛评论表';

-- 7. 课程表
CREATE TABLE IF NOT EXISTS `t_course` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '课程标题',
  `teacher` varchar(100) DEFAULT NULL COMMENT '授课老师',
  `track` varchar(20) NOT NULL COMMENT '赛道：algo|ds|contest',
  `duration` varchar(50) DEFAULT NULL COMMENT '课时时长',
  `progress` int DEFAULT 0 COMMENT '学习进度(百分比)'
) COMMENT='课程表';

-- 7.1 课程标签表
CREATE TABLE IF NOT EXISTS `t_course_tags` (
  `course_id` bigint NOT NULL,
  `tags` varchar(100) NOT NULL,
  FOREIGN KEY (`course_id`) REFERENCES `t_course`(`id`) ON DELETE CASCADE,
  INDEX `idx_course_id` (`course_id`)
) COMMENT='课程标签表';

-- 8. 课程评论表
CREATE TABLE IF NOT EXISTS `t_course_comment` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) NOT NULL COMMENT '用户名',
  `user_avatar` varchar(500) DEFAULT NULL COMMENT '用户头像',
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `likes` int DEFAULT 0 COMMENT '点赞数',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`course_id`) REFERENCES `t_course`(`id`) ON DELETE CASCADE
) COMMENT='课程评论表';

-- ============================================
-- 插入测试数据
-- ============================================

-- 插入测试用户
INSERT INTO `t_user` (`name`, `email`, `password`, `points`, `target_track`, `weekly_goal`, `status_emoji`, `status_mood`) 
VALUES ('测试用户', 'test@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.Mr/.wFzX6RrY.qIxZqRrZqRrZqRrZq', 100, 'algo', 10, '🎯', '学习中');

-- 插入测试关卡
INSERT INTO `t_level` (`name`, `track`, `sort_order`, `reward_points`, `question`, `answer`, `type`, `is_unlocked`) 
VALUES 
('二分查找基础', 'algo', 1, 20, '给定一个有序数组 [-1,0,3,5,9,12]，目标值 9，使用二分查找返回其下标。', '4', 'single', 1),
('冒泡排序', 'algo', 2, 25, '以下哪个是冒泡排序的时间复杂度？', 'O(n²)', 'single', 1),
('链表基础', 'ds', 1, 20, '链表插入操作的时间复杂度是多少？', 'O(1)', 'single', 1),
('动态规划入门', 'contest', 1, 30, '斐波那契数列使用动态规划的时间复杂度是？', 'O(n)', 'single', 1);

-- 插入关卡选项
INSERT INTO `t_level_options` (`level_id`, `options`) 
VALUES 
(1, '["-1", "0", "3", "4", "5", "9", "12"]'),
(2, '["O(n)", "O(n²)", "O(log n)", "O(n log n]'),
(3, '["O(1)", "O(n)", "O(log n)", "O(n²)"]'),
(4, '["O(1)", "O(n)", "O(n²)", "O(2^n)"]');

-- 插入测试课程
INSERT INTO `t_course` (`title`, `teacher`, `track`, `duration`, `progress`) 
VALUES 
('算法基础入门', '张老师', 'algo', '2小时', 30),
('数据结构精讲', '李老师', 'ds', '3小时', 0),
('竞赛算法进阶', '王老师', 'contest', '4小时', 0);

-- 插入课程标签
INSERT INTO `t_course_tags` (`course_id`, `tags`) 
VALUES 
(1, '基础'),
(1, '入门'),
(2, '数据结构'),
(2, '核心'),
(3, '竞赛'),
(3, '进阶');

-- 插入测试论坛帖子
INSERT INTO `t_forum_post` (`author`, `author_level`, `avatar`, `topic`, `content`, `tag`, `likes`, `comments`) 
VALUES 
('算法爱好者', 'Lv.5', '/avatars/default.png', '如何高效学习动态规划？', '最近在学动态规划，感觉很难入门，有什么好的学习方法推荐吗？', '求助', 10, 3),
('数据结构达人', 'Lv.8', '/avatars/default.png', '分享：红黑树详解', '花了一周时间整理的红黑树知识点，希望对大家有帮助！', '分享', 25, 5);

-- 插入论坛评论
INSERT INTO `t_forum_comment` (`post_id`, `user_id`, `author`, `avatar`, `author_level`, `content`, `likes`) 
VALUES 
(1, 1, '测试用户', '/avatars/default.png', 'Lv.3', '建议从简单的题目开始，先理解状态转移方程的概念。', 5),
(1, NULL, '路人甲', '/avatars/default.png', 'Lv.2', '同问，我也在学动态规划', 2);

SELECT '数据库初始化完成！' AS message;
