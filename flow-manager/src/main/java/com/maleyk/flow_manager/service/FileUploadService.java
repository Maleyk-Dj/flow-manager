package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.model.FileRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final MinioService minioService;
    private final FileRecordService recordService;

    private static final String SOURCE_BUCKET = "source-files";

    public FileRecord upload(MultipartFile file) throws Exception {
        String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioService.upload(SOURCE_BUCKET, objectKey,
                file.getInputStream(), file.getSize(), file.getContentType());
        return recordService.createProcessingRecord
                (file.getOriginalFilename(), SOURCE_BUCKET, objectKey);
    }
}
