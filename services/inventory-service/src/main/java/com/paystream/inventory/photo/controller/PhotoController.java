package com.paystream.inventory.photo.controller;

import com.paystream.inventory.photo.service.StorageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class PhotoController {

    private final StorageService storageService;

    @GetMapping("/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) throws IOException {
        File file = storageService.getImageFile(imageName);
        Resource resource = new FileSystemResource(file);

        // 파일의 실제 MINE 타입을 자동으로 감지 (image/png, image/jpeg 등)
        String contentType = Files.probeContentType(file.toPath());

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }
}
