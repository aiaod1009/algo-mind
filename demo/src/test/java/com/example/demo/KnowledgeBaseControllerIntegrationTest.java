package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class KnowledgeBaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        User admin = userRepository.findByEmailIgnoreCase("admin@example.com")
                .orElseGet(User::new);
        admin.setName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setIsAdmin(true);
        admin.setPoints(999);
        admin.setBio("Knowledge admin");
        admin.setGender("unknown");
        admin.setTargetTrack("algo");
        admin.setWeeklyGoal(10);
        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(OffsetDateTime.now());
        }
        admin.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(admin);
    }

    @Test
    void adminCrudFlowPersistsPublishedArticle() throws Exception {
        String token = loginAsAdmin();

        MvcResult createResult = mockMvc.perform(post("/knowledge-base/admin/articles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "it-graph-shortest-path",
                                  "title": "图最短路",
                                  "englishTitle": "Shortest Path",
                                  "sectionId": "graph",
                                  "sectionTitle": "图论",
                                  "summary": "用于验证知识库 CRUD。",
                                  "lead": "先建图，再按边权选算法。",
                                  "readTime": "8 min",
                                  "tags": ["图论", "最短路"],
                                  "learningObjectives": ["区分 BFS、Dijkstra、Bellman-Ford"],
                                  "strategySteps": [
                                    { "index": "01", "title": "看边权", "description": "先判断边权是否非负", "badge": "建模" }
                                  ],
                                  "insights": [
                                    { "title": "面试表达", "description": "先说选型，再补复杂度。", "accent": "emerald" }
                                  ],
                                  "codeBlocks": [
                                    { "language": "Java", "title": "Dijkstra 模板", "code": "void solve() {}", "callouts": ["优先队列"] }
                                  ],
                                  "checklist": ["能解释负权边为什么不能直接用 Dijkstra"],
                                  "spotlightEnabled": true,
                                  "spotlightTitle": "图论高频入口",
                                  "spotlightDescription": "复习最短路判型。",
                                  "spotlightAccent": "cyan",
                                  "published": true,
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.slug").value("it-graph-shortest-path"))
                .andReturn();

        Long articleId = readDataId(createResult);

        mockMvc.perform(get("/knowledge-base/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.sections[*].articles[*].slug").value(org.hamcrest.Matchers.hasItem("it-graph-shortest-path")));

        mockMvc.perform(put("/knowledge-base/admin/articles/{id}", articleId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "it-graph-shortest-path",
                                  "title": "图最短路（更新）",
                                  "englishTitle": "Shortest Path",
                                  "sectionId": "graph",
                                  "sectionTitle": "图论",
                                  "summary": "更新标题并转成草稿。",
                                  "lead": "验证更新后前台立即生效。",
                                  "readTime": "9 min",
                                  "tags": ["图论", "最短路"],
                                  "learningObjectives": ["验证更新"],
                                  "strategySteps": [
                                    { "index": "01", "title": "判定算法", "description": "按边权和图类型选择算法", "badge": "更新" }
                                  ],
                                  "insights": [
                                    { "title": "追问", "description": "对比不同最短路算法。", "accent": "amber" }
                                  ],
                                  "codeBlocks": [
                                    { "language": "Java", "title": "更新模板", "code": "void solveUpdated() {}", "callouts": ["更新"] }
                                  ],
                                  "checklist": ["验证草稿不会出现在前台"],
                                  "spotlightEnabled": false,
                                  "published": false,
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("图最短路（更新）"))
                .andExpect(jsonPath("$.data.published").value(false));

        mockMvc.perform(get("/knowledge-base/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[*].articles[*].slug").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem("it-graph-shortest-path"))));

        mockMvc.perform(delete("/knowledge-base/admin/articles/{id}", articleId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/knowledge-base/admin/dashboard")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.articles[*].id").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasItem(articleId.intValue()))));
    }

    @Test
    void deletingArticleRepairsDefaultSlugAndRelatedReferences() throws Exception {
        String token = loginAsAdmin();

        Long firstId = readDataId(mockMvc.perform(post("/knowledge-base/admin/articles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "it-dp-basics",
                                  "title": "动态规划入门",
                                  "sectionId": "dp",
                                  "sectionTitle": "动态规划",
                                  "summary": "默认文章",
                                  "lead": "会被另一篇文章引用。",
                                  "published": true,
                                  "sortOrder": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());

        Long secondId = readDataId(mockMvc.perform(post("/knowledge-base/admin/articles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "it-dp-advanced",
                                  "title": "动态规划进阶",
                                  "sectionId": "dp",
                                  "sectionTitle": "动态规划",
                                  "summary": "引用默认文章",
                                  "lead": "验证删除修复逻辑。",
                                  "relatedArticleSlugs": ["it-dp-basics"],
                                  "published": true,
                                  "sortOrder": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn());

        mockMvc.perform(put("/knowledge-base/admin/config")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "siteTitle": "AlgoMind 知识专区",
                                  "siteSubtitle": "integration test",
                                  "emptyStateTitle": "empty",
                                  "emptyStateDescription": "empty",
                                  "defaultArticleSlug": "it-dp-basics",
                                  "quickSearches": ["动态规划"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(delete("/knowledge-base/admin/articles/{id}", firstId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/knowledge-base/admin/articles/{id}", secondId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.relatedArticleSlugs").isEmpty());

        mockMvc.perform(get("/knowledge-base/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.defaultArticleSlug").value(org.hamcrest.Matchers.not("it-dp-basics")));
    }

    @Test
    void invalidKnowledgePayloadIsRejected() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/knowledge-base/admin/articles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "invalid-spotlight",
                                  "title": "推荐位校验",
                                  "sectionId": "validation",
                                  "sectionTitle": "校验",
                                  "spotlightEnabled": true,
                                  "spotlightTitle": "",
                                  "spotlightDescription": "",
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));

        mockMvc.perform(put("/knowledge-base/admin/config")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "siteTitle": "AlgoMind",
                                  "defaultArticleSlug": "not-exists",
                                  "quickSearches": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));
    }

    private String loginAsAdmin() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode response = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }

    private Long readDataId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("data").path("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
