package com.brandai.corpus.domain;

import com.brandai.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容实体（PostgreSQL 持久化元数据）
 */
@Getter
@Setter
@Entity
@Table(name = "content")
public class Content extends BaseEntity {

    @Column(nullable = false)
    private Long brandId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentType type;

    /** 知识节点分类 */
    @Column(length = 100)
    private String nodeCategory;

    /** 所属节点名称 */
    @Column(length = 100)
    private String nodeName;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status = ContentStatus.DRAFT;

    /** 已发布的媒体渠道 */
    @ElementCollection
    @CollectionTable(name = "content_media", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "media_channel")
    private List<String> mediaChannels = new ArrayList<>();

    /** 已收录的 AI 平台 */
    @ElementCollection
    @CollectionTable(name = "content_ai_platform", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "platform")
    private List<String> aiIncludedPlatforms = new ArrayList<>();

    @Column
    private java.time.LocalDateTime publishedAt;

    public enum ContentType {
        ARTICLE, VIDEO, IMAGE, REPORT
    }

    public enum ContentStatus {
        DRAFT,        // 草稿
        PENDING,      // 待发布
        PUBLISHED,    // 已发布
        FAILED,       // 发布失败
        ARCHIVED      // 已归档
    }
}
