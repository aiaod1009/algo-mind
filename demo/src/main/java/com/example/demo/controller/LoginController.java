package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.JwtService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        String identifier = normalize(req.getUsername());
        String rawPassword = normalize(req.getPassword());

        if (!StringUtils.hasText(identifier) || !StringUtils.hasText(rawPassword)) {
            return Result.fail(40001, "账号和密码不能为空");
        }

        Optional<User> userOptional = findUserByIdentifier(identifier);
        if (userOptional.isEmpty()) {
            return Result.fail(40001, "账号或密码错误");
        }

        User user = userOptional.get();
        if (!matchesPassword(rawPassword, user)) {
            return Result.fail(40001, "账号或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("points", user.getPoints());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("bio", user.getBio());
        userInfo.put("github", user.getGithub());
        userInfo.put("website", user.getWebsite());
        userInfo.put("targetTrack", user.getTargetTrack());
        userInfo.put("weeklyGoal", user.getWeeklyGoal());

        result.put("token", jwtService.generateToken(user));
        result.put("user", userInfo);
        result.put("points", user.getPoints() != null ? user.getPoints() : 0);

        return Result.success(result);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            Optional<User> byEmail = userRepository.findByEmailIgnoreCase(identifier);
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }

        Optional<User> byName = userRepository.findFirstByNameIgnoreCase(identifier);
        if (byName.isPresent()) {
            return byName;
        }

        return userRepository.findByEmailLocalPart(identifier);
    }

    private boolean matchesPassword(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }

        if (isBcryptPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (rawPassword.equals(storedPassword)) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            return true;
        }

        return false;
    }

    private boolean isBcryptPassword(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

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
