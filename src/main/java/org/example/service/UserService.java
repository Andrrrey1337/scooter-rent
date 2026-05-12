package org.example.service;

import org.example.dto.user.UserAdminUpdateDto;
import org.example.dto.user.UserCreateDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdateDto;
import org.example.entity.User;

import java.math.BigDecimal;

public interface UserService {
    UserResponseDto registerUser(UserCreateDto dto);

    UserResponseDto findByUsername(String username);

    User findEntityById(Long id);

    UserResponseDto getDtoById(Long id);

    UserResponseDto addBalance(Long userId, BigDecimal amount);

    UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto);

    void update(User user);

    UserResponseDto updateAdminFields(Long userId, UserAdminUpdateDto dto);

}
