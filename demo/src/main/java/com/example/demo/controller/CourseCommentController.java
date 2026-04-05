package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.CourseComment;
import com.example.demo.repository.CourseCommentRepository;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class CourseCommentController {

    private final CourseCommentRepository commentRepository;

    public CourseCommentController(CourseCommentRepository commentRepository) {
        this.commentRepository = commentRepository;
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
    public Result<Map<String, Object>> addComment(
            @PathVariable Long courseId,
            @RequestBody Map<String, Object> body) {
        
        CourseComment comment = new CourseComment();
        comment.setCourseId(courseId);
        comment.setUserId(body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : 1L);
        comment.setUserName((String) body.getOrDefault("userName", "匿名用户"));
        comment.setUserAvatar((String) body.get("userAvatar"));
        comment.setContent((String) body.get("content"));
        
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
        Optional<CourseComment> optional = commentRepository.findById(id);
        if (optional.isEmpty()) {
            return Result.fail(40401, "评论不存在");
        }
        
        CourseComment comment = optional.get();
        comment.setLikes(comment.getLikes() + 1);
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
        map.put("time", comment.getCreatedAt() != null ? 
            comment.getCreatedAt().toString().replace("T", " ").substring(0, 16) : "");
        return map;
    }
}
