-- phpMyAdmin SQL Dump
-- version 5.0.4
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2026-04-14 04:27:43
-- 服务器版本： 10.5.27-MariaDB-log
-- PHP 版本： 8.1.32

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `algomind_db`
--

-- --------------------------------------------------------

--
-- 表的结构 `course_tags`
--

CREATE TABLE `course_tags` (
  `course_id` bigint(20) NOT NULL,
  `tags` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `level_options`
--

CREATE TABLE `level_options` (
  `level_id` bigint(20) NOT NULL,
  `options` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `t_completed_error_item`
--

CREATE TABLE `t_completed_error_item` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `level_id` bigint(20) DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `level_type` varchar(50) DEFAULT NULL,
  `question` varchar(1000) DEFAULT NULL,
  `user_answer` varchar(500) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `correct_answer` varchar(500) DEFAULT NULL,
  `analysis_status` varchar(50) DEFAULT NULL,
  `analysis` varchar(2000) DEFAULT NULL,
  `source_error_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `last_error_at` datetime DEFAULT NULL,
  `completed_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 表的结构 `t_course`
--

CREATE TABLE `t_course` (
  `id` bigint(20) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `teacher` varchar(255) DEFAULT NULL,
  `track` varchar(255) DEFAULT NULL,
  `duration` varchar(255) DEFAULT NULL,
  `progress` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_course`
--

INSERT INTO `t_course` (`id`, `title`, `teacher`, `track`, `duration`, `progress`) VALUES
(1, '算法基础入门', '张老师', 'algo', '2小时', 30),
(2, '数据结构精讲', '李老师', 'ds', '3小时', 0),
(3, '竞赛算法进阶', '王老师', 'contest', '4小时', 0),
(4, '算法基础入门', '张老师', 'algo', '2小时', 30),
(5, '数据结构精讲', '李老师', 'ds', '3小时', 0),
(6, '竞赛算法进阶', '王老师', 'contest', '4小时', 0);

-- --------------------------------------------------------

--
-- 表的结构 `t_course_comment`
--

CREATE TABLE `t_course_comment` (
  `id` bigint(20) NOT NULL,
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(255) DEFAULT NULL,
  `user_avatar` varchar(255) DEFAULT NULL,
  `content` varchar(1000) NOT NULL COMMENT '评论内容',
  `likes` int(11) DEFAULT 0 COMMENT '点赞数',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID',
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程评论表';

-- --------------------------------------------------------

--
-- 表的结构 `t_course_tags`
--

CREATE TABLE `t_course_tags` (
  `course_id` bigint(20) NOT NULL,
  `tags` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程标签表';

--
-- 转存表中的数据 `t_course_tags`
--

INSERT INTO `t_course_tags` (`course_id`, `tags`) VALUES
(1, '基础'),
(1, '入门'),
(2, '数据结构'),
(2, '核心'),
(3, '竞赛'),
(3, '进阶');

-- --------------------------------------------------------

--
-- 表的结构 `t_error_item`
--

CREATE TABLE `t_error_item` (
  `id` bigint(20) NOT NULL,
  `level_id` bigint(20) DEFAULT NULL,
  `question` varchar(1000) NOT NULL,
  `user_answer` varchar(500) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `analysis_status` varchar(255) DEFAULT NULL,
  `analysis` varchar(2000) DEFAULT NULL,
  `correct_answer` varchar(500) DEFAULT NULL,
  `level_type` varchar(50) DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_error_item`
--

INSERT INTO `t_error_item` (`id`, `level_id`, `question`, `user_answer`, `description`, `created_at`, `analysis_status`, `analysis`, `correct_answer`, `level_type`, `title`, `updated_at`, `user_id`) VALUES
(3, 3, '快速排序的平均时间复杂度是？', 'O(n²)', '分治思想的典型代表。', '2026-04-08 11:24:02', '已分析', '', 'O(n log n)', 'single', '快速排序', '2026-04-10 07:36:08.000000', 4),
(5, 1, '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '0', '经典哈希表入门题。', '2026-04-08 13:53:23', '未分析', NULL, '哈希表', 'single', '两数之和', '2026-04-08 13:53:23.000000', 6),
(6, 3, '快速排序的平均时间复杂度是？', 'O(n²)', '分治思想的典型代表。', '2026-04-08 13:53:29', '已分析', '总结：这是一道需要回到题意和选项本身重新核对的题目。\n\n根因：对题意、边界或关键知识点的理解还不够稳定\n说明：建议先复盘题干，再对照自己的答案定位偏差点。\n\n建议：\n- 先做一次定向复盘：把正确答案、你的答案、错因三列写清楚。', 'O(n log n)', 'single', '快速排序', '2026-04-08 13:56:26.000000', 6),
(7, 4, '归并排序的额外空间复杂度通常是？', 'O(2^n)', '稳定排序，适合讲清楚分治流程。', '2026-04-08 13:53:35', '未分析', NULL, 'O(n)', 'single', '归并排序', '2026-04-08 13:53:35.000000', 6),
(8, 2, '在有序数组中查找目标值，二分查找的时间复杂度是？', 'O(n²)', '掌握边界写法比背模板更重要。', '2026-04-10 08:00:06', '已分析', '', 'O(log n)', 'single', '二分查找', '2026-04-10 08:01:30.000000', 4),
(11, 3, '快速排序的平均时间复杂度是？', 'O(n)', '分治思想的典型代表。', '2026-04-10 08:12:19', '已分析', '总结：这是一道需要回到题意和选项本身重新核对的题目。\n\n根因：对题意、边界或关键知识点的理解还不够稳定\n说明：建议先复盘题干，再对照自己的答案定位偏差点。\n\n建议：\n- 先做一次定向复盘：把正确答案、你的答案、错因三列写清楚。', 'O(n log n)', 'single', '快速排序', '2026-04-10 12:31:02.000000', 1),
(12, 10, '在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。', 'print(1)', '重点练边界，不只是背模板。', '2026-04-10 11:25:08', 'ANALYZED', '', '使用 while (left <= right) 的写法，注意 mid 和左右边界更新。', 'code', '实现二分查找', '2026-04-13 19:42:03.000000', 1),
(13, 4, '归并排序的额外空间复杂度通常是？', 'O(1)', '稳定排序，适合讲清楚分治流程。', '2026-04-10 12:23:30', '已分析', '总结：这是一道需要回到题意和选项本身重新核对的题目。\n\n根因：对题意、边界或关键知识点的理解还不够稳定\n说明：建议先复盘题干，再对照自己的答案定位偏差点。\n\n建议：\n- 先做一次定向复盘：把正确答案、你的答案、错因三列写清楚。', 'O(n)', 'single', '归并排序', '2026-04-10 12:24:37.000000', 1),
(14, 3, '快速排序的平均时间复杂度是？', 'O(n²)', '分治思想的典型代表。', '2026-04-10 15:12:28', '未分析', NULL, 'O(n log n)', 'single', '快速排序', '2026-04-10 15:13:32.000000', 8);

-- --------------------------------------------------------

--
-- 表的结构 `t_error_item_option_snapshot`
--

CREATE TABLE `t_error_item_option_snapshot` (
  `id` bigint(20) NOT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  `is_selected` bit(1) DEFAULT NULL,
  `option_content` varchar(1000) DEFAULT NULL,
  `option_key` varchar(10) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  `error_item_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_error_item_option_snapshot`
--

INSERT INTO `t_error_item_option_snapshot` (`id`, `is_correct`, `is_selected`, `option_content`, `option_key`, `sort_order`, `error_item_id`) VALUES
(12, b'0', b'0', '-1', 'A', 1, 5),
(13, b'0', b'1', '0', 'B', 2, 5),
(14, b'0', b'0', '3', 'C', 3, 5),
(15, b'0', b'0', '4', 'D', 4, 5),
(16, b'0', b'0', '5', 'E', 5, 5),
(17, b'0', b'0', '9', 'F', 6, 5),
(18, b'0', b'0', '12', 'G', 7, 5),
(19, b'0', b'0', 'O(1)', 'A', 1, 6),
(20, b'0', b'0', 'O(n)', 'B', 2, 6),
(21, b'0', b'0', 'O(log n)', 'C', 3, 6),
(22, b'0', b'1', 'O(n²)', 'D', 4, 6),
(23, b'0', b'0', 'O(1)', 'A', 1, 7),
(24, b'1', b'0', 'O(n)', 'B', 2, 7),
(25, b'0', b'0', 'O(n²)', 'C', 3, 7),
(26, b'0', b'1', 'O(2^n)', 'D', 4, 7),
(54, b'0', b'0', 'O(1)', 'A', 1, 3),
(55, b'0', b'0', 'O(n)', 'B', 2, 3),
(56, b'0', b'0', 'O(log n)', 'C', 3, 3),
(57, b'0', b'1', 'O(n²)', 'D', 4, 3),
(58, b'0', b'0', 'O(n)', 'A', 1, 8),
(59, b'0', b'1', 'O(n²)', 'B', 2, 8),
(60, b'1', b'0', 'O(log n)', 'C', 3, 8),
(61, b'0', b'0', 'O(n log n)', 'D', 4, 8),
(89, b'0', b'0', 'O(1)', 'A', 1, 11),
(90, b'0', b'1', 'O(n)', 'B', 2, 11),
(91, b'0', b'0', 'O(log n)', 'C', 3, 11),
(92, b'0', b'0', 'O(n²)', 'D', 4, 11),
(93, b'0', b'1', 'O(1)', 'A', 1, 13),
(94, b'1', b'0', 'O(n)', 'B', 2, 13),
(95, b'0', b'0', 'O(n²)', 'C', 3, 13),
(96, b'0', b'0', 'O(2^n)', 'D', 4, 13),
(129, b'0', b'0', 'O(1)', 'A', 1, 14),
(130, b'0', b'0', 'O(n)', 'B', 2, 14),
(131, b'0', b'0', 'O(log n)', 'C', 3, 14),
(132, b'0', b'1', 'O(n²)', 'D', 4, 14);

-- --------------------------------------------------------

--
-- 表的结构 `t_forum_comment`
--

CREATE TABLE `t_forum_comment` (
  `id` bigint(20) NOT NULL,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `author_level` varchar(255) DEFAULT NULL,
  `content` varchar(2000) NOT NULL,
  `likes` int(11) DEFAULT 0,
  `parent_id` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `company` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_forum_comment`
--

INSERT INTO `t_forum_comment` (`id`, `post_id`, `user_id`, `author`, `avatar`, `author_level`, `content`, `likes`, `parent_id`, `created_at`, `company`) VALUES
(3, 1, 4, 'aiaod', '/api/uploads/avatars/avatar_4_2a501be3.jpg', 'Lv.2', '刷题', 0, NULL, '2026-04-10 00:12:11', NULL),
(4, 4, 5, '一只爱刷题的熊', '/api/uploads/avatars/avatar_5_cc9712a4.jpg', 'Lv.1', '😭😭超级共情，dfs太难了', 0, NULL, '2026-04-10 00:36:50', NULL),
(5, 4, 6, 'Molic', '/api/uploads/avatars/avatar_6_78be5ceb.jpg', 'Lv.1', '最好是啊你', 0, NULL, '2026-04-10 02:00:43', NULL),
(6, 5, 4, 'aiaod', '/api/uploads/avatars/avatar_4_5712669e.jpg', 'Lv.2', '向大佬膜拜', 0, NULL, '2026-04-10 04:46:46', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_forum_post`
--

CREATE TABLE `t_forum_post` (
  `id` bigint(20) NOT NULL,
  `author` varchar(255) DEFAULT NULL,
  `author_level` varchar(255) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `topic` varchar(255) DEFAULT NULL,
  `content` varchar(2000) NOT NULL,
  `quote` varchar(255) DEFAULT NULL,
  `tag` varchar(255) DEFAULT NULL,
  `likes` int(11) DEFAULT 0,
  `comments` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `location` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_forum_post`
--

INSERT INTO `t_forum_post` (`id`, `author`, `author_level`, `avatar`, `topic`, `content`, `quote`, `tag`, `likes`, `comments`, `created_at`, `location`, `user_id`) VALUES
(4, 'xxxx', 'Lv.1', '/api/uploads/avatars/avatar_1_e4ae7c25.jpg', 'DFS太难了', 'DFS真的是算法入门最大的坎吧…代码就几行，逻辑绕到爆炸，递归栈执行顺序完全理不清，debug全靠蒙，提交全靠赌。明明回溯、剪枝的概念都懂，一到写题就卡壳，终止条件、回溯时机、剪枝逻辑全是坑。有没有大佬分享一下，到底怎么才能彻底搞懂DFS？求学习方法、刷题顺序，救救被递归折磨的孩子🙏😂', '', '# 学习交流', 1, 2, '2026-04-10 00:20:44', '未知', 1),
(5, 'Molic', 'Lv.1', '/api/uploads/avatars/avatar_6_78be5ceb.jpg', 'web网站开发太简单了啦', '一周多就搞出来了啊[惊讶]', '', '# 学习交流', 2, 1, '2026-04-10 02:00:26', '未知', 6);

-- --------------------------------------------------------

--
-- 表的结构 `t_learning_plan`
--

CREATE TABLE `t_learning_plan` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `track` varchar(255) DEFAULT NULL,
  `track_label` varchar(255) DEFAULT NULL,
  `weekly_goal` int(11) DEFAULT 10,
  `week_goals_json` text DEFAULT NULL,
  `daily_tasks_json` text DEFAULT NULL,
  `recommendations_json` text DEFAULT NULL,
  `is_ai_generated` bit(1) DEFAULT NULL,
  `ai_analysis` varchar(4000) DEFAULT NULL,
  `generated_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_learning_plan`
--

INSERT INTO `t_learning_plan` (`id`, `user_id`, `track`, `track_label`, `weekly_goal`, `week_goals_json`, `daily_tasks_json`, `recommendations_json`, `is_ai_generated`, `ai_analysis`, `generated_at`, `created_at`, `updated_at`) VALUES
(1, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"基础巩固练习\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"新知识点学习\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-07 15:21:21', '2026-04-07 15:21:21', '2026-04-07 15:21:21'),
(2, 4, 'algo', '算法思维赛道', 11, '[{\"progress\":0,\"id\":1,\"title\":\"基础巩固练习\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"新知识点学习\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-07 16:48:58', '2026-04-07 16:48:58', '2026-04-07 16:48:58'),
(3, 6, 'algo', '算法思维赛道', 11, '[{\"progress\":0,\"id\":1,\"title\":\"基础巩固练习\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"新知识点学习\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-08 13:06:16', '2026-04-08 13:06:16', '2026-04-08 13:06:16'),
(4, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 08:48:11', '2026-04-09 08:48:11', '2026-04-09 08:48:11'),
(5, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 08:55:10', '2026-04-09 08:55:10', '2026-04-09 08:55:10'),
(6, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 09:02:36', '2026-04-09 09:02:36', '2026-04-09 09:02:36'),
(7, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 09:40:00', '2026-04-09 09:40:00', '2026-04-09 09:40:00'),
(8, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 09:57:44', '2026-04-09 09:57:44', '2026-04-09 09:57:44'),
(9, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 10:30:27', '2026-04-09 10:30:27', '2026-04-09 10:30:27'),
(10, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-09 10:41:57', '2026-04-09 10:41:57', '2026-04-09 10:41:57'),
(11, 7, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：Dynamic Programming\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"Dynamic Programming 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 00:25:20', '2026-04-10 00:25:20', '2026-04-10 00:25:20'),
(12, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 00:25:28', '2026-04-10 00:25:28', '2026-04-10 00:25:28'),
(13, 7, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：Dynamic Programming\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"Dynamic Programming 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 00:26:29', '2026-04-10 00:26:29', '2026-04-10 00:26:29'),
(14, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"description\":\"覆盖数组、字符串、链表及single类薄弱知识点的入门关卡，达成每周既定目标\",\"id\":1,\"title\":\"完成10个算法关卡\",\"target\":10},{\"progress\":0,\"description\":\"包括哈希表统计、异或运算等基础解法，突破薄弱环节\",\"id\":2,\"title\":\"掌握3种single类知识点核心解题思路\",\"target\":3},{\"progress\":0,\"description\":\"培养学习连贯性，提升坚持指数\",\"id\":3,\"title\":\"实现连续3天打卡\",\"target\":3},{\"progress\":0,\"description\":\"记录擅长领域及薄弱点的解题框架、易错点，便于复习\",\"id\":4,\"title\":\"整理1份入门解题思路笔记\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":30,\"description\":\"掌握数组遍历、双指针等基础解题模板，巩固擅长领域\",\"title\":\"学习数组基础题型解题框架\",\"type\":\"learn\",\"priority\":\"high\"},{\"duration\":30,\"description\":\"应用所学框架完成题目，熟悉解题流程\",\"title\":\"练习2道数组入门关卡题目\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":20,\"description\":\"了解single类问题的核心特征，掌握哈希表统计的基础解法\",\"title\":\"学习single类知识点基础概念与入门解法\",\"type\":\"learn\",\"priority\":\"medium\"},{\"duration\":15,\"description\":\"记录数组题型框架及single类解法要点，便于后续复习\",\"title\":\"整理当天解题思路与学习笔记\",\"type\":\"review\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":25,\"description\":\"掌握字符串翻转、子串匹配等常用技巧，巩固擅长领域\",\"title\":\"学习字符串基础变形题型解法\",\"type\":\"learn\",\"priority\":\"high\"},{\"duration\":35,\"description\":\"结合擅长领域知识点，逐步提升解题熟练度\",\"title\":\"练习2道字符串+1道链表入门关卡题目\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":20,\"description\":\"应用哈希表解法完成题目，初步突破薄弱点\",\"title\":\"练习1道single类入门关卡题目\",\"type\":\"practice\",\"priority\":\"medium\"},{\"duration\":15,\"description\":\"复盘解题过程，总结single类题目易错点\",\"title\":\"复习当天解题及薄弱点思路\",\"type\":\"review\",\"priority\":\"medium\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":25,\"description\":\"掌握链表反转、快慢指针等技巧，深化擅长领域能力\",\"title\":\"学习链表进阶题型核心思路\",\"type\":\"learn\",\"priority\":\"high\"},{\"duration\":30,\"description\":\"综合应用擅长知识点，保持解题节奏\",\"title\":\"练习1道数组+1道链表入门关卡题目\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"description\":\"尝试用异或运算等新解法完成题目，强化薄弱点掌握\",\"title\":\"练习2道single类基础关卡题目\",\"type\":\"practice\",\"priority\":\"medium\"},{\"duration\":15,\"description\":\"整理3天的解题笔记，梳理擅长领域及薄弱点的知识框架\",\"title\":\"复盘前3天学习内容\",\"type\":\"review\",\"priority\":\"medium\"}]}]', '[{\"description\":\"针对single类知识点，从基础到进阶讲解哈希表、异或等解法，精准突破薄弱点\",\"source\":\"B站-代码随想录\",\"type\":\"video\",\"title\":\"算法入门：Single类问题核心解法精讲\",\"priority\":\"high\"},{\"description\":\"系统梳理擅长领域的核心解题模板，巩固基础提升解题效率\",\"source\":\"力扣官方题解\",\"type\":\"article\",\"title\":\"数组/字符串/链表入门题型解题框架总结\",\"priority\":\"high\"},{\"description\":\"包含5道入门级single类题目，针对性练习薄弱点，逐步建立解题信心\",\"source\":\"LeetCode\",\"type\":\"practice\",\"title\":\"LeetCode入门Single专题训练\",\"priority\":\"medium\"}]', b'1', '你属于算法思维赛道纯新手，累计解题量为0，无连续打卡记录，坚持指数处于中等水平（50/100）。擅长数组、字符串、链表基础题型，但对single类知识点掌握不足，暂无具体错题积累，需从入门级内容逐步推进，兼顾基础巩固与薄弱点突破，同时培养学习连贯性。', '2026-04-10 00:33:53', '2026-04-10 00:33:53', '2026-04-10 00:33:53'),
(15, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 04:45:57', '2026-04-10 04:45:57', '2026-04-10 04:45:57'),
(16, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 05:22:10', '2026-04-10 05:22:10', '2026-04-10 05:22:10'),
(17, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:01:17', '2026-04-10 06:01:17', '2026-04-10 06:01:17'),
(18, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:03:19', '2026-04-10 06:03:19', '2026-04-10 06:03:19'),
(19, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:06:45', '2026-04-10 06:06:45', '2026-04-10 06:06:45'),
(20, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:13:20', '2026-04-10 06:13:20', '2026-04-10 06:13:20'),
(21, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:30:53', '2026-04-10 06:30:53', '2026-04-10 06:30:53'),
(22, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:32:40', '2026-04-10 06:32:40', '2026-04-10 06:32:40'),
(23, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 06:34:51', '2026-04-10 06:34:51', '2026-04-10 06:34:51'),
(24, 4, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 07:19:53', '2026-04-10 07:19:53', '2026-04-10 07:19:53'),
(25, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：Dynamic Programming\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"Dynamic Programming 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：Dynamic Programming\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"Dynamic Programming 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"Dynamic Programming 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"Dynamic Programming 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:36:38', '2026-04-10 10:36:38', '2026-04-10 10:36:38'),
(26, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:39:02', '2026-04-10 10:39:02', '2026-04-10 10:39:02'),
(27, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:42:11', '2026-04-10 10:42:11', '2026-04-10 10:42:11'),
(28, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:43:09', '2026-04-10 10:43:09', '2026-04-10 10:43:09'),
(29, 2, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：动态规划\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"动态规划 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：动态规划\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"动态规划 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"动态规划 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"动态规划 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 10:48:17', '2026-04-10 10:48:17', '2026-04-10 10:48:17'),
(30, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 12:25:33', '2026-04-10 12:25:33', '2026-04-10 12:25:33'),
(31, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 12:33:18', '2026-04-10 12:33:18', '2026-04-10 12:33:18'),
(32, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 13:18:02', '2026-04-10 13:18:02', '2026-04-10 13:18:02');
INSERT INTO `t_learning_plan` (`id`, `user_id`, `track`, `track_label`, `weekly_goal`, `week_goals_json`, `daily_tasks_json`, `recommendations_json`, `is_ai_generated`, `ai_analysis`, `generated_at`, `created_at`, `updated_at`) VALUES
(33, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 13:18:10', '2026-04-10 13:18:10', '2026-04-10 13:18:10'),
(34, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-10 13:19:24', '2026-04-10 13:19:24', '2026-04-10 13:19:24'),
(35, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:06:24', '2026-04-12 13:06:24', '2026-04-12 13:06:24'),
(36, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:36:33', '2026-04-12 13:36:33', '2026-04-12 13:36:33'),
(37, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:46:32', '2026-04-12 13:46:32', '2026-04-12 13:46:32'),
(38, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:53:06', '2026-04-12 13:53:06', '2026-04-12 13:53:06'),
(39, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 13:53:18', '2026-04-12 13:53:18', '2026-04-12 13:53:18'),
(40, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-12 14:12:49', '2026-04-12 14:12:49', '2026-04-12 14:12:49'),
(41, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 11:40:40', '2026-04-13 11:40:40', '2026-04-13 11:40:40'),
(42, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 13:30:15', '2026-04-13 13:30:15', '2026-04-13 13:30:15'),
(43, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 14:46:20', '2026-04-13 14:46:20', '2026-04-13 14:46:20'),
(44, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 17:13:39', '2026-04-13 17:13:39', '2026-04-13 17:13:39'),
(45, 1, 'algo', '算法思维赛道', 10, '[{\"progress\":0,\"id\":1,\"title\":\"薄弱专项突破：single\",\"target\":4},{\"progress\":0,\"id\":2,\"title\":\"错题回顾与总结\",\"target\":3},{\"progress\":0,\"id\":3,\"title\":\"算法思维强化\",\"target\":3},{\"progress\":0,\"id\":4,\"title\":\"每日坚持打卡\",\"target\":1}]', '[{\"day\":\"今天\",\"tasks\":[{\"duration\":40,\"title\":\"single 专项练习\",\"type\":\"practice\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"错题回顾：single\",\"type\":\"review\",\"priority\":\"high\"},{\"duration\":30,\"title\":\"动态规划思想理解\",\"type\":\"learn\",\"priority\":\"medium\"}]},{\"day\":\"明天\",\"tasks\":[{\"duration\":30,\"title\":\"基础概念复习\",\"type\":\"review\",\"priority\":\"low\"},{\"duration\":25,\"title\":\"简单练习题\",\"type\":\"practice\",\"priority\":\"low\"}]},{\"day\":\"后天\",\"tasks\":[{\"duration\":30,\"title\":\"本周知识点总结\",\"type\":\"review\",\"priority\":\"medium\"},{\"duration\":40,\"title\":\"综合模拟测试\",\"type\":\"practice\",\"priority\":\"medium\"}]}]', '[{\"source\":\"针对薄弱点\",\"type\":\"video\",\"title\":\"single 专项讲解\",\"priority\":\"high\"},{\"source\":\"强化训练\",\"type\":\"practice\",\"title\":\"single 专项练习20题\",\"priority\":\"high\"},{\"source\":\"避免再犯\",\"type\":\"article\",\"title\":\"single 常见错误分析\",\"priority\":\"medium\"},{\"source\":\"基础巩固\",\"type\":\"video\",\"title\":\"动态规划入门讲解\",\"priority\":\"medium\"},{\"source\":\"必刷题集\",\"type\":\"practice\",\"title\":\"DP经典50题\",\"priority\":\"medium\"}]', b'0', NULL, '2026-04-13 17:39:06', '2026-04-13 17:39:06', '2026-04-13 17:39:06');

-- --------------------------------------------------------

--
-- 表的结构 `t_level`
--

CREATE TABLE `t_level` (
  `id` bigint(20) NOT NULL,
  `track` varchar(255) DEFAULT NULL,
  `sort_order` int(11) DEFAULT 0,
  `name` varchar(255) DEFAULT NULL,
  `is_unlocked` bit(1) DEFAULT NULL,
  `reward_points` int(11) DEFAULT 10,
  `type` varchar(255) DEFAULT NULL,
  `question` varchar(1000) NOT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `creator_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_level`
--

INSERT INTO `t_level` (`id`, `track`, `sort_order`, `name`, `is_unlocked`, `reward_points`, `type`, `question`, `answer`, `description`, `creator_id`) VALUES
(1, 'algo', 1, '两数之和', b'1', 10, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '哈希表', '经典哈希表入门题。', NULL),
(2, 'algo', 2, '二分查找', b'1', 10, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', 'O(log n)', '掌握边界写法比背模板更重要。', NULL),
(3, 'algo', 3, '快速排序', b'1', 15, 'single', '快速排序的平均时间复杂度是？', 'O(n log n)', '分治思想的典型代表。', NULL),
(4, 'algo', 4, '归并排序', b'1', 15, 'single', '归并排序的额外空间复杂度通常是？', 'O(n)', '稳定排序，适合讲清楚分治流程。', NULL),
(5, 'algo', 5, '动态规划入门', b'1', 20, 'single', '求斐波那契数列时，使用标准 DP 写法的时间复杂度是？', 'O(n)', '先定义状态，再找转移。', NULL),
(6, 'algo', 6, '爬楼梯问题', b'0', 20, 'single', '一次可以爬 1 或 2 阶，爬到第 n 阶的方案数，本质上接近哪类问题？', '斐波那契数列', '动态规划最常见的转化之一。', NULL),
(7, 'algo', 7, '最大子数组和', b'0', 25, 'single', 'Kadane 算法求最大子数组和的时间复杂度是？', 'O(n)', '理解“以当前位置结尾”的状态定义很关键。', NULL),
(8, 'algo', 8, '0-1 背包', b'0', 30, 'single', '0-1 背包的经典 DP 状态转移，时间复杂度通常写作？', 'O(nW)', 'n 是物品数，W 是背包容量。', NULL),
(9, 'algo', 9, '实现两数之和', b'0', 30, 'code', '给定 nums 和 target，返回两数之和对应的下标。要求时间复杂度 O(n)。', '用哈希表记录已经遍历过的数字与下标，边遍历边判断 target - nums[i] 是否出现过。', '把思路写成清晰的代码实现。', NULL),
(10, 'algo', 10, '实现二分查找', b'1', 25, 'code', '在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。', '使用 while (left <= right) 的写法，注意 mid 和左右边界更新。', '重点练边界，不只是背模板。', NULL),
(11, 'algo', 11, '实现斐波那契数列', b'1', 25, 'code', '实现一个函数，返回斐波那契数列第 n 项。要求使用迭代或 DP 思路。', '维护前两项或 DP 数组，自底向上迭代计算即可。', '适合作为 DP 代码题热身。', NULL),
(12, 'ds', 1, '数组基础', b'1', 10, 'single', '数组支持按下标随机访问，平均时间复杂度是？', 'O(1)', '数据结构基础题。', NULL),
(13, 'ds', 2, '链表插入', b'1', 10, 'single', '在单链表头部插入一个节点，时间复杂度通常是？', 'O(1)', '理解指针改动即可。', NULL),
(14, 'ds', 3, '栈的特性', b'0', 15, 'single', '栈最典型的访问顺序是？', '后进先出', 'LIFO 是核心关键词。', NULL),
(15, 'ds', 4, '队列的特性', b'0', 15, 'single', '队列最典型的访问顺序是？', '先进先出', 'FIFO 是核心关键词。', NULL),
(16, 'ds', 5, '二叉树遍历', b'0', 20, 'single', '二叉树前序遍历的顺序是？', '根左右', '先访问根，再递归左右子树。', NULL),
(17, 'ds', 6, '堆的性质', b'0', 25, 'single', '最大堆的堆顶元素满足什么性质？', '是当前堆中的最大值', '优先队列的常见底层结构。', NULL),
(18, 'ds', 7, '哈希表查询', b'0', 20, 'single', '哈希表平均查询时间复杂度通常是？', 'O(1)', '别忘了这是平均复杂度。', NULL),
(19, 'ds', 8, '平衡二叉树', b'0', 30, 'single', 'AVL 树保持平衡的关键条件通常描述为？', '左右子树高度差不超过 1', '理解“平衡”比记名词更重要。', NULL),
(20, 'ds', 9, '实现链表反转', b'0', 30, 'code', '反转单链表并返回新的头节点。要求额外空间复杂度 O(1)。', '使用 pre、cur、next 三个指针原地迭代反转。', '基础但很考察指针细节。', NULL),
(21, 'ds', 10, '实现前序遍历', b'0', 25, 'code', '返回二叉树的前序遍历结果，可以使用递归或显式栈。', '前序顺序为根、左、右；递归或栈模拟都可以。', '适合巩固树和栈的结合。', NULL),
(22, 'contest', 1, '滑动窗口', b'1', 15, 'single', '滑动窗口最适合解决哪类问题？', '连续子数组或子串问题', '看到“连续”通常就要敏感起来。', NULL),
(23, 'contest', 2, '前缀和', b'0', 15, 'single', '前缀和最常用于优化哪类计算？', '区间求和', '预处理后快速回答查询。', NULL),
(24, 'contest', 3, '单调栈', b'0', 20, 'single', '单调栈常见应用是？', '寻找下一个更大元素', '理解“维护单调性”比背题名更重要。', NULL),
(25, 'contest', 4, '并查集', b'0', 25, 'single', '并查集最擅长处理哪类问题？', '连通性', '动态合并集合时非常好用。', NULL),
(26, 'contest', 5, '线段树', b'0', 30, 'single', '线段树最常用于支持什么操作？', '区间查询与区间更新', '竞赛中非常常见的区间数据结构。', NULL),
(27, 'contest', 6, '字典树', b'0', 25, 'single', '字典树最适合处理什么场景？', '字符串前缀查询', '尤其适合批量单词前缀检索。', NULL),
(28, 'contest', 7, '状态压缩 DP', b'0', 35, 'single', '状态压缩 DP 通常使用什么表示状态？', '二进制位掩码', '当状态数量不大但组合很多时很常见。', NULL),
(29, 'contest', 8, '数位 DP', b'0', 35, 'single', '数位 DP 常用于解决哪类问题？', '满足条件的数字统计', '通常会和“按位枚举”一起出现。', NULL),
(30, 'contest', 9, '实现最长无重复子串', b'0', 35, 'code', '给定字符串 s，返回不含重复字符的最长子串长度。要求时间复杂度 O(n)。', '用滑动窗口维护当前无重复区间，并记录字符最近出现位置。', '竞赛与面试都很高频。', NULL),
(31, 'contest', 10, '实现三数之和', b'0', 40, 'code', '给定数组 nums，返回所有和为 0 的不重复三元组。', '先排序，再固定一个数，剩余部分使用双指针去重查找。', '双指针综合题。', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_level_options`
--

CREATE TABLE `t_level_options` (
  `level_id` bigint(20) NOT NULL,
  `options` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_level_options`
--

INSERT INTO `t_level_options` (`level_id`, `options`) VALUES
(1, '哈希表'),
(1, '暴力枚举'),
(1, '二分查找'),
(1, '二分查找'),
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
(4, 'O(2^n)'),
(1, '2'),
(1, '1'),
(1, '1');

-- --------------------------------------------------------

--
-- 表的结构 `t_question_attempt`
--

CREATE TABLE `t_question_attempt` (
  `id` bigint(20) NOT NULL,
  `first_submitted_at` datetime(6) DEFAULT NULL,
  `last_submitted_at` datetime(6) DEFAULT NULL,
  `latest_status` varchar(20) NOT NULL,
  `latest_user_answer` varchar(1000) DEFAULT NULL,
  `level_id` bigint(20) NOT NULL,
  `level_type` varchar(50) DEFAULT NULL,
  `question_content` varchar(1000) DEFAULT NULL,
  `question_title` varchar(200) DEFAULT NULL,
  `submit_count` int(11) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_question_attempt`
--

INSERT INTO `t_question_attempt` (`id`, `first_submitted_at`, `last_submitted_at`, `latest_status`, `latest_user_answer`, `level_id`, `level_type`, `question_content`, `question_title`, `submit_count`, `user_id`) VALUES
(1, '2026-04-08 11:05:21.000000', '2026-04-10 08:00:06.000000', 'WRONG', 'O(n²)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 6, 4),
(2, '2026-04-08 11:06:33.000000', '2026-04-08 11:06:33.000000', 'CORRECT', '用哈希表记录已经遍历过的数字与下标，边遍历边判断 target - nums[i] 是否出现过。', 9, 'code', '给定 nums 和 target，返回两数之和对应的下标。要求时间复杂度 O(n)。', '实现两数之和', 1, 4),
(3, '2026-04-08 11:22:28.000000', '2026-04-10 07:17:56.000000', 'CORRECT', '哈希表', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 4, 4),
(4, '2026-04-08 11:24:02.000000', '2026-04-10 06:31:09.000000', 'WRONG', 'O(n²)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 5, 4),
(5, '2026-04-08 11:29:42.000000', '2026-04-09 10:21:27.000000', 'CORRECT', '使用 while (left <= right) 的写法，注意 mid 和左右边界更新。', 10, 'code', '在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。', '实现二分查找', 5, 4),
(6, '2026-04-08 13:53:16.000000', '2026-04-08 13:53:16.000000', 'CORRECT', 'O(log n)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 1, 6),
(7, '2026-04-08 13:53:23.000000', '2026-04-08 13:53:23.000000', 'WRONG', '0', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 1, 6),
(8, '2026-04-08 13:53:29.000000', '2026-04-08 13:53:29.000000', 'WRONG', 'O(n²)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 1, 6),
(9, '2026-04-08 13:53:35.000000', '2026-04-08 13:53:35.000000', 'WRONG', 'O(2^n)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 6),
(10, '2026-04-08 13:55:53.000000', '2026-04-08 13:55:53.000000', 'CORRECT', '维护前两项或 DP 数组，自底向上迭代计算即可。', 11, 'code', '实现一个函数，返回斐波那契数列第 n 项。要求使用迭代或 DP 思路。', '实现斐波那契数列', 1, 6),
(11, '2026-04-10 06:31:18.000000', '2026-04-10 06:31:18.000000', 'CORRECT', 'O(n)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 4),
(12, '2026-04-10 08:06:58.000000', '2026-04-10 08:15:17.000000', 'CORRECT', '哈希表', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 5, 1),
(13, '2026-04-10 08:07:48.000000', '2026-04-13 13:42:16.000000', 'CORRECT', 'O(log n)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 4, 1),
(14, '2026-04-10 08:12:19.000000', '2026-04-10 12:23:25.000000', 'WRONG', 'O(n)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 5, 1),
(15, '2026-04-10 11:25:08.000000', '2026-04-13 11:53:18.000000', 'WRONG', 'print(1)', 10, 'code', '在有序数组 nums 中查找 target，不存在返回 -1。要求时间复杂度 O(log n)。', '实现二分查找', 20, 1),
(16, '2026-04-10 12:23:30.000000', '2026-04-10 12:23:30.000000', 'WRONG', 'O(1)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 1),
(17, '2026-04-10 15:11:36.000000', '2026-04-10 15:11:53.000000', 'CORRECT', '哈希表', 1, 'single', '给定整数数组 nums 和目标值 target，哪种思路能在 O(n) 时间内找到答案？', '两数之和', 2, 8),
(18, '2026-04-10 15:12:14.000000', '2026-04-10 15:12:14.000000', 'CORRECT', 'O(log n)', 2, 'single', '在有序数组中查找目标值，二分查找的时间复杂度是？', '二分查找', 1, 8),
(19, '2026-04-10 15:12:28.000000', '2026-04-10 15:13:32.000000', 'WRONG', 'O(n²)', 3, 'single', '快速排序的平均时间复杂度是？', '快速排序', 9, 8),
(20, '2026-04-10 15:14:16.000000', '2026-04-10 15:14:16.000000', 'CORRECT', 'O(n)', 4, 'single', '归并排序的额外空间复杂度通常是？', '归并排序', 1, 8);

-- --------------------------------------------------------

--
-- 表的结构 `t_user`
--

CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `points` int(11) DEFAULT 0,
  `target_track` varchar(255) DEFAULT NULL,
  `weekly_goal` int(11) DEFAULT 10,
  `avatar` varchar(500) DEFAULT NULL,
  `bio` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `github` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `status_emoji` varchar(255) DEFAULT NULL,
  `status_mood` varchar(255) DEFAULT NULL,
  `is_busy` bit(1) DEFAULT NULL,
  `busy_auto_reply` varchar(255) DEFAULT NULL,
  `busy_end_time` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `author_level_code` varchar(255) DEFAULT NULL,
  `author_level_updated_at` datetime(6) DEFAULT NULL,
  `author_score` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_user`
--

INSERT INTO `t_user` (`id`, `name`, `email`, `password`, `points`, `target_track`, `weekly_goal`, `avatar`, `bio`, `gender`, `github`, `website`, `status_emoji`, `status_mood`, `is_busy`, `busy_auto_reply`, `busy_end_time`, `created_at`, `updated_at`, `author_level_code`, `author_level_updated_at`, `author_score`) VALUES
(1, 'aiaod', 'admin@example.com', '$2a$10$O8UQPqD1iNfnQNvMIl9Mn.rmZ/jMadkqu/wabolilPPgRwdn3hCXS', 210, 'algo', 10, '/api/uploads/avatars/avatar_1_85bd22d6.jpg', 'Hello World', 'female', '', 'https://www.usloa.cn/profile', '🎯', '学习中', b'0', NULL, NULL, '2026-04-06 16:17:02', '2026-04-13 19:24:23', 'seed', '2026-04-10 11:09:38.000000', 70),
(2, 'Molic', '898@196.com', '$2a$10$50t0ZfYYx3p8eeZeZtfvTeEAQzuWLnHU5KJFg/hIdCNzzuOeTl8hW', 135, 'algo', 10, '/api/uploads/avatars/avatar_6_78be5ceb.jpg', 'Keep a steady pace.', 'unknown', '', '', NULL, NULL, NULL, NULL, NULL, '2026-04-08 12:39:50', '2026-04-10 15:54:21', NULL, NULL, NULL),
(3, '只想睡大觉', 'test@example.com', '$2a$10$/9ry2L0W1zsQcCJuyqPRx.D3QSDkEbnQPmOv0AaebEVMJYldesora', 100, 'algo', 10, '/api/uploads/avatars/avatar_1_e4ae7c25.jpg', 'Keep a steady pace.', 'unknown', '', '', '🎯', '学习中', b'0', NULL, NULL, '2026-04-06 16:11:39', '2026-04-10 15:54:21', NULL, NULL, NULL),
(4, '一只爱刷题的熊', 'aaa@qq.com', '$2a$10$FUsFAQLstBkWey5WqPQCPu1hYUu5P17bJz7lhHraMXeCW7iI8a/9q', 20, 'algo', 10, '/api/uploads/avatars/avatar_5_cc9712a4.jpg', 'Keep a steady pace.', 'unknown', '', '', NULL, NULL, NULL, NULL, NULL, '2026-04-08 05:17:48', '2026-04-10 15:54:21', NULL, NULL, NULL),
(5, 'Day', '3630714512@qq.com', '$2a$10$qUvVM9k6mRdid84OCphF..AcBix2HF.e3ZfsEWcVAjU0GnUpVLF.W', 120, 'algo', 10, '/api/uploads/avatars/avatar_7_c613d00f.jpg', '吃漂亮饭，睡觉', 'unknown', '', '', NULL, NULL, NULL, NULL, NULL, '2026-04-10 00:18:05', '2026-04-10 15:54:21', NULL, NULL, NULL),
(8, 'cqx', '110@qq.com', '$2a$10$HU4oxhd0yI8K2vqZ0ghr/uQZE6nw83hFgTwTA4dAKqUToea/F.8pa', 35, 'algo', 10, NULL, 'Keep a steady pace.', 'unknown', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-10 15:10:37', '2026-04-10 15:10:37', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `t_user_author_level_history`
--

CREATE TABLE `t_user_author_level_history` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `current_level_code` varchar(32) NOT NULL,
  `current_score` int(11) NOT NULL,
  `metrics_json` text DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `previous_level_code` varchar(32) DEFAULT NULL,
  `previous_score` int(11) NOT NULL,
  `trigger_ref_id` bigint(20) DEFAULT NULL,
  `trigger_type` varchar(50) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_user_author_level_history`
--

INSERT INTO `t_user_author_level_history` (`id`, `created_at`, `current_level_code`, `current_score`, `metrics_json`, `note`, `previous_level_code`, `previous_score`, `trigger_ref_id`, `trigger_type`, `user_id`) VALUES
(1, '2026-04-10 11:09:38.000000', 'seed', 70, '{\"total\":70,\"solving\":{\"label\":\"做题成长\",\"score\":52,\"tip\":\"做题数量、首答质量和正确率越稳定，成长越快。\",\"metrics\":{\"solvedCount\":2,\"firstTrySolved\":0,\"accuracyRate\":45,\"totalAttempts\":11}},\"posting\":{\"label\":\"发帖影响力\",\"score\":18,\"tip\":\"帖子数量和互动质量共同决定社区影响力。\",\"metrics\":{\"postCount\":1,\"receivedLikes\":1,\"receivedComments\":2,\"highImpactPosts\":0}},\"questionBank\":{\"label\":\"题库策展力\",\"score\":0,\"tip\":\"高质量题库越多人完成，你的作者等级提升越明显。\",\"metrics\":{\"publishedCount\":0,\"rewardPoints\":0,\"solvedCount\":0,\"distinctSolvers\":0,\"firstTrySolvers\":0}},\"rawTotal\":70}', '登录刷新作者等级', 'seed', 0, NULL, 'LOGIN', 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_user_post_like`
--

CREATE TABLE `t_user_post_like` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `post_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 转存表中的数据 `t_user_post_like`
--

INSERT INTO `t_user_post_like` (`id`, `created_at`, `post_id`, `user_id`) VALUES
(20, '2026-04-10 09:19:29.000000', 4, 2),
(21, '2026-04-10 09:20:58.000000', 5, 2),
(36, '2026-04-10 13:14:32.000000', 5, 1);

-- --------------------------------------------------------

--
-- 表的结构 `t_user_problem_record`
--

CREATE TABLE `t_user_problem_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `level_id` bigint(20) NOT NULL COMMENT '关卡ID',
  `level_name` varchar(200) DEFAULT NULL COMMENT '关卡名称',
  `track` varchar(20) DEFAULT NULL COMMENT '赛道',
  `is_correct` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否答对',
  `status` varchar(20) DEFAULT NULL COMMENT '作答状态',
  `stars` int(11) NOT NULL DEFAULT 0 COMMENT '星级',
  `attempt_no` int(11) NOT NULL DEFAULT 1 COMMENT '第几次尝试',
  `solve_time_ms` int(11) DEFAULT NULL COMMENT '耗时毫秒',
  `solved_at` datetime NOT NULL COMMENT '解题时间',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户题目答题记录表';

--
-- 转存表中的数据 `t_user_problem_record`
--

INSERT INTO `t_user_problem_record` (`id`, `user_id`, `level_id`, `level_name`, `track`, `is_correct`, `status`, `stars`, `attempt_no`, `solve_time_ms`, `solved_at`, `created_at`) VALUES
(1, 4, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 4, 4155, '2026-04-10 07:17:56', '2026-04-10 07:17:56'),
(2, 4, 2, '二分查找', 'algo', 1, 'CORRECT', 1, 5, 3059, '2026-04-10 07:21:11', '2026-04-10 07:21:11'),
(3, 4, 2, '二分查找', 'algo', 0, 'WRONG', 0, 6, 3337, '2026-04-10 08:00:06', '2026-04-10 08:00:06'),
(4, 1, 1, '两数之和', 'algo', 0, 'WRONG', 0, 1, 1762, '2026-04-10 08:06:58', '2026-04-10 08:06:58'),
(5, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 2, 1883, '2026-04-10 08:07:34', '2026-04-10 08:07:34'),
(6, 1, 2, '二分查找', 'algo', 0, 'WRONG', 0, 1, 5634, '2026-04-10 08:07:48', '2026-04-10 08:07:48'),
(7, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 3, 2550, '2026-04-10 08:08:10', '2026-04-10 08:08:10'),
(8, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 4, 2236, '2026-04-10 08:11:09', '2026-04-10 08:11:09'),
(9, 1, 2, '二分查找', 'algo', 1, 'CORRECT', 1, 2, 2178, '2026-04-10 08:12:13', '2026-04-10 08:12:14'),
(10, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 1, 1871, '2026-04-10 08:12:19', '2026-04-10 08:12:19'),
(11, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 2, 5251, '2026-04-10 08:12:22', '2026-04-10 08:12:22'),
(12, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 3, 8581, '2026-04-10 08:12:25', '2026-04-10 08:12:25'),
(13, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 4, 12324, '2026-04-10 08:12:29', '2026-04-10 08:12:29'),
(14, 1, 1, '两数之和', 'algo', 1, 'CORRECT', 1, 5, 2315, '2026-04-10 08:15:17', '2026-04-10 08:15:17'),
(15, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 1, 33360, '2026-04-10 11:25:08', '2026-04-10 11:25:08'),
(16, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 2, 49240, '2026-04-10 11:26:39', '2026-04-10 11:26:39'),
(17, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 3, 38996, '2026-04-10 11:29:23', '2026-04-10 11:29:23'),
(18, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 4, 232264, '2026-04-10 11:32:36', '2026-04-10 11:32:36'),
(19, 1, 3, '快速排序', 'algo', 0, 'WRONG', 0, 5, 2072, '2026-04-10 12:23:25', '2026-04-10 12:23:25'),
(20, 1, 4, '归并排序', 'algo', 0, 'WRONG', 0, 1, 1536, '2026-04-10 12:23:30', '2026-04-10 12:23:30'),
(21, 1, 2, '二分查找', 'algo', 1, 'CORRECT', 3, 3, 8270, '2026-04-10 12:39:28', '2026-04-10 12:39:28'),
(22, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 5, 58803, '2026-04-10 12:44:34', '2026-04-10 12:44:34'),
(23, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 6, 77967, '2026-04-10 13:12:28', '2026-04-10 13:12:28'),
(24, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 7, 35350, '2026-04-11 16:47:59', '2026-04-11 16:47:59'),
(25, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 8, 39403, '2026-04-11 17:55:30', '2026-04-11 17:55:30'),
(26, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 9, 37564, '2026-04-12 11:21:46', '2026-04-12 11:21:46'),
(27, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 10, 70853, '2026-04-12 11:22:19', '2026-04-12 11:22:19'),
(28, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 11, 98804, '2026-04-12 11:22:47', '2026-04-12 11:22:47'),
(29, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 12, 122994, '2026-04-12 11:23:11', '2026-04-12 11:23:11'),
(30, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 13, 52635, '2026-04-12 11:29:06', '2026-04-12 11:29:06'),
(31, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 14, 138456, '2026-04-12 11:30:32', '2026-04-12 11:30:32'),
(32, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 15, 52669, '2026-04-12 11:52:30', '2026-04-12 11:52:30'),
(33, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 16, 455193, '2026-04-12 11:59:13', '2026-04-12 11:59:13'),
(34, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 17, 67401, '2026-04-12 12:18:20', '2026-04-12 12:18:20'),
(35, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 18, 141007, '2026-04-12 12:19:34', '2026-04-12 12:19:34'),
(36, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 19, 47036, '2026-04-13 11:46:00', '2026-04-13 11:46:00'),
(37, 1, 10, '实现二分查找', 'algo', 0, 'WRONG', 0, 20, 57398, '2026-04-13 11:53:18', '2026-04-13 11:53:18');

--
-- 转储表的索引
--

--
-- 表的索引 `course_tags`
--
ALTER TABLE `course_tags`
  ADD KEY `FKp5efy36nmeh0845xdrrqh1gex` (`course_id`);

--
-- 表的索引 `level_options`
--
ALTER TABLE `level_options`
  ADD KEY `FK6hl9xqxytss729ge4t024jnxa` (`level_id`);

--
-- 表的索引 `t_completed_error_item`
--
ALTER TABLE `t_completed_error_item`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_completed_error_user_completed` (`user_id`,`completed_at`),
  ADD KEY `idx_completed_error_user_level` (`user_id`,`level_id`);

--
-- 表的索引 `t_course`
--
ALTER TABLE `t_course`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_course_comment`
--
ALTER TABLE `t_course_comment`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_course_tags`
--
ALTER TABLE `t_course_tags`
  ADD KEY `idx_course_id` (`course_id`);

--
-- 表的索引 `t_error_item`
--
ALTER TABLE `t_error_item`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_error_item_option_snapshot`
--
ALTER TABLE `t_error_item_option_snapshot`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKgi48l2at6hq4qnkm19m7qayis` (`error_item_id`);

--
-- 表的索引 `t_forum_comment`
--
ALTER TABLE `t_forum_comment`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_forum_post`
--
ALTER TABLE `t_forum_post`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_learning_plan`
--
ALTER TABLE `t_learning_plan`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_level`
--
ALTER TABLE `t_level`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `t_level_options`
--
ALTER TABLE `t_level_options`
  ADD KEY `idx_level_id` (`level_id`);

--
-- 表的索引 `t_question_attempt`
--
ALTER TABLE `t_question_attempt`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_question_attempt_user_level` (`user_id`,`level_id`);

--
-- 表的索引 `t_user`
--
ALTER TABLE `t_user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- 表的索引 `t_user_author_level_history`
--
ALTER TABLE `t_user_author_level_history`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_author_history_user_created` (`user_id`,`created_at`);

--
-- 表的索引 `t_user_post_like`
--
ALTER TABLE `t_user_post_like`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKg1dtdes05y2ybooaaymtrilca` (`user_id`,`post_id`);

--
-- 表的索引 `t_user_problem_record`
--
ALTER TABLE `t_user_problem_record`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_solved_at` (`user_id`,`solved_at`),
  ADD KEY `idx_user_level` (`user_id`,`level_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `t_completed_error_item`
--
ALTER TABLE `t_completed_error_item`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `t_course`
--
ALTER TABLE `t_course`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `t_course_comment`
--
ALTER TABLE `t_course_comment`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `t_error_item`
--
ALTER TABLE `t_error_item`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- 使用表AUTO_INCREMENT `t_error_item_option_snapshot`
--
ALTER TABLE `t_error_item_option_snapshot`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=133;

--
-- 使用表AUTO_INCREMENT `t_forum_comment`
--
ALTER TABLE `t_forum_comment`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `t_forum_post`
--
ALTER TABLE `t_forum_post`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- 使用表AUTO_INCREMENT `t_learning_plan`
--
ALTER TABLE `t_learning_plan`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- 使用表AUTO_INCREMENT `t_level`
--
ALTER TABLE `t_level`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- 使用表AUTO_INCREMENT `t_question_attempt`
--
ALTER TABLE `t_question_attempt`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- 使用表AUTO_INCREMENT `t_user`
--
ALTER TABLE `t_user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- 使用表AUTO_INCREMENT `t_user_author_level_history`
--
ALTER TABLE `t_user_author_level_history`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- 使用表AUTO_INCREMENT `t_user_post_like`
--
ALTER TABLE `t_user_post_like`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- 使用表AUTO_INCREMENT `t_user_problem_record`
--
ALTER TABLE `t_user_problem_record`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID', AUTO_INCREMENT=38;

--
-- 限制导出的表
--

--
-- 限制表 `course_tags`
--
ALTER TABLE `course_tags`
  ADD CONSTRAINT `FKp5efy36nmeh0845xdrrqh1gex` FOREIGN KEY (`course_id`) REFERENCES `t_course` (`id`);

--
-- 限制表 `level_options`
--
ALTER TABLE `level_options`
  ADD CONSTRAINT `FK6hl9xqxytss729ge4t024jnxa` FOREIGN KEY (`level_id`) REFERENCES `t_level` (`id`);

--
-- 限制表 `t_error_item_option_snapshot`
--
ALTER TABLE `t_error_item_option_snapshot`
  ADD CONSTRAINT `FKgi48l2at6hq4qnkm19m7qayis` FOREIGN KEY (`error_item_id`) REFERENCES `t_error_item` (`id`);

--
-- 限制表 `t_level_options`
--
ALTER TABLE `t_level_options`
  ADD CONSTRAINT `FKhxjbea8688ipuri7evmadokr6` FOREIGN KEY (`level_id`) REFERENCES `t_level` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
