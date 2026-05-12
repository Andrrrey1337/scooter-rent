package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointResponseDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.entity.RentalPoint;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.RentalPointMapper;
import org.example.repository.RentalPointRepository;
import org.example.service.RentalPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RentalPointServiceImpl implements RentalPointService {
    private final RentalPointRepository rentalPointRepository;
    private final RentalPointMapper rentalPointMapper;

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
        List<RentalPointResponseDto> savedPoints = dtos.stream()
                .map(this::createRentalPoint)
                .toList();

        log.info("Успешно завершено пакетное создание {} точек проката", savedPoints.size());

        return savedPoints;
    }

    public RentalPointResponseDto updateRentalPoint(Long id, RentalPointUpdateDto dto) {
        RentalPoint rentalPoint = findRentalPointById(id);
        String name = dto.getName();
        
        if (isNotBlank(name) && notEqual(name, rentalPoint.getName())) {
            validateNameUniqueness(name);
        }
        
        rentalPointMapper.updateEntity(dto, rentalPoint);
        processHierarchy(rentalPoint, dto.getParentId());

        log.info("Данные точки проката с ID {} успешно обновлены", rentalPoint.getId());
        return rentalPointMapper.toDto(rentalPoint);
    }

    @Transactional(readOnly = true)
    public int getAddressLevel(RentalPoint point) {
        boolean hasCity = isBlank(point.getCity());
        boolean hasStreet = isBlank(point.getStreet());
        boolean hasHouse = isBlank(point.getHouseNumber());

        if (hasHouse) return (hasCity && hasStreet) ? 3 : -1;
        if (hasStreet) return hasCity ? 2 : -1;
        if (hasCity) return 1;

        return 0;
    }

    @Transactional(readOnly = true)
    public RentalPoint findRentalPointById(Long id) {
        if (isNull(id)) {
            log.info("Вызван findRentalPointById с null ID, возвращаем null");
            return null;
        }
        return rentalPointRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Точка проката с ID " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<RentalPointResponseDto> findAllRentalPoints() {
        return rentalPointMapper.toDtos(rentalPointRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<RentalPoint> findNearestValidParkingPoint(BigDecimal latitude, BigDecimal longitude, double radius) {
        return rentalPointRepository.findNearestValidParkingPoint(latitude, longitude, radius);
    }

    public void deleteById(Long id) {
        rentalPointRepository.deleteById(id);
        log.info("Точка проката с ID {} успешно удалена", id);
    }

    @Transactional(readOnly = true)
    public RentalPointResponseDto getRentalPointDtoById(Long id) {
        return rentalPointMapper.toDto(findRentalPointById(id));
    }

    @Transactional(readOnly = true)
    public RentalPointResponseDto getRentalPointDtoByName(String name) {
        return rentalPointMapper.toDto(findRentalPointByName(name));
    }

    @Transactional(readOnly = true)
    public RentalPoint findRentalPointByName(String name) {
        return rentalPointRepository.findRentalPointByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Точка проката с названием " + name + " не найдена"));
    }

    private void processHierarchy(RentalPoint rentalPoint, Long parentId) {
        RentalPoint parent = resolveParentPoint(rentalPoint, parentId);
        if (null == parent) {
            log.info("Родительская точка не указана для '{}', валидация как корневой точки", rentalPoint.getName());
            validateRootPoint(rentalPoint);
            validateCoordinates(rentalPoint, 1);
            return;
        }

        validateAndApplyHierarchy(rentalPoint, parent);
        int level = getAddressLevel(rentalPoint);
        validateCoordinates(rentalPoint, level);
    }

    private RentalPoint resolveParentPoint(RentalPoint rentalPoint, Long parentId) {
        if (null != parentId) {
            return findRentalPointById(parentId);
        }
        return rentalPoint.getParent();
    }

    private void validateCoordinates(RentalPoint point, int level) {
        if (3 == level) {
            if (isNull(point.getLatitude()) || isNull(point.getLongitude())) {
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
        String parentCity = parent.getCity();
        String parentStreet = parent.getStreet();

        if (isBlank(child.getCity())) {
            log.info("Наследование города '{}' от родительской точки ID={}", parentCity, parent.getId());
            child.setCity(parentCity);
        }
        if (isBlank(child.getStreet())) {
            log.info("Наследование улицы '{}' от родительской точки ID={}", parentStreet, parent.getId());
            child.setStreet(parentStreet);
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
        if (1 != getAddressLevel(point)) {
            throw new BusinessException("Точка без родителя должна быть уровня 'Город' (указан только город)");
        }
    }

    private void validateAddressConsistency(RentalPoint child, RentalPoint parent) {
        String parentCity = parent.getCity();
        String parentStreet = parent.getStreet();

        if (nonNull(parentCity) && isFalse(parentCity.equalsIgnoreCase(child.getCity()))) {
            throw new BusinessException("Город дочерней точки не совпадает с городом родителя");
        }
        if (nonNull(parentStreet) && isFalse(parentStreet.equalsIgnoreCase(child.getStreet()))) {
            throw new BusinessException("Улица дочерней точки не совпадает с улицей родителя");
        }
    }


    private void validateNameUniqueness(String name) {
        Optional<RentalPoint> existingPoint = rentalPointRepository.findRentalPointByName(name);
        if (existingPoint.isPresent()) {
            throw new BusinessException("Точка проката с названием " + name + " уже существует");
        }
    }
}



