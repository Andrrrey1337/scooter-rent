package org.example.integration;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TariffControllerIntegrationTest extends BaseIntegrationTest {
    @Test
    @DisplayName("Интеграционный тест: Получение всех тарифов")
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/tariffs_all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getAllTariffs_ReturnsCorrectJson() throws Exception {
        String expectedJson = Files.readString(Paths.get("src/test/resources/examples/expected_tariffs.json"));

        mockMvc.perform(get("/api/tariffs").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // проверяем кодировку
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT)); // неважен порядок элементов в массие
    }

}
