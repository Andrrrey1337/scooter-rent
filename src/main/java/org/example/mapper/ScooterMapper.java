package org.example.mapper;

import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.dto.scooter.ScooterCreateDto;
import org.example.dto.scooter.ScooterResponseDto;
import org.example.dto.scooter.ScooterUpdateDto;
import org.example.entity.Scooter;
import org.example.service.ScooterModelService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

// если в dto поле будет null, тогда маппер не будет менять поле в entity
@Mapper(componentModel = "spring", uses = {ScooterModelService.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ScooterMapper {

    // @MappingTarget говорит что не надо создавать новый Scooter, надо просто обновить существующий
    // rentalPointId из dto попадет в RentalPointService и результат положится в поле rentalPoint нашего самоката
    @Mapping(target = "rentalPoint", ignore = true)
    void updateEntity(ScooterUpdateDto scooterUpdateDto, @MappingTarget Scooter scooter);

    @Mapping(target = "rentalPoint",  ignore = true)
    @Mapping(source = "modelId", target = "scooterModel")
    Scooter toEntity(ScooterCreateDto scooterCreateDto);

    @Mapping(source = "scooterModel.id", target = "modelId")
    @Mapping(source = "rentalPoint.id", target = "rentalPointId")
    @Mapping(source = "rentalPoint.name", target = "rentalPointName")
    @Mapping(source = "rentalPoint.city", target = "city")
    @Mapping(source = "rentalPoint.street", target = "street")
    @Mapping(source = "rentalPoint.houseNumber", target = "houseNumber")
    @Mapping(source = "scooterModel.name", target = "modelName")
    ScooterResponseDto toDto(Scooter scooter);

    List<ScooterResponseDto> toDtos(List<Scooter> scooters);

    @Mapping(source = "scooterModel.id", target = "modelId")
    @Mapping(source = "rentalPoint.id", target = "rentalPointId")
    @Mapping(source = "rentalPoint.name", target = "rentalPointName")
    @Mapping(source = "rentalPoint.city", target = "city")
    @Mapping(source = "rentalPoint.street", target = "street")
    @Mapping(source = "rentalPoint.houseNumber", target = "houseNumber")
    @Mapping(source = "scooterModel.name", target = "modelName")
    ScooterAdminResponseDto toAdminDto(Scooter scooter);

    List<ScooterAdminResponseDto> toAdminDtos(List<Scooter> scooters);
}
