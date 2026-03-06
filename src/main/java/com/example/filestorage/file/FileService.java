package com.example.filestorage.file;

import com.example.filestorage.apikey.ApiKey;
import com.example.filestorage.apikey.ApiKeyRepository;
import com.example.filestorage.apikey.ApiKeyService;
import com.example.filestorage.exception.FileNotFoundException;
import com.example.filestorage.exception.StorageException;
import com.example.filestorage.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageProvider storageProvider;
    private final FileRepository fileRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Value("${storage.provider}")
    private String storageProviderName;

    @Value("${storage.minio.bucket}")
    private String bucketName;

    public FileMetadata uploadFile(MultipartFile file, String rawApiKey) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            storageProvider.uploadFile(file, storedName);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file: " + file.getOriginalFilename(), e);
        }

        String hashedKey = ApiKeyService.hash(rawApiKey);
        ApiKey uploader = apiKeyRepository.findByHashedKeyAndIsActiveTrue(hashedKey)
                .orElseThrow(() -> new RuntimeException("API Key not found"));

        FileMetadata metadata = FileMetadata.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .storageProvider(storageProviderName)
                .bucketName(bucketName)
                .uploadedBy(uploader)
                .build();

        return fileRepository.save(metadata);
    }

    public byte[] downloadFile(UUID fileId) {
        FileMetadata metadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId.toString()));

        try {
            return storageProvider.downloadFile(metadata.getStoredName());
        } catch (Exception e) {
            throw new StorageException("Failed to download file: " + metadata.getOriginalName(), e);
        }
    }

    public void deleteFile(UUID fileId) {
        FileMetadata metadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId.toString()));

        try {
            storageProvider.deleteFile(metadata.getStoredName());
            fileRepository.delete(metadata);
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException("Failed to delete file: " + metadata.getOriginalName(), e);
        }
    }

    public FileMetadata getFileInfo(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId.toString()));
    }
}