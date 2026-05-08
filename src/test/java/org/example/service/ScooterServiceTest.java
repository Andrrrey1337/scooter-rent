package org.example.service;

import org.example.dto.scooter.ScooterCreateDto;
import org.example.dto.scooter.ScooterUpdateDto;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.dto.scooter.ScooterResponseDto;
import org.example.entity.RentalPoint;
import org.example.entity.Scooter;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.ScooterMapper;
import org.example.repository.RentalPointRepository;
import org.example.repository.ScooterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScooterServiceTest {

    @Mock private ScooterRepository scooterRepository;
    @Mock private ScooterMapper scooterMapper;
    @Mock private RentalPointRepository rentalPointRepository;
    @Mock private RentalPointService rentalPointService;

    @InjectMocks
    private ScooterService scooterService;

    private Scooter scooter;
    private ScooterCreateDto scooterCreateDto;
    private ScooterAdminResponseDto scooterAdminResponseDto;
    private ScooterResponseDto scooterResponseDto;
    private Long scooterId = 1L;
    private String serialNumber = "SN123";

    @BeforeEach
    void setUp() {
        scooter = new Scooter();
        scooter.setId(scooterId);
        scooter.setSerialNumber(serialNumber);

        scooterCreateDto = new ScooterCreateDto();
        scooterCreateDto.setSerialNumber(serialNumber);
        scooterCreateDto.setRentalPointId(1L);

        scooterAdminResponseDto = new ScooterAdminResponseDto();
        scooterAdminResponseDto.setSerialNumber(serialNumber);
        scooterResponseDto = new ScooterResponseDto();
        scooterResponseDto.setSerialNumber(serialNumber);
    }

    @Test
    @DisplayName("createScooter - Успех")
    void createScooter_Success() {
        when(scooterRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.empty());
        when(scooterMapper.toEntity(scooterCreateDto)).thenReturn(scooter);
        when(rentalPointRepository.findById(1L)).thenReturn(Optional.of(new RentalPoint()));
        when(rentalPointService.getAddressLevel(any())).thenReturn(3); // Обязательно уровень 3
        when(scooterRepository.create(any())).thenReturn(scooter);
        when(scooterMapper.toAdminDto(scooter)).thenReturn(scooterAdminResponseDto);

        ScooterAdminResponseDto result = scooterService.createScooter(scooterCreateDto);

        assertNotNull(result);
        assertEquals(serialNumber, result.getSerialNumber());
        verify(scooterRepository).create(scooter);
    }

    @Test
    @DisplayName("createScooter - Уже существует")
    void createScooter_AlreadyExists_ThrowsBusinessException() {
        when(scooterRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.of(scooter));
        assertThrows(BusinessException.class, () -> scooterService.createScooter(scooterCreateDto));
    }

    @Test
    @DisplayName("createScooter - Ошибка: Точка не 3 уровня")
    void createScooter_WrongLevel_ThrowsBusinessException() {
        when(scooterRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.empty());
        when(scooterMapper.toEntity(scooterCreateDto)).thenReturn(scooter);
        when(rentalPointRepository.findById(1L)).thenReturn(Optional.of(new RentalPoint()));
        when(rentalPointService.getAddressLevel(any())).thenReturn(2); // Улица, а не Дом

        assertThrows(BusinessException.class, () -> scooterService.createScooter(scooterCreateDto));
    }

    @Test
    @DisplayName("findScooterById - Успех")
    void findScooterById_Success() {
        when(scooterRepository.findById(scooterId)).thenReturn(Optional.of(scooter));
        Scooter result = scooterService.findScooterById(scooterId);
        assertEquals(scooterId, result.getId());
    }

    @Test
    @DisplayName("findScooterById - Не найден")
    void findScooterById_NotFound_ThrowsResourceNotFoundException() {
        when(scooterRepository.findById(scooterId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> scooterService.findScooterById(scooterId));
    }

    @Test
    @DisplayName("getScooterDtoBySerialNumber - Успех")
    void getScooterDtoBySerialNumber_Success() {
        when(scooterRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.of(scooter));
        when(scooterMapper.toDto(scooter)).thenReturn(scooterResponseDto);
        ScooterResponseDto result = scooterService.getScooterDtoBySerialNumber(serialNumber);
        assertEquals(serialNumber, result.getSerialNumber());
    }

    @Test
    @DisplayName("findAvailableScooters - Успех")
    void findAvailableScooters_Success() {
        when(scooterRepository.findAvailableByRentalPoint(1L, 50)).thenReturn(Collections.singletonList(scooter));
        when(scooterMapper.toDtos(any())).thenReturn(Collections.singletonList(scooterResponseDto));
        List<ScooterResponseDto> result = scooterService.findAvailableScooters(1L, 50);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("deleteScooterById - Успех")
    void deleteScooterById_Success() {

        scooterService.deleteScooterById(scooterId);

        verify(scooterRepository).deleteById(scooterId);
    }
}
