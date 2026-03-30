package ru.dashboardbattle.integration.real;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.dashboardbattle.dto.TopNReportDto;
import ru.dashboardbattle.exception.IntegrationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MoySkladClientHttpTest {

    @Test
    void fetchTopN_shouldMapRowsFromMoySkladResponse() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        MoySkladClientHttp client = new MoySkladClientHttp(restTemplate, "https://api.moysklad.ru/api/remap/1.2", new ObjectMapper());

        server.expect(requestTo("https://api.moysklad.ru/api/remap/1.2/entity/employee?limit=2"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(header(HttpHeaders.ACCEPT, "application/json;charset=utf-8"))
                .andExpect(header(HttpHeaders.ACCEPT_ENCODING, "gzip"))
                .andRespond(withSuccess("""
                        {
                          "rows": [
                            {"id":"emp-1","name":"Ivan"},
                            {"id":"emp-2","name":"Maria"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        TopNReportDto report = client.fetchTopN("test-token", 2);

        assertThat(report.getEntries()).hasSize(2);
        assertThat(report.getEntries().get(0).getEmployeeName()).isEqualTo("Ivan");
        assertThat(report.getEntries().get(1).getEmployeeIdMs()).isEqualTo("emp-2");
        server.verify();
    }

    @Test
    void fetchTopN_shouldFailWhenRowsAreEmpty() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        MoySkladClientHttp client = new MoySkladClientHttp(restTemplate, "https://api.moysklad.ru/api/remap/1.2", new ObjectMapper());

        server.expect(requestTo("https://api.moysklad.ru/api/remap/1.2/entity/employee?limit=3"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"rows\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchTopN("token", 3))
                .isInstanceOf(IntegrationException.class)
                .hasMessageContaining("пустой список");
    }
}
