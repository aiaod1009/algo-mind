ALTER TABLE `t_user`
  ADD COLUMN IF NOT EXISTS `author_score` int DEFAULT 0 COMMENT '作者成长值',
  ADD COLUMN IF NOT EXISTS `author_level_code` varchar(32) DEFAULT 'seed' COMMENT '作者等级编码',
  ADD COLUMN IF NOT EXISTS `author_level_updated_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '作者等级更新时间';

CREATE TABLE IF NOT EXISTS `t_user_author_level_history` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `trigger_type` varchar(50) NOT NULL COMMENT '触发动作',
  `trigger_ref_id` bigint DEFAULT NULL COMMENT '触发对象ID',
  `previous_level_code` varchar(32) DEFAULT NULL COMMENT '变更前等级',
  `current_level_code` varchar(32) NOT NULL COMMENT '变更后等级',
  `previous_score` int DEFAULT 0 COMMENT '变更前成长值',
  `current_score` int DEFAULT 0 COMMENT '变更后成长值',
  `note` varchar(255) DEFAULT NULL COMMENT '变更备注',
  `metrics_json` text DEFAULT NULL COMMENT '成长指标快照',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  INDEX `idx_author_history_user_created` (`user_id`, `created_at`)
) COMMENT='作者等级变更历史';

ALTER TABLE `t_forum_post`
  ADD COLUMN IF NOT EXISTS `user_id` bigint DEFAULT NULL COMMENT '用户ID';
