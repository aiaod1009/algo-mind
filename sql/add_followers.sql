ALTER TABLE `t_user`
ADD COLUMN `followers_count` int DEFAULT 0 COMMENT '粉丝数量',
ADD COLUMN `following_count` int DEFAULT 0 COMMENT '关注数量';

-- 并且可选增加一个关注表
CREATE TABLE IF NOT EXISTS `t_user_follow` (
    `id` bigint PRIMARY KEY AUTO_INCREMENT,
    `follower_id` bigint NOT NULL COMMENT '关注者ID（粉丝）',
    `following_id` bigint NOT NULL COMMENT '被关注者ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_follow` (`follower_id`, `following_id`)
) COMMENT='用户关注关系表';
