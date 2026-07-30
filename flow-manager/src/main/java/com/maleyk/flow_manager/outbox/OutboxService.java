package com.maleyk.flow_manager.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessagesRepository repository;

    @Transactional
    public void save(String topic, String payload) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setTopic(topic);
        outboxMessage.setPayload(payload);
        outboxMessage.setStatus(OutboxStatus.NEW);
        outboxMessage.setCreatedAt(LocalDateTime.now());
        repository.save(outboxMessage);
    }

    @Transactional(readOnly = true)
    public List<OutboxMessage> findAll() {
        return repository.findAllByStatus(OutboxStatus.NEW);
    }

    @Transactional
    public void markSent(Long id) {
        repository.findById(id).ifPresent(outboxMessage ->
        {
            outboxMessage.setStatus(OutboxStatus.SENT);
            repository.save(outboxMessage);
        });
    }
}
