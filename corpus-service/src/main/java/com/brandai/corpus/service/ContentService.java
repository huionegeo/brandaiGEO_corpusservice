package com.brandai.corpus.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.brandai.common.dto.PageResult;
import com.brandai.common.event.ContentPublishedEvent;
import com.brandai.common.exception.BusinessException;
import com.brandai.common.result.ResultCode;
import com.brandai.corpus.document.ContentDocument;
import com.brandai.corpus.domain.Content;
import com.brandai.corpus.dto.ContentDTO;
import com.brandai.corpus.repository.ContentRepository;
import com.brandai.corpus.repository.ContentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 内容语料库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;
    private final ContentSearchRepository searchRepository;
    private final ElasticsearchClient esClient;
    private final StreamBridge streamBridge;

    public PageResult<ContentDTO.ContentVO> list(Long brandId, ContentDTO.QueryRequest query) {
        PageRequest pageable = PageRequest.of(query.getPage() - 1, query.getPageSize());
        Page<Content> page;

        if (query.getType() != null) {
            page = contentRepository.findByBrandIdAndTypeAndIsDeletedFalse(
                    brandId, Content.ContentType.valueOf(query.getType().toUpperCase()), pageable);
        } else if (query.getStatus() != null) {
            page = contentRepository.findByBrandIdAndStatusAndIsDeletedFalse(
                    brandId, Content.ContentStatus.valueOf(query.getStatus().toUpperCase()), pageable);
        } else {
            page = contentRepository.findByBrandIdAndIsDeletedFalse(brandId, pageable);
        }

        List<ContentDTO.ContentVO> vos = page.getContent().stream()
                .map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, page.getTotalElements(), query.getPage(), query.getPageSize());
    }

    /** 全文搜索（Elasticsearch） */
    public List<ContentDTO.ContentVO> search(Long brandId, String keyword) throws Exception {
        SearchResponse<ContentDocument> response = esClient.search(s -> s
                .index("content")
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("brandId").value(brandId)))
                        .must(m -> m.multiMatch(mm -> mm
                                .query(keyword)
                                .fields("title^3", "body", "summary")))
                )),
                ContentDocument.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .map(this::docToVO)
                .collect(Collectors.toList());
    }

    public ContentDTO.ContentVO getById(Long id, Long brandId) {
        return toVO(findContent(id, brandId));
    }

    @Transactional
    public ContentDTO.ContentVO create(Long brandId, ContentDTO.CreateRequest request) {
        Content content = new Content();
        content.setBrandId(brandId);
        applyRequest(content, request);
        content = contentRepository.save(content);

        // 同步到 Elasticsearch
        syncToEs(content);

        log.info("内容创建成功: id={}, title={}", content.getId(), content.getTitle());
        return toVO(content);
    }

    @Transactional
    public ContentDTO.ContentVO update(Long id, Long brandId, ContentDTO.UpdateRequest request) {
        Content content = findContent(id, brandId);
        applyRequest(content, request);
        content = contentRepository.save(content);
        syncToEs(content);
        return toVO(content);
    }

    @Transactional
    public ContentDTO.ContentVO publish(Long id, Long brandId) {
        Content content = findContent(id, brandId);
        if (content.getStatus() == Content.ContentStatus.PUBLISHED) {
            throw new BusinessException("内容已发布");
        }
        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());
        content = contentRepository.save(content);
        syncToEs(content);

        // 发送 Kafka 事件
        ContentPublishedEvent event = new ContentPublishedEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setContentId(content.getId());
        event.setBrandId(brandId);
        event.setContentType(content.getType().name());
        event.setTitle(content.getTitle());
        event.setNodeCategory(content.getNodeCategory());
        event.setStatus("published");
        event.setPublishedAt(content.getPublishedAt());
        streamBridge.send("contentEvent-out-0", event);

        return toVO(content);
    }

    @Transactional
    public void delete(Long id, Long brandId) {
        Content content = findContent(id, brandId);
        content.setIsDeleted(true);
        contentRepository.save(content);
        try {
            searchRepository.deleteByContentId(id);
        } catch (Exception e) {
            log.warn("ES 删除失败（不影响数据删除）: {}", e.getMessage());
        }
    }

    private Content findContent(Long id, Long brandId) {
        return contentRepository.findById(id)
                .filter(c -> c.getBrandId().equals(brandId) && !c.getIsDeleted())
                .orElseThrow(() -> new BusinessException(ResultCode.CONTENT_NOT_FOUND));
    }

    private void applyRequest(Content content, ContentDTO.ContentRequest req) {
        if (req.getTitle() != null) content.setTitle(req.getTitle());
        if (req.getType() != null) content.setType(Content.ContentType.valueOf(req.getType().toUpperCase()));
        if (req.getBody() != null) content.setBody(req.getBody());
        if (req.getSummary() != null) content.setSummary(req.getSummary());
        if (req.getNodeCategory() != null) content.setNodeCategory(req.getNodeCategory());
        if (req.getNodeName() != null) content.setNodeName(req.getNodeName());
        if (req.getMediaChannels() != null) content.setMediaChannels(req.getMediaChannels());
    }

    private void syncToEs(Content content) {
        try {
            ContentDocument doc = new ContentDocument();
            doc.setId(content.getId().toString());
            doc.setContentId(content.getId());
            doc.setBrandId(content.getBrandId());
            doc.setTitle(content.getTitle());
            doc.setBody(content.getBody());
            doc.setSummary(content.getSummary());
            doc.setType(content.getType() != null ? content.getType().name() : null);
            doc.setNodeCategory(content.getNodeCategory());
            doc.setNodeName(content.getNodeName());
            doc.setStatus(content.getStatus().name());
            doc.setMediaChannels(content.getMediaChannels());
            doc.setAiIncludedPlatforms(content.getAiIncludedPlatforms());
            doc.setPublishedAt(content.getPublishedAt());
            doc.setCreatedAt(content.getCreatedAt());
            searchRepository.save(doc);
        } catch (Exception e) {
            log.warn("ES 同步失败（不影响数据保存）: {}", e.getMessage());
        }
    }

    private ContentDTO.ContentVO toVO(Content c) {
        ContentDTO.ContentVO vo = new ContentDTO.ContentVO();
        vo.setId(c.getId());
        vo.setBrandId(c.getBrandId());
        vo.setTitle(c.getTitle());
        vo.setType(c.getType() != null ? c.getType().name().toLowerCase() : null);
        vo.setBody(c.getBody());
        vo.setSummary(c.getSummary());
        vo.setNodeCategory(c.getNodeCategory());
        vo.setNodeName(c.getNodeName());
        vo.setStatus(c.getStatus().name().toLowerCase());
        vo.setMediaChannels(c.getMediaChannels());
        vo.setAiIncludedPlatforms(c.getAiIncludedPlatforms());
        vo.setPublishedAt(c.getPublishedAt());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private ContentDTO.ContentVO docToVO(ContentDocument doc) {
        ContentDTO.ContentVO vo = new ContentDTO.ContentVO();
        vo.setId(doc.getContentId());
        vo.setBrandId(doc.getBrandId());
        vo.setTitle(doc.getTitle());
        vo.setType(doc.getType());
        vo.setSummary(doc.getSummary());
        vo.setNodeCategory(doc.getNodeCategory());
        vo.setNodeName(doc.getNodeName());
        vo.setStatus(doc.getStatus());
        vo.setMediaChannels(doc.getMediaChannels());
        vo.setAiIncludedPlatforms(doc.getAiIncludedPlatforms());
        vo.setPublishedAt(doc.getPublishedAt());
        return vo;
    }
}
