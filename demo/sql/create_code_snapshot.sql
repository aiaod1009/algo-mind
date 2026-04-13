-- ============================================
-- 历史代码表 - 在服务器 algomind_db 上直接执行
-- 适配 MariaDB 10.5
-- ============================================

USE `algomind_db`;

CREATE TABLE IF NOT EXISTS `t_code_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `level_id` bigint(20) NOT NULL COMMENT '关卡ID',
  `level_name` varchar(200) DEFAULT NULL COMMENT '关卡名称快照',
  `language` varchar(30) NOT NULL COMMENT '编程语言',
  `code` longtext NOT NULL COMMENT '源代码',
  `stdin_input` text DEFAULT NULL COMMENT '标准输入',
  `score` int(11) NOT NULL DEFAULT 0 COMMENT 'AI评测分数(0-100)',
  `stars` int(11) NOT NULL DEFAULT 0 COMMENT '星级(0-3)',
  `compile_passed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '编译是否通过',
  `ai_eval_passed` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'AI评测是否通过(>=70分)',
  `ai_analysis` text DEFAULT NULL COMMENT 'AI评测分析摘要',
  `ai_correctness` varchar(500) DEFAULT NULL COMMENT '正确性评价',
  `ai_quality` varchar(500) DEFAULT NULL COMMENT '代码质量评价',
  `ai_efficiency` varchar(500) DEFAULT NULL COMMENT '效率评价',
  `ai_suggestions_json` text DEFAULT NULL COMMENT 'AI改进建议JSON',
  `run_output` text DEFAULT NULL COMMENT '运行输出',
  `is_best` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为该题最佳版本',
  `saved_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_code_snapshot_user` (`user_id`),
  KEY `idx_code_snapshot_level` (`level_id`),
  KEY `idx_code_snapshot_user_level` (`user_id`, `level_id`),
  KEY `idx_code_snapshot_saved_at` (`user_id`, `saved_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='历史代码表';
