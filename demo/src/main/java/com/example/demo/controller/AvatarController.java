package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FileStorageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users/me")
public class AvatarController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AvatarController(FileStorageService fileStorageService,
                            UserRepository userRepository,
                            CurrentUserService currentUserService) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail(40001, "请选择要上传的文件");
        }

        try {
            User user = currentUserService.requireCurrentUserEntity();
            String oldAvatar = user.getAvatar();
            String avatarUrl = fileStorageService.storeAvatar(file, user.getId());

            if (oldAvatar != null && !oldAvatar.isBlank() && !oldAvatar.equals(avatarUrl)) {
                safelyDeleteOldAvatar(oldAvatar);
            }

            user.setAvatar(avatarUrl);
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("message", "头像上传成功");
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.fail(40001, e.getMessage());
        } catch (IOException e) {
            return Result.fail(50001, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail(50001, "上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/avatar")
    public Result<Map<String, Object>> deleteAvatar() {
        try {
            User user = currentUserService.requireCurrentUserEntity();
            String oldAvatar = user.getAvatar();
            if (oldAvatar == null || oldAvatar.isBlank()) {
                return Result.fail(40001, "用户没有设置头像");
            }

            fileStorageService.deleteAvatar(oldAvatar);
            user.setAvatar(null);
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "头像删除成功");
            return Result.success(result);
        } catch (IOException e) {
            return Result.fail(50001, "删除头像失败: " + e.getMessage());
        }
    }

    @GetMapping("/avatar")
    public Result<Map<String, Object>> getAvatar() {
        User user = currentUserService.requireCurrentUserEntity();
        Map<String, Object> result = new HashMap<>();
        result.put("avatarUrl", user.getAvatar());
        return Result.success(result);
    }

    @PostMapping("/avatar-from-url")
    public Result<Map<String, Object>> uploadAvatarFromUrl(@RequestBody Map<String, String> request) {
        String imageUrl = request.get("url");
        if (imageUrl == null || imageUrl.isBlank()) {
            return Result.fail(40001, "请提供图片URL");
        }

        try {
            User user = currentUserService.requireCurrentUserEntity();
            String oldAvatar = user.getAvatar();
            String avatarUrl = fileStorageService.storeAvatarFromUrl(imageUrl, user.getId());

            if (oldAvatar != null && !oldAvatar.isBlank() && !oldAvatar.equals(avatarUrl)) {
                safelyDeleteOldAvatar(oldAvatar);
            }

            user.setAvatar(avatarUrl);
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("message", "头像上传成功");
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.fail(40001, e.getMessage());
        } catch (IOException e) {
            return Result.fail(50001, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return Result.fail(50001, "上传失败: " + e.getMessage());
        }
    }

    private void safelyDeleteOldAvatar(String oldAvatar) {
        try {
            fileStorageService.deleteAvatar(oldAvatar);
        } catch (IOException ignored) {
        }
    }
}
