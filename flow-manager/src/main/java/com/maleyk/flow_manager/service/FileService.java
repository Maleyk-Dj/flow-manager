package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.dto.FileDownload;
import com.maleyk.flow_manager.dto.FileStatusResponse;
import com.maleyk.flow_manager.exception.FileNotReadyException;
import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.model.RecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;
    private final FileRecordService recordService;

    private static final String SOURCE_BUCKET = "source-files";
    private static final String CONVERTED_BUCKET = "converted-files";

    public FileRecord upload(MultipartFile file) throws Exception {
        String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioService.upload(SOURCE_BUCKET, objectKey,
                file.getInputStream(), file.getSize(), file.getContentType());
        return recordService.createProcessingRecord
                (file.getOriginalFilename(), SOURCE_BUCKET, objectKey);
    }

    public FileStatusResponse getStatus(UUID id) {
        FileRecord fileRecord = recordService.findByIdOrThrows(id);
        return new FileStatusResponse(fileRecord.getId(), fileRecord.getRecordStatus(),
                fileRecord.getConvertedPath());
    }

    public FileDownload downloadConvertedFile(UUID id) throws Exception {
        FileRecord fileRecord = recordService.findByIdOrThrows(id);

        if (fileRecord.getRecordStatus() != RecordStatus.SUCCESS) {
            throw new FileNotReadyException("Файл еще не готов: " + id);
        }

        byte[] content = minioService.download(CONVERTED_BUCKET, fileRecord.getConvertedPath());
        return new FileDownload(content, fileRecord.getConvertedPath());

    }
}
