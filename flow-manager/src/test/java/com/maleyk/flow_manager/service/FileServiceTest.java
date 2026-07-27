package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.dto.FileDownload;
import com.maleyk.flow_manager.dto.FileStatusResponse;
import com.maleyk.flow_manager.exception.FileNotReadyException;
import com.maleyk.flow_manager.exception.FileRecordNotFoundException;
import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.model.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class FileServiceTest {

    @Mock
    private FileRecordService recordService;
    @Mock
    private MinioService minioService;

    @InjectMocks
    private FileService fileService;

    private FileRecord buildRecord(UUID id, Status status, String convertedPath) {
        FileRecord record = new FileRecord();
        record.setId(id);
        record.setStatus(status);
        record.setConvertedPath(convertedPath);
        return record;
    }

    @Test
    void upload_shouldStoreFileAndCreateRecord() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.docx", "application/octet-stream", "content".getBytes());

        FileRecord expectedRecord = new FileRecord();
        expectedRecord.setOriginalFilename("report.docx");

        when(recordService.createProcessingRecord(eq("report.docx"), eq("source-files"),
                anyString()))
                .thenReturn(expectedRecord);

        FileRecord result = fileService.upload(file);

        ArgumentCaptor<String> objectKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(minioService).upload(eq("source-files"), objectKeyCaptor.capture(),
                any(InputStream.class), eq((long) file.getSize()), eq("application/octet-stream"));
        verify(recordService).createProcessingRecord(
                eq("report.docx"), eq("source-files"), eq(objectKeyCaptor.getValue()));

        assertEquals(expectedRecord, result);
    }


    @Test
    void getStatus_shouldReturnStatusResponse() {
        UUID id = UUID.randomUUID();
        FileRecord record = buildRecord(id, Status.SUCCESS, "path/to/file.pdf");

        when(recordService.findByIdOrThrows(id)).thenReturn(record);

        FileStatusResponse response = fileService.getStatus(id);

        assertEquals(id, response.id());
        assertEquals(Status.SUCCESS, response.status());
        assertEquals("path/to/file.pdf", response.convertedPath());
    }

    @Test
    void getStatus_shouldPropagateException_whenRecordNotFound() {
        UUID id = UUID.randomUUID();
        when(recordService.findByIdOrThrows(id)).thenThrow(new FileRecordNotFoundException(id));

        assertThrows(FileRecordNotFoundException.class, () -> fileService.getStatus(id));
    }

    @Test
    void downloadConvertedFile_shouldReturnContent_whenStatusSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        FileRecord record = buildRecord(id, Status.SUCCESS, "path/to/file.pdf");

        when(recordService.findByIdOrThrows(id)).thenReturn(record);
        when(minioService.download("converted-files", "path/to/file.pdf")).thenReturn("pdf-bytes".getBytes());

        FileDownload download = fileService.downloadConvertedFile(id);

        assertArrayEquals("pdf-bytes".getBytes(), download.content());
        assertEquals("path/to/file.pdf", download.fileName());
    }

    @Test
    void downloadConvertedFile_shouldThrow_whenNotReady() {
        UUID id = UUID.randomUUID();
        FileRecord record = record = buildRecord(id, Status.PROCESSING, null);

        when(recordService.findByIdOrThrows(id)).thenReturn(record);

        assertThrows(FileNotReadyException.class, () -> fileService.downloadConvertedFile(id));
        verifyNoInteractions(minioService);
    }
}
