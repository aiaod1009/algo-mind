package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 前端传入的是 username，统一匹配
    private static final String FIX_USERNAME = "admin";
    private static final String FIX_PWD = "123456";

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        // 匹配前端传的 username + password
        if (FIX_USERNAME.equals(req.getUsername()) && FIX_PWD.equals(req.getPassword())) {
            Long currentUserId = 1L;
            User dbUser = userRepository.findById(currentUserId).orElse(null);

            Map<String, Object> result = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("id", 1);
            user.put("name", dbUser != null ? dbUser.getName() : "系统管理员");
            user.put("email", dbUser != null ? dbUser.getEmail() : "admin@example.com");
            user.put("points", dbUser != null ? dbUser.getPoints() : 999);
            user.put("avatar", dbUser != null ? dbUser.getAvatar() : null);
            user.put("bio", dbUser != null ? dbUser.getBio() : null);
            user.put("github", dbUser != null ? dbUser.getGithub() : null);
            user.put("website", dbUser != null ? dbUser.getWebsite() : null);
            user.put("targetTrack", dbUser != null ? dbUser.getTargetTrack() : "algo");
            user.put("weeklyGoal", dbUser != null ? dbUser.getWeeklyGoal() : 10);

            result.put("token", UUID.randomUUID().toString());
            result.put("user", user);
            result.put("points", dbUser != null ? dbUser.getPoints() : 999);

            return Result.success(result);
        }
        return Result.fail(40001, "账号或密码错误");
    }

    // 接收前端参数：username + password（和前端完全对应）
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}