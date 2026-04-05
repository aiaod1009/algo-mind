package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.ForumComment;
import com.example.demo.entity.ForumPost;
import com.example.demo.entity.UserPostLike;
import com.example.demo.repository.ForumCommentRepository;
import com.example.demo.repository.ForumPostRepository;
import com.example.demo.repository.UserPostLikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class ForumPostController {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserPostLikeRepository userPostLikeRepository;

    private static final Long CURRENT_USER_ID = 1L;

    public ForumPostController(ForumPostRepository forumPostRepository, 
                               ForumCommentRepository forumCommentRepository,
                               UserPostLikeRepository userPostLikeRepository) {
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.userPostLikeRepository = userPostLikeRepository;
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

        Sort sortCondition;
        if ("hot".equals(sort)) {
            sortCondition = Sort.by(Sort.Direction.DESC, "likes");
        } else {
            sortCondition = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sortCondition);
        Page<ForumPost> postPage = forumPostRepository.findAll(pageable);
        List<ForumPost> posts = postPage.getContent();

        Set<Long> likedPostIds = userPostLikeRepository.findPostIdsByUserId(CURRENT_USER_ID);

        for (ForumPost post : posts) {
            post.setLiked(likedPostIds.contains(post.getId()));
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("list", posts);
        responseData.put("page", page);
        responseData.put("pageSize", pageSize);
        responseData.put("total", postPage.getTotalElements());

        return Result.success(responseData);
    }

    /**
     * 获取单个帖子详情
     * GET /api/forum-posts/{id}
     * 响应data：ForumPost
     */
    @GetMapping("/forum-posts/{id}")
    public Result<ForumPost> getPostById(@PathVariable Long id) {
        Optional<ForumPost> postOptional = forumPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return Result.fail(40401, "帖子不存在");
        }
        
        ForumPost post = postOptional.get();
        
        boolean liked = userPostLikeRepository.existsByUserIdAndPostId(CURRENT_USER_ID, id);
        post.setLiked(liked);
        
        return Result.success(post);
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
     * 请求体：{action: "like"} 或 {action: "unlike"}
     * 响应data：{likes: 521, liked: true}
     */
    @Transactional
    @PostMapping("/forum-posts/{id}/like")
    public Result<Map<String, Object>> likePost(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String action = request.get("action");
        if (action == null || (!"like".equals(action) && !"unlike".equals(action))) {
            return Result.fail(40001, "参数错误，仅支持action=like或unlike");
        }

        Optional<ForumPost> postOptional = forumPostRepository.findById(id);
        if (postOptional.isEmpty()) {
            return Result.fail(40401, "帖子不存在");
        }
        ForumPost post = postOptional.get();

        boolean alreadyLiked = userPostLikeRepository.existsByUserIdAndPostId(CURRENT_USER_ID, id);

        if ("like".equals(action)) {
            if (alreadyLiked) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("likes", post.getLikes());
                responseData.put("liked", true);
                return Result.success(responseData);
            }
            UserPostLike userPostLike = new UserPostLike();
            userPostLike.setUserId(CURRENT_USER_ID);
            userPostLike.setPostId(id);
            userPostLikeRepository.save(userPostLike);

            post.setLikes(post.getLikes() + 1);
            forumPostRepository.save(post);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("likes", post.getLikes());
            responseData.put("liked", true);
            return Result.success(responseData);

        } else {
            if (!alreadyLiked) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("likes", post.getLikes());
                responseData.put("liked", false);
                return Result.success(responseData);
            }
            userPostLikeRepository.deleteByUserIdAndPostId(CURRENT_USER_ID, id);

            post.setLikes(Math.max(0, post.getLikes() - 1));
            forumPostRepository.save(post);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("likes", post.getLikes());
            responseData.put("liked", false);
            return Result.success(responseData);
        }
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

    /**
     * 删除帖子
     * DELETE /api/forum-posts/{id}
     */
    @DeleteMapping("/forum-posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        if (!forumPostRepository.existsById(id)) {
            return Result.fail(40401, "帖子不存在");
        }
        forumPostRepository.deleteById(id);
        return Result.success(null);
    }

    @GetMapping("/forum-posts/{id}/comments")
    public Result<Map<String, Object>> getPostComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        if (!forumPostRepository.existsById(id)) {
            return Result.fail(40401, "帖子不存在");
        }

        List<ForumComment> allComments = forumCommentRepository.findByPostIdOrderByCreatedAtDesc(id);

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, allComments.size());
        List<ForumComment> pageComments = start < allComments.size()
                ? allComments.subList(start, end)
                : List.of();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("list", pageComments);
        responseData.put("page", page);
        responseData.put("pageSize", pageSize);
        responseData.put("total", allComments.size());

        return Result.success(responseData);
    }

    @GetMapping("/forum-posts/{id}/hot-comments")
    public Result<List<ForumComment>> getHotComments(@PathVariable Long id) {
        if (!forumPostRepository.existsById(id)) {
            return Result.fail(40401, "帖子不存在");
        }
        List<ForumComment> hotComments = forumCommentRepository.findTop3ByPostIdOrderByLikesDesc(id);
        return Result.success(hotComments);
    }
}