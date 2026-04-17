package com.brandai.knowledge.controller;

import com.brandai.common.result.Result;
import com.brandai.knowledge.dto.KnowledgeDTO;
import com.brandai.knowledge.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱 API
 */
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /** 获取图谱节点列表 */
    @GetMapping("/nodes")
    public Result<List<KnowledgeDTO.NodeVO>> listNodes(
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestParam(required = false) String category) {
        return Result.success(knowledgeService.listNodes(brandId, category));
    }

    /** 获取雷达图数据 */
    @GetMapping("/radar")
    public Result<List<KnowledgeDTO.RadarVO>> getRadar(
            @RequestHeader("X-Brand-Id") Long brandId) {
        return Result.success(knowledgeService.getRadarData(brandId));
    }

    /** 创建知识节点 */
    @PostMapping("/nodes")
    public Result<KnowledgeDTO.NodeVO> createNode(
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestBody KnowledgeDTO.NodeRequest request) {
        return Result.success("节点创建成功", knowledgeService.createNode(brandId, request));
    }

    /** 更新知识节点 */
    @PutMapping("/nodes/{id}")
    public Result<KnowledgeDTO.NodeVO> updateNode(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestBody KnowledgeDTO.NodeRequest request) {
        return Result.success("更新成功", knowledgeService.updateNode(id, brandId, request));
    }

    /** 为节点添加关键词 */
    @PostMapping("/nodes/{id}/keywords")
    public Result<KnowledgeDTO.NodeVO> addKeyword(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestBody KnowledgeDTO.KeywordRequest request) {
        return Result.success("关键词已添加", knowledgeService.addKeyword(id, brandId, request));
    }

    /** 删除节点 */
    @DeleteMapping("/nodes/{id}")
    public Result<Void> deleteNode(
            @PathVariable Long id,
            @RequestHeader("X-Brand-Id") Long brandId) {
        knowledgeService.deleteNode(id, brandId);
        return Result.success();
    }

    /** 批量创建节点（含关键词），由 AI 服务调用 */
    @PostMapping("/nodes/batch")
    public Result<List<KnowledgeDTO.NodeVO>> batchCreateNodes(
            @RequestHeader("X-Brand-Id") Long brandId,
            @RequestBody List<KnowledgeDTO.BatchNodeRequest> requests) {
        return Result.success("批量创建成功", knowledgeService.batchCreateNodes(brandId, requests));
    }
}
