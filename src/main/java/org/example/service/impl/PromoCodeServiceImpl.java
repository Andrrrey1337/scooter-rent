package org.example.service.impl;

import org.example.service.PromoCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.promocode.PromoCodeCreateDto;
import org.example.dto.promocode.PromoCodeResponseDto;
import org.example.dto.promocode.PromoCodeUpdateDto;
import org.example.entity.PromoCode;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.PromoCodeMapper;
import org.example.repository.PromoCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeMapper promoCodeMapper;

    public PromoCodeResponseDto createPromoCode(PromoCodeCreateDto dto){
        PromoCode promoCode = promoCodeMapper.toEntity(dto);
        String code = promoCode.getCode();
        Optional<PromoCode> existingPromoCode = promoCodeRepository.findByCode(code);
        if (existingPromoCode.isPresent()){
            throw new BusinessException("Промокод " + code + " уже существует");
        }
        log.info("Создан новый промокод: {}", code);
        return promoCodeMapper.toDto(promoCodeRepository.create(promoCode));
    }

    @Transactional(readOnly = true)
    public PromoCode findByCode(String code) {
        return promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Промокод '" + code + "' не найден"));
    }

    public PromoCodeResponseDto getDtoById(Long id){
        return promoCodeMapper.toDto(findEntityById(id));
    }

    public List<PromoCodeResponseDto> findAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        log.info("Получен список всех промокодов. Количество: {}", promoCodes.size());
        return promoCodeMapper.toDtos(promoCodes);
    }

    public void deletePromoCode(Long id){
        promoCodeRepository.deleteById(id);
        log.info("Промокод с ID {} успешно удален", id);
    }

    public PromoCodeResponseDto updatePromoCode(Long id, PromoCodeUpdateDto promoCodeUpdateDto){
        PromoCode promoCode = findEntityById(id);
        String newCode = promoCodeUpdateDto.getCode();
        String oldCode = promoCode.getCode();

        if (isNotBlank(newCode) && notEqual(newCode, oldCode)) {
            Optional<PromoCode> existingPromoCode = promoCodeRepository.findByCode(newCode);
            if (existingPromoCode.isPresent()) {
                throw new BusinessException("Промокод '" + newCode + "' уже существует");
            }
            log.info("Обновление промокода с '{}' на '{}'", oldCode, newCode);
        } else {
            log.info("Промокод не предоставлен или не изменилось, пропуск валидации кода");
        }

        promoCodeMapper.updatePromoCode(promoCodeUpdateDto, promoCode);
        log.info("Данные промокода с ID {} успешно обновлены", id);

        return promoCodeMapper.toDto(promoCode);
    }

    private PromoCode findEntityById(Long id) {
        PromoCode promoCode = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Промокод с ID " + id + " не найден"));

        log.info("Успешно найден промокод с ID: {}", id);
        return promoCode;
    }
}
