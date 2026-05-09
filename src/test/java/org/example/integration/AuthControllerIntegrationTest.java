package org.example.integration;

import org.example.dto.auth.JwtRequest;
import org.example.dto.user.UserCreateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Интеграционный тест: Регистрация нового пользователя")
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/auth_register.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerUser_ReturnsCreated() throws Exception {

        //  DTO для регистрации
        UserCreateDto createDto = new UserCreateDto();
        createDto.setUsername("newuser");
        createDto.setPassword("MySecretPassword123");

        String expectedJson = Files.readString(Paths.get("src/test/resources/examples/expected_register.json"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJson, JsonCompareMode.LENIENT));
    }

    @Test
    @DisplayName("Интеграционный тест: Логин существующего пользователя")
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/auth_login.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void login_ReturnsJwtToken() throws Exception {

        // dto для логина
        JwtRequest loginRequest = new JwtRequest();
        loginRequest.setUsername("existinguser");
        loginRequest.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                //  тк токен всегда разный,  проверяем не точное совпадение,
                //  а просто то, что поле "token" существует и является строкой
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}