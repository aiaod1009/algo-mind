package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Comparator;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "file.upload-dir=target/test-uploads/avatars")
class AvatarControllerIntegrationTest {

    private static final Path TEST_UPLOAD_DIR = Path.of("target", "test-uploads", "avatars");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws IOException {
        cleanupUploadDir();

        User user = userRepository.findByEmailIgnoreCase("avatar@example.com")
                .orElseGet(User::new);
        user.setName("Avatar Tester");
        user.setEmail("avatar@example.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setPoints(0);
        user.setBio("avatar test user");
        user.setGender("unknown");
        user.setTargetTrack("algo");
        user.setWeeklyGoal(10);
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(OffsetDateTime.now());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanupUploadDir();
    }

    @Test
    void avatarFromUrlEndpointIsDisabled() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/users/me/avatar-from-url")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://example.com/avatar.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message", containsString("已关闭")));
    }

    @Test
    void uploadRejectsFakeImageContent() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "not-a-real-image".getBytes()
        );

        mockMvc.perform(multipart("/users/me/avatar")
                        .file(fakeImage)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message", containsString("图片")));
    }

    @Test
    void uploadAcceptsValidPngImage() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                createValidPng()
        );

        mockMvc.perform(multipart("/users/me/avatar")
                        .file(pngFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.avatarUrl", startsWith("/api/uploads/avatars/")));
    }

    @Test
    void controlledAvatarDownloadServesValidatedImage() throws Exception {
        String token = loginAndGetToken();
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                createValidPng()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/users/me/avatar")
                        .file(pngFile)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode uploadResponse = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        String avatarUrl = uploadResponse.path("data").path("avatarUrl").asText();
        String publicPath = avatarUrl.startsWith("/api") ? avatarUrl.substring(4) : avatarUrl;

        mockMvc.perform(get(publicPath))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Type", containsString("image/png")));
    }

    @Test
    void updateProfileRejectsExternalAvatarUrl() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Avatar Tester",
                                  "avatar": "https://example.com/avatar.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message", containsString("站内已上传图片")));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "avatar@example.com",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode response = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return response.path("data").path("token").asText();
    }

    private byte[] createValidPng() throws IOException {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }

    private void cleanupUploadDir() throws IOException {
        if (!Files.exists(TEST_UPLOAD_DIR)) {
            return;
        }

        try (var paths = Files.walk(TEST_UPLOAD_DIR)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}
