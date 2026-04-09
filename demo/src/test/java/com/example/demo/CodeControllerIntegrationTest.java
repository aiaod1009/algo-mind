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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CodeControllerIntegrationTest {

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
        User user = userRepository.findByEmailIgnoreCase("coder@example.com")
                .orElseGet(User::new);
        user.setName("Coder");
        user.setEmail("coder@example.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPoints(0);
        user.setBio("code runner test user");
        user.setGender("unknown");
        user.setTargetTrack("algo");
        user.setWeeklyGoal(10);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(OffsetDateTime.now());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @Test
    void javaMainTemplateRunsSuccessfully() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/run-code")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "language": "java",
                                  "code": "public class Main { public static void main(String[] args) { System.out.println(\\"Hello from Main\\"); } }"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.output").value("Hello from Main"))
                .andExpect(jsonPath("$.data.error").value(""));
    }

    @Test
    void javaRunCodeAcceptsStandardInput() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/run-code")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "language": "java",
                                  "stdinInput": "7 8\\n",
                                  "code": "import java.util.Scanner; public class Main { public static void main(String[] args) { Scanner scanner = new Scanner(System.in); int a = scanner.nextInt(); int b = scanner.nextInt(); System.out.println(a + b); } }"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.output").value("15"))
                .andExpect(jsonPath("$.data.error").value(""));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "coder@example.com",
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
