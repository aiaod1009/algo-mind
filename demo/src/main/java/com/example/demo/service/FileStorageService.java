package com.example.demo.service;

import com.example.demo.config.FileStorageConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = createContentTypeExtensions();

    private final FileStorageConfig fileStorageConfig;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    public String storeAvatar(MultipartFile file, Long userId) throws IOException {
        if (file.getSize() <= 0) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许 "
                    + (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        byte[] fileBytes = file.getBytes();
        AvatarFile avatarFile = validateAvatarContent(fileBytes);
        ensureAllowedType(avatarFile.contentType());

        Path uploadPath = ensureUploadDirectory();
        String newFilename = buildAvatarFilename(userId, avatarFile.extension());
        Path targetLocation = uploadPath.resolve(newFilename);
        Files.write(targetLocation, fileBytes);

        return buildPublicAvatarPath(newFilename);
    }

    public void deleteAvatar(String avatarUrl) throws IOException {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }

        String filename = extractFilename(avatarUrl);
        if (filename == null) {
            return;
        }

        Path filePath = resolveAvatarPath(filename);
        Files.deleteIfExists(filePath);
    }

    public StoredAvatar loadAvatar(String filename) throws IOException {
        Path avatarPath = resolveAvatarPath(filename);
        if (!Files.isRegularFile(avatarPath)) {
            throw new NoSuchFileException(filename);
        }

        byte[] fileBytes = Files.readAllBytes(avatarPath);
        AvatarFile avatarFile = validateAvatarContent(fileBytes);
        ensureAllowedType(avatarFile.contentType());
        return new StoredAvatar(avatarPath, avatarFile.contentType());
    }

    private Path ensureUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(fileStorageConfig.getUploadDir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private Path resolveAvatarPath(String filename) throws IOException {
        if (filename == null || filename.isBlank()
                || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new IllegalArgumentException("头像文件名无效");
        }

        Path uploadPath = ensureUploadDirectory().toAbsolutePath().normalize();
        Path resolvedPath = uploadPath.resolve(filename).normalize();
        if (!resolvedPath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("头像路径无效");
        }
        return resolvedPath;
    }

    private String buildAvatarFilename(Long userId, String extension) {
        return "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private String buildPublicAvatarPath(String filename) {
        return "/api/uploads/avatars/" + filename;
    }

    private void ensureAllowedType(String contentType) {
        if (!isAllowedType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持: "
                    + String.join(", ", fileStorageConfig.getAllowedTypes()));
        }
    }

    private boolean isAllowedType(String contentType) {
        return contentType != null && Arrays.asList(fileStorageConfig.getAllowedTypes()).contains(contentType);
    }

    private AvatarFile validateAvatarContent(byte[] fileBytes) {
        AvatarFile detectedAvatar = detectAvatarFile(fileBytes);
        if (detectedAvatar == null) {
            throw new IllegalArgumentException("文件内容不是受支持的图片格式");
        }

        if (requiresDecodingCheck(detectedAvatar.contentType()) && !isDecodableImage(fileBytes)) {
            throw new IllegalArgumentException("图片内容无效或已损坏，请重新选择图片");
        }

        return detectedAvatar;
    }

    private AvatarFile detectAvatarFile(byte[] fileBytes) {
        if (isJpeg(fileBytes)) {
            return new AvatarFile("image/jpeg", CONTENT_TYPE_EXTENSIONS.get("image/jpeg"));
        }
        if (isPng(fileBytes)) {
            return new AvatarFile("image/png", CONTENT_TYPE_EXTENSIONS.get("image/png"));
        }
        if (isGif(fileBytes)) {
            return new AvatarFile("image/gif", CONTENT_TYPE_EXTENSIONS.get("image/gif"));
        }
        if (isWebp(fileBytes)) {
            return new AvatarFile("image/webp", CONTENT_TYPE_EXTENSIONS.get("image/webp"));
        }
        return null;
    }

    private boolean requiresDecodingCheck(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/gif".equals(contentType);
    }

    private boolean isDecodableImage(byte[] fileBytes) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            BufferedImage image = ImageIO.read(inputStream);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isJpeg(byte[] fileBytes) {
        return fileBytes.length >= 3
                && fileBytes[0] == (byte) 0xFF
                && fileBytes[1] == (byte) 0xD8
                && fileBytes[2] == (byte) 0xFF;
    }

    private boolean isPng(byte[] fileBytes) {
        return fileBytes.length >= 8
                && fileBytes[0] == (byte) 0x89
                && fileBytes[1] == 0x50
                && fileBytes[2] == 0x4E
                && fileBytes[3] == 0x47
                && fileBytes[4] == 0x0D
                && fileBytes[5] == 0x0A
                && fileBytes[6] == 0x1A
                && fileBytes[7] == 0x0A;
    }

    private boolean isGif(byte[] fileBytes) {
        return fileBytes.length >= 6
                && fileBytes[0] == 0x47
                && fileBytes[1] == 0x49
                && fileBytes[2] == 0x46
                && fileBytes[3] == 0x38
                && (fileBytes[4] == 0x37 || fileBytes[4] == 0x39)
                && fileBytes[5] == 0x61;
    }

    private boolean isWebp(byte[] fileBytes) {
        return fileBytes.length >= 12
                && fileBytes[0] == 0x52
                && fileBytes[1] == 0x49
                && fileBytes[2] == 0x46
                && fileBytes[3] == 0x46
                && fileBytes[8] == 0x57
                && fileBytes[9] == 0x45
                && fileBytes[10] == 0x42
                && fileBytes[11] == 0x50;
    }

    private static Map<String, String> createContentTypeExtensions() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("image/jpeg", ".jpg");
        mappings.put("image/png", ".png");
        mappings.put("image/gif", ".gif");
        mappings.put("image/webp", ".webp");
        return mappings;
    }

    private String extractFilename(String avatarUrl) {
        String normalized = avatarUrl.trim();
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        int lastSlashIndex = normalized.lastIndexOf('/');
        if (lastSlashIndex < 0 || lastSlashIndex == normalized.length() - 1) {
            return null;
        }
        return normalized.substring(lastSlashIndex + 1);
    }

    private record AvatarFile(String contentType, String extension) {
    }

    public record StoredAvatar(Path path, String contentType) {
    }
}
