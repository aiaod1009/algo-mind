package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.AuthProperties;
import com.example.demo.auth.JwtService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthorLevelService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthorLevelService authorLevelService;
    private final AuthProperties authProperties;

    public LoginController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthorLevelService authorLevelService,
            AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authorLevelService = authorLevelService;
        this.authProperties = authProperties;
    }

    private static final long REMEMBER_EXPIRATION_MINUTES = 60L * 24 * 30;

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

        user.setAuthorLevelProfile(authorLevelService.refreshAuthorLevel(user.getId(), "LOGIN", null, "登录刷新作者等级"));

        long expirationMinutes = Boolean.TRUE.equals(req.getRemember())
                ? REMEMBER_EXPIRATION_MINUTES
                : authProperties.getExpirationMinutes();

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("points", user.getPoints());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("bio", user.getBio());
        userInfo.put("gender", user.getGender());
        userInfo.put("github", user.getGithub());
        userInfo.put("website", user.getWebsite());
        userInfo.put("targetTrack", user.getTargetTrack());
        userInfo.put("weeklyGoal", user.getWeeklyGoal());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        userInfo.put("statusEmoji", user.getStatusEmoji());
        userInfo.put("statusMood", user.getStatusMood());
        userInfo.put("isBusy", user.getIsBusy());
        userInfo.put("busyAutoReply", user.getBusyAutoReply());
        userInfo.put("busyEndTime", user.getBusyEndTime());
        userInfo.put("authorScore", user.getAuthorScore());
        userInfo.put("authorLevelCode", user.getAuthorLevelCode());
        userInfo.put("authorLevelProfile", user.getAuthorLevelProfile());

        result.put("token", jwtService.generateToken(user, expirationMinutes));
        result.put("user", userInfo);
        result.put("points", user.getPoints() != null ? user.getPoints() : 0);
        result.put("authorScore", user.getAuthorScore() != null ? user.getAuthorScore() : 0);
        result.put("authorLevelCode", user.getAuthorLevelCode());
        result.put("authorLevelProfile", user.getAuthorLevelProfile());

        return Result.success(result);
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        String email = normalize(req.getEmail()).toLowerCase();
        String rawPassword = normalize(req.getPassword());
        String confirmPassword = normalize(req.getConfirmPassword());
        String name = normalize(req.getName());

        if (!StringUtils.hasText(email) || !StringUtils.hasText(rawPassword)) {
            return Result.fail(40001, "邮箱和密码不能为空");
        }
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            return Result.fail(40001, "请输入有效邮箱");
        }
        if (rawPassword.length() < 6) {
            return Result.fail(40001, "密码长度至少6位");
        }
        if (StringUtils.hasText(confirmPassword) && !rawPassword.equals(confirmPassword)) {
            return Result.fail(40001, "两次输入的密码不一致");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return Result.fail(40001, "该邮箱已注册");
        }

        if (!StringUtils.hasText(name)) {
            int atIndex = email.indexOf('@');
            name = atIndex > 0 ? email.substring(0, atIndex) : "同学";
        }

        String baseName = name;
        int suffix = 1;
        while (userRepository.findFirstByNameIgnoreCase(name).isPresent()) {
            suffix++;
            name = baseName + suffix;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPoints(0);
        user.setAuthorScore(0);
        user.setAuthorLevelCode("seed");
        user.setBio("Keep a steady pace.");
        user.setGender("unknown");
        user.setTargetTrack("algo");
        user.setWeeklyGoal(10);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        user.setAuthorLevelUpdatedAt(OffsetDateTime.now());

        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("authorLevelCode", user.getAuthorLevelCode());
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
        private Boolean remember;

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

        public Boolean getRemember() {
            return remember;
        }

        public void setRemember(Boolean remember) {
            this.remember = remember;
        }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private String confirmPassword;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
