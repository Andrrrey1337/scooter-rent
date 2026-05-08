package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointDataDto;
import org.example.dto.point.RentalPointResponseDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.entity.RentalPoint;
import org.example.entity.Scooter;
import org.example.entity.ScooterStatus;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.RentalPointMapper;
import org.example.mapper.ScooterMapper;
import org.example.repository.RentalPointRepository;
import org.example.repository.ScooterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RentalPointService {
    private final RentalPointRepository rentalPointRepository;
    private final ScooterRepository scooterRepository;
    private final RentalPointMapper rentalPointMapper;
    private final ScooterMapper scooterMapper;

    public RentalPointResponseDto createRentalPoint(RentalPointCreateDto dto) {
        validateNameUniqueness(dto.getName());
        
        RentalPoint rentalPoint = rentalPointMapper.toEntity(dto);
        processHierarchy(rentalPoint, dto.getParentId());

        rentalPoint = rentalPointRepository.create(rentalPoint);
        log.info("Успешно создана новая точка проката: ID={}, название='{}'", rentalPoint.getId(), rentalPoint.getName());

        return rentalPointMapper.toDto(rentalPoint);
    }

    public List<RentalPointResponseDto> createRentalPointsBatch(List<RentalPointCreateDto> dtos) {
        log.info("Начато пакетное создание точек проката. Количество: {}", dtos.size());
        List<RentalPointResponseDto> savedPoints = dtos.stream().map(this::createRentalPoint).toList();
        log.info("Успешно завершено пакетное создание {} точек проката", savedPoints.size());
        return savedPoints;
    }

    public RentalPointResponseDto updateRentalPoint(Long id, RentalPointUpdateDto dto) {
        RentalPoint rentalPoint = findRentalPointById(id);
        
        if (dto.getName() != null && !dto.getName().equals(rentalPoint.getName())) {
            validateNameUniqueness(dto.getName());
        }
        
        rentalPointMapper.updateEntity(dto, rentalPoint);
        processHierarchy(rentalPoint, dto.getParentId());

        log.info("Данные точки проката с ID {} успешно обновлены", rentalPoint.getId());
        return rentalPointMapper.toDto(rentalPoint);
    }

    // реализация иерархии точек
    private void processHierarchy(RentalPoint rentalPoint, Long parentId) {
        RentalPoint parent = resolveParentPoint(rentalPoint, parentId);
        if (parent == null) {
            validateRootPoint(rentalPoint);
            validateCoordinates(rentalPoint, 1);
            return;
        }

        validateAndApplyHierarchy(rentalPoint, parent);
        int level = getAddressLevel(rentalPoint);
        validateCoordinates(rentalPoint, level);
    }

    private RentalPoint resolveParentPoint(RentalPoint rentalPoint, Long parentId) {
        if (parentId != null) {
            return findRentalPointById(parentId);
        }
        return rentalPoint.getParent();
    }

    private void validateCoordinates(RentalPoint point, int level) {
        if (level == 3) {
            if (point.getLatitude() == null || point.getLongitude() == null) {
                throw new BusinessException("Для точки уровня 'Дом' широта и долгота обязательны");
            }
        }
    }

    // проверка связи родителя и ребенка
    private void validateAndApplyHierarchy(RentalPoint child, RentalPoint parent) {
        applyInheritance(child, parent);
        validateAddressLevels(child, parent);
        validateAddressConsistency(child, parent);

        child.setParent(parent);
    }

    // заполнение данных (если не указаны) из родителя
    private void applyInheritance(RentalPoint child, RentalPoint parent) {
        if (isFieldBlank(child.getCity())) {
            child.setCity(parent.getCity());
        }
        if (isFieldBlank(child.getStreet())) {
            child.setStreet(parent.getStreet());
        }
    }

    // проверка последовательности уровней
    private void validateAddressLevels(RentalPoint child, RentalPoint parent) {
        int childLevel = getAddressLevel(child);
        int parentLevel = getAddressLevel(parent);

        if (childLevel == -1 || parentLevel == -1) {
            throw new BusinessException("Нарушена целостность адреса");
        }

        if (childLevel != parentLevel + 1) {
            throw new BusinessException(String.format(
                    "Нарушена последовательность: уровень %d не может быть дочерним для %d. " +
                    "Ожидается строго: Город -> Улица -> Дом", childLevel, parentLevel));
        }
    }

    // если у точки нет родителя, она должна быть городом
    private void validateRootPoint(RentalPoint point) {
        if (getAddressLevel(point) != 1) {
            throw new BusinessException("Точка без родителя должна быть уровня 'Город' (указан только город)");
        }
    }

    // проверка данных точки с данными родителя
    private void validateAddressConsistency(RentalPoint child, RentalPoint parent) {
        if (parent.getCity() != null && !child.getCity().equalsIgnoreCase(parent.getCity())) {
            throw new BusinessException("Город дочерней точки не совпадает с городом родителя");
        }
        if (parent.getStreet() != null && !child.getStreet().equalsIgnoreCase(parent.getStreet())) {
            throw new BusinessException("Улица дочерней точки не совпадает с улицей родителя");
        }
    }

    // определяет вес адреса
    @Transactional(readOnly = true)
    public int getAddressLevel(RentalPoint point) {
        boolean hasCity = !isFieldBlank(point.getCity());
        boolean hasStreet = !isFieldBlank(point.getStreet());
        boolean hasHouse = !isFieldBlank(point.getHouseNumber());

        if (hasHouse) return (hasCity && hasStreet) ? 3 : -1;
        if (hasStreet) return hasCity ? 2 : -1;
        if (hasCity) return 1;
        
        return 0;
    }

    private boolean isFieldBlank(String field) {
        return field == null || field.isBlank();
    }

    // проверка дублирования имен точек
    private void validateNameUniqueness(String name) {
        if (rentalPointRepository.findRentalPointByName(name).isPresent()) {
            throw new BusinessException("Точка проката с названием " + name + " уже существует");
        }
    }

    @Transactional(readOnly = true)
    public RentalPoint findRentalPointById(Long id) {
        if (id == null) return null;
        return rentalPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Точка проката с ID " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public RentalPoint findRentalPointByName(String name) {
        return rentalPointRepository.findRentalPointByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Точка проката с названием " + name + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<RentalPointResponseDto> findAllRentalPoints() {
        return rentalPointMapper.toDtos(rentalPointRepository.findAll());
    }

    public void deleteById(Long id) {
        rentalPointRepository.deleteById(id);
        log.info("Точка проката с ID {} успешно удалена", id);
    }

    @Transactional(readOnly = true)
    public List<ScooterAdminResponseDto> findAllScootersAtRentalPoint(Long rentalPointId) {
        findRentalPointById(rentalPointId);
        return scooterMapper.toAdminDtos(scooterRepository.findAllByRentalPoint(rentalPointId));
    }

    @Transactional(readOnly = true)
    public RentalPointDataDto getRentalPointDataById(Long id) {
        RentalPoint point = findRentalPointById(id);
        List<Scooter> allScooters = scooterRepository.findAllByRentalPoint(id);
        
        return buildRentalPointDataDto(point, allScooters);
    }

    @Transactional(readOnly = true)
    public RentalPointResponseDto getRentalPointDtoById(Long id) {
        return rentalPointMapper.toDto(findRentalPointById(id));
    }

    @Transactional(readOnly = true)
    public RentalPointResponseDto getRentalPointDtoByName(String name) {
        return rentalPointMapper.toDto(findRentalPointByName(name));
    }

    private RentalPointDataDto buildRentalPointDataDto(RentalPoint point, List<Scooter> scooters) {
        List<Scooter> available = getAvailableScooters(scooters);
        long rentedCount = countRentedScooters(scooters);
        Map<String, Long> modelsSummary = buildAvailableModelsSummary(available);
        List<ScooterAdminResponseDto> availableScooters = scooterMapper.toAdminDtos(available);

        return createRentalPointDataDto(
                point,
                scooters.size(),
                available.size(),
                rentedCount,
                modelsSummary,
                availableScooters
        );
    }

    private RentalPointDataDto createRentalPointDataDto(RentalPoint point,
                                                        int totalScooters,
                                                        int availableScootersCount,
                                                        long rentedScootersCount,
                                                        Map<String, Long> availableModelsSummary,
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
                .availableModelsSummary(availableModelsSummary)
                .availableScootersList(availableScooters)
                .build();
    }

    private List<Scooter> getAvailableScooters(List<Scooter> scooters) {
        return scooters.stream()
                .filter(s -> s.getScooterStatus() == ScooterStatus.AVAILABLE)
                .toList();
    }

    private long countRentedScooters(List<Scooter> scooters) {
        return scooters.stream()
                .filter(s -> s.getScooterStatus() == ScooterStatus.RENTED)
                .count();
    }

    private Map<String, Long> buildAvailableModelsSummary(List<Scooter> availableScooters) {
        return availableScooters.stream()
                .collect(Collectors.groupingBy(s -> s.getScooterModel().getName(), Collectors.counting()));
    }
}
