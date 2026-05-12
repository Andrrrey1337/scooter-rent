package org.example.service;

import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointResponseDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.entity.RentalPoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RentalPointService {
    RentalPointResponseDto createRentalPoint(RentalPointCreateDto dto);

    List<RentalPointResponseDto> createRentalPointsBatch(List<RentalPointCreateDto> dtos);

    RentalPointResponseDto updateRentalPoint(Long id, RentalPointUpdateDto dto);

    // определяет вес адреса
    int getAddressLevel(RentalPoint point);

    RentalPoint findRentalPointById(Long id);

    List<RentalPointResponseDto> findAllRentalPoints();

    Optional<RentalPoint> findNearestValidParkingPoint(BigDecimal latitude, BigDecimal longitude, double radius);

    void deleteById(Long id);

    RentalPointResponseDto getRentalPointDtoById(Long id);

    RentalPointResponseDto getRentalPointDtoByName(String name);

}
