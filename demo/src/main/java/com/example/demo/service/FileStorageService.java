package com.example.demo.service;

import com.example.demo.config.FileStorageConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final int REMOTE_CONNECT_TIMEOUT_MS = 10_000;
    private static final int REMOTE_READ_TIMEOUT_MS = 15_000;
    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = createContentTypeExtensions();

    private final FileStorageConfig fileStorageConfig;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    public String storeAvatar(MultipartFile file, Long userId) throws IOException {
        String contentType = normalizeContentType(file.getContentType());
        if (!isAllowedType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持: " + String.join(", ", fileStorageConfig.getAllowedTypes()));
        }

        if (file.getSize() <= 0) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制，最大允许 "
                    + (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        Path uploadPath = ensureUploadDirectory();
        String newFilename = buildAvatarFilename(userId, resolveExtension(file.getOriginalFilename(), contentType));
        Path targetLocation = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return buildPublicAvatarPath(newFilename);
    }

    public void deleteAvatar(String avatarUrl) throws IOException {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }

        String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
        Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    public String storeAvatarFromUrl(String imageUrl, Long userId) throws IOException {
        URL url = new URL(imageUrl);
        validateRemoteUrl(url);

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(REMOTE_CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(REMOTE_READ_TIMEOUT_MS);
        if (connection instanceof HttpURLConnection httpConnection) {
            httpConnection.setInstanceFollowRedirects(false);
        }

        String contentType = normalizeContentType(connection.getContentType());
        if (!isAllowedType(contentType)) {
            throw new IllegalArgumentException("远程图片类型不受支持，仅支持: " + String.join(", ", fileStorageConfig.getAllowedTypes()));
        }

        long contentLength = connection.getContentLengthLong();
        if (contentLength > fileStorageConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("远程图片大小超过限制");
        }

        Path uploadPath = ensureUploadDirectory();
        String extension = resolveExtensionFromRemote(url, contentType);
        String newFilename = buildAvatarFilename(userId, extension);
        Path targetLocation = uploadPath.resolve(newFilename);

        try (InputStream inputStream = connection.getInputStream()) {
            copyWithSizeLimit(inputStream, targetLocation, fileStorageConfig.getMaxFileSize());
        } catch (RuntimeException ex) {
            Files.deleteIfExists(targetLocation);
            throw ex;
        }

        return buildPublicAvatarPath(newFilename);
    }

    private Path ensureUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(fileStorageConfig.getUploadDir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    private String buildAvatarFilename(Long userId, String extension) {
        return "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    private String buildPublicAvatarPath(String filename) {
        return "/api/uploads/avatars/" + filename;
    }

    private boolean isAllowedType(String contentType) {
        return contentType != null && Arrays.asList(fileStorageConfig.getAllowedTypes()).contains(contentType);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private String resolveExtension(String filename, String contentType) {
        String extension = getFileExtension(filename);
        if (extension != null) {
            return extension;
        }
        return CONTENT_TYPE_EXTENSIONS.getOrDefault(contentType, ".jpg");
    }

    private String resolveExtensionFromRemote(URL url, String contentType) {
        String extension = getFileExtension(url.getPath());
        if (extension != null) {
            return extension;
        }
        return CONTENT_TYPE_EXTENSIONS.getOrDefault(contentType, ".jpg");
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return null;
        }
        String extension = filename.substring(index).toLowerCase(Locale.ROOT);
        if (extension.length() > 5) {
            return null;
        }
        return extension;
    }

    private void copyWithSizeLimit(InputStream inputStream, Path targetLocation, long maxBytes) throws IOException {
        long totalBytes = 0;
        byte[] buffer = new byte[8192];
        try (var outputStream = Files.newOutputStream(targetLocation)) {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                totalBytes += read;
                if (totalBytes > maxBytes) {
                    throw new IllegalArgumentException("远程图片大小超过限制");
                }
                outputStream.write(buffer, 0, read);
            }
        }
    }

    private void validateRemoteUrl(URL url) throws IOException {
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("仅支持 http/https 图片地址");
        }

        String host = url.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("图片地址无效");
        }

        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isSiteLocalAddress()
                    || address.isLinkLocalAddress()) {
                throw new IllegalArgumentException("不允许访问内网或本地地址");
            }
        }
    }

    private static Map<String, String> createContentTypeExtensions() {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("image/jpeg", ".jpg");
        mappings.put("image/png", ".png");
        mappings.put("image/gif", ".gif");
        mappings.put("image/webp", ".webp");
        return mappings;
    }
}
