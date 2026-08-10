package com.maleyk.flow_manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maleyk.flow_manager.dto.SubscriptionResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, SubscriptionResponse> subscriptionRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        Jackson2JsonRedisSerializer<SubscriptionResponse> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, SubscriptionResponse.class);

        RedisTemplate<String, SubscriptionResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        return template;
    }
}