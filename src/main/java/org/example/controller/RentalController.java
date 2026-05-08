package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.RentalAdminResponseDto;
import org.example.dto.rental.RentalResponseDto;
import org.example.dto.rental.StartRentalDto;
import org.example.entity.User;
import org.example.service.RentalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
@Tag(name = "Аренда", description = "Управление процессом аренды самокатов")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping("/start")
    @Operation(summary = "Начать аренду самоката")
    public ResponseEntity<RentalResponseDto> startRental(@Valid @RequestBody StartRentalDto startRentalDto) {
        RentalResponseDto rental = rentalService.startRental(startRentalDto);
        return new ResponseEntity<>(rental, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Завершить аренду самоката", description = "Рассчитывает стоимость и обновляет состояние самоката")
    public ResponseEntity<RentalResponseDto> finishRental(@PathVariable Long id, @Valid @RequestBody FinishRentalDto finishRentalDto) {
        RentalResponseDto rental = rentalService.finishRental(id, finishRentalDto);
        return ResponseEntity.ok(rental);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить историю аренды пользователя")
    public ResponseEntity<List<RentalResponseDto>> getUserRentals(@PathVariable Long userId) {
        List<RentalResponseDto> rentals = rentalService.findRentalsByUserId(userId);
        return ResponseEntity.ok(rentals);
}

    @GetMapping("/my")
    @Operation(summary = "Моя история аренды")
    public ResponseEntity<List<RentalResponseDto>> getMyRentals(@AuthenticationPrincipal User currentUser) {
        List<RentalResponseDto> rentals = rentalService.findRentalsByUserId(currentUser.getId());
        return ResponseEntity.ok(rentals);
    }

    @GetMapping("/scooter/{scooterId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить историю аренды конкретного самоката (админ)")
    public ResponseEntity<List<RentalAdminResponseDto>> getScooterRentals(@PathVariable Long scooterId) {
        List<RentalAdminResponseDto> rentals = rentalService.findRentalsByScooterId(scooterId);
        return ResponseEntity.ok(rentals);
    }


}
