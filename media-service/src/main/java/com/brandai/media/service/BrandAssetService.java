package com.brandai.media.service;

import com.brandai.media.domain.BrandAsset;
import com.brandai.media.repository.BrandAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BrandAssetService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final BrandAssetRepository brandAssetRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public BrandAsset upload(MultipartFile file, Long brandId, Long userId,
                             BrandAsset.AssetType type) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("仅支持 JPG/PNG/GIF/WebP/SVG 格式图片");
        }

        // LOGO 类型: 软删除旧 LOGO
        if (type == BrandAsset.AssetType.LOGO) {
            List<BrandAsset> oldLogos = brandAssetRepository
                    .findByBrandIdAndTypeAndIsDeletedFalse(brandId, BrandAsset.AssetType.LOGO);
            oldLogos.forEach(old -> {
                old.setIsDeleted(true);
                brandAssetRepository.save(old);
            });
        }

        String storedName = fileStorageService.store(file);

        BrandAsset asset = new BrandAsset();
        asset.setBrandId(brandId);
        asset.setUserId(userId);
        asset.setType(type);
        asset.setOriginalName(file.getOriginalFilename());
        asset.setStoredName(storedName);
        asset.setUrl("/api/media/files/" + storedName);
        asset.setFileSize(file.getSize());
        asset.setMimeType(contentType);

        return brandAssetRepository.save(asset);
    }

    public List<BrandAsset> listByBrand(Long brandId, String type) {
        if (type != null && !type.isBlank()) {
            return brandAssetRepository.findByBrandIdAndTypeAndIsDeletedFalse(
                    brandId, BrandAsset.AssetType.valueOf(type.toUpperCase()));
        }
        return brandAssetRepository.findByBrandIdAndIsDeletedFalse(brandId);
    }

    @Transactional
    public void delete(Long id, Long brandId) {
        BrandAsset asset = brandAssetRepository.findByIdAndBrandIdAndIsDeletedFalse(id, brandId)
                .orElseThrow(() -> new RuntimeException("素材不存在"));
        asset.setIsDeleted(true);
        brandAssetRepository.save(asset);
    }
}
