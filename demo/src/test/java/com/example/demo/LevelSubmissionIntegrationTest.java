package com.example.demo;

import com.example.demo.entity.ErrorItem;
import com.example.demo.entity.Level;
import com.example.demo.entity.QuestionAttempt;
import com.example.demo.entity.User;
import com.example.demo.repository.ErrorItemRepository;
import com.example.demo.repository.LevelRepository;
import com.example.demo.repository.QuestionAttemptRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LevelSubmissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LevelRepository levelRepository;

    @Autowired
    private ErrorItemRepository errorItemRepository;

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

    private User testUser;
    private Level testLevel;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmailIgnoreCase("submit@example.com").orElseGet(User::new);
        testUser.setName("Submit User");
        testUser.setEmail("submit@example.com");
        testUser.setPassword(passwordEncoder.encode("123456"));
        testUser.setPoints(0);
        testUser.setBio("submit test");
        testUser.setGender("unknown");
        testUser.setTargetTrack("algo");
        testUser.setWeeklyGoal(10);
        if (testUser.getCreatedAt() == null) {
            testUser.setCreatedAt(OffsetDateTime.now());
        }
        testUser.setUpdatedAt(OffsetDateTime.now());
        testUser = userRepository.save(testUser);

        testLevel = new Level();
        testLevel.setTrack("algo");
        testLevel.setOrder(99);
        testLevel.setName("二分查找复杂度");
        testLevel.setIsUnlocked(true);
        testLevel.setRewardPoints(20);
        testLevel.setType("single");
        testLevel.setQuestion("在有序数组中查找目标值，二分查找的时间复杂度是？");
        testLevel.setOptions(List.of("O(n)", "O(log n)", "O(n log n)", "O(1)"));
        testLevel.setAnswer("B");
        testLevel.setDescription("提交接口测试题");
        testLevel = levelRepository.save(testLevel);
    }

    @Test
    void wrongAnswerCreatesLatestErrorSnapshot() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "levelId": %d,
                                  "answer": "A"
                                }
                                """.formatted(testLevel.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.correct").value(false))
                .andExpect(jsonPath("$.data.latestStatus").value("WRONG"));

        ErrorItem errorItem = errorItemRepository.findByUserIdAndLevelId(testUser.getId(), testLevel.getId()).orElseThrow();
        assertThat(errorItem.getQuestion()).isEqualTo(testLevel.getQuestion());
        assertThat(errorItem.getCorrectAnswer()).isEqualTo("B");
        assertThat(errorItem.getOptionSnapshots()).hasSize(4);
        assertThat(errorItem.getOptionSnapshots())
                .anySatisfy(snapshot -> {
                    assertThat(snapshot.getOptionKey()).isEqualTo("A");
                    assertThat(snapshot.getIsSelected()).isTrue();
                })
                .anySatisfy(snapshot -> {
                    assertThat(snapshot.getOptionKey()).isEqualTo("B");
                    assertThat(snapshot.getIsCorrect()).isTrue();
                });

        QuestionAttempt attempt = questionAttemptRepository.findByUserIdAndLevelId(testUser.getId(), testLevel.getId()).orElseThrow();
        assertThat(attempt.getLatestStatus()).isEqualTo("WRONG");
        assertThat(attempt.getSubmitCount()).isEqualTo(1);
    }

    @Test
    void correctAnswerClearsErrorSnapshotAndAddsPoints() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "levelId": %d,
                                  "answer": "A"
                                }
                                """.formatted(testLevel.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/submit")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "levelId": %d,
                                  "answer": "B"
                                }
                                """.formatted(testLevel.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.pointsEarned").value(20))
                .andExpect(jsonPath("$.data.latestStatus").value("CORRECT"));

        assertThat(errorItemRepository.findByUserIdAndLevelId(testUser.getId(), testLevel.getId())).isEmpty();

        QuestionAttempt attempt = questionAttemptRepository.findByUserIdAndLevelId(testUser.getId(), testLevel.getId()).orElseThrow();
        assertThat(attempt.getLatestStatus()).isEqualTo("CORRECT");
        assertThat(attempt.getSubmitCount()).isEqualTo(2);

        User refreshedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(refreshedUser.getPoints()).isEqualTo(20);
    }

    private String loginAndGetToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "submit@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode response = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }
}
