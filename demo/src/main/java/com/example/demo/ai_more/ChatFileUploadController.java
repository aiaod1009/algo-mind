package com.example.demo.ai_more;

import com.example.demo.Result;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController("aiMoreChatFileUploadController")
@RequestMapping("/ai/chat")
public class ChatFileUploadController {

    private static final Logger log = LoggerFactory.getLogger(ChatFileUploadController.class);

    @Value("${app.upload.chat-files:uploads/chat-files}")
    private String chatFilesDir;

    private final ResourceLoader resourceLoader;

    public ChatFileUploadController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = getUploadPath();
            log.info("Attempting to create chat file upload directory: {}", uploadPath);
            
            // 确保父目录存在
            Path parentDir = uploadPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
                log.info("Created parent directory: {}", parentDir);
            }
            
            // 创建上传目录
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }
            
            // 验证目录是否可写
            if (!Files.isWritable(uploadPath)) {
                log.error("Upload directory is not writable: {}", uploadPath);
            } else {
                log.info("Chat file upload directory initialized successfully: {}", uploadPath);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {} - Error: {}", chatFilesDir, e.getMessage(), e);
        }
    }

    private Path getUploadPath() throws IOException {
        Path uploadPath;
        String normalizedPath = chatFilesDir.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        
        if (Paths.get(normalizedPath).isAbsolute()) {
            uploadPath = Paths.get(normalizedPath);
        } else {
            // 使用项目根目录作为基础路径
            String userDir = System.getProperty("user.dir");
            uploadPath = Paths.get(userDir, normalizedPath);
        }
        return uploadPath.toAbsolutePath().normalize();
    }

    @PostMapping("/upload")
    public Result<UploadResponse> uploadChatFile(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "Uploaded file cannot be empty");
        }

        try {
            Path uploadPath = getUploadPath();
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null) {
                int dotIndex = originalFilename.lastIndexOf('.');
                if (dotIndex >= 0) {
                    extension = originalFilename.substring(dotIndex);
                }
            }

            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path targetPath = uploadPath.resolve(newFilename).normalize();
            if (!targetPath.startsWith(uploadPath)) {
                return Result.fail(400, "Invalid upload path");
            }

            file.transferTo(targetPath);

            String fileUrl = "/api/uploads/chat-files/" + newFilename;

            log.info("Uploaded chat file: {}", targetPath);
            return Result.success(new UploadResponse(
                    fileUrl,
                    newFilename,
                    originalFilename,
                    file.getSize(),
                    file.getContentType()
            ));
        } catch (IOException e) {
            log.error("Failed to upload chat file", e);
            return Result.fail(500, "Failed to upload file: " + e.getMessage());
        }
    }

    public record UploadResponse(
            String url,
            String filename,
            String originalFilename,
            long size,
            String contentType
    ) {
    }
}
