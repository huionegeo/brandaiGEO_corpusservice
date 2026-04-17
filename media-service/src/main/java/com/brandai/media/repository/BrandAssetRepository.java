package com.brandai.media.repository;

import com.brandai.media.domain.BrandAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandAssetRepository extends JpaRepository<BrandAsset, Long> {
    List<BrandAsset> findByBrandIdAndIsDeletedFalse(Long brandId);
    List<BrandAsset> findByBrandIdAndTypeAndIsDeletedFalse(Long brandId, BrandAsset.AssetType type);
    Optional<BrandAsset> findByIdAndBrandIdAndIsDeletedFalse(Long id, Long brandId);
}
