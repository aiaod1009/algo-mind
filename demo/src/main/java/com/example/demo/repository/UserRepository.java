package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户（登录用）
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findFirstByName(String username);

    /**
     * 按赛道+积分排序查询排行榜
     * @param track 赛道（all则查全部）
     * @return 按积分降序的用户列表
     */
    @Query(value = "SELECT u FROM User u WHERE :track = 'all' OR u.targetTrack = :track ORDER BY u.points DESC")
    List<User> findRankingByTrack(@Param("track") String track);
}