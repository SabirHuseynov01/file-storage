package com.example.filestorage.apikey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByHashedKeyAndIsActiveTrue(String hashedKey);
    boolean existsByHashedKeyAndIsActiveTrue(String hashedKey);
}
