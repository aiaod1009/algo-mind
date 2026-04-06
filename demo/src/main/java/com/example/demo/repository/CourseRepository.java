package com.example.demo.repository;

import com.example.demo.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课程数据访问层
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * 根据赛道筛选课程
     * @param track 赛道：algo|ds|contest
     * @return 课程列表
     */
    List<Course> findByTrack(String track);
}