package org.example.service;

import org.example.service.impl.RentalPointServiceImpl;

import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.dto.point.RentalPointResponseDto;
import org.example.entity.RentalPoint;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.RentalPointMapper;
import org.example.repository.RentalPointRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalPointServiceTest {

    @Mock private RentalPointRepository rentalPointRepository;
    @Mock private RentalPointMapper rentalPointMapper;

    @InjectMocks
    private RentalPointServiceImpl rentalPointService;

    private RentalPoint rentalPoint;
    private RentalPointResponseDto rentalPointResponseDto;
    private Long id = 1L;
    private String name = "Point A";

    @BeforeEach
    void setUp() {
        rentalPoint = new RentalPoint();
        rentalPoint.setId(id);
        rentalPoint.setName(name);
        rentalPoint.setCity("Минск");
        rentalPointResponseDto = new RentalPointResponseDto();
        rentalPointResponseDto.setId(id);
        rentalPointResponseDto.setName(name);
    }

    @Test
    @DisplayName("createRentalPoint - Успех")
    void createRentalPoint_Success() {
        RentalPointCreateDto dto = new RentalPointCreateDto();
        dto.setName(name);
        dto.setCity("Минск");

        when(rentalPointRepository.findRentalPointByName(name)).thenReturn(Optional.empty());
        when(rentalPointMapper.toEntity(dto)).thenReturn(rentalPoint);
        when(rentalPointRepository.create(rentalPoint)).thenReturn(rentalPoint);
        when(rentalPointMapper.toDto(rentalPoint)).thenReturn(rentalPointResponseDto);

        RentalPointResponseDto result = rentalPointService.createRentalPoint(dto);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    @DisplayName("createRentalPoint - Уже существует")
    void createRentalPoint_AlreadyExists_ThrowsBusinessException() {
        RentalPointCreateDto dto = new RentalPointCreateDto();
        dto.setName(name);
        when(rentalPointRepository.findRentalPointByName(name)).thenReturn(Optional.of(rentalPoint));

        assertThrows(BusinessException.class, () -> rentalPointService.createRentalPoint(dto));
    }

    @Test
    @DisplayName("findRentalPointById - Успех")
    void findRentalPointById_Success() {
        when(rentalPointRepository.findById(id)).thenReturn(Optional.of(rentalPoint));
        RentalPoint result = rentalPointService.findRentalPointById(id);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findRentalPointById - Возвращает null, если передан null")
    void findRentalPointById_NullId_ReturnsNull() {
        RentalPoint result = rentalPointService.findRentalPointById(null);
        assertNull(result);
        verify(rentalPointRepository, never()).findById(any());
    }

    @Test
    @DisplayName("findRentalPointById - Не найдена")
    void findRentalPointById_NotFound_ThrowsResourceNotFoundException() {
        when(rentalPointRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rentalPointService.findRentalPointById(id));
    }

    @Test
    @DisplayName("getRentalPointDtoByName - Успех")
    void getRentalPointDtoByName_Success() {
        when(rentalPointRepository.findRentalPointByName(name)).thenReturn(Optional.of(rentalPoint));
        when(rentalPointMapper.toDto(rentalPoint)).thenReturn(rentalPointResponseDto);
        RentalPointResponseDto result = rentalPointService.getRentalPointDtoByName(name);
        assertEquals(name, result.getName());
    }

    @Test
    @DisplayName("findAllRentalPoints - Успех")
    void findAllRentalPoints_Success() {
        when(rentalPointRepository.findAll()).thenReturn(Collections.singletonList(rentalPoint));
        when(rentalPointMapper.toDtos(any())).thenReturn(Collections.singletonList(rentalPointResponseDto));
        List<RentalPointResponseDto> result = rentalPointService.findAllRentalPoints();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateRentalPoint - Успех")
    void updateRentalPoint_Success() {
        RentalPointUpdateDto updateDto = new RentalPointUpdateDto();
        updateDto.setName("New Name");

        when(rentalPointRepository.findById(id)).thenReturn(Optional.of(rentalPoint));

        rentalPointService.updateRentalPoint(id, updateDto);

        verify(rentalPointMapper).updateEntity(updateDto, rentalPoint);
    }

    @Test
    @DisplayName("validateAndApplyHierarchy - Перескакивание уровня (Город -> Дом)")
    void validateAndApplyHierarchy_LevelSkipping_ThrowsException() {
        RentalPoint parent = new RentalPoint();
        parent.setId(10L);
        parent.setCity("Минск"); // Уровень 1

        RentalPoint child = new RentalPoint();
        child.setCity("Минск");
        child.setStreet("Ленина");
        child.setHouseNumber("1"); // Уровень 3

        RentalPointCreateDto dto = new RentalPointCreateDto();
        dto.setParentId(10L);

        when(rentalPointMapper.toEntity(dto)).thenReturn(child);
        when(rentalPointRepository.findById(10L)).thenReturn(Optional.of(parent));

        assertThrows(BusinessException.class, () -> rentalPointService.createRentalPoint(dto));
    }

    @Test
    @DisplayName("validateAndApplyHierarchy - Наследование города")
    void validateAndApplyHierarchy_InheritCity_Success() {
        RentalPoint parent = new RentalPoint();
        parent.setId(2L);
        parent.setCity("Минск");

        RentalPoint child = new RentalPoint();
        child.setName("Child");
        child.setStreet("Ленина"); // Делаем ребенка детальнее (уровень 2)

        RentalPointCreateDto dto = new RentalPointCreateDto();
        dto.setParentId(2L);

        when(rentalPointMapper.toEntity(dto)).thenReturn(child);
        when(rentalPointRepository.findById(2L)).thenReturn(Optional.of(parent));
        when(rentalPointRepository.create(child)).thenReturn(child);

        rentalPointService.createRentalPoint(dto);

        assertEquals("Минск", child.getCity());
    }

    @Test
    @DisplayName("validateAndApplyHierarchy - Несоответствие города")
    void validateAndApplyHierarchy_CityMismatch_ThrowsException() {
        RentalPoint parent = new RentalPoint();
        parent.setId(2L);
        parent.setCity("Минск");

        RentalPoint child = new RentalPoint();
        child.setCity("Москва");
        child.setStreet("Ленина");

        RentalPointCreateDto dto = new RentalPointCreateDto();
        dto.setParentId(2L);

        when(rentalPointMapper.toEntity(dto)).thenReturn(child);
        when(rentalPointRepository.findById(2L)).thenReturn(Optional.of(parent));

        assertThrows(BusinessException.class, () -> rentalPointService.createRentalPoint(dto));
    }

    @Test
    @DisplayName("deleteById - Успех")
    void deleteById_Success() {

        rentalPointService.deleteById(id);

        verify(rentalPointRepository).deleteById(id);
    }
    }