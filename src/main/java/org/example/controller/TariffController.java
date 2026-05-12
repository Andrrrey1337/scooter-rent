package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.dto.tariff.TariffCreateDto;
import org.example.dto.tariff.TariffResponseDto;
import org.example.dto.tariff.TariffUpdateDto;
import org.example.service.TariffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@Tag(name = "Тарифы", description = "Управление тарифами на аренду (стоимость старта)")
public class TariffController {
    private final TariffService tariffService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новый тариф (админ)")
    public ResponseEntity<TariffResponseDto> createTariff(@Valid @RequestBody TariffCreateDto tariffCreateDto) {
        TariffResponseDto tariff = tariffService.createTariff(tariffCreateDto);
        return new ResponseEntity<>(tariff, HttpStatus.CREATED); // 201 статус
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить тариф по ID")
    public ResponseEntity<TariffResponseDto> getTariffById(@PathVariable Long id) {
        TariffResponseDto tariff = tariffService.getTariffDtoById(id);
        return ResponseEntity.ok(tariff);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Найти тариф по названию")
    public ResponseEntity<TariffResponseDto> getTariffByName(@PathVariable String name) {
        TariffResponseDto tariff = tariffService.getTariffDtoByName(name);
        return ResponseEntity.ok(tariff);
    }

    @GetMapping
    @Operation(summary = "Получить список всех тарифов")
    public ResponseEntity<List<TariffResponseDto>> getAllTariffs() {
        List<TariffResponseDto> tariffs = tariffService.findAllTariffs();
        return ResponseEntity.ok(tariffs);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить параметры тарифа (админ)")
    public ResponseEntity<TariffResponseDto> updateTariff(@PathVariable Long id, @Valid @RequestBody TariffUpdateDto tariffUpdateDto) {
        TariffResponseDto tariff = tariffService.updateTariff(id, tariffUpdateDto);
        return ResponseEntity.ok(tariff);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить тариф (админ)")
    public ResponseEntity<Void> deleteTariff(@PathVariable Long id) {
        tariffService.deleteTariffById(id);
        return ResponseEntity.noContent().build(); // 204 статус
    }
}
