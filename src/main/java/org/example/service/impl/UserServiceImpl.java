package org.example.service.impl;

import org.example.service.*;
import org.example.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.user.UserAdminUpdateDto;
import org.example.dto.user.UserCreateDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdateDto;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.UserMapper;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto registerUser(UserCreateDto dto)  {
        User user = userMapper.toEntity(dto);
        String username = user.getUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Пользователь с именем " + username + "' уже существует");
        }

        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user = userRepository.create(user);
        log.info("Успешно зарегистрирован новый пользователь: ID={}, username={}", user.getId(), username);

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с именем '" + username + " не найден"));

        log.info("Успешно выполнен поиск пользователя по username: {}", username);

        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public User findEntityById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));

        log.info("Успешно выполнен поиск пользователя по ID: {}", id);
        return user;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getDtoById(Long id) {
        return userMapper.toDto(findEntityById(id));
    }

    public UserResponseDto addBalance(Long userId, BigDecimal amount) {
        if (BigDecimal.ZERO.compareTo(amount) >= 0) {
            throw new BusinessException("Сумма пополнения должна быть больше нуля");
        }
        User user = findEntityById(userId);
        user.setBalance(user.getBalance().add(amount));

        log.info("Баланс пользователя с ID {} успешно пополнен на {}. Текущий баланс: {}", userId, amount, user.getBalance());

        return userMapper.toDto(user);
    }

    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = findEntityById(userId);

        if (isNotBlank(userUpdateDto.getUsername()) && notEqual(userUpdateDto.getUsername(), user.getUsername())
                && userRepository.findByUsername(userUpdateDto.getUsername()).isPresent()) {
            throw new BusinessException("Пользователь с таким именем '" + userUpdateDto.getUsername() + "' уже существует");
        }

        userMapper.updateEntity(userUpdateDto, user);

        if (isNotBlank(userUpdateDto.getPassword())) {
            log.info("Обновление пароля для пользователя ID={}", userId);
            user.setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        } else {
            log.info("Новый пароль не предоставлен для пользователя ID={}, пропуск обновления пароля", userId);
        }

        log.info("Данные пользователя с ID {} успешно обновлены", user.getId());

        return userMapper.toDto(user);
        }

        public void update(User user) {
        userRepository.update(user);
        log.info("Сущность пользователя с ID {} обновлена напрямую", user.getId());
        }

        public UserResponseDto updateAdminFields(Long userId, UserAdminUpdateDto dto) {

        User user = findEntityById(userId);

        userMapper.updateEntity(dto, user);

        log.info("Изменены права/статус пользователя с ID={}. Новая роль: {}, Активен: {}",
                user.getId(), user.getRole(), user.getIsActive());

        return userMapper.toDto(user);
    }
}
