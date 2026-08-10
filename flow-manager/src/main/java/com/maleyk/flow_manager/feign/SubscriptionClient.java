package com.maleyk.flow_manager.feign;

import com.maleyk.flow_manager.dto.SubscriptionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "subscription-service")
public interface SubscriptionClient {

    @GetMapping("/api/subscriptions/{login}")
    SubscriptionResponse getSubscription(@PathVariable String login,
                                         @RequestHeader("X-User-Login") String requesterLogin);
}
