package com.example.demo.repository;

import com.example.demo.entity.UserPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserPostLikeRepository extends JpaRepository<UserPostLike, Long> {

    Optional<UserPostLike> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Modifying
    @Query("DELETE FROM UserPostLike u WHERE u.userId = :userId AND u.postId = :postId")
    void deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    List<UserPostLike> findByUserIdAndPostIdIn(Long userId, List<Long> postIds);

    @Query("SELECT u.postId FROM UserPostLike u WHERE u.userId = :userId")
    Set<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
