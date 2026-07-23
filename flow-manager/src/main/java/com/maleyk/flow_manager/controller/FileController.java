package com.maleyk.flow_manager.controller;

import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService service;

    @PostMapping
    public ResponseEntity<FileRecord> upload(@RequestParam("file") MultipartFile file) throws Exception {
        FileRecord record = service.upload(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(record);
    }
}
