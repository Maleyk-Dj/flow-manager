package com.maleyk.flow_manager.consumer;

import com.maleyk.flow_manager.dto.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiredConsumer {

    private final RedisTemplate<String, SubscriptionResponse> subscriptionResponseRedisTemplate;

    @KafkaListener(topics = "${kafka.topics.subscription-expired}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String login, Acknowledgment ack) {
        String key = "subscription:" + login;
        subscriptionResponseRedisTemplate.delete(key);
        log.info("Кеш подписки инвалидирован для: {}", login);
        ack.acknowledge();
    }
}
