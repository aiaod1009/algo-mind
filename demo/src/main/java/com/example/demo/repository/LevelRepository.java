package com.example.demo.repository;

import com.example.demo.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    List<Level> findByTrack(String track);

    Level findByOrder(Integer order);

    boolean existsByTrackAndOrder(String track, Integer order);

    Optional<Level> findByTrackAndOrder(String track, Integer order);

    Page<Level> findByCreatorIdOrderByIdDesc(Long creatorId, Pageable pageable);

    long countByCreatorId(Long creatorId);
}
