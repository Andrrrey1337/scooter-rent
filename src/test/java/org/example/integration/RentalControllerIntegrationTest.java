package org.example.integration;

import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.StartRentalDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RentalControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @WithUserDetails(value = "testuser", setupBefore = TestExecutionEvent.TEST_EXECUTION) // пользователя уже создали
    @DisplayName("Интеграционный тест: Старт аренды")
    @Sql(scripts = "/sql/insert_rentals.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void startRental_ReturnsCreated() throws Exception {

        // данные для POST-запроса
        StartRentalDto startDto = new StartRentalDto();
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);
        startDto.setUserId(1L);

        String expectedJson = Files.readString(Paths.get("src/test/resources/examples/expected_rental_start.json"));

        mockMvc.perform(post("/api/rentals/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDto))) // dto в json
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
    }

    @Test
    @WithUserDetails(value = "finisher", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Интеграционный тест: Завершение аренды")
    @Sql(scripts = "/sql/insert_rentals.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void finishRental_ReturnsOk() throws Exception {

        // данные поездки для завершения
        FinishRentalDto finishDto = new FinishRentalDto();
        finishDto.setEndLatitude(new BigDecimal("53.9000"));
        finishDto.setEndLongitude(new BigDecimal("27.5600"));
        finishDto.setDistance(new BigDecimal("2.5"));
        finishDto.setBatteryLevel(45);

        String expectedJson = Files.readString(Paths.get("src/test/resources/examples/expected_rental_finish.json"));

        mockMvc.perform(post("/api/rentals/1/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finishDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
    }
}