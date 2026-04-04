package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // 创建新用户，不设置ID，让数据库自动生成
        User newUser = new User();
        newUser.setName("系统管理员");
        newUser.setEmail("admin@example.com");
        newUser.setPassword("admin");
        newUser.setPoints(999);
        newUser.setBio("先把基础打扎实，再冲更高难度。");
        newUser.setGender("unknown");
        newUser.setTargetTrack("algo");
        newUser.setWeeklyGoal(10);
        newUser.setCreatedAt(OffsetDateTime.now());
        newUser.setUpdatedAt(OffsetDateTime.now());
        return userRepository.save(newUser);
    }

    @Transactional
    public User saveUser(User user) {
        user.setUpdatedAt(OffsetDateTime.now());
        return userRepository.save(user);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
