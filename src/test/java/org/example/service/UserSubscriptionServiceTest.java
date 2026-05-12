package org.example.service;

import org.example.service.impl.UserSubscriptionServiceImpl;

import org.example.dto.subscription.UserSubscriptionResponseDto;
import org.example.entity.Subscription;
import org.example.entity.User;
import org.example.entity.UserSubscription;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.UserSubscriptionMapper;
import org.example.repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSubscriptionServiceTest {

    @Mock private UserSubscriptionRepository userSubscriptionRepository;
    @Mock private UserService userService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private UserSubscriptionMapper userSubscriptionMapper;

    @InjectMocks
    private UserSubscriptionServiceImpl userSubscriptionService;

    private User user;
    private Subscription subscription;
    private UserSubscription userSubscription;
    private UserSubscriptionResponseDto responseDto;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setBalance(new BigDecimal("1000.00"));

        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setName("Monthly");
        subscription.setPrice(new BigDecimal("500.00"));
        subscription.setDurationDays(30);
        subscription.setIncludeMinutes(100);

        userSubscription = UserSubscription.builder()
                .id(1L)
                .user(user)
                .subscription(subscription)
                .isActive(true)
                .endDate(LocalDateTime.now().plusDays(30))
                .build();
        responseDto = new UserSubscriptionResponseDto();
        responseDto.setIsActive(true);
    }

    @Test
    @DisplayName("buySubscription - Успех")
    void buySubscription_Success() {
        when(userService.findEntityById(1L)).thenReturn(user);
        when(subscriptionService.findSubscriptionById(1L)).thenReturn(subscription);
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.create(any(UserSubscription.class))).thenReturn(userSubscription);
        when(userSubscriptionMapper.toDto(userSubscription)).thenReturn(responseDto);

        UserSubscriptionResponseDto result = userSubscriptionService.buySubscription(1L, 1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), user.getBalance());
        verify(userSubscriptionRepository).create(any(UserSubscription.class));
    }

    @Test
    @DisplayName("buySubscription - Уже есть активная подписка")
    void buySubscription_AlreadyHasActive_ThrowsBusinessException() {
        when(userService.findEntityById(1L)).thenReturn(user);
        when(subscriptionService.findSubscriptionById(1L)).thenReturn(subscription);
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.of(userSubscription));

        assertThrows(BusinessException.class, () -> userSubscriptionService.buySubscription(1L, 1L));
    }

    @Test
    @DisplayName("buySubscription - Недостаточно средств")
    void buySubscription_InsufficientFunds_ThrowsBusinessException() {
        user.setBalance(new BigDecimal("100.00"));
        when(userService.findEntityById(1L)).thenReturn(user);
        when(subscriptionService.findSubscriptionById(1L)).thenReturn(subscription);
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userSubscriptionService.buySubscription(1L, 1L));
    }

    @Test
    @DisplayName("findActiveSubscription - Успех")
    void findActiveSubscription_Success() {
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.of(userSubscription));
        UserSubscription result = userSubscriptionService.findActiveSubscription(1L);
        assertNotNull(result);
        assertTrue(result.getIsActive());
    }

    @Test
    @DisplayName("findActiveSubscription - Не найдена")
    void findActiveSubscription_NotFound_ThrowsResourceNotFoundException() {
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userSubscriptionService.findActiveSubscription(1L));
    }

    @Test
    @DisplayName("findPurchaseHistoryDto - Успех")
    void findPurchaseHistoryDto_Success() {
        when(userSubscriptionRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(userSubscription));
        List<UserSubscription> result = userSubscriptionService.findPurchaseHistory(1L);

        assertEquals(1, result.size());
    }
}
