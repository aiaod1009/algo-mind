package com.example.demo.repository;

import com.example.demo.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    Page<ForumPost> findAll(Pageable pageable);

    Optional<ForumPost> findByTopic(String topic);

    long countByUserId(Long userId);

    @Query("select coalesce(sum(p.likes), 0) from ForumPost p where p.userId = :userId")
    Long sumLikesByUserId(@Param("userId") Long userId);

    @Query("select coalesce(sum(p.comments), 0) from ForumPost p where p.userId = :userId")
    Long sumCommentsByUserId(@Param("userId") Long userId);

    @Query("""
            select count(p) from ForumPost p
            where p.userId = :userId
              and (coalesce(p.likes, 0) >= :minLikes or coalesce(p.comments, 0) >= :minComments)
            """)
    long countHighImpactByUserId(@Param("userId") Long userId,
            @Param("minLikes") int minLikes,
            @Param("minComments") int minComments);
}
