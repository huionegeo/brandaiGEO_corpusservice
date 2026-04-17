package com.brandai.knowledge.dto;

import lombok.Data;
import java.util.List;

public class KnowledgeDTO {

    @Data
    public static class NodeRequest {
        private String name;
        private String category;
        private String description;
        private String status;
    }

    @Data
    public static class KeywordRequest {
        private String word;
        private String platform;
        private String status;
        private Integer ranking;
    }

    @Data
    public static class NodeVO {
        private Long id;
        private Long brandId;
        private String name;
        private String category;
        private String description;
        private Integer generatedCount;
        private Integer publishedCount;
        private Integer includedCount;
        private Integer keywordCount;
        private String status;
        private List<KeywordVO> keywords;
    }

    @Data
    public static class KeywordVO {
        private Long id;
        private String word;
        private String platform;
        private String status;
        private Integer ranking;
    }

    @Data
    public static class RadarVO {
        private String category;
        private Integer score;
        private Integer nodeCount;
    }

    /** 批量创建节点请求（含关键词） */
    @Data
    public static class BatchNodeRequest {
        private String name;
        private String category;
        private String description;
        private String status;
        private List<KeywordRequest> keywords;
    }
}
