package org.example.service;

import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointDataDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.entity.RentalPoint;
import org.example.entity.Scooter;
import org.example.entity.ScooterModel;
import org.example.entity.ScooterStatus;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.RentalPointMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalPointServiceTest {

    @Mock private RentalPointRepository rentalPointRepository;
    @Mock private ScooterRepository scooterRepository;
    @Mock private RentalPointMapper rentalPointMapper;
    @Mock private ScooterMapper scooterMapper;

    @InjectMocks
    private RentalPointService rentalPointService;

    private RentalPoint rentalPoint;
    private Long id = 1L;
    private String name = "Point A";

    @BeforeEach
    void setUp() {
        rentalPoint = new RentalPoint();
        rentalPoint.setId(id);
        rentalPoint.setName(name);
        rentalPoint.setCity("Минск");
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

        RentalPoint result = rentalPointService.createRentalPoint(dto);

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
    @DisplayName("findRentalPointByName - Успех")
    void findRentalPointByName_Success() {
        when(rentalPointRepository.findRentalPointByName(name)).thenReturn(Optional.of(rentalPoint));
        RentalPoint result = rentalPointService.findRentalPointByName(name);
        assertEquals(name, result.getName());
    }

    @Test
    @DisplayName("findAllRentalPoints - Успех")
    void findAllRentalPoints_Success() {
        when(rentalPointRepository.findAll()).thenReturn(Collections.singletonList(rentalPoint));
        List<RentalPoint> result = rentalPointService.findAllRentalPoints();
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
        when(rentalPointRepository.findById(id)).thenReturn(Optional.of(rentalPoint));

        rentalPointService.deleteById(id);

        verify(rentalPointRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("findAllScootersAtRentalPoint - Успех")
    void findAllScootersAtRentalPoint_Success() {
        Scooter scooter = new Scooter();
        when(rentalPointRepository.findById(id)).thenReturn(Optional.of(rentalPoint));
        when(scooterRepository.findAllByRentalPoint(id)).thenReturn(Collections.singletonList(scooter));

        List<Scooter> result = rentalPointService.findAllScootersAtRentalPoint(id);

        assertEquals(1, result.size());
        verify(scooterRepository, times(1)).findAllByRentalPoint(id);
    }

    @Test
    @DisplayName("getRentalPointDataById - Успех")
    void getRentalPointDataById_Success() {
        ScooterModel model = new ScooterModel();
        model.setName("Model X");

        Scooter scooter = new Scooter();
        scooter.setScooterStatus(ScooterStatus.AVAILABLE);
        scooter.setScooterModel(model);

        when(rentalPointRepository.findById(id)).thenReturn(Optional.of(rentalPoint));
        when(scooterRepository.findAllByRentalPoint(id)).thenReturn(Collections.singletonList(scooter));

        RentalPointDataDto result = rentalPointService.getRentalPointDataById(id);

        assertNotNull(result);
        assertEquals(1, result.getTotalScooters());
        assertEquals(1, result.getAvailableScooters());
    }

    @Test
    @DisplayName("getRentalPointDataById - Точка не найдена (Негативный)")
    void getRentalPointDataById_NotFound_ThrowsException() {
        when(rentalPointRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rentalPointService.getRentalPointDataById(id));

        verify(scooterRepository, never()).findAllByRentalPoint(any());
    }
}