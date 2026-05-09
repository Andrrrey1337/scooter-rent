package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointDataDto;
import org.example.dto.point.RentalPointResponseDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.service.RentalPointFacade;
import org.example.service.RentalPointService;
import org.example.service.ScooterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Tag(name = "Точки проката", description = "Управление точками проката самокатов")
public class RentalPointController {
    private final RentalPointService rentalPointService;
    private final ScooterService scooterService;
    private final RentalPointFacade rentalPointFacade;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать новую точку проката", description = "Доступно только администраторам")
    public ResponseEntity<RentalPointResponseDto> createRentalPoint(@Valid @RequestBody RentalPointCreateDto rentalPointCreateDto) {
        RentalPointResponseDto rentalPoint = rentalPointService.createRentalPoint(rentalPointCreateDto);
        return new ResponseEntity<>(rentalPoint, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать несколько точек проката", description = "Пакетное добавление новых точек")
    public ResponseEntity<List<RentalPointResponseDto>> createRentalPointsBatch(@Valid @RequestBody List<RentalPointCreateDto> dtos) {
        List<RentalPointResponseDto> rentalPoints = rentalPointService.createRentalPointsBatch(dtos);
        return new ResponseEntity<>(rentalPoints, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить точку проката по ID")
    public ResponseEntity<RentalPointResponseDto> getRentalPointById(@PathVariable Long id) {
        RentalPointResponseDto rentalPoint = rentalPointService.getRentalPointDtoById(id);
        return ResponseEntity.ok(rentalPoint);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Получить точку проката по названию")
    public ResponseEntity<RentalPointResponseDto> getRentalPointByName(@PathVariable String name) {
        RentalPointResponseDto rentalPoint = rentalPointService.getRentalPointDtoByName(name);
        return ResponseEntity.ok(rentalPoint);
    }

    @GetMapping
    @Operation(summary = "Получить список всех точек проката")
    public ResponseEntity<List<RentalPointResponseDto>> getAllRentalPoints() {
        List<RentalPointResponseDto> rentalPoints = rentalPointService.findAllRentalPoints();
        return ResponseEntity.ok(rentalPoints);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить данные точки проката", description = "Частичное обновление полей")
    public ResponseEntity<RentalPointResponseDto> updateRentalPoint(@PathVariable Long id, @Valid @RequestBody RentalPointUpdateDto rentalPointUpdateDto) {
        RentalPointResponseDto rentalPoint = rentalPointService.updateRentalPoint(id, rentalPointUpdateDto);
        return ResponseEntity.ok(rentalPoint);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить точку проката")
    public ResponseEntity<Void> deleteRentalPoint(@PathVariable Long id) {
        rentalPointService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/scooters/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список всех самокатов на точке проката (админ)")
    public ResponseEntity<List<ScooterAdminResponseDto>> getScootersAtPointById(@PathVariable Long id) {
        List<ScooterAdminResponseDto> scooters = scooterService.getScooterAdminDtosAtRentalPoint(id);
        return ResponseEntity.ok(scooters);
    }

    @GetMapping("/data/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить детальную статистику по точке проката", description = "Количество свободных/занятых самокатов, список моделей")
    public ResponseEntity<RentalPointDataDto> getRentalPointDataById(@PathVariable Long id) { // только для админов
        RentalPointDataDto data = rentalPointFacade.getRentalPointDataById(id);
        return ResponseEntity.ok(data);
    }
}
