package com.brandai.knowledge.node;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * 关键词节点（Neo4j）
 */
@Data
@Node("Keyword")
public class KeywordNode {

    @Id
    @GeneratedValue
    private Long id;

    @Property("word")
    private String word;

    /** 目标 AI 平台 */
    @Property("platform")
    private String platform;

    /** 收录状态：已收录 / 优化中 / 未收录 */
    @Property("status")
    private String status = "未收录";

    /** 搜索排名 */
    @Property("ranking")
    private Integer ranking;

    @Property("brandId")
    private Long brandId;
}
