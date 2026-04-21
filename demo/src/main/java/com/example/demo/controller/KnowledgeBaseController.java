package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.KnowledgeBaseService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/knowledge-base")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public KnowledgeBaseController(
            KnowledgeBaseService knowledgeBaseService,
            CurrentUserService currentUserService,
            UserRepository userRepository
    ) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @GetMapping("/catalog")
    public Result<KnowledgeBaseService.KnowledgeCatalog> getCatalog(
            @RequestParam(required = false) String keyword
    ) {
        return Result.success(knowledgeBaseService.getCatalog(keyword));
    }

    @GetMapping("/articles/{slug}")
    public Result<?> getArticle(@PathVariable String slug) {
        try {
            return Result.success(knowledgeBaseService.getArticle(slug));
        } catch (NoSuchElementException exception) {
            return Result.fail(40404, exception.getMessage());
        }
    }

    @GetMapping("/admin/dashboard")
    public Result<?> getAdminDashboard() {
        Optional<User> adminUser = requireAdminUser();
        if (adminUser.isEmpty()) {
            return unauthorizedResult();
        }
        return Result.success(knowledgeBaseService.getAdminDashboard());
    }

    @GetMapping("/admin/articles/{id}")
    public Result<?> getAdminArticle(@PathVariable Long id) {
        Optional<User> adminUser = requireAdminUser();
        if (adminUser.isEmpty()) {
            return unauthorizedResult();
        }

        try {
            return Result.success(knowledgeBaseService.getAdminArticle(id));
        } catch (NoSuchElementException exception) {
            return Result.fail(40404, exception.getMessage());
        }
    }

    @PostMapping("/admin/articles")
    public Result<?> createArticle(@RequestBody KnowledgeBaseService.AdminArticleInput input) {
        Optional<User> adminUser = requireAdminUser();
        if (adminUser.isEmpty()) {
            return unauthorizedResult();
        }

        try {
            return Result.success(knowledgeBaseService.createArticle(input, adminUser.get().getId()));
        } catch (IllegalArgumentException exception) {
            return Result.fail(40001, exception.getMessage());
        }
    }

    @PutMapping("/admin/articles/{id}")
    public Result<?> updateArticle(
            @PathVariable Long id,
            @RequestBody KnowledgeBaseService.AdminArticleInput input
    ) {
        Optional<User> adminUser = requireAdminUser();
        if (adminUser.isEmpty()) {
            return unauthorizedResult();
        }

        try {
            return Result.success(knowledgeBaseService.updateArticle(id, input, adminUser.get().getId()));
        } catch (IllegalArgumentException exception) {
            return Result.fail(40001, exception.getMessage());
        } catch (NoSuchElementException exception) {
            return Result.fail(40404, exception.getMessage());
        }
    }

    @DeleteMapping("/admin/articles/{id}")
    public Result<?> deleteArticle(@PathVariable Long id) {
        Optional<User> adminUser = requireAdminUser();
        if (adminUser.isEmpty()) {
            return unauthorizedResult();
        }

        try {
            knowledgeBaseService.deleteArticle(id);
            return Result.success("ok");
        } catch (NoSuchElementException exception) {
            return Result.fail(40404, exception.getMessage());
        }
    }

    @PutMapping("/admin/config")
    public Result<?> updateConfig(@RequestBody KnowledgeBaseService.AdminConfigInput input) {
        Optional<User> adminUser = requireAdminUser();
        if (adminUser.isEmpty()) {
            return unauthorizedResult();
        }
        return Result.success(knowledgeBaseService.updateConfig(input));
    }

    private Optional<User> requireAdminUser() {
        return currentUserService.getCurrentUserId()
                .flatMap(userRepository::findById)
                .filter(user -> Boolean.TRUE.equals(user.getIsAdmin()));
    }

    private Result<?> unauthorizedResult() {
        if (currentUserService.getCurrentUserId().isEmpty()) {
            return Result.fail(40101, "请先登录管理员账号");
        }
        return Result.fail(40301, "当前账号没有知识库管理权限");
    }
}
