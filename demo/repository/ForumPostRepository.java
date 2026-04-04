package com.example.demo.repository;

import com.example.demo.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 论坛帖子数据访问层
 */
@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    /**
     * 分页查询帖子，支持排序（最新/最热）
     * @param pageable 分页+排序条件
     * @return 分页结果
     */
    Page<ForumPost> findAll(Pageable pageable);
}