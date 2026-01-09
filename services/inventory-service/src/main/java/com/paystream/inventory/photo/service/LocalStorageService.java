package com.paystream.inventory.photo.service;

import com.paystream.inventory.photo.entity.Photo;
import com.paystream.inventory.photo.repository.PhotoRepository;
import com.paystream.inventory.utils.ImageUtils;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("local")
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    @Value("${file.upload-dir}")
    private String basePath;

    @Value("${server.port}")
    private String port;

    private final ImageUtils imageUtils;
    private final PhotoRepository photoRepository;

    @Override
    public String getImageUrl(String fileName) {
        return "http://localhost:" + port + "/uploads/" + fileName;
    }

    @Override
    public File getImageFile(String fileName) {
        Photo findPhoto = photoRepository.findByFileName(fileName);
        return imageUtils.getImage(findPhoto.getImagePath());
    }
}
