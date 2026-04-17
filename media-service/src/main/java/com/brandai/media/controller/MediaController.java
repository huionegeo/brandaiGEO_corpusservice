package com.brandai.media.controller;

import com.brandai.common.result.Result;
import com.brandai.media.dto.MediaChannelDTO;
import com.brandai.media.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @GetMapping
    public Result<List<MediaChannelDTO.MediaChannelVO>> list(
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        return Result.success(mediaService.list(brandId, type, keyword));
    }

    @PostMapping("/{id}/authorize")
    public Result<MediaChannelDTO.MediaChannelVO> authorize(
            @RequestHeader("X-Brand-Id") Long brandId,
            @PathVariable Long id) {
        return Result.success("授权成功", mediaService.authorize(brandId, id));
    }

    @PostMapping("/{id}/unbind")
    public Result<MediaChannelDTO.MediaChannelVO> unbind(
            @RequestHeader("X-Brand-Id") Long brandId,
            @PathVariable Long id) {
        return Result.success("解绑成功", mediaService.unbind(brandId, id));
    }

    @PostMapping("/{id}/plan")
    public Result<MediaChannelDTO.MediaChannelVO> addToPlan(
            @RequestHeader("X-Brand-Id") Long brandId,
            @PathVariable Long id,
            @RequestBody(required = false) MediaChannelDTO.AddToPlanRequest request) {
        int count = request != null ? request.getArticleCount() : 1;
        return Result.success("已加入优化计划", mediaService.addToPlan(brandId, id, count));
    }

    @PostMapping("/self")
    public Result<MediaChannelDTO.MediaChannelVO> createSelfMedia(
            @RequestHeader("X-Brand-Id") Long brandId,
            @Valid @RequestBody MediaChannelDTO.CreateSelfMediaRequest request) {
        return Result.success("账号添加成功", mediaService.createSelfMedia(brandId, request));
    }
}
