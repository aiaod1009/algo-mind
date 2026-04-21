package com.example.demo.repository;

import com.example.demo.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    Optional<KnowledgeArticle> findBySlugAndPublishedTrue(String slug);

    Optional<KnowledgeArticle> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<KnowledgeArticle> findAllBySlugIn(Collection<String> slugs);

    List<KnowledgeArticle> findAllByPublishedTrueOrderBySectionIdAscSortOrderAscUpdatedAtDesc();

    List<KnowledgeArticle> findAllByOrderBySectionIdAscSortOrderAscUpdatedAtDesc();
}
