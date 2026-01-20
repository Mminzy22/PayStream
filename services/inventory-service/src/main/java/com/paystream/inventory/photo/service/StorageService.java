package com.paystream.inventory.photo.service;

import java.io.File;

public interface StorageService {

    String getImageUrl(String fileName);

    File getImageFile(String fileName);
}
