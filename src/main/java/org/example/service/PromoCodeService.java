package org.example.service;

import org.example.dto.promocode.PromoCodeCreateDto;
import org.example.dto.promocode.PromoCodeResponseDto;
import org.example.dto.promocode.PromoCodeUpdateDto;
import org.example.entity.PromoCode;

import java.util.List;

public interface PromoCodeService {
    PromoCodeResponseDto createPromoCode(PromoCodeCreateDto dto);

    PromoCode findByCode(String code);

    PromoCodeResponseDto getDtoById(Long id);

    List<PromoCodeResponseDto> findAllPromoCodes();

    void deletePromoCode(Long id);

    PromoCodeResponseDto updatePromoCode(Long id, PromoCodeUpdateDto promoCodeUpdateDto);

}
