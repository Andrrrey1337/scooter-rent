package org.example.controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.example.dto.tariff.TariffCreateDto;
import org.example.dto.tariff.TariffResponseDto;
import org.example.dto.tariff.TariffUpdateDto;
import org.example.service.TariffService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TariffController.class)
@AutoConfigureMockMvc(addFilters = false) // отключаем Security для тестов контроллера
class TariffControllerTest extends BaseControllerTest {

    @MockitoBean
    private TariffService tariffService;
    private TariffResponseDto responseDto;

    @BeforeEach
    void setUp() {
        responseDto = new TariffResponseDto(1L, "Standard", "Description", BigDecimal.valueOf(10.0));
    }

    @Test
    @DisplayName("POST /api/tariffs - Успешное создание")
    void createTariff_Success() throws Exception {
        TariffCreateDto createDto = new TariffCreateDto();
        createDto.setName("Standard");
        createDto.setPrice(BigDecimal.valueOf(10.0));
        createDto.setDescription("Description");

        when(tariffService.createTariff(any(TariffCreateDto.class))).thenReturn(responseDto);
        mockMvc.perform(post("/api/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Standard"));
    }

    @Test
    @DisplayName("GET /api/tariffs/{id} - Получение по ID")
    void getTariffById_Success() throws Exception {
        when(tariffService.getTariffDtoById(1L)).thenReturn(responseDto);
        mockMvc.perform(get("/api/tariffs/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Standard"));
    }

    @Test
    @DisplayName("GET /api/tariffs/name/{name} - Получение по названию")
    void getTariffByName_Success() throws Exception {
        when(tariffService.getTariffDtoByName("Standard")).thenReturn(responseDto);

        mockMvc.perform(get("/api/tariffs/name/{name}", "Standard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard"));
    }

    @Test
    @DisplayName("GET /api/tariffs - Получение всех тарифов")
    void getAllTariffs_Success() throws Exception {
        when(tariffService.findAllTariffs()).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/api/tariffs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Standard"));
    }

    @Test
    @DisplayName("PATCH /api/tariffs/{id} - Обновление тарифа")
    void updateTariff_Success() throws Exception {
        TariffUpdateDto updateDto = new TariffUpdateDto();
        updateDto.setName("Updated Name");

        when(tariffService.updateTariff(eq(1L), any(TariffUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/tariffs/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/tariffs/{id} - Удаление тарифа")
    void deleteTariff_Success() throws Exception {
        doNothing().when(tariffService).deleteTariffById(1L);

        mockMvc.perform(delete("/api/tariffs/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(tariffService, times(1)).deleteTariffById(1L);
    }
}