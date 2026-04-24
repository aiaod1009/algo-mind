-- ============================================
-- 知识库性能优化索引
-- 针对知识专区加载慢的问题进行数据库层面优化
-- ============================================

-- 1. 添加全文搜索索引（MySQL 5.6+ 支持）
-- 用于加速标题、摘要、标签等字段的关键词搜索
ALTER TABLE t_knowledge_article 
ADD FULLTEXT INDEX idx_fulltext_search (title, summary, lead, tags_text, badge);

-- 2. 添加栏目+排序+时间的复合索引（目录查询优化）
-- 这是最常用的查询模式，用于目录展示
ALTER TABLE t_knowledge_article 
ADD INDEX idx_section_sort_updated (section_id, sort_order, updated_at);

-- 3. 添加发布状态+栏目+排序的复合索引（已发布文章查询优化）
ALTER TABLE t_knowledge_article 
ADD INDEX idx_published_section_sort (published, section_id, sort_order);

-- 4. 添加slug索引（文章详情查询优化）
ALTER TABLE t_knowledge_article 
ADD INDEX idx_slug_published (slug, published);

-- 5. 添加推荐位索引（首页推荐卡片查询优化）
ALTER TABLE t_knowledge_article 
ADD INDEX idx_spotlight_published (spotlight_enabled, published, sort_order);

-- 6. 添加英文标题索引（支持英文搜索）
ALTER TABLE t_knowledge_article 
ADD INDEX idx_english_title (english_title);

-- ============================================
-- 查询优化说明：
-- 
-- 1. 全文索引 idx_fulltext_search:
--    - 支持 MATCH AGAINST 语法进行高效全文搜索
--    - 比 LIKE '%keyword%' 快 10-100 倍
--    - 适用于标题、摘要、标签的模糊搜索
--
-- 2. 复合索引 idx_section_sort_updated:
--    - 优化目录查询 ORDER BY section_id, sort_order, updated_at
--    - 避免 filesort，提高排序性能
--
-- 3. 复合索引 idx_published_section_sort:
--    - 优化 WHERE published=true 的查询
--    - 支持目录展示时的过滤和排序
--
-- 4. 索引 idx_slug_published:
--    - 优化文章详情页查询
--    - slug 已经是 UNIQUE，但添加 published 可以优化联合查询
--
-- 5. 索引 idx_spotlight_published:
--    - 优化首页推荐位查询
--    - WHERE spotlight_enabled=1 AND published=1
--
-- ============================================

-- 验证索引创建成功
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    CARDINALITY,
    INDEX_TYPE
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 't_knowledge_article'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
