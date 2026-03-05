package com.example.filestorage.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileMetadata, UUID> {
    Optional<FileMetadata> findByStoredName(String storedName);
}
