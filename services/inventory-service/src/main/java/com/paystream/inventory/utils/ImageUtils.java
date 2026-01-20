package com.paystream.inventory.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ImageUtils {

    @Value("${file.upload-dir}")
    private String basePath;

    /**
     * 해당 경로에 있는 이미지 파일을 가져옵니다.
     *
     * @param path 이미지 파일의 경로
     * @return 이미지 파일
     */
    public File getImage(String path) {
        File file = new File(path);

        // 파일이 존재하지 않거나, 폴더(Directory)인 경우 예외 처리
        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("해당 경로에 이미지 파일이 존재하지 않습니다.: " + path);
        }

        return file;
    }

    /**
     * 이미지를 경로에 저장합니다.
     *
     * @param image 이미지파일
     * @return 이미지 이름과 저장 위치를 반환합니다.
     * @throws IOException 파일 저장시 문제가 있다면 예외가 발생합지다.
     */
    public Map<String, String> saveImage(MultipartFile image) throws IOException {
        // 파일 이름 생성
        String fileName =
                UUID.randomUUID().toString().replace("-", "") + "-" + image.getOriginalFilename();

        // 파일 저장경로
        String filePath = basePath + fileName;

        Path path = Paths.get(filePath);
        // 폴더가 없으면 생성, 이미 있는 폴더면 생성 x
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());

        Map<String, String> result = new HashMap<>();
        result.put("name", fileName);
        result.put("path", filePath);

        return result;
    }
}
