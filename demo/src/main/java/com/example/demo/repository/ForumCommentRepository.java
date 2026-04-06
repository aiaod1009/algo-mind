package com.example.demo.repository;

import com.example.demo.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    List<ForumComment> findByPostIdOrderByCreatedAtDesc(Long postId);

    List<ForumComment> findByPostId(Long postId);

    List<ForumComment> findTop3ByPostIdOrderByLikesDesc(Long postId);

    Optional<ForumComment> findFirstByPostIdAndUserIdAndContent(Long postId, Long userId, String content);

    long countByPostId(Long postId);
}
