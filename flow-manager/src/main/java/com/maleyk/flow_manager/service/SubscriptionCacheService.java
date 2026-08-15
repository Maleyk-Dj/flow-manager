package com.maleyk.flow_manager.service;

import com.maleyk.flow_manager.dto.SubscriptionResponse;
import com.maleyk.flow_manager.feign.SubscriptionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SubscriptionCacheService {

    private final RedisTemplate<String, SubscriptionResponse> subscriptionRedisTemplate;
    private final SubscriptionClient subscriptionClient;

    public SubscriptionResponse getSubscriptionCached(String login) {
        String key = "subscription:" + login;

        SubscriptionResponse cached = subscriptionRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        SubscriptionResponse fresh = subscriptionClient.getSubscription(login, login);
        subscriptionRedisTemplate.opsForValue().set(key, fresh, Duration.ofMinutes(10));
        return fresh;
    }
}
