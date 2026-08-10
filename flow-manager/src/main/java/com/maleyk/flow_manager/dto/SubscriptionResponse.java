package com.maleyk.flow_manager.dto;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        String login,
        SubscriptionType subscriptionType,
        LocalDateTime expiresAt
) {
}
