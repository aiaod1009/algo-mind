package com.example.demo.service;

import com.example.demo.config.FileStorageConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    public String storeAvatar(MultipartFile file, Long userId) throws IOException {
        System.out.println("开始存储头像，用户ID: " + userId);
        System.out.println("文件名称: " + file.getOriginalFilename());
        System.out.println("文件大小: " + file.getSize());
        System.out.println("文件类型: " + file.getContentType());
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedType(contentType)) {
            System.err.println("不支持的文件类型: " + contentType);
            throw new IllegalArgumentException("不支持的文件类型，仅支持: " + String.join(", ", fileStorageConfig.getAllowedTypes()));
        }

        // 验证文件大小
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            System.err.println("文件大小超过限制: " + file.getSize());
            throw new IllegalArgumentException("文件大小超过限制，最大允许: " + (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        // 创建上传目录
        String uploadDir = fileStorageConfig.getUploadDir();
        System.out.println("上传目录: " + uploadDir);
        Path uploadPath = Paths.get(uploadDir);
        System.out.println("绝对路径: " + uploadPath.toAbsolutePath());
        
        if (!Files.exists(uploadPath)) {
            System.out.println("目录不存在，创建目录...");
            Files.createDirectories(uploadPath);
            System.out.println("目录创建成功");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        System.out.println("新文件名: " + newFilename);

        // 保存文件
        Path targetLocation = uploadPath.resolve(newFilename);
        System.out.println("目标路径: " + targetLocation.toAbsolutePath());
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("文件保存成功");

        // 返回访问路径
        String result = "/uploads/" + newFilename;
        System.out.println("返回路径: " + result);
        return result;
    }

    public void deleteAvatar(String avatarUrl) throws IOException {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }

        String filename = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
        Path filePath = Paths.get(fileStorageConfig.getUploadDir()).resolve(filename);
        
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    private boolean isAllowedType(String contentType) {
        return Arrays.asList(fileStorageConfig.getAllowedTypes()).contains(contentType);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
