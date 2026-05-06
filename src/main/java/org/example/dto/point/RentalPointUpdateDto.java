package org.example.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO для обновления данных точки проката")
public class RentalPointUpdateDto {
    @Size(max = 50, message = "Название не должно превышать 50 символов")
    @Schema(description = "Новое название точки проката", example = "Центральный парк")
    private String name;

    @Size(max = 50, message = "Название города не должно превышать 50 символов")
    @Schema(description = "Новое название города", example = "Минск")
    private String city;

    @Size(max = 50, message = "Название улицы не должно превышать 50 символов")
    @Schema(description = "Новое название улицы", example = "пр-т Независимости")
    private String street;

    @Size(max = 10, message = "Номер дома не должен превышать 10 символов")
    @Schema(description = "Новый номер дома", example = "1")
    private String houseNumber;

    @Schema(description = "Новый ID родительской точки", example = "1")
    private Long parentId;

    @DecimalMin(value = "-90.0", message = "Широта должна быть от -90 до 90")
    @DecimalMax(value = "90.0", message = "Широта должна быть от -90 до 90")
    @Schema(description = "Новая широта", example = "53.9006")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Долгота должна быть от -180 до 180")
    @DecimalMax(value = "180.0", message = "Долгота должна быть от -180 до 180")
    @Schema(description = "Новая долгота", example = "27.5590")
    private BigDecimal longitude;
}
