package com.brandai.media.domain;

import com.brandai.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 媒体渠道实体
 */
@Getter
@Setter
@Entity
@Table(name = "media_channel")
public class MediaChannel extends BaseEntity {

    @Column(nullable = false)
    private Long brandId;

    @Column(nullable = false, length = 100)
    private String name;

    /** 渠道类型：official（权威官媒）/ kol（KOL账号）/ self（品牌自媒体） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType type;

    /** 所在平台：微博/微信/抖音/B站/小红书 等 */
    @Column(length = 50)
    private String platform;

    /** 账号名称（自媒体） */
    @Column(length = 100)
    private String accountName;

    /** 授权状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthStatus authStatus = AuthStatus.UNAUTHORIZED;

    /** 刊例价（官媒/KOL） */
    @Column(length = 50)
    private String listPrice;

    /** 粉丝数量描述 */
    @Column(length = 50)
    private String followersCount;

    /** 联系方式 */
    @Column(length = 200)
    private String contactInfo;

    /** 是否已加入优化计划 */
    @Column(nullable = false)
    private Boolean inPlan = false;

    public enum ChannelType {
        OFFICIAL, KOL, SELF
    }

    public enum AuthStatus {
        AUTHORIZED, UNAUTHORIZED, PUBLISHABLE
    }
}
