package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.dto.scooterModel.ScooterModelCreateDto;
import org.example.dto.scooterModel.ScooterModelResponseDto;
import org.example.dto.scooterModel.ScooterModelUpdateDto;
import org.example.service.ScooterModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scooter-models")
@RequiredArgsConstructor
@Tag(name = "Модели самокатов", description = "Управление типами и характеристиками самокатов")
public class ScooterModelController {
    private final ScooterModelService scooterModelService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую модель самоката (админ)")
    public ResponseEntity<ScooterModelResponseDto> createScooterModel(
            @Valid @RequestBody ScooterModelCreateDto scooterModelCreateDto) {
        ScooterModelResponseDto scooterModel = scooterModelService.createScooterModel(scooterModelCreateDto);
        return new ResponseEntity<>(scooterModel, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить информацию о модели по ID")
    public ResponseEntity<ScooterModelResponseDto> getScooterModelById(@PathVariable Long id) {
        ScooterModelResponseDto scooterModel = scooterModelService.getScooterModelDtoById(id);
        return ResponseEntity.ok(scooterModel);
    }

    @GetMapping
    @Operation(summary = "Получить список всех доступных моделей")
    public ResponseEntity<List<ScooterModelResponseDto>> getAllScooterModels() {
        List<ScooterModelResponseDto> scooterModels = scooterModelService.findAllScooterModels();
        return ResponseEntity.ok(scooterModels);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить характеристики модели (админ)")
    public ResponseEntity<ScooterModelResponseDto> updateScooterModel(
            @PathVariable Long id, 
            @Valid @RequestBody ScooterModelUpdateDto scooterModelUpdateDto) {
        ScooterModelResponseDto scooterModel = scooterModelService.updateScooterModel(id, scooterModelUpdateDto);
        return ResponseEntity.ok(scooterModel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить модель из системы (админ)")
    public ResponseEntity<Void> deleteScooterModel(@PathVariable Long id) {
        scooterModelService.deleteScooterModelById(id);
        return ResponseEntity.noContent().build();
    }
}
