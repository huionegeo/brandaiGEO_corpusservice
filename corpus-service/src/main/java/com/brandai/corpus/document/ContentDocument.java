package com.brandai.corpus.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 文档（全文检索用）
 */
@Data
@Document(indexName = "content", createIndex = true)
public class ContentDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long contentId;

    @Field(type = FieldType.Long)
    private Long brandId;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String body;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String nodeCategory;

    @Field(type = FieldType.Keyword)
    private String nodeName;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private List<String> mediaChannels;

    @Field(type = FieldType.Keyword)
    private List<String> aiIncludedPlatforms;

    @Field(type = FieldType.Date)
    private LocalDateTime publishedAt;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
}
