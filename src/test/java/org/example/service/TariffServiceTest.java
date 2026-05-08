package org.example.service;

import org.example.dto.tariff.TariffUpdateDto;
import org.example.dto.tariff.TariffResponseDto;
import org.example.dto.tariff.TariffCreateDto;
import org.example.entity.Tariff;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.TariffMapper;
import org.example.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {

    @Mock private TariffRepository tariffRepository;
    @Mock private TariffMapper tariffMapper;

    @InjectMocks
    private TariffService tariffService;

    private Tariff testTariff;
    private TariffResponseDto tariffResponseDto;

    @BeforeEach
    void setUp() {
        testTariff = Tariff.builder()
                .id(1L)
                .name("Базовый")
                .description("Описание тарифа")
                .price(BigDecimal.valueOf(50.0))
                .build();
        tariffResponseDto = new TariffResponseDto();
        tariffResponseDto.setName("Базовый");
    }

    @Test
    @DisplayName("Успешное создание тарифа")
    void createTariff_Success() {
        TariffCreateDto createDto = new TariffCreateDto();
        createDto.setName("Базовый");
        when(tariffRepository.findByName("Базовый")).thenReturn(Optional.empty());
        when(tariffMapper.toEntity(createDto)).thenReturn(testTariff);
        when(tariffRepository.create(any(Tariff.class))).thenReturn(testTariff);
        when(tariffMapper.toDto(testTariff)).thenReturn(tariffResponseDto);

        TariffResponseDto result = tariffService.createTariff(createDto);

        assertNotNull(result);
        assertEquals("Базовый", result.getName());
        verify(tariffRepository, times(1)).create(any(Tariff.class));
    }

    @Test
    @DisplayName("Ошибка создания: тариф с таким именем уже существует")
    void createTariff_NameAlreadyExists_ThrowsBusinessException() {
        TariffCreateDto createDto = new TariffCreateDto();
        createDto.setName("Базовый");
        when(tariffMapper.toEntity(createDto)).thenReturn(testTariff);
        when(tariffRepository.findByName(testTariff.getName())).thenReturn(Optional.of(testTariff));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tariffService.createTariff(createDto));

        assertEquals("Тариф с названием 'Базовый' уже существует", exception.getMessage());

        verify(tariffRepository, never()).create(any(Tariff.class));
    }

    @Test
    @DisplayName("Успешный поиск тарифа по ID")
    void findTariffById_Success() {
        when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff));

        Tariff result = tariffService.findTariffById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(tariffRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Успешный поиск тарифа по названию")
    void findTariffByName_Success() {
        when(tariffRepository.findByName("Базовый")).thenReturn(Optional.of(testTariff));

        Tariff result = tariffService.findTariffByName("Базовый");

        assertNotNull(result);
        assertEquals("Базовый", result.getName());
        verify(tariffRepository, times(1)).findByName("Базовый");
    }

    @Test
    @DisplayName("Успешное получение списка всех тарифов")
    void findAllTariffs_Success() {
        when(tariffRepository.findAll()).thenReturn(List.of(testTariff));
        when(tariffMapper.toDtos(any())).thenReturn(List.of(tariffResponseDto));

        List<TariffResponseDto> result = tariffService.findAllTariffs();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tariffRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Успешное обновление тарифа")
    void updateTariff_Success() {
        TariffUpdateDto updateDto = new TariffUpdateDto();
        updateDto.setDescription("Новое описание");

        when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff));
        when(tariffMapper.toDto(testTariff)).thenReturn(tariffResponseDto);

        TariffResponseDto result = tariffService.updateTariff(1L, updateDto);

        assertNotNull(result);
        verify(tariffMapper, times(1)).updateEntity(updateDto, testTariff);
    }

    @Test
    @DisplayName("Ошибка обновления: новое имя тарифа уже занято другим тарифом")
    void updateTariff_NameAlreadyTaken_ThrowsBusinessException() {
        TariffUpdateDto updateDto = new TariffUpdateDto();
        updateDto.setName("Премиум"); //  переименовать "Базовый" в "Премиум"

        Tariff existingPremiumTariff = new Tariff();
        existingPremiumTariff.setId(2L);
        existingPremiumTariff.setName("Премиум");

        when(tariffRepository.findById(1L)).thenReturn(Optional.of(testTariff)); //  текущий тариф
        // имя "Премиум" уже занято другим тарифом в базе
        when(tariffRepository.findByName("Премиум")).thenReturn(Optional.of(existingPremiumTariff));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> tariffService.updateTariff(1L, updateDto));

        assertEquals("Тариф с названием 'Премиум' уже существует", exception.getMessage());

        verify(tariffMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("Ошибка: тариф не найден по ID")
    void findTariffById_NotFound_ThrowsResourceNotFoundException() {
        when(tariffRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> tariffService.findTariffById(99L));

        assertEquals("Тариф с ID 99 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Успешное удаление тарифа")
    void deleteTariffById_Success() {

        tariffService.deleteTariffById(1L);

        verify(tariffRepository, times(1)).deleteById(1L);
    }
}