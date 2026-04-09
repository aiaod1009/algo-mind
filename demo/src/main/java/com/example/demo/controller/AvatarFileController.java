package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Duration;

@RestController
public class AvatarFileController {

    private final FileStorageService fileStorageService;

    public AvatarFileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/uploads/avatars/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            FileStorageService.StoredAvatar storedAvatar = fileStorageService.loadAvatar(filename);
            Resource resource = new FileSystemResource(storedAvatar.path());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(storedAvatar.contentType()))
                    .contentLength(Files.size(storedAvatar.path()))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchFileException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
