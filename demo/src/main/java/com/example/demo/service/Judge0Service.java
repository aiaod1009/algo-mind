package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Judge0Service {

    private static final Logger log = LoggerFactory.getLogger(Judge0Service.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final int MAX_POLL_ATTEMPTS = 30;
    private static final int POLL_INTERVAL_MS = 500;

    private static final String C_HELPER_PREFIX = """
            #include <stdio.h>
            #include <stdlib.h>
            #include <string.h>
            """;

    private static final String CPP_HELPER_PREFIX = """
            #include <iostream>
            #include <cstdio>
            """;

    private static final int C_HELPER_LINES = 3;
    private static final int CPP_HELPER_LINES = 3;

    private static final long WORKER_COOLDOWN_MS = 30_000L;
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    @Value("${judge0.api.url:http://localhost:2358}")
    private String judge0BaseUrl;

    @Value("${judge0.api.urls:}")
    private String judge0UrlsConfig;

    @Value("${judge0.api.health-check-enabled:true}")
    private boolean healthCheckEnabled;

    private final List<WorkerNode> workers = new CopyOnWriteArrayList<>();
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    @PostConstruct
    void init() {
        if (judge0UrlsConfig != null && !judge0UrlsConfig.isBlank()) {
            String[] urls = judge0UrlsConfig.split(",");
            for (String url : urls) {
                String trimmed = url.trim();
                if (!trimmed.isEmpty()) {
                    workers.add(new WorkerNode(trimmed));
                    log.info("Registered Judge0 worker: {}", trimmed);
                }
            }
        }

        if (workers.isEmpty() && judge0BaseUrl != null && !judge0BaseUrl.isBlank()) {
            workers.add(new WorkerNode(judge0BaseUrl.trim()));
            log.info("Single Judge0 worker from judge0.api.url: {}", judge0BaseUrl);
        }

        log.info("Judge0 cluster initialized with {} worker(s)", workers.size());
    }

    public Map<String, Object> execute(String language, String sourceCode, String stdinInput) {
        Integer languageId = LANGUAGE_ID_MAP.get(language.toLowerCase());
        if (languageId == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        String wrappedCode = wrapCode(language, sourceCode);

        if (workers.size() == 1) {
            return executeOnWorker(workers.get(0), languageId, wrappedCode, stdinInput, language);
        }

        return executeWithFailover(languageId, wrappedCode, stdinInput, language);
    }

    private Map<String, Object> executeWithFailover(int languageId, String sourceCode, String stdinInput, String language) {
        int attempts = 0;
        int maxAttempts = workers.size();

        while (attempts < maxAttempts) {
            WorkerNode worker = selectWorker();
            if (worker == null) {
                throw new RuntimeException("No available Judge0 worker");
            }

            try {
                Map<String, Object> result = executeOnWorker(worker, languageId, sourceCode, stdinInput, language);
                worker.recordSuccess();
                return result;
            } catch (Exception e) {
                worker.recordFailure();
                log.warn("Judge0 worker {} failed (consecutive failures: {}): {}",
                        worker.url, worker.consecutiveFailures, e.getMessage());
                attempts++;
            }
        }

        throw new RuntimeException("All Judge0 workers failed after " + maxAttempts + " attempts");
    }

    private WorkerNode selectWorker() {
        List<WorkerNode> available = workers.stream()
                .filter(WorkerNode::isAvailable)
                .toList();

        if (available.isEmpty()) {
            log.warn("All Judge0 workers are in cooldown, attempting to use least-recently-failed worker");
            return workers.stream()
                    .min((a, b) -> Long.compare(a.lastFailureTime, b.lastFailureTime))
                    .orElse(null);
        }

        int idx = Math.abs(roundRobinIndex.getAndIncrement()) % available.size();
        return available.get(idx);
    }

    private Map<String, Object> executeOnWorker(WorkerNode worker, int languageId, String sourceCode, String stdinInput, String language) {
        try {
            String token = submitToWorker(worker, sourceCode, languageId, stdinInput);
            Map<String, Object> result = pollFromWorker(worker, token);
            int lineOffset = getHelperLineOffset(language);
            if (lineOffset > 0) {
                remapErrorLines(result, lineOffset);
            }
            return result;
        } catch (Exception e) {
            log.error("Judge0 execution failed on worker {}", worker.url, e);
            throw new RuntimeException("Code execution failed: " + e.getMessage(), e);
        }
    }

    private String submitToWorker(WorkerNode worker, String sourceCode, int languageId, String stdinInput) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "source_code", sourceCode,
                "language_id", languageId,
                "stdin", stdinInput != null ? stdinInput : "",
                "cpu_time_limit", 5,
                "memory_limit", 128000,
                "wall_time_limit", 10
        );

        String jsonBody = MAPPER.writeValueAsString(requestBody);
        String url = worker.url + "/submissions?base64_encoded=false&wait=false";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new RuntimeException("Judge0 submission failed on " + worker.url + ": " + response.statusCode() + " - " + response.body());
        }

        JsonNode node = MAPPER.readTree(response.body());
        return node.get("token").asText();
    }

    private Map<String, Object> pollFromWorker(WorkerNode worker, String token) throws Exception {
        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            Map<String, Object> result = getResultFromWorker(worker, token);
            int statusId = (int) result.getOrDefault("status_id", 1);

            if (statusId > 2) {
                return result;
            }

            Thread.sleep(POLL_INTERVAL_MS);
        }

        throw new RuntimeException("Judge0 execution timeout on worker " + worker.url);
    }

    private Map<String, Object> getResultFromWorker(WorkerNode worker, String token) throws Exception {
        String url = worker.url + "/submissions/" + token + "?base64_encoded=false&fields=stdout,stderr,compile_output,status_id,memory,time,language_id";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Judge0 result fetch failed on " + worker.url + ": " + response.statusCode());
        }

        return MAPPER.readValue(response.body(), Map.class);
    }

    public Map<String, Object> getStatuses() {
        WorkerNode worker = selectWorker();
        if (worker == null) return Map.of();

        try {
            String url = worker.url + "/statuses";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return MAPPER.readValue(response.body(), Map.class);
        } catch (Exception e) {
            log.error("Failed to get statuses from {}", worker.url, e);
            return Map.of();
        }
    }

    public Map<String, Object> getClusterStatus() {
        return Map.of(
                "totalWorkers", workers.size(),
                "availableWorkers", workers.stream().filter(WorkerNode::isAvailable).count(),
                "workers", workers.stream().map(w -> Map.of(
                        "url", w.url,
                        "available", w.isAvailable(),
                        "consecutiveFailures", w.consecutiveFailures,
                        "totalRequests", w.totalRequests.get(),
                        "totalFailures", w.totalFailures.get(),
                        "lastFailureTime", w.lastFailureTime > 0 ? Instant.ofEpochMilli(w.lastFailureTime).toString() : "none"
                )).toList()
        );
    }

    private String wrapCode(String language, String sourceCode) {
        String normalized = language.toLowerCase();
        if (normalized.equals("c") && !hasInclude(sourceCode, "stdio.h")) {
            return C_HELPER_PREFIX + sourceCode;
        }
        if (normalized.equals("cpp") && !hasInclude(sourceCode, "iostream")) {
            return CPP_HELPER_PREFIX + sourceCode;
        }
        return sourceCode;
    }

    private boolean hasInclude(String code, String header) {
        return code.contains("#include") && code.contains(header);
    }

    private int getHelperLineOffset(String language) {
        String normalized = language.toLowerCase();
        if (normalized.equals("c")) return C_HELPER_LINES;
        if (normalized.equals("cpp")) return CPP_HELPER_LINES;
        return 0;
    }

    private void remapErrorLines(Map<String, Object> result, int lineOffset) {
        for (String key : new String[]{"stderr", "compile_output"}) {
            Object obj = result.get(key);
            if (obj instanceof String text && !text.isBlank()) {
                result.put(key, shiftLineNumbers(text, lineOffset));
            }
        }
    }

    private String shiftLineNumbers(String text, int offset) {
        return text.replaceAll("(line|行)\\s*(\\d+)", m -> {
            String prefix = m.group(1);
            int originalLine = Integer.parseInt(m.group(2));
            int shiftedLine = Math.max(1, originalLine - offset);
            return prefix + shiftedLine;
        });
    }

    static class WorkerNode {
        final String url;
        volatile int consecutiveFailures = 0;
        volatile long lastFailureTime = 0;
        final AtomicLong totalRequests = new AtomicLong(0);
        final AtomicLong totalFailures = new AtomicLong(0);

        WorkerNode(String url) {
            this.url = url;
        }

        boolean isAvailable() {
            if (consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
                return true;
            }
            return System.currentTimeMillis() - lastFailureTime > WORKER_COOLDOWN_MS;
        }

        void recordSuccess() {
            consecutiveFailures = 0;
            totalRequests.incrementAndGet();
        }

        void recordFailure() {
            consecutiveFailures++;
            lastFailureTime = System.currentTimeMillis();
            totalRequests.incrementAndGet();
            totalFailures.incrementAndGet();
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
