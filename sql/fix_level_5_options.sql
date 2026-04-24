-- 修复 level_id=5 (岛屿数量与最大面积) 缺失的选项数据
-- 执行此 SQL 前请先备份数据库

-- 1. 添加缺失的选项数据
INSERT INTO t_level_options (level_id, options) VALUES 
(5, 'O(n)'),
(5, 'O(n²)'),
(5, 'O(n*m)'),
(5, 'O(n+m)');

-- 2. (可选) 如果需要修正题目内容，取消下面的注释
-- 当前题目内容是关于斐波那契数列的，与标题"岛屿数量与最大面积"不匹配
-- 以下是建议的正确题目内容：

-- UPDATE t_level SET 
--   question = '在一个二维网格中，计算岛屿的数量和最大岛屿面积，最优解的时间复杂度通常是？',
--   answer = 'O(n*m)',
--   description = 'DFS/BFS 遍历每个格子，每个格子最多访问一次。'
-- WHERE id = 5;
