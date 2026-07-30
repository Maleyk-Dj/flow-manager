package com.maleyk.flow_manager.controller;

import com.maleyk.flow_manager.dto.FileDownload;
import com.maleyk.flow_manager.dto.FileStatusResponse;
import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService service;

    @PostMapping
    public ResponseEntity<FileRecord> upload(@RequestParam("file") MultipartFile file) throws Exception {
        FileRecord record = service.upload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(record);
    }

    @GetMapping("/{id}/status")
    public FileStatusResponse getStatus(@PathVariable UUID id) {
        return service.getStatus(id);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID id) throws Exception {
        FileDownload download = service.downloadConvertedFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.fileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(download.content());
    }
}
