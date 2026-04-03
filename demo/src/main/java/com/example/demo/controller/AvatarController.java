package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users/me")
public class AvatarController {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    public AvatarController(FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long currentUserId = 1L;

        if (file.isEmpty()) {
            return Result.fail(40001, "请选择要上传的文件");
        }

        try {
            // 先尝试查找用户
            Optional<User> existingUser = userRepository.findById(currentUserId);
            User user;
            String oldAvatar = null;
            
            if (existingUser.isPresent()) {
                // 用户存在，直接更新
                user = existingUser.get();
                oldAvatar = user.getAvatar();
                System.out.println("找到现有用户，ID: " + user.getId());
            } else {
                // 用户不存在，创建新用户（不设置ID，让数据库自动生成）
                System.out.println("用户不存在，创建新用户...");
                User newUser = new User();
                // 不设置ID，让数据库自动生成
                newUser.setName("系统管理员");
                newUser.setEmail("admin@example.com");
                newUser.setPassword("admin");
                newUser.setPoints(999);
                newUser.setBio("先把基础打扎实，再冲更高难度。");
                newUser.setGender("unknown");
                newUser.setTargetTrack("algo");
                newUser.setWeeklyGoal(10);
                newUser.setCreatedAt(java.time.OffsetDateTime.now());
                newUser.setUpdatedAt(java.time.OffsetDateTime.now());
                user = userRepository.save(newUser);
                System.out.println("新用户创建成功，ID: " + user.getId());
            }

            // 存储新头像
            String avatarUrl = fileStorageService.storeAvatar(file, user.getId());

            // 删除旧头像文件
            if (oldAvatar != null && !oldAvatar.isEmpty() && !oldAvatar.equals(avatarUrl)) {
                try {
                    fileStorageService.deleteAvatar(oldAvatar);
                } catch (IOException e) {
                    // 删除旧文件失败不影响新文件保存
                    System.err.println("删除旧头像失败: " + e.getMessage());
                }
            }

            // 更新用户头像URL
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            System.out.println("用户头像更新成功: " + avatarUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("message", "头像上传成功");

            return Result.success(result);

        } catch (IllegalArgumentException e) {
            System.err.println("头像上传参数错误: " + e.getMessage());
            return Result.fail(40001, e.getMessage());
        } catch (IOException e) {
            System.err.println("头像上传IO错误: " + e.getMessage());
            e.printStackTrace();
            return Result.fail(50001, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("头像上传未知错误: " + e.getMessage());
            e.printStackTrace();
            return Result.fail(50001, "上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/avatar")
    public Result<Map<String, Object>> deleteAvatar() {
        Long currentUserId = 1L;

        try {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String oldAvatar = user.getAvatar();
            if (oldAvatar == null || oldAvatar.isEmpty()) {
                return Result.fail(40001, "用户没有设置头像");
            }

            // 删除头像文件
            fileStorageService.deleteAvatar(oldAvatar);

            // 清空用户头像URL
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
        Long currentUserId = 1L;

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> result = new HashMap<>();
        result.put("avatarUrl", user.getAvatar());

        return Result.success(result);
    }

    @PostMapping("/avatar-from-url")
    public Result<Map<String, Object>> uploadAvatarFromUrl(@RequestBody Map<String, String> request) {
        Long currentUserId = 1L;
        String imageUrl = request.get("url");

        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.fail(40001, "请提供图片URL");
        }

        try {
            // 先尝试查找用户
            Optional<User> existingUser = userRepository.findById(currentUserId);
            User user;
            String oldAvatar = null;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                oldAvatar = user.getAvatar();
                System.out.println("找到现有用户，ID: " + user.getId());
            } else {
                System.out.println("用户不存在，创建新用户...");
                User newUser = new User();
                newUser.setName("系统管理员");
                newUser.setEmail("admin@example.com");
                newUser.setPassword("admin");
                newUser.setPoints(999);
                newUser.setBio("先把基础打扎实，再冲更高难度。");
                newUser.setGender("unknown");
                newUser.setTargetTrack("algo");
                newUser.setWeeklyGoal(10);
                newUser.setCreatedAt(java.time.OffsetDateTime.now());
                newUser.setUpdatedAt(java.time.OffsetDateTime.now());
                user = userRepository.save(newUser);
                System.out.println("新用户创建成功，ID: " + user.getId());
            }

            // 从URL下载图片并存储
            String avatarUrl = fileStorageService.storeAvatarFromUrl(imageUrl, user.getId());

            // 删除旧头像文件
            if (oldAvatar != null && !oldAvatar.isEmpty() && !oldAvatar.equals(avatarUrl)) {
                try {
                    fileStorageService.deleteAvatar(oldAvatar);
                } catch (IOException e) {
                    System.err.println("删除旧头像失败: " + e.getMessage());
                }
            }

            // 更新用户头像URL
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            System.out.println("用户头像更新成功: " + avatarUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("message", "头像上传成功");

            return Result.success(result);

        } catch (IllegalArgumentException e) {
            System.err.println("头像上传参数错误: " + e.getMessage());
            return Result.fail(40001, e.getMessage());
        } catch (IOException e) {
            System.err.println("头像上传IO错误: " + e.getMessage());
            e.printStackTrace();
            return Result.fail(50001, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("头像上传未知错误: " + e.getMessage());
            e.printStackTrace();
            return Result.fail(50001, "上传失败: " + e.getMessage());
        }
    }
}
