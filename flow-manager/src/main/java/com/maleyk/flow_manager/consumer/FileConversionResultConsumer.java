package com.maleyk.flow_manager.consumer;

import com.maleyk.flow_manager.dto.FileConversionResult;
import com.maleyk.flow_manager.service.FileRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileConversionResultConsumer {

    private final FileRecordService service;
    private final ObjectMapper objectMapper;
    private final DeadLetterPublishingRecoverer dltRecoverer;

    @KafkaListener(topics = "${kafka.topics.output}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        try {
            FileConversionResult result = objectMapper.readValue(message, FileConversionResult.class);
            service.applyConversionResult(result);
            ack.acknowledge();
        } catch (JacksonException e) {
            log.error("Неверный формат сообщения, пропускаю: {}", message, e);
            dltRecoverer.accept(record, e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Не удалось обработать сообщение, попробуем снова: {}", message, e);
            throw new RuntimeException(e);
        }
    }
}
