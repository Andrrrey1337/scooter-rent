package org.example.dto.point;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "DTO для ответа с основной информацией о точке проката")
public class RentalPointResponseDto {
    @Schema(description = "ID точки проката", example = "1")
    private Long id;
    @Schema(description = "Название точки проката", example = "Центральный парк")
    private String name;
    @Schema(description = "Город", example = "Минск")
    private String city;
    @Schema(description = "Улица", example = "пр-т Независимости")
    private String street;
    @Schema(description = "Номер дома", example = "1")
    private String houseNumber;
    @Schema(description = "ID родительской точки проката", example = "1")
    private Long parentId;
    @Schema(description = "Широта", example = "53.9006")
    private BigDecimal latitude;
    @Schema(description = "Долгота", example = "27.5590")
    private BigDecimal longitude;
}
