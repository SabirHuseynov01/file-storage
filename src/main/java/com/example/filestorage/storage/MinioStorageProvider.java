package com.example.filestorage.storage;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
public class MinioStorageProvider implements StorageProvider{

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioStorageProvider(MinioClient minioClient,
                                @Value("${storage.minio.bucket}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }




    @Override
    public String uploadFile(MultipartFile file, String storedName) {
        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded: {}", storedName);
            return storedName;
        }catch (Exception e){
            throw new RuntimeException("Failed to upload file to MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFile(String storedName) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedName)
                            .build()
            );
            return stream.readAllBytes();

        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String storedName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storedName)
                            .build()
            );
            log.info("File deleted from MinIO: {}", storedName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO: " + e.getMessage(), e);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket created: {}", bucketName);
        }
    }
}
