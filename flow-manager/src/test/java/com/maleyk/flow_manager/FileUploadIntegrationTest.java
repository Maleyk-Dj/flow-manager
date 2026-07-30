package com.maleyk.flow_manager;
import com.jayway.jsonpath.JsonPath;
import com.maleyk.flow_manager.model.FileRecord;
import com.maleyk.flow_manager.model.RecordStatus;
import com.maleyk.flow_manager.outbox.OutboxMessagesRepository;
import com.maleyk.flow_manager.outbox.OutboxStatus;
import com.maleyk.flow_manager.repository.FileRecordRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileUploadIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileRecordRepository recordRepository;

    @Autowired
    private OutboxMessagesRepository outboxMessagesRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MinioClient minioClient;

    @Test
    void uploadThenConvertThenDownload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.text", "text/plain", "hello".getBytes()
        );

        String response = mockMvc.perform(multipart("/api/files").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.recordStatus").value("PROCESSING"))
                .andReturn().getResponse().getContentAsString();

        UUID id = UUID.fromString(JsonPath.read(response, "$.id"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() ->
                assertTrue(outboxMessagesRepository.findAll().stream()
                        .anyMatch(m -> m.getStatus() == OutboxStatus.SENT)));
        String convertedPath = "converted/" + id + ".pdf";
        minioClient.putObject(PutObjectArgs.builder()
                .bucket("converted-files")
                .object(convertedPath)
                .stream(new ByteArrayInputStream("%PDF-fake".getBytes()), 9, -1)
                .contentType("application/pdf")
                .build());

        String resultMessage = """
                {"originalMessageId":"%s","status":"SUCCESS","bucket":"converted-files","pdfPath":"%s"}
                """.formatted(id, convertedPath);
        kafkaTemplate.send("files.output", resultMessage);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            FileRecord updated = recordRepository.findById(id).orElseThrow();
            assertEquals(RecordStatus.SUCCESS, updated.getRecordStatus());
        });

        mockMvc.perform(get("/api/files/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordStatus").value("SUCCESS"));

        mockMvc.perform(get("/api/files/{id}/file", id))
                .andExpect(status().isOk())
                .andExpect(content().bytes("%PDF-fake".getBytes()));
    }
}
