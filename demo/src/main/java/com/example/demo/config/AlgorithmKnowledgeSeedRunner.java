package com.example.demo.config;

import com.example.demo.entity.KnowledgeArticle;
import com.example.demo.repository.KnowledgeArticleRepository;
import com.example.demo.service.AlgorithmKnowledgeSeedFactory;
import com.example.demo.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "algo.knowledge.seed", name = "enabled", havingValue = "true")
public class AlgorithmKnowledgeSeedRunner implements CommandLineRunner {

    private final AlgorithmKnowledgeSeedFactory algorithmKnowledgeSeedFactory;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeArticleRepository knowledgeArticleRepository;

    @Value("${algo.knowledge.seed.operator-user-id:1}")
    private Long operatorUserId;

    @Value("${algo.knowledge.seed.replace-existing:false}")
    private boolean replaceExisting;

    @Override
    public void run(String... args) {
        List<KnowledgeBaseService.AdminArticleInput> seeds = algorithmKnowledgeSeedFactory.buildSeeds();
        List<String> seedSlugs = seeds.stream().map(KnowledgeBaseService.AdminArticleInput::slug).toList();

        log.info("Preparing to seed {} algorithm knowledge articles", seeds.size());
        try {
            if (replaceExisting) {
                List<KnowledgeArticle> existingSeedArticles = knowledgeArticleRepository.findAllBySlugIn(seedSlugs);
                if (!existingSeedArticles.isEmpty()) {
                    log.info("Removing {} existing seeded articles before refresh", existingSeedArticles.size());
                    knowledgeArticleRepository.deleteAllInBatch(existingSeedArticles);
                }
            }

            knowledgeBaseService.updateConfig(new KnowledgeBaseService.AdminConfigInput(
                    "AlgoMind 算法知识库",
                    "系统化覆盖 60 个高频算法主题，支持后台持续维护、运营配置和批量导入。",
                    "没有找到匹配的算法主题",
                    "可以尝试搜索“动态规划”“最短路径”“KMP”“线段树”等关键词，或者从左侧分类开始浏览。",
                    seeds.getFirst().slug(),
                    List.of("数组", "动态规划", "图论", "排序", "字符串", "回溯")
            ));

            KnowledgeBaseService.BatchUpsertResult result = knowledgeBaseService.batchUpsertArticles(seeds, operatorUserId);
            log.info(
                    "Algorithm knowledge seeding finished: total={}, inserted={}, updated={}",
                    result.total(),
                    result.inserted(),
                    result.updated()
            );
        } catch (Exception exception) {
            log.error("Failed to seed algorithm knowledge articles", exception);
            throw exception;
        }
    }
}
