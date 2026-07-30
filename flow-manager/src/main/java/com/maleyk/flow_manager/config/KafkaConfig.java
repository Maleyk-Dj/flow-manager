package com.maleyk.flow_manager.config;

import com.maleyk.flow_manager.dto.FileConversionResult;
import com.maleyk.flow_manager.service.FileRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.databind.ObjectMapper;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public DeadLetterPublishingRecoverer dltRecoverer(KafkaTemplate<String, String> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate);
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer dltRecoverer,
                                            FileRecordService service,
                                            ObjectMapper objectMapper) {

        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            dltRecoverer.accept(record, exception);
            try {
                FileConversionResult original = objectMapper.readValue(
                        (String) record.value(), FileConversionResult.class);

                FileConversionResult applyFailure = FileConversionResult.failed(
                        original.getOriginalMessageId(),
                        "Не удалось сохранить результат конвертации после ретраев: " +
                                rootCauseMessage(exception));

                service.applyConversionResult(applyFailure);
            } catch (Exception e) {
                log.error("Не удалось пометить запись ERROR после исчерпания ретраев: {}",
                        record.value(), e);
            }
        };
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(10_000L);
        backOff.setMaxElapsedTime(60_000L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }
}
