package com.example.filestorage.apikey;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyCreatedResponse createApiKey(String name) {
        String rawKey = UUID.randomUUID().toString().replace("-", "");
        String hashedKey = hash(rawKey);

        ApiKey apiKey = ApiKey.builder()
                .hashedKey(hashedKey)
                .name(name)
                .isActive(true)
                .build();

        apiKeyRepository.save(apiKey);

        return new ApiKeyCreatedResponse(rawKey, name);
    }

    public boolean isValidKey(String rawKey){
        if (rawKey == null || rawKey.isBlank()) return false;
        String hashed = hash(rawKey);
        return apiKeyRepository.existsByHashedKeyAndIsActiveTrue(hashed);
    }

    public void revokeKey(String rawKey){
        String hashed = hash(rawKey);
        apiKeyRepository.findByHashedKeyAndIsActiveTrue(hashed).ifPresent(apiKey -> {
            apiKey.setIsActive(false);
            apiKeyRepository.save(apiKey);
        });
    }

    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }



}
