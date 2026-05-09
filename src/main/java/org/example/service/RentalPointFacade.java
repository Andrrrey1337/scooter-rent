package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.point.RentalPointDataDto;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.entity.RentalPoint;
import org.example.entity.Scooter;
import org.example.entity.ScooterStatus;
import org.example.mapper.ScooterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalPointFacade {
    private final RentalPointService rentalPointService;
    private final ScooterService scooterService;
    private final ScooterMapper scooterMapper;

    @Transactional(readOnly = true)
    public RentalPointDataDto getRentalPointDataById(Long id) {
        RentalPoint point = rentalPointService.findRentalPointById(id);
        List<Scooter> allScooters = scooterService.findAllByRentalPoint(id);

        return buildRentalPointDataDto(point, allScooters);
    }

    private RentalPointDataDto buildRentalPointDataDto(RentalPoint point, List<Scooter> scooters) {
        List<Scooter> available = getAvailableScooters(scooters);
        long rentedCount = countRentedScooters(scooters);
        Map<String, Long> modelsSummary = buildAvailableModelsSummary(available);
        List<ScooterAdminResponseDto> availableScooters = scooterMapper.toAdminDtos(available);

        return buildDataDto(point, scooters.size(), available.size(), rentedCount, modelsSummary, availableScooters);
    }

    private RentalPointDataDto buildDataDto(RentalPoint point, int totalScooters, int availableScootersCount,
                                               long rentedScootersCount, Map<String, Long> modelsSummary, 
                                               List<ScooterAdminResponseDto> availableScooters) {
        return RentalPointDataDto.builder()
                .rentalPointId(point.getId())
                .rentalPointName(point.getName())
                .city(point.getCity())
                .street(point.getStreet())
                .houseNumber(point.getHouseNumber())
                .totalScooters(totalScooters)
                .availableScooters(availableScootersCount)
                .rentedScooters(rentedScootersCount)
                .availableModelsSummary(modelsSummary)
                .availableScootersList(availableScooters)
                .build();
    }

    private List<Scooter> getAvailableScooters(List<Scooter> scooters) {
        return scooters.stream()
                .filter(s -> ScooterStatus.AVAILABLE == s.getScooterStatus())
                .toList();
    }

    private long countRentedScooters(List<Scooter> scooters) {
        return scooters.stream()
                .filter(s -> ScooterStatus.RENTED == s.getScooterStatus())
                .count();
    }

    private Map<String, Long> buildAvailableModelsSummary(List<Scooter> availableScooters) {
        return availableScooters.stream()
                .collect(Collectors.groupingBy(s -> s.getScooterModel().getName(), Collectors.counting()));
    }
}
