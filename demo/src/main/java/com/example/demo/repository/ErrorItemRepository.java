package com.example.demo.repository;

import com.example.demo.entity.ErrorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErrorItemRepository extends JpaRepository<ErrorItem, Long> {

    List<ErrorItem> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ErrorItem> findByIdAndUserId(Long id, Long userId);

    Optional<ErrorItem> findByUserIdAndLevelId(Long userId, Long levelId);

    void deleteByUserIdAndLevelId(Long userId, Long levelId);
}
