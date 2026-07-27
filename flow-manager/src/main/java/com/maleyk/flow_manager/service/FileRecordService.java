package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.dto.ConversionStatus;
import com.maleyk.flow_manager.dto.FileConversionRequest;
import com.maleyk.flow_manager.dto.FileConversionResult;
import com.maleyk.flow_manager.exception.FileRecordNotFoundException;
import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.model.Status;
import com.maleyk.flow_manager.outbox.OutboxService;
import com.maleyk.flow_manager.repository.FileRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileRecordService {

    private final OutboxService outboxService;
    private final FileRecordRepository fileRecordRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.input}")
    private String topic;

    @Transactional
    public FileRecord createProcessingRecord(
            String originalName, String bucket, String objectKey)  {
        FileRecord fileRecord = new FileRecord();
        fileRecord.setOriginalFilename(originalName);
        fileRecord.setSourcePath(objectKey);
        fileRecord.setStatus(Status.PROCESSING);
        fileRecord.setCreatedAt(LocalDateTime.now());
        fileRecord.setUpdatedAt(LocalDateTime.now());
        fileRecordRepository.save(fileRecord);

        FileConversionRequest request = new FileConversionRequest();
        request.setMessageId(fileRecord.getId().toString());
        request.setBucket(bucket);
        request.setFilePath(objectKey);

        outboxService.save(topic, objectMapper.writeValueAsString(request));
        return fileRecord;
    }

    @Transactional
    public void applyConversionResult(FileConversionResult result) {
        UUID recordId = UUID.fromString(result.getOriginalMessageId());

        fileRecordRepository.findById(recordId).ifPresentOrElse(record -> {
            if (result.getStatus() == ConversionStatus.SUCCESS) {
                record.setStatus(Status.SUCCESS);
                record.setConvertedPath(result.getPdfPath());
            } else {
                record.setStatus(Status.ERROR);
            }
            record.setUpdatedAt(LocalDateTime.now());
            fileRecordRepository.save(record);
        }, () -> log.warn("Не найдена запись FileRecord для messageId {}", result.getOriginalMessageId()));
    }

    public FileRecord findByIdOrThrows(UUID id) {
        return fileRecordRepository.findById(id)
                .orElseThrow(() -> new FileRecordNotFoundException(id));
    }
}
