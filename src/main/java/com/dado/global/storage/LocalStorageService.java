package com.dado.global.storage;

import com.dado.global.exception.FileStorageException;
import com.dado.global.exception.InvalidFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url}")
    private String accessUrl;

    @Value("${file.max-size}")
    private DataSize maxSize;

    @Override
    public String upload(MultipartFile file) {
        validateFile(file);

        String savedFileName = UUID.randomUUID() + ".png";
        Path targetPath = Paths.get(uploadDir).resolve(savedFileName);

        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            log.info("파일 저장 완료: {}", targetPath);
        } catch (IOException e) {
            throw new FileStorageException("파일 저장 실패", e);
        }

        return accessUrl + savedFileName;
    }

    @Override
    public void delete(String fileUrl) {
        String fileName = fileUrl.replace(accessUrl, "");

        Path filePath = Paths.get(uploadDir)
                .resolve(fileName)
                .normalize();

        // path traversal 공격 방지
        if (!filePath.startsWith(Paths.get(uploadDir).normalize())) {
            throw new InvalidFileException("잘못된 파일 경로입니다.");
        }

        try {
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", filePath);
        } catch (IOException e) {
            throw new FileStorageException("파일 삭제 실패", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("파일이 비어있습니다.");
        }

        if (file.getSize() > maxSize.toBytes()) {
            throw new InvalidFileException("파일 크기 제한을 초과했습니다.");
        }
    }
}
