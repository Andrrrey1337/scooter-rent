package org.example.service;

import org.example.dto.subscription.UserSubscriptionResponseDto;
import org.example.entity.UserSubscription;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionService {
    UserSubscriptionResponseDto buySubscription(Long userId, Long subscriptionId);

    Optional<UserSubscription> findValidActiveSubscription(Long userId);

    void updateSubscription(UserSubscription userSubscription);

    UserSubscription findActiveSubscription(Long userId);

    List<UserSubscription> findPurchaseHistory(Long userId);

    UserSubscriptionResponseDto findActiveSubscriptionDto(Long userId);

    List<UserSubscriptionResponseDto> findPurchaseHistoryDto(Long userId);

}
