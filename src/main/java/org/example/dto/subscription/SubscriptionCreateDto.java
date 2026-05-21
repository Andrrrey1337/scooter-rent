package org.example.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO для создания нового абонемента")
public class SubscriptionCreateDto {

    @NotBlank(message = "Название обязательно")
    @Size(max = 50, message = "Название не должно превышать 50 символов")
    @Schema(description = "Название абонемента", example = "Месяц фристартов")
    private String name;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
    @Schema(description = "Стоимость абонемента", example = "399.00")
    private BigDecimal price;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть минимум 1 день")
    @Schema(description = "Длительность абонемента в днях", example = "30")
    private Integer durationDays;

    @NotNull(message = "Укажите количество включенных минут (можно 0)")
    @Min(value = 0, message = "Минуты не могут быть отрицательными")
    @Schema(description = "Пакет бесплатных минут на поездки", example = "0")
    private Integer includeMinutes;

    @NotNull(message = "Укажите, включен ли бесплатный старт")
    @Schema(description = "Дает ли абонемент бесплатный старт", example = "true")
    private Boolean isFreeStart;
}