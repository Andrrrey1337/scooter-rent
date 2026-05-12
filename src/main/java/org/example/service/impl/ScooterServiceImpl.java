package org.example.service.impl;

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
import org.example.repository.ScooterRepository;
import org.example.service.RentalPointService;
import org.example.service.ScooterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScooterServiceImpl implements ScooterService {
    private final ScooterRepository scooterRepository;
    private final ScooterMapper scooterMapper;
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
        Long pointId = scooterDto.getRentalPointId();

        if (nonNull(pointId)) {
            assignRentalPoint(scooter, pointId);
        } else {
            log.info("Новая точка проката не указана для самоката ID={}, пропуск перепривязки", id);
        }

        log.info("Данные самоката с ID {} успешно обновлены", scooter.getId());
        return scooterMapper.toAdminDto(scooter);
    }

    @Transactional(readOnly = true)
    public List<Scooter> findAllByRentalPoint(Long rentalPointId) {
        return scooterRepository.findAllByRentalPoint(rentalPointId);
    }

    @Transactional(readOnly = true)
    public List<ScooterAdminResponseDto> getScooterAdminDtosAtRentalPoint(Long rentalPointId) {
        return scooterMapper.toAdminDtos(findAllByRentalPoint(rentalPointId));
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Самокат с серийным номером " + serialNumber + " не найден"));
        return scooterMapper.toDto(scooter);
    }

    @Transactional(readOnly = true)
    public List<ScooterResponseDto> findAvailableScooters(Long rentalPointId, Integer minBatteryLevel) {
        return scooterMapper.toDtos(scooterRepository.findAvailableByRentalPoint(rentalPointId, minBatteryLevel));
    }

    public void update(Scooter scooter) {
        scooterRepository.update(scooter);
        log.info("Сущность самоката с ID {} успешно обновлена напрямую", scooter.getId());
    }

    public void deleteScooterById(Long scooterId) {
        scooterRepository.deleteById(scooterId);
        log.info("Самокат с ID {} успешно удален из базы", scooterId);
    }

    // установка точки
    private void assignRentalPoint(Scooter scooter, Long rentalPointId) {
        if (null == rentalPointId) {
            log.info("Метод assignRentalPoint вызван с null ID, пропуск");
            return;
        }

        RentalPoint point = validateRentalPointForScooter(rentalPointId);
        scooter.setRentalPoint(point);
    }

    // есть ли у точки дочерние элементы
    private RentalPoint validateRentalPointForScooter(Long rentalPointId) {
        RentalPoint point = rentalPointService.findRentalPointById(rentalPointId);

        if (rentalPointService.getAddressLevel(point) != 3) {
            throw new BusinessException("Самокат можно привязать только к конечной точке проката");
        }
        return point;
    }

    private void validateSerialNumberUniqueness(String serialNumber) {
        Optional<Scooter> existingScooter = scooterRepository.findBySerialNumber(serialNumber);
        if (existingScooter.isPresent()) {
            throw new BusinessException("Самокат с серийным номером " + serialNumber + " уже существует в базе");
        }
    }
}
