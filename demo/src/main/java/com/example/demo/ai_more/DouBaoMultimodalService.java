package com.example.demo.ai_more;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("aiMoreDouBaoMultimodalService")
@RequiredArgsConstructor
public class DouBaoMultimodalService {

    private static final long STREAM_TIMEOUT_MS = 180_000L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ExecutorService MULTIMODAL_POOL = new ThreadPoolExecutor(
            4,
            8,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "ai-more-multimodal-" + r.hashCode())
    );

    private final DouBaoProperties properties;

    @Value("${app.upload.chat-files:uploads/chat-files}")
    private String chatFilesDir;

    private String apiKey;

    @PostConstruct
    void init() {
        apiKey = properties.getMultimodalApiKey();
        if (!hasText(apiKey)) {
            log.warn("Multimodal API key is not configured");
        }
    }

    public String chatWithImages(String text, List<ImageFile> images) {
        if (!hasText(apiKey)) {
            return "Multimodal service is not configured";
        }
        if (!hasText(text) && (images == null || images.isEmpty())) {
            return "Please provide text or upload at least one image";
        }

        try {
            String requestBody = buildRequestBody(text, images, false);
            String response = sendRequest(requestBody);
            return extractContent(response);
        } catch (Exception e) {
            log.error("Multimodal request failed", e);
            return "Multimodal request failed: " + e.getMessage();
        }
    }

    public SseEmitter chatWithImagesStream(String text, List<ImageFile> images) {
        SseEmitter emitter = createEmitter();
        if (!hasText(apiKey)) {
            completeWithError(emitter, "Multimodal service is not configured");
            return emitter;
        }
        if (!hasText(text) && (images == null || images.isEmpty())) {
            completeWithError(emitter, "Please provide text or upload at least one image");
            return emitter;
        }

        MULTIMODAL_POOL.execute(() -> {
            try {
                String requestBody = buildRequestBody(text, images, true);
                streamRequest(requestBody, emitter);
            } catch (Exception e) {
                log.error("Multimodal streaming request failed", e);
                completeWithError(emitter, "Multimodal request failed: " + e.getMessage());
            }
        });
        return emitter;
    }

    private String buildRequestBody(String text, List<ImageFile> images, boolean stream) throws IOException {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", properties.getModel());
        if (stream) {
            root.put("stream", true);
        }

        ArrayNode messages = root.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        ArrayNode content = userMessage.putArray("content");

        if (images != null) {
            for (ImageFile image : images) {
                if (image == null) {
                    continue;
                }

                if (isImagePayload(image)) {
                    String pureBase64 = resolveImageBase64(image);
                    if (!hasText(pureBase64)) {
                        continue;
                    }

                    ObjectNode imageContent = content.addObject();
                    imageContent.put("type", "image_url");
                    ObjectNode imageUrlNode = imageContent.putObject("image_url");
                    imageUrlNode.put("url", buildDataUrl(pureBase64, resolveMimeType(image)));
                    continue;
                }

                ObjectNode fileContent = content.addObject();
                fileContent.put("type", "text");
                fileContent.put(
                        "text",
                        "[file: " + safeText(image.getName()) + ", size: " + formatFileSize(image.getSize()) + "]"
                );
            }
        }

        if (hasText(text)) {
            ObjectNode textContent = content.addObject();
            textContent.put("type", "text");
            textContent.put("text", text.trim());
        }

        if (content.isEmpty()) {
            throw new IllegalArgumentException("No valid multimodal input was found");
        }

        return OBJECT_MAPPER.writeValueAsString(root);
    }

    private boolean isImagePayload(ImageFile image) {
        if (image == null) {
            return false;
        }
        if (image.isImage()) {
            return true;
        }
        if (hasText(image.getType()) && image.getType().startsWith("image/")) {
            return true;
        }
        if (hasText(image.getBase64())) {
            return true;
        }
        return hasText(image.getUrl()) && image.getUrl().startsWith("data:image/");
    }

    private String resolveImageBase64(ImageFile image) {
        if (image == null) {
            return null;
        }

        if (hasText(image.getBase64())) {
            return extractPureBase64(image.getBase64());
        }

        if (!hasText(image.getUrl())) {
            return null;
        }

        String imageUrl = image.getUrl().trim();
        if (imageUrl.startsWith("data:")) {
            return extractPureBase64(imageUrl);
        }

        Path localFilePath = resolveLocalUploadPath(imageUrl);
        if (localFilePath != null && Files.exists(localFilePath)) {
            return encodeFileToBase64(localFilePath);
        }

        return null;
    }

    public static String extractPureBase64(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String normalized = rawValue.trim();
        if (!normalized.startsWith("data:")) {
            return normalized;
        }

        int commaIndex = normalized.indexOf(',');
        if (commaIndex < 0 || commaIndex >= normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid data URL base64 value");
        }

        return normalized.substring(commaIndex + 1).trim();
    }

    private String resolveMimeType(ImageFile image) {
        if (hasText(image.getType())) {
            return image.getType().trim();
        }
        if (hasText(image.getName())) {
            return detectMimeTypeByName(image.getName());
        }
        return "image/jpeg";
    }

    private String buildDataUrl(String pureBase64, String mimeType) {
        String safeMimeType = hasText(mimeType) ? mimeType.trim() : "image/jpeg";
        return "data:" + safeMimeType + ";base64," + extractPureBase64(pureBase64);
    }

    private Path resolveLocalUploadPath(String imageUrl) {
        int markerIndex = imageUrl.lastIndexOf("/chat-files/");
        if (markerIndex < 0) {
            return null;
        }

        String filename = imageUrl.substring(markerIndex + "/chat-files/".length()).trim();
        if (!hasText(filename) || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return null;
        }

        Path uploadRoot = Paths.get(chatFilesDir).toAbsolutePath().normalize();
        Path localFilePath = uploadRoot.resolve(filename).normalize();
        if (!localFilePath.startsWith(uploadRoot)) {
            return null;
        }
        return localFilePath;
    }

    private String encodeFileToBase64(Path filePath) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded image: " + filePath, e);
        }
    }

    private String detectMimeTypeByName(String fileName) {
        String lower = safeText(fileName).toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        return "image/jpeg";
    }

    private String sendRequest(String requestBody) throws IOException {
        HttpURLConnection conn = openConnection(false);
        try {
            conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
            try (InputStream stream = getResponseStream(conn);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            conn.disconnect();
        }
    }

    private void streamRequest(String requestBody, SseEmitter emitter) {
        HttpURLConnection conn = null;
        try {
            conn = openConnection(true);
            conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

            int responseCode = conn.getResponseCode();
            try (InputStream stream = getResponseStream(conn, responseCode);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder rawResponse = new StringBuilder();
                boolean deliveredContent = false;
                String line;
                while ((line = reader.readLine()) != null) {
                    rawResponse.append(line).append('\n');
                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }

                    String content = extractStreamContent(data);
                    if (hasText(content)) {
                        emitter.send(content);
                        deliveredContent = true;
                    }
                }

                if (!deliveredContent) {
                    String fallbackContent = extractContent(rawResponse.toString());
                    if (hasText(fallbackContent)) {
                        emitter.send(fallbackContent);
                    }
                }
            }

            emitter.complete();
        } catch (Exception e) {
            log.error("Streaming multimodal request failed", e);
            completeWithError(emitter, "Multimodal request failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(boolean stream) throws IOException {
        URL url = new URL(properties.getUrl());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        if (stream) {
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setReadTimeout(180_000);
        } else {
            conn.setReadTimeout(120_000);
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(30_000);
        return conn;
    }

    private InputStream getResponseStream(HttpURLConnection conn) throws IOException {
        return getResponseStream(conn, conn.getResponseCode());
    }

    private InputStream getResponseStream(HttpURLConnection conn, int responseCode) throws IOException {
        InputStream stream = responseCode >= 200 && responseCode < 300
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (stream == null) {
            throw new IOException("Remote model returned an empty response. HTTP status: " + responseCode);
        }
        return stream;
    }

    private String extractContent(String response) {
        try {
            String normalizedResponse = response == null ? "" : response.trim();
            if (!hasText(normalizedResponse)) {
                return "Model did not return any text content";
            }

            JsonNode root = OBJECT_MAPPER.readTree(normalizedResponse);
            if (root.has("error")) {
                return "Model error: " + root.path("error").path("message").asText("Unknown error");
            }

            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                JsonNode content = message.path("content");
                if (content.isTextual()) {
                    return content.asText();
                }
                if (content.isArray()) {
                    StringBuilder builder = new StringBuilder();
                    for (JsonNode item : content) {
                        String text = item.path("text").asText(null);
                        if (text != null) {
                            builder.append(text);
                        }
                    }
                    if (builder.length() > 0) {
                        return builder.toString();
                    }
                }
            }

            return "Model did not return any text content";
        } catch (Exception e) {
            log.error("Failed to parse multimodal response: {}", response, e);
            if (hasText(response)) {
                return response.trim();
            }
            return "Failed to parse multimodal response";
        }
    }

    private String extractStreamContent(String data) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(data);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }

            JsonNode delta = choices.get(0).path("delta");
            JsonNode content = delta.path("content");
            if (content.isTextual()) {
                return content.asText();
            }

            if (content.isArray()) {
                StringBuilder builder = new StringBuilder();
                for (JsonNode item : content) {
                    String text = item.path("text").asText(null);
                    if (text != null) {
                        builder.append(text);
                    }
                }
                return builder.length() == 0 ? null : builder.toString();
            }
            return null;
        } catch (Exception e) {
            log.debug("Failed to parse streaming chunk: {}", data, e);
            return null;
        }
    }

    private SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        emitter.onTimeout(() -> {
            log.warn("Multimodal stream timed out");
            emitter.complete();
        });
        emitter.onError(error -> log.warn("Multimodal emitter error", error));
        return emitter;
    }

    private void completeWithError(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(hasText(errorMsg) ? errorMsg : "Multimodal request failed");
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        }
        return String.format("%.2f MB", size / (1024.0 * 1024));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public static class ImageFile {
        @JsonAlias({"fileName", "filename"})
        private String name;
        private long size;
        private String type;
        private String url;
        private String base64;
        @JsonAlias("isImage")
        private boolean image;

        public ImageFile() {
        }

        public ImageFile(String name, long size, String type, String url, boolean image) {
            this.name = name;
            this.size = size;
            this.type = type;
            this.url = url;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getBase64() {
            return base64;
        }

        public void setBase64(String base64) {
            this.base64 = base64;
        }

        public boolean isImage() {
            return image;
        }

        public void setImage(boolean image) {
            this.image = image;
        }
    }
}
