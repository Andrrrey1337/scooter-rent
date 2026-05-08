package org.example.controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.example.dto.scooterModel.ScooterModelCreateDto;
import org.example.dto.scooterModel.ScooterModelResponseDto;
import org.example.dto.scooterModel.ScooterModelUpdateDto;
import org.example.entity.ScooterModel;
import org.example.mapper.ScooterModelMapper;
import org.example.service.ScooterModelService;
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

@WebMvcTest(ScooterModelController.class)
@AutoConfigureMockMvc(addFilters = false)
class ScooterModelControllerTest extends BaseControllerTest {

    @MockitoBean
    private ScooterModelService scooterModelService;

    private ScooterModel scooterModel;
    private ScooterModelResponseDto responseDto;

    @BeforeEach
    void setUp() {
        scooterModel = new ScooterModel();
        scooterModel.setId(1L);
        scooterModel.setName("Model X");

        responseDto = new ScooterModelResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Model X");
    }

    @Test
    @DisplayName("POST /api/scooter-models - Создать модель (Админ)")
    void createScooterModel_ReturnsCreated() throws Exception {
        ScooterModelCreateDto createDto = new ScooterModelCreateDto();
        createDto.setName("Model X");
        createDto.setPricePerMinute(new BigDecimal("5.00"));

        when(scooterModelService.createScooterModel(any(ScooterModelCreateDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/scooter-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Model X"));
    }

    @Test
    @DisplayName("GET /api/scooter-models/{id} - По ID")
    void getScooterModelById_ReturnsOk() throws Exception {
        when(scooterModelService.getScooterModelDtoById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/scooter-models/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/scooter-models - Все модели")
    void getAllScooterModels_ReturnsOk() throws Exception {
        when(scooterModelService.findAllScooterModels()).thenReturn(Collections.singletonList(responseDto));

        mockMvc.perform(get("/api/scooter-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Model X"));
    }

    @Test
    @DisplayName("PATCH /api/scooter-models/{id} - Обновить (Админ)")
    void updateScooterModel_ReturnsOk() throws Exception {
        ScooterModelUpdateDto updateDto = new ScooterModelUpdateDto();
        updateDto.setName("New Name");

        when(scooterModelService.updateScooterModel(eq(1L), any(ScooterModelUpdateDto.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/scooter-models/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/scooter-models/{id} - Удалить (Админ)")
    void deleteScooterModel_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/scooter-models/1"))
                .andExpect(status().isNoContent());
    }
}
