package com.paystream.inventory.photo.service;

import com.paystream.inventory.photo.entity.Photo;
import com.paystream.inventory.photo.repository.PhotoRepository;
import com.paystream.inventory.utils.ImageUtils;
import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
class TestStorageService implements StorageService {

    @Value("${server.port}")
    private String port;

    @Autowired private PhotoRepository photoRepository;

    @Autowired private ImageUtils imageUtils;

    @Override
    public String getImageUrl(String fileName) {
        // 서버 주소가 바뀌어도 유연하게 대응
        return "http://localhost:" + port + "/uploads/" + fileName;
    }

    @Override
    public File getImageFile(String fileName) {
        Photo findPhoto = photoRepository.findByFileName(fileName);
        return imageUtils.getImage(findPhoto.getImagePath());
    }
}
