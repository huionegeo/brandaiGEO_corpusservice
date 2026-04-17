package com.brandai.media.service;

import com.brandai.common.exception.BusinessException;
import com.brandai.common.result.ResultCode;
import com.brandai.media.domain.MediaChannel;
import com.brandai.media.dto.MediaChannelDTO;
import com.brandai.media.repository.MediaChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaChannelRepository mediaChannelRepository;

    public List<MediaChannelDTO.MediaChannelVO> list(Long brandId, String type, String keyword) {
        List<MediaChannel> channels;
        boolean hasType = type != null && !type.isBlank();
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasType && hasKeyword) {
            channels = mediaChannelRepository.findByBrandIdAndTypeAndNameContainingIgnoreCase(
                    brandId, MediaChannel.ChannelType.valueOf(type.toUpperCase()), keyword);
        } else if (hasType) {
            channels = mediaChannelRepository.findByBrandIdAndType(
                    brandId, MediaChannel.ChannelType.valueOf(type.toUpperCase()));
        } else if (hasKeyword) {
            channels = mediaChannelRepository.findByBrandIdAndNameContainingIgnoreCase(brandId, keyword);
        } else {
            channels = mediaChannelRepository.findByBrandId(brandId);
        }
        return channels.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional
    public MediaChannelDTO.MediaChannelVO authorize(Long brandId, Long channelId) {
        MediaChannel channel = findChannel(brandId, channelId);
        channel.setAuthStatus(MediaChannel.AuthStatus.AUTHORIZED);
        channel = mediaChannelRepository.save(channel);
        log.info("媒体渠道授权成功: id={}, name={}", channel.getId(), channel.getName());
        return toVO(channel);
    }

    @Transactional
    public MediaChannelDTO.MediaChannelVO unbind(Long brandId, Long channelId) {
        MediaChannel channel = findChannel(brandId, channelId);
        channel.setAuthStatus(MediaChannel.AuthStatus.UNAUTHORIZED);
        channel.setAccountName(null);
        channel = mediaChannelRepository.save(channel);
        log.info("媒体渠道解绑成功: id={}, name={}", channel.getId(), channel.getName());
        return toVO(channel);
    }

    @Transactional
    public MediaChannelDTO.MediaChannelVO addToPlan(Long brandId, Long channelId, int articleCount) {
        MediaChannel channel = findChannel(brandId, channelId);
        channel.setInPlan(true);
        channel = mediaChannelRepository.save(channel);
        log.info("加入优化计划: id={}, name={}, articleCount={}", channel.getId(), channel.getName(), articleCount);
        return toVO(channel);
    }

    @Transactional
    public MediaChannelDTO.MediaChannelVO createSelfMedia(Long brandId, MediaChannelDTO.CreateSelfMediaRequest request) {
        MediaChannel channel = new MediaChannel();
        channel.setBrandId(brandId);
        channel.setName(request.getName());
        channel.setType(MediaChannel.ChannelType.SELF);
        channel.setPlatform(request.getPlatform());
        channel.setAccountName(request.getAccountName());
        channel.setAuthStatus(MediaChannel.AuthStatus.UNAUTHORIZED);
        channel = mediaChannelRepository.save(channel);
        log.info("自媒体账号创建成功: id={}, name={}", channel.getId(), channel.getName());
        return toVO(channel);
    }

    private MediaChannel findChannel(Long brandId, Long channelId) {
        return mediaChannelRepository.findById(channelId)
                .filter(c -> c.getBrandId().equals(brandId))
                .orElseThrow(() -> new BusinessException(ResultCode.MEDIA_CHANNEL_NOT_FOUND));
    }

    private MediaChannelDTO.MediaChannelVO toVO(MediaChannel c) {
        MediaChannelDTO.MediaChannelVO vo = new MediaChannelDTO.MediaChannelVO();
        vo.setId(c.getId());
        vo.setBrandId(c.getBrandId());
        vo.setName(c.getName());
        vo.setType(c.getType().name().toLowerCase());
        vo.setPlatform(c.getPlatform());
        vo.setAccountName(c.getAccountName());
        vo.setAuthStatus(c.getAuthStatus().name().toLowerCase());
        vo.setListPrice(c.getListPrice());
        vo.setFollowersCount(c.getFollowersCount());
        vo.setInPlan(c.getInPlan());
        return vo;
    }
}
