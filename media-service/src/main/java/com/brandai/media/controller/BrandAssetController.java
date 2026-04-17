package com.brandai.media.controller;

import com.brandai.common.result.Result;
import com.brandai.media.domain.BrandAsset;
import com.brandai.media.service.BrandAssetService;
import com.brandai.media.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class BrandAssetController {

    private final BrandAssetService brandAssetService;
    private final FileStorageService fileStorageService;

    @PostMapping("/assets/upload")
    public Result<BrandAsset> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "OTHER") String type,
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestHeader("X-User-Id") Long userId) throws IOException {
        BrandAsset.AssetType assetType = BrandAsset.AssetType.valueOf(type.toUpperCase());
        BrandAsset asset = brandAssetService.upload(file, brandId, userId, assetType);
        return Result.success("上传成功", asset);
    }

    @GetMapping("/assets")
    public Result<List<BrandAsset>> list(
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestParam(required = false) String type) {
        return Result.success(brandAssetService.listByBrand(brandId, type));
    }

    @DeleteMapping("/assets/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId) {
        brandAssetService.delete(id, brandId);
        return Result.success();
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = fileStorageService.getFilePath(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(resource);
            }
        } catch (Exception e) {
            // fall through to 404
        }
        return ResponseEntity.notFound().build();
    }
}
