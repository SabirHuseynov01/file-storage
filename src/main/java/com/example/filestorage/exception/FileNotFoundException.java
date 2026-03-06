package com.example.filestorage.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String fileId) {
        super("File not found with id: " + fileId);
    }
}

















