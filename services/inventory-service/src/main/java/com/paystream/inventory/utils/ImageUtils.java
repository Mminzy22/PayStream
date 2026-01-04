package com.paystream.inventory.utils;

import com.paystream.inventory.config.FileStorageConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageUtils {

    private final FileStorageConfig config;

    public File getImage(String path) {
        return new File(path);
    }

    public String saveImage(MultipartFile image) throws IOException {
        // 파일 이름 생성
        String fileName =
                UUID.randomUUID().toString().replace("-", "") + "-" + image.getOriginalFilename();

        // 파일 저장경로
        String filePath = config.getBasePath() + fileName;

        Path path = Paths.get(filePath);
        // 폴더가 없으면 생성, 이미 있는 폴더면 생성 x
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());

        return filePath;
    }
}
