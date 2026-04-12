package com.example.demo.repository;

import com.example.demo.entity.CompletedErrorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompletedErrorItemRepository extends JpaRepository<CompletedErrorItem, Long> {

    List<CompletedErrorItem> findByUserIdOrderByCompletedAtDesc(Long userId);

    Optional<CompletedErrorItem> findByUserIdAndLevelId(Long userId, Long levelId);

    void deleteByUserIdAndLevelId(Long userId, Long levelId);
}
