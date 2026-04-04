package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir:uploads/avatars}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB
    private long maxFileSize;

    @Value("${file.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String[] allowedTypes;

    public String getUploadDir() {
        return uploadDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public String[] getAllowedTypes() {
        return allowedTypes;
    }
}
