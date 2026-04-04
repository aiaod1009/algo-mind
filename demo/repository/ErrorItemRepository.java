package com.example.demo.repository;

import com.example.demo.entity.ErrorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 错题数据访问层
 */
@Repository
public interface ErrorItemRepository extends JpaRepository<ErrorItem, Long> {

}