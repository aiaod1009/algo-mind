package com.example.demo.ai_more;

import com.example.demo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/upload")
    public Result<UploadResponse> uploadChatFile(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "Uploaded file cannot be empty");
        }

        try {
            Path uploadPath = Paths.get(chatFilesDir).toAbsolutePath().normalize();
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
