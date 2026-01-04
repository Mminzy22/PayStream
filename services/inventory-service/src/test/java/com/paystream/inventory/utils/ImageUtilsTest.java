package com.paystream.inventory.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.paystream.inventory.config.FileStorageConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ImageUtilsTest {

    @Autowired private FileStorageConfig config;

    @Autowired private ImageUtils imageUtils;

    @AfterEach
    void cleanup() throws IOException {
        Path path = Paths.get(config.getBasePath());
        if (Files.exists(path)) {
            // 폴더 내부의 모든 파일 삭제 후 폴더 삭제
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @DisplayName("이미지 성공적으로 저장 후 저장경로 반환")
    @Test
    void fileUploadTest() throws IOException {
        // given
        String originalName = "test_image.png";
        MockMultipartFile file =
                new MockMultipartFile(
                        "file", // API에서 받을 파라미터 이름 (@RequestParam("file")
                        originalName, // 원본 파일명 (filename)
                        "image/png", // 파일 타입 (contentType)
                        "test content".getBytes() // 파일 내용 (byte[])
                        );

        String basePath = config.getBasePath();

        // when
        String savedPath = imageUtils.saveImage(file);

        // then
        assertThat(savedPath).startsWith(basePath);
        assertThat(savedPath).endsWith("-" + originalName);
    }
}
