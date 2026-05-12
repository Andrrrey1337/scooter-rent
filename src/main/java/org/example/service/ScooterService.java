package org.example.service;

import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.dto.scooter.ScooterCreateDto;
import org.example.dto.scooter.ScooterResponseDto;
import org.example.dto.scooter.ScooterUpdateDto;
import org.example.entity.Scooter;

import java.util.List;

public interface ScooterService {
    ScooterAdminResponseDto createScooter(ScooterCreateDto scooterDto);

    List<ScooterAdminResponseDto> createScootersBatch(List<ScooterCreateDto> dtos);

    ScooterAdminResponseDto updateScooter(Long id, ScooterUpdateDto scooterDto);

    List<Scooter> findAllByRentalPoint(Long rentalPointId);

    List<ScooterAdminResponseDto> getScooterAdminDtosAtRentalPoint(Long rentalPointId);

    Scooter findScooterById(Long id);

    ScooterAdminResponseDto getScooterAdminDtoById(Long id);

    ScooterResponseDto getScooterDtoBySerialNumber(String serialNumber);

    List<ScooterResponseDto> findAvailableScooters(Long rentalPointId, Integer minBatteryLevel);

    void update(Scooter scooter);

    void deleteScooterById(Long scooterId);

}
