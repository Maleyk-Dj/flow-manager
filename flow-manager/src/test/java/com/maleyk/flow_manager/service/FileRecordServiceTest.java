package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.dto.FileConversionResult;
import com.maleyk.flow_manager.exception.FileRecordNotFoundException;
import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.model.RecordStatus;
import com.maleyk.flow_manager.outbox.OutboxService;
import com.maleyk.flow_manager.repository.FileRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileRecordServiceTest {

    @Mock
    private OutboxService outboxService;

    @Mock
    private FileRecordRepository repository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private FileRecordService service;

    private UUID id;
    private FileRecord existingRecord;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        existingRecord = new FileRecord();
        existingRecord.setId(id);
        existingRecord.setRecordStatus(RecordStatus.PROCESSING);
    }

    @Test
    void createProcessingRecord_shouldSaveRecordAndOutboxMessage() {
        ReflectionTestUtils.setField(service, "topic", "files.input");
        when(repository.save(any(FileRecord.class))).thenAnswer(invocation -> {
            FileRecord record = invocation.getArgument(0);
            record.setId(UUID.randomUUID());
            return record;
        });

        FileRecord result = service.createProcessingRecord
                ("report.docx", "source-files", "abc-report.docx");

        assertEquals("report.docx", result.getOriginalFilename());
        assertEquals("abc-report.docx", result.getSourcePath());
        assertEquals(RecordStatus.PROCESSING, result.getRecordStatus());
        assertNotNull(result.getId());

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(outboxService).save(eq("files.input"), payloadCaptor.capture());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains(result.getId().toString()));
        assertTrue(payload.contains("source-files"));
        assertTrue(payload.contains("abc-report.docx"));
    }


    @Test
    void applyConversionResult_shouldSetSuccessStatus_whenResultIsSuccess() {
        when(repository.findById(id)).thenReturn(Optional.of(existingRecord));
        FileConversionResult result = FileConversionResult.success(id.toString(), "converted-files", "path/to/file.pdf");

        service.applyConversionResult(result);

        assertEquals(RecordStatus.SUCCESS, existingRecord.getRecordStatus());
        assertEquals("path/to/file.pdf", existingRecord.getConvertedPath());
        verify(repository).save(existingRecord);
    }

    @Test
    void applyConversionResult_shouldSetErrorStatus_whenResultIsFailed() {
        when(repository.findById(id)).thenReturn(Optional.of(existingRecord));
        FileConversionResult result = FileConversionResult.failed(id.toString(), "conversion error");

        service.applyConversionResult(result);

        assertEquals(RecordStatus.ERROR, existingRecord.getRecordStatus());
        assertNull(existingRecord.getConvertedPath());
        verify(repository).save(existingRecord);
    }

    @Test
    void applyConversionResult_shouldDoNothing_whenRecordNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        FileConversionResult result = FileConversionResult.success(id.toString(), "converted-files", "path/to/file.pdf");

        service.applyConversionResult(result);

        verify(repository, never()).save(any());
    }

    @Test
    void findByIdOrThrows_shouldReturnRecord() {
        when(repository.findById(id)).thenReturn(Optional.of(existingRecord));

        FileRecord result = service.findByIdOrThrows(id);

        assertEquals(existingRecord, result);
    }

    @Test
    void findByIdOrThrows_shouldThrowException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(FileRecordNotFoundException.class, () -> service.findByIdOrThrows(id));
    }

}
