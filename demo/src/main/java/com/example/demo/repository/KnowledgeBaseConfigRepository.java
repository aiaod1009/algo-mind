package com.example.demo.repository;

import com.example.demo.entity.KnowledgeBaseConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeBaseConfigRepository extends JpaRepository<KnowledgeBaseConfig, Long> {
}
