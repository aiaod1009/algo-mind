package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.ForumPost;
import com.example.demo.repository.ForumPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 论坛帖子接口，完全匹配开发文档要求
 */
@RestController
public class ForumPostController {

    private final ForumPostRepository forumPostRepository;

    // 构造器注入
    public ForumPostController(ForumPostRepository forumPostRepository) {
        this.forumPostRepository = forumPostRepository;
    }

    /**
     * 5.7 论坛帖子列表
     * GET /api/forum-posts
     * Query：page=1、pageSize=20、sort=latest|hot
     * 响应data：分页结构（list: ForumPost[]）
     */
    @GetMapping("/forum-posts")
    public Result<Map<String, Object>> getForumPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "latest") String sort) {

        // 构建排序条件：latest=按创建时间降序，hot=按点赞数降序
        Sort sortCondition;
        if ("hot".equals(sort)) {
            sortCondition = Sort.by(Sort.Direction.DESC, "likes");
        } else {
            sortCondition = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        // 分页查询（Spring Data JPA的page从0开始，前端从1开始，需减1）
        Pageable pageable = PageRequest.of(page - 1, pageSize, sortCondition);
        Page<ForumPost> postPage = forumPostRepository.findAll(pageable);

        // 构造分页响应（匹配开发文档分页结构）
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("list", postPage.getContent());
        responseData.put("page", page);
        responseData.put("pageSize", pageSize);
        responseData.put("total", postPage.getTotalElements());

        return Result.success(responseData);
    }

    /**
     * 5.8 发布帖子
     * POST /api/forum-posts
     * 请求体：ForumPost（author/authorLevel/avatar/topic/content/quote/tag）
     * 响应data：创建后的ForumPost
     */
    @PostMapping("/forum-posts")
    public Result<ForumPost> publishPost(@RequestBody ForumPost forumPost) {
        // 补全默认值
        if (forumPost.getLikes() == null) {
            forumPost.setLikes(0);
        }
        if (forumPost.getComments() == null) {
            forumPost.setComments(0);
        }
        if (forumPost.getCreatedAt() == null) {
            forumPost.setCreatedAt(java.time.OffsetDateTime.now());
        }

        // 保存并返回创建后的帖子
        ForumPost savedPost = forumPostRepository.save(forumPost);
        return Result.success(savedPost);
    }

    /**
     * 6.2 点赞帖子
     * POST /api/forum-posts/{id}/like
     * 请求体：{action: "like"}
     * 响应data：{likes: 521, liked: true}
     */
    @PostMapping("/forum-posts/{id}/like")
    public Result<Map<String, Object>> likePost(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        // 校验参数
        String action = request.get("action");
        if (action == null || !"like".equals(action)) {
            return Result.fail(40001, "参数错误，仅支持action=like");
        }

        // 查询帖子
        Optional<ForumPost> postOptional = forumPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return Result.fail(40401, "帖子不存在");
        }
        ForumPost post = postOptional.get();

        // 点赞逻辑（简单实现：每次点赞+1，liked=true）
        post.setLikes(post.getLikes() + 1);
        forumPostRepository.save(post);

        // 构造响应
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("likes", post.getLikes());
        responseData.put("liked", true);

        return Result.success(responseData);
    }

    /**
     * 6.3 评论计数（简版）
     * POST /api/forum-posts/{id}/comment-count
     * 响应data：{comments: 30}
     */
    @PostMapping("/forum-posts/{id}/comment-count")
    public Result<Map<String, Object>> updateCommentCount(@PathVariable Long id) {
        // 查询帖子
        Optional<ForumPost> postOptional = forumPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return Result.fail(40401, "帖子不存在");
        }
        ForumPost post = postOptional.get();

        // 评论数+1（简版实现）
        post.setComments(post.getComments() + 1);
        forumPostRepository.save(post);

        // 构造响应
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("comments", post.getComments());

        return Result.success(responseData);
    }
}