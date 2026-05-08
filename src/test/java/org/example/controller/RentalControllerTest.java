package org.example.controller;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.RentalAdminResponseDto;
import org.example.dto.rental.RentalResponseDto;
import org.example.dto.rental.StartRentalDto;
import org.example.entity.Rental;
import org.example.mapper.RentalMapper;
import org.example.service.RentalService;
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

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalControllerTest extends BaseControllerTest {

    @MockitoBean
    private RentalService rentalService;

    private Rental rental;
    private RentalResponseDto responseDto;

    @BeforeEach
    void setUp() {
        rental = new Rental();
        rental.setId(1L);

        responseDto = new RentalResponseDto();
        responseDto.setId(1L);
    }

    @Test
    @DisplayName("POST /api/rentals/start - Начать аренду")
    void startRental_ReturnsCreated() throws Exception {
        StartRentalDto startDto = new StartRentalDto();
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);
        startDto.setUserId(1L);

        when(rentalService.startRental(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/rentals/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/rentals/{id}/finish - Завершить аренду")
    void finishRental_ReturnsOk() throws Exception {
        FinishRentalDto finishDto = new FinishRentalDto();
        finishDto.setEndLatitude(new BigDecimal("53.9100"));
        finishDto.setEndLongitude(new BigDecimal("27.5700"));
        finishDto.setDistance(new BigDecimal("5.5"));
        finishDto.setBatteryLevel(80);


        when(rentalService.finishRental(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/rentals/1/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finishDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/rentals/my - Моя история")
    void getMyRentals_ReturnsOk() throws Exception {
        when(rentalService.findRentalsByUserId(1L)).thenReturn(Collections.singletonList(responseDto));

        mockMvc.perform(get("/api/rentals/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/rentals/user/{userId} - История пользователя (Админ)")
    void getUserRentals_ReturnsOk() throws Exception {
        when(rentalService.findRentalsByUserId(1L)).thenReturn(Collections.singletonList(responseDto));

        mockMvc.perform(get("/api/rentals/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/rentals/scooter/{scooterId} - История самоката (Админ)")
    void getScooterRentals_ReturnsOk() throws Exception {
        RentalAdminResponseDto adminResponseDto = new RentalAdminResponseDto();
        when(rentalService.findRentalsByScooterId(1L)).thenReturn(Collections.singletonList(adminResponseDto));

        mockMvc.perform(get("/api/rentals/scooter/1"))
                .andExpect(status().isOk());
    }
}
