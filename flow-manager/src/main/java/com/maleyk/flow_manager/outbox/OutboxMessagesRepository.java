package com.maleyk.flow_manager.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessagesRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findAllByStatus(OutboxStatus status);
}

