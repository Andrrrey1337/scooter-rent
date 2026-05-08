package org.example.service;

import org.example.dto.subscription.SubscriptionUpdateDto;
import org.example.dto.subscription.SubscriptionCreateDto;
import org.example.dto.subscription.SubscriptionResponseDto;
import org.example.entity.Subscription;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.SubscriptionMapper;
import org.example.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription subscription;
    private SubscriptionResponseDto responseDto;

    private Long id = 1L;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
        subscription.setId(id);
        subscription.setName("Monthly");
        responseDto = new SubscriptionResponseDto();
        responseDto.setId(id);
        responseDto.setName("Monthly");
    }

    @Test
    @DisplayName("createSubscription - Успех")
    void createSubscription_Success() {
        SubscriptionCreateDto createDto = new SubscriptionCreateDto();
        createDto.setName("Monthly");
        when(subscriptionMapper.toEntity(createDto)).thenReturn(subscription);
        when(subscriptionRepository.create(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toDto(subscription)).thenReturn(responseDto);
        SubscriptionResponseDto result = subscriptionService.createSubscription(createDto);
        assertNotNull(result);
        verify(subscriptionRepository).create(any(Subscription.class));
    }

    @Test
    @DisplayName("findSubscriptionById - Успех")
    void findSubscriptionById_Success() {
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        Subscription result = subscriptionService.findSubscriptionById(id);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findSubscriptionById - Не найдена")
    void findSubscriptionById_NotFound_ThrowsResourceNotFoundException() {
        when(subscriptionRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.findSubscriptionById(id));
    }

    @Test
    @DisplayName("findAllSubscriptions - Успех")
    void findAllSubscriptions_Success() {
        when(subscriptionRepository.findAll()).thenReturn(Collections.singletonList(subscription));
        when(subscriptionMapper.toDtos(any())).thenReturn(Collections.singletonList(responseDto));
        List<SubscriptionResponseDto> result = subscriptionService.findAllSubscriptions();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateSubscription - Успех")
    void updateSubscription_Success() {
        SubscriptionUpdateDto updateDto = new SubscriptionUpdateDto();
        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toDto(subscription)).thenReturn(responseDto);

        SubscriptionResponseDto result = subscriptionService.updateSubscription(id, updateDto);

        assertNotNull(result);
        verify(subscriptionMapper).updateSubscription(updateDto, subscription);
    }

    @Test
    @DisplayName("deleteSubscription - Успех")
    void deleteSubscription_Success() {
        subscriptionService.deleteSubscription(id);
        verify(subscriptionRepository).deleteById(id);
    }
}
