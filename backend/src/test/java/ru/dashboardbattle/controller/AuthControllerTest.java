package ru.dashboardbattle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void register_shouldReturn200AndIds() throws Exception {
        var body = Map.of(
                "email", "smoke@test.ru",
                "password", "secret123",
                "companyName", "Smoke Inc"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.companyId").isNumber())
                .andExpect(jsonPath("$.email").value("smoke@test.ru"))
                .andExpect(jsonPath("$.companyName").value("Smoke Inc"));
    }

    @Test
    void register_duplicateEmail_shouldReturn409() throws Exception {
        var body = Map.of(
                "email", "dup@test.ru",
                "password", "secret",
                "companyName", "First"
        );

        // первый запрос — успех
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        // второй — конфликт
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }
}
