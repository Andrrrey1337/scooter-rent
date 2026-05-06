package org.example.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.dto.scooter.ScooterAdminResponseDto;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@Schema(description = "DTO со статистическими данными по точке проката (админ)")
public class RentalPointDataDto {
    @Schema(description = "ID точки проката", example = "1")
    private Long rentalPointId;
    @Schema(description = "Название точки проката", example = "Центральный парк")
    private String rentalPointName;
    @Schema(description = "Город", example = "Минск")
    private String city;
    @Schema(description = "Улица", example = "пр-т Независимости")
    private String street;
    @Schema(description = "Номер дома", example = "1")
    private String houseNumber;
    @Schema(description = "Всего самокатов на точке", example = "25")
    private long totalScooters;
    @Schema(description = "Доступно самокатов для аренды", example = "10")
    private long availableScooters;
    @Schema(description = "Самокатов в аренде", example = "15")
    private long rentedScooters;
    @Schema(description = "Сводка по доступным моделям (название модели - количество)")
    private Map<String, Long> availableModelsSummary; // модели - кол-во
    @Schema(description = "Список доступных самокатов с детальной информацией")
    private List<ScooterAdminResponseDto> availableScootersList;
}
