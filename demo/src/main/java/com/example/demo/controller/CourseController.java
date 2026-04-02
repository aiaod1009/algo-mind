package com.example.demo.controller;

import com.example.demo.Result;
import com.example.demo.entity.Course;
import com.example.demo.repository.CourseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程接口，完全匹配开发文档扩展接口要求
 */
@RestController
public class CourseController {

    private final CourseRepository courseRepository;

    // 构造器注入
    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * 6.1 获取课程列表
     * GET /api/courses?track=algo|ds|contest&page=1&pageSize=20
     * 响应data：分页结构（list: Course[]）
     */
    @GetMapping("/courses")
    public Result<Map<String, Object>> getCourses(
            @RequestParam(required = false) String track,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        // 1. 筛选课程
        List<Course> courseList;
        if (track != null && !track.isBlank()) {
            courseList = courseRepository.findByTrack(track);
        } else {
            courseList = courseRepository.findAll();
        }

        // 2. 模拟分页（简单实现，实际项目用Pageable）
        int total = courseList.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Course> pageList = start < total ? courseList.subList(start, end) : List.of();

        // 3. 构造分页响应
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("list", pageList);
        responseData.put("page", page);
        responseData.put("pageSize", pageSize);
        responseData.put("total", total);

        // 文档要求：列表接口返回空数组，不返回null
        return Result.success(responseData);
    }
}