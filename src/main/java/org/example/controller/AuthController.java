package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.auth.JwtRequest;
import org.example.dto.auth.JwtResponse;
import org.example.dto.user.UserCreateDto;
import org.example.dto.user.UserResponseDto;
import org.example.service.AuthService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "Управление токенами и входом в систему")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя. По умолчанию назначается роль USER.")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto userCreateDto) {
        UserResponseDto user = userService.registerUser(userCreateDto);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Войти в систему", description = "Возвращает JWT токен при успешном вводе логина и пароля")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody JwtRequest jwtRequest) {
        JwtResponse response = authService.login(jwtRequest);
        return ResponseEntity.ok(response);
    }
}
