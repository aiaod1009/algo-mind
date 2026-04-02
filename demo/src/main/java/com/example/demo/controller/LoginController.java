package com.example.demo.controller;

import com.example.demo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class LoginController {

    // 前端传入的是 username，统一匹配
    private static final String FIX_USERNAME = "admin";
    private static final String FIX_PWD = "123456";

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        // 匹配前端传的 username + password
        if (FIX_USERNAME.equals(req.getUsername()) && FIX_PWD.equals(req.getPassword())) {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> user = new HashMap<>();
            user.put("id", 1);
            user.put("name", "系统管理员");
            user.put("email", "admin@example.com");
            user.put("points", 999);

            result.put("token", UUID.randomUUID().toString());
            result.put("user", user);
            result.put("points", 999);

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