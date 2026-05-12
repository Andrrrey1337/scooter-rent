package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.promocode.PromoCodeCreateDto;
import org.example.dto.promocode.PromoCodeResponseDto;
import org.example.dto.promocode.PromoCodeUpdateDto;
import org.example.service.PromoCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promocodes")
@RequiredArgsConstructor
@Tag(name = "Админ: Промокоды", description = "Управление системой скидок")
@PreAuthorize("hasRole('ADMIN')")
public class PromoCodeController {
    private final PromoCodeService promoCodeService;

    @PostMapping()
    @Operation(summary = "Создать новый промокод (админ)")
    public ResponseEntity<PromoCodeResponseDto> create(@Valid @RequestBody PromoCodeCreateDto promoCodeCreateDto) {
        PromoCodeResponseDto promoCode = promoCodeService.createPromoCode(promoCodeCreateDto);
        return new ResponseEntity<>(promoCode, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Получить все промокоды (админ)")
    public ResponseEntity<List<PromoCodeResponseDto>> getAll() {
        List<PromoCodeResponseDto> promoCodes = promoCodeService.findAllPromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить промокод по его id (админ)")
    public ResponseEntity<PromoCodeResponseDto> getPromoCodeById(@PathVariable Long id) {
        PromoCodeResponseDto promoCode = promoCodeService.getDtoById(id);
        return ResponseEntity.ok(promoCode);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Обновить промокод (админ)")
    public ResponseEntity<PromoCodeResponseDto> update(@PathVariable Long id, @Valid @RequestBody PromoCodeUpdateDto dto) {
        PromoCodeResponseDto promoCode = promoCodeService.updatePromoCode(id, dto);
        return ResponseEntity.ok(promoCode);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить промокод (админ)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promoCodeService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }
}
