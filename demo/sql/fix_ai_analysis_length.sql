-- ============================================
-- 修复 ai_analysis 字段长度不足的问题
-- 将 text 改为 longtext 以支持更长的 AI 分析内容
-- ============================================

USE `algomind_db`;

-- 修改 ai_analysis 字段为 longtext
ALTER TABLE `t_code_snapshot` MODIFY COLUMN `ai_analysis` LONGTEXT DEFAULT NULL COMMENT 'AI评测分析摘要';

-- 同时建议将其他可能较长的 text 字段也改为 longtext
ALTER TABLE `t_code_snapshot` MODIFY COLUMN `ai_suggestions_json` LONGTEXT DEFAULT NULL COMMENT 'AI改进建议JSON';
ALTER TABLE `t_code_snapshot` MODIFY COLUMN `run_output` LONGTEXT DEFAULT NULL COMMENT '运行输出';
ALTER TABLE `t_code_snapshot` MODIFY COLUMN `stdin_input` LONGTEXT DEFAULT NULL COMMENT '标准输入';

SELECT '字段长度修改完成' AS message;
