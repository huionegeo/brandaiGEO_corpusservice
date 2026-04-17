package com.brandai.corpus.repository;

import com.brandai.corpus.domain.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    Page<Content> findByBrandIdAndIsDeletedFalse(Long brandId, Pageable pageable);

    Page<Content> findByBrandIdAndTypeAndIsDeletedFalse(
            Long brandId, Content.ContentType type, Pageable pageable);

    Page<Content> findByBrandIdAndStatusAndIsDeletedFalse(
            Long brandId, Content.ContentStatus status, Pageable pageable);

    long countByBrandIdAndIsDeletedFalse(Long brandId);
}
