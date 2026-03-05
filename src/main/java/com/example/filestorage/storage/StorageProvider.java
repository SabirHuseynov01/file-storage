package com.example.filestorage.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider {
    String uploadFile(MultipartFile file, String storedName);
    byte[] downloadFile(String storedName);
    void deleteFile(String storedName);
}
