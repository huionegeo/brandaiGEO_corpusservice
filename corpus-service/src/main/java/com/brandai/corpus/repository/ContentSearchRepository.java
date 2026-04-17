package com.brandai.corpus.repository;

import com.brandai.corpus.document.ContentDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentSearchRepository extends ElasticsearchRepository<ContentDocument, String> {

    List<ContentDocument> findByBrandId(Long brandId);

    void deleteByContentId(Long contentId);
}
