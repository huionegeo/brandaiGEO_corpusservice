package com.brandai.media.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("文件上传目录: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadPath, e);
        }
    }

    public String store(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID() + extension;
        Path targetPath = uploadPath.resolve(storedName);
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    public void delete(String storedName) {
        try {
            Path filePath = uploadPath.resolve(storedName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", storedName, e);
        }
    }

    public Path getFilePath(String storedName) {
        return uploadPath.resolve(storedName);
    }
}
