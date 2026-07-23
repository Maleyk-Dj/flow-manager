package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.model.Status;
import com.maleyk.flow_manager.repository.FileRecordRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final MinioClient minioClient;
    private final FileRecordRepository repository;

    private static final String SOURCE_BUCKET = "source-files";

    public FileRecord upload(MultipartFile file) throws Exception {
        String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(SOURCE_BUCKET)
                        .object(objectKey)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        FileRecord record = new FileRecord();
        record.setOriginalFilename(file.getOriginalFilename());
        record.setSourcePath(objectKey);
        record.setStatus(Status.PROCESSING);
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(Instant.now());

        return repository.save(record);
    }
}
