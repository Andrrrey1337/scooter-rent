package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserAdminUpdateDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdateDto;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Управление профилями пользователей и их балансом")
public class UserController {
    private final UserService userService;

    @GetMapping("/username/{username}")
    @Operation(summary = "Получить пользователя по имени (админ)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) { // админам
        UserResponseDto user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    @Operation(summary = "Получить мой профиль")
    public ResponseEntity<UserResponseDto> getMyProfile(@AuthenticationPrincipal User currentUser) { // только себе
        UserResponseDto user = userService.getDtoById(currentUser.getId());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID (админ/владелец)", description = "Доступно администраторам или самому владельцу аккаунта")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) { // админам или самому себе
        UserResponseDto user = userService.getDtoById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Пополнить баланс (админ)")
    public ResponseEntity<UserResponseDto> addBalance(@PathVariable Long id, @RequestParam BigDecimal amount) {
        UserResponseDto user = userService.addBalance(id, amount);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/me/balance")
    @Operation(summary = "Пополнить мой баланс")
    public ResponseEntity<UserResponseDto> addMyBalance(@RequestParam BigDecimal amount, @AuthenticationPrincipal User currentUser) {
        UserResponseDto user = userService.addBalance(currentUser.getId(), amount);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить личную информацию пользователя (админ)", description = "Изменение имени или пароля.")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,@Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto user = userService.updateUser(id,userUpdateDto);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/me")
    @Operation(summary = "Обновить мою информацию")
    public ResponseEntity<UserResponseDto> updateMe(@Valid @RequestBody UserUpdateDto dto, @AuthenticationPrincipal User currentUser) {
        UserResponseDto user = userService.updateUser(currentUser.getId(), dto);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить права и статус пользователя (админ)", description = "Изменение роли или блокировка пользователя.")
    public ResponseEntity<UserResponseDto> updateAdminFields(
            @PathVariable Long id,
            @Valid @RequestBody UserAdminUpdateDto userAdminUpdateDto) {
        UserResponseDto user = userService.updateAdminFields(id,userAdminUpdateDto);
        return ResponseEntity.ok(user);
    }
}
