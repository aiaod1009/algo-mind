-- 消息和通知表
CREATE TABLE IF NOT EXISTS `t_message` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收消息的用户ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID，如果是系统通知则为NULL',
  `type` varchar(20) NOT NULL COMMENT '消息类型: sys-notice, likes, comments, follows, chat',
  `title` varchar(100) DEFAULT NULL COMMENT '消息标题(系统通知)',
  `content` varchar(1000) NOT NULL COMMENT '消息内容',
  `is_read` tinyint DEFAULT 0 COMMENT '是否已读',
  `related_id` bigint DEFAULT NULL COMMENT '关联的实体ID(如帖子、评论的ID)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP
) COMMENT='消息通知表';

-- 插入一些测试数据（给id为1的用户发消息）
INSERT INTO `t_message` (`user_id`, `sender_id`, `type`, `title`, `content`, `is_read`, `created_at`) VALUES
(1, NULL, 'sys-notice', '算法挑战赛获奖通知', '恭喜你在上周的「春季算法挑战赛」中获得第 3 名！你的积分奖励已经发放，请前往个人中心查看。', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, NULL, 'sys-notice', '系统升级维护通知', '尊敬的用户，为了提供更好的体验，我们将于本周五凌晨 02:00 - 04:00 进行系统升级维护。期间部分功能可能无法使用，给您带来的不便敬请谅解。', 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 2, 'chat', NULL, '你的简历上写 AI了吗 ❓', 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 3, 'chat', NULL, '还没有收到小红花，要相信好人有好报', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));
