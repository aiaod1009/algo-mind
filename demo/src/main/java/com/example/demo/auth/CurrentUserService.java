package com.example.demo.auth;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<AuthenticatedUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser);
        }

        return Optional.empty();
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(AuthenticatedUser::id);
    }

    public Long requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("未登录或登录已过期"));
    }

    public User requireCurrentUserEntity() {
        Long currentUserId = requireCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("当前用户不存在"));
    }
}
