package org.example.dto.scooter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.entity.ScooterStatus;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO с полной информацией о самокате (для админа)")
public class ScooterAdminResponseDto {
    @Schema(description = "ID самоката", example = "1")
    private Long id;
    @Schema(description = "Серийный номер", example = "SN12345678")
    private String serialNumber;
    @Schema(description = "ID модели", example = "1")
    private Long modelId;
    @Schema(description = "Название модели", example = "Xiaomi M365")
    private String modelName;
    @Schema(description = "ID текущей точки проката", example = "1")
    private Long rentalPointId;
    @Schema(description = "Название точки проката", example = "Центральный парк")
    private String rentalPointName;
    @Schema(description = "Город", example = "Минск")
    private String city;
    @Schema(description = "Улица", example = "пр-т Независимости")
    private String street;
    @Schema(description = "Номер дома", example = "1")
    private String houseNumber;
    @Schema(description = "Уровень заряда батареи", example = "80")
    private Integer batteryLevel;
    @Schema(description = "Текущая широта", example = "53.9006")
    private BigDecimal latitude;
    @Schema(description = "Текущая долгота", example = "27.5590")
    private BigDecimal longitude;
    @Schema(description = "Статус самоката", example = "AVAILABLE")
    private ScooterStatus scooterStatus;
    @Schema(description = "Общий пробег (км)", example = "5.5")
    private BigDecimal mileage;
}
