package com.example.filestorage.file;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMetadata> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Api-Key") String apiKey) {

        FileMetadata metadata = fileService.uploadFile(file, apiKey);
        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        FileMetadata info = fileService.getFileInfo(id);
        byte[] data = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + info.getOriginalName() + "\" ")
                .contentType(MediaType.parseMediaType(
                        info.getContentType() != null ? info.getContentType() : "application/octet-stream"))
                .body(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileMetadata> getInfo(@PathVariable UUID id) {
        return ResponseEntity.ok(fileService.getFileInfo(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();

    }
}
