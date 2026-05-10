package org.example.service.impl;

import org.example.service.*;
import org.example.service.TariffService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.tariff.TariffCreateDto;
import org.example.dto.tariff.TariffResponseDto;
import org.example.dto.tariff.TariffUpdateDto;
import org.example.entity.Tariff;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.TariffMapper;
import org.example.repository.TariffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {
    private final TariffRepository tariffRepository;
    private final TariffMapper tariffMapper;

    public TariffResponseDto createTariff(TariffCreateDto dto) {
        Tariff tariff = tariffMapper.toEntity(dto);
        if (tariffRepository.findByName(tariff.getName()).isPresent()) {
            throw new BusinessException("Тариф с названием '" + tariff.getName() + "' уже существует");
        }
        tariff = tariffRepository.create(tariff);

        log.info("Успешно создан новый тариф: ID={}, название='{}', цена={}",
                tariff.getId(), tariff.getName(), tariff.getPrice());

        return tariffMapper.toDto(tariff);
    }

    @Transactional(readOnly = true)
    public Tariff findTariffById(Long id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Тариф с ID " + id + " не найден"));

        log.info("Успешно найден тариф с ID: {}", id);
        return tariff;
    }

    @Transactional(readOnly = true)
    public Tariff findTariffByName(String name) {
        Tariff tariff = tariffRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Тариф с названием '" + name + "' не найден"));

        log.info("Успешно найден тариф с названием: '{}'", name);
        return tariff;
    }

    @Transactional(readOnly = true)
    public List<TariffResponseDto> findAllTariffs() {
        List<Tariff> tariffs = tariffRepository.findAll();

        log.info("Получен список всех тарифов. Количество записей: {}", tariffs.size());
        return tariffMapper.toDtos(tariffs);
    }

    public TariffResponseDto updateTariff(Long id, TariffUpdateDto tariffDto) {
        Tariff existTariff = findTariffById(id);

        if (isNotBlank(tariffDto.getName()) && notEqual(tariffDto.getName(), existTariff.getName())) {
            log.info("Обновление названия тарифа с '{}' на '{}'", existTariff.getName(), tariffDto.getName());
            if (tariffRepository.findByName(tariffDto.getName()).isPresent()) {
                throw new BusinessException("Тариф с названием '" + tariffDto.getName() + "' уже существует");
            }
        } else {
            log.info("Название тарифа не предоставлено или не изменилось, пропуск валидации имени");
        }

        tariffMapper.updateEntity(tariffDto, existTariff);
        log.info("Данные тарифа с ID {} успешно обновлены", existTariff.getId());

        return tariffMapper.toDto(existTariff);
    }

    @Transactional(readOnly = true)
    public TariffResponseDto getTariffDtoById(Long id) {
        return tariffMapper.toDto(findTariffById(id));
    }

    @Transactional(readOnly = true)
    public TariffResponseDto getTariffDtoByName(String name) {
        return tariffMapper.toDto(findTariffByName(name));
    }

    public void deleteTariffById(Long id) {
        tariffRepository.deleteById(id);
        log.info("Тариф с ID {} успешно удален", id);
    }
}
