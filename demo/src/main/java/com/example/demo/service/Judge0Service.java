package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class Judge0Service {

    private static final Logger log = LoggerFactory.getLogger(Judge0Service.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final long TIMEOUT_MS = 60_000L;
    private static final int MAX_POLL_ATTEMPTS = 30;
    private static final int POLL_INTERVAL_MS = 500;

    @Value("${judge0.api.url:http://43.139.148.97:2358}")
    private String judge0BaseUrl;

    public Map<String, Object> execute(String language, String sourceCode, String stdinInput) {
        Integer languageId = LANGUAGE_ID_MAP.get(language.toLowerCase());
        if (languageId == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        try {
            String token = submit(sourceCode, languageId, stdinInput);
            return pollForResult(token);
        } catch (Exception e) {
            log.error("Judge0 execution failed", e);
            throw new RuntimeException("Code execution failed: " + e.getMessage(), e);
        }
    }

    private String submit(String sourceCode, int languageId, String stdinInput) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "source_code", sourceCode,
                "language_id", languageId,
                "stdin", stdinInput != null ? stdinInput : "",
                "cpu_time_limit", 5,
                "memory_limit", 128000,
                "wall_time_limit", 10
        );

        String jsonBody = MAPPER.writeValueAsString(requestBody);
        String url = judge0BaseUrl + "/submissions?base64_encoded=false&wait=false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new RuntimeException("Judge0 submission failed: " + response.statusCode() + " - " + response.body());
        }

        JsonNode node = MAPPER.readTree(response.body());
        return node.get("token").asText();
    }

    private Map<String, Object> pollForResult(String token) throws Exception {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            Map<String, Object> result = getResult(token);
            int statusId = (int) result.getOrDefault("status_id", 1);

            if (statusId > 2) {
                return result;
            }

            Thread.sleep(POLL_INTERVAL_MS);
        }

        throw new RuntimeException("Judge0 execution timeout");
    }

    private Map<String, Object> getResult(String token) throws Exception {
        String url = judge0BaseUrl + "/submissions/" + token + "?base64_encoded=false&fields=stdout,stderr,compile_output,status_id,memory,time,language_id";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Judge0 result fetch failed: " + response.statusCode());
        }

        return MAPPER.readValue(response.body(), Map.class);
    }

    public Map<String, Object> getStatuses() {
        try {
            String url = judge0BaseUrl + "/statuses";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return MAPPER.readValue(response.body(), Map.class);
        } catch (Exception e) {
            log.error("Failed to get statuses", e);
            return Map.of();
        }
    }

    private static final Map<String, Integer> LANGUAGE_ID_MAP = Map.ofEntries(
            Map.entry("c", 50),
            Map.entry("cpp", 54),
            Map.entry("java", 62),
            Map.entry("python", 71),
            Map.entry("js", 63),
            Map.entry("javascript", 63),
            Map.entry("go", 60),
            Map.entry("rust", 73),
            Map.entry("ruby", 72),
            Map.entry("php", 68),
            Map.entry("csharp", 51),
            Map.entry("kotlin", 78),
            Map.entry("swift", 83),
            Map.entry("typescript", 74)
    );
}
