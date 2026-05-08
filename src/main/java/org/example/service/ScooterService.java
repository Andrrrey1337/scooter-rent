package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.dto.scooter.ScooterCreateDto;
import org.example.dto.scooter.ScooterResponseDto;
import org.example.dto.scooter.ScooterUpdateDto;
import org.example.entity.RentalPoint;
import org.example.entity.Scooter;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.ScooterMapper;
import org.example.repository.RentalPointRepository;
import org.example.repository.ScooterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScooterService {
    private final ScooterRepository scooterRepository;
    private final ScooterMapper scooterMapper;
    private final RentalPointRepository rentalPointRepository;
    private final RentalPointService rentalPointService;

    public ScooterAdminResponseDto createScooter(ScooterCreateDto scooterDto) {
        validateSerialNumberUniqueness(scooterDto.getSerialNumber());

        Scooter scooter = scooterMapper.toEntity(scooterDto);
        assignRentalPoint(scooter, scooterDto.getRentalPointId());

        scooter = scooterRepository.create(scooter);
        log.info("Успешно зарегистрирован новый самокат: SN={}, ID={}", scooter.getSerialNumber(), scooter.getId());

        return scooterMapper.toAdminDto(scooter);
    }

    public List<ScooterAdminResponseDto> createScootersBatch(List<ScooterCreateDto> dtos) {
        log.info("Начато пакетное создание самокатов. Количество: {}", dtos.size());
        List<ScooterAdminResponseDto> savedScooters = dtos.stream().map(this::createScooter).toList();
        log.info("Успешно завершено пакетное создание {} самокатов", savedScooters.size());
        return savedScooters;
    }

    public ScooterAdminResponseDto updateScooter(Long id, ScooterUpdateDto scooterDto) {
        Scooter scooter = findScooterById(id);
        scooterMapper.updateEntity(scooterDto, scooter);

        if (scooterDto.getRentalPointId() != null) {
            assignRentalPoint(scooter, scooterDto.getRentalPointId());
        }

        log.info("Данные самоката с ID {} успешно обновлены", scooter.getId());
        return scooterMapper.toAdminDto(scooter);
    }

    // установка точки
    private void assignRentalPoint(Scooter scooter, Long rentalPointId) {
        if (rentalPointId == null) return;

        validateRentalPointForScooter(rentalPointId);

        scooter.setRentalPoint(rentalPointRepository.findById(rentalPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Точка проката с ID " + rentalPointId + " не найдена")));
    }

    // есть ли у точки дочерние элементы
    private void validateRentalPointForScooter(Long rentalPointId) {
        RentalPoint point = rentalPointRepository.findById(rentalPointId)
                .orElseThrow(() -> new ResourceNotFoundException("Точка проката не найдена"));

        if (rentalPointService.getAddressLevel(point) != 3) {
            throw new BusinessException("Самокат можно привязать только к конечной точке проката");
        }
    }

    private void validateSerialNumberUniqueness(String serialNumber) {
        if (scooterRepository.findBySerialNumber(serialNumber).isPresent()) {
            throw new BusinessException("Самокат с серийным номером " + serialNumber + " уже существует в базе");
        }
    }

    @Transactional(readOnly = true)
    public Scooter findScooterById(Long id) {
        return scooterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Самокат с id " + id + " не найден"));
    }

    @Transactional(readOnly = true)
    public ScooterAdminResponseDto getScooterAdminDtoById(Long id) {
        return scooterMapper.toAdminDto(findScooterById(id));
    }

    @Transactional(readOnly = true)
    public ScooterResponseDto getScooterDtoBySerialNumber(String serialNumber) {
        Scooter scooter = scooterRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Самокат с серийным номером " + serialNumber + " не найден"));
        return scooterMapper.toDto(scooter);
    }

    @Transactional(readOnly = true)
    public List<ScooterResponseDto> findAvailableScooters(Long rentalPointId, Integer minBatteryLevel) {
        return scooterMapper.toDtos(scooterRepository.findAvailableByRentalPoint(rentalPointId, minBatteryLevel));
    }

    public void deleteScooterById(Long scooterId) {
        scooterRepository.deleteById(scooterId);
        log.info("Самокат с ID {} успешно удален из базы", scooterId);
    }
}
