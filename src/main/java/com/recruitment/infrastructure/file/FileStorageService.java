package com.recruitment.infrastructure.file;

import com.recruitment.common.exception.BusinessException;
import com.recruitment.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.storage.upload-dir}") String uploadDir) {
        Path path = Paths.get(uploadDir);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(uploadDir);
        }
        this.uploadDir = path;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String key = UUID.randomUUID() + ext;
        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(key);
            file.transferTo(target.toFile());
            log.info("文件已保存: key={}, size={}", key, file.getSize());
            return key;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件保存失败: " + e.getMessage());
        }
    }

    public byte[] read(String key) {
        try {
            return Files.readAllBytes(uploadDir.resolve(key));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在: " + key);
        }
    }

    public void delete(String key) {
        try {
            Files.deleteIfExists(uploadDir.resolve(key));
        } catch (IOException e) {
            log.warn("删除文件失败: key={}", key, e);
        }
    }
}
