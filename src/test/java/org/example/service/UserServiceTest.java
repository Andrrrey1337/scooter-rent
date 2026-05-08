package org.example.service;

import org.example.dto.user.UserAdminUpdateDto;
import org.example.dto.user.UserUpdateDto;
import org.example.dto.user.UserCreateDto;
import org.example.dto.user.UserResponseDto;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDto responseDto;

    private Long userId = 1L;
    private String username = "testuser";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword("password");
        user.setBalance(BigDecimal.ZERO);
        user.setRole(Role.USER);
        user.setIsActive(true);
        responseDto = new UserResponseDto();
        responseDto.setId(userId);
        responseDto.setUsername(username);
        responseDto.setBalance(BigDecimal.ZERO);
        responseDto.setRole(Role.USER);
    }

    @Test
    @DisplayName("registerUser - Успех")
    void registerUser_Success() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername(username);
        createDto.setPassword("password");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userMapper.toEntity(any(UserCreateDto.class))).thenReturn(user);
        when(userRepository.create(any(User.class))).thenReturn(user);
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setRole(Role.USER);
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.registerUser(createDto);

        assertNotNull(result);
        assertEquals(Role.USER, result.getRole());
        verify(userRepository).create(user);
    }

    @Test
    @DisplayName("registerUser - Пользователь уже существует")
    void registerUser_AlreadyExists_ThrowsBusinessException() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername(username);
        createDto.setPassword("password");
        when(userMapper.toEntity(any(UserCreateDto.class))).thenReturn(user);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        assertThrows(BusinessException.class, () -> userService.registerUser(createDto));
    }

    @Test
    @DisplayName("getDtoById - Успех")
    void getDtoById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);
        UserResponseDto result = userService.getDtoById(userId);
        assertEquals(userId, result.getId());
    }

    @Test
    @DisplayName("getDtoById - Не найден")
    void getDtoById_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getDtoById(userId));
    }

    @Test
    @DisplayName("findByUsername - Успех")
    void findByUsername_Success() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);
        UserResponseDto result = userService.findByUsername(username);
        assertEquals(username, result.getUsername());
    }

    @Test
    @DisplayName("addBalance - Успех")
    void addBalance_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserResponseDto updatedDto = new UserResponseDto();
        updatedDto.setBalance(new BigDecimal("100.00"));
        when(userMapper.toDto(user)).thenReturn(updatedDto);
        BigDecimal amount = new BigDecimal("100.00");
        UserResponseDto result = userService.addBalance(userId, amount);
        assertEquals(new BigDecimal("100.00"), result.getBalance());
    }

    @Test
    @DisplayName("addBalance - Отрицательная сумма")
    void addBalance_NegativeAmount_ThrowsBusinessException() {
        assertThrows(BusinessException.class, () -> userService.addBalance(userId, new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("updateUser - Успех")
    void updateUser_Success() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("newUsername");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(userMapper.toDto(user)).thenReturn(responseDto);

        userService.updateUser(userId, updateDto);

        verify(userMapper).updateEntity(updateDto, user);
    }

    @Test
    @DisplayName("updateUser - Ошибка: Новое имя пользователя уже занято")
    void updateUser_UsernameAlreadyTaken_ThrowsBusinessException() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setUsername("taken_username");

        User anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setUsername("taken_username");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("taken_username")).thenReturn(Optional.of(anotherUser));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUser(userId, updateDto));

        verify(userMapper, never()).updateEntity((UserUpdateDto) any(), any());
    }

    @Test
    @DisplayName("updateAdminFields - Успех")
    void updateAdminFields_Success() {
        UserAdminUpdateDto adminDto = new UserAdminUpdateDto();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        userService.updateAdminFields(userId, adminDto);

        verify(userMapper).updateEntity(adminDto, user);
    }
}
