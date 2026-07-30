package com.maleyk.flow_manager.outbox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxService outboxService;
    private static final long SEND_TIMEOUT_SECONDS = 5;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
            name = "flowManagerOutboxPoller",
            lockAtLeastFor = "PT4S",
            lockAtMostFor = "PT30S")
    public void poll() {
        outboxService.findAll().forEach(msg -> {
            try {
                kafkaTemplate.send(msg.getTopic(), msg.getPayload())
                        .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                outboxService.markSent(msg.getId());
                log.info("Outbox сообщение отправлено: {}", msg.getId());
            } catch (Exception e) {
                log.error("Не удалось отправить outbox сообщение: {}", msg.getId(), e);
            }
        });
    }

}

