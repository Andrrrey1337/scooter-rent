package org.example.service.impl;

import org.example.service.*;
import org.example.service.ScooterModelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.scooterModel.ScooterModelCreateDto;
import org.example.dto.scooterModel.ScooterModelResponseDto;
import org.example.dto.scooterModel.ScooterModelUpdateDto;
import org.example.entity.ScooterModel;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.ScooterModelMapper;
import org.example.repository.ScooterModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.ObjectUtils.notEqual;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ScooterModelServiceImpl implements ScooterModelService {
    private final ScooterModelRepository scooterModelRepository;
    private final ScooterModelMapper scooterModelMapper;

    public ScooterModelResponseDto createScooterModel(ScooterModelCreateDto dto) {
        ScooterModel scooterModel = scooterModelMapper.toEntity(dto);
        if (scooterModelRepository.findByName(scooterModel.getName()).isPresent()) {
            throw new BusinessException("Модель самоката с названием '" + scooterModel.getName() + "' уже существует");
        }

        ScooterModel scooterModelNew = scooterModelRepository.create(scooterModel);

        log.info("Успешно добавлена новая модель самоката: ID={}, название='{}'",
                scooterModelNew.getId(), scooterModelNew.getName());

        return scooterModelMapper.toDto(scooterModelNew);
    }

    @Transactional(readOnly = true)
    public ScooterModel findScooterModelById(Long id) {
        ScooterModel model = scooterModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Модель самоката с ID " + id + " не найдена"));

        log.info("Успешно найдена модель самоката с ID: {}", id);

        return model;
    }

    @Transactional(readOnly = true)
    public ScooterModelResponseDto getScooterModelDtoById(Long id) {
        return scooterModelMapper.toDto(findScooterModelById(id));
    }

    @Transactional(readOnly = true)
    public List<ScooterModelResponseDto> findAllScooterModels() {
        List<ScooterModel>  scooterModels = scooterModelRepository.findAll();

        log.info("Получен список всех моделей самокатов. Количество записей: {}", scooterModels.size());

        return scooterModelMapper.toDtos(scooterModels);
    }

    public ScooterModelResponseDto updateScooterModel(Long id, ScooterModelUpdateDto scooterModelUpdateDto) {
        ScooterModel scooterModel = findScooterModelById(id);

        if (isNotBlank(scooterModelUpdateDto.getName()) && notEqual(scooterModelUpdateDto.getName(), scooterModel.getName())) {
            log.info("Обновление названия модели самоката с '{}' на '{}'", scooterModel.getName(), scooterModelUpdateDto.getName());
            if (scooterModelRepository.findByName(scooterModelUpdateDto.getName()).isPresent()) {
                throw new BusinessException("Модель самоката с названием '" + scooterModelUpdateDto.getName() + "' уже существует");
            }
        } else {
            log.info("Название модели самоката не предоставлено или не изменилось, пропуск валидации имени");
        }

        scooterModelMapper.updateEntity(scooterModelUpdateDto, scooterModel);

        log.info("Данные модели самоката с ID {} успешно обновлены", scooterModel.getId());

        return scooterModelMapper.toDto(scooterModel);
    }

    public void deleteScooterModelById(Long id) {
        scooterModelRepository.deleteById(id);
        log.info("Модель самоката с ID {} успешно удалена", id);
    }
}
