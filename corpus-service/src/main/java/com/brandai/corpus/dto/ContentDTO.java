package com.brandai.corpus.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class ContentDTO {

    @Data
    public static class ContentRequest {
        private String title;
        private String type;   // article / video / image / report
        private String body;
        private String summary;
        private String nodeCategory;
        private String nodeName;
        private List<String> mediaChannels;
    }

    @Data
    public static class CreateRequest extends ContentRequest {
        @NotBlank(message = "标题不能为空")
        private String title;
        @NotBlank(message = "内容类型不能为空")
        private String type;
    }

    @Data
    public static class UpdateRequest extends ContentRequest {
    }

    @Data
    public static class QueryRequest {
        private int page = 1;
        private int pageSize = 20;
        private String type;
        private String status;
        private String nodeCategory;
        private String keyword;
    }

    @Data
    public static class ContentVO {
        private Long id;
        private Long brandId;
        private String title;
        private String type;
        private String body;
        private String summary;
        private String nodeCategory;
        private String nodeName;
        private String status;
        private List<String> mediaChannels;
        private List<String> aiIncludedPlatforms;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
    }
}
