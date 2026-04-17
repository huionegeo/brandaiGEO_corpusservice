package com.brandai.knowledge.repository;

import com.brandai.knowledge.node.BrandKnowledgeNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeNodeRepository extends Neo4jRepository<BrandKnowledgeNode, Long> {

    List<BrandKnowledgeNode> findByBrandId(Long brandId);

    List<BrandKnowledgeNode> findByBrandIdAndCategory(Long brandId, String category);

    Optional<BrandKnowledgeNode> findByBrandIdAndName(Long brandId, String name);

    @Query("MATCH (n:BrandKnowledge {brandId: $brandId})-[:HAS_KEYWORD]->(k:Keyword) " +
           "RETURN n, collect(k) ORDER BY n.category")
    List<BrandKnowledgeNode> findAllWithKeywords(Long brandId);

}
