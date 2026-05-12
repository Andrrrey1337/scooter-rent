package org.example.service;

import org.example.dto.subscription.SubscriptionCreateDto;
import org.example.dto.subscription.SubscriptionResponseDto;
import org.example.dto.subscription.SubscriptionUpdateDto;
import org.example.entity.Subscription;

import java.util.List;

public interface SubscriptionService {
    SubscriptionResponseDto createSubscription(SubscriptionCreateDto dto);

    Subscription findSubscriptionById(Long id);

    List<SubscriptionResponseDto> findAllSubscriptions();

    SubscriptionResponseDto updateSubscription(Long id, SubscriptionUpdateDto subscriptionUpdateDto);

    SubscriptionResponseDto getSubscriptionDtoById(Long id);

    void deleteSubscription(Long id);

}
