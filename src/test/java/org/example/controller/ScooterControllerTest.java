package org.example.controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.dto.scooter.ScooterCreateDto;
import org.example.dto.scooter.ScooterResponseDto;
import org.example.dto.scooter.ScooterUpdateDto;
import org.example.entity.Scooter;
import org.example.mapper.ScooterMapper;
import org.example.service.ScooterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScooterController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScooterControllerTest extends BaseControllerTest {

    @MockitoBean
    private ScooterService scooterService;

    private Scooter scooter;
    private ScooterResponseDto scooterResponseDto;
    private ScooterAdminResponseDto scooterAdminResponseDto;

    @BeforeEach
    void setUp() {
        scooter = new Scooter();
        scooter.setId(1L);
        scooter.setSerialNumber("SN123");

        scooterResponseDto = ScooterResponseDto.builder()
                .id(1L)
                .serialNumber("SN123")
                .build();

        scooterAdminResponseDto = ScooterAdminResponseDto.builder()
                .id(1L)
                .serialNumber("SN123")
                .build();
    }

    @Test
    @DisplayName("POST /api/scooters - Создать самокат (Админ)")
    void createScooter_ReturnsCreated() throws Exception {
        ScooterCreateDto createDto = new ScooterCreateDto();
        createDto.setSerialNumber("SN123");
        createDto.setModelId(1L);
        createDto.setLatitude(new BigDecimal("53.9"));
        createDto.setLongitude(new BigDecimal("27.5"));

        when(scooterService.createScooter(any(ScooterCreateDto.class))).thenReturn(scooterAdminResponseDto);

        mockMvc.perform(post("/api/scooters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serialNumber").value("SN123"));
    }

    @Test
    @DisplayName("GET /api/scooters/{id} - По ID (Админ)")
    void getScooterById_ReturnsOk() throws Exception {
        when(scooterService.getScooterAdminDtoById(1L)).thenReturn(scooterAdminResponseDto);

        mockMvc.perform(get("/api/scooters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/scooters/number/{number} - По номеру")
    void getScooterByNumber_ReturnsOk() throws Exception {
        when(scooterService.getScooterDtoBySerialNumber("SN123")).thenReturn(scooterResponseDto);

        mockMvc.perform(get("/api/scooters/number/SN123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("SN123"));
    }

    @Test
    @DisplayName("DELETE /api/scooters/{id} - Удалить (Админ)")
    void deleteScooter_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/scooters/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/scooters/{id} - Обновить (Админ)")
    void updateScooter_ReturnsOk() throws Exception {
        ScooterUpdateDto updateDto = new ScooterUpdateDto();
        updateDto.setLatitude(new BigDecimal("53.9100"));
        updateDto.setLongitude(new BigDecimal("27.5700"));
        when(scooterService.updateScooter(eq(1L), any(ScooterUpdateDto.class))).thenReturn(scooterAdminResponseDto);

        mockMvc.perform(patch("/api/scooters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serialNumber").value("SN123"));
    }
}
