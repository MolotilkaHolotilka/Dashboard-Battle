package ru.dashboardbattle.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.dashboardbattle.entity.Company;
import ru.dashboardbattle.entity.MoySkladIntegration;
import ru.dashboardbattle.entity.User;
import ru.dashboardbattle.repository.CompanyRepository;
import ru.dashboardbattle.repository.MoySkladIntegrationRepository;
import ru.dashboardbattle.repository.UserRepository;

import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class ReportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CompanyRepository companyRepository;
    @Autowired MoySkladIntegrationRepository moySkladIntegrationRepository;

    private Long companyId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("report-test-" + System.nanoTime() + "@test.ru");
        user.setPasswordHash("hash");
        user = userRepository.save(user);

        Company company = new Company();
        company.setName("Report Test Co");
        company.setUser(user);
        company = companyRepository.save(company);
        companyId = company.getId();

        MoySkladIntegration ms = new MoySkladIntegration();
        ms.setCompany(company);
        ms.setTokenEncrypted("mock-token");
        ms.setStatus("ACTIVE");
        moySkladIntegrationRepository.save(ms);
    }

    @Test
    void requestTopN_shouldReturn200WithEntries() throws Exception {
        mockMvc.perform(post("/api/reports/top-n/request/" + companyId)
                        .param("topN", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.entries").isArray());
    }

    @Test
    void requestTopN_unknownCompany_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/reports/top-n/request/99999")
                        .param("topN", "5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void confirmTopN_shouldReturn200WithConfirmedStatus() throws Exception {
        // создаём отчёт
        String response = mockMvc.perform(post("/api/reports/top-n/request/" + companyId)
                        .param("topN", "3"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long reportId = objectMapper.readTree(response).get("id").asLong();

        // подтверждаем
        mockMvc.perform(post("/api/reports/top-n/" + reportId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void archiveTopN_shouldArchiveAndHideFromDefaultList() throws Exception {
        String response = mockMvc.perform(post("/api/reports/top-n/request/" + companyId)
                        .param("topN", "3"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long reportId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(post("/api/reports/top-n/" + reportId + "/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));

        mockMvc.perform(get("/api/reports/top-n")
                        .param("companyId", String.valueOf(companyId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void listReports_shouldReturnEmptyListForUnknownCompany() throws Exception {
        mockMvc.perform(get("/api/reports/top-n")
                        .param("companyId", "88888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
