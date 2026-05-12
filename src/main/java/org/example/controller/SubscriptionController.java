package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.subscription.SubscriptionCreateDto;
import org.example.dto.subscription.SubscriptionResponseDto;
import org.example.dto.subscription.SubscriptionUpdateDto;
import org.example.dto.subscription.UserSubscriptionResponseDto;
import org.example.entity.User;
import org.example.service.SubscriptionService;
import org.example.service.UserSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Абонементы", description = "Просмотр и управление тарифными планами")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final UserSubscriptionService userSubscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новый абонемент (админ)")
    public ResponseEntity<SubscriptionResponseDto> create(@Valid @RequestBody SubscriptionCreateDto dto) {
        SubscriptionResponseDto subscription = subscriptionService.createSubscription(dto);
        return new ResponseEntity<>(subscription, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/buy")
    @Operation(summary = "Купить абонемент", description = "Списывает средства с баланса текущего авторизованного пользователя")
    public ResponseEntity<UserSubscriptionResponseDto> buySubscription(@PathVariable Long id, @AuthenticationPrincipal User user) { // id подписки
        // id пользователя возьмем из токена
        UserSubscriptionResponseDto subscription = userSubscriptionService.buySubscription(user.getId(), id);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping
    @Operation(summary = "Посмотреть список всех абонементов")
    public ResponseEntity<List<SubscriptionResponseDto>> getAll() {
        List<SubscriptionResponseDto> subscriptions = subscriptionService.findAllSubscriptions();
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить абонемент по его id (админ)")
    public ResponseEntity<SubscriptionResponseDto> getSubscriptionById(@PathVariable Long id) {
        SubscriptionResponseDto subscription = subscriptionService.getSubscriptionDtoById(id);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/my/active")
    @Operation(summary = "Мой активный абонемент", description = "Возвращает информацию о текущем действующем абонементе пользователя")
    public ResponseEntity<UserSubscriptionResponseDto> getMyActiveSubscription(@AuthenticationPrincipal User currentUser) {
        UserSubscriptionResponseDto subscription = userSubscriptionService.findActiveSubscriptionDto(currentUser.getId());
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/my/history")
    @Operation(summary = "История моих покупок", description = "Возвращает список всех когда-либо купленных абонементов")
    public ResponseEntity<List<UserSubscriptionResponseDto>> getMySubscriptionHistory(@AuthenticationPrincipal User currentUser) {
        List<UserSubscriptionResponseDto> subscriptions = userSubscriptionService.findPurchaseHistoryDto(currentUser.getId());
        return ResponseEntity.ok(subscriptions);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Изменить условия абонемента (админ)")
    public ResponseEntity<SubscriptionResponseDto> update(@PathVariable Long id, @Valid @RequestBody SubscriptionUpdateDto dto) {
        SubscriptionResponseDto subscription = subscriptionService.updateSubscription(id, dto);
        return ResponseEntity.ok(subscription);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить абонемент (админ)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }
}
