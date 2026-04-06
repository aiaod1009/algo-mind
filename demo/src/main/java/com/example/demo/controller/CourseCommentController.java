package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.auth.CurrentUserService;
import com.example.demo.entity.CourseComment;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseCommentRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class CourseCommentController {

    private final CourseCommentRepository commentRepository;
    private final CurrentUserService currentUserService;

    public CourseCommentController(CourseCommentRepository commentRepository,
                                   CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/courses/{courseId}/comments")
    public Result<List<Map<String, Object>>> getComments(@PathVariable Long courseId) {
        List<CourseComment> comments = commentRepository.findByCourseIdAndParentIdIsNullOrderByCreatedAtDesc(courseId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseComment comment : comments) {
            Map<String, Object> item = toMap(comment);
            List<CourseComment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            List<Map<String, Object>> replyList = new ArrayList<>();
            for (CourseComment reply : replies) {
                replyList.add(toMap(reply));
            }
            item.put("replies", replyList);
            result.add(item);
        }

        return Result.success(result);
    }

    @PostMapping("/courses/{courseId}/comments")
    public Result<Map<String, Object>> addComment(@PathVariable Long courseId,
                                                  @RequestBody Map<String, Object> body) {
        String content = body.get("content") == null ? "" : body.get("content").toString().trim();
        if (!StringUtils.hasText(content)) {
            return Result.fail(40001, "评论内容不能为空");
        }

        User currentUser = currentUserService.requireCurrentUserEntity();

        CourseComment comment = new CourseComment();
        comment.setCourseId(courseId);
        comment.setUserId(currentUser.getId());
        comment.setUserName(currentUser.getName());
        comment.setUserAvatar(currentUser.getAvatar());
        comment.setContent(content);

        if (body.get("parentId") != null) {
            comment.setParentId(Long.valueOf(body.get("parentId").toString()));
        }

        comment.setCreatedAt(LocalDateTime.now());
        comment.setLikes(0);

        CourseComment saved = commentRepository.save(comment);
        return Result.success(toMap(saved));
    }

    @PostMapping("/comments/{id}/like")
    public Result<Map<String, Object>> likeComment(@PathVariable Long id) {
        currentUserService.requireCurrentUserId();

        Optional<CourseComment> optional = commentRepository.findById(id);
        if (optional.isEmpty()) {
            return Result.fail(40401, "评论不存在");
        }

        CourseComment comment = optional.get();
        comment.setLikes((comment.getLikes() == null ? 0 : comment.getLikes()) + 1);
        commentRepository.save(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("likes", comment.getLikes());
        return Result.success(result);
    }

    private Map<String, Object> toMap(CourseComment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("userId", comment.getUserId());
        map.put("userName", comment.getUserName());
        map.put("userAvatar", comment.getUserAvatar());
        map.put("content", comment.getContent());
        map.put("likes", comment.getLikes());
        map.put("parentId", comment.getParentId());
        map.put("time", comment.getCreatedAt() != null
                ? comment.getCreatedAt().toString().replace("T", " ").substring(0, 16)
                : "");
        return map;
    }
}
