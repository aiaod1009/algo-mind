package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.entity.ForumComment;
import com.example.demo.entity.ForumPost;
import com.example.demo.entity.User;
import com.example.demo.entity.UserPostLike;
import com.example.demo.repository.ForumCommentRepository;
import com.example.demo.repository.ForumPostRepository;
import com.example.demo.repository.UserPostLikeRepository;
import com.example.demo.util.IpLocation;
import com.example.demo.util.IpLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
public class ForumPostController {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserPostLikeRepository userPostLikeRepository;
    private final CurrentUserService currentUserService;
    private final IpLocationUtil ipLocationUtil;

    public ForumPostController(ForumPostRepository forumPostRepository,
                               ForumCommentRepository forumCommentRepository,
                               UserPostLikeRepository userPostLikeRepository,
                               CurrentUserService currentUserService,
                               IpLocationUtil ipLocationUtil) {
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.userPostLikeRepository = userPostLikeRepository;
        this.currentUserService = currentUserService;
        this.ipLocationUtil = ipLocationUtil;
    }

    @GetMapping("/forum-posts")
    public Result<Map<String, Object>> getForumPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "latest") String sort) {

        Sort sortCondition = "hot".equals(sort)
                ? Sort.by(Sort.Direction.DESC, "likes")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(pageSize, 1), sortCondition);
        Page<ForumPost> postPage = forumPostRepository.findAll(pageable);
        List<ForumPost> posts = postPage.getContent();

        Set<Long> likedPostIds = currentUserService.getCurrentUserId()
                .map(userPostLikeRepository::findPostIdsByUserId)
                .orElse(Set.of());
        posts.forEach(post -> post.setLiked(likedPostIds.contains(post.getId())));

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("list", posts);
        responseData.put("page", page);
        responseData.put("pageSize", pageSize);
        responseData.put("total", postPage.getTotalElements());
        return Result.success(responseData);
    }

    @GetMapping("/forum-posts/{id}")
    public Result<ForumPost> getPostById(@PathVariable Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElse(null);
        if (post == null) {
            return Result.fail(40401, "帖子不存在");
        }

        boolean liked = currentUserService.getCurrentUserId()
                .map(userId -> userPostLikeRepository.existsByUserIdAndPostId(userId, id))
                .orElse(false);
        post.setLiked(liked);
        return Result.success(post);
    }

    @PostMapping("/forum-posts")
    public Result<ForumPost> publishPost(@RequestBody ForumPost forumPost, HttpServletRequest request) {
        User currentUser = currentUserService.requireCurrentUserEntity();
        forumPost.setUserId(currentUser.getId());
        forumPost.setAuthor(currentUser.getName());
        forumPost.setAuthorLevel(currentUser.getLevel());
        forumPost.setAvatar(currentUser.getAvatar());

        if (!StringUtils.hasText(forumPost.getTopic())) {
            return Result.fail(40001, "帖子标题不能为空");
        }
        if (!StringUtils.hasText(forumPost.getContent())) {
            return Result.fail(40001, "帖子内容不能为空");
        }
        if (forumPost.getLikes() == null) {
            forumPost.setLikes(0);
        }
        if (forumPost.getComments() == null) {
            forumPost.setComments(0);
        }
        if (forumPost.getCreatedAt() == null) {
            forumPost.setCreatedAt(OffsetDateTime.now());
        }
        if (!StringUtils.hasText(forumPost.getLocation())) {
            IpLocation location = ipLocationUtil.getLocationDetail(request);
            forumPost.setLocation(location.getDisplayName());
        }

        ForumPost savedPost = forumPostRepository.save(forumPost);
        savedPost.setLiked(false);
        return Result.success(savedPost);
    }

    @Transactional
    @PostMapping("/forum-posts/{id}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long id,
                                                @RequestBody Map<String, String> request) {
        String action = request.get("action");
        if (!"like".equals(action) && !"unlike".equals(action)) {
            return Result.fail(40001, "参数错误，仅支持 action=like 或 unlike");
        }

        ForumPost post = forumPostRepository.findById(id)
                .orElse(null);
        if (post == null) {
            return Result.fail(40401, "帖子不存在");
        }

        Long currentUserId = currentUserService.requireCurrentUserId();
        boolean alreadyLiked = userPostLikeRepository.existsByUserIdAndPostId(currentUserId, id);

        if ("like".equals(action)) {
            if (!alreadyLiked) {
                UserPostLike userPostLike = new UserPostLike();
                userPostLike.setUserId(currentUserId);
                userPostLike.setPostId(id);
                userPostLikeRepository.save(userPostLike);

                post.setLikes(defaultInt(post.getLikes()) + 1);
                forumPostRepository.save(post);
            }
            return Result.success(buildLikeResponse(post.getLikes(), true));
        }

        if (alreadyLiked) {
            userPostLikeRepository.deleteByUserIdAndPostId(currentUserId, id);
            post.setLikes(Math.max(0, defaultInt(post.getLikes()) - 1));
            forumPostRepository.save(post);
        }
        return Result.success(buildLikeResponse(post.getLikes(), false));
    }

    @PostMapping("/forum-posts/{id}/comment-count")
    public Result<Map<String, Object>> updateCommentCount(@PathVariable Long id) {
        currentUserService.requireCurrentUserId();
        ForumPost post = forumPostRepository.findById(id)
                .orElse(null);
        if (post == null) {
            return Result.fail(40401, "帖子不存在");
        }

        post.setComments(defaultInt(post.getComments()) + 1);
        forumPostRepository.save(post);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("comments", post.getComments());
        return Result.success(responseData);
    }

    @DeleteMapping("/forum-posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        ForumPost post = forumPostRepository.findById(id).orElse(null);
        if (post == null) {
            return Result.fail(40401, "帖子不存在");
        }
        if (!canManagePost(post, currentUserId)) {
            return Result.fail(40301, "无权删除该帖子");
        }
        forumPostRepository.delete(post);
        return Result.success(null);
    }

    @GetMapping("/forum-posts/{id}/comments")
    public Result<Map<String, Object>> getPostComments(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer pageSize) {
        if (!forumPostRepository.existsById(id)) {
            return Result.fail(40401, "帖子不存在");
        }

        List<ForumComment> allComments = forumCommentRepository.findByPostIdOrderByCreatedAtDesc(id);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.max(pageSize, 1);
        int start = (safePage - 1) * safePageSize;
        int end = Math.min(start + safePageSize, allComments.size());
        List<ForumComment> pageComments = start < allComments.size()
                ? allComments.subList(start, end)
                : List.of();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("list", pageComments);
        responseData.put("page", safePage);
        responseData.put("pageSize", safePageSize);
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

    @PostMapping("/forum-posts/{id}/comments")
    @Transactional
    public Result<ForumComment> publishComment(@PathVariable Long id, @RequestBody ForumComment comment) {
        ForumPost post = forumPostRepository.findById(id)
                .orElse(null);
        if (post == null) {
            return Result.fail(40401, "帖子不存在");
        }
        if (!StringUtils.hasText(comment.getContent())) {
            return Result.fail(40001, "评论内容不能为空");
        }

        User currentUser = currentUserService.requireCurrentUserEntity();
        comment.setPostId(id);
        comment.setUserId(currentUser.getId());
        comment.setAuthor(currentUser.getName());
        comment.setAuthorLevel(currentUser.getLevel());
        comment.setAvatar(currentUser.getAvatar());
        if (comment.getLikes() == null) {
            comment.setLikes(0);
        }
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(OffsetDateTime.now());
        }

        ForumComment savedComment = forumCommentRepository.save(comment);
        post.setComments(defaultInt(post.getComments()) + 1);
        forumPostRepository.save(post);
        return Result.success(savedComment);
    }

    private Map<String, Object> buildLikeResponse(Integer likes, boolean liked) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("likes", defaultInt(likes));
        responseData.put("liked", liked);
        return responseData;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean canManagePost(ForumPost post, Long currentUserId) {
        if (post.getUserId() != null) {
            return post.getUserId().equals(currentUserId);
        }

        return currentUserService.getCurrentUser()
                .map(currentUser -> StringUtils.hasText(post.getAuthor()) && post.getAuthor().equals(currentUser.name()))
                .orElse(false);
    }
}
