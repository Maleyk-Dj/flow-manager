package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.consumer.FileConversionResultConsumer;
import com.maleyk.flow_manager.dto.FileConversionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileConversionResultConsumerTest {

    @Mock
    private FileRecordService service;
    @Mock
    private Acknowledgment ack;
    @Mock
    private DeadLetterPublishingRecoverer dltRecoverer;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks
    private FileConversionResultConsumer consumer;

    private ConsumerRecord<String, String> record(String value) {
        return new ConsumerRecord<>("files.output", 0, 0L, "key", value);
    }

    @Test
    void consume_shouldProcessAndAck_whenMessageValid() {
        String message = """
                {"originalMessageId":"123","recordStatus":"SUCCESS","bucket":"converted-files","pdfPath":"path/to/file.pdf"}
                """;

        consumer.consume(record(message), ack);

        verify(service).applyConversionResult(any(FileConversionResult.class));
        verify(ack).acknowledge();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not a json",
            "{invalid}",
            "",
            "{\"recordStatus\":}"
    })
    void consume_shouldAckAndSkip_whenMessageMalformed(String badMessage) {
        consumer.consume(record(badMessage), ack);

        verify(service, never()).applyConversionResult(any());
        verify(dltRecoverer).accept(any(ConsumerRecord.class), any());
        verify(ack).acknowledge();
    }

    @Test
    void consume_shouldNotAck_whenProcessingFails() {
        String message = """
                {"originalMessageId":"123","recordStatus":"SUCCESS","bucket":"converted-files","pdfPath":"path/to/file.pdf"}
                """;
        doThrow(new RuntimeException("db down")).when(service).applyConversionResult(any());

        assertThrows(RuntimeException.class, () -> consumer.consume(record(message), ack));

        verify(ack, never()).acknowledge();
    }
}