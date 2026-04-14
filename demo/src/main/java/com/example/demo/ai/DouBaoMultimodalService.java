package com.example.demo.ai;

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
@Service
@RequiredArgsConstructor
public class DouBaoMultimodalService {

    private static final String MULTIMODAL_API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final long STREAM_TIMEOUT_MS = 180_000L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ExecutorService MULTIMODAL_POOL = new ThreadPoolExecutor(
            4,
            8,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            r -> new Thread(r, "multimodal-pool-" + r.hashCode())
    );

    private final DouBaoProperties properties;

    @Value("${app.upload.chat-files:uploads/chat-files}")
    private String chatFilesDir;

    private String apiKey;

    @PostConstruct
    public void init() {
        this.apiKey = properties.getMultimodalApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("多模态 API KEY 未配置，多模态功能将不可用");
        } else {
            log.info("多模态服务初始化成功，使用 API KEY: {}...", apiKey.substring(0, Math.min(8, apiKey.length())));
        }
    }

    public String chatWithImages(String text, List<ImageFile> images) {
        if (apiKey == null || apiKey.isBlank()) {
            return "多模态服务未配置，请检查 API KEY";
        }

        try {
            String requestBody = buildRequestBody(text, images, false);
            String response = sendRequest(requestBody);
            return extractContent(response);
        } catch (Exception e) {
            log.error("多模态对话失败", e);
            return "调用失败：" + e.getMessage();
        }
    }

    public SseEmitter chatWithImagesStream(String text, List<ImageFile> images) {
        SseEmitter emitter = createEmitter();
        
        if (apiKey == null || apiKey.isBlank()) {
            completeWithError(emitter, "多模态服务未配置，请检查 API KEY");
            return emitter;
        }

        MULTIMODAL_POOL.execute(() -> {
            try {
                String requestBody = buildRequestBody(text, images, true);
                streamRequest(requestBody, emitter);
            } catch (Exception e) {
                log.error("多模态流式对话失败", e);
                completeWithError(emitter, "调用失败：" + e.getMessage());
            }
        });
        
        return emitter;
    }

    private String buildRequestBody(String text, List<ImageFile> images, boolean stream) throws Exception {
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

                if (image.isImage()) {
                    String imageUrl = resolveImageUrl(image);
                    ObjectNode imageContent = content.addObject();
                    imageContent.put("type", "image_url");
                    ObjectNode imageUrlNode = imageContent.putObject("image_url");
                    imageUrlNode.put("url", imageUrl);
                } else {
                    ObjectNode textContent = content.addObject();
                    textContent.put("type", "text");
                    textContent.put("text", "[文件: " + safeText(image.getName()) + ", 大小: " + formatFileSize(image.getSize()) + "]");
                }
            }
        }

        if (text != null && !text.isBlank()) {
            ObjectNode textContent = content.addObject();
            textContent.put("type", "text");
            textContent.put("text", text);
        }

        return OBJECT_MAPPER.writeValueAsString(root);
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

            try (InputStream stream = getResponseStream(conn);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }

                    String content = extractStreamContent(data);
                    if (content != null && !content.isEmpty()) {
                        emitter.send(content);
                    }
                }
            }

            emitter.complete();
        } catch (Exception e) {
            log.error("多模态流式请求失败", e);
            completeWithError(emitter, "调用失败：" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(boolean stream) throws IOException {
        URL url = new URL(MULTIMODAL_API_URL);
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
        int responseCode = conn.getResponseCode();
        InputStream stream = responseCode >= 200 && responseCode < 300
                ? conn.getInputStream()
                : conn.getErrorStream();

        if (stream == null) {
            throw new IOException("远程模型返回空响应，状态码: " + responseCode);
        }
        return stream;
    }

    private String extractContent(String response) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(response);
            if (root.has("error")) {
                return "错误: " + root.path("error").path("message").asText("未知错误");
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
                    return builder.toString();
                }
            }

            return "模型未返回有效内容";
        } catch (Exception e) {
            log.error("解析响应失败: {}", response, e);
            return "解析响应失败";
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
                return builder.length() > 0 ? builder.toString() : null;
            }

            return null;
        } catch (Exception e) {
            log.debug("解析流式数据失败: {}", data, e);
            return null;
        }
    }

    private String resolveImageUrl(ImageFile image) {
        if (image == null || image.getUrl() == null || image.getUrl().isBlank()) {
            throw new IllegalArgumentException("图片 URL 不能为空");
        }

        String imageUrl = image.getUrl().trim();
        
        if (imageUrl.startsWith("data:")) {
            return imageUrl;
        }

        Path localFilePath = resolveLocalUploadPath(imageUrl);
        if (localFilePath != null && Files.exists(localFilePath)) {
            return fileToDataUrl(localFilePath, image.getType());
        }

        return imageUrl;
    }

    private Path resolveLocalUploadPath(String imageUrl) {
        int markerIndex = imageUrl.lastIndexOf("/chat-files/");
        if (markerIndex < 0) {
            return null;
        }

        String filename = imageUrl.substring(markerIndex + "/chat-files/".length()).trim();
        if (filename.isEmpty() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return null;
        }

        Path uploadPath = Paths.get(chatFilesDir).toAbsolutePath().normalize();
        Path localFilePath = uploadPath.resolve(filename).normalize();
        if (!localFilePath.startsWith(uploadPath)) {
            return null;
        }

        return localFilePath;
    }

    private String fileToDataUrl(Path filePath, String fallbackMimeType) {
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null || mimeType.isBlank()) {
                mimeType = fallbackMimeType != null && !fallbackMimeType.isBlank() ? fallbackMimeType : "image/jpeg";
            }
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("读取本地图片失败: " + filePath, e);
        }
    }

    private SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
        emitter.onTimeout(() -> {
            log.warn("多模态请求超时");
            emitter.complete();
        });
        emitter.onError(error -> log.warn("多模态 emitter 错误", error));
        return emitter;
    }

    private void completeWithError(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(errorMsg == null || errorMsg.isBlank() ? "调用失败，请稍后重试" : errorMsg);
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

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public static class ImageFile {
        private String name;
        private long size;
        private String type;
        private String url;
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

        public boolean isImage() {
            return image;
        }

        public void setImage(boolean image) {
            this.image = image;
        }
    }
}
