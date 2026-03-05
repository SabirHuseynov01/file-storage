package com.example.filestorage.file;

import com.example.filestorage.apikey.ApiKey;
import com.example.filestorage.apikey.ApiKeyRepository;
import com.example.filestorage.apikey.ApiKeyService;
import com.example.filestorage.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final StorageProvider storageProvider;

    @Value( "${storage.provider}")
    private String storageProviderName;

    @Value( "${storage.minio.bucket}")
    private String bucketName;

    public FileMetadata uploadFile(MultipartFile file, String rawApiKey) {
        String storedName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        storageProvider.uploadFile(file, storedName);

        String hashedKey = ApiKeyService.hash(rawApiKey);
        ApiKey uploader = apiKeyRepository.findByHashedKeyAndIsActiveTrue(hashedKey)
                .orElseThrow(() -> new RuntimeException("API Key not found" ));

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

    public byte[] downloadFile(UUID fileID) {
        FileMetadata metadata = fileRepository.findById(fileID)
                .orElseThrow(() -> new RuntimeException("File not found" + fileID));

        return storageProvider.downloadFile(metadata.getStoredName());
    }

    public void deleteFile(UUID fileID) {
        FileMetadata metadata = fileRepository.findById(fileID)
                .orElseThrow(() -> new RuntimeException("File not found" + fileID));
    }

    public FileMetadata getFileInfo(UUID fileID) {
        return fileRepository.findById(fileID)
                .orElseThrow(() -> new RuntimeException("File not found" + fileID));
    }
}
