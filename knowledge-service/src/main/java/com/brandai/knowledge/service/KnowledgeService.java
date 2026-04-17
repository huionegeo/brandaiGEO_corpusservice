package com.brandai.knowledge.service;

import com.brandai.common.exception.BusinessException;
import com.brandai.knowledge.dto.KnowledgeDTO;
import com.brandai.knowledge.node.BrandKnowledgeNode;
import com.brandai.knowledge.node.KeywordNode;
import com.brandai.knowledge.repository.KnowledgeNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识图谱服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeNodeRepository nodeRepository;

    @Transactional
    public List<KnowledgeDTO.NodeVO> listNodes(Long brandId, String category) {
        List<BrandKnowledgeNode> nodes;
        if (category != null) {
            nodes = nodeRepository.findByBrandIdAndCategory(brandId, category);
        } else {
            nodes = nodeRepository.findByBrandId(brandId);
            if (nodes.isEmpty()) {
                nodes = seedDefaultNodes(brandId);
            }
        }
        return nodes.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public KnowledgeDTO.NodeVO createNode(Long brandId, KnowledgeDTO.NodeRequest request) {
        BrandKnowledgeNode node = new BrandKnowledgeNode();
        node.setBrandId(brandId);
        node.setName(request.getName());
        node.setCategory(request.getCategory());
        node.setDescription(request.getDescription());
        node = nodeRepository.save(node);
        log.info("知识节点创建: id={}, name={}, brandId={}", node.getId(), node.getName(), brandId);
        return toVO(node);
    }

    @Transactional
    public KnowledgeDTO.NodeVO updateNode(Long id, Long brandId, KnowledgeDTO.NodeRequest request) {
        BrandKnowledgeNode node = findNode(id, brandId);
        if (request.getName() != null) node.setName(request.getName());
        if (request.getCategory() != null) node.setCategory(request.getCategory());
        if (request.getDescription() != null) node.setDescription(request.getDescription());
        if (request.getStatus() != null) node.setStatus(request.getStatus());
        node = nodeRepository.save(node);
        return toVO(node);
    }

    @Transactional
    public KnowledgeDTO.NodeVO addKeyword(Long nodeId, Long brandId, KnowledgeDTO.KeywordRequest request) {
        BrandKnowledgeNode node = findNode(nodeId, brandId);
        KeywordNode keyword = new KeywordNode();
        keyword.setWord(request.getWord());
        keyword.setPlatform(request.getPlatform());
        keyword.setStatus(request.getStatus() != null ? request.getStatus() : "未收录");
        keyword.setRanking(request.getRanking());
        keyword.setBrandId(brandId);
        node.getKeywords().add(keyword);
        node.setKeywordCount(node.getKeywords().size());
        node = nodeRepository.save(node);
        return toVO(node);
    }

    @Transactional
    public void deleteNode(Long id, Long brandId) {
        BrandKnowledgeNode node = findNode(id, brandId);
        nodeRepository.delete(node);
    }

    /** 获取图谱雷达图数据（按 category 聚合各维度分数） */
    @Transactional
    public List<KnowledgeDTO.RadarVO> getRadarData(Long brandId) {
        List<BrandKnowledgeNode> nodes = nodeRepository.findByBrandId(brandId);
        if (nodes.isEmpty()) {
            nodes = seedDefaultNodes(brandId);
        }
        return nodes.stream()
                .collect(Collectors.groupingBy(BrandKnowledgeNode::getCategory))
                .entrySet().stream()
                .map(entry -> {
                    String cat = entry.getKey();
                    List<BrandKnowledgeNode> catNodes = entry.getValue();
                    int totalIncluded = catNodes.stream().mapToInt(n -> n.getIncludedCount() != null ? n.getIncludedCount() : 0).sum();
                    int totalGenerated = catNodes.stream().mapToInt(n -> n.getGeneratedCount() != null ? n.getGeneratedCount() : 0).sum();
                    double score = totalGenerated > 0 ? Math.min(100.0, (double) totalIncluded / totalGenerated * 100) : 0;
                    KnowledgeDTO.RadarVO vo = new KnowledgeDTO.RadarVO();
                    vo.setCategory(cat);
                    vo.setScore((int) score);
                    vo.setNodeCount(catNodes.size());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /** 批量创建知识节点（含关键词），由 AI 服务生成图谱时调用 */
    @Transactional
    public List<KnowledgeDTO.NodeVO> batchCreateNodes(Long brandId, List<KnowledgeDTO.BatchNodeRequest> requests) {
        // 先清除旧节点
        List<BrandKnowledgeNode> existing = nodeRepository.findByBrandId(brandId);
        if (!existing.isEmpty()) {
            nodeRepository.deleteAll(existing);
            log.info("已清除旧知识节点: brandId={}, count={}", brandId, existing.size());
        }

        List<BrandKnowledgeNode> created = new ArrayList<>();
        for (KnowledgeDTO.BatchNodeRequest req : requests) {
            BrandKnowledgeNode node = new BrandKnowledgeNode();
            node.setBrandId(brandId);
            node.setName(req.getName());
            node.setCategory(req.getCategory());
            node.setDescription(req.getDescription());
            node.setStatus(req.getStatus() != null ? req.getStatus() : "optimizing");

            // 添加关键词
            if (req.getKeywords() != null && !req.getKeywords().isEmpty()) {
                for (KnowledgeDTO.KeywordRequest kr : req.getKeywords()) {
                    KeywordNode kw = new KeywordNode();
                    kw.setWord(kr.getWord());
                    kw.setPlatform(kr.getPlatform());
                    kw.setStatus(kr.getStatus() != null ? kr.getStatus() : "未收录");
                    kw.setRanking(kr.getRanking());
                    kw.setBrandId(brandId);
                    node.getKeywords().add(kw);
                }
                node.setKeywordCount(node.getKeywords().size());
            }

            created.add(node);
        }

        List<BrandKnowledgeNode> saved = nodeRepository.saveAll(created);
        log.info("批量创建知识节点: brandId={}, count={}", brandId, saved.size());
        return saved.stream().map(this::toVO).collect(Collectors.toList());
    }

    /** 为新品牌初始化默认知识节点 */
    private List<BrandKnowledgeNode> seedDefaultNodes(Long brandId) {
        // 并发防护：事务内再次检查
        List<BrandKnowledgeNode> existing = nodeRepository.findByBrandId(brandId);
        if (!existing.isEmpty()) return existing;

        log.info("初始化品牌知识节点: brandId={}", brandId);
        List<BrandKnowledgeNode> seeds = new ArrayList<>();
        seeds.add(buildSeed(brandId, "产品与服务", "核心产品A", 120, 85, 70, 15, "optimizing"));
        seeds.add(buildSeed(brandId, "产品与服务", "核心产品B", 95, 68, 55, 12, "optimizing"));
        seeds.add(buildSeed(brandId, "产品与服务", "售后服务体系", 45, 30, 25, 8, "good"));
        seeds.add(buildSeed(brandId, "产品与服务", "行业解决方案", 60, 42, 30, 10, "optimizing"));
        seeds.add(buildSeed(brandId, "核心优势", "技术领先性", 110, 80, 65, 18, "good"));
        seeds.add(buildSeed(brandId, "核心优势", "服务专业性", 75, 55, 42, 11, "optimizing"));
        seeds.add(buildSeed(brandId, "核心优势", "行业洞察力", 50, 35, 28, 9, "optimizing"));
        seeds.add(buildSeed(brandId, "品牌理念", "愿景与使命", 200, 150, 140, 30, "good"));
        seeds.add(buildSeed(brandId, "品牌理念", "核心价值观", 80, 60, 40, 12, "optimizing"));
        seeds.add(buildSeed(brandId, "品牌理念", "企业文化", 65, 48, 38, 10, "good"));
        seeds.add(buildSeed(brandId, "用户口碑", "客户成功案例", 90, 70, 58, 14, "good"));
        seeds.add(buildSeed(brandId, "用户口碑", "用户评价与反馈", 55, 38, 22, 7, "optimizing"));
        seeds.add(buildSeed(brandId, "用户口碑", "社区口碑传播", 40, 25, 15, 6, "optimizing"));
        seeds.add(buildSeed(brandId, "市场地位", "行业排名与份额", 70, 50, 40, 11, "good"));
        seeds.add(buildSeed(brandId, "市场地位", "标杆合作伙伴", 85, 60, 48, 13, "good"));
        seeds.add(buildSeed(brandId, "市场地位", "生态体系建设", 35, 22, 12, 5, "optimizing"));
        seeds.add(buildSeed(brandId, "企业荣誉", "年度最佳AI应用创新奖", 50, 40, 35, 5, "good"));
        seeds.add(buildSeed(brandId, "企业荣誉", "行业领军品牌认证", 40, 30, 22, 6, "optimizing"));
        seeds.add(buildSeed(brandId, "企业荣誉", "技术专利成果", 30, 20, 15, 4, "optimizing"));
        return nodeRepository.saveAll(seeds);
    }

    private BrandKnowledgeNode buildSeed(Long brandId, String category, String name,
                                          int generated, int published, int included, int keywords, String status) {
        BrandKnowledgeNode node = new BrandKnowledgeNode();
        node.setBrandId(brandId);
        node.setCategory(category);
        node.setName(name);
        node.setGeneratedCount(generated);
        node.setPublishedCount(published);
        node.setIncludedCount(included);
        node.setKeywordCount(keywords);
        node.setStatus(status);
        return node;
    }

    private BrandKnowledgeNode findNode(Long id, Long brandId) {
        return nodeRepository.findById(id)
                .filter(n -> n.getBrandId().equals(brandId))
                .orElseThrow(() -> new BusinessException("知识节点不存在"));
    }

    private KnowledgeDTO.NodeVO toVO(BrandKnowledgeNode node) {
        KnowledgeDTO.NodeVO vo = new KnowledgeDTO.NodeVO();
        vo.setId(node.getId());
        vo.setBrandId(node.getBrandId());
        vo.setName(node.getName());
        vo.setCategory(node.getCategory());
        vo.setDescription(node.getDescription());
        vo.setGeneratedCount(node.getGeneratedCount());
        vo.setPublishedCount(node.getPublishedCount());
        vo.setIncludedCount(node.getIncludedCount());
        vo.setKeywordCount(node.getKeywordCount());
        vo.setStatus(node.getStatus());
        if (node.getKeywords() != null) {
            vo.setKeywords(node.getKeywords().stream().map(k -> {
                KnowledgeDTO.KeywordVO kvo = new KnowledgeDTO.KeywordVO();
                kvo.setId(k.getId());
                kvo.setWord(k.getWord());
                kvo.setPlatform(k.getPlatform());
                kvo.setStatus(k.getStatus());
                kvo.setRanking(k.getRanking());
                return kvo;
            }).collect(Collectors.toList()));
        }
        return vo;
    }
}
