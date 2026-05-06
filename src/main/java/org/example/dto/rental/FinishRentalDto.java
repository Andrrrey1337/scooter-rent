package org.example.dto.rental;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO для завершения аренды")
public class FinishRentalDto {
    @NotNull(message = "Конечная широта обязательна")
    @DecimalMin(value = "-90.0", message = "Широта должна быть от -90 до 90")
    @DecimalMax(value = "90.0", message = "Широта должна быть от -90 до 90")
    @Schema(description = "Широта в точке завершения", example = "53.9006")
    private BigDecimal endLatitude;

    @NotNull(message = "Долгота обязательна")
    @DecimalMin(value = "-180.0", message = "Долгота должна быть от -180 до 180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть от -180 до 180")
    @Schema(description = "Долгота в точке завершения", example = "27.5590")
    private BigDecimal endLongitude;

    @NotNull(message = "Расстояние (дистанция) обязательно")
    @DecimalMin(value = "0.0", message = "Дистанция не может быть отрицательной")
    @Schema(description = "Расстояние поездки, присланное самокатом (км)", example = "1.52")
    private BigDecimal distance;

    @NotNull(message = "Уровень заряда батареи обязателен")
    @Min(value = 0, message = "Уровень заряда не может быть меньше 0")
    @Max(value = 100, message = "Уровень заряда не может быть больше 100")
    @Schema(description = "Текущий уровень заряда батареи (%)", example = "85")
    private Integer batteryLevel;
}
