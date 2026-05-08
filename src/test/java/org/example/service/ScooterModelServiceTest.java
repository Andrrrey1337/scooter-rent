package org.example.service;

import org.example.dto.scooterModel.ScooterModelUpdateDto;
import org.example.dto.scooterModel.ScooterModelCreateDto;
import org.example.dto.scooterModel.ScooterModelResponseDto;
import org.example.entity.ScooterModel;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.ScooterModelMapper;
import org.example.repository.ScooterModelRepository;
import org.example.service.ScooterModelService;
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
class ScooterModelServiceTest {

    @Mock private ScooterModelRepository scooterModelRepository;
    @Mock private ScooterModelMapper scooterModelMapper;

    @InjectMocks
    private ScooterModelService scooterModelService;

    private ScooterModel scooterModel;
    private ScooterModelResponseDto scooterModelResponseDto;
    private Long id = 1L;
    private String name = "Model X";

    @BeforeEach
    void setUp() {
        scooterModel = new ScooterModel();
        scooterModel.setId(id);
        scooterModel.setName(name);
        scooterModelResponseDto = new ScooterModelResponseDto();
        scooterModelResponseDto.setId(id);
        scooterModelResponseDto.setName(name);
    }

    @Test
    @DisplayName("createScooterModel - Успех")
    void createScooterModel_Success() {
        ScooterModelCreateDto createDto = new ScooterModelCreateDto();
        createDto.setName("Yamaha");
        ScooterModel modelFromDto = new ScooterModel();
        modelFromDto.setName("Yamaha");
        when(scooterModelMapper.toEntity(createDto)).thenReturn(modelFromDto);
        when(scooterModelRepository.findByName("Yamaha")).thenReturn(Optional.empty());
        when(scooterModelRepository.create(any(ScooterModel.class))).thenReturn(scooterModel);
        when(scooterModelMapper.toDto(scooterModel)).thenReturn(scooterModelResponseDto);
        ScooterModelResponseDto result = scooterModelService.createScooterModel(createDto);

        assertNotNull(result);
        assertEquals(name, result.getName());
    }

    @Test
    @DisplayName("createScooterModel - Уже существует")
    void createScooterModel_AlreadyExists_ThrowsBusinessException() {
        ScooterModelCreateDto createDto = new ScooterModelCreateDto();
        createDto.setName("Yamaha");
        ScooterModel modelFromDto = new ScooterModel();
        modelFromDto.setName("Yamaha");
        when(scooterModelMapper.toEntity(createDto)).thenReturn(modelFromDto);
        when(scooterModelRepository.findByName("Yamaha")).thenReturn(Optional.of(scooterModel));
        assertThrows(BusinessException.class, () -> scooterModelService.createScooterModel(createDto));
    }

    @Test
    @DisplayName("findScooterModelById - Успех")
    void findScooterModelById_Success() {
        when(scooterModelRepository.findById(id)).thenReturn(Optional.of(scooterModel));
        ScooterModel result = scooterModelService.findScooterModelById(id);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findScooterModelById - Не найдена")
    void findScooterModelById_NotFound_ThrowsResourceNotFoundException() {
        when(scooterModelRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> scooterModelService.findScooterModelById(id));
    }

    @Test
    @DisplayName("findAllScooterModel - Успех")
    void findAllScooterModel_Success() {
        when(scooterModelRepository.findAll()).thenReturn(Collections.singletonList(scooterModel));
        when(scooterModelMapper.toDtos(any())).thenReturn(Collections.singletonList(scooterModelResponseDto));
        List<ScooterModelResponseDto> result = scooterModelService.findAllScooterModels();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateScooterModel - Успех")
    void updateScooterModel_Success() {
        ScooterModelUpdateDto updateDto = new ScooterModelUpdateDto();
        when(scooterModelRepository.findById(id)).thenReturn(Optional.of(scooterModel));
        when(scooterModelMapper.toDto(scooterModel)).thenReturn(scooterModelResponseDto);

        scooterModelService.updateScooterModel(id, updateDto);

        verify(scooterModelMapper).updateEntity(updateDto, scooterModel);
    }

    @Test
    @DisplayName("deleteScooterModelById - Успех")
    void deleteScooterModelById_Success() {
        scooterModelService.deleteScooterModelById(id);
        verify(scooterModelRepository).deleteById(id);
    }
}
