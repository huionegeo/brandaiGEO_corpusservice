package com.brandai.corpus.controller;

import com.brandai.common.dto.PageResult;
import com.brandai.common.result.Result;
import com.brandai.corpus.dto.ContentDTO;
import com.brandai.corpus.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容语料库 API
 */
@RestController
@RequestMapping("/api/corpus")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    /** 分页查询内容列表 */
    @GetMapping
    public Result<PageResult<ContentDTO.ContentVO>> list(
            @RequestHeader("X-Brand-Id") Long brandId,
            ContentDTO.QueryRequest query) {
        // 若有关键词则走 ES 搜索
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            try {
                List<ContentDTO.ContentVO> results = contentService.search(brandId, query.getKeyword());
                return Result.success(PageResult.of(results, results.size(), 1, results.size()));
            } catch (Exception e) {
                return Result.fail("搜索失败：" + e.getMessage());
            }
        }
        return Result.success(contentService.list(brandId, query));
    }

    /** 查看内容详情 */
    @GetMapping("/{id}")
    public Result<ContentDTO.ContentVO> getById(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId) {
        return Result.success(contentService.getById(id, brandId));
    }

    /** 创建内容 */
    @PostMapping
    public Result<ContentDTO.ContentVO> create(
            @RequestHeader("X-Brand-Id") Long brandId,
            @Valid @RequestBody ContentDTO.CreateRequest request) {
        return Result.success("内容创建成功", contentService.create(brandId, request));
    }

    /** 更新内容 */
    @PutMapping("/{id}")
    public Result<ContentDTO.ContentVO> update(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestBody ContentDTO.UpdateRequest request) {
        return Result.success("更新成功", contentService.update(id, brandId, request));
    }

    /** 发布内容 */
    @PostMapping("/{id}/publish")
    public Result<ContentDTO.ContentVO> publish(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId) {
        return Result.success("发布成功", contentService.publish(id, brandId));
    }

    /** 删除内容（软删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId) {
        contentService.delete(id, brandId);
        return Result.success();
    }
}
