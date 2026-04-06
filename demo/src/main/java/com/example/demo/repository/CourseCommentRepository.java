package com.example.demo.repository;

import com.example.demo.entity.CourseComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseCommentRepository extends JpaRepository<CourseComment, Long> {
    List<CourseComment> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<CourseComment> findByCourseIdAndParentIdIsNullOrderByCreatedAtDesc(Long courseId);
    List<CourseComment> findByParentIdOrderByCreatedAtAsc(Long parentId);
}
