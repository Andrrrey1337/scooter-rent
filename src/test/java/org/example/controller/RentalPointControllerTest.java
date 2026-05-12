package org.example.controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.example.dto.point.RentalPointCreateDto;
import org.example.dto.point.RentalPointDataDto;
import org.example.dto.point.RentalPointResponseDto;
import org.example.dto.point.RentalPointUpdateDto;
import org.example.dto.scooter.ScooterAdminResponseDto;
import org.example.entity.RentalPoint;
import org.example.service.RentalPointFacade;
import org.example.service.RentalPointService;
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

@WebMvcTest(RentalPointController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalPointControllerTest extends BaseControllerTest {

    @MockitoBean
    private RentalPointService rentalPointService;

    @MockitoBean
    private ScooterService scooterService;

    @MockitoBean
    private RentalPointFacade rentalPointFacade;

    private RentalPoint rentalPoint;
    private RentalPointResponseDto responseDto;

    @BeforeEach
    void setUp() {
        rentalPoint = new RentalPoint();
        rentalPoint.setId(1L);
        rentalPoint.setName("Point A");

        responseDto = new RentalPointResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Point A");
    }

    @Test
    @DisplayName("POST /api/points - Создать точку (Админ)")
    void createRentalPoint_ReturnsCreated() throws Exception {
        RentalPointCreateDto createDto = new RentalPointCreateDto();
        createDto.setName("Point A");
        createDto.setLatitude(new BigDecimal("53.9000"));
        createDto.setLongitude(new BigDecimal("27.5667"));

        when(rentalPointService.createRentalPoint(any(RentalPointCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Point A"));
    }

    @Test
    @DisplayName("GET /api/points/{id} - По ID")
    void getRentalPointById_ReturnsOk() throws Exception {
        when(rentalPointService.getRentalPointDtoById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/points/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/points/name/{name} - По названию")
    void getRentalPointByName_ReturnsOk() throws Exception {
        when(rentalPointService.getRentalPointDtoByName("Point A")).thenReturn(responseDto);

        mockMvc.perform(get("/api/points/name/Point A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Point A"));
    }

    @Test
    @DisplayName("GET /api/points - Все точки")
    void getAllRentalPoints_ReturnsOk() throws Exception {
        when(rentalPointService.findAllRentalPoints()).thenReturn(Collections.singletonList(responseDto));

        mockMvc.perform(get("/api/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Point A"));
    }

    @Test
    @DisplayName("PATCH /api/points/{id} - Обновить (Админ)")
    void updateRentalPoint_ReturnsOk() throws Exception {
        RentalPointUpdateDto updateDto = new RentalPointUpdateDto();
        updateDto.setName("New Name");

        when(rentalPointService.updateRentalPoint(eq(1L), any(RentalPointUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/points/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/points/{id} - Удалить (Админ)")
    void deleteRentalPoint_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/points/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/points/scooters/{id} - Доступные самокаты на точке")
    void getScootersAtPointById_ReturnsOk() throws Exception {
        ScooterResponseDto scooterDto = new ScooterResponseDto();
        scooterDto.setSerialNumber("SN123");
        when(scooterService.findAvailableScooters(eq(1L), any())).thenReturn(Collections.singletonList(scooterDto));

        mockMvc.perform(get("/api/points/scooters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serialNumber").value("SN123"));
    }

    @Test
    @DisplayName("GET /api/points/data/{id} - Детальная статистика (Админ)")
    void getRentalPointDataById_ReturnsOk() throws Exception {
        RentalPointDataDto dataDto = RentalPointDataDto.builder()
                .rentalPointId(1L)
                .rentalPointName("Point A")
                .build();

        when(rentalPointFacade.getRentalPointDataById(1L)).thenReturn(dataDto);

        mockMvc.perform(get("/api/points/data/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentalPointId").value(1L));
    }
}
