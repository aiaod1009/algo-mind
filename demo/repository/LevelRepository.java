package com.example.demo.repository;

import com.example.demo.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 关卡数据访问层
 */
@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    /**
     * 根据赛道筛选关卡（匹配文档的track参数）
     */
    List<Level> findByTrack(String track);

    /**
     * 根据排序号查询下一关
     */
    Level findByOrder(Integer order);
}