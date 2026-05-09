package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.subscription.UserSubscriptionResponseDto;
import org.example.entity.Subscription;
import org.example.entity.User;
import org.example.entity.UserSubscription;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.UserSubscriptionMapper;
import org.example.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserSubscriptionService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final UserSubscriptionMapper userSubscriptionMapper;

    public UserSubscriptionResponseDto buySubscription(Long userId, Long subscriptionId) {
        User user = userService.findEntityById(userId);
        Subscription subscription = subscriptionService.findSubscriptionById(subscriptionId);
        validateSubscriptionPurchaseAvailability(userId);
        validateUserBalanceForPurchase(user, subscription);
        withdrawSubscriptionPriceFromUserBalance(user, subscription);

        UserSubscription userSubscription = buildUserSubscription(user, subscription);
        userSubscription = userSubscriptionRepository.create(userSubscription);

        log.info("Пользователь {} успешно купил абонемент '{}'. Списано: {} руб.",
                user.getUsername(), subscription.getName(), subscription.getPrice());

        return userSubscriptionMapper.toDto(userSubscription);
    }

    public Optional<UserSubscription> findValidActiveSubscription(Long userId) {
        Optional<UserSubscription> userSubscriptionOptional = userSubscriptionRepository.findActiveByUserId(userId);

        if (userSubscriptionOptional.isPresent()) {
            UserSubscription userSubscription = userSubscriptionOptional.get();
            if (userSubscription.getEndDate().isBefore(LocalDateTime.now())) {
                log.info("Срок действия абонемента ID={} для пользователя ID={} истек. Деактивация.",
                        userSubscription.getId(), userId);
                userSubscription.setIsActive(false);
                userSubscriptionRepository.update(userSubscription);
                return Optional.empty();
            }
        }
        return userSubscriptionOptional;
    }

    public void updateSubscription(UserSubscription userSubscription) {
        userSubscriptionRepository.update(userSubscription);
    }

    private void validateSubscriptionPurchaseAvailability(Long userId) {
        if (findValidActiveSubscription(userId).isPresent()) {
            throw new BusinessException("У вас уже есть активный абонемент. Дождитесь его окончания.");
        }
    }

    private void validateUserBalanceForPurchase(User user, Subscription subscription) {
        if (user.getBalance().compareTo(subscription.getPrice()) < 0) {
            throw new BusinessException("Недостаточно средств для покупки абонемента. Пополните баланс.");
        }
    }

    private void withdrawSubscriptionPriceFromUserBalance(User user, Subscription subscription) {
        user.setBalance(user.getBalance().subtract(subscription.getPrice()));
        userService.update(user);
    }

    private UserSubscription buildUserSubscription(User user, Subscription subscription) {
        return UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .endDate(LocalDateTime.now().plusDays(subscription.getDurationDays()))
                .remainingMinutes(subscription.getIncludeMinutes())
                .isActive(true)
                .build();
    }

    public UserSubscription findActiveSubscription(Long userId) { // активный абонемент
        return findValidActiveSubscription(userId)
                .orElseThrow(() -> new ResourceNotFoundException("У вас нет активного абонемента"));
    }

    @Transactional(readOnly = true)
    public List<UserSubscription> findPurchaseHistory(Long userId) { // История покупок абонементов
        List<UserSubscription> history = userSubscriptionRepository.findAllByUserId(userId);
        log.info("Получена история подписок для пользователя ID={}. Записей: {}", userId, history.size());
        return history;
    }

    @Transactional(readOnly = true)
    public UserSubscriptionResponseDto findActiveSubscriptionDto(Long userId) {
        return userSubscriptionMapper.toDto(findActiveSubscription(userId));
    }

    @Transactional(readOnly = true)
    public List<UserSubscriptionResponseDto> findPurchaseHistoryDto(Long userId) {
        return findPurchaseHistory(userId).stream()
                .map(userSubscriptionMapper::toDto)
                .toList();
    }
}
