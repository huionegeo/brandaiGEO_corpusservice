package com.brandai.media.repository;

import com.brandai.media.domain.MediaChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaChannelRepository extends JpaRepository<MediaChannel, Long> {
    List<MediaChannel> findByBrandId(Long brandId);
    List<MediaChannel> findByBrandIdAndType(Long brandId, MediaChannel.ChannelType type);
    List<MediaChannel> findByBrandIdAndNameContainingIgnoreCase(Long brandId, String keyword);
    List<MediaChannel> findByBrandIdAndTypeAndNameContainingIgnoreCase(Long brandId, MediaChannel.ChannelType type, String keyword);
}
