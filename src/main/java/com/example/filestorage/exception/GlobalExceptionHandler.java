package com.example.filestorage.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Error 404 - File not found
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFileNotFound(FileNotFoundException ex) {
        log.warn("File not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }

    // Error 500 - MinIO or storage error
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Map<String, Object>> handleStorageException(StorageException ex) {
        log.error("Storage error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "error", "Storage Error",
                "message", ex.getMessage()
        ));
    }

    // Error 413 - File is too large
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 413,
                "error", "File Too Large",
                "message", "File size exceeds the maximum allowed limit"
        ));
    }

    // Error 400 - Invalid query
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage()
        ));
    }

    // Error 500 - Unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 500,
                "error", "Internal Server Error",
                "message", "An unexpected error occurred"
        ));
    }
}