package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.subscription.SubscriptionCreateDto;
import org.example.dto.subscription.SubscriptionResponseDto;
import org.example.dto.subscription.SubscriptionUpdateDto;
import org.example.entity.Subscription;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.SubscriptionMapper;
import org.example.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionResponseDto createSubscription(SubscriptionCreateDto dto) {
        Subscription subscription = subscriptionMapper.toEntity(dto);
        subscription = subscriptionRepository.create(subscription);
        log.info("Создан новый абонемент: {}", subscription.getName());
        return subscriptionMapper.toDto(subscription);
    }

    public Subscription findSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Абонемент с ID " + id + " не найден"));

        log.info("Успешно найден абонемент с ID: {}", id);
        return subscription;
    }

    public List<SubscriptionResponseDto> findAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        log.info("Получен список всех абонементов. Количество: {}", subscriptions.size());
        return subscriptionMapper.toDtos(subscriptions);
    }

    public SubscriptionResponseDto updateSubscription(Long id, SubscriptionUpdateDto subscriptionUpdateDto) {
        Subscription subscription = findSubscriptionById(id);

        subscriptionMapper.updateSubscription(subscriptionUpdateDto, subscription);
        log.info("Данные абонемента с ID {} успешно обновлены", id);

        return subscriptionMapper.toDto(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDto getSubscriptionDtoById(Long id) {
        return subscriptionMapper.toDto(findSubscriptionById(id));
    }

    public void deleteSubscription(Long id){
        subscriptionRepository.deleteById(id);
        log.info("Абонемент с ID {} успешно удален", id);
    }
}
