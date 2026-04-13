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
  `creator_id` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `name` varchar(100) NOT NULL COMMENT '关卡名称',
  `is_unlocked` tinyint DEFAULT 0 COMMENT '是否解锁',
  `reward_points` int DEFAULT 10 COMMENT '通关奖励积分',
  `type` varchar(20) DEFAULT 'single' COMMENT '题目类型：single|multi|judge|fill|code',
  `question` varchar(1000) NOT NULL COMMENT '题目题干',
  `answer` varchar(500) NOT NULL COMMENT '正确答案',
  `description` varchar(500) DEFAULT NULL COMMENT '关卡描述'
) COMMENT='关卡表';

ALTER TABLE `t_level`
  ADD COLUMN IF NOT EXISTS `creator_id` bigint DEFAULT NULL COMMENT '创建者用户ID';

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

-- 3.1 题目尝试聚合表（每个用户每道题一条）
CREATE TABLE IF NOT EXISTS `t_question_attempt` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `level_id` bigint NOT NULL COMMENT '关卡ID',
  `question_title` varchar(200) DEFAULT NULL COMMENT '题目标题',
  `question_content` varchar(1000) DEFAULT NULL COMMENT '题目内容快照',
  `level_type` varchar(50) DEFAULT NULL COMMENT '题目类型',
  `latest_status` varchar(20) NOT NULL COMMENT '最新状态 CORRECT/WRONG',
  `latest_user_answer` varchar(1000) DEFAULT NULL COMMENT '最新提交答案',
  `submit_count` int DEFAULT 0 COMMENT '累计提交次数',
  `first_submitted_at` datetime DEFAULT NULL COMMENT '首次提交时间',
  `last_submitted_at` datetime DEFAULT NULL COMMENT '最后提交时间',
  UNIQUE KEY `uk_question_attempt_user_level` (`user_id`, `level_id`),
  INDEX `idx_question_attempt_user` (`user_id`),
  INDEX `idx_question_attempt_level` (`level_id`)
) COMMENT='题目尝试聚合表';

-- 3.2 用户做题明细记录表（热力图/活动流）
CREATE TABLE IF NOT EXISTS `t_user_problem_record` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `level_id` bigint NOT NULL COMMENT '关卡ID',
  `level_name` varchar(200) DEFAULT NULL COMMENT '题目名称快照',
  `track` varchar(20) DEFAULT 'algo' COMMENT '赛道',
  `is_correct` tinyint NOT NULL DEFAULT 0 COMMENT '本次是否正确',
  `status` varchar(20) DEFAULT NULL COMMENT 'CORRECT/WRONG',
  `stars` int NOT NULL DEFAULT 0 COMMENT '本次星级奖励',
  `attempt_no` int NOT NULL DEFAULT 1 COMMENT '该题第几次提交',
  `solve_time_ms` int DEFAULT NULL COMMENT '本次耗时(毫秒)',
  `solved_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_solved_at` (`user_id`, `solved_at`),
  INDEX `idx_user_level` (`user_id`, `level_id`)
) COMMENT='用户做题明细记录表';

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
  `location` varchar(50) DEFAULT '北京' COMMENT '发布地点',
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
  `company` varchar(100) DEFAULT NULL COMMENT '用户公司/学校信息',
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

-- 9. 历史代码表（用户保存的编译通过、AI测评通过的代码）
CREATE TABLE IF NOT EXISTS `t_code_snapshot` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `level_id` bigint NOT NULL COMMENT '关卡ID',
  `level_name` varchar(200) DEFAULT NULL COMMENT '关卡名称快照',
  `language` varchar(30) NOT NULL COMMENT '编程语言',
  `code` longtext NOT NULL COMMENT '源代码',
  `stdin_input` text DEFAULT NULL COMMENT '标准输入',
  `score` int NOT NULL DEFAULT 0 COMMENT 'AI评测分数(0-100)',
  `stars` tinyint NOT NULL DEFAULT 0 COMMENT '星级(0-3)',
  `compile_passed` tinyint NOT NULL DEFAULT 0 COMMENT '编译是否通过',
  `ai_eval_passed` tinyint NOT NULL DEFAULT 0 COMMENT 'AI评测是否通过(≥70分)',
  `ai_analysis` text DEFAULT NULL COMMENT 'AI评测分析摘要',
  `ai_correctness` varchar(500) DEFAULT NULL COMMENT '正确性评价',
  `ai_quality` varchar(500) DEFAULT NULL COMMENT '代码质量评价',
  `ai_efficiency` varchar(500) DEFAULT NULL COMMENT '效率评价',
  `ai_suggestions_json` text DEFAULT NULL COMMENT 'AI改进建议JSON',
  `run_output` text DEFAULT NULL COMMENT '运行输出',
  `is_best` tinyint NOT NULL DEFAULT 0 COMMENT '是否为该题最佳版本',
  `saved_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `t_user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`level_id`) REFERENCES `t_level`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_code_snapshot_user_level_best` (`user_id`, `level_id`, `is_best`),
  INDEX `idx_code_snapshot_user` (`user_id`),
  INDEX `idx_code_snapshot_level` (`level_id`),
  INDEX `idx_code_snapshot_user_level` (`user_id`, `level_id`),
  INDEX `idx_code_snapshot_saved_at` (`user_id`, `saved_at`)
) COMMENT='历史代码表';

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
(1, '-1'),
(1, '0'),
(1, '3'),
(1, '4'),
(1, '5'),
(1, '9'),
(1, '12'),
(2, 'O(n)'),
(2, 'O(n²)'),
(2, 'O(log n)'),
(2, 'O(n log n)'),
(3, 'O(1)'),
(3, 'O(n)'),
(3, 'O(log n)'),
(3, 'O(n²)'),
(4, 'O(1)'),
(4, 'O(n)'),
(4, 'O(n²)'),
(4, 'O(2^n)');

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
INSERT INTO `t_forum_post` (`author`, `author_level`, `avatar`, `topic`, `content`, `tag`, `likes`, `comments`, `location`) 
VALUES 
('算法爱好者', 'Lv.5', '/avatars/default.png', '如何高效学习动态规划？', '最近在学动态规划，感觉很难入门，有什么好的学习方法推荐吗？', '求助', 10, 3, '北京'),
('数据结构达人', 'Lv.8', '/avatars/default.png', '分享：红黑树详解', '花了一周时间整理的红黑树知识点，希望对大家有帮助！', '分享', 25, 5, '上海');

-- 插入论坛评论
INSERT INTO `t_forum_comment` (`post_id`, `user_id`, `author`, `avatar`, `author_level`, `content`, `likes`, `company`) 
VALUES 
(1, 1, 'zjh3029', '/avatars/default.png', 'Lv.3', '需要重点突出AI的能力，现在的岗位和AI强关联。有兴趣联系我', 5, '阿里巴巴集团 服务端研发'),
(1, NULL, '路人甲', '/avatars/default.png', 'Lv.2', '同问，我也在学动态规划', 2, '北京大学 计算机系'),
(1, NULL, '算法小白', '/avatars/default.png', 'Lv.1', '感谢分享，很有用！', 0, '清华大学 软件学院');

SELECT '数据库初始化完成！' AS message;
