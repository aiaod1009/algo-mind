package com.example.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * 缓存配置 - 用于加速知识库数据访问
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 知识库目录缓存 - 5分钟过期
     * 适合数据变化不频繁但访问频繁的场景
     */
    @Bean("knowledgeCatalogCache")
    public CacheManager knowledgeCatalogCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("knowledgeCatalog");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats());
        return cacheManager;
    }

    /**
     * 知识库文章详情缓存 - 10分钟过期
     */
    @Bean("knowledgeArticleCache")
    public CacheManager knowledgeArticleCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("knowledgeArticle");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());
        return cacheManager;
    }

    /**
     * 通用缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10)));
        return cacheManager;
    }
}
