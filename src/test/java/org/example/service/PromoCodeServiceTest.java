package org.example.service;

import org.example.dto.promocode.PromoCodeUpdateDto;
import org.example.dto.promocode.PromoCodeCreateDto;
import org.example.dto.promocode.PromoCodeResponseDto;
import org.example.entity.PromoCode;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.PromoCodeMapper;
import org.example.repository.PromoCodeRepository;
import org.example.service.PromoCodeService;
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
class PromoCodeServiceTest {

    @Mock private PromoCodeRepository promoCodeRepository;
    @Mock private PromoCodeMapper promoCodeMapper;

    @InjectMocks
    private PromoCodeService promoCodeService;

    private PromoCode promoCode;
    private PromoCodeResponseDto promoCodeResponseDto;
    private Long id = 1L;
    private String code = "SALE50";

    @BeforeEach
    void setUp() {
        promoCode = new PromoCode();
        promoCode.setId(id);
        promoCode.setCode(code);
        promoCode.setDiscount(50);
        promoCodeResponseDto = new PromoCodeResponseDto();
        promoCodeResponseDto.setId(id);
        promoCodeResponseDto.setCode(code);
        promoCodeResponseDto.setDiscount(50);
    }

    @Test
    @DisplayName("createPromoCode - Успех")
    void createPromoCode_Success() {
        PromoCodeCreateDto createDto = new PromoCodeCreateDto();
        createDto.setCode(code);
        createDto.setDiscount(50);
        when(promoCodeMapper.toEntity(createDto)).thenReturn(promoCode);
        when(promoCodeRepository.findByCode(code)).thenReturn(Optional.empty());
        when(promoCodeRepository.create(promoCode)).thenReturn(promoCode);
        when(promoCodeMapper.toDto(promoCode)).thenReturn(promoCodeResponseDto);
        PromoCodeResponseDto result = promoCodeService.createPromoCode(createDto);

        assertNotNull(result);
        assertEquals(code, result.getCode());
        verify(promoCodeRepository).create(promoCode);
    }

    @Test
    @DisplayName("createPromoCode - Уже существует")
    void createPromoCode_AlreadyExists_ThrowsBusinessException() {
        PromoCodeCreateDto createDto = new PromoCodeCreateDto();
        createDto.setCode(code);
        createDto.setDiscount(50);
        when(promoCodeMapper.toEntity(createDto)).thenReturn(promoCode);
        when(promoCodeRepository.findByCode(code)).thenReturn(Optional.of(promoCode));
        assertThrows(BusinessException.class, () -> promoCodeService.createPromoCode(createDto));
    }

    @Test
    @DisplayName("findPromoCodeById - Успех")
    void findPromoCodeById_Success() {
        when(promoCodeRepository.findById(id)).thenReturn(Optional.of(promoCode));
        when(promoCodeMapper.toDto(promoCode)).thenReturn(promoCodeResponseDto);
        PromoCodeResponseDto result = promoCodeService.getDtoById(id);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findPromoCodeById - Не найден")
    void findPromoCodeById_NotFound_ThrowsResourceNotFoundException() {
        when(promoCodeRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> promoCodeService.getDtoById(id));
    }

    @Test
    @DisplayName("findAllPromoCodes - Успех")
    void findAllPromoCodes_Success() {
        when(promoCodeRepository.findAll()).thenReturn(Collections.singletonList(promoCode));
        PromoCodeResponseDto dto = new PromoCodeResponseDto();
        when(promoCodeMapper.toDtos(any())).thenReturn(Collections.singletonList(dto));
        List<PromoCodeResponseDto> result = promoCodeService.findAllPromoCodes();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updatePromoCode - Успех")
    void updatePromoCode_Success() {
        PromoCodeUpdateDto updateDto = new PromoCodeUpdateDto();
        updateDto.setCode("NEWCODE");
        
        when(promoCodeRepository.findById(id)).thenReturn(Optional.of(promoCode));
        when(promoCodeRepository.findByCode("NEWCODE")).thenReturn(Optional.empty());

        promoCodeService.updatePromoCode(id, updateDto);

        verify(promoCodeMapper).updatePromoCode(updateDto, promoCode);
    }

    @Test
    @DisplayName("deletePromoCode - Успех")
    void deletePromoCode_Success() {
        promoCodeService.deletePromoCode(id);
        verify(promoCodeRepository).deleteById(id);
    }
}
