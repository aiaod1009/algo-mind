package com.example.demo.repository;

import com.example.demo.entity.UserAuthorLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAuthorLevelHistoryRepository extends JpaRepository<UserAuthorLevelHistory, Long> {

    List<UserAuthorLevelHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
}
