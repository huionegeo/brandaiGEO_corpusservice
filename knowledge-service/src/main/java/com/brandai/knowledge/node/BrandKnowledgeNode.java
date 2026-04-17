package com.brandai.knowledge.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * 品牌知识图谱节点（Neo4j）
 */
@Data
@Node("BrandKnowledge")
public class BrandKnowledgeNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("brandId")
    private Long brandId;

    @Property("name")
    private String name;

    /** 节点类别：产品/优势/品牌理念/口碑/地位/荣誉 */
    @Property("category")
    private String category;

    @Property("description")
    private String description;

    /** 已生成内容数量 */
    @Property("generatedCount")
    private Integer generatedCount = 0;

    /** 已发布内容数量 */
    @Property("publishedCount")
    private Integer publishedCount = 0;

    /** AI平台收录数量 */
    @Property("includedCount")
    private Integer includedCount = 0;

    /** 关联关键词数量 */
    @Property("keywordCount")
    private Integer keywordCount = 0;

    /** 优化状态：good / optimizing */
    @Property("status")
    private String status = "optimizing";

    @Relationship(type = "HAS_KEYWORD", direction = Relationship.Direction.OUTGOING)
    private List<KeywordNode> keywords = new ArrayList<>();
}
