package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.dto.FileDownload;
import com.maleyk.flow_manager.dto.FileStatusResponse;
import com.maleyk.flow_manager.dto.SubscriptionResponse;
import com.maleyk.flow_manager.dto.SubscriptionType;
import com.maleyk.flow_manager.exception.FileAccessDeniedException;
import com.maleyk.flow_manager.exception.FileNotReadyException;
import com.maleyk.flow_manager.exception.FileSizeLimitExceededException;
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
    private final SubscriptionCacheService subscriptionCacheService;

    private static final String SOURCE_BUCKET = "source-files";
    private static final String CONVERTED_BUCKET = "converted-files";
    private static final long FREE_TIER_FILE_SIZE_LIMIT = 100L * 1024 * 1024;

    public FileRecord upload(MultipartFile file, String userLogin) throws Exception {
        SubscriptionResponse subscription = subscriptionCacheService.getSubscriptionCached(userLogin);
        if (subscription.subscriptionType() == SubscriptionType.FREE && file.getSize() > FREE_TIER_FILE_SIZE_LIMIT) {
            throw new FileSizeLimitExceededException("Файл превышает лимит 100MB для бесплатной подписки");
        }
        String objectKey = UUID.randomUUID() + "-" + file.getOriginalFilename();

        minioService.upload(SOURCE_BUCKET, objectKey,
                file.getInputStream(), file.getSize(), file.getContentType());
        return recordService.createProcessingRecord
                (file.getOriginalFilename(), SOURCE_BUCKET, objectKey, userLogin);
    }

    public FileStatusResponse getStatus(UUID id, String userLogin) {
        FileRecord fileRecord = recordService.findByIdOrThrows(id);
        chekOwner(fileRecord, userLogin);
        return new FileStatusResponse(fileRecord.getId(), fileRecord.getRecordStatus(),
                fileRecord.getConvertedPath());
    }

    public FileDownload downloadConvertedFile(UUID id, String userLogin) throws Exception {
        FileRecord fileRecord = recordService.findByIdOrThrows(id);
        chekOwner(fileRecord, userLogin);

        if (fileRecord.getRecordStatus() != RecordStatus.SUCCESS) {
            throw new FileNotReadyException("Файл еще не готов: " + id);
        }

        byte[] content = minioService.download(CONVERTED_BUCKET, fileRecord.getConvertedPath());
        return new FileDownload(content, fileRecord.getConvertedPath());
    }

    private void chekOwner(FileRecord fileRecord, String userLogin) {
        if (!fileRecord.getOwnerLogin().equals(userLogin)) {
            throw new FileAccessDeniedException("Нет доступа к чужому файлу " +
                    fileRecord.getId());
        }
    }
}
