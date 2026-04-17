package com.brandai.media.domain;

import com.brandai.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "brand_asset")
public class BrandAsset extends BaseEntity {

    @Column(nullable = false)
    private Long brandId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetType type;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 255)
    private String storedName;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 100)
    private String mimeType;

    public enum AssetType {
        LOGO, PRODUCT, OTHER
    }
}
