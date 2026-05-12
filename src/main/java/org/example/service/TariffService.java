package org.example.service;

import org.example.dto.tariff.TariffCreateDto;
import org.example.dto.tariff.TariffResponseDto;
import org.example.dto.tariff.TariffUpdateDto;
import org.example.entity.Tariff;

import java.util.List;

public interface TariffService {
    TariffResponseDto createTariff(TariffCreateDto dto);

    Tariff findTariffById(Long id);

    Tariff findTariffByName(String name);

    List<TariffResponseDto> findAllTariffs();

    TariffResponseDto updateTariff(Long id, TariffUpdateDto tariffDto);

    TariffResponseDto getTariffDtoById(Long id);

    TariffResponseDto getTariffDtoByName(String name);

    void deleteTariffById(Long id);

}
