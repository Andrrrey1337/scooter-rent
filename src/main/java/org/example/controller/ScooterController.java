package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.dto.scooter.ScooterCreateDto;
import org.example.dto.scooter.ScooterResponseDto;
import org.example.dto.scooter.ScooterUpdateDto;
import org.example.entity.Scooter;
import org.example.mapper.ScooterMapper;
import org.example.service.ScooterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scooters")
@RequiredArgsConstructor
@Tag(name = "Самокаты", description = "Управление парком самокатов")
public class ScooterController {
    private final ScooterService scooterService;
    private final ScooterMapper scooterMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Добавить новый самокат", description = "Доступно только администраторам")
    public ResponseEntity<ScooterAdminResponseDto> createScooter(@Valid @RequestBody ScooterCreateDto scooterCreateDto) {
        Scooter scooter = scooterService.createScooter(scooterCreateDto);
        return new  ResponseEntity<>(scooterMapper.toAdminDto(scooter), HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Добавить несколько самокатов", description = "Пакетное добавление новых самокатов")
    public ResponseEntity<List<ScooterAdminResponseDto>> createScootersBatch(@Valid @RequestBody List<ScooterCreateDto> dtos) {
        List<Scooter> scooters = scooterService.createScootersBatch(dtos);
        return new ResponseEntity<>(scooterMapper.toAdminDtos(scooters), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить самокат по ID (админ)", description = "Возвращает полную информацию о самокате")
    public ResponseEntity<ScooterAdminResponseDto> getScooterById(@PathVariable Long id) {
        Scooter scooter= scooterService.findScooterById(id);
        return ResponseEntity.ok(scooterMapper.toAdminDto(scooter));
    }

    @GetMapping("/number/{number}")
    @Operation(summary = "Найти самокат по серийному номеру")
    public ResponseEntity<ScooterResponseDto> getScooterByNumber(@PathVariable String number) { // всем
        Scooter scooter = scooterService.findScooterBySerialNumber(number);
        return ResponseEntity.ok(scooterMapper.toDto(scooter));
    }

    @GetMapping("/available")
    @Operation(summary = "Получить список доступных самокатов на точке", description = "Фильтрация по ID точки и минимальному уровню заряда")
    public ResponseEntity<List<ScooterResponseDto>> getAvailableScooters( // всем
            @RequestParam("pointId") Long id,
            @RequestParam(value = "minBattery", defaultValue = "20") Integer minBatteryLevel) {
        List<Scooter> scooters = scooterService.findAvailableScooters(id, minBatteryLevel);
        return ResponseEntity.ok(scooterMapper.toDtos(scooters));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить самокат из системы")
    public ResponseEntity<Void> deleteScooter(@PathVariable Long id) {
        scooterService.deleteScooterById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить данные самоката (админ)", description = "Изменение статуса, координат, уровня заряда или точки привязки")
    public ResponseEntity<ScooterAdminResponseDto> updateScooter(@PathVariable Long id, @Valid @RequestBody ScooterUpdateDto scooterUpdateDto) {
        Scooter scooter = scooterService.updateScooter(id,  scooterUpdateDto);
        return ResponseEntity.ok(scooterMapper.toAdminDto(scooter));
    }
}
